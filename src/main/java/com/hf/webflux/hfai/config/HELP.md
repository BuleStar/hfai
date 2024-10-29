## 在 Spring Boot 启动之后，可以通过以下手段实现缓存预热：
### 1.使用启动监听事件实现缓存预热。使用 @PostConstruct 注解实现缓存预热。
### 2.使用 CommandLineRunner 或 ApplicationRunner 实现缓存预热。

#### CommandLineRunner 和 ApplicationRunner 区别如下：
##### 方法签名不同：
* CommandLineRunner 接口有一个 run(String... args) 方法，它接收命令行参数作为可变长度字符串数组。
* ApplicationRunner 接口则提供了一个 run(ApplicationArguments args) 方法，它接收一个 ApplicationArguments 对象作为参数，这个对象提供了对传入的所有命令行参数（包括选项和非选项参数）的访问。
##### 参数解析方式不同：
* CommandLineRunner 接口更简单直接，适合处理简单的命令行参数。ApplicationRunner 接口提供了一种更强大的参数解析能力，可以通过 ApplicationArguments 获取详细的参数信息，比如获取选项参数及其值、非选项参数列表以及查询是否存在特定参数等。
##### 使用场景不同：
* 当只需要处理一组简单的命令行参数时，可以使用 CommandLineRunner。
### 3.通过实现 InitializingBean 接口，并重写 afterPropertiesSet 方法实现缓存预热。
### 小结
缓存预热是指在 Spring Boot 项目启动时，预先将数据加载到缓存系统（如 Redis）中的一种机制。它可以通过监听 ContextRefreshedEvent 或 ApplicationReadyEvent 启动事件，或使用 @PostConstruct 注解，或实现 CommandLineRunner 接口、ApplicationRunner 接口，和 InitializingBean 接口的方式来完成