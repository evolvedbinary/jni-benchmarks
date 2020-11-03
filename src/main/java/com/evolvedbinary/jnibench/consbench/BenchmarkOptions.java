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
package com.evolvedbinary.jnibench.consbench;

import java.util.List;
import java.util.Map;

public class BenchmarkOptions {
  private final int iterations;
  private final boolean outputAsCSV;
  private final boolean noCsvHeader;
  private final boolean inNs;
  private final boolean close;
  private final Map<String, List<String>> params;

  public BenchmarkOptions(final int iterations, final boolean outputAsCSV, final boolean noCsvHeader, final boolean inNs, final boolean close, final Map<String, List<String>> params) {
    this.iterations = iterations;
    this.outputAsCSV = outputAsCSV;
    this.noCsvHeader = noCsvHeader;
    this.inNs = inNs;
    this.close = close;
    this.params = params;
  }

  public int getIterations() {
    return iterations;
  }

  public boolean isOutputAsCSV() {
    return outputAsCSV;
  }

  public boolean isNoCsvHeader() {
    return noCsvHeader;
  }

  public boolean isInNs() {
    return inNs;
  }

  public boolean isClose() {
    return close;
  }

  public Map<String, List<String>> getParams() {
    return params;
  }
}
