package com.evolvedbinary.jnibench.common.array;

public class FooNativeObjectArray extends NativeObjectArray<FooObject> {

  public FooNativeObjectArray(final FooObject[] fooObjects) {
    super(fooObjects);
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
