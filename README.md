JNI Construction Benchmark
==========================

We provide the code for a small benchmark to compare costs of JNI object creation and then present the [results](#results).

The code contrast three different approaches to construction a Java Object that wraps a C++ object (which it has to construct). Such a scenario is common when writing Java API wrappers for existing C++ projects.

Scenario 1 - By Call
--------------------
From Java we call a JNI C++ member function to construct the C++ object and return a `jlong` which represents the memory pointer to the C++ object.

```Java
public class FooByCall extends NativeBackedObject {
    public FooByCall() {
        super();
        this._nativeHandle = newFoo();
    }

	private native long newFoo();

    ...

```

```C++
jlong Java_com_evolvedbinary_jni_consbench_FooByCall_newFoo(JNIEnv* env, jobject jobj) {
  consbench::Foo* foo = new consbench::Foo();
  return reinterpret_cast<jlong>(foo);
}
```


Scenario 2 - By Call, Static
----------------------------
Similar to *Scenario 1*, except that we use a static call to a JNI C++ function.

```java
public class FooByCallStatic extends NativeBackedObject {
    public FooByCallStatic() {
        super();
        this._nativeHandle = newFoo();
    }

    private static native long newFoo();

    ...
```

```C++
jlong Java_com_evolvedbinary_jni_consbench_FooByCallStatic_newFoo(JNIEnv* env, jclass jcls) {
  consbench::Foo* foo = new consbench::Foo();
  return reinterpret_cast<jlong>(foo);
}
```


Scenario 3 - By Call, Invoke
----------------------------
Similar to *Scenario 1*, however instead of returning a `jlong` pointer, we instead in C++ find the `_nativeHandle` member of the calling Java object, and then directly set the `long` field from C++.

```java
public class FooByCallInvoke extends NativeBackedObject {
    public FooByCallInvoke() {
        super();
        newFoo();   //the native method, will find _nativeHandle from the class and set it directly
    }

    private native void newFoo();

    ...
```

```C++
void Java_com_evolvedbinary_jni_consbench_FooByCallInvoke_newFoo(JNIEnv* env, jobject jobj) {
  consbench::Foo* foo = new consbench::Foo();

  //set the _nativeHandle in Java
  consbench::FooByCallInvokeJni::setHandle(env, jobj, foo);
}

template<class PTR, class DERIVED> class FooJniClass {
 public:
  // Get the java class id
  static jclass getJClass(JNIEnv* env, const char* jclazz_name) {
    jclass jclazz = env->FindClass(jclazz_name);
    assert(jclazz != nullptr);
    return jclazz;
  }

  // Get the field id of the member variable to store
  // the ptr
  static jfieldID getHandleFieldID(JNIEnv* env) {
    static jfieldID fid = env->GetFieldID(
        DERIVED::getJClass(env), "_nativeHandle", "J");
    assert(fid != nullptr);
    return fid;
  }

  // Get the pointer from Java
  static PTR getHandle(JNIEnv* env, jobject jobj) {
    return reinterpret_cast<PTR>(
        env->GetLongField(jobj, getHandleFieldID(env)));
  }

  // Pass the pointer to the java side.
  static void setHandle(JNIEnv* env, jobject jdb, PTR ptr) {
    env->SetLongField(
        jdb, getHandleFieldID(env),
        reinterpret_cast<jlong>(ptr));
  }
};


// The portal class for com.evolvedbinary.jni.consbench.FooByCallInvoke
class FooByCallInvokeJni : public FooJniClass<consbench::Foo*, FooByCallInvokeJni> {
 public:
  static jclass getJClass(JNIEnv* env) {
    return FooJniClass::getJClass(env,
        "com/evolvedbinary/jni/consbench/FooByCallInvoke");
  }
};

```


Results
-------
Test machine: MacBook Pro Retina Mid-2015: 2.8 GHz Intel Core i7 / 16 GB 1600 MHz DDR3.
```bash
$ java -version
java version "1.8.0_51"
Java(TM) SE Runtime Environment (build 1.8.0_51-b16)
Java HotSpot(TM) 64-Bit Server VM (build 25.51-b03, mixed mode)
```

The `com.evolvedbinary.jni.consbench.Benchmark` class already calls each scenario 1,000,000 times, so for the benchmark we repeated this 100 times and plotted the results.

![Image of Benchmark Results](https://raw.githubusercontent.com/adamretter/jni-construction-benchmark/master/benchmark-results.png)


Conclusion
----------
Scenario 2 - By Call, Static, appears to have the lowest JNI overhead for constructing C++ objects from Java.


Reproducing
-----------
If you want to run the code yourself, you need to have Java 8, Maven 3, and a C++ compiler that supports the C++ 11 standard. You can then simply run:

```bash
$ mvn clean compile package
```

In the `target/` sub-directory, you will then find both a `jni-construction-benchmark-1.0-SNAPSHOT-application` folder and a `jni-construction-benchmark-1.0-SNAPSHOT-application.zip` file, you can use either of these. They both contain bash scripts in their `bin/` sub-folders for Mac, Linux, Unix and batch scripts for Windows.