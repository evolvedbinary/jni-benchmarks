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

import com.evolvedbinary.jnibench.common.call.*;

import static com.evolvedbinary.jnibench.consbench.BenchmarkHelper.outputResults;
import static com.evolvedbinary.jnibench.consbench.BenchmarkHelper.time;

public class CallBenchmark implements BenchmarkInterface {
  @Override
  public void test(final BenchmarkOptions benchmarkOptions) {
    if (benchmarkOptions.isClose()) {
      testWithClose(benchmarkOptions.getIterations(), benchmarkOptions.isOutputAsCSV(), benchmarkOptions.isInNs());
    } else {
      testWithoutClose(benchmarkOptions.getIterations(), benchmarkOptions.isOutputAsCSV(), benchmarkOptions.isInNs());
    }
  }

  private static void testWithClose(final int iterations, final boolean outputAsCSV, final boolean inNs) {
    //TEST1 - Foo By Call
    final long start1 = time(inNs);
    for(int i = 0; i < iterations; i++) {
      final FooByCall fooByCall = new FooByCall();
      fooByCall.close();
    }
    final long end1 = time(inNs);

    //TEST2 - Foo By Call Static
    final long start2 = time(inNs);
    for(int i = 0; i < iterations; i++) {
      final FooByCallStatic fooByCallStatic = new FooByCallStatic();
      fooByCallStatic.close();
    }
    final long end2 = time(inNs);

    //TEST3 - Foo By Call Invoke
    final long start3 = time(inNs);
    for(int i = 0; i < iterations; i++) {
      final FooByCallInvoke fooByCallStatic = new FooByCallInvoke();
      fooByCallStatic.close();
    }
    final long end3 = time(inNs);

    //TEST4 - Foo By Call Final
    final long start4 = time(inNs);
    for(int i = 0; i < iterations; i++) {
      final FooByCallFinal fooByCallFinal = new FooByCallFinal();
      fooByCallFinal.close();
    }
    final long end4 = time(inNs);


    //TEST5 - Foo By Call Static Final
    final long start5 = time(inNs);
    for(int i = 0; i < iterations; i++) {
      final FooByCallStaticFinal fooByCallStaticFinal = new FooByCallStaticFinal();
      fooByCallStaticFinal.close();
    }
    final long end5 = time(inNs);

    //TEST6 - Foo By Call Invoke Final
    final long start6 = time(inNs);
    for(int i = 0; i < iterations; i++) {
      final FooByCallInvokeFinal fooByCallInvokeFinal = new FooByCallInvokeFinal();
      fooByCallInvokeFinal.close();
    }
    final long end6 = time(inNs);

    String[] names = {
        "FooByCall",
        "FooByCallStatic",
        "FooByCallInvoke",
        "FooByCallFinal",
        "FooByCallStaticFinal",
        "FooByCallInvokeFinal"
    };
    long[] results = {
        end1 - start1,
        end2 - start2,
        end3 - start3,
        end4 - start4,
        end5 - start5,
        end6 - start6
    };
    outputResults(outputAsCSV, inNs, names, results);
  }

  private static void testWithoutClose(final int iterations, final boolean outputAsCSV, final boolean inNs) {
    //TEST1 - Foo By Call
    final long start1 = time(inNs);
    for(int i = 0; i < iterations; i++) {
      final FooByCall fooByCall = new FooByCall();
    }
    final long end1 = time(inNs);

    //TEST2 - Foo By Call Static
    final long start2 = time(inNs);
    for(int i = 0; i < iterations; i++) {
      final FooByCallStatic fooByCallStatic = new FooByCallStatic();
    }
    final long end2 = time(inNs);

    //TEST3 - Foo By Call Invoke
    final long start3 = time(inNs);
    for(int i = 0; i < iterations; i++) {
      final FooByCallInvoke fooByCallInvoke = new FooByCallInvoke();
    }
    final long end3 = time(inNs);

    //TEST4 - Foo By Call Final
    final long start4 = time(inNs);
    for(int i = 0; i < iterations; i++) {
      final FooByCallFinal fooByCallFinal = new FooByCallFinal();
    }
    final long end4 = time(inNs);


    //TEST5 - Foo By Call Static Final
    final long start5 = time(inNs);
    for(int i = 0; i < iterations; i++) {
      final FooByCallStaticFinal fooByCallStaticFinal = new FooByCallStaticFinal();
    }
    final long end5 = time(inNs);

    //TEST6 - Foo By Call Invoke Final
    final long start6 = time(inNs);
    for(int i = 0; i < iterations; i++) {
      final FooByCallInvokeFinal fooByCallInvokeFinal = new FooByCallInvokeFinal();
    }
    final long end6 = time(inNs);

    String[] names = {
        "FooByCall",
        "FooByCallStatic",
        "FooByCallInvoke",
        "FooByCallFinal",
        "FooByCallStaticFinal",
        "FooByCallInvokeFinal"
    };
    long[] results = {
        end1 - start1,
        end2 - start2,
        end3 - start3,
        end4 - start4,
        end5 - start5,
        end6 - start6
    };
    outputResults(outputAsCSV, inNs, names, results);
  }
}
