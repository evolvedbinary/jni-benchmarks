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

#include "com_evolvedbinary_jnibench_common_array_JniGetArray.h"
#include "FooObject.h"

/*
 * Class:     com_evolvedbinary_jnibench_common_array_JniGetArray
 * Method:    getArray
 * Signature: (J)[Lcom/evolvedbinary/jnibench/common/array/FooObject;
 */
jobjectArray Java_com_evolvedbinary_jnibench_common_array_JniGetArray_getArray
  (JNIEnv *env, jclass, jlong handle) {
  const auto& cpp_array = *reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  jsize length = static_cast<jsize>(cpp_array.size());
  jclass clazz = env->FindClass("com/evolvedbinary/jnibench/common/array/FooObject");
  jmethodID ctor = env->GetMethodID(clazz, "<init>", "(Ljava/lang/String;J)V");
  //jstring init_name = env->NewStringUTF("name");
  //jobject initial_obj = env->NewObject(clazz, ctor, init_name, static_cast<jlong>(0));
  jobjectArray java_array = env->NewObjectArray(length, clazz, nullptr);
  for (size_t i = 0; i < cpp_array.size(); ++i) {
    const jnibench::FooObject& cpp_obj = cpp_array[i];
    jobject java_obj = env->NewObject(clazz, ctor,
        env->NewStringUTF(cpp_obj.GetName().c_str()),
        static_cast<jlong>(cpp_obj.GetValue()));
    env->SetObjectArrayElement(java_array, static_cast<jsize>(i), java_obj);
  }
  return java_array;
}