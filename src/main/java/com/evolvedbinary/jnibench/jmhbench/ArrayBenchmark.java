package com.evolvedbinary.jnibench.jmhbench;

import com.evolvedbinary.jnibench.common.array.AllocateInJavaGetArray;
import com.evolvedbinary.jnibench.common.array.FooObject;
import com.evolvedbinary.jnibench.common.array.FooNativeObjectArray;
import com.evolvedbinary.jnibench.common.array.JniGetArray;
import com.evolvedbinary.jnibench.consbench.NarSystem;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ArrayBenchmark {

  static {
    NarSystem.loadLibrary();
  }

  private static final List<FooObject> FOO_OBJECTS = Arrays.asList(
      new FooObject("Jon", 23),
      new FooObject("Mary", 24)
  );

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    FooNativeObjectArray fooObjectArray;
    AllocateInJavaGetArray allocateInJavaGetArray;
    JniGetArray jniGetArray;

    public BenchmarkState() {

    }

    public BenchmarkState(FooNativeObjectArray fooObjectArray,
                          AllocateInJavaGetArray allocateInJavaGetArray,
                          JniGetArray jniGetArray) {
      this.fooObjectArray = fooObjectArray;
      this.allocateInJavaGetArray = allocateInJavaGetArray;
      this.jniGetArray = jniGetArray;
    }

    @Setup
    public void setup() {
      fooObjectArray = new FooNativeObjectArray(
          FOO_OBJECTS);
      allocateInJavaGetArray = new AllocateInJavaGetArray();
      jniGetArray = new JniGetArray();
    }

    @TearDown
    public void tearDown() {
      fooObjectArray.close();
    }
  }

  @Benchmark
  public void allocateInJavaArray(BenchmarkState benchmarkState) {
    benchmarkState.allocateInJavaGetArray.getObjectList(benchmarkState.fooObjectArray);
  }

  @Benchmark
  public void jniGetArray(BenchmarkState benchmarkState) {
    benchmarkState.jniGetArray.getObjectList(benchmarkState.fooObjectArray);
  }
}
