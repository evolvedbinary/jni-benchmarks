# Data Transfer Benchmarks

Evolution or adaptation of the original benchmarks, to focus on efficiency of data transfer across JNI,
and to try to as accurately as possible simulate the real loads involved in calling a database implemented in `C++`
from Java, and transferring data in either direction across the JNI interface.

## The Model

- In `C++` we represent the on-disk data as an in-memory map of `(key, value)` pairs.
- For a fetch query, we expect the result to be a Java object with access to the contents of the _value_. This may be a standard Java object which does the job of data access (a `byte[]` or a `ByteBuffer`) or an object of our own devising which holds references to the value in some form (a `FastBuffer` pointing to `com.sun.unsafe.Unsafe` unsafe memory, for instance).

### Data Types

#### Byte Array

```java
byte[]
```

At the C++ side, the method [`JNIEnv.GetArrayCritical()`](https://docs.oracle.com/en/java/javase/13/docs/specs/jni/functions.html#getprimitivearraycritical) allows access to a C++ pointer to the underlying array.

There are methods in `JNIEnv` for fetching references/copies to and from the contents of a byte array `GetByteArrayElements()` and `ReleaseByteArrayElements()` with less concern for critical sections than the _critical_ methods, though these will often result in copies.

There are methods in `JNIEnv` for transferring raw C++ buffer data to and from the contents of a byte array `GetByteArrayRegion()` and `SetByteArrayRegion()`. These presumably do _critical-ish_ things underneath, and we would surmise have similar costs to that.

#### Byte Buffer

```java
ByteBuffer
```

There are 2 types of byte buffers in Java, _indirect_ and _direct_.
Indirect byte buffers are normal/default, whereas direct byte buffers are usd to wrap off-heap memory which is
accessible to direct network I/O. Either type of `ByteBuffer` can be allocated at the Java side, using the `allocate()` and
`allocateDirect()` methods respectively.

Direct byte buffers can be created in C++ using
the JNI method
[`JNIEnv.NewDirectByteBuffer()`](https://docs.oracle.com/en/java/javase/13/docs/specs/jni/functions.html#newdirectbytebuffer)
to wrap some native (C++) memory.

Direct byte buffers can be accessed in C++ using the
[`JNIEnv.GetDirectBufferAddress()`](https://docs.oracle.com/en/java/javase/13/docs/specs/jni/functions.html#GetDirectBufferAddress)
and measured using [`JNIEnv.GetDirectBufferCapacity()`](https://docs.oracle.com/en/java/javase/13/docs/specs/jni/functions.html#GetDirectBufferCapacity)

#### Unsafe Memory

```java
com.sun.unsafe.Unsafe.allocateMemory()
```

The returned handle is (of course) just a pointer to raw memory, and can be used as such on the C++ side.
We could turn it into a byte buffer on the C++ side by calling `JNIEnv.NewDirectByteBuffer()`.

### Allocation

A separate concern to the cost of transferring data across JNI (and associated copying) is the allocation of the data itself.
There are many models for how data is allocated, freed and re-allocated, and these have a major influence on overall performance.
In order to focus on measuring JNI performance, wee want to make data/memory allocation consistent.
Current tests mix allocation types:

- Allocate data on the Java side for every call (be it `byte[]`, direct or indirect `ByteBuffer`, or unsafe memory)
- Allocate data on the C++ side for every call (be it `byte[]`, direct or indirect `ByteBuffer`, or unsafe memory)
- Allocate a data container on the Java side, and re-use it for every call (this works better for `get()` then `put()`)
- Return direct memory from the C++ side, wrapped in a mechanism that allows it to be unpinned when it has been read/used

In testing multiple JNI transfer options, we need to consistently use the same allocation patterns so that they do not
impact the JNI measurements.

### Copy and Transfer Mechanisms

The options for how to transfer data between Java and C++ structures depend on the data allocation types used.
For some allocation types there are multiple options. But it's not combinatorial,
and in general we just need to systematically enumerate our options.

#### `GetPrimitiveArrayCritical` for `byte[]`

Used to get a C++ pointer from a Java array, which can then be transferred using `memcpy()`,
within a short critical section, before closing with `ReleasePrimitiveArrayCritical`

#### `Get/SetByteArrayRegion` for `byte[]`

Abstracted method for transferring data to/from a Java array, presumably it uses uses the critical section method underneath.
We can confirm this with performance tests.

#### `GetDirectBufferAddress` for direct `ByteBuffer`

Then we can `memcpy()` into or out of the buffer respectively.

#### `byte[] array()` for indirect `ByteBuffer`s ?

The `byte[] array()` method is optional, but if it returns non-null we can then do `byte[]` things.
That is what is used by the current test(s), followed by `SetByteArray`, as for `byte[]`

Are there any alternatives ?

#### TODO from here

#### Unsafe memory and the homebrew `FastBuffer` wrapper

#### Unsafe memory wrapped into a direct `ByteBuffer`

#### TODO What to do with the data we have read

Perform a checksum operation through whatever form of container the data is returned within.
That is to say, we need to implement checksum
over `byte[]`, over `ByteBuffer` and over `FastBuffer`

#### What to do to generate the data we will write (e.g. fill the buffer)
