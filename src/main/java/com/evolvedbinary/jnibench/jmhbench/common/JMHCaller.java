/**
 * Copyright © 2021, Evolved Binary Ltd
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
package com.evolvedbinary.jnibench.jmhbench.common;

/**
 * Utility which uses assumptions about the JMH setup call stack
 * in order to figure out which benchmark class / method is being run.
 * The caller can use this information to guide the setup.
 */
public class JMHCaller {

    private final static int JMH_MAGIC = 3;

    public String benchmarkMethod;
    public String benchmarkClass;

    public static JMHCaller fromStack() {
        JMHCaller caller = new JMHCaller();
        Exception e = new Exception();
        StackTraceElement[] stack = e.getStackTrace();
        assert stack.length > JMH_MAGIC;
        char dot = Character.toChars(0x2E)[0];
        char backslash = Character.toChars(0x5C)[0];
        String clazz = stack[JMH_MAGIC].getClassName();
        String[] clazzPath = clazz.split(String.valueOf(backslash) + String.valueOf(dot));
        String[] methodParts = stack[JMH_MAGIC].getMethodName().split(String.valueOf('_'));
        assert "jmh_generated".equals(clazzPath[clazzPath.length - 2]);
        String[] clazzParts = clazzPath[clazzPath.length - 1].split(String.valueOf('_'));
        caller.benchmarkClass = clazzParts[0];
        caller.benchmarkMethod = clazzParts[1];
        assert caller.benchmarkMethod.equals(methodParts[0]);

        return caller;
    }
}
