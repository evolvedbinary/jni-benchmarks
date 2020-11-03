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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        boolean noCsvHeader = false;
        boolean inNs = false;
        boolean close = false;
        String benchmarkName = "CallBenchmark";
        final Map<String, List<String>> params = new TreeMap<>();

        if (args != null && args.length > 0) {
            for (String arg : args) {
                if (arg.startsWith("--iterations=")) {
                    arg = arg.substring("--iterations=".length());
                    iterations = Integer.parseInt(arg);
                } else if (arg.equals("--csv")) {
                    outputAsCSV = true;
                } else if (arg.equals("--no-csv-header")) {
                    noCsvHeader = true;
                } else if (arg.equals("--ns")) {
                    inNs = true;
                } else if (arg.equals("--close")) {
                    close = true;
                } else if (arg.startsWith("--benchmark=")) {
                    benchmarkName = arg.substring("--benchmark=".length());
                } else if (arg.startsWith("--param=")) {
                    final String param = arg.substring("--param=".length());
                    final String[] nameValue = param.split(":");
                    List<String> values = params.get(nameValue[0]);
                    if (values == null) {
                        values = new ArrayList<>();
                        params.put(nameValue[0], values);
                    }
                    values.add(nameValue[1]);
                } else if (arg.equals("--help") || arg.equals("-h") || arg.equals("/?")) {
                    System.out.println();
                    System.out.println("Benchmark");
                    System.out.println("--iterations=n          set the number of iterations");
                    System.out.println("--csv                   output results in CSV format");
                    System.out.println("--no-csv-header         disable CSV header line");
                    System.out.println("--ns                    compute times in ns as opposed to ms");
                    System.out.println("--close                 native objects should be closed (disposed) after use");
                    System.out.println("--benchmark=name        name of the benchmark");
                    System.out.println("--param=name:value      parameter for the benchmark");
                    System.out.println();
                }
            }
        }

        NarSystem.loadLibrary();

        final BenchmarkOptions benchmarkOptions = new BenchmarkOptions(iterations, outputAsCSV, noCsvHeader, inNs, close, params);

        try {
            final Class<?> benchmarkClazz = Class.forName("com.evolvedbinary.jnibench.consbench." + benchmarkName);
            final BenchmarkInterface benchmarkObject = (BenchmarkInterface) benchmarkClazz.newInstance();
            benchmarkObject.test(benchmarkOptions);
        } catch (final ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }
}
