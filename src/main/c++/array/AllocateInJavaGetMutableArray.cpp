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

#include "com_evolvedbinary_jnibench_common_array_AllocateInJavaGetMutableArray.h"
#include "FooObject.h"
#include "Portal.h"

/*
 * Class:     com_evolvedbinary_jnibench_common_array_AllocateInJavaGetMutableArray
 * Method:    getArraySize
 * Signature: (J)J
 */
jlong Java_com_evolvedbinary_jnibench_common_array_AllocateInJavaGetMutableArray_getArraySize(
    JNIEnv *, jclass, jlong handle) {
  const auto& cpp_array = *reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  return static_cast<jlong>(cpp_array.size());
}

/*
 * Class:     com_evolvedbinary_jnibench_common_array_AllocateInJavaGetMutableArray
 * Method:    getArray
 * Signature: (J[Lcom/evolvedbinary/jnibench/common/array/FooObject;)V
 */
void Java_com_evolvedbinary_jnibench_common_array_AllocateInJavaGetMutableArray_getArray(
    JNIEnv *env, jclass, jlong handle, jobjectArray jobject_array) {
  const jclass jfoo_obj_clazz = FooObjectJni::getJClass(env);
  if (jfoo_obj_clazz == nullptr) {
    // exception occurred accessing class
    return;
  }

  const jfieldID fid_name = FooObjectJni::getNameField(env, jfoo_obj_clazz);
  if (fid_name == nullptr) {
    // exception occurred accessing field
    return;
  }

  const jfieldID fid_value = FooObjectJni::getValueField(env, jfoo_obj_clazz);
  if (fid_value == nullptr) {
    // exception occurred accessing field
    return;
  }

  auto* cpp_array = reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  for (jsize i = 0; i < env->GetArrayLength(jobject_array); i++) {
    jnibench::FooObject foo_obj = (*cpp_array)[static_cast<size_t>(i)];

    jobject jfoo_obj = env->GetObjectArrayElement(jobject_array, i);
    if(env->ExceptionCheck()) {
      // exception thrown: ArrayIndexOutOfBoundsException
      // or ArrayStoreException
      if (jfoo_obj != nullptr) {
        env->DeleteLocalRef(jfoo_obj);
      }
      return;
    }

    // set name field
    jstring jname = env->NewStringUTF(foo_obj.GetName().c_str());
    if (env->ExceptionCheck()) {
      if (jname != nullptr) {
        env->DeleteLocalRef(jname);
      }
      env->DeleteLocalRef(jfoo_obj);
      return;
    }
    env->SetObjectField(jfoo_obj, fid_name, jname);
    if (env->ExceptionCheck()) {
          env->DeleteLocalRef(jname);
          env->DeleteLocalRef(jfoo_obj);
          return;
    }
    env->DeleteLocalRef(jname);

    // set value field
    env->SetLongField(jfoo_obj, fid_value, static_cast<jlong>(foo_obj.GetValue()));
    if (env->ExceptionCheck()) {
      env->DeleteLocalRef(jfoo_obj);
      return;
    }

    env->DeleteLocalRef(jfoo_obj);
  }
}