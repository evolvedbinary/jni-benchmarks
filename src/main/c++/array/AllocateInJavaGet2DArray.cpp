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
#include <jni.h>
#include <vector>

#include "com_evolvedbinary_jnibench_common_array_AllocateInJavaGet2DArray.h"
#include "FooObject.h"

/*
 * Class:     com_evolvedbinary_jnibench_common_array_AllocateInJavaGet2DArray
 * Method:    getArraySize
 * Signature: (J)J
 */
jlong Java_com_evolvedbinary_jnibench_common_array_AllocateInJavaGet2DArray_getArraySize(
    JNIEnv *, jclass, jlong handle) {
  const auto& cpp_array = *reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  return static_cast<jlong>(cpp_array.size());
}

/*
 * Class:     com_evolvedbinary_jnibench_common_array_AllocateInJavaGet2DArray
 * Method:    getArrays
 * Signature: (J[Ljava/lang/String;[J)V
 */
void Java_com_evolvedbinary_jnibench_common_array_AllocateInJavaGet2DArray_getArrays(
    JNIEnv *env, jclass, jlong handle, jobjectArray name_array, jlongArray value_array) {
  jlong* value_array_ptr = env->GetLongArrayElements(value_array, nullptr);
  if (value_array_ptr == nullptr) {
    // exception thrown: OutOfMemoryError
    return;
  }

  auto* cpp_array = reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  for (jsize i = 0; i < env->GetArrayLength(name_array); i++) {
    jnibench::FooObject foo_obj = (*cpp_array)[static_cast<size_t>(i)];

    jstring jname = env->NewStringUTF(foo_obj.GetName().c_str());
    if (jname == nullptr) {
      // exception thrown: OutOfMemoryError
      env->ReleaseLongArrayElements(value_array, value_array_ptr, JNI_ABORT);
      return;
    }
    env->SetObjectArrayElement(name_array, i, jname);
    if (env->ExceptionCheck()) {
      // exception thrown: ArrayIndexOutOfBoundsException
      env->DeleteLocalRef(jname);
      env->ReleaseLongArrayElements(value_array, value_array_ptr, JNI_ABORT);
      return;
    }

    env->DeleteLocalRef(jname);

    value_array_ptr[static_cast<size_t>(i)] = static_cast<jlong>(foo_obj.GetValue());
  }

  env->ReleaseLongArrayElements(value_array, value_array_ptr, 0);
}