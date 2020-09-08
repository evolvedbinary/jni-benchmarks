package com.evolvedbinary.jnibench.common.array;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllocateInJavaGetArray implements JniListSupplier<FooObject> {

  @Override
  public List<FooObject> getObjectList(NativeObjectArray<FooObject> nativeObjectArray) {
    final int len = (int) getArraySize(nativeObjectArray.get_nativeHandle());
    if (len == 0) {
      return Collections.emptyList();
    } else {
      final String names[] = new String[len];
      final long values[] = new long[len];

      getArrays(nativeObjectArray.get_nativeHandle(), names, values);

      final List<FooObject> objectList = new ArrayList<>();
      for (int i = 0; i < len; i++) {
        objectList.add(new FooObject(names[i], values[i]));
      }
      return objectList;
    }
  }

  private static native long getArraySize(final long handle);

  private static native void getArrays(final long handle, final String[] paths,
                                       final long[] targetSizes);
}
