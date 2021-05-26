package com.evolvedbinary.jnibench.jmhbench.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ByteProcessor;

public class ByteBufCache extends LinkedListAllocationCache<ByteBuf> {

    PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;

    @Override
    ByteBuf allocate(int valueSize) {
        ByteBuf byteBuf = allocator.directBuffer(valueSize);
        byteBuf.retain();

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

