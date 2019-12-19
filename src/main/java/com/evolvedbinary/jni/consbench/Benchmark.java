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
package com.evolvedbinary.jni.consbench;

/**
 * A small JNI Benchmark to show the difference
 * in cost between various models of Object Construction
 * for a Java API that wraps a C++ API using JNI
 *
 * @author <a href="mailto:adam@evolvedbinary.com">Adam Retter</a>
 */
public class Benchmark {
    private final static int DEFAULT_ITERATIONS = 1_000_000;

    public static final void main(final String args[]) {

        int iterations = DEFAULT_ITERATIONS;
        boolean outputAsCSV = false;
        boolean inNs = false;
        boolean close = false;

        if (args != null && args.length > 0) {
            for (String arg : args) {
                if (arg.startsWith("--iterations=")) {
                    arg = arg.substring("--iterations=".length());
                    iterations = Integer.parseInt(arg);
                } else if (arg.equals("--csv")) {
                    outputAsCSV = true;
                } else if (arg.equals("--ns")) {
                    inNs = true;
                } else if (arg.equals("--close")) {
                    close = true;
                } else if (arg.equals("--help") || arg.equals("-h") || arg.equals("/?")) {
                    System.out.println();
                    System.out.println("Benchmark");
                    System.out.println("--iterations=n    set the number of iterations");
                    System.out.println("--csv             output results in CSV format");
                    System.out.println("--ns              compute times in ns as opposed to ms");
                    System.out.println("--close           native objects should be closed (disposed) after use");
                    System.out.println();
                }
            }
        }

        NarSystem.loadLibrary();

        if (close) {
            testWithClose(iterations, outputAsCSV, inNs);
        } else {
            testWithoutClose(iterations, outputAsCSV, inNs);
        }
    }

    private static final void testWithClose(final int iterations, final boolean outputAsCSV, final boolean inNs) {
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

        outputResults(outputAsCSV, inNs,
                end1 - start1,
                end2 - start2,
                end3 - start3,
                end4 - start4,
                end5 - start5,
                end6 - start6
        );
    }

    private static final void testWithoutClose(final int iterations, final boolean outputAsCSV, final boolean inNs) {
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

        outputResults(outputAsCSV, inNs,
                end1 - start1,
                end2 - start2,
                end3 - start3,
                end4 - start4,
                end5 - start5,
                end6 - start6
        );
    }

    private static final void outputResults(final boolean outputAsCSV, final boolean inNs,
            final long res1, final long res2, final long res3, final long res4, final long res5, final long res6) {
        if (outputAsCSV) {
            System.out.println(String.format("%d,%d,%d,%d,%d,%d", res1, res2, res3, res4, res5, res6));
        } else {
            final String timeUnits = timeUnits(inNs);
            System.out.println("FooByCall: " + res1 + timeUnits);
            System.out.println("FooByCallStatic: " + res2 + timeUnits);
            System.out.println("FooByCallInvoke: " + res3 + timeUnits);
            System.out.println("FooByCallFinal: " + res4 + timeUnits);
            System.out.println("FooByCallStaticFinal: " + res5 + timeUnits);
            System.out.println("FooByCallInvokeFinal: " + res6 + timeUnits);
        }
    }

    private static final long time(final boolean inNs) {
        if (inNs) {
            return System.nanoTime();
        } else {
            return System.currentTimeMillis();
        }
    }

    private static final String timeUnits(final boolean inNs) {
        if (inNs) {
            return "ns";
        } else {
            return "ms";
        }
    }
}
