# Java 基础复习

## 概述

### Java 特点

* 面向对象（封装、继承、多态）
* 平台无关性，“一次编写，到处运行”
* 可靠性、安全性
* 支持多线程
* 支持网络编程，并且方便
* 编译与解释并存

### 编译与解释共存

* 编译型语言是指编译器针对特定的操作系统将源代码一次性翻译成可被该平台执行的机器码；
* 解释型语言是指解释器对源程序逐行解释成特定平台的机器码并立即执行。

Java 语言既具有编译型语言的特征，也具有解释型语言的特征，因为 Java 程序要经过先编译，后解释两个步骤，由 Java 编写的程序需要先经过编译步骤，生成字节码（\*.class 文件），这种字节码必须由 Java 解释器来解释执行。因此，我们可以认为 Java 语言编译与解释并存。

### Java 与 C++

关系：

* 都是面向对象的语言，都支持封装、继承、多态

区别：

* Java 没有指针概念，C++支持指针
* C++ 支持多继承，Java 只支持单继承
* Java 垃圾自动回收，不需要手动删除，C 需要手动释放内存
* Java 不支持操作符重载，C++ 支持

### JVM、JRE 和 JDK

* Java 虚拟机（JVM）是运行 Java 字节码的虚拟机。JVM 有针对不同系统的特定实现，目的是使用相同的字节码，都会给出相同的结果。字节码和不同系统的 JVM 实现是 Java 语言 “一次编译，随处可以运行” 的关键所在
* JRE 是 Java Runtime Environment 缩写，是运行已编译 Java 程序所需要的所有内容的集合，包括 JVM，Java 类库，Java 命令和其他的一些基础构建。但是不能用于创建新程序
* JDK 是 Java Development Kit 的缩写，是功能齐全的 Java SDK，它拥有 JRE，还有编译器（Javac）和工具（javadoc和jdb）。它能够创建和编译程序

### 字节码

Java “一次编译，到处运行” 的特性是因为 JVM 针对各种操作系统定制了 Java 虚拟机。通过将程序编译为 字节码文件供 JVM 运行。

**字节码好处**

解决了传统解释型语言执行效率低的问题，同时又保留了解释型语言可移植的特点。

## 基础语法

### 数据类型

* 基本数据类型：byte\(1\), short\(2\), int\(4\), long\(8\), float\(4\), double\(8\), char\(2\), boolean\(1/4\)
* 引用数据类型

> 在 JVM 中并没有提供 boolean 专用的字节码指令，而 boolean 类型数据在经过编译后在 JVM 中会通过 int 类型来表示，此时 boolean 数据 4 字节 32 位，而 boolean 数组将会被编码成 Java 虚拟机的 byte 数组，此时每个 boolean 数据 1 字节占 8bit。

### int 和 Integer 区别

1. int 是一种基本数据类型，`Integer` 是 int 的包装类型，其值是对象的引用
2. int 的默认值是 0，`Integer` 的默认值是 null
3. 基本类型不能用于泛型，包装类型可以用于泛型

Integer 中会缓存 -128~127 的值

```java
public static Integer valueOf(int i) {
        assert IntegerCache.high >= 127;
        if (i >= IntegerCache.low && i <= IntegerCache.high) {
            return IntegerCache.cache[i + (-IntegerCache.low)];
        }
        return new Integer(i);
    }
}
```

### 访问修饰符

* **default**\(默认\)：在同一包内可见，可以修饰接口中的方法
* **private**：同一类可见，不能修饰外部类
* **public**：对所有类可见
* **protected**：同一包内的类和所有子内可见，不能修饰外部类

![image-20210219173433142](https://camo.githubusercontent.com/9fc74ff665c0e40db0ef9784c4713bf664da3add070989598e9825435bea912a/687474703a2f2f626c6f672d696d672e636f6f6c73656e2e636e2f696d672f696d6167652d32303231303231393137333433333134322e706e67)

### switch

Java 5 以前，switch\(expr\) 只能使用 byte、short、char、int

Java 5 引入了 枚举类型，switch 也可以使用枚举

从 Java 7 开始，可以在 `switch` 判断语句中可以使用 `String` 对象，但是不支持 `Long` 类型的变量，因为 `switch` 设计初衷是对少数几个值进行等值判断。

### final

* 基本类型：数值不能改变
* 引用类型：引用不能改变，但被引用的对象本身可以修改
* 类：不能被继承
* 方法：不能被子类重写\(private 隐式地被指定为 final\)

**finalize**

finalize 是在 `java.lang.Object` 里定义的方法，也就是说每一个对象都有这么个方法，这个方法在 `gc` 启动，该对象被回收的时候被调用。

一个对象的 finalize 方法**只会被调用一次**，finalize 被调用不一定会立即回收该对象，所以有可能调用 finalize 后，该对象又不需要被回收了，然后到了真正要被回收的时候，因为前面调用过一次，所以不会再次调用 finalize 了，进而产生问题，因此不推荐使用 finalize 方法。

### static

* 静态变量：在类准备阶段赋零值，初始化阶段赋值。被所有的实例共享
* 静态方法：
  * 类加载的时候就存在，**不能是抽象方法**，必须有实现。
  * 只能访问静态字段和静态方法，
  * 方法中不能有 `this` 和 `super` 关键字
  * **不能被重写（Override）**，但是能够被再次声明
* 静态代码块：在初始化时运行一次
* 静态内部类：不能访问外部类的非静态的变量和方法

执行顺序：**父类静态代码块 ---&gt; 子类静态代码块 ---&gt; 父类代码块 ---&gt; 父类构造器 ---&gt; 子类代码块 ---&gt; 子类构造器**

## 面向对象

### 面向对象与面向过程

* 面向过程
  * 优点：性能高，因为类调用时需要实例化，开销较大
  * 缺点：没有面向对象易维护、易复用、易扩展
* 面向对象：
  * 优点：易维护、易复用、易扩展。因为有封装、继承、多态特性，可以设计出低耦合的系统，使系统更加灵活、更加易于维护
  * 缺点：性能比面向过程低

### 面向对象三大特性

* 封装：利用抽象数据类型将数据和基于数据的操作封装在一起，使其构成一个不可分割的独立实体。数据被保护在抽象数据类型的内部，尽可能地隐藏内部的细节，只保留一些对外的接口使其与外部发生联系。用户无需关心对象内部的细节，但可以通过对象对外提供的接口来访问该对象。
* 继承：继承是指这样一种能力：它可以使用现有类的所有功能，在无需重新编写原来类的情况下对这些功能进行扩展。
* 多态：多态是指在父类中定义的属性和方法被子类继承之后，可以具有不同的数据类型或表现出不同的行为，着使得同一个属性或方法在父类及其各个子类中具有不同的意义

### 多态的实现

* 编译时多态：在编译时就已经确定，运行时调用的时确定的方法
* 运行时多态：编译时不确定调用哪个方法，直到运行时才能确定。
  * 继承
  * 重写：子类重写父类的方法
  * 向上转型：将子类的引用赋给父类对象

### 重写和重载

方法的**重载实现的是编译时**的多态性，方法的**重写实现的是运行时**的多态性。

* **重载\(overload\)**：发生在同一个类中，方法名必须相同，而参数类型、个数顺序不同 ，在**编译期间可以确定**。
* **重写\(override\)**：发生在**运行期间**，是子类对父类的允许访问的方法实现过程进行重新编写，要求返回值类型、方法名、参数列表必须相同，抛出的异常范围小于等于父类，访问修饰符范围大于等于父类

  被 private、final、static修饰的方法不能被重写，但是被 static 修饰的放法能够被再次声明

### 接口与抽象类

**语法层面**

**相同点：**

* 都不能被实例化

**不同点：**

* 接口的变量默认位 `public static final`，抽象方法默认时 `public abstract`
* 接口支持多实现，抽象类只支持单继承
* JDK 8 前接口里只能有变量和抽象方法，而抽象类可以有方法的实现。JDK 8 之后接口中支持默认方法和静态方法，但是仍然不能使用静态代码块
* 接口强调特定功能的实现，而抽象类强调所属关系

```java
interface inter {
    public abstract void a();
    default int defaultMethod() {
        return 1;
    }
    static void staticMethod() {
        //...
    }
}
```

**设计层面上**

### 对象创建的方式

* new 创建对象
* 反射
* 采用 clone
* 反序列化机制

前两者都需要显式地调用构造方法。对于 clone 机制，需要注意浅拷贝和深拷贝的区别，对于序列化机制需要明确其实现原理，在 java 中序列化可以通过实现 Externalizable 或者 Serializable 来实现。

## Object 通用方法

### == 与 equals

== 是判断两个对象的地址是不是相等。即，判断两个对象是不是同一个对象（基本类型比较的是值，引用类型比较的是内存地址）

equals 是判断两个对象是不是相等：

* 没有重写 equals 方法，比较两个对象的地址。在 Object 的 equals方法中就是通过`return (this == obj);` 进行比较的
* 重写了 equals 方法，按照重写的方法比较

### hashCode 与 equals

`hashcode`：在 Java 中，对象的存储在哈希表中，`hashcode`返回的是根据对象的地址转换之后的一个哈希值。散列表存储的是键值对 \(key-value\)，它的特点是：能根据 “键” 快速的检索出对应的 “值”。

**如果两个对象相等，则 `hashcode` 一定相等。但是两个对象的 `hashcode` 相等，它们则不一定相等。**判断两个对象是否相等的时候，会先通过 `hashcode` 来找到对象，如果没有重写 `hashcode`， class 的两个对象不会相等。

在 Java 中的一些容器中，不允许有两个完全相同的对象，插入的时候，如果判断相同则会进行覆盖。这时候如果只重写了 equals（）的方法，而不重写 hashcode 的方法，Object 中 hashcode 是根据对象的存储地址转换而形成的一个哈希值。这时候就有可能因为没有重写 hashcode 方法，造成相同的对象散列到不同的位置而造成对象的不能覆盖的问题。

### clone

clone 是 Object 的 protected 方法，一个类不显示地重写 clone 方法就不能被其他类直接调用该类实例的 clone 方法。重写 clone 方法还应该实现 `Cloneable` 接口，否则调用时会抛出 `CloneNotSupportedException`

**clone 的替代方案**

clone 方法拷贝对象既复杂又有风险，还会抛出异常 ，并且需要类型转换。《Effective Java》提到最好不要使用 clone\(\) 方法，可以在构造函数中拷贝，或者使用工厂拷贝一个对象

#### **浅拷贝**

**浅拷贝的对象和原始对象引用同一个对象**

```java
@Override
protected ShallowClone clone() throws CloneNotSupportedException {
    return (ShallowClone) super.clone();
}
```

#### **深拷贝**

**拷贝对象和原始对象引用不同的对象**

```java
@Override
protected DeepClone clone() throws CloneNotSupportedException {
    DeepClone result = (DeepClone) super.clone();
    result.arr = new int[arr.length];
    for (int i = 0; i < arr.length; i++) {
        result.arr[i] = arr[i];
    }
    return result;
}
```

#### wait/notify

**wait 与 sleep 的区别**

* wait 会释放锁资源，sleep 不会释放锁资源
* wait 是 Object 的方法，sleep 是 Thread 的方法
* 使用 wait 前需要获取对象锁

## String

### String 不可变性

**原因**

1. `String` 被声明为 `final`，表示不能被继承
2. `String`内部使用数组存储数据，且该数组被声明为 `final`，表示该数组的引用不能改变

**不可变的好处**

1. 可以缓存 `hash` 值：可以用作 `HashMap` 的 key，哈希值可以被缓存，快速获取对象
2. String Pool 需要：可以保证重字符串常量池中获取的对象都是同一个。
3. 安全性：`String` 经常作为参数，`String` 的不可变性可以保证参数不可变。比如作为网络连接的参数需要保证不变。
4. 线程安全，可以在多个线程中安全使用

### String、StringBuffer、StringBuilder

**1. 可变性**

* `String` 不可变
* `StringBuffer`和 `StringBuilder` 可变

**2. 线程安全**

* `String` 不可变，因此是线程安全的
* `StringBuilder` 不是线程安全的
* `StringBuffer` 是线程安全的，内部使用 `synchronized` 进行同步

### String.intern\(\)

1.8 之后，如果字符串常量池中没有，将字符串加入字符串常量池中并返回；如果字符串常量池中已经存在了，返回该引用

### 字符串常量池

java中常量池的概念主要有三个：`全局字符串常量池`，`class文件常量池`，`运行时常量池`。我们现在所说的就是`全局字符串常量池`

jvm为了提升性能和减少内存开销，避免字符的重复创建，维护了一块内存空间，即字符串池。当需要使用字符串时，先去字符串常量池中查看该字符串是否已经存在，如果存在，则可以直接使用，如果不存在，初始化，并将该字符串放入字符串常量池中。

在jdk6中，常量池的位置在永久代（方法区）中，此时常量池中存储的是**对象**。在jdk7中，常量池的位置在堆中，此时，常量池存储的就是**引用**了。在jdk8中，永久代（方法区）被元空间取代了。

## 枚举

enum 关键字在 java5 中引入，表示一种特殊类型的类。枚举类在编译后编译器会生成一个类，这个类继承了 `java.lang.Enum` 类。

```java
public enum PizzaStatus {
    ORDERED,
    READY, 
    DELIVERED; 
}
```

枚举可以 new 吗

### 枚举与常量

枚举相较于常量，有以下优点：

* 更具可读性
* 允许编译时检查
* 预先记录可接受值的列表，并避免由于传入无效值而引起的意外行为

### 单例模式

## 异常

### 异常体系

`Throwable` 是所有异常的超类，`Exception` 和 `Error` 都继承自 `Throwable`。

`Error` 是指在 Java **运行时系统的内部错误和资源耗尽错误**。出现了 Error 会使程序终止。比如 `OutOfMemoryError`、`StackOverflowError` 等

`Exception` 分为运行时异常 `RuntimeException` 和 检查型`CheckedException`

* `RuntimeException` 是可能在 Java 虚拟机**正常运行期间抛出的异常**，如：`NullPointerException`、`ArithmeticException`、`ClassCastException`、`ArrayIndexBoundsException`、`IllegalArgumentException`
* `CheckedException` 一般是**外部错误**，**通常发生在编译阶段**，Java 编译器会强制程序捕获该异常。比如：`SQLException`、`IOException`、`ClassNotFoundException`

![img](https://www.pdai.tech/_images/java/java-basic-exception-1.png)

#### Error 与 Exception 区别

* Exception：程序可以处理的异常，可以通过 catch 来捕获。可以分为运行时异常 RuntimeException 和 检查型异常
* `Error` 是指在 Java **运行时系统的内部错误和资源耗尽错误**。出现了 Error 会使程序终止。比如 `OutOfMemoryError`、`StackOverflowError` 等

#### throws 与 throw

* throw：用在方法内部，用于抛出异常
* throws：用在方法声明上，用来表示该方法可能抛出的异常列表。

### 自定义异常

自定义异常只需要继承 `Exception` 集合

```java
public class MyException extends Exception {
    public MyException(){ }
    public MyException(String msg){
        super(msg);
    }
    // ...
}
```

### JVM 异常处理机制

#### **异常表**

异常表包含了一个或多个异常处理器\(Exception Handler\) 的信息：

* from：可能发生异常的起始点
* to：可能发生异常的结束点
* target：异常处理器的位置
* type：异常处理器处理的异常类型

当程序触发异常时，JVM 会从上至下遍历异常表中所有的条目。当触发异常的字节码索引值在某个异常表条目的监控范围时，JVM 会判断抛出的异常和该条目想要捕获的异常是否匹配，如果匹配，JVM 会将控制流转移到该条目 target 指向的字节码

如果变量一

1. 遍历异常表，比较触发异常的字节码的索引值是否在异常处理器的范围内
2. 如果找到了，比较抛出的异常类型是否和异常处理器的 type 是否相同
3. 类型匹配成功后会跳转到异常处理器对应的字节码执行
4. 如果没有匹配到异常处理器，会弹出当前方法对应的栈帧，堆调用者重复上述操作
5. 如果所有栈帧被弹出，仍让没有被处理，则抛给当前 Thread，Thread 则会终止

#### try/catch/finally

**return 和 finally**

finally 中的 return 语句会覆盖 try 中的 return 语句，但是 finally 中对返回的值类型的修改不会影响返回值。但是如果返回的时引用类型，还是会被修改

```java
public int returnTest() {
    int a = 10;
    try {
        return a; // 返回 10
    } finally {
        a = 11;
        //return a; // 返回 11
    }
}
```

**在以下 3 种特殊情况下，`finally` 块不会被执行：**

1. 在 `try` 或 `catch`块中用了 `System.exit(int)` 退出程序。
2. 程序所在的线程死亡。或守护线程在进入 finally 之前非守护线程退出
3. 关闭 CPU。

#### **异常为什么耗时**

1. 建立异常对象耗时
2. 抛出、捕获异常耗时

#### OOM 场景

#### StackoverflowError 场景

## 反射

反射是**在运行状态中，对于任意一个类，都能够知道这个类的所有属性和方法；对于任意一个对象，都能够调用它的任意一个方法和属性；**这种动态获取的信息以及动态调用对象的方法的功能称为 Java 语言的反射机制。

**优点**：能够运行时动态获取类的实例，提高灵活性；可与动态编译结合,比如`Class.forName("com.mysql.jdbc.Driver.class");` 加载MySQL的驱动类。

**缺点**： 1, 性能瓶颈：反射相当于一系列解释操作，通知 JVM 要做的事情，性能比直接的 java 代码要慢很多。2, 安全问题，让我们可以动态操作改变类的属性同时也增加了类的安全隐患。。

#### 反射的应用

* 通过外部类的全路径名创建对象，并使用这些类，实现一些扩展的功能。
* 枚举类的全部成员，包括构造函数、属性、方法。以帮助开发者写出正确的代码。
* 测试时可以利用反射 API 访问类的私有成员，以保证测试代码覆盖率。
* JDBC 连接数据库时使用 `Class.forName()` 通过反射加载数据库的驱动程序；
* Spring 框架的 IOC（动态加载管理 Bean）创建对象以及 AOP（动态代理）功能都和反射有联系；
* 动态配置实例的属性；

#### 原理

1. 反射获取类实例 `Class.forName()`，并没有将实现留给了java,而是交给了jvm去加载！主要是先获取 `ClassLoader`, 然后调用 native 方法，获取信息，加载类则是回调 `java.lang.ClassLoader`。最后，jvm又会回调 `ClassLoader` 进类加载！
2. `newInstance()` 主要做了三件事：
3. 权限检测，如果不通过直接抛出异常；
4. 查找无参构造器，并将其缓存起来；
5. 调用具体方法的无参构造方法，生成实例并返回。
6. 获取Method对象，

## 泛型

泛型是 JDK1.5 的一个新特性，**泛型就是将类型参数化，在编译时才确定具体的参数。**这种参数类型可以用在类、接口和方法的创建中，分别称为泛型类、泛型接口、泛型方法。

#### 好处

远在 JDK 1.4 版本的时候，那时候是没有泛型的概念的，如果使用 Object 来实现通用、不同类型的处理，有这么两个缺点：

1. 每次使用时都需要强制转换成想要的类型
2. 在编译时编译器并不知道类型转换是否正常，运行时才知道，不安全。

**好处**

* 类型安全：编译期间就可以检查出类型错误
* 消除强制类型转换：
* 潜在性能收益：

#### 泛型擦除

## 序列化

## 注解

## 集合

### 集合体系

**Map 是 与 Collection 并列的集合的上层接口**，没有继承关系；List 和 Set 是 Collection 的子接口，还包括 Queue

* `List` 是有序的，主要包括 `ArrayList`、`LinkedList` 和 `Vector`。`ArrayList` 底层通过数组实现，线程不安全；Vector 是线程安全的 `ArrayList`，但效率低；`LinkedList` 底层通过双向链表实现，与 `ArrayList` 相比增删快，查询慢
* Set 是无序的，并且不包含重复元素。主要包括 `HashSet`、`LinkedHashSet`、`TreeSet`。`HashSet` 底层是 HashMap，使用 key 来保证元素的唯一性；`LinkedHashSet` 可以按照 key 的操作顺序排序，`TreeSet` 支持按照默认或指定的默认排序规则排序
* `Queue` 是队列结构，主要有 `ArrayBlockingQueue`，基于数组的阻塞队列；`LinkedBlockingQueue` 基于链表的阻塞队列等
* `Map` 以 key-value 键值对的形式存储元素，主要包括 HashMap、`LinkedHashMap` 和 `TreeMap`。HashMap 底层通过 数组 + 链表/红黑树 实现；`LinkedHashMap` 可以按照 key 的操作顺序对集合排序；`TreeMap` 可以按照默认或指定的排序规则对集合排序。

![1585810070650](file://F:/note/%E5%AE%9E%E4%B9%A0/asset/1585810070650.png?lastModify=1612706804)

### 快速失败（Fail-Fast） 机制

快速失败时 Java 集合的一种错**误检测机制**，当多个线程对集合进行结构上的改变操作时，有可能会产生 fail-fast

**底层实现**

迭代器在遍历时直接访问集合中的内容，并且在遍历过程中使用一个 modCount 变量。集合在被遍历期间如果内容发生变化，就会改变 modCount 的值。当迭代器使用 hashNext \(\)/next \(\) 遍历下一个元素之前，都会检测 modCount 变量是否为 expectedModCount 值，是的话就返回遍历；否则抛出异常，终止遍历。JDK 源码中的判断大概是这样的：

### ArrayList

#### ArrayList 扩容

ArrayList 扩容是以 **1.5 倍扩容**的。如果扩容后的大小比需要的大小还要小，就将预期值作为新的容量。如果新容量 大于 （int 的最大值 - 8），就会将 int 的最大值作为其容量

#### 线程不安全

ArrayList 线程不安全。如果要保证线程安全，可以使用 `Collections.synchronizedList(list)` 或者 `CopyOnWriteArrayList`

#### ArrayList 和 Vector

* ArrayList 1.5 倍扩容，Vector 默认两倍扩容，但是可以指定扩容增加量
* ArrayList 线程不安全，Vector 线程安全

### LinkedList

LinkedList 底层是一个双向链表

#### ArrayList 与 LinkedList

* 数据结构：ArrayList 底层是数组，LinkedList 底层是双向链表
* 随机读写时间复杂度：ArrayList O\(1\); LinkedList O\(N\)
* 
### HashMap

#### 底层原理

* JDK 8 之前 HashMap 采用数组 + 链表来实现
* JDK 8 后采用 **数组 + 链表 + 红黑树** 来实现。当链表长度大于 8 且数组元素个数大于 64 时，链表会转化为红黑树，当节点个数小于 6 时，红黑树转化为链表

#### HashMap get

1. 计算 hash 值
2. 通过 hash 值找到数组对应下标的桶 
   * 如果桶中第一个元素的 hash 值与 内容都相等，直接返回
   * 否则遍历链表或红黑树查找

**两个key 的 hashcode 相等，如何获取对象**

HashCode相同，通过equals 比较内容获取值对象

#### HashMap put

1. 计算 hash 值
2. 散列表为空，进行扩容
3. 找到数组对应的下标
   * 如果对用下标位置没有元素，直接添加
   * 否则，发生碰撞，遍历
     * 如果没有找到，直接插入链表尾部或者红黑树。如果链表节点大于 8，树化
     * 如果找到了，根据 `onlyIfAbsent` 选择是否覆盖
4. 如果元素数量大于阈值，扩容

#### HashMap 扩容

**扩容时机:**

1. **第一次 put** 时，进行初始化扩容，默认大小为 16
2. 数组**元素数量达到阈值**时
3. **树化时，如果数组长度小于 64**，通过扩容来减小链表长度

**扩容机制：**

* 初始化扩容
  * 无参构造函数：容量为默认值 16，计算阈值 12
  * 有参构造函数：容量等于阈值，重新计算阈值
* 其他扩容： 1. **2 倍扩容** 2. rehash
  * 只有一个元素：`newTab[e.hash & (newCap - 1)] = e`
  * 链表：`e.hash & oldCap`，分成两个链表，挂在新数组上
  * 红黑树：

#### hash 函数

```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

hash 函数将 key 的 hashcode 的**高 16 位与其进行异或运算**，这样做可以在大多数场景下使算出的 hash 值比较分散

**其他 hash 函数实现方法**

还有平方取中法、除留余数法、伪随机法

**为什么不直接将 key 作为哈希值，而是与高 16 位做异或运算**

因为数组位置的确定用的是与运算，仅仅最后四位有效，设计者将 key 的哈希值与高 16 位做异或运算使得在做 & 运算确定数组的插入位置时，此时的低位实际是高位与低位的结合，**增加了随机性**，减少了哈希碰撞的次数。

比如如果数组长度为 16，不管高位如何变化，通过 & 操作得到的高位都是 0。

#### 属性、实现相关

**loadFactor**

loadFactor 是负载因子，主要用来判断数组是否需要扩容。判断方法是比较 数组容量 \* loadFactor 和数组实际大小。

**数组容量为什么是 2 的幂**

HashMap 为了存取高效，要尽量较少碰撞，就是要尽量把数据分配均匀，每个链表长度大致相同，这个实现就在把数据存到哪个链表中的算法。HashMap 通过对数组长度取模的结果作为数组的下标，为了提高速度，HashMap 使用 & 运算代替取模操作。当数组长度是 2 的幂次方时， `hash & (cap - 1)`得到的结果才是期望结果。

**为什么使用红黑树**

**为什么链表元素数量超过 8 时改为红黑树**

当链表个数太多时，遍历会很耗时，可以转化为红黑树使遍历的复杂度降低。但是维护红黑树的成本比较高，如果元素太少，效率没有链表高。

选择 8 是因为在理想的情况下，随机的 hashcode 的频率遵循**泊松分布**，在负载因子选择 0.75 的情形下，**节点数量超过 8 的概率非常小**，不到千万分之一。

```markup
0:    0.60653066
1:    0.30326533
2:    0.07581633
3:    0.01263606
4:    0.00157952
5:    0.00015795
6:    0.00001316
7:    0.00000094
8:    0.00000006
```

#### jdk8 中 HashMap 变化

1. 由原先的 数组 + 链表 改为了 数据 + 链表 + 红黑树
2. 优化了高位运算的 hash 算法：h^\(h&gt;&gt;&gt;16\)
3. 链表的头插法改为了尾插法
4. rehash时，JDK 8 之前时将所有元素 rehash，JDK 8 之后会将 hash 值与就数组长度进行与运算

#### HashMap 与 Hashtabale

* **数据结构**：`HashMap` 底层数据结构是 数组 + 链表 + 红黑树。`HashTable 底`层数据结构是数组 + 链表
* **空值**：`HashMap` 支持 key、value 为 null，但是 `HashTable` 不允许，会抛出 `NullPointerException`
* `HashTable` 默认初始容量为11，若指定容量，则使用容量。`HashMap` 在第一次 put 时才会初始化数组，默认容量为 16，如果构造函数中指定了大小，则以最接近该数的 2 的幂次为容量。
* **线程安全**：`HashMap` 线程不安全，`HashTable`线程安全，内部方法被 `synchronized` 修饰

### Hashtable

#### 线程安全

**Hashtable 是怎么实现线程安全的，两个线程可以同时分别调用 get 和 put 吗**

Hashtable 中所有的方法都被 `synchronized` 修饰，以此来保证线程安全。

