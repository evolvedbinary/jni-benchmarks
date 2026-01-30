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
package com.evolvedbinary.jnibench.jmhbench.cache;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.stream.IntStream;

public class MemorySegmentCache extends LinkedListAllocationCache<MemorySegment> {
  private final Arena arena;

  public MemorySegmentCache() {
    arena = Arena.ofConfined();
  }


  @Override
  MemorySegment allocate(final int valueSize) {
    return arena.allocate(valueSize);
  }

  @Override
  void free(final MemorySegment buffer) {
    // Nothing to do here, as we override taerdown() directly.
  }

  @Override
  public void tearDown() {
    super.tearDown();
    arena.close();
  }

  @Override
  protected int byteChecksum(final MemorySegment item) {
    return IntStream.range(0, (int) item.byteSize()).map(offset -> item.get(JAVA_BYTE, offset)).sum();
  }

  @Override
  protected int longChecksum(final MemorySegment item) {
    return byteChecksum(item);
  }

  @Override
  protected byte[] copyOut(final MemorySegment item) {
    // Get a cached byte array of the correct size
    byte[] array = byteArrayOfSize((int) item.byteSize());

    // Perform bulk copy from native memory to Java heap array
    MemorySegment.copy(item, ValueLayout.JAVA_BYTE, 0, array, 0, (int) item.byteSize());

    return array;
  }

  @Override
  protected long copyIn(final MemorySegment item, final byte fillByte) {
    // Highly optimized bulk fill (native memset equivalent)
    item.fill(fillByte);

    return fillByte;
  }
}
