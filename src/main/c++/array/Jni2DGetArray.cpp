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

#include "com_evolvedbinary_jnibench_common_array_Jni2DGetArray.h"
#include "FooObject.h"

/*
 * Class:     com_evolvedbinary_jnibench_common_array_Jni2DGetArray
 * Method:    get2DArray
 * Signature: (J)[[Ljava/lang/Object;
 */
jobjectArray Java_com_evolvedbinary_jnibench_common_array_Jni2DGetArray_get2DArray
  (JNIEnv *env, jclass, jlong handle) {
  const auto& cpp_array = *reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  jsize len = static_cast<jsize>(cpp_array.size());
  jclass jlong_clazz = env->FindClass("java/lang/Long");
  jmethodID jlong_ctor = env->GetMethodID(jlong_clazz, "<init>", "(J)V");
  jobjectArray name_array = env->NewObjectArray(len, env->FindClass("java/lang/String"), nullptr);
  jobjectArray value_array = env->NewObjectArray(len, jlong_clazz, nullptr);

  for (size_t i = 0; i < cpp_array.size(); ++i) {
    const jnibench::FooObject& foo_obj = cpp_array[i];
    jstring name = env->NewStringUTF(foo_obj.GetName().c_str());
    jobject value = env->NewObject(jlong_clazz, jlong_ctor, static_cast<jlong>(foo_obj.GetValue()));
    env->SetObjectArrayElement(name_array, static_cast<jsize>(i), name);
    env->SetObjectArrayElement(value_array, static_cast<jsize>(i), value);
  }

  jobjectArray obj_array = env->NewObjectArray(2, env->FindClass("java/lang/Object"), nullptr);
  env->SetObjectArrayElement(obj_array, 0, name_array);
  env->SetObjectArrayElement(obj_array, 1, value_array);

  return obj_array;
}