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
/**
 * */
package com.evolvedbinary.jni.consbench;

/**
 *
 * A small JNI Benchmark to show the difference
 * in cost between various models of Object Construction
 * for a Java API that wraps a C++ API using JNI
 *
 * @author Adam Retter <adam.retter@googlemail.com>
 */
public class Benchmark {

    private final static int ITERATIONS = 1000000;

    public final static void main(final String args[]) {

        NarSystem.loadLibrary();

        //TEST1 - Foo By Call
        final long start1 = System.currentTimeMillis();
        for(int i = 0; i < ITERATIONS; i++) {
            final FooByCall fooByCall = new FooByCall();
        }
        final long end1 = System.currentTimeMillis();
        System.out.println("FooByCall: " + (end1 - start1) + "ms");


        //TEST2 - Foo By Call Static
        final long start2 = System.currentTimeMillis();
        for(int i = 0; i < ITERATIONS; i++) {
            final FooByCallStatic fooByCallStatic = new FooByCallStatic();
        }
        final long end2 = System.currentTimeMillis();
        System.out.println("FooByCallStatic: " + (end2 - start2) + "ms");

        //TEST3 - Foo By Call Invoke
        final long start3 = System.currentTimeMillis();
        for(int i = 0; i < ITERATIONS; i++) {
            final FooByCallInvoke fooByCallInvoke = new FooByCallInvoke();
        }
        final long end3 = System.currentTimeMillis();
        System.out.println("FooByCallInvoke: " + (end3 - start3) + "ms");
    }
}
