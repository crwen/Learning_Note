# LinkedBlockingQueue

## 属性

`LinkedBlockingQueue` 是一个用链表实现的阻塞队列，其内部的 `Node` 节点的定义如下：

```java
static class Node<E> {
    E item;
    Node<E> next;
    Node(E x) { item = x; }
}
```

其成员变量如下：

```java
private final int capacity; // 容量

private final AtomicInteger count = new AtomicInteger(); // 队列中元素数量

transient Node<E> head; // 头结点

private transient Node<E> last; // 尾节点

private final ReentrantLock takeLock = new ReentrantLock(); // take 时的锁

private final Condition notEmpty = takeLock.newCondition(); // take 的条件

private final ReentrantLock putLock = new ReentrantLock(); // put 时的锁

private final Condition notFull = putLock.newCondition(); // put 的条件
```

## 构造函数

```java
public LinkedBlockingQueue() {} // this(Integer.MAX_VALUE);

public LinkedBlockingQueue(int capacity) {
    if (capacity <= 0) throw new IllegalArgumentException();
    this.capacity = capacity;
    last = head = new Node<E>(null);
}

public LinkedBlockingQueue(Collection<? extends E> c) {
    this(Integer.MAX_VALUE);
    final ReentrantLock putLock = this.putLock;
    putLock.lock(); // Never contended, but necessary for visibility
    try {
        int n = 0;
        for (E e : c) { 
            if (e == null)
                throw new NullPointerException();
            if (n == capacity)
                throw new IllegalStateException("Queue full");
            enqueue(new Node<E>(e));
            ++n;
        }
        count.set(n);
    } finally {
        putLock.unlock();
    }
}
```

从构造函数可以看出，如果传入午餐的构造函数，或者将集合作为参数，阻塞队列的容量为 int 的最大值，也就相当于容量是无限的。如果传入一个容量作为参数，就会将该容量作为阻塞队列的容量。

## 常用方法

### 添加

`LinkedBlockingQueue` 继承了 `AbstractQueue` ，所以拥有其所有的 `add` 添加方法，而`LinkedBlockingQueue` 并没有重写这些方法，所以调用的时候会直接调用 `AbstractQueue` 的 `add` 方法。但是在 `AbstractBlockingQueue` 的 `add` 方法中调用了子类的 `offer` 方法，所以最终还是会调用 `LinkedBlocking` 的添加方法。

```java
public boolean add(E e) {}
public boolean addAll(Collection<? extends E> c) {}
```

```java
public boolean add(E e) {
    if (offer(e))
        return true;
    else
        throw new IllegalStateException("Queue full");
}
```

`LinkedBlockingQueue` 自身也重写了一些 `Queue` 接口的添加方法

```java
public boolean offer(E e) {}
public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {}
public void put(E e) throws InterruptedException {}
```

`put` 、`offer`、`add` 之间由如下区别：

* `add` 方法继承自 `AbstractQueue`，但是最终还是会调用子类的 `offer` 方法，添加失败会抛出异常
* `offer` 方法添加失败会返回 false，如果传入参数，并且队列满了，就会阻塞相应的时间，并且在添加成功会唤醒 `take` 等待线程
* `put` 时，如果队列满了，就会阻塞，直到队列由空余。

```java
public void put(E e) throws InterruptedException {
    // 如果新增元素为空，抛出异常
    if (e == null) throw new NullPointerException(); 
    int c = -1;
    Node<E> node = new Node<E>(e);
    final ReentrantLock putLock = this.putLock;
    final AtomicInteger count = this.count;
    putLock.lockInterruptibly(); // 设置可中断锁
    try {
         // 如果队列满，阻塞当前线程，否则直接新增，计数器自增
        while (count.get() == capacity) {
            notFull.await();
        }
        enqueue(node); 
        c = count.getAndIncrement();
        // 队列没满，尝试唤醒一个 put 的等待线程
        if (c + 1 < capacity) 
            notFull.signal();
    } finally {
        putLock.unlock();
    }
    if (c == 0) // 表明 队列中有一个元素（getAndIncrement 返回的是旧值，所以 c 比容量小 1），
        signalNotEmpty(); // 尝试唤醒一个 take 的等待线程
}
```

`put` 方法与生产者消费者模式中的生产者类似，添加元素的时候，如果队列满了，就会阻塞，如果添加后，队列中只有一个元素，就会唤醒 `take` 等待线程。

## 删除

```java
public E remove() {}
public boolean remove(Object o) {}
public boolean removeAll(Collection<?> c) {}
public E poll() {}
public E poll(long timeout, TimeUnit unit) throws InterruptedException {}
public E take() throws InterruptedException {}
```

`remove`、`poll`、`take` 的区别如下：

* `remove` 删除成功返回 true，否则返回 false。在删除前会对 `putLock` 和 `takeLock` 加锁
* `poll` 添加失败返回 null，否则返回添加元素，会对 `takeLock` 加锁
* `take` 时如果队列为空，会阻塞。

```java
public E take() throws InterruptedException {
    E x;
    int c = -1;
    final AtomicInteger count = this.count;
    final ReentrantLock takeLock = this.takeLock;
    takeLock.lockInterruptibly(); // 设置可中断锁
    try {
        // 如果 队列为空，阻塞当前线程，否则出队
        while (count.get() == 0) { 
            notEmpty.await();
        }
        x = dequeue();
        c = count.getAndDecrement();
        // 如果出队后 队列中还有元素，尝试唤醒一个 take 线程
        if (c > 1) 
            notEmpty.signal();
    } finally {
        takeLock.unlock();
    }
    // 尝试唤醒一个 put 线程
    if (c == capacity) 
        signalNotFull();
    return x;
}
```

```java
private E dequeue() {
    Node<E> h = head;
    Node<E> first = h.next;
    h.next = h; // help GC
    head = first;
    E x = first.item;
    first.item = null;
    return x;
}
```

`take` 方法与生产者消费者模式中的消费者类似，取出元素的时候，如果队列为空，就会阻塞，如果取出元素导致队列变为非满状态，就会唤醒 `put` 等待线程。

## 查看

```java
public E peek() {
    if (count.get() == 0) // 队列为空，返回 null
        return null;
    final ReentrantLock takeLock = this.takeLock;
    takeLock.lock(); // 加锁
    try {
        Node<E> first = head.next;
        if (first == null) // 队首为空，返回 null
            return null;
        else
            return first.item; // 返回队首元素
    } finally {
        takeLock.unlock();
    }
}
```

查看的时候会对 `takeLock` 加锁，在此期间，所有的删除操作都会被阻塞，一次保证数据的一致性。

