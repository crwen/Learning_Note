# 网络复习

## 1. 概述

网络的七层架构从下到上主要包括物理层、数据链路层、网络层、传输层、会话层、表示层和应用层。

* 物理层：物理层主要定义物理设备标准，主要作用是传输比特流，具体做法是在发送端将 1、0 转化为电流强弱来进行传输，在到达目的地之后再将电流强弱转化为 1、0，也就是我们常说的模数转换与数模转换，这一层的数据叫做比特。
* 数据链路层：数据链路层主要用于对数据包中的 MAC 地址进行解析和封装。这一层的数据叫做帧，在这一层工作的设备是网卡、网桥、交换机。
* 网络层：网络层主要用于对数据包中的 IP 地址进行封装和解析，这一层的数据叫做数据包。在这一层工作的设备有路由器、交换机、防火墙等。
* 传输层：传输层定义了传输数据的协议和端口号，主要用于数据的分段、传输和重组。在这一层工作的协议有 TCP 和 UDP 等。TCP 是传输控制协议，传输效率低，可靠性强，用于传输对可靠性要求高，数据量大的数据，比如支付宝转账业务；UDP 是用户数据报协议，用于传输可靠性要求不高，数据量小的数据，例如抖音等视频服务。
* 会话层：会话层在传输层的基础上建立连接和管理会话，具体包括登陆验证、断点续传、数据粘包与分包等。在设备之间需要互相识别的可以是 IP，也可以是 MAC 或者主机名。
* 表示层：表示层主要对接收的数据进行解释、加密、解密、压缩、解压缩等，即把计算机能够识别的内容转换成人能够识别的内容（图片、声音、文字等）。
* 应用层：基于网络构建具体应用，例如 FTP 上传文件下载服务、Telnet 服务、HTTP 服务、DNS 服务、SNMP 邮件服务等。

## 2. 数据链路层

### 2.1 以太帧

* **封装成帧**：将网络层传下来分组添加首部和尾部，用于标记帧的开始和结束
* **透明传输**：帧使用首部和尾部进行定界，如果数据部分包含和首部和尾部相同的内容，就在内容前插入转义字符。
* **差错检验**：检查帧传递过程中是否出错

**以太帧格式**

![&#x5E27;&#x683C;&#x5F0F;](https://cs-notes-1256109796.cos.ap-guangzhou.myqcloud.com/164944d3-bbd2-4bb2-924b-e62199c51b90.png)

* 类型：标记上层使用的协议
* 数据：长度在 46~1500 之间，如果太小需要填充
* FCS：帧检验序列，使用 CRC

### 2.2 交换机

为了将以太帧发送到正确的机器，交换机需要知道此帧中的目标 MAC 地址，交换机通过维护一个 **CAM 表**来达到目的。

#### 2.1.1 CAM 表

CAM\(Content Addressable Memory\) 表是“内容可寻址寄存表”，记录着**接口与连接接口的机器的 MAC 地址之间的映射关系**。

| 端口 | MAC 地址 |
| :--- | :--- |
| 1 | 1 号端口连接机器的 MAC 地址 |
| 2 | 2 号端口连接机器的 MAC 地址 |
| 3 | 3 号端口连接机器的 MAC 地址 |

当 1 号机器想要发送一个帧给 3 号机器时，交换机就读取帧里面包含的目标 MAC 地址，根据 CAM 表中映射关系，找到对应的端口，将帧发送给 3 号机器。

**CAM 表原理**

交换机的 CAM 表时以**动态的方式被构建**的，当交换机收到一个帧后，会从帧中读取源 MAC 地址，并更新或记录 MAC 地址与端口的映射。

如果帧中的 MAC 地址没有被记录在 CAM 表中，就会广播此帧，发送给除了源 MAC 地址所关联的机器以外的所有机器。这种操作称为 flooding（泛洪）。当目标机器收到此帧后，会回复交换机，交换机更新 CAM 表

**CAM 表生存时间**

CAM 表的内容会不断增加，为了防止 CAM 表不断增大，CAM 表中的数据都有一个**生存时间**，一旦超过这个时间，就将数据移除。

#### 2.1.2 攻击

1. 批量**向不存在的目标 MAC 地址发送数据帧以达到饱和**
2. 发送大量帧，使 CAM 表饱和，这样交换机没有时间读取 CAM 表，就会广播

### 网卡

#### 发送数据

网卡驱动从 IP 模块获取包之后，会将其复制到网卡内的缓冲区中，然后向 MAC 模块发送发送包的命令

MAC 模块会将包从缓冲区中取出，并在开头加上帧头和起始帧定界符，并在末尾加上用于检测错误的帧校验序列 FCS。然后 MAC 模块从包头开始将数字信号转换为电信号，然后由 MAU/PHY 模块将信号转换为可传输的模式，通过网线发送出去。

#### 接收数据

根据帧的波形同步时钟，找到起始帧分界符，开始将后面的信号转换为数字信号。

MAU 模块会将信号转换成通用格式并发送给 MAC 模块

MAC 模块再从头开始将信号转换为数字信号，并存放在缓冲区中。

当到达信号末尾时，检查 FCS， 出错，丢弃。

检查数据帧中的目标 MAC 地址与网卡的 MAC 地址是否一致。不一致，丢弃，否则将包放在缓冲区。

网卡向扩展总线中的中断信号发送中断信号（硬件的中断号和相应的驱动程序绑定），中断程序从缓冲区中读取数据。

IP 模块检查数据包接受方的 IP 地址是否本机 IP 地址，不是，发送 ICMP 报文通知错误

TCP 模块找到对应的套接字，放入缓冲区，等待应用层读取数据

## 3. 网络层

### 路由器

路由器是 OSI 第三层的硬件，可以连接多个网络。因此，对于所连接的每个网络，路由器都有一个对应的接口。当路由器接收到数据包时，会将数据包发送出去

#### 路由表

#### 路由器分组转发流程

* 从数据报的首部提取目的主机的 IP 地址 D，得到目的网络地址 N。
* 若 N 就是与此路由器直接相连的某个网络地址，则进行直接交付；
* 若路由表中有目的地址为 D 的特定主机路由，则把数据报传送给表中所指明的下一跳路由器；
* 若路由表中有到达网络 N 的路由，则把数据报传送给路由表中所指明的下一跳路由器；
* 若路由表中有一个默认路由，则把数据报传送给路由表中所指明的默认路由器；
* 报告转发分组出错。

作用

与交换机的区别

### 子网掩码

子网掩码指明了 IP 地址的哪一部分是网络地址，哪一部分是主机地址，通过网络地址，可以**确定与之相关联的 IP 地址范围**。

子网掩码的二进制形式所有的 1 都在 0 的左边

作用、原理

### IP

![](https://cs-notes-1256109796.cos.ap-guangzhou.myqcloud.com/85c05fb1-5546-4c50-9221-21f231cdc8c5.jpg)

mtu

分片

### ARP

数据包到达网络层后，要想将数据发送到链路层需要知道链路层的 MAC 地址。使用 ARP 可以实现**由 IP 地址到 MAC 地址的转化**。

每个主机都有一个 ARP 高速缓存，里面有本局域网上的各主机和路由器的 IP 地址到 MAC 地址的映射表。

假设主机 A 知道主机 B 的 IP 地址，A 给 B 发送数据帧的工作流程大致如下：

* 查本地 ARP 缓存，看 ARP 缓存有没有该 IP 地址到 MAC 地址的映射
  * 如果有，直接向 MAC 地址发送信息
  * 如果没有，发送 ARP 广播。主机 B 收到该请求后发送 ARP 响应分组给主机 A。主机 A 将 B 的 IP 地址到 MAC 地址的映射写入 ARP 缓存中。
* 发送数据帧

#### ARP 欺骗

ARP 攻击是利用 ARP 广播来进行的。当一台机器进行 ARP 广播时，攻击者**在目标机响应之后（等待一 2s）对该请求进行响应**，那么目标机器的 IP 到 MAC 地址的映射会被修改。这样，每次源主机发送给目标主机的消息都会发送到攻击者那里。

攻击者只要使用相同的方法致使源主机与目标主机的 ARP 表篡改，然后自己充当中间人就可以截取这两台主机之间的所有流量了。这种攻击属于**中间人攻击**。

这种攻击存在两个问题：

* 一台机器在攻击值响应之后进行响应，导致 ARP 缓存修改
* 一段时间，ARP 缓存删除超时记录

攻击方法改进：**当收到 ARP 响应时，即使主机没有发送 ARP 请求，也会认为该信息是有效的**。因此，可以持续不断地用 ARP 响应攻击主机。

### 路由选择协议

#### **内部网关协议 RIP**

RIP 是一种基于**距离向量**的路由选择协议。距离是指跳数，直接相连的路由器跳数为 1，跳数最多为 15，超过 15 表示不可达。，它要求每个路由器都要维护从自己到其它每一个目的网络的距离记录。

**RIP 按固定的时间间隔，仅和相邻路由器交换自己的路由表**。经过若干次交换（大约每 30s 发送一次 RIP 响应报文）之后，所有路由器最终会知道到达本自治系统中任意一个网络的最短距离和下一跳路由器地址。

每台路由器维护一张选路表（RIP 表），选路表包括该路由器的距离向量和路由器的转发表。

对相邻路由器发来的 RIP 报文，先修改报文中的所有项目，假设相邻路由器为 X，就把下一跳字段中的地址改为 X，并把所有的距离字段加 1；对修改后的 RIP 报文中的每一个项目进行检查：

* 如果原来的路由表中没有目的网络 ，则把该项目添加到路由表中；
* 否则：如果下一跳路由器地址是 X，就把收到的项目替换原来路由表中的项目；否则：若收到的项目中的距离 小于路由表中的距离，就进行更新；否则什么也不做。

如果 3 分钟还没有收到相邻路由器的更新路由表，则把该相邻路由器标为不可达，即把距离置为 16。

#### **内部网关协议 OSPF**

开放最短路径优先 OSPF，是为了克服 RIP 的缺点而开发出来的。

开放表示 OSPF 不受某一家厂商控制，而是公开发表的；最短路径优先表示使用了 Dijkstra 提出的最短路径算法 SPF。

OSPF 具有以下特点：

* 向本自治系统中的**所有路由器发送信息**，这种方法是洪泛法。
* **发送的信息就是与相邻路由器的链路状态**，然后路由器使用链路状态构造出自己的路由表。链路状态包括与哪些路由器相连以及链路的度量，度量用费用、距离、时延、带宽等来表示。
* 只有当链路状态发生变化时，路由器才会发送信息。

**所有路由器都具有全网的拓扑结构图，并且是一致的**。相比于 RIP，OSPF 的更新过程收敛的很快。

#### **外部网关协议 BGP**

BGP（Border Gateway Protocol，边界网关协议）

AS 之间的路由选择很困难，主要是由于：

* 互联网规模很大；
* 各个 AS 内部使用不同的路由选择协议，无法准确定义路径的度量；
* AS 之间的路由选择必须考虑有关的策略，比如有些 AS 不愿意让其它 AS 经过。

BGP 只能寻找一条比较好的路由，而不是最佳路由。

每个 AS 都必须配置 BGP 发言人，通过在两个相邻 BGP 发言人之间建立 TCP 连接来交换路由信息。

### ICMP

ICMP 是为了更有效地转发 IP 数据报和提高交付成功的机会。它封装在 IP 数据报中，但是不属于高层协议。

#### ping

Ping 是 ICMP 的一个重要应用，主要用来测试两台主机之间的连通性。

Ping 的原理是通过向目的主机发送 ICMP Echo 请求报文，目的主机收到之后会发送 Echo 回答报文。Ping 会根据时间和成功响应的次数估算出数据包往返时间以及丢包率。

## 4. 传输层

### 4.1 TCP

TCP 是面向连接、可靠的面向字节流的服务

**TCP报文结构**

![TCP &#x62A5;&#x6587;&#x7ED3;&#x6784;](https://gblobscdn.gitbook.com/assets%2F-ME0Wzu5e_0LZXvULoua%2F-MEHA0iXk9BKgs18e5Kw%2F-MEHgY-jidyzOISKJp8m%2FRYYGDSU%5B38J39R1O9IAEB8W.png?alt=media&token=3f96b02c-8c9d-47bf-a630-c42dc070bd67)

* 源、目的端口
* 序列号
* 确认号
* 头部长度
* flag
* 窗口
* 校验和

#### 4.1.1 可靠传输

* 校验和
* 序列号
* 超时重传
* 流量控制、拥塞控制

#### 4.1.2 三次握手

* 客户端发送 SYN 包，并选择一个随机的初始序号 ISN，表明要建立一个连接，然后进入 `SYN_SEND` 状态，等待服务端确认。（客户端发送能力、服务端接受能力正常）
* 服务端收到客户端的 SYN 包后，需要对其进行确认。同时也会发送一个 SYN 包作为应答，同时也会选择一个随机的初始序号 ISN。在这个 SYN + ACK 包中，ack 为客户端 SYN 包的序号 + 1。之后服务端进入 `SYN_RECV` 状态。（服务端的收发能力、客户端发送能力正常）
* 客户端收到服务端的 SYN + ACK 包后，需要进行确认，发送一个 ACK 包，序号为收到的服务端的包的序号 + 1。之后客户端进入 `ESTABLISHED` 状态。
* 服务端收到确认后，也进入 `ESTABLISHED` 状态

![&#x4E09;&#x6B21;&#x63E1;&#x624B;](http://cdn.yuanrengu.com/img/20200210134500.png)

**为什么要三次握手，两次不行吗？**

1. 第三次握手是为了**防止失效的连接请求服务器，让服务器错误的打开连接**。

   请求连接的报文可能在网络中滞留，直到释放连接后才到达服务器。如果没有第三次握手，服务器会建立连接，等待客户端发送数据，但是客户端户忽略服务端发来的确认。如果有第三次握手，服务端发出确认后等不到客户端的确认，会关闭连接。

2. 三次握手的主要作用就是为了**建立连接**并**确认双方的接收能力和发送能力**是否正常
3. 第一次握手：客户端发送网络包，服务端收到了。 结论：客户端的发送能力、服务端的接收能力是正常的。
4. 第二次握手：服务端发包，客户端收到了。

   结论：服务端的接收、发送能力，客户端的接收、发送能力是正常的。不过此时服务器并不能确认客户端的接收能力是否正常。

5. 第三次握手：客户端发包，服务端收到了。

   结论：客户端的接收、发送能力正常，服务器自己的发送、接收能力也正常。

**三次握手过程中可以携带数据吗**

第三次握手的时候，是可以携带数据的。但是，**第一次、第二次握手不能携带数据**。

假如第一次握手可以携带数据的话，如果有人要**恶意攻击**服务器，那他每次都在第一次握手中的 SYN 报文中放入大量的数据。因为攻击者根本就不理服务器的接收、发送能力是否正常，然后疯狂地重复发 SYN 报文的话，这会让服务器花费很多时间、内存空间来接收这些报文。

**第三次握手失败怎么办？是否会重发？重发多少次？**

如果第三次握手失败，客户端处于 `ESTABLISHED`状态，而服务端处于 `SYN_RECV` 状态

* 如果客户端发送了数据，并且服务端收到了，会包含一个相同的 ACK，一切正常
* 如果客户端没有发送数据，或者应由服务端发送数据，会重传，重传一定次数（默认5次，由 `/proc/sys/net/ipv4/tcp_synack_retries` 指定）之后发送 RST 复位报文，进入 `CLOSED` 状态。

**半/全连接队列**

服务端回复收到客户端的 SYN 之后，会处于 `SYN_RCVD` 状态，此时双方还没有完全建立连接，服务端会将此种状态下的请求放在**半连接队列**中。

当连接建立后就会将请求从半连接队列中移除，放入**全连接队列**，每次应用调用 accept\(\) 函数会移除队列头的连接，如果队列为空，accept 会阻塞。

如果服务端发送完 `SYN-ACK` 等待一段时间没有收到确认，会进行重传，重传次数超过系统规定最大重传次数，就会将该链接信息从半连接队列中删除。

**syn 洪泛攻击**

**服务器端的资源分配是在二次握手时分配的，而客户端的资源是在完成三次握手时分配的**，所以服务器容易受到 SYN 洪泛攻击。SYN 攻击就是 **Client 在短时间内伪造大量不存在的 IP 地址，并向 Server 不断地发送 SYN 包**，Server 则回复确认包，并等待 Client 确认，由于源地址不存在，因此 Server 需要不断重发直至超时\(63s\)，这些伪**造的 SYN 包将长时间占用未连接队列，导致正常的 SYN 请求因为队列满而被丢弃，从而引起网络拥塞甚至系统瘫痪**。SYN 攻击是一种典型的 DoS/DDoS 攻击。

通俗的理解是：当第三次握手没有发送确认信息时，等待一段时间后，主机就会断开之前的半开连接并回收资源，这为 dos（deny of service）攻击埋下隐患，当主动方主动发送大量的 syn 数据包，但并不做出第三次握手响应，server 就会为这些 syn 包分配资源（但并未使用），就会使 server 占用大量内存，使 server 连接环境耗尽，这就是 syn 洪泛攻击

**检测方法**

在服务器上看到大量的`半连接状态`时，特别是源 IP 地址是随机的，基本上可以断定这是一次 SYN 攻击。

在 Linux/Unix 上可以使用系统自带的 netstat 命令来检测 SYN 攻击。

```text
netstat -n -p TCP | grep SYN_RECV
```

**防御方法**

* 缩短超时时间（SYN Timeout）
* 增加最大半连接数
* 过滤网关防护
* SYN cookies 技术，半连接队列满后，发送一个 Sequence Number\(cookie\)，如果是攻击者不会响应，正常连接会发回来。然后服务端可以通过 cookie 建立连接（即使不在 SYN 队列中）。

#### 4.1.3 四次挥手

* 客户端发送一个连接释放报文，并停止发送数据包。该报文将 FIN 置为 1，序列号为最后一个确认接收到的数据的最后一个字节序号 + 1，然后进入 `FIN-WAIT-1` 状态
* 服务端收到连接释放报文后发送一个 ACK 确认，ack 为收到的序列号 + 1，序号为成功发送过的数据序号 + 1，然后进入 `CLOSE-WAIT` 状态（此时TCP处于半关闭状态）。客户端收到来自服务端的确认后进入 `FIN-WAIT-2` 状态，等待服务端发出的连接释放报文
* 服务端数据发送完毕后，会向客户端发送连接释放报文，将 FIN 置为 1，序号为上次发送过数据的最后一个字节的序号 + 1，服务端进入 `LAST-ACK` 状态
* 客户端收到来自服务端的连接报文后，需要发出确认报文段，将 ACK 置为 1，ack 为收到报文的序列号 + 1，客户端进入 `TIME-WAIT` 状态。发送 ACK 后2MSL 之后，客户端进入 `CLOSED`，服务端接收到 ACK 后进入 `CLOSED` 状态

![&#x56DB;&#x6B21;&#x6325;&#x624B;](http://cdn.yuanrengu.com/img/20200210134547.png)

**为什么需要四次握手**

当连接处于半关闭状态时，**TCP 是允许单向传输数据的**。当主动方关闭连接时，被动方仍然可以在不调用 close 函数的状态下，长时间发送数据，此时连接处于**半关闭状态**。这一特性是 TCP 的`双向通道互相独立所致`，却也使得关闭连接必须通过四次挥手才能做到。

**发送的 FIN 标志中 Sequence Number 怎么设置**

**为什么要2MSL**

* 确保最后一个确认报文能够到达。
* 等待一段时间是为了让本连接持续时间内所产生的所有报文都从网络中消失，使得下一个新的连接不会出现旧的连接请求报文。

#### 4.1.4 滑动窗口

发送方和接收方都有一个窗口，接收方通过 TCP 报文段中的窗口字段告诉发送方自己的窗口大小，发送方根据这个值和其他信息设置自己的窗口大小。

**滑动窗口为 0 怎么办**

当接收方通告窗口大小为 0 时，发送方就不能向接收方发送数据了。等到**接收方有可用缓存区时（至少 2 个报文段长度），会发送一个窗口更新公告**，通知发送方发送数据。为了防止窗口更新公告的丢失，**接收方会开启坚持定时器**，如果定时器超时也没有收到更新窗口的通知，就会发送一个字节的**窗口探测包**。如果**若干次数还没有收到，就会重置连接**。

#### 4.1.5 拥塞控制

TCP 主要通过四个算法来进行拥塞控制：慢启动、拥塞避免、快重传、快恢复。

发送方会维持一个叫做拥塞窗口 cwnd 的状态变量。拥塞窗口的大小取决于网络的拥塞程度，并且动态地在变化。

发送方控制拥塞窗口的原则：

* 网络没有出现拥塞，拥塞窗口就可以增大一些，以便把更多的分组发送出去，这样可以提高网络的利用率。
* 网络出现拥塞或可能出现拥塞，就必须把拥塞窗口减小一些，以减少注入到网络中的分组数，以便缓解网络出现的拥塞。

**慢启动**

拥塞窗口被初始化为一个报文段。**每收到一个 ACK，拥塞窗口就增加一个报文段**。经过 k 轮发送后，拥塞窗口就会变为 2^k （拥塞窗口小于滑动窗口的情况下）。

**拥塞避免**

如果 **crwn &gt; ssthresh** ，就不再执行慢启动，而是**执行拥塞避免算法**。在拥塞避免算法中，每收到一个确认，cwnd 增加 1/cwnd。

当发生超时，sshressh 会被设置为当前窗口大小的一半，crwnd 被重新设置为 1，开始慢启动

**快重传**

**快重传算法要求每次接收到报文段都应该对最后一个已收到的有序报文段进行确认。**例如已经接收到 M1 和 M2，此时收到 M4，应当发送对 M2 的确认。

在发送方，如果**收到三个重复确认**，这是很可能是有报文段丢失了，此时**执行快重传，立即重传下一个报文段，进入快恢复阶段**。这时不采用慢启动是因为接收方只有在收到另一个报文段时才会产生重复的 ACK，这就说明此时网络状态还比较畅通。

**快恢复**

在快恢复阶段，做如下处理：

* 收到第 3 个重复的 ACK 时，设置 ssthresh = cwnd / 2，cwnd = ssthresh + 3 \* MSS
* 每次收到另一个重复的 ACK 时，cwnd 增加一个报文段大小并发送一个分组
* 当下一个确认新数据的 ACK 到达时，设置 cwnd = ssthresh，结束快恢复，进入拥塞避免。

总结一下拥塞控制：

执行慢开始，拥塞窗口指数级增加

当 ssthresh &gt; crwn，执行拥塞避免，拥塞窗口每经过一轮增加 1

如果发送重传

* **超时重传**，ssthresh = cwnd / 2; cwnd = 1; 执行慢开始
* **三次重复确认**，ssthresh = crwnd / 2; cwnd = ssthresh；执行快恢复

#### 差错控制

* 校验和：主要用来校验数据包是否发生错误
* 确认：对收到报文的确认，累计确认，会告诉发送方其下一个希望收到的字节编号
* 超时重传：发送端发送数据后，会将报文段保存在一个队列中，并启动 RTO 重传计时器，如果超时都没有收到确认，重新发送

#### 4.1.6 TCP 粘包

#### 4.1.7 TCP 连接复用技术

TCP 连接复用技术通过将多个客户的 HTTP 请求复用到后端与服务器建立的一个 TCP 连接上。这种技术能够大大减小服务器的性能负载，减少与服务器之间建立TCP连接所带来的延时，并最大限度地降低客户端对服务器的并发连接请求，减少服务器的资源占用。

客**户端与负载均衡设备之间建立三次握手**并发送 HTTP 请求，负载均衡设备接收到请求后，会**检测服务端是否存在空闲的长连接**，如果不存在，就建立一个连接。当请求响应完毕后，**客户端与负载均衡设备之间断开连接，而负载均衡设备与服务器之间继续保持连接**。当有其他请求来到时，负载均衡设备会直接复用这个连接。

![](https://s4.51cto.com/attachment/201305/131348235.jpg)

HTTP 复用与 TCP 连接复用的区别在于，TCP 连接复用是将多个客户端的HTTP请求服用到一个 TCP 连接上，而 HTTP 请求复用是一个客户端的多个 HTTP 请求通过一个 TCP 连接进行处理。

### 4.2 UDP

#### UDP 首部

![](https://cs-notes-1256109796.cos.ap-guangzhou.myqcloud.com/d4c3a4a1-0846-46ec-9cc3-eaddfca71254.jpg)

### TCP和 UDP 的区别

* TCP 提供的是面向连接的、可靠的数据流传输；UDP 提供的是非面向连接的、不可靠的数据流传输

  TCP 提供可靠的服务，通过 TCP 连接传送的数据，无差错、不丢失、不重复，按序到达；UDP 尽最大努力交付，即不保证可靠交付

* TCP 面向字节流；UDP 面向报文
* TCP 连接只能是点到点的；UDP 支持一对一、一对多、多对一和多对多的交互通信
* UDP 具有较好的实时性，工作效率比 TCP 高，适用于对高速传输和实时性有较高的通信或广播通信
* TCP 对系统资源要求较多，UDP 对系统资源要求较少。TCP 首部有 20 字节；UDP 的首部只有 8 个字节
* TCP 的逻辑通信信道是全双工的可靠信道；UDP 的逻辑通信信道是不可靠信道。

#### 实现 UDP 可靠传输

在应用层模仿 TCP 的可靠传输

* 添加 seq/ack，确保数据发送到对端：发送端发送数据时生成一个随机的 seq=x
* 添加发送和接收缓冲区：数据到达后放入缓冲区，并发送要给 ack=y。发送方接收到 ack 后，删除缓冲区数据。
* 超时重传：定时检查是否需要重传数据

## 5. 应用层

### 5.1 DNS

DNS 是域名服务器，他提供了**主机名和 IP 地址之间相互转换**的服务。DNS 协议运行在 UDP 之上，使用 53 端口。

#### 分层

DNS 服务器以层次方式组织，从上到下依次为根域名服务器、顶级域名服务器、权威域名服务器。上级服务器保存这子服务器的 IP 地址。

主机向本地域名服务器的查询一般都是采用递归查询（recursive query）。本地域名服务器向根域名服务器的查询通常采用迭代查询（iterative）

**递归查询**

当主机发出 DNS 请求时，该请求会被发送到本地 DNS 服务器。本地域名服务器起着一个代理作用，负责将 DNS 请求转发到 DNS 服务器层次结构中。

如果主机所询问的本地域名服务器不知道被查询域名的IP地址, 本地域名服务器就会以DNS客户的身份, 向其他根域名服务器继续发出请求报文, \(代替主机去查询, 不是主机自己去查询\).

**迭代查询**

当用户发出一个查询请求时，会先从根服务器查询，返回一个顶级域名服务器的 IP。然后用户从顶级域名服务器中查询，返回一个权威域名服务器的 IP。最后从该权威域名服务器中查询 要查询域名的 IP 地址

DNS 服务器根据域名的层级，进行分级查询，从根域名开始，一次查询每一级域名，直到查到最终的 IP 地址

#### 缓存

在一个请求链中，当某 DNS 服务器接收到一个 DNS 回答时，将其映射缓存在本地存储器中，下次查询时能够直接提供对应的 IP 地址。

为保持高速缓存中的内容的正确，域名服务器应为 每项内容设置计时器并处理超过合理时间的项（通常为2天）。当权限域名服务器回答一个查询请求时，在响应中都指明绑定有效存在的时间值。增加此时间值可减少网络开销，而减少此时间值可提高域名转换的准确性。

本地 DNS 服务器也可以缓存 TLD 服务器的 IP 地址，因而允许本地 DNS 绕过查询链中的根 DNS 服务器。

不但在本地域名服务器中需要高速缓存，在主机中也很需要。许多主机在启动时从本地域名服务器下载名字和地址的全部数据库，维护存放自己最近使用的域名的高速缓存，并且只在从缓存中找不到名字时才使用域名服务器。

#### 查询过程

本地 host 文件 --&gt; 本地缓存 --&gt; 本地 DNS 服务器 递归查询 --&gt; DNS 服务器迭代查询

### 6.1 报文结构

HTTP 报文大致由三部分组成，即请求行/状态行、报文首部、报文实体。

#### 请求报文

请求报文结构：

* 请求行：第一行是包含了请求方法、URI、协议版本；
* 首部：接下来的多行都是请求首部 Header，每个首部都有一个首部名称，以及对应的值。
* 一个空行用来分隔首部和内容主体 Body
* 主体：最后是请求的内容主体

```markup
GET http://www.example.com/ HTTP/1.1
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8
Accept-Encoding: gzip, deflate
Accept-Language: zh-CN,zh;q=0.9,en;q=0.8
Cache-Control: max-age=0
Host: www.example.com
If-Modified-Since: Thu, 17 Oct 2019 07:18:26 GMT
If-None-Match: "3147526947+gzip"
Proxy-Connection: keep-alive
Upgrade-Insecure-Requests: 1
User-Agent: Mozilla/5.0 xxx

param1=1&param2=2
```

#### 响应报文

响应报文结构：

* 相应行：第一行包含协议版本、状态码以及描述，最常见的是 200 OK 表示请求成功了
* 首部：接下来多行也是首部内容
* 一个空行分隔首部和内容主体
* 主体：最后是响应的内容主体

```markup
HTTP/1.1 200 OK
Age: 529651
Cache-Control: max-age=604800
Connection: keep-alive
Content-Encoding: gzip
Content-Length: 648
Content-Type: text/html; charset=UTF-8
Date: Mon, 02 Nov 2020 17:53:39 GMT
Etag: "3147526947+ident+gzip"
Expires: Mon, 09 Nov 2020 17:53:39 GMT
Keep-Alive: timeout=4
Last-Modified: Thu, 17 Oct 2019 07:18:26 GMT
Proxy-Connection: keep-alive
Server: ECS (sjc/16DF)
Vary: Accept-Encoding
X-Cache: HIT

<!doctype html>
<html>
<head>
    <title>Example Domain</title>
    // 省略... 
</body>
</html>
```

#### 报文首部

**通用首部字段**

| 首部字段名 | 说明 |
| :---: | :---: |
| Cache-Control | 控制缓存的行为 |
| Connection | 控制不再转发给代理的首部字段、管理持久连接 |
| Date | 创建报文的日期时间 |
| Pragma | 报文指令 |
| Trailer | 报文末端的首部一览 |
| Transfer-Encoding | 指定报文主体的传输编码方式 |
| Upgrade | 升级为其他协议 |
| Via | 代理服务器的相关信息 |
| Warning | 错误通知 |

**请求首部字段**

| 首部字段名 | 说明 |
| :---: | :---: |
| Accept | 用户代理可处理的媒体类型 |
| Accept-Charset | 优先的字符集 |
| **Accept-Encoding** | 优先的内容编码 |
| Accept-Language | 优先的语言（自然语言） |
| Authorization | Web 认证信息 |
| Expect | 期待服务器的特定行为 |
| From | 用户的电子邮箱地址 |
| **Host** | 请求资源所在服务器 |
| If-Match | 比较实体标记（ETag） |
| If-Modified-Since | 比较资源的更新时间 |
| If-None-Match | 比较实体标记（与 If-Match 相反） |
| If-Range | 资源未更新时发送实体 Byte 的范围请求 |
| If-Unmodified-Since | 比较资源的更新时间（与 If-Modified-Since 相反） |
| Max-Forwards | 最大传输逐跳数 |
| Proxy-Authorization | 代理服务器要求客户端的认证信息 |
| Range | 实体的字节范围请求 |
| Referer | 对请求中 URI 的原始获取方 |
| TE | 传输编码的优先级 |
| User-Agent | HTTP 客户端程序的信息 |

**响应首部字段**

| 首部字段名 | 说明 |
| :---: | :---: |
| Accept-Ranges | 是否接受字节范围请求 |
| Age | 推算资源创建经过时间 |
| ETag | 资源的匹配信息 |
| Location | 令客户端重定向至指定 URI |
| Proxy-Authenticate | 代理服务器对客户端的认证信息 |
| Retry-After | 对再次发起请求的时机要求 |
| Server | HTTP 服务器的安装信息 |
| Vary | 代理服务器缓存的管理信息 |
| WWW-Authenticate | 服务器对客户端的认证信息 |

**实体首部字段**

| 首部字段名 | 说明 |
| :---: | :---: |
| Allow | 资源可支持的 HTTP 方法 |
| Content-Encoding | 实体主体适用的编码方式 |
| Content-Language | 实体主体的自然语言 |
| Content-Length | 实体主体的大小 |
| Content-Location | 替代对应资源的 URI |
| Content-MD5 | 实体主体的报文摘要 |
| Content-Range | 实体主体的位置范围 |
| Content-Type | 实体主体的媒体类型 |
| Expires | 实体主体过期的日期时间 |
| Last-Modified | 资源的最后修改日期时间 |

### 6.2 HTTP 方法

客户端发送的 请求报文 第一行为请求行，包含了方法字段

* **GET**：获取资源
* **HEAD**：获取报文首部，但是不包含报文实体
* **POST**：传输实体，主要用来传输数据
* **PUT**：传输文件，报文主题包含文件内容。不带验证机制，所有人都能上传，存在安全问题
* **PATCH**：对资源进行部分修改
* **DELETE**：删除文件，与 PUT 一样不带验证机制
* **OPTIONS**：询问支持的方法，返回支持的 HTTP 方法
* **TRACE**：追踪路径。返回通信路径，容易收到 XST\(跨站追踪\) 攻击
* **CONNECT**：要求在与代理服务器通信时建立隧道

**GET 与 POST 区别**

* 作用：GET 请求主要用于获取资源，POST 请求主要用于传输实体
* 参数：GET 的参数是以字符串的形式出现在 URL 中的，而 POST 的参数是存储再实体主体中
* 安全：GET 方法是安全的，而 POST 不是安全的。因为 POST 可能会修改服务器的状态
* 幂等性：GET 是幂等的，POST 不是幂等的

> 安全的 HTTP 方法不会改变服务器状态，也就是说它是制度的
>
> 幂等的 HTTP 方法，同样的请求被执行一次与连续执行多次的效果是一样的，服务器的状态也是一样的。

### 6.3 状态码

* **100 Continue**：到目前为止正常

**2XX**

* **200 OK**
* **204 No Content**：消息成功处理，但是返回的响应报文不包含实体部分
* **206 Partial Content**：表示客户端进行了范围请求，响应报文包含由 Content-Range 指定范围的实体内容。

**3XX 重定向**

* **301 Moved Permanently**：永久重定向
* **302 Found**：临时重定向
* **303 See Other**：临时重定向，要求客户端采用 GET 获取资源
* **304 Not Modified**：请求报文首部包含条件\(if-Match、if-Modified-Since...\)，但是条件不满足
* **307 Temporary Redirect**：临时重定向，要求浏览器不会POST 改成 GET

**4XX 客户端错误**

* **400 Bad Request**：请求报文中存在语法错误
* **401 Unauthorized**：当前请求需要用户验证或认证失败
* **403 Forbidden**：请求被拒绝
* **404 Not Found**：请求希望得到的资源未在服务器上发现
* **405 Method Not Allowed**：请求行中指定的请求方法不能被用于请求相应资源

**5XX 服务器错误**

* **500 Internal Server Error**：，服务器执行请求时发生错误
* **502 Bad Gateway**： 网关或代理服务器尝试执行请求时接收到无效响应。一般是服务器过载
* **503 Service Unavailable**：，服务器临时过载或正在维护，现在无法处理请求
* **504 Gateway Timeout**： 网关或代理服务器尝试执行请求时未能及时接收到响应。

### 6.4 连接管理

#### 长连接与短链接

HTTP 请求时建立在 TCP 连接之上的，而 TCP 连接分为长连接和短链接

**短连接**

短链接是指对于每一个 HTTP 请求都建立一个连接，传输完数据后断开连接。

HTTP/1.0 中默认使用短链接

**长连接**

长连接是指发送 HTTP 请求时先建立一个 TCP 连接，请求发送完毕后先不关闭 TCP 连接，之后的 HTTP 请求可以复用这个 TCP 连接。

**keep-alive**

keep-alive 是客户端和服务端的一个约定，如果开启 keep-alive，则服务端在返回 response 后不关闭 TCP 连接；同样的，在接收完响应报文后，客户端也不关闭连接，发送下一个 HTTP 请求时会重用该连接。

#### Cookie

HTTP 协议是无状态的，主要是为了让 HTTP 协议尽可能简单，使得它能够处理大量事务。HTTP/1.1 引入 Cookie 来保存状态信息。

Cookie 是服务器发送到用户**浏览器并保存在本地的一小块数据**，它会在浏览器之后向同一服务器再次发起请求时被携带上，用于告知服务端两个请求是否来自同一浏览器。由于之后每次请求都会需要携带 Cookie 数据，因此会带来额外的性能开销（尤其是在移动环境下）。

**用途：**

* 会话状态管理（如用户登录状态、购物车、游戏分数或其它需要记录的信息）
* 个性化设置（如用户自定义设置、主题等）
* 浏览器行为跟踪（如跟踪分析用户行为等）

**创建过程**

1. 服务器发送的响应报文包含 **Set-Cookie** 首部字段，客户端得到响应报文后将 Cookie 内容保持到浏览器
2. 客户端对服务器发送请求时，从浏览器提取 Cookie 发送给浏览器

#### Session

当客户端访问服务器时，服务器根据需求设置 Session，将会话信息保存在服务器上，同时将标示 Session 的 SessionId 传递给客户端浏览器，以后浏览器每次请求都会额外加上这个参数值，服务器会根据这个 SessionId，就能取得客户端的数据信息。

如果浏览器禁用 cookie，可以在 URL 中携带 sessionId

#### 请求过程

**1.解析 URL**

```markup
http://www.baidu.com :80  /file1.html? user=1
协议   Web 服务器域名  端口 数据源路径        参数
```

**2. DNS 查询域名**

本地 host 文件 --&gt; 本地缓存 --&gt; 本地 DNS 服务器 递归查询 --&gt; DNS 服务器迭代查询

**3. 建立 TCP 连接**

**4. 发送 HTTP 请求**

**5. 网卡收到网络包，将电信号转换为数字信号，交给上层**

报头的波形同步时钟，起始帧分界符，MAU 模块，MAC 模块，检查 FCS，检查 MAC 地址，通知知计算机收到一个包

报文的波形同步时钟，然后遇到起始帧分界符时，开始将后面的信号转换为数字信号。

MAU 模块会将信号转换成通用格式并发送给 MAC 模块，MAC 模块再从头开始将信号转换为数字信号，并存放在缓冲区中。

当到达信号末尾时，检查 FCS， 出错，丢弃。

检查 MAC 头部中介绍放的 MAC 地址与网卡的 MAC 地址是否一致。不一致，丢弃，否则将包放在缓冲区。

网卡向扩展总线中的中断信号发送中断信号（硬件的中断号和相应的驱动程序绑定），中断程序从缓冲区中读取数据。

IP 模块检查数据包接受方的 IP 地址是否本机 IP 地址，不是，发送 ICMP 报文通知错误

TCP 模块找到对应的套接字，放入缓冲区，等待应用层读取数据

**5. 服务端处理请求，并响应**

**5.浏览器得到响应，解析信息**

一旦浏览器收到数据的第一块，它就可以开始解析收到的信息。“推测性解析”，“解析” 是浏览器将通过网络接收的数据转换为 DOM 和 CSSOM 的步骤，通过渲染器把 DOM 和 CSSOM 在屏幕上绘制成页面。

**6. 渲染**

渲染步骤包括**样式、布局、绘制**，在某些情况下还包括合成。在解析步骤中创建的 CSSOM 树和 DOM 树组合成一个 Render 树，然后用于计算每个可见元素的布局，然后将其绘制到屏幕上。在某些情况下，可以将内容提升到它们自己的层并进行合成，通过在 GPU 而不是 CPU 上绘制屏幕的一部分来提高性能，从而释放主线程。

**上面过程所有请求到达主机前都会经过 ARP 将 IP 转化为 MAC 地址的过程**

### 6.5 HTTP 版本

#### HTTP 1.x

* 基于 TCP
* **队头阻塞**，每个 TCP 连接同时只能处理一个请求-响应。如果上一个响应没有返回，后续请求-响应都会阻塞
  * 域名分片，将同一页面资源分散到不同域名下
* 浏览器最多同时处理 6~8 个 TCP 连接
* **明文传输**：不安全

**HTTP 1.0**：

* 默认使用短连接，每次请求都需要重新建立一次连接

**HTTP 1.1**

* 默认使用长连接，减少 TCP 建立连接耗费的时间
* 支持请求流水线 Pipeline，客户端收到 HTTP 响应报文之前就能接着发送新的请求报文。通过建立多个连接来实现
* 支持断点续传，返回 206\(Partial Content\)

#### HTTP 2.0

* 头部压缩：使用 HPACK 算法，每次请求和响应只发送差异头部，一般可以达到 50%~90% 的高压缩率。
* 采用**多路复用技术**，将报文以二进制的形式编码，同时将其分成多个帧，叫做**二进制帧**。同一个请求的二进制帧带有相同的标识，供接收方将其拼凑成一个完整的报文
* 同一域名下的所有请求建立在一个 TCP 连接上

**缺陷：**

* 如果传输中发生数据丢包，即使丢失的数据仅涉及单个请求，所有请求和响应也同样会受到数据包丢失的影响而需要重传。因为尽管 HTTP/2 可以在不同的流上隔离不同的 HTTP 交换，但是底层的 TCP 并无法对他们进行区别，TCP 能看到的只是没有任何标志的字节流。
* 多路复用没有限制同时请求数。请求的平均数量与往常相同，但实际会有许多请求的短暂爆发，导致瞬时 QPS 暴增。

**二进制帧**

帧是数据传输的最小单位，以二进制传输代替原本的明文传输，原本的消息被分为更小的数据帧

**多路复用**

多路复用通常表示在一个信道上传输**多路**信号或数据流的过程和技术。

在一个 TCP 连接上，可以向对方不断发送帧，每帧的 stream identifier 都标明这一帧属于哪个流。接收方接收时，根据 stream identifier 拼接每个流的所有帧组成一整块数据。

流的概念实现了单连接上多请求 - 响应并行，解决了线头阻塞的问题，减少了 TCP 连接数量和 TCP 连接慢启动造成的问题

* 同域名下所有通信都在单个连接上完成
* 单个连接可以承载任意数量的双向数据流
* 数据流以消息的形式发送，而消息由一个或多个帧组成，多个帧之间可以乱序发送，以标识符重连。

#### HTTP 3.0

* 基于 TCP，使用 QUIC（quick udp internet connection）
* 无需等待应答，一旦建立连接，只管数据的发送

### HTTPs

HTTPS 是以安全为目标的 HTTP 通道，它在 HTTP 中加入 SSL 层以提高数据传输的安全性。

HTTP 被用于在 Web 浏览器和网站服务器之间传递信息，但以明文方式发送内容，不提供任何方式的数据加密，如果攻击者截取了 Web 浏览器和网站服务器之间的传输报文，就可以直接读懂其中的信息，因此 HTTP 不适合传输一些敏感信息，比如身份证号码、密码等。

为了数据传输的安全，HTTPS 在 HTTP 的基础上加入了 SSL 协议，SSL 依靠证书来验证服务器的身份，并对浏览器和服务器之间的通信进行数据加密，以保障数据传输的安全性，

#### 加密算法

**1. 对称加密**

**对称加密加密和解密都使用同一个密钥**。常见的有 DES、DES3、AES 等

优点：加密速度快、效率高，适合加密比较大的数据

缺点：

* 安全性：密钥在传输过程中可能被截获
* 密钥管理：每次使用对称加密算法时，都需要使用他人不知道的唯一密钥，这回使得收发信双方用用密钥数量急剧增长

**2. 非对称加密**

**非对称加密加密和解密使用不同的密钥**，使用公钥加密，私钥解密。常见的有 RSA

公开密钥所有人都可以获得，通信发送方获得接收方的公开密钥之后，就可以使用公开密钥进行加密，接收方收到通信内容后使用私有密钥解密。

* 优点：算法公开，加密和解密使用不同的钥匙，私钥不需要通过网络进行传输，安全性很高。
* 缺点：计算量比较大，加密和解密速度相比对称加密慢很多。

**3. HTTPS 采用的加密方式**

上面提到对称密钥加密方式的传输效率更高，但是无法安全地将密钥传输给通信方。而非对称密钥加密方式可以保证传输的安全性，因此可以利用非对称密钥加密方式将密钥传输给通信方。HTTPS 采用混合的加密机制，正是利用了上面提到的方案：

* 使用非对称加密方式，传输对称加密方式所需要的 Secret Key，从而保证安全性；
* 获取到 Secret Key 后，再使用对称密钥加密方式进行通信，从而保证效率。（下图中的 Session Key 就是 Secret Key）

#### CA 认证

#### **数字证书**

服务器的运营人员向 CA 提出公开密钥的申请，CA 在判明提出申请者的身份之后，会对已申请的公开密钥做数字签名，然后分配这个已签名的公开密钥，并将该公开密钥放入公开密钥证书后绑定在一起。

进行 HTTPS 通信时，服务器会把证书发送给客户端。客户端取得其中的公开密钥之后，先使用数字签名进行验证，如果验证通过，就可以开始通信了。

#### SSL/TLS

TLS 由若干不同职责的模块组成，比较常见的由握手协议、变更密码规范协议、镜报协议和记录协议等。

![](https://upload-images.jianshu.io/upload_images/2573196-14bab45e133d24e6.png)

从体系结构图可以看出，SSL/TLS 协议可分为两层：握手协议和记录协议

* 握手协议：握手协议用来协商会话参数，建立在 SSL 记录协议之上，用于在实际传输数据之前，通信双方进行身份认证、协商加密算法、交换会话密钥
* 记录协议：建立在可靠的传输协议（如 TCP）之上，为高层协议提供数据封装、压缩及加密等基本功能支持。

#### 连接过程

1. **建立 TCP 连接**：客户端通过 TCP 和服务器建立连接
2. **发起请求**：发送一个请求证书给服务端，在该请求消息里包含一个随机数、支持的加密算法列表等信息
3. **证书返回**：服务端收到消息后回应客户端，响应包括数字证书、加密算法、“server random” 随机数等信息
4. **证书验证**：客户端收到证书后，验证证书签发机构，并使用该签发机构的公钥确认签名是否有效，验证证书中域名是否是连接的域名。如果证书有效，生成一个"premaster secret" 随机数，并使用公钥加密，发送给服务端。
5. **密钥交换**：服务端接收后使用私钥解密，利用 client random、server random、premaster secret 和一定的算法生成对称密钥，发送给客户端
6. **数据传输**：客户端和客服端进行使用对称密钥进行数据传输

#### HTTP 与 HTTPs 的区别

* HTTP 使用 80 端口，HTTPs 使用 443 端口
* HTTP 效率比 HTTPs 更高，HTTPs 加密会浪费时间并且会增加密钥交换的网络开销
* HTTPs 更加安全

