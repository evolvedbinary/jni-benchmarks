package com.evolvedbinary.jnibench.jmhbench;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.foreign.MemorySegment;
import java.util.Optional;

public class FFMHelper {
    
    public static MethodHandle getIntoMemorySegment(Linker linker, SymbolLookup loaderLookup) {
        return loaderLookup.find("getIntoMemorySegment")
                                                 .map(symbol -> linker.downcallHandle(symbol,
                                                                                      FunctionDescriptor.of(
                                                                                          ValueLayout.JAVA_INT,
                                                                                          ValueLayout.ADDRESS,
                                                                                          ValueLayout.JAVA_INT,
                                                                                          ValueLayout.ADDRESS,
                                                                                          ValueLayout.JAVA_INT)))
                                                 .orElseThrow();
    }

    public static MemorySegment allocateFromArena(Arena arena, ValueLayout.OfByte valueLayout, byte[] bytes) {
        return arena.allocateArray(valueLayout, bytes);
    }
}