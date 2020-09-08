package com.evolvedbinary.jnibench.common.array;

import java.util.Collection;
import java.util.Collections;

public class FooNativeObjectArray extends NativeObjectArray<FooObject> {

  public FooNativeObjectArray() {
    super(new FooObject[0]);
  }

  public FooNativeObjectArray(Collection<FooObject> fooObjects) {
    super(fooObjects.toArray(new FooObject[0]));
  }

  @Override
  protected long newObjectArray(FooObject[] objectArray) {
    return newFooObjectArray(objectArray);
  }

  @Override
  protected void disposeInternal() {
    disposeInternal(_nativeHandle);
  }

  private static native long newFooObjectArray(Object[] fooObjects);

  private native void disposeInternal(final long handle);
}
