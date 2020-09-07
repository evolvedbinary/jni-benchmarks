package com.evolvedbinary.jnibench.jmhbench;

import com.evolvedbinary.jnibench.common.array.AllocateInJavaGetArray;
import com.evolvedbinary.jnibench.common.array.FooObject;
import com.evolvedbinary.jnibench.common.array.FooObjectArray;
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

  @Benchmark
  public void allocateInJavaArray() {
    FooObjectArray fooObjectArray = new FooObjectArray(
        FOO_OBJECTS,
        new AllocateInJavaGetArray());
    List<FooObject> fooObjects = fooObjectArray.getFooObjects();
  }
}
