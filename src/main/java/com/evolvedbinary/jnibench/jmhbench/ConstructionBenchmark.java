package com.evolvedbinary.jnibench.jmhbench;

import com.evolvedbinary.jnibench.common.call.FooByCall;
import com.evolvedbinary.jnibench.common.call.FooByCallInvoke;
import com.evolvedbinary.jnibench.common.call.FooByCallStatic;
import com.evolvedbinary.jnibench.consbench.NarSystem;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ConstructionBenchmark {

    static {
        NarSystem.loadLibrary();
    }

    @Benchmark
    public void fooByCall(Blackhole blackhole) {
        final FooByCall fooByCall = new FooByCall();
        blackhole.consume(fooByCall);
    }

    @Benchmark
    public void fooByCallStatic(Blackhole blackhole) {
        final FooByCallStatic fooByCallStatic = new FooByCallStatic();
        blackhole.consume(fooByCallStatic);
    }

    @Benchmark
    public void fooByCallInvoke(Blackhole blackhole) {
        final FooByCallInvoke fooByCallInvoke = new FooByCallInvoke();
        blackhole.consume(fooByCallInvoke);
    }
}
