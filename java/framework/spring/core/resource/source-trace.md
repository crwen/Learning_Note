# 资源解析源码追踪

```java
public static void main(String[] args) {
    ApplicationContext context = new ClassPathXmlApplicationContext("spring/spring-config.xml");
}
```

## 构造器

```java
public ClassPathXmlApplicationContext(
    String[] configLocations, boolean refresh, @Nullable ApplicationContext parent)
    throws BeansException {

    super(parent);
    // 设置配置文件路径
    setConfigLocations(configLocations);
    if (refresh) {
        refresh();
    }
}
```

在 `ClassPathXmlApplicationContext` 的构造器中主要做了三件事情：

* 调用父类构造器，创建资源解析器
* 设置配置文件路径
* 调用 refresh 方法，创建容器

**父类构造器**

`ClassPathXmlApplicationContext` 会调用父类 `AbstractApplicationContext` 的构造方法

```java
public AbstractApplicationContext(@Nullable ApplicationContext parent) {
    this();
    setParent(parent);
}

public AbstractApplicationContext() {
    this.resourcePatternResolver = getResourcePatternResolver();
}

protected ResourcePatternResolver getResourcePatternResolver() {
    return new PathMatchingResourcePatternResolver(this);
}
```

在 `AbstractApplicationContext` 的构造方法中为资源解析器赋值，`PathMatchingResourcePatternResolver` 支持Ant风格的路径解析。

## 设置配置文件路径

```java
public void setConfigLocations(@Nullable String... locations) {
    if (locations != null) {
        Assert.noNullElements(locations, "Config locations must not be null");
        this.configLocations = new String[locations.length];
        for (int i = 0; i < locations.length; i++) {
            this.configLocations[i] = resolvePath(locations[i]).trim();
        }
    }
    else {
        this.configLocations = null;
    }
}

protected String resolvePath(String path) {
    return getEnvironment().resolveRequiredPlaceholders(path);
}
```

`setConfigLocations` 方法的目的在于将占位符\(placeholder\)解析成实际的地址。比如 `new ClassPathXmlApplicationContext("classpath:config.xml");`那么classpath:就是需要被解析的。

## 资源解析

以 xml 配置的配置文件的解析工作实在 `refresh()` 方法的 `obtainFreshBeanFactory` 方法中进行的。该方法对 xml 进行解析，并将 bean 封装为 `BeanDefinition` 对象，并注册到容器中，最后返回一个 beanFactory 工厂。

```java
public void refresh() throws BeansException, IllegalStateException {
    // 避免容器处在 refresh 阶段时，容器进行了初始化或者销毁的操作
    synchronized (this.startupShutdownMonitor) {
        ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
    }
}

protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
    refreshBeanFactory();
    return getBeanFactory();
}
```

在 `refreshBeanFactory` 方法中创建了一个 `DefaultListableBeanFactory` 工厂，完成加载工作后再 `getBeanFactory` 中将其返回。

```java
protected final void refreshBeanFactory() throws BeansException {
    // 创建一个 DefaultListableBeanFactory 对象
    DefaultListableBeanFactory beanFactory = createBeanFactory();
    beanFactory.setSerializationId(getId());
    // 判断是否允许覆盖 bean，是否允许循环依赖
    customizeBeanFactory(beanFactory);
    // 加载
    loadBeanDefinitions(beanFactory);
    synchronized (this.beanFactoryMonitor) {
        this.beanFactory = beanFactory;
    }
}
```

主要的加载工作是在 `loadBeanDefinitions` 中完成的。

```java
protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
    // BeanDefinitionReader 将配置文件转化为 BeanDefinition，定义了一些加载资源的方法
    XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
    loadBeanDefinitions(beanDefinitionReader);
}

protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
    Resource[] configResources = getConfigResources();
    if (configResources != null) {
        reader.loadBeanDefinitions(configResources);
    }
    String[] configLocations = getConfigLocations();
    if (configLocations != null) {
        reader.loadBeanDefinitions(configLocations);
    }
}

public int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException {
    Assert.notNull(locations, "Location array must not be null");
    int count = 0;
    for (String location : locations) {
        count += loadBeanDefinitions(location);
    }
    return count;
}
```

在 `loadBeanDefinitions` 中创建了一个 `BeanDefinitionRead` 对象，这个对象的作用是将配置文件转化为 `BeanDefinition` 对象，并且定义了一些资源加载的方法。后面资源的加载就是通过这个类的子类去完成的。创建了解析器对象之后，通过遍历资源路径，对每个资源路径都调用了 `loadBeanDefinitions` 方法。在这个方法中，通过 `getResource` 方法将资源路径转换为 `Resource` 对象，然后继续进行加载。

```java
public int loadBeanDefinitions(String location, @Nullable Set<Resource> actualResources) throws BeanDefinitionStoreException {
    //获取资源加载类，主要的作用就是根据路径和类加载器获取 Resource 对象
    ResourceLoader resourceLoader = getResourceLoader();

    // ResourcePatternResolver 用于加载 多个文件 或者能够加载 Ant 风格路径的文件资源
    if (resourceLoader instanceof ResourcePatternResolver) {
        Resource[] resources = ((ResourcePatternResolver) resourceLoader).getResources(location);
        int count = loadBeanDefinitions(resources);
        return count;
        }
    }else {
        // 默认的 ResourceLoader 实例，加载单个文件资源
        Resource resource = resourceLoader.getResource(location);
        int count = loadBeanDefinitions(resource);
        return count;
    }
}
```

### **将资源路径转化为 Resource 资源**

```java
public Resource getResource(String location) {
    Assert.notNull(location, "Location must not be null");

    // ProtocolResolver：自定义资源路径
    for (ProtocolResolver protocolResolver : getProtocolResolvers()) {
        Resource resource = protocolResolver.resolve(location, this);
        if (resource != null) {
            return resource;
        }
    }

    // 如果以 "/" 开头，则构造 ClassPathContextResource
    if (location.startsWith("/")) {
        return getResourceByPath(location);
    }
    // 如果以 "classpaath:" 开头，则构造 ClassPathResource
    else if (location.startsWith(CLASSPATH_URL_PREFIX)) {
        return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader());
    }
    else {
        // 构造 URL，尝试通过它进行资源定位，若没有，ClassPathContextResource
        // 判断是否为 FileURL，如果是，构造 FileURLResource，否则构造 URLResource
        try {
            URL url = new URL(location);
            return (ResourceUtils.isFileURL(url) ? new FileUrlResource(url) : new UrlResource(url));
        }
        catch (MalformedURLException ex) {
            // ClassPathContextResource
            return getResourceByPath(location);
        }
    }
}
```

在 `getResource` 方法中，根据路径的的形式，构造不同的 资源实例

### **加载解析资源**

获取到资源后，会调用 `XmlBeanDefinitionReader` 的 `loadBeanDefinitions` 方法进行解析资源

```java
public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
    InputStream inputStream = encodedResource.getResource().getInputStream();
    InputSource inputSource = new InputSource(inputStream);
    // 进行真正的解析
    return doLoadBeanDefinitions(inputSource, encodedResource.getResource());
}

protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource)
    throws BeanDefinitionStoreException {
    // 获取 dom 树
    Document doc = doLoadDocument(inputSource, resource);
    // 解析 dom 树，保存到 BeanDefinition 中，并向容器中注册 BeanDefinition
    int count = registerBeanDefinitions(doc, resource);
    return count;

}
```

`XmlBeanDefinitionReader` 的 `loadBeanDefinitions` 方法调用了 `doLoadBeanDefinitions` 方法进行真正的解析工作。在这个方法中先获取了 dom 树，然后对 dom 树进行解析，将解析得到的 `BeanDefinition` 注册到容器中。

#### **解析 DOM 树**

```java
public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
    // 创建 BeanDefinitionReader，从 dom 树中读取 BeanDefinition
    BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
    // 获取注册表 beanDefinitionMap 在本次加载前的 BeanDefinition 数量
    int countBefore = getRegistry().getBeanDefinitionCount();
    // 加载并注册
    documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
    // 本次加载后容器里 BeanDefinition 的数量减去先前的，即本次加载的 BeanDefinitino 数量
    return getRegistry().getBeanDefinitionCount() - countBefore;
}

public int getBeanDefinitionCount() {
       // beanDefinitionMap 是一个 ConcurrentHashMap<String, BeanDefinition>
    return this.beanDefinitionMap.size();
}
```

`registerBeanDefinitions` 方法返回的是本次注册的 `BeanDefinition` 的数量。其实现方法是在注册之前获取容器中 `BeanDefinition` 的数量，注册之后再获取一遍数量，最后返回其差值。

#### **注册 BeanDefinition**

```java
public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
    this.readerContext = readerContext;
    doRegisterBeanDefinitions(doc.getDocumentElement());
}

protected void doRegisterBeanDefinitions(Element root) {
    // BeanDefinition 解析委托类，用来解析 Document 对象
    BeanDefinitionParserDelegate parent = this.delegate;
    this.delegate = createDelegate(getReaderContext(), root, parent);
    // 检测是否符合 spring 规范，并进行一些处理
    //"http://www.springframework.org/schema/beans"
    if (this.delegate.isDefaultNamespace(root)) {}
    // preProcessXml postProcessXml 都是钩子方法
    preProcessXml(root);
    parseBeanDefinitions(root, this.delegate);
    postProcessXml(root);
    this.delegate = parent;
}
```

进行注册的方法调用了 `doRegisterBeanDefinitions` 方法进行注册，这个方法主要讲注册逻辑委托给了 `BeanDefinitionParserDelegate` 对象，调用 `parseBeanDefinitions` 进行委托。在该方法中，会遍历根节点下的所有子节点，然后调用相应的方法进行解析。如果没有定义扩展的自定义代理类，一般会使用 Spring 提供的逻辑进行解析。在 `parseDefaultElement` 方法中分别对 xml 文件中共的 import alias bean beans 标签进行了解析。

```java
protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
    // 如果是默认的名命空间
    if (delegate.isDefaultNamespace(root)) {
        // 遍历所有子节点
        for (int i = 0; i < nl.getLength(); i++) {
            if (delegate.isDefaultNamespace(ele)) {
                // 按照 spring 原有逻辑进行解析
                parseDefaultElement(ele, delegate);
            }
            else {
                // 否则使用扩展的自定义代理类进行解析
                delegate.parseCustomElement(ele);
            }
        }
    }else {
        // 使用扩展的自定义代理类进行解析
        delegate.parseCustomElement(root);
    }
}

private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
    // 如果元素节点是 <import> 导入元素，进行导入解析
    //<import resource="classpath:applicationContext-datasource.xml"/>
    if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
        importBeanDefinitionResource(ele);
    }
    // 如果元素节点是 <Alias> 别名元素，进行别名解析
    else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
        processAliasRegistration(ele);
    }
    // 普通 <bean> 元素，按照 Spring 的 Bean 规则解析
    else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
        processBeanDefinition(ele, delegate);
    }
    // 如果是 <beans> 元素，注册
    else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
        // recurse
        doRegisterBeanDefinitions(ele);
    }
}
```

bean 的解析

```java
protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
    //BeanDefinitionHolder 是对 BeanDefinition 的封装，即 Bean 定义的封装类
    // 对 Document 对象种 <bean> 元素的解析由 BeanDefinitionParserDelegate 实现
    BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
    if (bdHolder != null) {
        bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
        try {
            // 向 IOC 容器注册解析得到的 BeanDefinition
            BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
        }
        catch (BeanDefinitionStoreException ex) {
            getReaderContext().error();
        }
        getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
    }
}

public BeanDefinitionHolder parseBeanDefinitionElement(Element ele, @Nullable BeanDefinition containingBean) {
    AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean);
    return new BeanDefinitionHolder(beanDefinition, beanName, aliasesArray);
}

public AbstractBeanDefinition parseBeanDefinitionElement(
    Element ele, String beanName, @Nullable BeanDefinition containingBean) {
    AbstractBeanDefinition bd = createBeanDefinition(className, parent);
    // 进行一些列的设置
    return bd;
}
```

在 bean 的解析中，创建出了 `BeanDefinition` 后将其包装到了 `BeanDefinitionHolder` 对象中。而 `BeanDefinition` 是通过 `new GenericBeanDefinition` 得到的。

bean 的注册

```java
public static void registerBeanDefinition(
    BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry)
    throws BeanDefinitionStoreException {
    // 将 beandefinition 及其名字注册到容器中
    String beanName = definitionHolder.getBeanName();
    registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());

    // 如果存在别名，则逐个注册进容器 <alias name="name" alias="xx"/>
    String[] aliases = definitionHolder.getAliases();
    if (aliases != null) {
        for (String alias : aliases) {
            registry.registerAlias(beanName, alias);
        }
    }
}

public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
    throws BeanDefinitionStoreException {
    BeanDefinition existingDefinition = this.beanDefinitionMap.get(beanName);
    if (existingDefinition != null) {
        // 如果不允许覆盖，则抛出异常，否则覆盖
        if (!isAllowBeanDefinitionOverriding()) {
            throw new BeanDefinitionOverrideException();
        }
        this.beanDefinitionMap.put(beanName, beanDefinition);
    }else {
        // 将 beanName到 beanDefinition 的映射加入到beanDefinitionMap
        this.beanDefinitionMap.put(beanName, beanDefinition);
        this.beanDefinitionNames.add(beanName);
        removeManualSingletonName(beanName);
    }

    // 检查是否有同名的 BeanDefinition 已经在 IOC 容器中注册
    if (existingDefinition != null || containsSingleton(beanName)) {
        // 重置所有已经注册过的 BeanDefinition 的缓存
        // beanDefinition 的缓存（BeanDefinition会把parent的 BeanDefinition属性合并在一起）
        resetBeanDefinition(beanName);
    }
}
```

从代码可以看出，注册主要就是调用了一行代码，将 beanName 到 `BeanDefinition` 的映射加入到容器中。如果容器中已经注册过 beanName，则根据配置来判断是否允许覆盖。

```java
this.beanDefinitionMap.put(beanName, beanDefinition);
```

