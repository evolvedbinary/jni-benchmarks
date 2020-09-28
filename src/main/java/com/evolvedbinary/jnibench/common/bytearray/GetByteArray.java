package com.evolvedbinary.jnibench.common.bytearray;

public class GetByteArray {

  public static native byte[] get(final byte[] key, final int keyOffset,
                                  final int keyLength);

  public static int get(final byte[] key, final byte[] value) {
    return get(key, 0, key.length, value, 0, value.length);
  }

  private static native int get(final byte[] key,
                               final int keyOffset, final int keyLength, final byte[] value,
                               final int valueOffset, final int valueLength);
}
