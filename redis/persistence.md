# 持久化

Redis 有两种持久化方式，分别是 RDB 和 AOF

## RDB

### RDB 触发机制

有三种触发 RDB 的方法，分别是

* `SAVE` 同步命令：`SAVE` 命令回阻塞 Redis 服务器进程，直到 RDB 文件创建完毕为止。这时客户端发送的所有命令都会被阻塞
* `BGSAVE` 异步命令：`BGSAVE` 命令回 `fork` 出一个进程用于创建 RDB  文件
* 自动触发：在配置文件中配置，比如 `save 60 10000`，当满足这个条件时，会使用 `BGSAVE` 生成 RDB 文件

需要注意的是，在 `BGSAVE` 命令执行期间，如果客户端发送 `SAVE` 、`BGSAVE` 命令都会被拒绝。因为这两个命令最终都会调用 `rdbSave` 方法，如果同时执行会产生竞争条件。

对于自动保存生成 RDB 文件的方式，Redis 会设置服务器状态 redisServer 结构的 saveparams 属性

```c
struct redisServer {
    // ...
    struct saveparam *saveparams; // 记录保存条件的数组
    //...
    long long dirty; // 记录距上次执行 SAVE/BGSAVE 后，服务器对数据库状态做了多少次修改
    time_t lastsave; // 记录上次保存的时间戳
}
```

Redis 会周期性地操作函数 `serverCron`，检查 `save` 选项所设置地保存条件是否已经满足（默认每个 100ms 执行依次）。 程序会遍历并检查 `saveparams` 数组中的所有保存条件，只要有一个条件满足，就会执行 `BGSAVE` 命令。

### RDB 文件结构

* REDIS：保存 "REDIS" 5 个字符，用于快速检测所载入的文件是否是 RDB 文件，占用 5 字节
* `db_version`：记录 RDB 文件的版本号，如 "0006" 代表第六版，占用 4 字节
* `databases`：包含任意个数据库，长度随数据库的键值对变化
  * `SELETCDB`：标识接下来要读入的时一个数据库号码
  * `db_number`：数据库号码
  * `key_value_pairs`：保存对应数据库中所有的键值对
    * `TYPE`：记录了 `value` 的类型
    * `key`：键值对的键，是一个 `REDIS_RDB_TYPE_STRING` 类型的字符串对象
    * `value`：保存的值
    * `EXPIRETIME_MS`：标识接下来要读取的是国企时间，单位为毫秒。占用 1 字节
    * `ms`：过期时间的时间戳形式
* `EOF`：标志 RDB 文件正文内容的结束。占用 1 字节
* `check_sum`：通过对 REDIS、db\_version、databases、EOF 四部分的内容计算得出。在载入 RDB 文件时会通过 check\_sum 和计算得出的值进行对比，以此来检查 RDB 文件是否出错或损坏

## AOF

