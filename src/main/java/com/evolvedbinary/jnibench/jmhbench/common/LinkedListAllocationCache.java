/**
 * Copyright © 2021, Evolved Binary Ltd
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
package com.evolvedbinary.jnibench.jmhbench.common;

import org.openjdk.jmh.infra.Blackhole;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public abstract class LinkedListAllocationCache<T> implements AllocationCache<T> {

    private Checksum checksum;
    private Blackhole blackhole;

    @Override
    public final T acquire() {
        return cacheBuffers.removeFirst();
    }

    @Override
    public final void release(T buffer) {
        cacheBuffers.addLast(buffer);
    }

    abstract T allocate(int valueSize);

    abstract void free(T buffer);

    // As many elements of valueSize as fit in cacheSize
    private LinkedList<T> cacheBuffers = new LinkedList<>();

    protected byte[] byteArrayOfSize(int size) {
        return copyOutCache.computeIfAbsent(size, k -> new byte[size]);
    }

    Map<Integer, byte[]> copyOutCache = new HashMap<>();

    public void setup(int valueSize, int valueOverhead, int cacheSize, Checksum checksum, Blackhole blackhole) {
        this.checksum = checksum;
        this.blackhole = blackhole;

        for (int totalBuffers = 0; totalBuffers < cacheSize; totalBuffers += valueSize + valueOverhead)
        {
            cacheBuffers.addLast(allocate(valueSize));
        }
    }

    // Hope we have done enough to free direct BB memory.
    public void tearDown() {

        while (!cacheBuffers.isEmpty()) {
            T buffer = cacheBuffers.removeFirst();
            free(buffer);
        }
    }

    abstract protected int byteChecksum(T item);

    abstract protected int longChecksum(T item);

    abstract protected byte[] copyOut(T item);

    public void checksumBuffer(T item) {
        switch (checksum) {
            case copyout:
                blackhole.consume(this.copyOut(item));
                break;
            case bytesum:
                blackhole.consume(this.byteChecksum(item));
                break;
            case longsum:
                blackhole.consume(this.longChecksum(item));
                break;
            case none:
                break;
        }

    }
}
