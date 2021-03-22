# ThreadPoolExecutor

## 属性

在线程池中，线程的状态和工作的线程数量使用一个 `AtomicInteger` 类型的原子变量 `ctl` 表示。其表示方法如下：

* 高 3 位表示线程状态
* 低 29 位表示工作线程数量

```java
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0)); //容量 + 状态 
private static final int COUNT_BITS = Integer.SIZE - 3;
private static final int CAPACITY   = (1 << COUNT_BITS) - 1; // 最大容量 2^29 - 1

// 线程状态，用高三位类存储
private static final int RUNNING    = -1 << COUNT_BITS; // 111
private static final int SHUTDOWN   =  0 << COUNT_BITS; // 000
private static final int STOP       =  1 << COUNT_BITS; // 001
private static final int TIDYING    =  2 << COUNT_BITS; // 010
private static final int TERMINATED =  3 << COUNT_BITS; // 011

private static int runStateOf(int c)     { return c & ~CAPACITY; } // 返回线程状态
private static int workerCountOf(int c)  { return c & CAPACITY; } // 返回工作线程数
private static int ctlOf(int rs, int wc) { return rs | wc; }
```

其他属性：

```java
// 访问控制相关
private final ReentrantLock mainLock = new ReentrantLock(); 
private final Condition termination = mainLock.newCondition();
private final HashSet<Worker> workers = new HashSet<Worker>(); // 存放线程池中所有工作线程
private static final RuntimePermission shutdownPerm =
    new RuntimePermission("modifyThread");
private final AccessControlContext acc;

private int largestPoolSize; // 线程池最大容量
private long completedTaskCount; // 已经完成的任务数量

// 构造函数相关参数
private final BlockingQueue<Runnable> workQueue; // 阻塞队列
private volatile ThreadFactory threadFactory; // 线程工厂
private volatile RejectedExecutionHandler handler; //拒绝处理器
private volatile long keepAliveTime; // 存活时间
private volatile boolean allowCoreThreadTimeOut; // 是否允许核心线程回收
private volatile int corePoolSize; // 核心线程数量
private volatile int maximumPoolSize; // 最大线程数量
private static final RejectedExecutionHandler defaultHandler =
    new AbortPolicy(); // 拒绝策略
```

在线程池中，将 Thread 封装为了 一个 Worker，Worker 的定义如下：

```java
private final class Worker
    extends AbstractQueuedSynchronizer
        implements Runnable
    {
        private static final long serialVersionUID = 6138294804551838833L;
        final Thread thread; // 任务运行线程
        Runnable firstTask; // 需要执行的线程，可能为 null
        volatile long completedTasks; // 完成的任务数量

        Worker(Runnable firstTask) {
            setState(-1); // inhibit interrupts until runWorker
            this.firstTask = firstTask;
            this.thread = getThreadFactory().newThread(this); // 将自己作为运行线程的任务
        }

        // 线程执行的入口
        public void run() {
            runWorker(this);
        }

        // Lock methods
        // 0 表示没有锁； 1 表示 锁住状态
        protected boolean isHeldExclusively() {
            return getState() != 0;
        }

        protected boolean tryAcquire(int unused) { // 尝试加锁
            if (compareAndSetState(0, 1)) { 
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        protected boolean tryRelease(int unused) { // 尝试释放锁
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        public void lock()        { acquire(1); }
        public boolean tryLock()  { return tryAcquire(1); }
        public void unlock()      { release(1); }
        public boolean isLocked() { return isHeldExclusively(); }

        void interruptIfStarted() {
            Thread t;
            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                }
            }
        }
    }
```

## 构造方法

```java
public ThreadPoolExecutor(int corePoolSize,int maximumPoolSize,long keepAliveTime,TimeUnit unit,BlockingQueue<Runnable> workQueue) {}

public ThreadPoolExecutor(int corePoolSize,int maximumPoolSize,long keepAliveTime,TimeUnit unit,BlockingQueue<Runnable> workQueue,RejectedExecutionHandler handler) {}

public ThreadPoolExecutor(int corePoolSize,int maximumPoolSize,long keepAliveTime,TimeUnit unit,BlockingQueue<Runnable> workQueue,ThreadFactory threadFactory) {}

public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory,
                          RejectedExecutionHandler handler) {
    // 各个参数是否为空，并且是否何合要求，如果为空或不符合要求，抛出异常

    this.acc = System.getSecurityManager() == null ? null :AccessController.getContext();
    this.corePoolSize = corePoolSize;
    this.maximumPoolSize = maximumPoolSize;
    this.workQueue = workQueue;
    this.keepAliveTime = unit.toNanos(keepAliveTime);
    this.threadFactory = threadFactory;
    this.handler = handler;
}
```

* `corePoolSize`：核心线程数。线程池在完成初始化后，默认情况下，线程池中并没有任何线程，线程池会等待有任务的到来时，再创建新的线程去执行任务。
* `maximumPoolSize`：线程池有可能会在核心线程数的基础上，额外增加一些线程，但是这些新增加的线程数有一个上限。
* `keepAliveTime`：如果线程池当前的线程数大于 `corePoolSize`，那么当多余的线程空闲时间超过 `keepAliveTime` 时，就会被终止。
* `unit`：时间单位
* `workQueue`：存放任务的阻塞队列
* `threadFactory`：创建线程的工厂，目的是为了统一创建线程的方式。如果不指定，就为默认的线程工厂，创建的线程强制为 非守护线程，且优先级为 5
* `handler`：拒绝处理器。如果不指定，就采用默认的拒绝策略

## 提交任务

`ThreadPoolExecutor` 由继承自 `Executor` 的 `execute` 方法和继承自 `AbstractExecutor` 的 `submit` 方法。`ThreadPoolExecutor` 对 `execute` 做了实现，而 `submit` 方法没有做覆盖。

```java
public void execute(Runnable command) {
public <T> Future<T> submit(Callable<T> task) {}
public Future<?> submit(Runnable task) {}
public <T> Future<T> submit(Runnable task, T result) {}
```

`execute` 和 `submit` 的区别在于 `execute` 只能够接收 `Runnable` 类型的参数，而 `submit` 既能够接收 `Runnable` 类型的参数，又能够接收 `Callable` 类型的参数。而且 `submit` 方法也是调用 `execute` 方法来提交任务的。

```java
public <T> Future<T> submit(Runnable task, T result) {
    if (task == null) throw new NullPointerException();
    RunnableFuture<T> ftask = newTaskFor(task, result);
    execute(ftask);
    return ftask;
}
protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
    return new FutureTask<T>(runnable, value);
}
```

`submit` 方法将传入的任务封装为了 `RunnableFuture`，而 `RunnableFuture` 继承了 `Runnbale` 和 `Future` 接口。所以不管是传入的 `Runnable` 还是传入 `Callable`，最终都会变为 `RunnableFuture` \`。

```java
public void execute(Runnable command) {
    if (command == null)
        throw new NullPointerException();

    int c = ctl.get();
    // 如果工作线程数量小于核心线程数量，创建一个线程来执行
    if (workerCountOf(c) < corePoolSize) { 
        if (addWorker(command, true))
            return;
        c = ctl.get();
    }

    if (isRunning(c) && workQueue.offer(command)) { // 检查线程状态
        // 再次检查线程状态。因为在添加任务到阻塞队列期间线程池可能被关闭
        int recheck = ctl.get();
        // 如果线程状态不为 RUNNING，移除 command，并拒绝
        if (! isRunning(recheck) && remove(command)) 
            reject(command);
        // 极端情况下工作线程数量为 0
        else if (workerCountOf(recheck) == 0) 
            addWorker(null, false);
    }
    // 队列满了，创建一个线程。如果工作线程大于等于 maximumPoolSize，直接拒绝
    else if (!addWorker(command, false))
        reject(command);
}
```

线程启动的逻辑都在 `addWorker` 方法中，在该方法中，主要是检查线程状态是否合法，然后尝试通过 CAS 更新 `ctl` 变量。更新成功之后创建了一个 `Worker`。`Worker` 在初始化的时候会将自己作为参数，利用线程工厂创建一个线程，并赋给 `Worker` 的成员变量，最后启动线程的时候，执行的是 `Worker` 的 run 方法中的 `runWorker` 方法

```java
private boolean addWorker(Runnable firstTask, boolean core) {
    retry:
    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);
        // 检查线程状态或者任务或者阻塞队列是否为空。如果线程状态为 RUNNING 会执行下面的逻辑
        if (rs >= SHUTDOWN && ! (rs == SHUTDOWN && firstTask == null && ! workQueue.isEmpty()))
            return false;

        for (;;) {
            int wc = workerCountOf(c);
            if (wc >= CAPACITY ||
                wc >= (core ? corePoolSize : maximumPoolSize))
                return false;
            if (compareAndIncrementWorkerCount(c)) // CAS 线程数量加一，跳出标签
                break retry;
            c = ctl.get();  //更新失败，说明线程数量改变了
            if (runStateOf(c) != rs) //如果线程状态改变了，继续 retry，进行检查
                continue retry;
            // else CAS failed due to workerCount change; retry inner loop
        }
    }

    boolean workerStarted = false;
    boolean workerAdded = false;
    Worker w = null;
    try {
        w = new Worker(firstTask); // 创建一个 Worker，实际上会利用线程工厂创建一个线程
        final Thread t = w.thread; // 获取创建的线程
        if (t != null) {
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                int rs = runStateOf(ctl.get());
                if (rs < SHUTDOWN || (rs == SHUTDOWN && firstTask == null)) { 
                    if (t.isAlive()) // 如果线程已经启动了，抛出异常
                        throw new IllegalThreadStateException(); 
                    workers.add(w);
                    int s = workers.size();
                    if (s > largestPoolSize)
                        largestPoolSize = s;
                    workerAdded = true;
                }
            } finally {
                mainLock.unlock();
            }
            if (workerAdded) {
                // 启动线程，执行 runWorker 方法
                t.start(); 
                workerStarted = true;
            }
        }
    } finally {
        if (! workerStarted)
            addWorkerFailed(w);
    }
    return workerStarted;
}
```

```java
final void runWorker(Worker w) {
    Thread wt = Thread.currentThread();
    Runnable task = w.firstTask; // 获取任务
    w.firstTask = null; // 将 firstTask 设置为 null，之后其他线程就不会执行这个任务了
    w.unlock(); // allow interrupts
    boolean completedAbruptly = true;
    try {
        // task 为空有两种情况：1. 任务入队，但是发现没有工作线程。 2. 线程执行完毕，再次回到 while
        // getTask() 会从阻塞队列中取任务
        while (task != null || (task = getTask()) != null) {
            w.lock();
            //线程池已经 stop，线程可能还没有中断，帮助中断
            if ((runStateAtLeast(ctl.get(), STOP) ||
                 (Thread.interrupted() &&
                  runStateAtLeast(ctl.get(), STOP))) &&
                !wt.isInterrupted())
                wt.interrupt();
            try {
                // 钩子方法，在任务执行前执行的逻辑
                beforeExecute(wt, task); 
                Throwable thrown = null;
                try {
                    task.run(); // 执行任务
                } 
                //catch (RuntimeException Error Throwable  x) {throw} 
                finally {
                    // 钩子方法，在任务执行后执行的逻辑
                    afterExecute(task, thrown); 
                }
            } finally {
                task = null;
                w.completedTasks++;
                w.unlock();
            }
        }
        completedAbruptly = false;
    } finally { // 做一些抛出异常的善后工作
        processWorkerExit(w, completedAbruptly);
    }
}
```

根据上面的提交任务流程，我们总结一下：

* 如果工作线程数 &gt; 核心线程数，即 `workerCount`  &lt; `corePoolSize`，创建一个线程执行任务
* 如果 核心线程数 &lt; 工作线程数 &lt; 最大线程数，即 `corePoolSize` &lt; `workerCount` ，将任务加入到阻塞队列中
* 如果队列满了，并且核心线程数量小于最大线程数量，创建一个现线程来执行任务
* 如果队列满了，并且核心线程数量大于等于最大线程数量，拒绝该任务

## 线程执行后的处理

从 `runWorker` 方法中我们看到 while 循环的条件

```java
while (task != null || (task = getTask()) != null) {}
```

也就始说线程执行完毕后会回到while 循环再次判断。由于 task 已经被置为空了，所以会执行 `task = getTask()` 方法。

```java
private Runnable getTask() {
    boolean timedOut = false; // Did the last poll() time out?

    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);

        // 线程池关闭，并且队列为空，不需要运行了，直接返回 null
        if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
            decrementWorkerCount();
            return null;
        }
        int wc = workerCountOf(c);
        // Are workers subject to culling?
        boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;

        if ((wc > maximumPoolSize || (timed && timedOut))
            && (wc > 1 || workQueue.isEmpty())) {
            if (compareAndDecrementWorkerCount(c))
                return null;
            continue;
        }

        try { // 从阻塞队列中取任务
            Runnable r = timed ?
                workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
            workQueue.take();
            if (r != null)
                return r;
            timedOut = true;
        } catch (InterruptedException retry) {
            timedOut = false;
        }
    }
}
```

从上面可以看出线程执行完毕后主要有三种情况：

* 从阻塞队列中取出任务，回到 `runWorker` 的 while 循环判断，执行任务
* 如果允许回收 核心线程，或者队列为空，即没有任务要执行了，返回 null。这样就相当于线程执行完毕，会被回收
* 线程池关闭，并且队列为空，不需要运行了，直接返回 null

## 拒绝策略

当拒绝的时候会执行下面的代码

```java
final void reject(Runnable command) {
    handler.rejectedExecution(command, this);
}
```

线程池主要有 4 中拒绝策略，分别如下：

### 1. 执行任务

```java
public static class CallerRunsPolicy implements RejectedExecutionHandler {
    public CallerRunsPolicy() { }

    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        if (!e.isShutdown()) {
            r.run();
        }
    }
}
```

### 2. 抛出异常\(默认\)

```java
public static class AbortPolicy implements RejectedExecutionHandler {
    public AbortPolicy() { }

    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        throw new RejectedExecutionException("Task " + r.toString() +
                                             " rejected from " +
                                             e.toString());
    }
}
```

### 3. 什么都不做

```java
public static class DiscardPolicy implements RejectedExecutionHandler {
    public DiscardPolicy() { }

    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {}
}
```

### 4. 丢弃阻塞队列中最老的任务

```java
public static class DiscardOldestPolicy implements RejectedExecutionHandler {
    public DiscardOldestPolicy() { }

    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        if (!e.isShutdown()) {
            e.getQueue().poll();
            e.execute(r);
        }
    }
}
```

## 线程池工具类

JDK 为我们提供了线程池的工具类 `Executors` 来方便我们使用线程池。`Executors` 有以下常用的方法来创建线程池。这些方法实际上就是 创建了一个线程池的实例而已。

```java
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                  0L, TimeUnit.MILLISECONDS,
                                  new LinkedBlockingQueue<Runnable>());
}

public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>()));
}

public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>());
}

public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
    return new ScheduledThreadPoolExecutor(corePoolSize);
}
```

这些方法主要使用了两种阻塞队列：

* `LinkedBlockingQueue`：无界队列：工作队列容量没有限制
* `SynchronousQueue`：直接交接

