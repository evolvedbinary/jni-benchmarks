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
