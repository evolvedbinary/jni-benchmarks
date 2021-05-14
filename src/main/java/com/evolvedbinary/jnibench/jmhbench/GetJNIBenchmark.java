/**
 * Copyright © 2021, Evolved Binary Ltd
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

import com.evolvedbinary.jnibench.common.getputjni.GetPutJNI;
import com.evolvedbinary.jnibench.consbench.NarSystem;
import com.evolvedbinary.jnibench.jmhbench.common.ByteArrayCache;
import com.evolvedbinary.jnibench.jmhbench.common.DirectByteBufferCache;
import com.evolvedbinary.jnibench.jmhbench.common.JMHCaller;
import com.evolvedbinary.jnibench.jmhbench.common.UnsafeBufferCache;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Benchmark getting byte arrays from native methods.
 */
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 100, time = 1000, timeUnit = TimeUnit.NANOSECONDS)
@Measurement(iterations = 500, time = 2000, timeUnit = TimeUnit.NANOSECONDS)
public class GetJNIBenchmark {

    private static final Logger LOG = Logger.getLogger(GetJNIBenchmark.class.getName());

    static {
        NarSystem.loadLibrary();
    }

    @State(Scope.Benchmark)
    public static class GetJNIBenchmarkState {

        @Param({
                "10",
                "50",
                "512",
                "1024",
                "4096",
                "16384",
                "65536"})
        int valueSize;

        @Param({"4", "16"}) int cacheMB;
        final static int MB = 1024 * 1024;
        @Param({"1024"}) int cacheEntryOverhead;

        enum Checksum {
            none,
            copyout,
            bytesum,
            longsum,
        };
        @Param({"none", "copyout", "bytesum", "longsum"}) String checksum;
        Checksum readChecksum;

        String keyBase;
        byte[] keyBytes;

        JMHCaller caller;

        @Setup
        public void setup() {
            this.caller = JMHCaller.fromStack();

            keyBase = "testKeyWithReturnValueSize" + String.format("%07d", valueSize) + "Bytes";

            keyBytes = keyBase.getBytes();

            readChecksum = Checksum.valueOf(checksum);
        }

        @TearDown
        public void tearDown() {

        }
    }

    @State(Scope.Thread)
    public static class GetJNIThreadState {

        private DirectByteBufferCache directByteBufferCache = new DirectByteBufferCache();
        private UnsafeBufferCache unsafeBufferCache = new UnsafeBufferCache();
        private ByteArrayCache byteArrayCache = new ByteArrayCache();

        int valueSize;
        int cacheSize;

        @Setup
        public void setup(GetJNIBenchmarkState benchmarkState) {
            valueSize = benchmarkState.valueSize;
            cacheSize = benchmarkState.cacheMB * GetJNIBenchmarkState.MB;

            switch (benchmarkState.caller.benchmarkMethod) {
                case "getIntoDirectByteBuffer":
                    directByteBufferCache.setup(valueSize, cacheSize, benchmarkState.cacheEntryOverhead);
                    break;
                case "getIntoDirectByteBufferFromUnsafe":
                case "buffersOnlyDirectByteBufferFromUnsafe":
                case "getIntoUnsafe":
                    unsafeBufferCache.setup(valueSize, cacheSize, benchmarkState.cacheEntryOverhead);
                    break;
                case "getIntoByteArraySetRegion":
                    byteArrayCache.setup(valueSize, cacheSize, benchmarkState.cacheEntryOverhead);
                    break;
                default:
                    throw new RuntimeException("Don't know how to setup() for benchmark: " + benchmarkState.caller.benchmarkMethod);
            }
        }

        @TearDown
        public void tearDown(GetJNIBenchmarkState benchmarkState) {

            switch (benchmarkState.caller.benchmarkMethod) {
                case "getIntoDirectByteBuffer":
                    directByteBufferCache.tearDown();
                    break;
                case "getIntoDirectByteBufferFromUnsafe":
                case "buffersOnlyDirectByteBufferFromUnsafe":
                case "getIntoUnsafe":
                    unsafeBufferCache.tearDown();
                    break;
                case "getIntoByteArraySetRegion":
                    byteArrayCache.tearDown();
                    break;
                default:
                    throw new RuntimeException("Don't know how to tearDown() for benchmark: " + benchmarkState.caller.benchmarkMethod);
            }
        }
    }

    @Benchmark
    public void getIntoDirectByteBuffer(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        ByteBuffer byteBuffer = threadState.directByteBufferCache.acquire();
        byteBuffer.clear();
        GetPutJNI.getIntoDirectByteBuffer(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, byteBuffer, benchmarkState.valueSize);
        switch (benchmarkState.readChecksum) {
            case copyout:
                blackhole.consume(threadState.directByteBufferCache.copyOut(byteBuffer));
                break;
            case bytesum:
                blackhole.consume(threadState.directByteBufferCache.byteChecksum(byteBuffer));
                break;
            case longsum:
                blackhole.consume(threadState.directByteBufferCache.longChecksum(byteBuffer));
                break;
            case none:
                break;
        }
        threadState.directByteBufferCache.release(byteBuffer);
    }

    @Benchmark
    public void buffersOnlyDirectByteBufferFromUnsafe(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState) {
        UnsafeBufferCache.UnsafeBuffer unsafeBuffer = threadState.unsafeBufferCache.acquire();
        threadState.unsafeBufferCache.release(unsafeBuffer);
    }

    @Benchmark
    public void getIntoDirectByteBufferFromUnsafe(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        UnsafeBufferCache.UnsafeBuffer unsafeBuffer = threadState.unsafeBufferCache.acquire();
        ByteBuffer byteBuffer = GetPutJNI.getIntoDirectByteBufferFromUnsafe(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, unsafeBuffer.handle, benchmarkState.valueSize);
        switch (benchmarkState.readChecksum) {
            case bytesum:
                blackhole.consume(threadState.unsafeBufferCache.byteChecksum(unsafeBuffer));
                break;
            case copyout:
                blackhole.consume(threadState.unsafeBufferCache.copyOut(unsafeBuffer));
                break;
            case longsum:
                blackhole.consume(threadState.unsafeBufferCache.longChecksum(unsafeBuffer));
                break;
            case none:
                break;
        }
        threadState.unsafeBufferCache.release(unsafeBuffer);
    }

    @Benchmark
    public void getIntoUnsafe(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        UnsafeBufferCache.UnsafeBuffer unsafeBuffer = threadState.unsafeBufferCache.acquire();
        int size = GetPutJNI.getIntoUnsafe(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, unsafeBuffer.handle, benchmarkState.valueSize);
        switch (benchmarkState.readChecksum) {
            case bytesum:
                blackhole.consume(threadState.unsafeBufferCache.byteChecksum(unsafeBuffer));
                break;
            case copyout:
                blackhole.consume(threadState.unsafeBufferCache.copyOut(unsafeBuffer));
                break;
            case longsum:
                blackhole.consume(threadState.unsafeBufferCache.longChecksum(unsafeBuffer));
                break;
            case none:
                break;
        }
        threadState.unsafeBufferCache.release(unsafeBuffer);
    }

    @Benchmark
    public void getIntoByteArraySetRegion(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        byte[] array = threadState.byteArrayCache.acquire();
        int size = GetPutJNI.getIntoByteArraySetRegion(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, array, benchmarkState.valueSize);
        switch (benchmarkState.readChecksum) {
            case bytesum:
                blackhole.consume(threadState.byteArrayCache.byteChecksum(array));
                break;
            case copyout:
                blackhole.consume(threadState.byteArrayCache.copyOut(array));
                break;
            case longsum:
                blackhole.consume(threadState.byteArrayCache.longChecksum(array));
                break;
            case none:
                break;
        }
        threadState.byteArrayCache.release(array);
    }

    /**
     * Run from the IDE
     *
     * You will need this in the VM args of the run configuration,
     * in order for NAR to find at runtime the native lib it has built:
     *
     * -Djava.library.path=PATH_TO_REPO/target/jni-benchmarks-1.0.0-SNAPSHOT-application/jni-benchmarks-1.0.0-SNAPSHOT/lib
     *
     * The parameters we set here configure for debugging,
     * typically we want a much shorter runs than is needed for accurate benchmarking
     * SO DON'T TRUST THE NUMBERS GENERATED BY THIS RUN
     * fork(0) runs everything is in a single process so we don't need to configure JDWP
     * Again this affects JMH
     * {@link https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_12_Forking.java}
     * It's a convenience for debugging the tests so that they actually run, that is all.
     *
     * @param args
     * @throws RunnerException
     */
    public static void main(String[] args) throws RunnerException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss.SSS");
        Options opt = new OptionsBuilder()
                .forks(0)
                .param("checksum", "none", "copyout")
                //.param("valueSize", "50", "4096", "16384", "65536")
                .param("valueSize", "65536")
                .param("cacheMB", "4")
                .warmupIterations(10)
                .measurementIterations(50)
                .include(GetJNIBenchmark.class.getSimpleName())
                .result("results/" +  simpleDateFormat.format(new Date()) + "_" + GetJNIBenchmark.class.getSimpleName() + ".csv")
                .build();

        new Runner(opt).run();
    }

}
