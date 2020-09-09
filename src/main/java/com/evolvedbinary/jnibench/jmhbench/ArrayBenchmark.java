package com.evolvedbinary.jnibench.jmhbench;

import com.evolvedbinary.jnibench.common.array.*;
import com.evolvedbinary.jnibench.consbench.NarSystem;
import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ArrayBenchmark {

  static {
    NarSystem.loadLibrary();
  }

  // TODO(AR) parameterize the array size
  private static final int DEFAULT_ARRAY_SIZE = 20;

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    FooNativeObjectArray fooObjectArray;
    AllocateInJavaGetArray allocateInJavaGetArray;
    JniGetArray jniGetArray;
    Jni2DGetArray jni2DGetArray;
    Jni2DGetArrayListWrapper jni2DGetArrayListWrapper;
    AllocateInJavaGetArrayList allocateInJavaGetArrayList;
    JniGetArrayList jniGetArrayList;

    public BenchmarkState() {

    }

    public BenchmarkState(final FooNativeObjectArray fooObjectArray,
                          final AllocateInJavaGetArray allocateInJavaGetArray,
                          final JniGetArray jniGetArray,
                          final Jni2DGetArray jni2DGetArray,
                          final Jni2DGetArrayListWrapper jni2DGetArrayListWrapper,
                          final AllocateInJavaGetArrayList allocateInJavaGetArrayList,
                          final JniGetArrayList jniGetArrayList) {
      this.fooObjectArray = fooObjectArray;
      this.allocateInJavaGetArray = allocateInJavaGetArray;
      this.jniGetArray = jniGetArray;
      this.jni2DGetArray = jni2DGetArray;
      this.jni2DGetArrayListWrapper = jni2DGetArrayListWrapper;
      this.allocateInJavaGetArrayList = allocateInJavaGetArrayList;
      this.jniGetArrayList = jniGetArrayList;
    }

    @Param({"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"})
    int iteration;

    @Setup
    public void setup() {
      final FooObject[] fooObjects = new FooObject[DEFAULT_ARRAY_SIZE];
      final Random random = new Random();
      for (int i = 0; i < DEFAULT_ARRAY_SIZE; i++) {
        final int num = random.nextInt();
        fooObjects[i] = new FooObject("str" + num, num);
      }
      fooObjectArray = new FooNativeObjectArray(
          fooObjects);
      allocateInJavaGetArray = new AllocateInJavaGetArray();
      jniGetArray = new JniGetArray();
      jni2DGetArray = new Jni2DGetArray();
      jni2DGetArrayListWrapper = new Jni2DGetArrayListWrapper();
      allocateInJavaGetArrayList = new AllocateInJavaGetArrayList();
      jniGetArrayList = new JniGetArrayList();
    }

    @TearDown
    public void tearDown() {
      fooObjectArray.close();
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public void allocateInJavaArray(BenchmarkState benchmarkState) {
    benchmarkState.allocateInJavaGetArray.getObjectList(benchmarkState.fooObjectArray);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public void jniGetArray(BenchmarkState benchmarkState) {
    benchmarkState.jniGetArray.getObjectList(benchmarkState.fooObjectArray);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public void jni2DGetArray(BenchmarkState benchmarkState) {
    benchmarkState.jni2DGetArray.getObjectList(benchmarkState.fooObjectArray);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public void jni2DGetArrayListWrapper(BenchmarkState benchmarkState) {
    benchmarkState.jni2DGetArrayListWrapper.getObjectList(benchmarkState.fooObjectArray);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public void allocateInJavaArrayList(BenchmarkState benchmarkState) {
    benchmarkState.allocateInJavaGetArrayList.getObjectList(benchmarkState.fooObjectArray);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public void jniGetArrayList(BenchmarkState benchmarkState) {
    benchmarkState.jniGetArrayList.getObjectList(benchmarkState.fooObjectArray);
  }
}
