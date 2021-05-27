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
package com.evolvedbinary.jnibench.jmhbench.cache;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ByteProcessor;

public class NettyByteBufCache extends LinkedListAllocationCache<ByteBuf> {

    PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;

    @Override
    protected long copyIn(ByteBuf byteBuf, byte fillByte) {

        for (int i = 0; i < byteBuf.capacity(); i++) {
            byteBuf.setByte(i, fillByte);
        }
        return fillByte;
    }

    @Override
    ByteBuf allocate(int valueSize) {
        ByteBuf byteBuf = allocator.directBuffer(valueSize);
        assert byteBuf.refCnt() == 1;

        return byteBuf;
    }

    @Override
    void free(ByteBuf byteBuf) {
        byteBuf.release();
    }


    @Override
    public byte[] copyOut(ByteBuf byteBuf) {

        byte[] array = byteArrayOfSize(byteBuf.capacity());
        byteBuf.forEachByte(new ByteCopyProcessor(array));

        return array;
    }

    private static class ByteCopyProcessor implements ByteProcessor {

        final byte[] copy;
        int pos = 0;

        ByteCopyProcessor(byte[] copy) {
            this.copy = copy;
        }

        @Override
        public boolean process(byte b) {
            this.copy[pos++] = b;
            return true;
        }
    }

    private static class ByteSumProcessor implements ByteProcessor {

        int sum = 0;

        @Override
        public boolean process(byte b) {
            sum += b;
            return true;
        }
    }

    @Override
    public int byteChecksum(ByteBuf byteBuf) {

        ByteSumProcessor byteProcessor = new ByteSumProcessor();

        byteBuf.forEachByte(byteProcessor);
        return byteProcessor.sum;
    }

    @Override
    public int longChecksum(ByteBuf byteBuf) {

        //TODO this is probably not optimal, but the checksum stuff is not core
        return byteChecksum(byteBuf);
    }
}

