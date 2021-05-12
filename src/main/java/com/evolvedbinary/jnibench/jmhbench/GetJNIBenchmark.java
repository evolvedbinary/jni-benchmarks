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
import org.openjdk.jmh.annotations.*;

import java.nio.ByteBuffer;
import java.util.LinkedList;
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

        @Param({"16"}) int cacheMB;
        final static int MB = 1024 * 1024;

        @Param({"128"}) int cacheEntryOverhead;

        String keyBase;
        byte[] keyBytes;

        @Setup
        public void setup() {
            LOG.info("setup benchmark");
            keyBase = "testKeyWithReturnValueSize" + String.format("%07d", valueSize) + "Bytes";

            keyBytes = keyBase.getBytes();
        }

        @TearDown
        public void tearDown() {

        }
    }

    @State(Scope.Thread)
    public static class GetJNIThreadState {

        // As many elements of valueSize as fit in cacheSize
        private LinkedList<ByteBuffer> cacheBuffers = new LinkedList<>();

        int valueSize;
        int cacheSize;

        @Setup
        public void setup(GetJNIBenchmarkState benchmarkState) {
            LOG.info("setup thread");
            valueSize = benchmarkState.valueSize;
            cacheSize = benchmarkState.cacheMB * GetJNIBenchmarkState.MB;
            for (int totalBuffers = 0; totalBuffers < cacheSize; totalBuffers += valueSize + benchmarkState.cacheEntryOverhead)
            {
                cacheBuffers.addLast(ByteBuffer.allocateDirect(valueSize));
            }
        }

        ByteBuffer takeBuffer() {
            return cacheBuffers.getFirst();
        }

        void putBackBuffer(ByteBuffer byteBuffer) {
            cacheBuffers.addLast(byteBuffer);
        }
    }

    @Benchmark
    public void getIntoDirectByteBuffer(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState) {
        ByteBuffer byteBuffer = threadState.takeBuffer();
        GetPutJNI.getIntoDirectByteBuffer(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, byteBuffer, benchmarkState.valueSize);
        threadState.putBackBuffer(byteBuffer);
    }

    @Benchmark
    public void getIntoDirectByteBufferAllocate(GetJNIBenchmarkState benchmarkState, GetJNIThreadState threadState) {
        ByteBuffer byteBuffer = threadState.takeBuffer();
        GetPutJNI.getIntoDirectByteBufferAllocate(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, benchmarkState.valueSize);
        threadState.putBackBuffer(byteBuffer);
    }
}
