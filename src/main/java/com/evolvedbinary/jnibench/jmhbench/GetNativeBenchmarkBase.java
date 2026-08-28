package com.evolvedbinary.jnibench.jmhbench;

import com.evolvedbinary.jnibench.jmhbench.cache.AllocationCache;
import com.evolvedbinary.jnibench.jmhbench.common.JMHCaller;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import com.evolvedbinary.jnibench.consbench.NarSystem;

/**
 * Benchmark getting byte arrays from native methods.
 */
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 20, time = 100, timeUnit = TimeUnit.NANOSECONDS)
@Measurement(iterations = 200, time = 1000, timeUnit = TimeUnit.NANOSECONDS)
public class GetNativeBenchmarkBase {

    private static final Logger LOG = Logger.getLogger(GetJNIBenchmark.class.getName());

    static {
        NarSystem.loadLibrary();
    }

    @State(Scope.Benchmark)
    public static class GetNativeBenchmarkState {

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

        @Param({"4", "16"})
        int cacheMB;
        final static int MB = 1024 * 1024;
        @Param({"1024"})
        int cacheEntryOverhead;

        @Param({"none", "copyout", "bytesum", "longsum"})
        String checksum;
        AllocationCache.Checksum readChecksum;

        String keyBase;
        byte[] keyBytes;

        JMHCaller caller;

        protected final JMHCaller getCaller() {
            return caller;
        }

        @Setup
        public void setup() {
            this.caller = JMHCaller.fromStack();
            keyBase = "testKeyWithReturnValueSize" + String.format("%07d", valueSize) + "Bytes";

            keyBytes = keyBase.getBytes();
            readChecksum = AllocationCache.Checksum.valueOf(checksum);
        }
    }
}
