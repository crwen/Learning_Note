# 面试高频问题



## ArrayList

* ArrayList 怎么扩容？具体怎么实现

> 答：ArrayList 扩容是以 1.5 倍扩容的。如果扩容后的大小比需要的大小还要小，就将预期值作为新的容量。如果新容量 大于 （int 的最大值 - 8），就会将 int 的最大值作为其容量

* ArrayList 是否线程安全

> 答：ArrayList 线程不安全。如果要保证线程安全，可以使用 `Collections.synchronizedList(list)` 或者 `CopyOnWriteArrayList`



* List 遍历时如何删除元素？fail-fast 是什么？fail-save 是什么？

```java
for (int i = 0; i < list.size(); i++) {
 if (list.get(i) == 2)
     list.remove(i)
}
```

