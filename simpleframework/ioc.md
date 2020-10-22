# IOC

实现 IOC 主要分三步：

1. 资源定位（提取标记对象）
2. 容器创建与加载
3. 依赖注入

## 提取标记对象

* 指定范围，获取范围内的所有类
* 遍历所有类，获取被注解标记的类并加载进容器

### 获取包下类集合

1. 获取类加载器，是为了获取项目发布的**实际路径**
2. 通过类加载器获取到加载的资源
3. 依据不同的资源类型，采用不同的方式获取资源的集合

```java
public static Set<Class<?>> extractPackageClass(String packageName) {
    // 1. 获取类加载器，是为了获取项目发布的实际路径
    ClassLoader classLoader = getClassLoader();
    // 2. 通过类加载器获取到加载的资源
    URL url = classLoader.getResource(packageName.replace(".", "/"));
    if (url == null) {
        log.warn("unable to retrieve anything from package" + packageName);
        return null;
    }
    // 3. 依据不同的资源类型，采用不同的方式获取资源的集合
    Set<Class<?>> classSet = null;
    // 过滤处文件类型的资源
    if (url.getProtocol().equalsIgnoreCase(FILE_PROTOCOL)) {
        classSet = new HashSet<Class<?>>();
        File packageDirectory = new File(url.getPath());
        extractClassFile(classSet, packageDirectory, packageName);
    }
    // TODO 针对其他类型资源的处理
    return classSet;
}
```

递归获取指定 package 下的 所有Class 文件，也包括子 package 下的文件

```java
private static void extractClassFile(Set<Class<?>> emptyClassSet, File fileSource, String packageName) {
    if (!fileSource.isDirectory()) {
        return ;
    }
    // 如果是文件夹，则掉调用 listFiles 方法获取当前文件夹下的文件/文件夹
    File[] files = fileSource.listFiles(new FileFilter() {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            } else {
                // 获取文件的绝对值路径
                String absoluteFilePath = file.getAbsolutePath();
                if (absoluteFilePath.endsWith(".class")) {
                    // 如果是 class 文件，直接加载
                    addToClassSet(absoluteFilePath);
                }
            }
            return false;
        }
        // 根据 class 文件的绝对路径，获取并生成 class 对象，并法如 classSet 中
        private void addToClassSet(String absoluteFilePath) {
            // 1. 从 class 文件的绝对值路径里提取出包含了 package 的类名
            absoluteFilePath = absoluteFilePath.replace(File.separator, ".");
            String className = absoluteFilePath.substring(absoluteFilePath.indexOf(packageName));
            className = className.substring(0, className.lastIndexOf("."));
            // 2. 通过反射获取对象加到 classSet 里
            Class<?> targetClass = loadClass(className);
            emptyClassSet.add(targetClass);
        }
    });

    if (files != null) {
        for (File f : files) {
            // 递归调用
            extractClassFile(emptyClassSet, f, packageName);
        }
    }
}
```

## 容器的创建与加载

### 创建容器

使用枚举方式创建单例容器

```java
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanContainer {

    // 存放所有被配置标记的目标对象的 Map
    private final Map<Class<?>, Object> beanMap = new ConcurrentHashMap();
    // 加载 bean 的注解列表
    private static final List<Class<? extends Annotation>> BEAN_ANNOTATION
            = Arrays.asList(Component.class, Controller.class, Repository.class, Service.class, Aspect.class);
    // 容器是否已经加载过 bean
    private boolean loaded = false;

    public static BeanContainer getInstance() {
        return ContainerHolder.HOLDER.instance;
    }

    private enum ContainerHolder {
        HOLDER;
        private BeanContainer instance;
        ContainerHolder() {
            instance = new BeanContainer();
        }
    }
}
```

### 加载

扫描类，将特定注解加入容器中

```java
public synchronized void loadBeans(String packageName) {
    // 判断 bean 容器是否被加载过
    if (isLoaded()) {
        return;
    }
    Set<Class<?>> classSet = ClassUtil.extractPackageClass(packageName);
    if (ValidationUtil.isEmpty(classSet)) {
        log.warn("extract nothing from packageName" + packageName);
        return;
    }

    for (Class<?> clazz :classSet) {
        for (Class<? extends Annotation> annotation : BEAN_ANNOTATION) {
            // 如果类上面标记了定义的注解
            if (clazz.isAnnotationPresent(annotation)) {
                beanMap.put(clazz,ClassUtil. newInstance(clazz, true));
            }
        }
    }
    loaded = true;
}
```

## 依赖注入

1. 遍历 Bean 容器中所有的 Class 对象
2. 遍历 Class 对象的所有成员变量
3. 找出被 Autowired 标记的成员变量
4. 获取这些成员变量的类型
5. 获取这些成员变量的类型在容器里对应的实例
6. 通过反射将对应的成员变量实例注入到成员变量所在类的实例里

```java
public void doIoc() {
    // 1. 遍历 Bean 容器中所有的 Class 对象
    if (ValidationUtil.isEmpty(beanContainer.getClasses())) {
        log.warn("empty classset in BeanContainer");
        return;
    }
    for (Class<?> clazz : beanContainer.getClasses()) {
        // 2. 遍历 Class 对象的所有成员变量
        Field[] fields = clazz.getDeclaredFields();
        if (ValidationUtil.isEmpty(fields)) {
            continue;
        }
        for (Field field : fields) {
            // 3. 找出被 Autowired 标记的成员变量
            if (field.isAnnotationPresent(Autowired.class)) {
                Autowired autowired = field.getAnnotation(Autowired.class);
                String autowiredValue = autowired.value();
                // 4. 获取这些成员变量的类型
                Class<?> fieldClass = field.getType();
                // 5. 获取这些成员变量的类型在容器里对应的实例
                Object filedValue = getFieldInstance(fieldClass, autowiredValue);
                if (filedValue == null) {
                    throw new RuntimeException("unable to inject relevant type, target fieldClass is:"
                                               + fieldClass.getName() + "autowiredValue is " + autowiredValue);
                } else {
                    // 6. 通过反射将对应的成员变量实例注入到成员变量所在类的实例里
                    Object targetBean = beanContainer.getBean(clazz);
                    ClassUtil.setField(field, targetBean, filedValue, true);
                }

            }
        }
    }
}
```

