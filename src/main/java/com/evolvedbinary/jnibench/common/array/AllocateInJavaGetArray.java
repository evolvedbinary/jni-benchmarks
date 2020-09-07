package com.evolvedbinary.jnibench.common.array;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllocateInJavaGetArray implements JniListSupplier<FooObject> {

  @Override
  public List<FooObject> getObjectList(long nativeObjectHandle) {
    final int len = (int) getArraySize(nativeObjectHandle);
    if (len == 0) {
      return Collections.emptyList();
    } else {
      final String names[] = new String[len];
      final long values[] = new long[len];

      getArrays(nativeObjectHandle, names, values);

      final List<FooObject> objectArray = new ArrayList<>();
      for (int i = 0; i < len; i++) {
        objectArray.add(new FooObject(names[i], values[i]));
      }
      return objectArray;
    }
  }

  private static native long getArraySize(final long handle);

  private static native void getArrays(final long handle, final String[] paths,
                                       final long[] targetSizes);
}
