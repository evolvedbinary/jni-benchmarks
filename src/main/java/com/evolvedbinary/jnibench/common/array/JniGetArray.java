package com.evolvedbinary.jnibench.common.array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JniGetArray implements JniListSupplier<FooObject> {

  @Override
  public List<FooObject> getObjectList(NativeObjectArray<FooObject> nativeObjectArray) {
    return new ArrayList<>(Arrays.asList(getArray(nativeObjectArray.get_nativeHandle())));
  }

  private static native FooObject[] getArray(final long handle);
}
