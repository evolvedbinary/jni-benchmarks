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
import java.util.function.Function;

public class FFMHelper {

        public static MethodHandle getIntoMemorySegment(Linker linker, SymbolLookup loaderLookup,
                        FFMMethodOption fMethodOption) {
                FunctionDescriptor fd = FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS,
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS,
                                ValueLayout.JAVA_INT);
                Function<MemorySegment, MethodHandle> mapSymbol = switch (fMethodOption) {
                        case NON_CRITICAL -> symbol -> linker.downcallHandle(symbol, fd);
                        case CRITICAL_AS_OPTIMIZATION -> symbol -> linker.downcallHandle(symbol, fd, Linker.Option.critical(false));
                        case ALLOW_HEAP -> symbol -> linker.downcallHandle(symbol, fd, Linker.Option.critical(true));
                };

                return loaderLookup.find("getIntoMemorySegment")
                                .map(mapSymbol)
                                .orElseThrow();
        }

        public static MethodHandle putFromMemorySegment(Linker linker, SymbolLookup loaderLookup, FFMMethodOption fMethodOption) {
                 FunctionDescriptor fd = FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS,
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS,
                                ValueLayout.JAVA_INT);
                Function<MemorySegment, MethodHandle> mapSymbol = switch (fMethodOption) {
                        case NON_CRITICAL -> symbol -> linker.downcallHandle(symbol, fd);
                        case CRITICAL_AS_OPTIMIZATION -> symbol -> linker.downcallHandle(symbol, fd, Linker.Option.critical(false));
                        case ALLOW_HEAP -> symbol -> linker.downcallHandle(symbol, fd, Linker.Option.critical(true));
                };

                return loaderLookup.find("putFromMemorySegment")
                                .map(mapSymbol)
                                .orElseThrow();
        }

        public static MemorySegment allocateFromArena(Arena arena, ValueLayout.OfByte valueLayout, byte[] bytes) {
                return arena.allocateFrom(valueLayout, bytes);
        }
}
