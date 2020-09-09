/**
 * Copyright © 2016, Evolved Binary Ltd
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
    final int iterations = benchmarkOptions.getIterations();
    final FooNativeObjectArray fooObjectArray = new FooNativeObjectArray(fooObjects);

    final ArrayBenchmarkFixture[] benchmarkFixtures = {
            new ArrayBenchmarkFixture("Allocate array in Java", AllocateInJavaGetArray::new),
            new ArrayBenchmarkFixture("Allocate array of mutable objects in Java", AllocateInJavaGetMutableArray::new),
            new ArrayBenchmarkFixture("Allocate 2D array in Java", AllocateInJavaGet2DArray::new),
            new ArrayBenchmarkFixture("Allocate array in CPP", AllocateInCppGetArray::new),
            new ArrayBenchmarkFixture("Allocate 2D object array in CPP", AllocateInCppGet2DArray::new),
            new ArrayBenchmarkFixture("Allocate 2D object array in CPP and wrap result in custom Java List", AllocateInCppGet2DArrayListWrapper::new),
            new ArrayBenchmarkFixture("Allocate array list in Java", AllocateInJavaGetArrayList::new),
            new ArrayBenchmarkFixture("Allocate array list in CPP", AllocateInCppGetArrayList::new)
    };

    // run each benchmark fixture
    for (final ArrayBenchmarkFixture benchmarkFixture : benchmarkFixtures) {
      final JniListSupplier<FooObject> listSupplier = benchmarkFixture.listSupplierConstructor.get();
      benchmarkFixture.start = time(benchmarkOptions.isInNs());
      for (int i = 0; i < iterations; i++) {
        listSupplier.getObjectList(fooObjectArray);
      }
      benchmarkFixture.end = time(benchmarkOptions.isInNs());
    }

    // output the results of the benchmarks
    outputResults(benchmarkOptions.isOutputAsCSV(), benchmarkOptions.isInNs(), benchmarkFixtures);
  }
}
