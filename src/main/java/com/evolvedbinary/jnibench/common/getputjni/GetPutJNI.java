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
package com.evolvedbinary.jnibench.common.getputjni;

import java.nio.ByteBuffer;

public class GetPutJNI {

    public static native ByteBuffer getIntoDirectByteBufferFromUnsafe(
            final byte[] key,
            final int keyOffset,
            final int keyLength,
            final long bufferHandle,
            final int valueLength);

    public static native int getIntoUnsafe(
            final byte[] key,
            final int keyOffset,
            final int keyLength,
            final long bufferHandle,
            final int valueLength);

    public static native int putFromUnsafe(
            final byte[] key,
            final int keyOffset,
            final int keyLength,
            final long bufferHandle,
            final int valueLength);

    public static native int getIntoDirectByteBuffer(
            final byte[] key,
            final int keyOffset,
            final int keyLength,
            final ByteBuffer value,
            final int valueLength);

    public static native int putFromDirectByteBuffer(
            final byte[] key,
            final int keyOffset,
            final int keyLength,
            final ByteBuffer value,
            final int valueLength);

    public static native int getIntoIndirectByteBufferSetRegion(
            final byte[] key,
            final int keyOffset,
            final int keyLength,
            final ByteBuffer value,
            final int valueLength);

    public static native int putFromIndirectByteBufferGetRegion(
            final byte[] key,
            final int keyOffset,
            final int keyLength,
            final ByteBuffer value,
            final int valueLength);

    public static native int getIntoIndirectByteBufferGetElements(
            final byte[] key,
            final int keyOffset,
            final int keyLength,
            final ByteBuffer value,
            final int valueLength);

    public static native int putFromIndirectByteBufferGetElements(
            final byte[] key,
            final int keyOffset,
            final int keyLength,
            final ByteBuffer value,
            final int valueLength);

    public static native int getIntoIndirectByteBufferGetCritical(
            final byte[] key,
            final int keyOffset,
            final int keyLength,
            final ByteBuffer value,
            final int valueLength);

    public static native int putFromIndirectByteBufferGetCritical(
            final byte[] key,
            final int keyOffset,
            final int keyLength,
            final ByteBuffer value,
            final int valueLength);

    public static native int getIntoByteArraySetRegion(
            final byte[] key,
            final int keyOffset,
            final int keyLength,
            final byte[] value,
            final int valueLength);

    public static native int putFromByteArrayGetRegion(
            final byte[] key,
            final int keyOffset,
            final int keyLength,
            final byte[] value,
            final int valueLength);

    public static native int getIntoByteArrayGetElements(
            final byte[] key,
            final int keyOffset,
            final int keyLength,
            final byte[] value,
            final int valueLength);

    public static native int putFromByteArrayGetElements(
            final byte[] key,
            final int keyOffset,
            final int keyLength,
            final byte[] value,
            final int valueLength);

    public static native int getIntoByteArrayCritical(
            final byte[] key,
            final int keyOffset,
            final int keyLength,
            final byte[] value,
            final int valueLength);

    public static native int putFromByteArrayCritical(
            final byte[] key,
            final int keyOffset,
            final int keyLength,
            final byte[] value,
            final int valueLength);
}
