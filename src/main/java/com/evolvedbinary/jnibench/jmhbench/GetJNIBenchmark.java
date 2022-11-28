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

        @Param({"none", "copyout", "bytesum", "longsum"}) String checksum;
        AllocationCache.Checksum readChecksum;

        String keyBase;
        byte[] keyBytes;

        JMHCaller caller;

        @Setup
        public void setup() {
            this.caller = JMHCaller.fromStack();

            keyBase = "testKeyWithReturnValueSize" + String.format("%07d", valueSize) + "Bytes";

            keyBytes = keyBase.getBytes();

            readChecksum = AllocationCache.Checksum.valueOf(checksum);
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
        private IndirectByteBufferCache indirectByteBufferCache = new IndirectByteBufferCache();
        private PooledByteBufAllocator pooledByteBufAllocator;
        private NettyByteBufCache nettyByteBufCache = new NettyByteBufCache();

        int valueSize;
        int cacheSize;

        @Setup
        public void setup(GetJNIBenchmarkState benchmarkState, Blackhole blackhole) {
            valueSize = benchmarkState.valueSize;
            cacheSize = benchmarkState.cacheMB * GetJNIBenchmarkState.MB;

            switch (benchmarkState.caller.benchmarkMethod) {
                case "getIntoPooledNettyByteBuf":
                    pooledByteBufAllocator = PooledByteBufAllocator.DEFAULT;
                    //create a 0-sized cache so that we can use it to do checksum
                    nettyByteBufCache.setup(valueSize, 0/*cacheSize*/, benchmarkState.cacheEntryOverhead, benchmarkState.readChecksum, blackhole);
                    break;
                case "getIntoNettyByteBuf":
                    nettyByteBufCache.setup(valueSize, cacheSize, benchmarkState.cacheEntryOverhead, benchmarkState.readChecksum, blackhole);
                    break;
                case "getIntoDirectByteBuffer":
                    directByteBufferCache.setup(valueSize, cacheSize, benchmarkState.cacheEntryOverhead, benchmarkState.readChecksum, blackhole);
                    break;
                case "getIntoIndirectByteBufferSetRegion":
                case "getIntoIndirectByteBufferGetElements":
                case "getIntoIndirectByteBufferGetCritical":
                    indirectByteBufferCache.setup(valueSize, cacheSize, benchmarkState.cacheEntryOverhead, benchmarkState.readChecksum, blackhole);
                    break;
                case "getIntoDirectByteBufferFromUnsafe":
                case "buffersOnlyDirectByteBufferFromUnsafe":
                case "getIntoUnsafe":
                    unsafeBufferCache.setup(valueSize, cacheSize, benchmarkState.cacheEntryOverhead, benchmarkState.readChecksum, blackhole);
                    break;
                case "getIntoByteArraySetRegion":
                case "getIntoByteArrayGetElements":
                case "getIntoByteArrayCritical":
                    byteArrayCache.setup(valueSize, cacheSize, benchmarkState.cacheEntryOverhead, benchmarkState.readChecksum, blackhole);
                    break;
                default:
                    throw new RuntimeException("Don't know how to setup() for benchmark: " + benchmarkState.caller.benchmarkMethod);
            }
        }

        @TearDown
        public void tearDown(GetJNIBenchmarkState benchmarkState) {

            switch (benchmarkState.caller.benchmarkMethod) {
                case "getIntoPooledNettyByteBuf":
                    pooledByteBufAllocator = null;
                    break;
                case "getIntoNettyByteBuf":
                    nettyByteBufCache.tearDown();
                    break;
                case "getIntoDirectByteBuffer":
                    directByteBufferCache.tearDown();
                    break;
                case "getIntoIndirectByteBufferSetRegion":
                case "getIntoIndirectByteBufferGetElements":
                case "getIntoIndirectByteBufferGetCritical":
                    indirectByteBufferCache.tearDown();
                    break;
                case "getIntoDirectByteBufferFromUnsafe":
                case "buffersOnlyDirectByteBufferFromUnsafe":
                case "getIntoUnsafe":
                    unsafeBufferCache.tearDown();
                    break;
                case "getIntoByteArraySetRegion":
                case "getIntoByteArrayGetElements":
                case "getIntoByteArrayCritical":
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
    public void getIntoDirectByteBuffer(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        ByteBuffer byteBuffer = threadState.directByteBufferCache.acquire();
        byteBuffer.clear();
        GetPutJNI.getIntoDirectByteBuffer(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, byteBuffer, benchmarkState.valueSize);
        threadState.directByteBufferCache.checksumBuffer(byteBuffer);
        threadState.directByteBufferCache.release(byteBuffer);
    }

    @Benchmark
    public void getIntoUnsafe(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        UnsafeBufferCache.UnsafeBuffer unsafeBuffer = threadState.unsafeBufferCache.acquire();
        int size = GetPutJNI.getIntoUnsafe(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, unsafeBuffer.handle, benchmarkState.valueSize);
        threadState.unsafeBufferCache.checksumBuffer(unsafeBuffer);
        threadState.unsafeBufferCache.release(unsafeBuffer);
    }

    @Benchmark
    public void getIntoPooledNettyByteBuf(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        ByteBuf byteBuf = threadState.pooledByteBufAllocator.directBuffer(benchmarkState.valueSize);
        byteBuf.readerIndex(0);
        int size = GetPutJNI.getIntoUnsafe(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, byteBuf.memoryAddress(), benchmarkState.valueSize);
        byteBuf.writerIndex(size);
        //Use 0-sized cache which we created specially to do checksumBuffer operation
        threadState.nettyByteBufCache.checksumBuffer(byteBuf);
        // Allocated buffer already has retain count of 1
        byteBuf.release();
    }

    @Benchmark
    public void getIntoNettyByteBuf(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        ByteBuf byteBuf = threadState.nettyByteBufCache.acquire();
        byteBuf.readerIndex(0);
        int size = GetPutJNI.getIntoUnsafe(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, byteBuf.memoryAddress(), benchmarkState.valueSize);
        byteBuf.writerIndex(size);
        threadState.nettyByteBufCache.checksumBuffer(byteBuf);
        threadState.nettyByteBufCache.release(byteBuf);
    }

    @Benchmark
    public void getIntoByteArraySetRegion(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        byte[] array = threadState.byteArrayCache.acquire();
        int size = GetPutJNI.getIntoByteArraySetRegion(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, array, benchmarkState.valueSize);
        threadState.byteArrayCache.checksumBuffer(array);
        threadState.byteArrayCache.release(array);
    }

    @Benchmark
    public void getIntoByteArrayGetElements(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        byte[] array = threadState.byteArrayCache.acquire();
        int size = GetPutJNI.getIntoByteArrayGetElements(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, array, benchmarkState.valueSize);
        threadState.byteArrayCache.checksumBuffer(array);
        threadState.byteArrayCache.release(array);
    }

    @Benchmark
    public void getIntoByteArrayCritical(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        byte[] array = threadState.byteArrayCache.acquire();
        int size = GetPutJNI.getIntoByteArrayCritical(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, array, benchmarkState.valueSize);
        threadState.byteArrayCache.checksumBuffer(array);
        threadState.byteArrayCache.release(array);
    }

    //final supplied buffer(s)
    //TODO this can be done in as many different ways as supplying a byte[]
    //But why shouldn't we just expect the same performance as byte[] ?
    //Start with one instance (one that seems good in the byte[] case), and check for surprises...
    @Benchmark
    public void getIntoIndirectByteBufferSetRegion(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        ByteBuffer byteBuffer = threadState.indirectByteBufferCache.acquire();
        byteBuffer.clear();
        GetPutJNI.getIntoIndirectByteBufferSetRegion(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, byteBuffer, benchmarkState.valueSize);
        threadState.indirectByteBufferCache.checksumBuffer(byteBuffer);
        threadState.indirectByteBufferCache.release(byteBuffer);
    }

    @Benchmark
    public void getIntoIndirectByteBufferGetElements(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        ByteBuffer byteBuffer = threadState.indirectByteBufferCache.acquire();
        byteBuffer.clear();
        int size = GetPutJNI.getIntoIndirectByteBufferGetElements(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, byteBuffer, benchmarkState.valueSize);
        threadState.indirectByteBufferCache.checksumBuffer(byteBuffer);
        threadState.indirectByteBufferCache.release(byteBuffer);
    }

    @Benchmark
    public void getIntoIndirectByteBufferGetCritical(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState, Blackhole blackhole) {
        ByteBuffer byteBuffer = threadState.indirectByteBufferCache.acquire();
        byteBuffer.clear();
        int size = GetPutJNI.getIntoIndirectByteBufferGetCritical(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, byteBuffer, benchmarkState.valueSize);
        threadState.indirectByteBufferCache.checksumBuffer(byteBuffer);
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
                .param("checksum", "none", "copyout")
                .param("valueSize", "50", "4096", "16384", "65536")
                .param("cacheMB", "4")
                .warmupIterations(10)
                .measurementIterations(50)
                .include(GetJNIBenchmark.class.getSimpleName())
                .result("analysis/testplots/" +  simpleDateFormat.format(new Date()) + "_" + GetJNIBenchmark.class.getSimpleName() + ".csv")
                .build();

        new Runner(opt).run();
    }

}
