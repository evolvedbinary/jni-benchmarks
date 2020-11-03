/**
 * Copyright © 2016, Evolved Binary Ltd
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.evolvedbinary.jnibench.jmhbench;

import com.evolvedbinary.jnibench.common.call.FooByCall;
import com.evolvedbinary.jnibench.common.call.FooByCallFinal;
import com.evolvedbinary.jnibench.common.call.FooByCallInvoke;
import com.evolvedbinary.jnibench.common.call.FooByCallInvokeFinal;
import com.evolvedbinary.jnibench.common.call.FooByCallStatic;
import com.evolvedbinary.jnibench.common.call.FooByCallStaticFinal;
import com.evolvedbinary.jnibench.consbench.NarSystem;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ConstructionBenchmark {

    static {
        NarSystem.loadLibrary();
    }

    @Benchmark
    public FooByCall fooByCall() {
        return new FooByCall();
    }

    @Benchmark
    public FooByCallStatic fooByCallStatic() {
        return new FooByCallStatic();
    }

    @Benchmark
    public FooByCallInvoke fooByCallInvoke() {
        return new FooByCallInvoke();
    }

    @Benchmark
    public FooByCallFinal fooByCallFinal() {
        return new FooByCallFinal();
    }

    @Benchmark
    public FooByCallStaticFinal fooByCallStaticFinal() {
        return new FooByCallStaticFinal();
    }

    @Benchmark
    public FooByCallInvokeFinal fooByCallInvokeFinal() {
        return new FooByCallInvokeFinal();
    }
}
