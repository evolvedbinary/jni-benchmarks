package com.evolvedbinary.jnibench.consbench;

import com.evolvedbinary.jnibench.common.array.*;

import java.util.Arrays;
import java.util.List;

import static com.evolvedbinary.jnibench.consbench.BenchmarkHelper.outputResults;
import static com.evolvedbinary.jnibench.consbench.BenchmarkHelper.time;

public class ArrayBenchmark implements BenchmarkInterface {

  private static final List<FooObject> FOO_OBJECTS = Arrays.asList(
      new FooObject("Jon", 23),
      new FooObject("Mary", 24)
  );


  @Override
  public void test(BenchmarkOptions benchmarkOptions) {
    int iterations = benchmarkOptions.getIterations();

    FooNativeObjectArray fooObjectArray = new FooNativeObjectArray(FOO_OBJECTS);
    AllocateInJavaGetArray allocateInJavaGetArray = new AllocateInJavaGetArray();

    //TEST1 - allocate in Java
    final long start1 = time(benchmarkOptions.isInNs());
    for (int i = 0; i < iterations; i++) {
      allocateInJavaGetArray.getObjectList(fooObjectArray);
    }
    final long end1 = time(benchmarkOptions.isInNs());

    JniGetArray jniGetArray = new JniGetArray();
    //TEST2 - create object array in JNI
    final long start2 = time(benchmarkOptions.isInNs());
    for (int i = 0; i < iterations; i++) {
      jniGetArray.getObjectList(fooObjectArray);
    }
    final long end2 = time(benchmarkOptions.isInNs());

    Jni2DGetArray jni2DGetArray = new Jni2DGetArray();
    //TEST3 - create 2D object array in JNI
    final long start3 = time(benchmarkOptions.isInNs());
    for (int i = 0; i < iterations; i++) {
      jni2DGetArray.getObjectList(fooObjectArray);
    }
    final long end3 = time(benchmarkOptions.isInNs());

    String names[] = {
        "Allocate in Java",
        "Create object array in JNI",
        "Create 2D object array in JNI"
    };
    long results[] = {
        end1 - start1,
        end2 - start2,
        end3 - start3
    };
    outputResults(benchmarkOptions.isOutputAsCSV(), benchmarkOptions.isInNs(), names, results);
  }
}
