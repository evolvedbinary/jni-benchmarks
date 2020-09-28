package com.evolvedbinary.jnibench.jmhbench;

import com.evolvedbinary.jnibench.common.bytearray.GetByteArray;
import com.evolvedbinary.jnibench.consbench.NarSystem;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

// https://www.oracle.com/technical-resources/articles/java/architect-benchmarking.html
public class ByteArrayBenchmark {

  static {
    NarSystem.loadLibrary();
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    @Param({
        "10",
        "50",
        "512",
        "1024",
        "4096",
        "16384"})
    int valueSize;

    String key;

    @Setup
    public void setup() {
      key = "testKeyWithReturnValueSize" + String.format("%07d" , valueSize) + "Bytes";
    }

    @TearDown
    public void tearDown() {

    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public void basicGetByteArray(BenchmarkState benchmarkState) {
    GetByteArray.get(benchmarkState.key.getBytes(), 0, benchmarkState.key.length());
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public void preallocatedGetByteArray(BenchmarkState benchmarkState) {
    byte[] valueBuffer = new byte[benchmarkState.valueSize];
    GetByteArray.get(benchmarkState.key.getBytes(), valueBuffer);
  }
}
