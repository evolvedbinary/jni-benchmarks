package com.evolvedbinary.jnibench.common.array;

import java.util.Collection;

public class FooNativeObjectArray extends NativeObjectArray<FooObject> {

  public FooNativeObjectArray() {
    super(new FooObject[0]);
  }

  public FooNativeObjectArray(final Collection<FooObject> fooObjects) {
    super(fooObjects.toArray(new FooObject[0]));
  }

  @Override
  protected long newObjectArray(final FooObject[] objectArray) {
    return newFooObjectArray(objectArray);
  }

  @Override
  protected void disposeInternal() {
    disposeInternal(_nativeHandle);
  }

  private static native long newFooObjectArray(final Object[] fooObjects);

  private native void disposeInternal(final long handle);
}
