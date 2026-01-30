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

import com.evolvedbinary.jnibench.jmhbench.cache.MemorySegmentCache;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

public class GetJNIBenchmarkJava25 extends GetJNIBenchmark {
  private static final MethodHandle GET_INTO_MEMORY_SEGMENT_HANDLE;

  static {
    // 1. Initialize the Linker and Lookup
    Linker linker = Linker.nativeLinker();
    SymbolLookup loaderLookup = SymbolLookup.loaderLookup();

    // 2. Find the symbol and create the Downcall Handle once
    GET_INTO_MEMORY_SEGMENT_HANDLE = loaderLookup.find("getIntoMemorySegment")
                                                 .map(symbol -> linker.downcallHandle(symbol,
                                                                                      FunctionDescriptor.of(
                                                                                          ValueLayout.JAVA_INT,
                                                                                          ValueLayout.ADDRESS,
                                                                                          ValueLayout.JAVA_INT,
                                                                                          ValueLayout.ADDRESS,
                                                                                          ValueLayout.JAVA_INT),
                                                                                      Linker.Option.critical(false)))
                                                 .orElseThrow();

  }

  @State(Scope.Benchmark)
  public static class GetJNIBenchmarkStateJava25 extends GetJNIBenchmarkState {
    private Arena arena;
    private MemorySegment keyMemorySegment;

    @Setup
    public void setupJava25() {
      super.setup();
      arena = Arena.ofShared();
      keyMemorySegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, keyBytes);

    }

    @TearDown
    public void tearDown() {
      if (arena != null) {
        arena.close();
      }
    }
  }

  @State(Scope.Thread)
  public static class GetJNIThreadStateJava25 extends GetJNIThreadState {
    private MemorySegmentCache memorySegmentCache = new MemorySegmentCache();

    @Setup
    public void setup(final GetJNIBenchmarkStateJava25 benchmarkState, final Blackhole blackhole) {
      if ("getIntoMemorySegment".equals(benchmarkState.getCaller().benchmarkMethod)) {
        memorySegmentCache.setup(benchmarkState.valueSize, benchmarkState.cacheMB * GetJNIBenchmarkState.MB,
                                 benchmarkState.cacheEntryOverhead, benchmarkState.readChecksum, blackhole);
      } else {
        super.setup(benchmarkState, blackhole);
      }
    }

    @TearDown
    public void tearDown(final GetJNIBenchmarkStateJava25 benchmarkState) {
      if ("getIntoMemorySegment".equals(benchmarkState.getCaller().benchmarkMethod)) {
        memorySegmentCache.tearDown();
      } else {
        super.tearDown(benchmarkState);
      }
    }
  }

  @Benchmark
  public void getIntoMemorySegment(GetJNIBenchmarkStateJava25 benchmarkState, GetJNIThreadStateJava25 threadState,
                                   Blackhole blackhole) {
    final var segment = threadState.memorySegmentCache.acquire();

    try {
      final var size = (int) GET_INTO_MEMORY_SEGMENT_HANDLE.invokeExact(
          benchmarkState.keyMemorySegment, // Pre-allocated segment for key
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
}
