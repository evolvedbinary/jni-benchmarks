package com.evolvedbinary.jnibench.jmhbench;

import java.util.concurrent.TimeUnit;

import com.evolvedbinary.jnibench.common.getputjni.GetPutJNI;
import com.evolvedbinary.jnibench.consbench.NarSystem;
import com.evolvedbinary.jnibench.jmhbench.PutNativeBenchmarkBase.PutNativeBenchmarkState;
import com.evolvedbinary.jnibench.jmhbench.cache.AllocationCache;
import com.evolvedbinary.jnibench.jmhbench.cache.ByteArrayCache;
import com.evolvedbinary.jnibench.jmhbench.cache.DirectByteBufferCache;
import com.evolvedbinary.jnibench.jmhbench.cache.IndirectByteBufferCache;
import com.evolvedbinary.jnibench.jmhbench.cache.NettyByteBufCache;
import com.evolvedbinary.jnibench.jmhbench.cache.UnsafeBufferCache;
import com.evolvedbinary.jnibench.jmhbench.common.JMHCaller;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.logging.Logger;

import com.evolvedbinary.jnibench.jmhbench.cache.AllocationCache;
import com.evolvedbinary.jnibench.jmhbench.common.JMHCaller;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 20, time = 100, timeUnit = TimeUnit.NANOSECONDS)
@Measurement(iterations = 200, time = 1000, timeUnit = TimeUnit.NANOSECONDS)
//@Warmup(iterations = 100, time = 1000, timeUnit = TimeUnit.NANOSECONDS)
//@Measurement(iterations = 500, time = 2000, timeUnit = TimeUnit.NANOSECONDS)
public class PutNativeBenchmarkBase {
    
  private static final Logger LOG = Logger.getLogger(GetJNIBenchmark.class.getName());

  static {
    NarSystem.loadLibrary();
  }

  @State(Scope.Benchmark)
  public static class PutNativeBenchmarkState {

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

    @Param({"none", "copyin"})
    String preparation;
    AllocationCache.Prepare writePreparation;

    @Param({"17"})
    byte fillByte;

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

      writePreparation = AllocationCache.Prepare.valueOf(preparation);
    }
  }
}
