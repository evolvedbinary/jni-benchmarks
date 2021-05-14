package com.evolvedbinary.jnibench.jmhbench.common;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

public class ByteArrayCache extends LinkedListAllocationCache<byte[]>  {

    @Override
    byte[] allocate(int valueSize) {
        return new byte[valueSize];
    }

    @Override
    void free(byte[] buffer) {
        //nothing to do
    }


    @Override
    public byte[] copyOut(byte[] item) {

        byte[] array = byteArrayOfSize(item.length);
        ByteBuffer target = ByteBuffer.wrap(array);
        target.put(item);

        return array;
    }

    @Override
    public int byteChecksum(byte[] item) {

        int sum = 0;
        for (int i = 0; i < item.length; i++) {
            sum += item[i];
        }

        return sum;
    }

    @Override
    public int longChecksum(byte[] item) {
        LongBuffer buffer = ByteBuffer.wrap(item).asLongBuffer();
        long sum = 0;
        while (buffer.remaining() > 0) {
            sum += buffer.get();
        }
        return (int)sum;
    }
}

