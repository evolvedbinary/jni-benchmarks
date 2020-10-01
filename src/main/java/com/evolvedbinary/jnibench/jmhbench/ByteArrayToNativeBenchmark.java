package com.evolvedbinary.jnibench.jmhbench;

import com.evolvedbinary.jnibench.common.bytearray.GetByteArray;
import com.evolvedbinary.jnibench.consbench.NarSystem;
import org.openjdk.jmh.annotations.*;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark passing byte arrays to native methods and reading them.
 */
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
@Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
public class ByteArrayToNativeBenchmark {

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

    private static final Random random = new Random();

    @Param({"38", "128", "512"})
    int keySize;

    String keyBase;
    byte[] keyBytes;

    @Setup
    public void setup() {
      // Fixed return value
      keyBase = "testKeyWithReturnValueSize0000512Bytes";

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
  public void passKeyAsByteArray(BenchmarkState benchmarkState) {
    GetByteArray.get(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length);
  }

  @Benchmark
  public void passKeyAsByteArrayCritical(BenchmarkState benchmarkState) {
    GetByteArray.getWithCriticalKey(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length);
  }

  @Benchmark
  public void passKeyAsDirectByteBuffer(BenchmarkState benchmarkState) {
    ByteBuffer keyBuffer = ByteBuffer.allocateDirect(benchmarkState.keyBytes.length);
    keyBuffer.put(benchmarkState.keyBytes);
    GetByteArray.getDirectBufferKey(keyBuffer, 0, benchmarkState.keyBytes.length);
  }

  @Benchmark
  public void passKeyAsUnsafeAllocated(BenchmarkState benchmarkState) {
    final long keyArrayHandle = unsafe.allocateMemory(benchmarkState.keySize);
    for (int i = 0; i < benchmarkState.keyBytes.length; ++i) {
      unsafe.putByte(keyArrayHandle + i, benchmarkState.keyBytes[i]);
    }
    GetByteArray.getUnsafeAllocatedKey(keyArrayHandle, 0, benchmarkState.keyBytes.length);
    unsafe.freeMemory(keyArrayHandle);
  }
}
