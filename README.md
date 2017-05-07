# jaxrsapi说明
>这个是一个框架的核心，用于处理服务器之间依据restful规范通信的网络通信的底层实现。
## 它是怎么运作的
>项目本身来自wildfly-swarm中的jarsapi组件，该组件的作用是通过接口就可以访问远程服务器，
当然这里理念不是什么新奇的，但是，其先进性在于进行通信的时候，只需要接口，而不需要考虑
网络通信部分，然而其一些理念我是不认同的，比如服务器信息必须在接口文件上指定，彼此访问
都需要新建一个HttpClient然后划归10个线程给其使用，这个明显是一种浪费。而服务器信息是一种
共通性信息，不应该属于接口(这里接口应该属于服务器)，所以在这种情况想，我完成了自己的jarxsapi
框架。<br> 
对于一台服务器，拥有一个激活器对应，激活器中存储者该服务器的所有信息和访问该服务器的线程数量
一个远程服务器只有一个线程池控制访问。其中激活器中同时也存放这可以访问该服务器的所有接口信息。

```java
	/**
	 * 获取基础路径地址
	 * @return
	 */
	String getBaseUrl();
	
	/**
	 * 获取接口列表
	 * @return
	 */
	Set<Class<?>> getClasses();
```

这是最主要的两个接口，getBaseUrl获取服务器的url，getClasses获取绑定在该服务器上的接口。
其实框架的重点在于我们**不需要**进行通信层的编码，在系统运行的时候会自动勾结接口的实现，
该部分需要感谢Jboss社区的restesay-client组件，我们可以通过该组件的IsHttpMethod方法获取
Http方法代理，该技术使用的sun的基本代理Proxy实现的，当然在这之上，还需呀一层业务代理，
wildfly-swarm使用的是ow2组件，相比ow2组件的可读性，我选择javassist作为我的代码自动生成部分。
因为在业务代理生产中，其中包括拦截器，默认值，数据验证等业务的生产，使用javassist比较简单一些。

```java
	// 初始化工厂
	NSCFactory.build(MpServerActivator.class);
	UserRest rest = NSCFactory.get(UserRest.class, MpWxConsts.NAMED);
```

在实际生成环境中，需要使用NSCFactory工程对激活器中的接口对应的实体进行生产，然后通过工程的get方法获取，
NSC是native service client缩写。用于生产接口实现体，但是如果使用jboss的框架（wildlfy或wildfly-swarm）
则不比使用该工程，ClientServiceFactory是更好的选择。

## 它的优势
>开始编程的时候，我写的代码是业务代码，代码所做的事情是一件事情，后来我喜欢上反射，写的
代码完成的是一类事情，然后是注解和代码动态生成，事情理念和反射差不多，但是在执行速度
上基本没有损耗。这是处于这样的原因，我系统构建一个可以根据约定，一种接口上注解的约定，
动态生成业务实现的代码，写的是一类事情，完成的确实大量重复的业务。<br>
通过SystemValue, ThreadValue, DefaultValue, InterceptParam注解，我们可以完成对参数的赋值
和格式化，通过InterceptResult我们可以完成对结果集的拦截和修正。LogicProxy来控制通过本地模拟
远程访问得到的结果集。然后这些我们通过javassist方式动态构建远程访问的执行代码达到访问控制。<br>
这样不仅我们可以仅仅通过接口就可以进行服务器之间restful标准的通信，可以以就一些值记性忽略，
使其通过注解向系统，激活器，线程，默认值，拦截器中获取数据，达到接口使用难道的进一步简化。<br>
需要强调的是，Restesay的Client是线程非安全的，因为其使用了httpclient作为底层通信的依据。
这里我提供了ClientHttpEngineProxy4和ClientHttpEngineProxy43这2个client http engine所为resteasy client
引擎。使其具有线程安全性，这样，只需要一个Client就可以同时反问多个远程服务器接口，对服务器的通信
负载具有很好的控制性。在使用过程中通过ClientBuilderFactory.initHttpEngineThreadSaft对客户端引擎进行初始化

```java
		ClientBuilder clientBuilder = createClientBuilder();// 配置网络通信内容
		if (clientBuilder instanceof ResteasyClientBuilder) {
			ResteasyClientBuilder rcBuilder = (ResteasyClientBuilder) clientBuilder;
			if (executor != null) {
				rcBuilder.asyncExecutor(executor); // 配置线程池，默认使用线程池为固定大小最大10个线程
			}
			if (providerFactory != null) {
				rcBuilder.providerFactory(providerFactory);
			}
			ClientBuilderFactory.initHttpEngineThreadSaft(rcBuilder); // 设定HTTP客户端引擎的线程安全性
		}
		Client client = clientBuilder.build();
		// 加入Produces矫正监听器
		client.register(WxClientResponseFilter.class);
		return client;
```

## 总结
>我希望提出一个符合现代理念，在javaee规范上的服务器通信框架，这样，api包拿到手，我们只需要写一个远程服务器的描述
激活器就可以记性远程访问的框架，来解放大批工作量，程序员不需要考虑网络是怎么通信的，就像RMI一样。程序员只需要
关注业务需求的产品，像调用本地服务一样调用远程服务，设置写起来比本地服务器还简单。<br>
还有我是jboss社区的忠实粉丝，目前在研究和使用wildfly-swarm框架，为公司节省人力成本。所以开发过程中，使用的框架
和技术基本上都是来自jboss。

## 框架使用开源清单
>resteasy(网络通信)<br>
reflections(反射控制)<br>
jandex(代码自分析，比通过反射方式访问代码的结构和注解更加方便)<br>
fasterxml(结果集转换为对象)<br>

