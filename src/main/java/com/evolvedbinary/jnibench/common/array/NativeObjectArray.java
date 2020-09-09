package com.evolvedbinary.jnibench.common.array;

import com.evolvedbinary.jnibench.common.NativeBackedObject;

public abstract class NativeObjectArray<T> extends NativeBackedObject {
  NativeObjectArray(T[] objects) {
    _nativeHandle = newObjectArray(objects);
  }

  protected abstract long newObjectArray(final T[] objectArray);
}
