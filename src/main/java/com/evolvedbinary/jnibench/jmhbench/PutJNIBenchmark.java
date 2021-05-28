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
import com.evolvedbinary.jnibench.jmhbench.cache.*;
import com.evolvedbinary.jnibench.jmhbench.common.*;
import io.netty.buffer.PooledByteBufAllocator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import io.netty.buffer.ByteBuf;

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
@Warmup(iterations = 20, time = 100, timeUnit = TimeUnit.NANOSECONDS)
@Measurement(iterations = 200, time = 1000, timeUnit = TimeUnit.NANOSECONDS)
//@Warmup(iterations = 100, time = 1000, timeUnit = TimeUnit.NANOSECONDS)
//@Measurement(iterations = 500, time = 2000, timeUnit = TimeUnit.NANOSECONDS)
public class PutJNIBenchmark {

    private static final Logger LOG = Logger.getLogger(GetJNIBenchmark.class.getName());

    static {
        NarSystem.loadLibrary();
    }

    @State(Scope.Benchmark)
    public static class GetJNIBenchmarkState {

        @Param({
                "10",
                "50",
                "128",
                "512",
                "1024",
                "4096",
                "8192",
                "16384",
                "32768",
                "65536",
                "131072"})
        int valueSize;

        @Param({"4", "16"}) int cacheMB;
        final static int MB = 1024 * 1024;
        @Param({"1024"}) int cacheEntryOverhead;

        @Param({"none", "copyin"}) String preparation;
        AllocationCache.Prepare writePreparation;

        @Param({"17"}) byte fillByte;

        String keyBase;
        byte[] keyBytes;

        JMHCaller caller;

        @Setup
        public void setup() {
            this.caller = JMHCaller.fromStack();

            keyBase = "testKeyWithReturnValueSize" + String.format("%07d", valueSize) + "Bytes";

            keyBytes = keyBase.getBytes();

            writePreparation = AllocationCache.Prepare.valueOf(preparation);
        }

        @TearDown
        public void tearDown() {

        }
    }

    @State(Scope.Thread)
    public static class GetJNIThreadState {

        private final DirectByteBufferCache directByteBufferCache = new DirectByteBufferCache();
        private final UnsafeBufferCache unsafeBufferCache = new UnsafeBufferCache();
        private final ByteArrayCache byteArrayCache = new ByteArrayCache();
        private final IndirectByteBufferCache indirectByteBufferCache = new IndirectByteBufferCache();
        private final PooledByteBufAllocator pooledByteBufAllocator = PooledByteBufAllocator.DEFAULT;
        private final NettyByteBufCache nettyByteBufCache = new NettyByteBufCache();

        int valueSize;
        int cacheSize;

        @Setup
        public void setup(GetJNIBenchmarkState benchmarkState, Blackhole blackhole) {
            valueSize = benchmarkState.valueSize;
            cacheSize = benchmarkState.cacheMB * GetJNIBenchmarkState.MB;

            switch (benchmarkState.caller.benchmarkMethod) {
                case "putFromPooledNettyByteBuf":
                    break;
                case "putFromNettyByteBuf":
                    nettyByteBufCache.setup(valueSize, cacheSize, benchmarkState.cacheEntryOverhead, benchmarkState.writePreparation, blackhole);
                    break;
                case "putFromDirectByteBuffer":
                    directByteBufferCache.setup(valueSize, cacheSize, benchmarkState.cacheEntryOverhead, benchmarkState.writePreparation, blackhole);
                    break;
                case "putFromIndirectByteBufferSetRegion":
                case "putFromIndirectByteBufferGetElements":
                case "putFromIndirectByteBufferGetCritical":
                    indirectByteBufferCache.setup(valueSize, cacheSize, benchmarkState.cacheEntryOverhead, benchmarkState.writePreparation, blackhole);
                    break;
                case "putFromDirectByteBufferFromUnsafe":
                case "buffersOnlyDirectByteBufferFromUnsafe":
                case "putFromUnsafe":
                    unsafeBufferCache.setup(valueSize, cacheSize, benchmarkState.cacheEntryOverhead, benchmarkState.writePreparation, blackhole);
                    break;
                case "putFromByteArraySetRegion":
                case "putFromByteArrayGetElements":
                case "putFromByteArrayCritical":
                    byteArrayCache.setup(valueSize, cacheSize, benchmarkState.cacheEntryOverhead, benchmarkState.writePreparation, blackhole);
                    break;
                default:
                    throw new RuntimeException("Don't know how to setup() for benchmark: " + benchmarkState.caller.benchmarkMethod);
            }
        }

        @TearDown
        public void tearDown(GetJNIBenchmarkState benchmarkState) {

            switch (benchmarkState.caller.benchmarkMethod) {
                case "putFromPooledNettyByteBuf":
                    break;
                case "putFromNettyByteBuf":
                    nettyByteBufCache.tearDown();
                    break;
                case "putFromDirectByteBuffer":
                    directByteBufferCache.tearDown();
                    break;
                case "putFromIndirectByteBufferSetRegion":
                case "putFromIndirectByteBufferGetElements":
                case "putFromIndirectByteBufferGetCritical":
                    indirectByteBufferCache.tearDown();
                    break;
                case "putFromDirectByteBufferFromUnsafe":
                case "buffersOnlyDirectByteBufferFromUnsafe":
                case "putFromUnsafe":
                    unsafeBufferCache.tearDown();
                    break;
                case "putFromByteArraySetRegion":
                case "putFromByteArrayGetElements":
                case "putFromByteArrayCritical":
                    byteArrayCache.tearDown();
                    break;
                default:
                    throw new RuntimeException("Don't know how to tearDown() for benchmark: " + benchmarkState.caller.benchmarkMethod);
            }
        }
    }

    //@Benchmark
    public void buffersOnlyDirectByteBufferFromUnsafe(GetJNIThreadState threadState) {
        UnsafeBufferCache.UnsafeBuffer unsafeBuffer = threadState.unsafeBufferCache.acquire();
        threadState.unsafeBufferCache.release(unsafeBuffer);
    }

    @Benchmark
    public void putFromDirectByteBuffer(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        ByteBuffer byteBuffer = threadState.directByteBufferCache.acquire();
        byteBuffer.clear();
        threadState.directByteBufferCache.prepareBuffer(byteBuffer, benchmarkState.fillByte);
        int size = GetPutJNI.putFromDirectByteBuffer(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, byteBuffer, benchmarkState.valueSize);
        blackhole.consume(size);
        if (size < benchmarkState.valueSize) {
            throw new RuntimeException("Put actual " + size + ", requested " + benchmarkState.valueSize);
        }
        threadState.directByteBufferCache.release(byteBuffer);
    }

    @Benchmark
    public void putFromUnsafe(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        UnsafeBufferCache.UnsafeBuffer unsafeBuffer = threadState.unsafeBufferCache.acquire();
        threadState.unsafeBufferCache.prepareBuffer(unsafeBuffer, benchmarkState.fillByte);
        int size = GetPutJNI.putFromUnsafe(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, unsafeBuffer.handle, benchmarkState.valueSize);
        blackhole.consume(size);
        if (size < benchmarkState.valueSize) {
            throw new RuntimeException("Put actual " + size + ", requested " + benchmarkState.valueSize);
        }
        threadState.unsafeBufferCache.release(unsafeBuffer);
    }

    @Benchmark
    public void putFromPooledNettyByteBuf(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        ByteBuf byteBuf = threadState.pooledByteBufAllocator.directBuffer(benchmarkState.valueSize);
        //TODO prepareBuffer operation - we can use this for the "none" checksum in the meantime.

        int size = GetPutJNI.putFromUnsafe(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, byteBuf.memoryAddress(), benchmarkState.valueSize);
        blackhole.consume(size);
        if (size < benchmarkState.valueSize) {
            throw new RuntimeException("Put actual " + size + ", requested " + benchmarkState.valueSize);
        }
        // Allocated buffer already has retain count of 1
        byteBuf.release();
    }

    @Benchmark
    public void putFromNettyByteBuf(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        ByteBuf byteBuf = threadState.nettyByteBufCache.acquire();
        threadState.nettyByteBufCache.prepareBuffer(byteBuf, benchmarkState.fillByte);
        int size = GetPutJNI.putFromUnsafe(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, byteBuf.memoryAddress(), benchmarkState.valueSize);
        blackhole.consume(size);
        if (size < benchmarkState.valueSize) {
            throw new RuntimeException("Put actual " + size + ", requested " + benchmarkState.valueSize);
        }
        threadState.nettyByteBufCache.release(byteBuf);
    }

    @Benchmark
    public void putFromByteArraySetRegion(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        byte[] array = threadState.byteArrayCache.acquire();
        threadState.byteArrayCache.prepareBuffer(array, benchmarkState.fillByte);
        int size = GetPutJNI.putFromByteArrayGetRegion(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, array, benchmarkState.valueSize);
        blackhole.consume(size);
        if (size < benchmarkState.valueSize) {
            throw new RuntimeException("Put actual " + size + ", requested " + benchmarkState.valueSize);
        }
        threadState.byteArrayCache.release(array);
    }

    @Benchmark
    public void putFromByteArrayGetElements(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        byte[] array = threadState.byteArrayCache.acquire();
        threadState.byteArrayCache.prepareBuffer(array, benchmarkState.fillByte);
        int size = GetPutJNI.putFromByteArrayGetElements(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, array, benchmarkState.valueSize);
        blackhole.consume(size);
        if (size < benchmarkState.valueSize) {
            throw new RuntimeException("Put actual " + size + ", requested " + benchmarkState.valueSize);
        }
        threadState.byteArrayCache.release(array);
    }

    @Benchmark
    public void putFromByteArrayCritical(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        byte[] array = threadState.byteArrayCache.acquire();
        threadState.byteArrayCache.prepareBuffer(array, benchmarkState.fillByte);
        int size = GetPutJNI.putFromByteArrayCritical(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, array, benchmarkState.valueSize);
        blackhole.consume(size);
        if (size < benchmarkState.valueSize) {
            throw new RuntimeException("Put actual " + size + ", requested " + benchmarkState.valueSize);
        }
        threadState.byteArrayCache.release(array);
    }

    //final supplied buffer(s)
    //TODO this can be done in as many different ways as supplying a byte[]
    //But why shouldn't we just expect the same performance as byte[] ?
    //Start with one instance (one that seems good in the byte[] case), and check for surprises...
    @Benchmark
    public void putFromIndirectByteBufferSetRegion(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        ByteBuffer byteBuffer = threadState.indirectByteBufferCache.acquire();
        byteBuffer.clear();
        threadState.indirectByteBufferCache.prepareBuffer(byteBuffer, benchmarkState.fillByte);
        int size = GetPutJNI.putFromIndirectByteBufferGetRegion(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, byteBuffer, benchmarkState.valueSize);
        blackhole.consume(size);
        if (size < benchmarkState.valueSize) {
            throw new RuntimeException("Put actual " + size + ", requested " + benchmarkState.valueSize);
        }
        threadState.indirectByteBufferCache.release(byteBuffer);
    }

    @Benchmark
    public void putFromIndirectByteBufferGetElements(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        ByteBuffer byteBuffer = threadState.indirectByteBufferCache.acquire();
        byteBuffer.clear();
        threadState.indirectByteBufferCache.prepareBuffer(byteBuffer, benchmarkState.fillByte);
        int size = GetPutJNI.putFromIndirectByteBufferGetElements(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, byteBuffer, benchmarkState.valueSize);
        blackhole.consume(size);
        if (size < benchmarkState.valueSize) {
            throw new RuntimeException("Put actual " + size + ", requested " + benchmarkState.valueSize);
        }
        threadState.indirectByteBufferCache.release(byteBuffer);
    }

    @Benchmark
    public void putFromIndirectByteBufferGetCritical(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        ByteBuffer byteBuffer = threadState.indirectByteBufferCache.acquire();
        byteBuffer.clear();
        threadState.indirectByteBufferCache.prepareBuffer(byteBuffer, benchmarkState.fillByte);
        int size = GetPutJNI.putFromIndirectByteBufferGetCritical(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, byteBuffer, benchmarkState.valueSize);
        blackhole.consume(size);
        if (size < benchmarkState.valueSize) {
            throw new RuntimeException("Put actual " + size + ", requested " + benchmarkState.valueSize);
        }
        threadState.indirectByteBufferCache.release(byteBuffer);
    }

    //create/allocate the result buffers, analogous to the "into" methods (but no unsafe ones here)
    //TODO getReturnDirectByteBuffer
    //TODO getReturnIndirectByteBuffer
    //TODO getReturnByteArrayCritical
    //TODO getReturnByteArrayGetElements
    //TODO getReturnByteArraySetRegion

    //TODO env->NewDirectByteBuffer() - what aree the ownership rules ?
    //TODO track whether the byte[] copying/sharing methods we are using are doing copies
    //env->GetByteArrayElements(..., &is_copy)

    //TODO graphing - dig into the Python stuff a bit more

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
                .shouldFailOnError(true)
                .param("preparation", "none", "copyin")
                .param("valueSize", "50", "1024", "4096", "16384", "65536", "262144")
                .param("cacheMB", "4")
                .warmupIterations(10)
                .measurementIterations(50)
                .include(PutJNIBenchmark.class.getSimpleName())
                .result("analysis/testplots/" +  simpleDateFormat.format(new Date()) + "_" + GetJNIBenchmark.class.getSimpleName() + ".csv")
                .build();

        new Runner(opt).run();
    }

}
