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
package com.evolvedbinary.jnibench.common.array;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllocateInJavaGet2DArray implements JniListSupplier<FooObject> {

  @Override
  public List<FooObject> getObjectList(final NativeObjectArray<FooObject> nativeObjectArray) {
    final int len = (int) getArraySize(nativeObjectArray.get_nativeHandle());
    if (len == 0) {
      return Collections.emptyList();
    } else {
      final String names[] = new String[len];
      final long values[] = new long[len];

      getArrays(nativeObjectArray.get_nativeHandle(), names, values);

      final List<FooObject> objectList = new ArrayList<>();
      for (int i = 0; i < len; i++) {
        objectList.add(new FooObject(names[i], values[i]));
      }
      return objectList;
    }
  }

  private static native long getArraySize(final long handle);

  private static native void getArrays(final long handle, final String[] paths,
                                       final long[] targetSizes);
}
