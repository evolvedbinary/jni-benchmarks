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

import com.evolvedbinary.jnibench.common.NativeBackedObject;
import com.evolvedbinary.jnibench.common.call.*;

import java.util.Arrays;

import static com.evolvedbinary.jnibench.consbench.BenchmarkHelper.outputResults;
import static com.evolvedbinary.jnibench.consbench.BenchmarkHelper.time;

public class CallBenchmark implements BenchmarkInterface {

  @Override
  public void test(final BenchmarkOptions benchmarkOptions) {
    if (benchmarkOptions.isClose()) {
      testWithClose(benchmarkOptions.getIterations(), benchmarkOptions.isOutputAsCSV(), benchmarkOptions.isNoCsvHeader(), benchmarkOptions.isInNs());
    } else {
      testWithoutClose(benchmarkOptions.getIterations(), benchmarkOptions.isOutputAsCSV(), benchmarkOptions.isNoCsvHeader(), benchmarkOptions.isInNs());
    }
  }

  private static void testWithClose(final int iterations, final boolean outputAsCSV, final boolean noCsvHeader, final boolean inNs) {
    final CallBenchmarkFixture[] benchmarkFixtures = {
            new CallBenchmarkFixture("FooByCall", FooByCall::new),
            new CallBenchmarkFixture("FooByCallStatic", FooByCallStatic::new),
            new CallBenchmarkFixture("FooByCallInvoke", FooByCallInvoke::new),
            new CallBenchmarkFixture("FooByCallFinal", FooByCallFinal::new),
            new CallBenchmarkFixture("FooByCallStaticFinal", FooByCallStaticFinal::new),
            new CallBenchmarkFixture("FooByCallInvokeFinal", FooByCallInvokeFinal::new),
    };

    // run each benchmark fixture
    for (final CallBenchmarkFixture benchmarkFixture : benchmarkFixtures) {
      benchmarkFixture.start = time(inNs);
      for (int i = 0; i < iterations; i++) {
        final NativeBackedObject nativeBackedObject = benchmarkFixture.nativeBackedObjectConstructor.get();
        nativeBackedObject.close(); // CLOSE
      }
      benchmarkFixture.end = time(inNs);
    }

    outputResults(outputAsCSV, noCsvHeader, inNs, Arrays.asList(benchmarkFixtures));
  }

  private static void testWithoutClose(final int iterations, final boolean outputAsCSV, final boolean noCsvHeader, final boolean inNs) {
    final CallBenchmarkFixture[] benchmarkFixtures = {
            new CallBenchmarkFixture("FooByCall", FooByCall::new),
            new CallBenchmarkFixture("FooByCallStatic", FooByCallStatic::new),
            new CallBenchmarkFixture("FooByCallInvoke", FooByCallInvoke::new),
            new CallBenchmarkFixture("FooByCallFinal", FooByCallFinal::new),
            new CallBenchmarkFixture("FooByCallStaticFinal", FooByCallStaticFinal::new),
            new CallBenchmarkFixture("FooByCallInvokeFinal", FooByCallInvokeFinal::new),
    };

    // run each benchmark fixture
    for (final CallBenchmarkFixture benchmarkFixture : benchmarkFixtures) {
      benchmarkFixture.start = time(inNs);
      for (int i = 0; i < iterations; i++) {
        final NativeBackedObject nativeBackedObject = benchmarkFixture.nativeBackedObjectConstructor.get();
      }
      benchmarkFixture.end = time(inNs);
    }

    outputResults(outputAsCSV, noCsvHeader, inNs, Arrays.asList(benchmarkFixtures));
  }
}
