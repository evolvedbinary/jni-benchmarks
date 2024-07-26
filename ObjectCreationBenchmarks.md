# JNI Object Creation Benchmarks

The code contrasts three different approaches to constructing a Java Object that
wraps a C++ object (which it has to construct). Such a scenario is common when
writing a Java API wrapper for an existing C++ project.

## Scenario 1 - By Call

From Java we call a JNI C++ member function to construct the C++ object and
return a `jlong` which represents the memory pointer to the C++ object.

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

## Scenario 2 - By Call, Static

Similar to _Scenario 1_, except that we use a static call to a JNI C++ function.

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

## Scenario 3 - By Call, Invoke

Similar to _Scenario 1_, however instead of returning a `jlong` pointer, we
instead in C++ find the `_nativeHandle` member of the calling Java object, and
then directly set the `long` field from C++.

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

## Scenario 4, 5, and 6

Scenarios 4, 5, and 6 are similar to 1, 2, and 3 respectively, except that the
Java classes have been marked as `final`.

## Object Creation Results

Test machine: MacBook Pro 15-inch 2019: 2.4 GHz 8-Core Intel Core i9 / 32 GB
2400 MHz DDR4. OS X 10.15.2 / Oracle JDK 8.

```bash
$ java -version
java version "1.8.0_221"
Java(TM) SE Runtime Environment (build 1.8.0_221-b11)
Java HotSpot(TM) 64-Bit Server VM (build 25.221-b11, mixed mode)
```

```
$ clang --version
Apple clang version 11.0.0 (clang-1100.0.33.16)
Target: x86_64-apple-darwin19.2.0
Thread model: posix
```

The `com.evolvedbinary.jnibench.consbench.Benchmark` class already calls each
scenario 1,000,000 times, so for the benchmark we repeated this 100 times and
plotted the results.

![Image of JNI Object Creation Benchmark Results](https://raw.githubusercontent.com/evolvedbinary/jni-benchmarks/main/benchmark-results.png)

## Object Creation Conclusions

The difference between the non-final (Scenarios 1 - 3) and the final (Scenarios
4 - 6) class versions is so small that it could easily be accounted for by
system noise.

Scenario 2 and 5 - By Call, Static, appear to have the lowest JNI overhead for
constructing C++ objects from Java.