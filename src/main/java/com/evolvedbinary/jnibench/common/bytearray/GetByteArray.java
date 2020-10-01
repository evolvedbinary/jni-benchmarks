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
