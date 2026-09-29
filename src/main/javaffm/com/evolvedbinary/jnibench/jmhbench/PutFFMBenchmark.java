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

import com.evolvedbinary.jnibench.jmhbench.cache.AllocationCache;
import com.evolvedbinary.jnibench.jmhbench.cache.ByteArrayCache;
import com.evolvedbinary.jnibench.jmhbench.PutFFMBenchmark.PutFFMBenchmarkState;
import com.evolvedbinary.jnibench.jmhbench.PutFFMBenchmark.PutFFMThreadState;
import com.evolvedbinary.jnibench.jmhbench.PutNativeBenchmarkBase.PutNativeBenchmarkState;
import com.evolvedbinary.jnibench.jmhbench.cache.MemorySegmentCache;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.List;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

public class PutFFMBenchmark extends PutNativeBenchmarkBase {

  private final static List<String> supportedBenchmarks =
  List.of("putFromMemorySegmentOfArena", "putFromMemorySegmentWrappingArray", "putFromMemorySegmentMarkedCritical");

  private static final MethodHandle PUT_FROM_MEMORY_SEGMENT_HANDLE;
  private static final MethodHandle PUT_FROM_MEMORY_SEGMENT_HANDLE_CRITICAL;
  private static final MethodHandle PUT_FROM_MEMORY_SEGMENT_HANDLE_ALLOW_HEAP;

  static {
    // 1. Initialize the Linker and Lookup
    Linker linker = Linker.nativeLinker();
    SymbolLookup loaderLookup = SymbolLookup.loaderLookup();

    // 2. Find the symbol and create the Downcall Handle once
    PUT_FROM_MEMORY_SEGMENT_HANDLE = FFMHelper.putFromMemorySegment(linker, loaderLookup, FFMMethodOption.NON_CRITICAL);
    PUT_FROM_MEMORY_SEGMENT_HANDLE_CRITICAL = FFMHelper.putFromMemorySegment(linker, loaderLookup, FFMMethodOption.CRITICAL_AS_OPTIMIZATION);
    PUT_FROM_MEMORY_SEGMENT_HANDLE_ALLOW_HEAP = FFMHelper.putFromMemorySegment(linker, loaderLookup, FFMMethodOption.ALLOW_HEAP);
  }

  @State(Scope.Benchmark)
  public static class PutFFMBenchmarkState extends PutNativeBenchmarkState {

  }

  @State(Scope.Thread)
  public static class PutFFMThreadState {

    private Arena keyArena;
    private MemorySegment keyMemorySegment;

    private final MemorySegmentCache memorySegmentCache = new MemorySegmentCache();

    private final ByteArrayCache putSourceCache = new ByteArrayCache();

    @Setup
    public void setup(final PutFFMBenchmarkState benchmarkState, final Blackhole blackhole) {

      int cacheSize = benchmarkState.cacheMB * PutNativeBenchmarkState.MB;

      if (isPutFromMemorySegment(benchmarkState)) {
        memorySegmentCache.setup(benchmarkState.valueSize, cacheSize, benchmarkState.cacheEntryOverhead,
                                 benchmarkState.writePreparation, blackhole);
                                 System.err.println("Setup cache size " + cacheSize);
        putSourceCache.setup(benchmarkState.valueSize, cacheSize, benchmarkState.cacheEntryOverhead,
                                 AllocationCache.Prepare.none, blackhole);
      } else {
        throw new RuntimeException(
                "Don't know how to setup() for benchmark: " + benchmarkState.caller.benchmarkMethod);
      }

      keyArena = Arena.ofConfined();
      keyMemorySegment = FFMHelper.allocateFromArena(keyArena, ValueLayout.JAVA_BYTE, benchmarkState.keyBytes);
    }

    private static boolean isPutFromMemorySegment(final PutFFMBenchmarkState benchmarkState) {
      return supportedBenchmarks.contains(benchmarkState.getCaller().benchmarkMethod);
    }

    @TearDown
    public void tearDown(final PutFFMBenchmarkState benchmarkState) {
      if (isPutFromMemorySegment(benchmarkState)) {
        memorySegmentCache.tearDown();
        putSourceCache.tearDown();

        if (keyArena != null) {
          keyArena.close();
        }
      } else {
        throw new RuntimeException(
                "Don't know how to tearDown() for benchmark: " + benchmarkState.caller.benchmarkMethod);
      }
    }
  }

  @Benchmark
  public void putFromMemorySegmentOfArena(PutFFMBenchmarkState benchmarkState, PutFFMThreadState threadState,
                                   Blackhole blackhole) {
    final var segment = threadState.memorySegmentCache.acquire();
    threadState.memorySegmentCache.prepareBuffer(segment, threadState.putSourceCache);

    try {
      final var size = (int) PUT_FROM_MEMORY_SEGMENT_HANDLE.invokeExact(
          threadState.keyMemorySegment, // Pre-allocated segment for key
          benchmarkState.keyBytes.length,
          segment,
          benchmarkState.valueSize
      );
      blackhole.consume(size);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }

    threadState.memorySegmentCache.release(segment);
  }

  @Benchmark
  public void putFromMemorySegmentMarkedCritical(PutFFMBenchmarkState benchmarkState, PutFFMThreadState threadState,
                                   Blackhole blackhole) {
    final var segment = threadState.memorySegmentCache.acquire();
    threadState.memorySegmentCache.prepareBuffer(segment, threadState.putSourceCache);

    try {
      final var size = (int) PUT_FROM_MEMORY_SEGMENT_HANDLE_CRITICAL.invokeExact(
          threadState.keyMemorySegment, // Pre-allocated segment for key
          benchmarkState.keyBytes.length,
          segment,
          benchmarkState.valueSize
      );
      blackhole.consume(size);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }

    threadState.memorySegmentCache.release(segment);
  }

  @Benchmark
  public void putFromMemorySegmentWrappingArray(
    PutFFMBenchmarkState benchmarkState,
    PutFFMThreadState threadState,
    Blackhole blackhole) {

    final var bytes = threadState.putSourceCache.acquire();
    final MemorySegment bytesAsSegment = MemorySegment.ofArray(bytes);
    
    try {
      final var size = (int) PUT_FROM_MEMORY_SEGMENT_HANDLE_ALLOW_HEAP.invokeExact(
          threadState.keyMemorySegment, // Pre-allocated segment for key
          benchmarkState.keyBytes.length,
          bytesAsSegment,
          benchmarkState.valueSize
      );
      blackhole.consume(size);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }

    threadState.putSourceCache.release(bytes);
  }
}
