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
