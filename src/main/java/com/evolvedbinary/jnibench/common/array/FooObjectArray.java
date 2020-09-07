package com.evolvedbinary.jnibench.common.array;

import com.evolvedbinary.jnibench.common.NativeBackedObject;

import java.util.Collection;
import java.util.List;

public class FooObjectArray extends NativeBackedObject {

  private JniListSupplier<FooObject> fooObjectsJniListSupplier;

  public FooObjectArray(Collection<FooObject> fooObjects,
                        JniListSupplier<FooObject> fooObjectsJniListSupplier) {
    super();
    _nativeHandle = newFooObjectArray(fooObjects.toArray());
    this.fooObjectsJniListSupplier = fooObjectsJniListSupplier;
  }

  public void setFooObjectsJniListSupplier(JniListSupplier<FooObject> fooObjectsJniListSupplier) {
    this.fooObjectsJniListSupplier = fooObjectsJniListSupplier;
  }

  public List<FooObject> getFooObjects() {
    return fooObjectsJniListSupplier.getObjectList(_nativeHandle);
  }

  @Override
  protected void disposeInternal() {
    disposeInternal(_nativeHandle);
  }

  private static native long newFooObjectArray(Object[] fooObjects);

  private native void disposeInternal(final long handle);
}
