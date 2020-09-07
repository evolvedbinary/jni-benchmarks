package com.evolvedbinary.jnibench.consbench;

import com.evolvedbinary.jnibench.common.array.AllocateInJavaGetArray;
import com.evolvedbinary.jnibench.common.array.FooObject;
import com.evolvedbinary.jnibench.common.array.FooObjectArray;

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

    FooObjectArray fooObjectArray = new FooObjectArray(
        FOO_OBJECTS,
        new AllocateInJavaGetArray());

    //TEST1 - allocate in Java
    final long start1 = time(benchmarkOptions.isInNs());
    for (int i = 0; i < iterations; i++) {
      fooObjectArray.getFooObjects();
    }
    final long end1 = time(benchmarkOptions.isInNs());

    String names[] = {
        "Allocate in Java"
    };
    Long results[] = {
        end1 - start1
    };
    outputResults(benchmarkOptions.isOutputAsCSV(), benchmarkOptions.isInNs(), names, results);
  }
}
