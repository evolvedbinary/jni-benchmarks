# JNI Benchmarks

[![Build Status](https://travis-ci.com/evolvedbinary/jni-benchmarks.svg?branch=main)](https://travis-ci.com/evolvedbinary/jni-benchmarks)
[![License](https://img.shields.io/badge/license-BSD%203-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)

We provide the code for a small set of benchmarks to compare various approaches to solving common JNI use-cases and then present the results.

The benchmarks at present are:
* [com.evolvedbinary.jnibench.common.call](tree/main/src/main/java/com/evolvedbinary/jnibench/common/call) - Benchmarks for [Creating Objects with JNI](#jni-object-creation-benchmarks) [(results)](#object-creation-results).
* [com.evolvedbinary.jnibench.common.array](tree/main/src/main/java/com/evolvedbinary/jnibench/common/array) - Benchmarks for [Passing Arrays with JNI](#jni-array-passing-benchmarks) [(results)](#array-passing-results).

## JNI Object Creation Benchmarks

The code contrasts three different approaches to constructing a Java Object that wraps a C++ object (which it has to construct). Such a scenario is common when writing a Java API wrapper for an existing C++ project.

### Scenario 1 - By Call
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


### Scenario 2 - By Call, Static
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


### Scenario 3 - By Call, Invoke
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

### 3 Scenario 4, 5, and 6
Scenarios 4, 5, and 6 are similar to 1, 2, and 3 respectively, except that the Java classes have been marked as `final`.


### Object Creation Results
Test machine: MacBook Pro 15-inch 2019: 2.4 GHz 8-Core Intel Core i9 / 32 GB 2400 MHz DDR4. OS X 10.15.2 / Oracle JDK 8.
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

The `com.evolvedbinary.jnibench.consbench.Benchmark` class already calls each scenario 1,000,000 times, so for the benchmark we repeated this 100 times and plotted the results.

![Image of JNI Object Creation Benchmark Results](https://raw.githubusercontent.com/evolvedbinary/jni-benchmarks/main/benchmark-results.png)

### Object Creation Conclusions
The difference between the non-final (Scenarios 1 - 3) and the final (Scenarios 4 - 6) class versions is so small that
it could easily be accounted for by system noise.

Scenario 2 and 5 - By Call, Static, appear to have the lowest JNI overhead for constructing C++ objects from Java.


## JNI Array Passing Benchmarks

The code contrasts several different approaches to passing an array of complex objects from C++ into Java. This can be done as either 2 independent arrays (one for each complex object property), or as an array of tuple objects holding the values. Allocation can be done in either C++ or Java. Such a scenario is common when writing a Java API wrapper for an existing C++ project.
NOTE: The C++ JNI code includes appropriate error checking, as the code has to be correct as well as performant! 

The complex object in Java looks like:
```java
public class FooObject {
  final String name;
  final long value;
  
  public FooObject(final String name, final long value) {
    this.name = name;
    this.value = value;
  }
}
```

The complex object in C++ looks like:
```C++
class FooObject {
  public:
    FooObject(const std::string& n, int64_t v) : name(n), value(v){}

    const std::string& GetName() const { return name; }
    int64_t GetValue() const { return value; }

  private:
    const std::string name;
    const int64_t value;
};
```

The goal is to benchmark different approaches for returning Arrays/Lists of the C++ FooObject to Java.  

### Scenario 1 - Allocate Complex Object array in Java, Fill in C++
We allocate a Java array in Java, and then in C++ we create Java complex objects and add them to the array. We then return to Java and wrap the array in an ArrayList.
```java
public class AllocateInJavaGetArray implements JniListSupplier<FooObject> {
  public List<FooObject> getObjectList(final NativeObjectArray<FooObject> nativeObjectArray) {
    final int len = (int) getArraySize(nativeObjectArray.get_nativeHandle());
    final FooObject objectList[] = new FooObject[len];
    getArray(nativeObjectArray.get_nativeHandle(), objectList);
    return Arrays.asList(objectList);
  }

  private static native long getArraySize(final long handle);
  private static native void getArray(final long handle, final FooObject[] objectList);
}
```

```C++
jlong Java_com_evolvedbinary_jnibench_common_array_AllocateInJavaGetArray_getArraySize(
    JNIEnv *, jclass, jlong handle) {
  const auto& cpp_array = *reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  return static_cast<jlong>(cpp_array.size());
}

void Java_com_evolvedbinary_jnibench_common_array_AllocateInJavaGetArray_getArray(
    JNIEnv *env, jclass, jlong handle, jobjectArray jobject_array) {
  const jclass jfoo_obj_clazz = FooObjectJni::getJClass(env);
  if (jfoo_obj_clazz == nullptr) {
    // exception occurred accessing class
    return;
  }

  auto* cpp_array = reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  for (jsize i = 0; i < env->GetArrayLength(jobject_array); i++) {
    jnibench::FooObject foo_obj = (*cpp_array)[static_cast<size_t>(i)];

    jobject jfoo_obj = FooObjectJni::construct(env, jfoo_obj_clazz, foo_obj);
    if (jfoo_obj == nullptr) {
        // exception occurred
        return;
    }

    env->SetObjectArrayElement(jobject_array, i, jfoo_obj);
    if(env->ExceptionCheck()) {
      // exception thrown: ArrayIndexOutOfBoundsException
      // or ArrayStoreException
      env->DeleteLocalRef(jfoo_obj);
      return;
    }

    env->DeleteLocalRef(jfoo_obj);
  }
}
```

### Scenario 2 - Allocate Complex Object array and fill with mutable objects in Java, mutate the objects in C++
We allocate a Java array in Java, and fill it with mutable complex java objects. In C++ we then update the mutable objects, then returning to Java, where we wrap the array in an ArrayList.

```java
public class AllocateInJavaGetMutableArray implements JniListSupplier<FooObject> {
  public List<FooObject> getObjectList(final NativeObjectArray<FooObject> nativeObjectArray) {
    final int len = (int) getArraySize(nativeObjectArray.get_nativeHandle());
    final FooObject objectList[] = new FooObject[len];
    for (int i = 0; i < len; i++) {
      objectList[i] = new FooObject();
    }

    getArray(nativeObjectArray.get_nativeHandle(), objectList);

    return Arrays.asList(objectList);
  }

  private static native long getArraySize(final long handle);
  private static native void getArray(final long handle, final FooObject[] objectList);
}
```

```C++
jlong Java_com_evolvedbinary_jnibench_common_array_AllocateInJavaGetMutableArray_getArraySize(
    JNIEnv *, jclass, jlong handle) {
  const auto& cpp_array = *reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  return static_cast<jlong>(cpp_array.size());
}

void Java_com_evolvedbinary_jnibench_common_array_AllocateInJavaGetMutableArray_getArray(
    JNIEnv *env, jclass, jlong handle, jobjectArray jobject_array) {
  const jclass jfoo_obj_clazz = FooObjectJni::getJClass(env);
  if (jfoo_obj_clazz == nullptr) {
    // exception occurred accessing class
    return;
  }

  const jfieldID fid_name = FooObjectJni::getNameField(env, jfoo_obj_clazz);
  if (fid_name == nullptr) {
    // exception occurred accessing field
    return;
  }

  const jfieldID fid_value = FooObjectJni::getValueField(env, jfoo_obj_clazz);
  if (fid_value == nullptr) {
    // exception occurred accessing field
    return;
  }

  auto* cpp_array = reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  for (jsize i = 0; i < env->GetArrayLength(jobject_array); i++) {
    jnibench::FooObject foo_obj = (*cpp_array)[static_cast<size_t>(i)];

    jobject jfoo_obj = env->GetObjectArrayElement(jobject_array, i);
    if(env->ExceptionCheck()) {
      // exception thrown: ArrayIndexOutOfBoundsException
      // or ArrayStoreException
      if (jfoo_obj != nullptr) {
        env->DeleteLocalRef(jfoo_obj);
      }
      return;
    }

    // set name field
    jstring jname = env->NewStringUTF(foo_obj.GetName().c_str());
    if (env->ExceptionCheck()) {
      if (jname != nullptr) {
        env->DeleteLocalRef(jname);
      }
      env->DeleteLocalRef(jfoo_obj);
      return;
    }
    env->SetObjectField(jfoo_obj, fid_name, jname);
    if (env->ExceptionCheck()) {
          env->DeleteLocalRef(jname);
          env->DeleteLocalRef(jfoo_obj);
          return;
    }
    env->DeleteLocalRef(jname);

    // set value field
    env->SetLongField(jfoo_obj, fid_value, static_cast<jlong>(foo_obj.GetValue()));
    if (env->ExceptionCheck()) {
      env->DeleteLocalRef(jfoo_obj);
      return;
    }

    env->DeleteLocalRef(jfoo_obj);
  }
}
```

### Scenario 3 - Allocate 2 arrays in Java, Fill in C++, copy to Complex Object Array in Java
In Java we allocate 2 arrays, one for each property of the complex object of which we ultimately want to return an array of.
We then pass those 2 arrays to C++ via JNI. In C++ we populate those two arrays, and return them to Java. Back in Java we create
an array of complex objects based on the values of those two arrays.

```java
public class AllocateInJavaGet2DArray implements JniListSupplier<FooObject> {
  public List<FooObject> getObjectList(final NativeObjectArray<FooObject> nativeObjectArray) {
    final int len = (int) getArraySize(nativeObjectArray.get_nativeHandle());
    final String names[] = new String[len];
    final long values[] = new long[len];

    getArrays(nativeObjectArray.get_nativeHandle(), names, values);

    final List<FooObject> objectList = new ArrayList<>();
    for (int i = 0; i < len; i++) {
      objectList.add(new FooObject(names[i], values[i]));
    }
    return objectList;
  }

  private static native long getArraySize(final long handle);
  private static native void getArrays(final long handle,
      final String[] paths, final long[] targetSizes);
}
````

```C++
jlong Java_com_evolvedbinary_jnibench_common_array_AllocateInJavaGet2DArray_getArraySize
  (JNIEnv *, jclass, jlong handle) {
  const auto& cpp_array = *reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  return static_cast<jlong>(cpp_array.size());
}

void Java_com_evolvedbinary_jnibench_common_array_AllocateInJavaGet2DArray_getArrays(
    JNIEnv *env, jclass, jlong handle, jobjectArray name_array, jlongArray value_array) {
  jlong* value_array_ptr = env->GetLongArrayElements(value_array, nullptr);
  if (value_array_ptr == nullptr) {
    // exception thrown: OutOfMemoryError
    return;
  }

  auto* cpp_array = reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  for (jsize i = 0; i < env->GetArrayLength(name_array); i++) {
    jnibench::FooObject foo_obj = (*cpp_array)[i];

    jstring jname = env->NewStringUTF(foo_obj.GetName().c_str());
    if (jname == nullptr) {
      // exception thrown: OutOfMemoryError
      env->ReleaseLongArrayElements(value_array, value_array_ptr, JNI_ABORT);
      return;
    }
    env->SetObjectArrayElement(name_array, i, jname);
    if (env->ExceptionCheck()) {
      // exception thrown: ArrayIndexOutOfBoundsException
      env->DeleteLocalRef(jname);
      env->ReleaseLongArrayElements(value_array, value_array_ptr, JNI_ABORT);
      return;
    }

    value_array_ptr[i] = static_cast<jlong>(foo_obj.GetValue());
  }

  env->ReleaseLongArrayElements(value_array, value_array_ptr, JNI_COMMIT);
}
```
### Scenario 4 - Allocate Complex Object Array in C++, Fill in C++
In C++ we allocate a Java array, and then we create Java complex objects and add them to the array. We then return to Java and wrap the array in an ArrayList.

```java
public class AllocateInCppGetArray implements JniListSupplier<FooObject> {
  public List<FooObject> getObjectList(final NativeObjectArray<FooObject> nativeObjectArray) {
    return Arrays.asList(getArray(nativeObjectArray.get_nativeHandle()));
  }

  private static native FooObject[] getArray(final long handle);
}
```

```C++
jobjectArray Java_com_evolvedbinary_jnibench_common_array_AllocateInCppGetArray_getArray(
    JNIEnv *env, jclass, jlong handle) {
  const auto& cpp_array = *reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  jsize length = static_cast<jsize>(cpp_array.size());

  jclass jfoo_obj_clazz = FooObjectJni::getJClass(env);
  if (jfoo_obj_clazz == nullptr) {
    // exception occurred accessing class
    return nullptr;
  }

  jobjectArray java_array = env->NewObjectArray(length, jfoo_obj_clazz, nullptr);
  if (java_array == nullptr) {
      // exception thrown: OutOfMemoryError
      return nullptr;
  }

  for (size_t i = 0; i < cpp_array.size(); ++i) {
    const jnibench::FooObject& foo_obj = cpp_array[i];
    jobject jfoo_obj = FooObjectJni::construct(env, jfoo_obj_clazz, foo_obj);
    if (jfoo_obj == nullptr) {
        // exception occurred
        env->DeleteLocalRef(java_array);
        return nullptr;
    }
    env->SetObjectArrayElement(java_array, static_cast<jsize>(i), jfoo_obj);
    if (env->ExceptionCheck()) {
      // exception thrown: ArrayIndexOutOfBoundsException
      // or ArrayStoreException
      env->DeleteLocalRef(jfoo_obj);
      env->DeleteLocalRef(java_array);
      return nullptr;
    }

    env->DeleteLocalRef(jfoo_obj);
  }
  return java_array;
}
```

### Scenario 5 - Allocate 2 arrays in C++, Fill in C++, copy to Complex Object Array in Java
In C++ we allocate 2 Java arrays, one for each property of the complex object of which we ultimately want to return an array of.
We then populate those 2 arrays, and return them to Java. Back in Java we create
an array of complex objects based on the values of those two arrays.

```java
public class AllocateInCppGet2DArray implements JniListSupplier<FooObject> {
  public List<FooObject> getObjectList(final NativeObjectArray<FooObject> nativeObjectArray) {
    final Object[][] objArr = get2DArray(nativeObjectArray.get_nativeHandle());
    final String[] names = (String[]) objArr[0];
    final Long[] values = (Long[]) objArr[1];
    final List<FooObject> objList = new ArrayList<>();
    for (int i = 0; i < names.length; ++i) {
      objList.add(new FooObject(names[i], values[i]));
    }
    return objList;
  }

  protected static native Object[][] get2DArray(final long handle);
}
```

```C++
jobjectArray Java_com_evolvedbinary_jnibench_common_array_AllocateInCppGet2DArray_get2DArray
  (JNIEnv *env, jclass, jlong handle) {
  const auto& cpp_array = *reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  jsize len = static_cast<jsize>(cpp_array.size());

  jclass jstring_clazz = StringJni::getJClass(env);
  if (jstring_clazz == nullptr) {
    // exception occurred accessing class
    return nullptr;
  }

  jclass jlong_clazz = LongJni::getJClass(env);
  if (jlong_clazz == nullptr) {
      // exception occurred accessing class
      return nullptr;
  }

  jobjectArray jname_array = env->NewObjectArray(len, jstring_clazz, nullptr);
  if (jname_array == nullptr) {
    // exception thrown: OutOfMemoryError
    return nullptr;
  }
  jobjectArray jvalue_array = env->NewObjectArray(len, jlong_clazz, nullptr);
  if (jvalue_array == nullptr) {
    // exception thrown: OutOfMemoryError
    env->DeleteLocalRef(jname_array);
    return nullptr;
  }

  for (size_t i = 0; i < cpp_array.size(); ++i) {
    const jnibench::FooObject& foo_obj = cpp_array[i];
    jstring jname = env->NewStringUTF(foo_obj.GetName().c_str());
    if (env->ExceptionCheck()) {
      if (jname != nullptr) {
        env->DeleteLocalRef(jname_array);
        env->DeleteLocalRef(jvalue_array);
        env->DeleteLocalRef(jname);
      }
      return nullptr;
    }

    jobject jvalue = LongJni::construct(env, jlong_clazz, foo_obj.GetValue());
    if (jvalue == nullptr) {
      env->DeleteLocalRef(jname_array);
      env->DeleteLocalRef(jvalue_array);
      env->DeleteLocalRef(jname);
      return nullptr;
    }

    env->SetObjectArrayElement(jname_array, static_cast<jsize>(i), jname);
    if (env->ExceptionCheck()) {
      // exception thrown: ArrayIndexOutOfBoundsException
      // or ArrayStoreException
      env->DeleteLocalRef(jname_array);
      env->DeleteLocalRef(jvalue_array);
      env->DeleteLocalRef(jname);
      env->DeleteLocalRef(jvalue);
      return nullptr;
    }
    env->SetObjectArrayElement(jvalue_array, static_cast<jsize>(i), jvalue);
    if (env->ExceptionCheck()) {
      // exception thrown: ArrayIndexOutOfBoundsException
      // or ArrayStoreException
      env->DeleteLocalRef(jname_array);
      env->DeleteLocalRef(jvalue_array);
      env->DeleteLocalRef(jname);
      env->DeleteLocalRef(jvalue);
      return nullptr;
    }

    env->DeleteLocalRef(jname);
    env->DeleteLocalRef(jvalue);
  }

  jobjectArray jobj_array = env->NewObjectArray(2, env->FindClass("java/lang/Object"), nullptr);
  if (jobj_array == nullptr) {
    // exception thrown: OutOfMemoryError
    env->DeleteLocalRef(jname_array);
    env->DeleteLocalRef(jvalue_array);
    return nullptr;
  }

  env->SetObjectArrayElement(jobj_array, 0, jname_array);
  if (env->ExceptionCheck()) {
    // exception thrown: ArrayIndexOutOfBoundsException
    // or ArrayStoreException
    env->DeleteLocalRef(jname_array);
    env->DeleteLocalRef(jvalue_array);
    env->DeleteLocalRef(jobj_array);
    return nullptr;
  }
  env->SetObjectArrayElement(jobj_array, 1, jvalue_array);
  if (env->ExceptionCheck()) {
    // exception thrown: ArrayIndexOutOfBoundsException
    // or ArrayStoreException
    env->DeleteLocalRef(jname_array);
    env->DeleteLocalRef(jvalue_array);
    env->DeleteLocalRef(jobj_array);
    return nullptr;
  }

  return jobj_array;
}
```

### Scenario 6 - Allocate 2 arrays in C++, Fill in C++, copy to custom List (backed by 2 arrays) in Java
This is an extended version of Scenario 5, where the resultant 2 arrays are wrapped in a custom list. This scenario
is concerned with reducing the number of data copies that are needed in Scenario 3. The C++ code is the same as that in Scenario 3, for the Java code see:
[AllocateInJavaGetArrayList.java](https://github.com/evolvedbinary/jni-benchmarks/blob/main/src/main/java/com/evolvedbinary/jnibench/common/array/AllocateInCppGet2DArrayListWrapper.java).


### Scenario 7 - Allocate ArrayList in Java, and fill with Complex Object in C++
This is similar to Scenario 1, but operates directly with a `java.util.ArrayList` instead of an array.

```java
public class AllocateInJavaGetArrayList implements JniListSupplier<FooObject> {
  @Override
  public List<FooObject> getObjectList(final NativeObjectArray<FooObject> nativeObjectArray) {
      final List<FooObject> objectList = new ArrayList<>(len);
      getList(nativeObjectArray.get_nativeHandle(), objectList);
      return objectList;
  }

  private static native long getListSize(final long handle);
  private static native void getList(final long handle, final List<FooObject> list);
}
```

```C++
jlong Java_com_evolvedbinary_jnibench_common_array_AllocateInJavaGetArrayList_getListSize(
    JNIEnv *, jclass, jlong handle) {
  const auto& cpp_array = *reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  return static_cast<jlong>(cpp_array.size());
}

void Java_com_evolvedbinary_jnibench_common_array_AllocateInJavaGetArrayList_getList(
    JNIEnv *env, jclass, jlong handle, jobject jlist) {

  const jclass jfoo_obj_clazz = FooObjectJni::getJClass(env);
  if (jfoo_obj_clazz == nullptr) {
    // exception occurred accessing class
    return;
  }

  const jmethodID add_mid = ListJni::getListAddMethodId(env);
  if (add_mid == nullptr) {
    // exception occurred accessing method
    return;
  }

  const auto& cpp_array = *reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  for (auto foo_obj : cpp_array) {
    // create java FooObject
    const jobject jfoo_obj = FooObjectJni::construct(env, jfoo_obj_clazz, foo_obj);
    if (jfoo_obj == nullptr) {
      // exception occurred constructing object
      return;
    }

    // add to list
    const jboolean rs = env->CallBooleanMethod(jlist, add_mid, jfoo_obj);
    if (env->ExceptionCheck() || rs == JNI_FALSE) {
      // exception occurred calling method, or could not add
      env->DeleteLocalRef(jfoo_obj);
      return;
    }
  }
}
``` 

### Scenario 8 - Allocate ArrayList in C++, and fill with Complex Object in C++
This is similar to Scenario 4, but operates directly with a `java.util.ArrayList` instead of an array.

```java
public class AllocateInCppGetArrayList implements JniListSupplier<FooObject> {
    public List<FooObject> getObjectList(final NativeObjectArray<FooObject> nativeObjectArray) {
        return getArrayList(nativeObjectArray.get_nativeHandle());
    }

    private static native List<FooObject> getArrayList(final long handle);
}
```

```C++
jobject Java_com_evolvedbinary_jnibench_common_array_AllocateInCppGetArrayList_getArrayList(
    JNIEnv *env, jclass, jlong handle) {

  const jclass jfoo_obj_clazz = FooObjectJni::getJClass(env);
  if (jfoo_obj_clazz == nullptr) {
    // exception occurred accessing class
    return nullptr;
  }

  const jclass clazz_array_list = ListJni::getArrayListClass(env);
  const jmethodID ctor_array_list = ListJni::getArrayListConstructorMethodId(env);
  if (ctor_array_list == nullptr) {
    // exception occurred accessing method
    return nullptr;
  }

  const jmethodID add_mid = ListJni::getListAddMethodId(env);
  if (add_mid == nullptr) {
    // exception occurred accessing method
    return nullptr;
  }

  const auto& cpp_array = *reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  const jsize len = static_cast<jsize>(cpp_array.size());

  // create new java.util.ArrayList
  const jobject jlist = env->NewObject(clazz_array_list, ctor_array_list,
              static_cast<jint>(len));
  if (env->ExceptionCheck()) {
    // exception occurred constructing object
    if (jlist != nullptr) {
      env->DeleteLocalRef(jlist);
    }
    return nullptr;
  }

  for (auto foo_obj : cpp_array) {
    // create java FooObject
    const jobject jfoo_obj = FooObjectJni::construct(env, jfoo_obj_clazz, foo_obj);
    if (jfoo_obj == nullptr) {
      // exception occurred constructing object
      return nullptr;
    }

    // add to list
    const jboolean rs = env->CallBooleanMethod(jlist, add_mid, jfoo_obj);
    if (env->ExceptionCheck() || rs == JNI_FALSE) {
      // exception occurred calling method, or could not add
      env->DeleteLocalRef(jlist);
      env->DeleteLocalRef(jfoo_obj);
      return nullptr;
    }
  }

  return jlist;
}
```

### Array Passing Results
Test machine: MacBook Pro 15-inch 2019: 2.4 GHz 8-Core Intel Core i9 / 32 GB 2400 MHz DDR4. OS X 10.15.2 / Liberica OpenJDK 8.
```bash
$ java -version
openjdk version "1.8.0_252"
OpenJDK Runtime Environment (build 1.8.0_252-b09)
OpenJDK 64-Bit Server VM (build 25.252-b09, mixed mode)
```

```
$ clang --version
Apple clang version 11.0.3 (clang-1103.0.32.62)
Target: x86_64-apple-darwin19.6.0
Thread model: posix
```

The `com.evolvedbinary.jnibench.consbench.Benchmark` class already calls each scenario 1,000,000 times, so for the benchmark we repeated this 100 times and plotted the results.

![Image of JNI Array Passing Benchmark Results when size is 2](https://raw.githubusercontent.com/evolvedbinary/jni-benchmarks/main/jni-arrays-size-2.png)
![Image of JNI Array Passing Benchmark Results when size is 20](https://raw.githubusercontent.com/evolvedbinary/jni-benchmarks/main/jni-arrays-size-20.png)

### Array Passing Conclusions
The fastest approach appears to be by performing most of the allocations in Java, and then passing arrays of simple types between C++ and Java.
For the array/list of complex objects to be returned from C++ to Java,
allocating one array in Java for each of the complex objects property's,
and then populating those arrays in C++ seems to be the most performant approach (see `AllocatedInJavaGet2DArray.java`).

# Reproducing
If you want to run the code yourself, you need to have Java 8, Maven 3, and a C++ compiler that supports the C++ 11 standard. You can then simply run:

```bash
$ mvn clean compile package
```

In the `target/` sub-directory, you will then find both a `jni-benchmarks-1.0.0-SNAPSHOT-application` folder
and a `jni-benchmarks-1.0.0-SNAPSHOT-application.zip` file, you can use either of these.
They both contain bash scripts in their `bin/` sub-folders for Mac, Linux, Unix and batch scripts for Windows.
These scripts will run a single iteration of the benchmark.

If you want to run multiple iterations and get a CSV file of the results, you can use `benchmark-100.sh`
and/or `benchmark-100-with-close.sh`, or `array-benchmark-100.sh`. 

## JMH support
We have support for running the tests via JMH, see `jmh-benchmarks.sh`. You can also pass `--help`
to the script to see JMH options.

### Byte array benchmarks
There are two benchmarks, which are currently available only via JMH: ByteArrayFromNativeBenchmark and ByteArrayToNativeBenchmark.
They can be run multiple times using `jmh-benchmarks-parametrized.sh` with:

```bash
./jmh-benchmarks-parametrized.sh -i 10 -b ByteArrayToNativeBenchmark -o results/ -f csv
```

Above command will run JMH with ByteArrayToNativeBenchmark benchmarks 10 times and store result in CSV files in 'results' directory.
You can also pass `--help` to the script to see additional JMH options that can be passed.
<p>
Results can then be plotted using `process_byte_array_benchmarks_results.py` script.
For results of ByteArrayToNativeBenchmark benchmarks:
```bash
python3 process_byte_array_benchmarks_results.py -p results/ --param-name "Param: keySize" --chart-title "Performance comparison of passing byte array with {} bytes via JNI"
```
Command line parameter `p` expects path to directory with JMH result CSV files from running benchmarks with `jmh-benchmarks-parametrized.sh`.
The `{}` in `chart-title` parameter will be replaced by value from `param-name` column.