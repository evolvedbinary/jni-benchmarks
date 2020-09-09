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

#include "com_evolvedbinary_jnibench_common_array_FooNativeObjectArray.h"
#include "FooObject.h"

/*
 * Class:     com_evolvedbinary_jnibench_common_array_FooNativeObjectArray
 * Method:    newFooObjectArray
 * Signature: ()J
 */
jlong Java_com_evolvedbinary_jnibench_common_array_FooNativeObjectArray_newFooObjectArray
  (JNIEnv *env, jclass, jobjectArray object_array) {
  auto* cpp_array = new std::vector<jnibench::FooObject>();
  for (jsize i = 0; i < env->GetArrayLength(object_array); ++i) {
    jobject obj = env->GetObjectArrayElement(object_array, i);
    jclass obj_clazz = env->GetObjectClass(obj);
    jfieldID name_field_id = env->GetFieldID(obj_clazz, "name" , "Ljava/lang/String;");
    jfieldID value_field_id = env->GetFieldID(obj_clazz, "value" , "J");
    jstring jname = (jstring) env->GetObjectField(obj, name_field_id);
    const char *native_name = env->GetStringUTFChars(jname, nullptr);
    jlong jvalue = env->GetLongField(obj, value_field_id);
    cpp_array->push_back(jnibench::FooObject(std::string(native_name), static_cast<int64_t>(jvalue)));
  }
  return reinterpret_cast<jlong>(cpp_array);
}

/*
 * Class:     com_evolvedbinary_jnibench_common_array_FooNativeObjectArray
 * Method:    disposeInternal
 * Signature: (J)V
 */
void Java_com_evolvedbinary_jnibench_common_array_FooNativeObjectArray_disposeInternal
  (JNIEnv *, jobject, jlong handle) {
  auto* cpp_array = reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  delete cpp_array;
}