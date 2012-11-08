MemoryObserver
==============

A neat JVM Memory Observer tool that allows for runtime calculation of the size of objects in memory. 

==============
This is a neat JVM Memory Observer that allows for runtime calculation of the size of objects in memory. 
It act as complement to more expensive/complex Java profilers. It calculates shallow and deep sizes of object.

The tool utilizes the instrumentation interfaces found in Sun API. It is configured in the startup of the JVM using the (-javaagent) argument. An example is illustrated in run.bat/.sh

## 1. How to build it?

Both Ant and Maven Script are provided to build this tool.
run "ant" if you want to build it in Ant
run "mvn install" if you want to build it in Ant

## 2. How to run it?

Run run.bat/.sh scripts to execute the example contained in the source code.

## 3. Example of using it?

example.ExampleApp.java

public class ExampleApp {

    public static void calcSize(Object o) {
        long memShallow = MemoryObserver.shallowSizeOf(o);
        long memDeep = MemoryObserver.deepSizeOf(o);
        System.out.printf("%s, shallow=%d, deep=%d%n", o.getClass().getSimpleName(), memShallow, memDeep);
    }

    public static void main(String[] args) throws Exception {
        calcSize(Boolean.TRUE);

        Map<Integer, String> map = new HashMap<Integer, String>();
        calcSize(map);
        map.put(1, "One");
        calcSize(map);
        map.put(2, "Two");
        calcSize(map);

        map = new ConcurrentHashMap<Integer, String>();
        calcSize(map);
        map.put(1, "AAAAA");
        calcSize(map);
        map.put(2, "BBBBB");
        calcSize(map);

        TestObject to = new TestObject(45454, null);
        calcSize(to);
        to.setChild(new TestObject(446, null));
        calcSize(to);
    }
}

## Example Result:
```html
Boolean, shallow=0, deep=0
HashMap, shallow=48, deep=128
HashMap, shallow=48, deep=152
HashMap, shallow=48, deep=176
ConcurrentHashMap, shallow=48, deep=208
ConcurrentHashMap, shallow=48, deep=312
ConcurrentHashMap, shallow=48, deep=416
TestObject, shallow=16, deep=16
TestObject, shallow=16, deep=32
```