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

import com.evolvedbinary.jnibench.jmhbench.GetFFMBenchmark.GetFFMBenchmarkStateJava;
import com.evolvedbinary.jnibench.jmhbench.GetFFMBenchmark.GetFFMThreadStateJava;
import com.evolvedbinary.jnibench.jmhbench.GetNativeBenchmarkBase.GetNativeBenchmarkState;
import com.evolvedbinary.jnibench.jmhbench.cache.AllocationCache;
import com.evolvedbinary.jnibench.jmhbench.cache.ByteArrayCache;
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

public class GetFFMBenchmark extends GetNativeBenchmarkBase {
  private static final MethodHandle GET_INTO_MEMORY_SEGMENT_HANDLE;
  private static final MethodHandle GET_INTO_MEMORY_SEGMENT_HANDLE_CRITICAL;

  static {
    // 1. Initialize the Linker and Lookup
    Linker linker = Linker.nativeLinker();
    SymbolLookup loaderLookup = SymbolLookup.loaderLookup();

    // 2. Find the symbol and create the Downcall Handle once
    GET_INTO_MEMORY_SEGMENT_HANDLE = FFMHelper.getIntoMemorySegment(linker, loaderLookup, false /*critical */);
    GET_INTO_MEMORY_SEGMENT_HANDLE_CRITICAL = FFMHelper.getIntoMemorySegment(linker, loaderLookup, true /*critical */);
  }

  @State(Scope.Benchmark)
  public static class GetFFMBenchmarkStateJava extends GetNativeBenchmarkState {

  }

  @State(Scope.Thread)
  public static class GetFFMThreadStateJava {

    private final static List<String> supportedBenchmarks =
    List.of("getIntoMemorySegmentOfArena", "getIntoMemorySegmentWrappingArray", "getIntoMemorySegmentMarkedCritical");

    private Arena keyArena;
    private MemorySegment keyMemorySegment;

    private MemorySegmentCache memorySegmentCache = new MemorySegmentCache();
    private final ByteArrayCache byteArrayCache = new ByteArrayCache();

    @Setup
    public void setup(final GetFFMBenchmarkStateJava benchmarkState, final Blackhole blackhole) {
      if (supportedBenchmarks.contains(benchmarkState.getCaller().benchmarkMethod)) {
        memorySegmentCache.setup(benchmarkState.valueSize, benchmarkState.cacheMB * GetNativeBenchmarkState.MB,
                                 benchmarkState.cacheEntryOverhead, benchmarkState.readChecksum, blackhole);
        byteArrayCache.setup(benchmarkState.valueSize, benchmarkState.cacheMB * GetNativeBenchmarkState.MB, benchmarkState.cacheEntryOverhead,
                                 AllocationCache.Prepare.none, blackhole);
      } else {
        throw new RuntimeException(
                "Don't know how to setup() for benchmark: " + benchmarkState.caller.benchmarkMethod);
      }

      keyArena = Arena.ofConfined();
      keyMemorySegment = FFMHelper.allocateFromArena(keyArena, ValueLayout.JAVA_BYTE, benchmarkState.keyBytes);
    }

    @TearDown
    public void tearDown(final GetFFMBenchmarkStateJava benchmarkState) {
      if (supportedBenchmarks.contains(benchmarkState.getCaller().benchmarkMethod)) {
        memorySegmentCache.tearDown();
        byteArrayCache.tearDown();

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
  public void getIntoMemorySegmentOfArena(GetFFMBenchmarkStateJava benchmarkState, GetFFMThreadStateJava threadState,
                                   Blackhole blackhole) {
    final var segment = threadState.memorySegmentCache.acquire();

    try {
      final var size = (int) GET_INTO_MEMORY_SEGMENT_HANDLE.invokeExact(
          threadState.keyMemorySegment, // Pre-allocated segment for key
          benchmarkState.keyBytes.length,
          segment,
          benchmarkState.valueSize
      );
      blackhole.consume(size);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }

    threadState.memorySegmentCache.checksumBuffer(segment);
    threadState.memorySegmentCache.release(segment);
  }

  @Benchmark
  public void getIntoMemorySegmentMarkedCritical(GetFFMBenchmarkStateJava benchmarkState, GetFFMThreadStateJava threadState,
                                   Blackhole blackhole) {
    final var segment = threadState.memorySegmentCache.acquire();

    try {
      final var size = (int) GET_INTO_MEMORY_SEGMENT_HANDLE_CRITICAL.invokeExact(
          threadState.keyMemorySegment, // Pre-allocated segment for key
          benchmarkState.keyBytes.length,
          segment,
          benchmarkState.valueSize
      );
      blackhole.consume(size);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }

    threadState.memorySegmentCache.checksumBuffer(segment);
    threadState.memorySegmentCache.release(segment);
  }

  @Benchmark
  public void getIntoMemorySegmentWrappingArray(GetFFMBenchmarkStateJava benchmarkState, GetFFMThreadStateJava threadState,
                                   Blackhole blackhole) {
    final var bytes = threadState.byteArrayCache.acquire();
    final MemorySegment bytesAsSegment = MemorySegment.ofArray(bytes);

    try {
      final var size = (int) GET_INTO_MEMORY_SEGMENT_HANDLE_CRITICAL.invokeExact(
          threadState.keyMemorySegment, // Pre-allocated segment for key
          benchmarkState.keyBytes.length,
          bytesAsSegment,
          benchmarkState.valueSize
      );
      blackhole.consume(size);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }

    threadState.byteArrayCache.checksumBuffer(bytes);
    threadState.byteArrayCache.release(bytes);
  }
}
