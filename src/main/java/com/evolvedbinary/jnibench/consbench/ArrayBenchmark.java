package com.evolvedbinary.jnibench.consbench;

import com.evolvedbinary.jnibench.common.array.*;

import java.util.Random;

import static com.evolvedbinary.jnibench.consbench.BenchmarkHelper.outputResults;
import static com.evolvedbinary.jnibench.consbench.BenchmarkHelper.time;

public class ArrayBenchmark implements BenchmarkInterface {

  // TODO(AR) parameterize the array size
  private static final int DEFAULT_ARRAY_SIZE = 20;
  private final FooObject[] fooObjects;

  public ArrayBenchmark() {
    this.fooObjects = new FooObject[DEFAULT_ARRAY_SIZE];
    final Random random = new Random();
    for (int i = 0; i < DEFAULT_ARRAY_SIZE; i++) {
      final int num = random.nextInt();
      fooObjects[i] = new FooObject("str" + num, num);
    }
  }

  @Override
  public void test(final BenchmarkOptions benchmarkOptions) {
    int iterations = benchmarkOptions.getIterations();

    FooNativeObjectArray fooObjectArray = new FooNativeObjectArray(fooObjects);

    AllocateInJavaGetArray allocateInJavaGetArray = new AllocateInJavaGetArray();
    //TEST1 - allocate array in Java
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

    final Jni2DGetArrayListWrapper jni2DGetArrayListWrapper = new Jni2DGetArrayListWrapper();
    //TEST4 - create 2D object array in JNI and wrap result in java list
    final long start4 = time(benchmarkOptions.isInNs());
    for (int i = 0; i < iterations; i++) {
      jni2DGetArrayListWrapper.getObjectList(fooObjectArray);
    }
    final long end4 = time(benchmarkOptions.isInNs());

    final AllocateInJavaGetArrayList allocateInJavaGetArrayList = new AllocateInJavaGetArrayList();
    //TEST5 - allocate array list in Java
    final long start5 = time(benchmarkOptions.isInNs());
    for (int i = 0; i < iterations; i++) {
      allocateInJavaGetArrayList.getObjectList(fooObjectArray);
    }
    final long end5 = time(benchmarkOptions.isInNs());

    final JniGetArrayList jniGetArrayList = new JniGetArrayList();
    //TEST6 - create object array list in JNI
    final long start6 = time(benchmarkOptions.isInNs());
    for (int i = 0; i < iterations; i++) {
      jniGetArrayList.getObjectList(fooObjectArray);
    }
    final long end6 = time(benchmarkOptions.isInNs());

    String names[] = {
        "Allocate array in Java",
        "Create object array in JNI",
        "Create 2D object array in JNI",
        "Create 2D object array in JNI and wrap result in custom Java List",
        "Allocate list in Java",
        "Create object array list in JNI"
    };
    long results[] = {
        end1 - start1,
        end2 - start2,
        end3 - start3,
        end4 - start4,
        end5 - start5,
        end6 - start6,
    };
    outputResults(benchmarkOptions.isOutputAsCSV(), benchmarkOptions.isInNs(), names, results);
  }
}
