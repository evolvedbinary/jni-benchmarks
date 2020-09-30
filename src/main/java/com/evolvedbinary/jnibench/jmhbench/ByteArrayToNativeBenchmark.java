package com.evolvedbinary.jnibench.jmhbench;

import com.evolvedbinary.jnibench.common.bytearray.GetByteArray;
import com.evolvedbinary.jnibench.consbench.NarSystem;
import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ByteArrayToNativeBenchmark {

  static {
    NarSystem.loadLibrary();
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
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
  @Measurement(iterations = 100, time = 200, timeUnit = TimeUnit.NANOSECONDS)
  public void basicGetByteArray(ByteArrayFromNativeBenchmark.BenchmarkState benchmarkState) {
    GetByteArray.get(benchmarkState.keyBytes, 0, benchmarkState.keyBytes.length);
  }

}
