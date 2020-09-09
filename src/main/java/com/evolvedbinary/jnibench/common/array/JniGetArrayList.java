package com.evolvedbinary.jnibench.common.array;

import java.util.List;

public class JniGetArrayList implements JniListSupplier<FooObject> {

    @Override
    public List<FooObject> getObjectList(final NativeObjectArray<FooObject> nativeObjectArray) {
        return getArrayList(nativeObjectArray.get_nativeHandle());
    }

    private static native List<FooObject> getArrayList(final long handle);
}
