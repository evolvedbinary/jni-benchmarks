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
package com.evolvedbinary.jnibench.common.bytearray;

import java.nio.ByteBuffer;

public class GetByteArray {

  /**
   * User allocates and provides key in Java.
   * We allocate and fill byte[] value in C++ and return it.
   *
   * @param key
   * @param keyOffset
   * @param keyLength
   * @return
   */
  public static native byte[] get(final byte[] key, final int keyOffset,
                                  final int keyLength);

  /**
   * User allocates and provides key in Java using direct {@link ByteBuffer}.
   * We allocate and fill byte[] value in C++ and return it.
   *
   * @param key
   * @param keyOffset
   * @param keyLength
   * @return
   */
  public static native byte[] getDirectBufferKey(final ByteBuffer key, final int keyOffset,
                                  final int keyLength);

  /**
   * User allocates and provides key in Java using {@link sun.misc.Unsafe}.
   * We allocate and fill byte[] value in C++ and return it.
   *
   * @param keyByteArrayHandle
   * @param keyOffset
   * @param keyLength
   * @return
   */
  public static native byte[] getUnsafeAllocatedKey(final long keyByteArrayHandle, final int keyOffset,
                                  final int keyLength);

  /**
   * User allocates and provides key and value byte[]s in Java.
   * We fill value in C++ up to value#length and return both it and the total length of the available value in the db.
   *
   * @param key
   * @param keyOffset
   * @param keyLength
   * @param value
   * @param valueOffset
   * @param valueLength
   * @return
   */
  public static native int get(final byte[] key,
                               final int keyOffset, final int keyLength, final byte[] value,
                               final int valueOffset, final int valueLength);

  /**
   * Same as {@link GetByteArray#get(byte[], int, int)} but instead of returning a byte[]
   * we allocate and return a Direct Byte Buffer.
   *
   * @param key
   * @param keyOffset
   * @param keyLength
   * @return
   */
  public static native ByteBuffer getInDirectBuffer(final byte[] key, final int keyOffset,
                                                    final int keyLength);

  /**
   * Same as {@link GetByteArray#getInDirectBuffer(byte[], int, int)} but returning a non-direct byte buffer.
   *
   * @param key
   * @param keyOffset
   * @param keyLength
   * @return
   */
  public static native ByteBuffer getInBuffer(final byte[] key, final int keyOffset,
                                              final int keyLength);

  /**
   * User allocates and provides key byte array and value ByteBuffer instance (non-direct) in Java.
   * We fill value in C++ up to value#capacity and return both it and the total length of the available value in the db.
   *
   * @param key
   * @param keyOffset
   * @param keyLength
   * @param value
   * @param valueOffset
   * @param valueLength
   * @return
   */
  public static native int getInBuffer(final byte[] key,
                                       final int keyOffset, final int keyLength, final ByteBuffer value,
                                       final int valueOffset, final int valueLength);

  /**
   * User allocates and provides key byte array and value Direct Byte Buffer instance in Java.
   * We fill value in C++ up to value#capacity and return both it and the total length of the available value in the db.
   *
   * @param key
   * @param keyOffset
   * @param keyLength
   * @param value
   * @param valueOffset
   * @param valueLength
   * @return
   */
  public static native int getInDirectBuffer(final byte[] key,
                                       final int keyOffset, final int keyLength, final ByteBuffer value,
                                       final int valueOffset, final int valueLength);

  /**
   * User allocates and provides key and value direct {@link ByteBuffer} in Java.
   * We fill value in C++ up to value#capacity and return both it and the total length of the available value in the db.
   *
   * @param key
   * @param keyOffset
   * @param keyLength
   * @param value
   * @param valueOffset
   * @param valueLength
   * @return
   */
  public static native int getInDirectBuffer(final ByteBuffer key,
                                             final int keyOffset, final int keyLength, final ByteBuffer value,
                                             final int valueOffset, final int valueLength);

  /**
   * User allocates and provides key in Java.
   * We allocate and fill byte[] value in C++ and return it.
   * On native side GetPrimitiveArrayCritical function is used to read key.
   *
   * @param key
   * @param keyOffset
   * @param keyLength
   * @return
   */
  public static native byte[] getWithCriticalKey(final byte[] key, final int keyOffset,
                                  final int keyLength);

  /**
   * User allocates and provides key and value byte[]s in Java.
   * We fill value in C++ up to value#length and return both it and the total length of the available value in the db.
   * On native side GetPrimitiveArrayCritical function is used to read key and write value.
   *
   * @param key
   * @param keyOffset
   * @param keyLength
   * @param value
   * @param valueOffset
   * @param valueLength
   * @return
   */
  public static native int getCritical(final byte[] key,
                               final int keyOffset, final int keyLength, final byte[] value,
                               final int valueOffset, final int valueLength);

  /**
   * User allocates and provides key byte[] in Java.
   * User also allocates memory for value byte array using {@link sun.misc.Unsafe}.
   * We fill value in C++ up to valueLength and return it.
   *
   * @param key
   * @param keyOffset
   * @param keyLength
   * @param valueArrayHandle
   * @param valueOffset
   * @param valueLength
   * @return
   */
  public static native int getUnsafe(final byte[] key,
                                       final int keyOffset, final int keyLength, final long valueArrayHandle,
                                       final int valueOffset, final int valueLength);
}
