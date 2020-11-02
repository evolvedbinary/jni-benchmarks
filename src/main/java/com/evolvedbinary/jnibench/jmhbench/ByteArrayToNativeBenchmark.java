/**
 * Copyright © 2016, Evolved Binary Ltd
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
@Warmup(iterations = 100, time = 1000, timeUnit = TimeUnit.NANOSECONDS)
@Measurement(iterations = 500, time = 2000, timeUnit = TimeUnit.NANOSECONDS)
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

    ByteBuffer keyBuffer;
    long unsafeKeyHandle;

    @Setup
    public void setup() {
      // Fixed return value
      keyBase = "testKeyWithReturnValueSize0000512Bytes";

      // Allocate and generate byte array with given size
      keyBytes = new byte[keySize];
      System.arraycopy(keyBase.getBytes(), 0, keyBytes, 0, keyBase.length());
      int randomPartLength = keySize - keyBase.length();
      if (randomPartLength > 0) {
        byte[] randomPart = new byte[randomPartLength];
        random.nextBytes(randomPart);
        System.arraycopy(randomPart, 0, keyBytes, keyBase.length(), randomPartLength);
      }

      // Byte array as direct byte buffer
      keyBuffer = ByteBuffer.allocateDirect(keyBytes.length);
      keyBuffer.put(keyBytes);

      // Byte array allocated with Unsafe
      unsafeKeyHandle = unsafe.allocateMemory(keySize);
      for (int i = 0; i < keyBytes.length; ++i) {
        unsafe.putByte(unsafeKeyHandle + i, keyBytes[i]);
      }
    }

    @TearDown
    public void tearDown() {
      unsafe.freeMemory(unsafeKeyHandle);
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
    GetByteArray.getDirectBufferKey(benchmarkState.keyBuffer, 0, benchmarkState.keyBytes.length);
  }

  @Benchmark
  public void passKeyAsDirectByteBufferWithAllocate(BenchmarkState benchmarkState) {
    ByteBuffer keyBuffer = ByteBuffer.allocateDirect(benchmarkState.keyBytes.length);
    keyBuffer.put(benchmarkState.keyBytes);
    GetByteArray.getDirectBufferKey(keyBuffer, 0, benchmarkState.keyBytes.length);
  }

  @Benchmark
  public void passKeyAsUnsafe(BenchmarkState benchmarkState) {
    GetByteArray.getUnsafeAllocatedKey(benchmarkState.unsafeKeyHandle, 0, benchmarkState.keyBytes.length);
  }

  @Benchmark
  public void passKeyAsUnsafeWithAllocate(BenchmarkState benchmarkState) {
    final long keyArrayHandle = unsafe.allocateMemory(benchmarkState.keySize);
    for (int i = 0; i < benchmarkState.keyBytes.length; ++i) {
      unsafe.putByte(keyArrayHandle + i, benchmarkState.keyBytes[i]);
    }
    GetByteArray.getUnsafeAllocatedKey(keyArrayHandle, 0, benchmarkState.keyBytes.length);
    unsafe.freeMemory(keyArrayHandle);
  }
}
