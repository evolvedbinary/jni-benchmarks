package com.evolvedbinary.jnibench.jmhbench;

import com.evolvedbinary.jnibench.common.bytearray.GetByteArray;
import com.evolvedbinary.jnibench.consbench.NarSystem;
import org.openjdk.jmh.annotations.*;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark getting byte arrays from native methods.
 */
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 100, time = 1000, timeUnit = TimeUnit.NANOSECONDS)
@Measurement(iterations = 500, time = 2000, timeUnit = TimeUnit.NANOSECONDS)
public class ByteArrayFromNativeBenchmark {

  static {
    NarSystem.loadLibrary();
  }

  private static Unsafe unsafe;
  static {
    try {
      Field f = Unsafe.class.getDeclaredField("theUnsafe");
      f.setAccessible(true);
      unsafe = (Unsafe) f.get(null);
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
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

    String keyBase;
    byte[] keyBytes;

    @Setup
    public void setup() {
      keyBase = "testKeyWithReturnValueSize" + String.format("%07d", valueSize) + "Bytes";

      keyBytes = keyBase.getBytes();
    }

    @TearDown
    public void tearDown() {

    }
  }

  @Benchmark
  public void basicGetByteArray(BenchmarkState benchmarkState) {
    GetByteArray.get(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length);
  }

  @Benchmark
  public void preallocatedGetByteArray(BenchmarkState benchmarkState) {
    byte[] valueBuffer = new byte[benchmarkState.valueSize];
    GetByteArray.get(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, valueBuffer, 0, benchmarkState.valueSize);
  }

  @Benchmark
  public void bufferGetByteArray(BenchmarkState benchmarkState) {
    GetByteArray.getInBuffer(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length);
  }

  @Benchmark
  public void directBufferGetByteArray(BenchmarkState benchmarkState) {
    GetByteArray.getInDirectBuffer(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length);
  }

  @Benchmark
  public void directKeyAndValueBuffersPreallocatedGetByteArray(BenchmarkState benchmarkState) {
    ByteBuffer keyBuffer = ByteBuffer.allocateDirect(benchmarkState.keyBytes.length);
    keyBuffer.put(benchmarkState.keyBytes);
    ByteBuffer valueBuffer = ByteBuffer.allocateDirect(benchmarkState.valueSize);
    GetByteArray.getInDirectBuffer(keyBuffer, 0, benchmarkState.keyBytes.length, valueBuffer, 0, benchmarkState.valueSize);
  }

  @Benchmark
  public void directValueBufferOnlyPreallocatedGetByteArray(BenchmarkState benchmarkState) {
    ByteBuffer valueBuffer = ByteBuffer.allocateDirect(benchmarkState.valueSize);
    GetByteArray.getInDirectBuffer(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length,
        valueBuffer, 0, benchmarkState.valueSize);
  }

  @Benchmark
  public void bufferValueOnlyPreallocatedGetByteArray(BenchmarkState benchmarkState) {
    ByteBuffer valueBuffer = ByteBuffer.allocate(benchmarkState.valueSize);
    GetByteArray.getInBuffer(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length,
        valueBuffer, 0, benchmarkState.valueSize);
  }

  @Benchmark
  public void preallocatedGetByteArrayWithGetPrimitiveArrayCritical(BenchmarkState benchmarkState) {
    byte[] valueBuffer = new byte[benchmarkState.valueSize];
    GetByteArray.getCritical(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, valueBuffer, 0, benchmarkState.valueSize);
  }

  @Benchmark
  public void unsafeAllocatedGetByteArray(BenchmarkState benchmarkState) {
    long arrayHandle = unsafe.allocateMemory(benchmarkState.valueSize);
    GetByteArray.getUnsafe(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length, arrayHandle, 0, benchmarkState.valueSize);
    // Access through getByte with address offset
    //System.out.println("First byte: " + benchmarkState.unsafe.getByte(arrayHandle));
    unsafe.freeMemory(arrayHandle);
  }

}
