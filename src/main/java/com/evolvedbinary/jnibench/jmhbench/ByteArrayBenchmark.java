package com.evolvedbinary.jnibench.jmhbench;

import com.evolvedbinary.jnibench.common.bytearray.GetByteArray;
import com.evolvedbinary.jnibench.consbench.NarSystem;
import org.openjdk.jmh.annotations.*;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

// https://www.oracle.com/technical-resources/articles/java/architect-benchmarking.html
public class ByteArrayBenchmark {

  static {
    NarSystem.loadLibrary();
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    private static final Random random = new Random();

    @Param({"38", "128", "512"})
    int keySize;

    @Param({
        "10",
        "50",
        "512",
        "1024",
        "4096",
        "16384"})
    int valueSize;

    String keyBase;
    byte[] keyBytes;

    @Setup
    public void setup() {
      keyBase = "testKeyWithReturnValueSize" + String.format("%07d" , valueSize) + "Bytes";

      keyBytes = new byte[keySize];
      System.arraycopy(keyBase.getBytes(), 0, keyBytes, 0, keyBase.length());
      int randomPartLength = keySize - keyBase.length();
      if (randomPartLength > 0) {
        byte[] randomPart = new byte[randomPartLength];
        random.nextBytes(randomPart);
        System.arraycopy(randomPart, 0, keyBytes, keyBase.length(), randomPartLength);
      }
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
    GetByteArray.get(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public void preallocatedGetByteArray(BenchmarkState benchmarkState) {
    byte[] valueBuffer = new byte[benchmarkState.valueSize];
    GetByteArray.get(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, valueBuffer, 0, benchmarkState.valueSize);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public void bufferGetByteArray(BenchmarkState benchmarkState) {
    GetByteArray.getInBuffer(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public void directBufferGetByteArray(BenchmarkState benchmarkState) {
    GetByteArray.getInDirectBuffer(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public void directBuffersPreallocatedGetByteArray(BenchmarkState benchmarkState) {
    ByteBuffer keyBuffer = ByteBuffer.allocateDirect(benchmarkState.keyBytes.length);
    keyBuffer.put(benchmarkState.keyBytes);
    ByteBuffer valueBuffer = ByteBuffer.allocateDirect(benchmarkState.valueSize);
    GetByteArray.getInDirectBuffer(keyBuffer, 0, benchmarkState.keyBytes.length, valueBuffer, 0, benchmarkState.valueSize);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public void directValueBufferOnlyPreallocatedGetByteArray(BenchmarkState benchmarkState) {
    ByteBuffer valueBuffer = ByteBuffer.allocateDirect(benchmarkState.valueSize);
    GetByteArray.getInDirectBuffer(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length,
        valueBuffer, 0, benchmarkState.valueSize);
  }
//
//  @Benchmark
//  @BenchmarkMode(Mode.SingleShotTime)
//  @OutputTimeUnit(TimeUnit.NANOSECONDS)
//  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
//  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
//  public void valueBufferOnlyPreallocatedGetByteArray(BenchmarkState benchmarkState) {
//    ByteBuffer valueBuffer = ByteBuffer.allocate(benchmarkState.valueSize);
//    GetByteArray.getInBuffer(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length,
//        valueBuffer, 0, benchmarkState.valueSize);
//  }
}
