# AbstractQueuedSynchronizer

## 属性

**简单属性**

```java
// 同步器的状态，子类会根据状态字段进行判断是否可以获得锁
// 比如 CAS 成功给 state 赋值 1 算得到锁，失败为得不到锁， CAS 成功给 state 赋值 0 算释放锁，失败为释放失败
// 可重入锁，每次获得锁 +1，每次释放锁 -1
private volatile int state;

// 自旋超时阀值，单位纳秒
// 当设置等待时间时才会用到这个属性
static final long spinForTimeoutThreshold = 1000L;
```

**同步队列属性**

所谓同步队列，就是指当多个线程请求锁的时候，在某一时刻有且只有一个线程能够获得锁，剩余线程都会加到同步队列中排队并阻塞。当有线程主动释放锁时，就会从同步队列队首开始释放一个排队的线程，让线程重新去竞争锁。

```java
// 同步队列的头。
private transient volatile Node head;

// 同步队列的尾
private transient volatile Node tail;
```

**条件队列属性**

条件队列和同步队列的功能一样，管理获取步到锁的线程，但是条件队列不直接和锁打交道，但经常和锁配合使用。

```java
public class ConditionObject implements Condition, java.io.Serializable {
    private static final long serialVersionUID = 1173984872572414699L;
    // 条件队列中第一个 node
    private transient Node firstWaiter;
    // 条件队列中最后一个 node
    private transient Node lastWaiter;
}
```

**Node**

```java
static final class Node {
    /**
     * 同步队列单独的属性
     */
    //node 是共享模式
    static final Node SHARED = new Node();

    //node 是排它模式
    static final Node EXCLUSIVE = null;

    // 当前节点的前驱节点
    // 节点 acquire 成功后就会变成head
    // head 节点不能被 cancelled
    volatile Node prev;

    // 当前节点的下一个节点
    volatile Node next;

    /**
     * 两个队列共享的属性
     */
    // 表示当前节点的状态，通过节点的状态来控制节点的行为
    // 普通同步节点，就是 0 ，条件节点是 CONDITION -2
    volatile int waitStatus;

    // waitStatus 的状态有以下几种
    // 被取消
    static final int CANCELLED =  1;

    // SIGNAL 状态的意义：同步队列中的节点在自旋获取锁的时候，如果前一个节点的状态是 SIGNAL，那么自己就可以阻塞休息了，否则自己一直自旋尝试获得锁
    static final int SIGNAL    = -1;

    // 表示当前 node 正在条件队列中，当有节点从同步队列转移到条件队列时，状态就会被更改成 CONDITION
    static final int CONDITION = -2;

    // 无条件传播,共享模式下，该状态的进程处于可运行状态
    static final int PROPAGATE = -3;

    // 当前节点的线程
    volatile Thread thread;

    // 在同步队列中，nextWaiter 并不真的是指向其下一个节点，我们用 next 表示同步队列的下一个节点，nextWaiter 只是表示当前 Node 是排它模式还是共享模式
    // 但在条件队列中，nextWaiter 就是表示下一个节点元素
    Node nextWaiter;
}
```

## 独占锁

### acquire 排他锁

```java
public final void acquire(int arg) {
    // tryAcquire 由实现类实现，一般都是 CAS 给 state 赋值来决定是否能够获取到锁
    // 尝试以排他模式 CAS 加入同步队列队尾，然后将当前节点的前驱节点设置文 SIGNAL，然后阻塞自己
    if (!tryAcquire(arg) && 
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

在 `acquire` 方法里面，先尝试获取锁，如果获取锁失败，尝试将线程放进同步队列中。放入同步队列的过程有两步：1. 调用 `addWaiter` 把当前线程放到同步队列队尾。2. 调用 `acquireQueued`，使当前节点阻塞

`addWaiter` 的逻辑很简单，就是将当前节点插入到同步队列的队尾

```java
private Node addWaiter(Node mode) {
    Node node = new Node(Thread.currentThread(), mode);
    // Try the fast path of enq; backup to full enq on failure
    Node pred = tail;
    if (pred != null) {
        node.prev = pred;
        // CAS 将 node 作为尾节点，因为 CAS 大多会一次成功，如果失败了再进行自旋
        if (compareAndSetTail(pred, node)) { 
            pred.next = node; 
            return node;
        }
    }
    // prev 为空，或者 CAS 失败
    // 自旋，采用 CAS 尝试将 当前节点加入到队尾
    enq(node); 
    return node;
}

private Node enq(final Node node) {
    for (;;) {
        Node t = tail;
        // 如果为空表示同步队列还没初始化，进行初始化
        if (t == null) { 
            if (compareAndSetHead(new Node()))
                tail = head;
        } else {
            // CAS 将 node 设置为尾节点
            node.prev = t;
            if (compareAndSetTail(t, node)) {
                t.next = node;
                return t;
            }
        }
    }
}
```

`addWaiter` 主要做了两件事情

* 将 mode 封装为一个 Node
* 利用 CAS + 自旋操作，将节点添加到同步队列队尾。如果头结点为空，先进行队列的初始化

```java
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            // 如果前驱节点就是根节点，就尝试获取锁，如果成功就把自己设置为头结点
            // p=head可能是因为在enq方法中队列才进行初始化，这时锁没有被占用。所以这时可以尝试获取锁
            // 也有可能是前驱节点持有锁，这是获取锁可能失败，会进入获取锁失败的流程
            // 也有可能是节点之前被阻塞，然后被唤醒，此时头结点正好是节点的前驱节点
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }
            // 获取锁失败，或者前驱节点不是头结点
            // 判断是否应该自旋，如果前驱节点的状态为 SIGNAL，则当前节点就可以阻塞了
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt()) // 使用 LockSupport.park 挂起线程
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

`acquireQueued` 方法主要做了以下几件事情

* 判断前驱节点是否为 head 节点，如果是，尝试获取锁。
* 如果前驱节点不是 head 节点，或者获取锁失败，将前驱节点

```java
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    int ws = pred.waitStatus;
    // 如果当前前驱节点状态为 SIGNAL，直接返回
    if (ws == Node.SIGNAL)
        return true;
    if (ws > 0) {
        // 如果节点被取消了，移除节点，直到找到一个没有被取消的节点
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else { // 如果 ws <= 0，直接把节点设置为 SIGNAL
        /*
         * waitStatus must be 0 or PROPAGATE.  Indicate that we
         * need a signal, but don't park yet.  Caller will need to
         * retry to make sure it cannot acquire before parking.
         */
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}
```

`acquire` 大致分为三步：

1. 使用 `tryAcquire` 方法尝试获取锁，成功直接返回，否则走 2
2. 把当前节点加入同步队列队尾
3. 自旋，将当前节点的前驱节点设置文 SIGNAL，然后阻塞自己

![](../../../../.gitbook/assets/aqs-du-zhan-suo-huo-qu-.jpg)

### 释放排他锁 release

排他锁的释放比较简单，从队头开始，找到它的下一个节点，如果下一个节点是空的，就会从队尾开始，一直找到状态节点不是取消的节点，然后释放该节点

```java
public final boolean release(int arg) {
    // tryRelease 由子类实现
    if (tryRelease(arg)) {
        Node h = head;
        // 如果头结点不为空，并且不是初始化状态
        // 如果为初始化状态，可能是因为只有一个节点
        if (h != null && h.waitStatus != 0)
            // 从头唤醒等待锁的节点
            unparkSuccessor(h);
        return true;
    }
    return false;
}

private void unparkSuccessor(Node node) {
    // node 为当前释放锁的节点
    int ws = node.waitStatus;
    if (ws < 0) // 更新为普通节点
        compareAndSetWaitStatus(node, ws, 0);

    Node s = node.next;
    // 如果后继节点为空，或者后继节点已经被取消了
    if (s == null || s.waitStatus > 0) {
        s = null;
        // 从尾节点开始遍历，直到找到一个没有被取消的节点
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0) 
                s = t;
    }
    if (s != null) // 唤醒
        LockSupport.unpark(s.thread);
}
```

释放节点后，将继续执行 `acquireQueued` 中的循环

```java
for (;;) {
    final Node p = node.predecessor();
    // 如果前驱节点就是根节点，就尝试获取锁，如果成功就把自己设置为头结点
    // p=head可能是因为在enq方法中队列才进行初始化，这时锁没有被占用。所以这时可以尝试获取锁
    // 也有可能是前驱节点持有锁，这是获取锁可能失败，会进入获取锁失败的流程
    // 也有可能是节点之前被阻塞，然后被唤醒，此时头结点正好是节点的前驱节点
    if (p == head && tryAcquire(arg)) {
        setHead(node);
        p.next = null; // help GC
        failed = false;
        return interrupted;
    }
    // 获取锁失败，或者前驱节点不是头结点
    // 判断是否应该自旋，如果前驱节点的状态为 SIGNAL，则当前节点就可以阻塞了
    if (shouldParkAfterFailedAcquire(p, node) &&
        parkAndCheckInterrupt()) // 使用 LockSupport.park 挂起线程
        interrupted = true;
}
```

## 共享锁

### acquireShared 获取共享锁

```java
public final void acquireShared(int arg) {
    if (tryAcquireShared(arg) < 0)
        doAcquireShared(arg);
}
```

```java
private void doAcquireShared(int arg) {
    // 以共享模式加入到同步队列队尾
    final Node node = addWaiter(Node.SHARED); 
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            // 如果前驱节点就是头节点，就尝试获取锁，
            if (p == head) {
                int r = tryAcquireShared(arg);
                if (r >= 0) {
                    //获取锁成功，把当前节点设置为头结点，并唤醒
                    setHeadAndPropagate(node, r);
                    p.next = null; // help GC
                    if (interrupted)
                        selfInterrupt();
                    failed = false;
                    return;
                }
            }
            // 前驱节点不是 头结点
            // 前驱节点是 头结点，但是当前节点获取锁失败
            // 判断是否应该自旋，如果前驱节点的状态为 SIGNAL，则当前节点就可以阻塞了
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

```java
private void setHeadAndPropagate(Node node, int propagate) {
    Node h = head; // Record old head for check below
    setHead(node); // 将当前节点设置为头结点

    if (propagate > 0 || h == null || h.waitStatus < 0 ||
        (h = head) == null || h.waitStatus < 0) {
        Node s = node.next;
        // 如果后继节点也是共享模式，就唤醒或者后继节点为空，唤醒后继节点
        if (s == null || s.isShared())
            doReleaseShared();
    }
}
```

获取共享锁的逻辑大致如下：

1. 尝试获取共享锁，成功直接返回，否则走 2
2. 封装为节点，加入同步队列尾部，走 3
3. 如果前驱节点是头结点，再次尝试获取共享锁，成功走 4，否则走 5
4. 将当前节点设置为头结点并唤醒，返回
5. 将当前节点的前驱节点设置为 SIGNAL，然后阻塞

`setHeaderAndPropagate` 这个方法主要做了两件事情：

* 将当前节点设置为新的头结点，这就意味着前驱节点已经获取了共享锁
* 唤醒后继节点

### 释放共享锁

```java
public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) {
        doReleaseShared(); // 释放共享锁成功，唤醒后继节点
        return true;
    }
    return false;
}
```

```java
private void doReleaseShared() {
    for (;;) {
        Node h = head;
        if (h != null && h != tail) {
            int ws = h.waitStatus;
            if (ws == Node.SIGNAL) {
                if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                    continue;            // loop to recheck cases
                // 唤醒后继节点，唤醒后继续自旋逻辑，尝试获取锁
                unparkSuccessor(h); 
            }
            // 如果后继节点暂时不需要唤醒，将状态设置为 PROPAGATE，方便传递给后继节点
            else if (ws == 0 &&
                     !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                continue;                // loop on failed CAS
        }
        // 1. setHeadAndPropagate 中调用的该方法，唤醒节点
        // 2. releaseShared 中调用，释放锁成功
        // 3. 头结点为空，或只有一个节点，直接退出循环
        if (h == head)                   // loop if head changed
            break;
    }
}
```

