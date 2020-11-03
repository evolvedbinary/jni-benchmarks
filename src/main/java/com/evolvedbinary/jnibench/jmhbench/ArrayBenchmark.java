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
package com.evolvedbinary.jnibench.jmhbench;

import com.evolvedbinary.jnibench.common.array.*;
import com.evolvedbinary.jnibench.consbench.NarSystem;
import org.openjdk.jmh.annotations.*;

import java.util.List;
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
    AllocateInJavaGet2DArray allocateInJavaGet2DArray;
    AllocateInCppGetArray allocateInCppGetArray;
    AllocateInCppGet2DArray allocateInCppGet2DArray;
    AllocateInCppGet2DArrayListWrapper jni2DGetArrayListWrapper;
    AllocateInJavaGetArrayList allocateInJavaGetArrayList;
    AllocateInCppGetArrayList allocateInCppGetArrayList;

    public BenchmarkState() {

    }

    public BenchmarkState(final FooNativeObjectArray fooObjectArray,
                          final AllocateInJavaGet2DArray allocateInJavaGet2DArray,
                          final AllocateInCppGetArray allocateInCppGetArray,
                          final AllocateInCppGet2DArray allocateInCppGet2DArray,
                          final AllocateInCppGet2DArrayListWrapper jni2DGetArrayListWrapper,
                          final AllocateInJavaGetArrayList allocateInJavaGetArrayList,
                          final AllocateInCppGetArrayList allocateInCppGetArrayList) {
      this.fooObjectArray = fooObjectArray;
      this.allocateInJavaGet2DArray = allocateInJavaGet2DArray;
      this.allocateInCppGetArray = allocateInCppGetArray;
      this.allocateInCppGet2DArray = allocateInCppGet2DArray;
      this.jni2DGetArrayListWrapper = jni2DGetArrayListWrapper;
      this.allocateInJavaGetArrayList = allocateInJavaGetArrayList;
      this.allocateInCppGetArrayList = allocateInCppGetArrayList;
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
      allocateInJavaGet2DArray = new AllocateInJavaGet2DArray();
      allocateInCppGetArray = new AllocateInCppGetArray();
      allocateInCppGet2DArray = new AllocateInCppGet2DArray();
      jni2DGetArrayListWrapper = new AllocateInCppGet2DArrayListWrapper();
      allocateInJavaGetArrayList = new AllocateInJavaGetArrayList();
      allocateInCppGetArrayList = new AllocateInCppGetArrayList();
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
  public List<FooObject> allocateInJavaArray(BenchmarkState benchmarkState) {
    return benchmarkState.allocateInJavaGet2DArray.getObjectList(benchmarkState.fooObjectArray);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public List<FooObject> jniGetArray(BenchmarkState benchmarkState) {
    return benchmarkState.allocateInCppGetArray.getObjectList(benchmarkState.fooObjectArray);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public List<FooObject> jni2DGetArray(BenchmarkState benchmarkState) {
    return benchmarkState.allocateInCppGet2DArray.getObjectList(benchmarkState.fooObjectArray);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public List<FooObject> jni2DGetArrayListWrapper(BenchmarkState benchmarkState) {
    return benchmarkState.jni2DGetArrayListWrapper.getObjectList(benchmarkState.fooObjectArray);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public List<FooObject> allocateInJavaArrayList(BenchmarkState benchmarkState) {
    return benchmarkState.allocateInJavaGetArrayList.getObjectList(benchmarkState.fooObjectArray);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public List<FooObject> allocateInCppGetArrayList(BenchmarkState benchmarkState) {
    return benchmarkState.allocateInCppGetArrayList.getObjectList(benchmarkState.fooObjectArray);
  }
}
