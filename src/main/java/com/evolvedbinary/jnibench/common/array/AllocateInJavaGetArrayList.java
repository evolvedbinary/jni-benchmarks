package com.evolvedbinary.jnibench.common.array;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllocateInJavaGetArrayList implements JniListSupplier<FooObject> {

  @Override
  public List<FooObject> getObjectList(final NativeObjectArray<FooObject> nativeObjectArray) {
    final int len = (int) getListSize(nativeObjectArray.get_nativeHandle());
    if (len == 0) {
      return Collections.emptyList();
    } else {
      final List<FooObject> objectList = new ArrayList<>(len);

      getList(nativeObjectArray.get_nativeHandle(), objectList);

      return objectList;
    }
  }

  private static native long getListSize(final long handle);

  private static native void getList(final long handle, final List<FooObject> list);
}
