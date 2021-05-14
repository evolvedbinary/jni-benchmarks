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

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeBufferCache extends LinkedListAllocationCache<UnsafeBufferCache.UnsafeBuffer>  {

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

    @Override
    public int byteChecksum(UnsafeBuffer item) {
        int sum = 0;
        long handle = item.handle;
        for (int i = 0; i < item.size; i++) {
            sum += unsafe.getByte(handle++);
        }
        return sum;
    }

    @Override
    public int longChecksum(UnsafeBuffer item) {
        long sum = 0;
        long handle = item.handle;
        for (int i = 0; i < item.size; i += Long.BYTES) {
            sum += unsafe.getLong(handle);
            handle += Long.BYTES;
        }
        return (int)sum;
    }

    public static class UnsafeBuffer {

        public long handle;
        public long size;

        public UnsafeBuffer(long handle, long size) {
            this.handle = handle;
            this.size = size;
        }

        public void reset() {
            this.handle = 0;
            this.size = 0;
        }
    }

    @Override
    UnsafeBuffer allocate(int bytes) {
        long handle = unsafe.allocateMemory(bytes);
        return new UnsafeBuffer(handle, bytes);
    }

    @Override
    void free(UnsafeBuffer buffer) {
        if (buffer.handle != 0 && buffer.size > 0) {
            unsafe.freeMemory(buffer.handle);
            buffer.reset();
        }
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
