package com.evolvedbinary.jnibench.common.array;

import java.util.ArrayList;
import java.util.List;

public class Jni2DGetArray implements JniListSupplier<FooObject> {
  @Override
  public List<FooObject> getObjectList(NativeObjectArray<FooObject> nativeObjectArray) {
    Object[][] objArr = get2DArray(nativeObjectArray.get_nativeHandle());
    String[] names = (String[]) objArr[0];
    Long[] values = (Long[]) objArr[1];
    List<FooObject> objList = new ArrayList<>();
    for (int i = 0; i < names.length; ++i) {
      objList.add(new FooObject(names[i], values[i]));
    }
    return objList;
  }

  private static native Object[][] get2DArray(final long handle);
}
