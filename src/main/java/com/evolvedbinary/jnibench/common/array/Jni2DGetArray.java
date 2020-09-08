package com.evolvedbinary.jnibench.common.array;

import java.util.List;

public class Jni2DGetArray implements JniListSupplier<FooObject> {
  @Override
  public List<FooObject> getObjectList(NativeObjectArray<FooObject> nativeObjectArray) {
    // TODO
    return null;
  }

  private static native Object[][] get2DArray(final long handle);
}
