package com.evolvedbinary.jnibench.common.array;

import java.util.List;

public interface JniListSupplier<T> {
  List<T> getObjectList(NativeObjectArray<T> nativeObjectArray);
}

