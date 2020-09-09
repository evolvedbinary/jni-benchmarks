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

#include "com_evolvedbinary_jnibench_common_array_AllocateInCppGet2DArray.h"
#include "FooObject.h"
#include "Portal.h"

/*
 * Class:     com_evolvedbinary_jnibench_common_array_AllocateInCppGet2DArray
 * Method:    get2DArray
 * Signature: (J)[[Ljava/lang/Object;
 */
jobjectArray Java_com_evolvedbinary_jnibench_common_array_AllocateInCppGet2DArray_get2DArray(
    JNIEnv *env, jclass, jlong handle) {
  const auto& cpp_array = *reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  jsize len = static_cast<jsize>(cpp_array.size());

  const jclass jstring_clazz = StringJni::getJClass(env);
  if (jstring_clazz == nullptr) {
    // exception occurred accessing class
    return nullptr;
  }

  const jclass jlong_clazz = LongJni::getJClass(env);
  if (jlong_clazz == nullptr) {
      // exception occurred accessing class
      return nullptr;
  }

  jobjectArray jname_array = env->NewObjectArray(len, jstring_clazz, nullptr);
  if (jname_array == nullptr) {
    // exception thrown: OutOfMemoryError
    return nullptr;
  }
  jobjectArray jvalue_array = env->NewObjectArray(len, jlong_clazz, nullptr);
  if (jvalue_array == nullptr) {
    // exception thrown: OutOfMemoryError
    env->DeleteLocalRef(jname_array);
    return nullptr;
  }

  for (size_t i = 0; i < cpp_array.size(); ++i) {
    const jnibench::FooObject& foo_obj = cpp_array[i];
    jstring jname = env->NewStringUTF(foo_obj.GetName().c_str());
    if (env->ExceptionCheck()) {
      if (jname != nullptr) {
        env->DeleteLocalRef(jname_array);
        env->DeleteLocalRef(jvalue_array);
        env->DeleteLocalRef(jname);
      }
      return nullptr;
    }

    jobject jvalue = LongJni::construct(env, jlong_clazz, foo_obj.GetValue());
    if (jvalue == nullptr) {
      env->DeleteLocalRef(jname_array);
      env->DeleteLocalRef(jvalue_array);
      env->DeleteLocalRef(jname);
      return nullptr;
    }

    env->SetObjectArrayElement(jname_array, static_cast<jsize>(i), jname);
    if (env->ExceptionCheck()) {
      // exception thrown: ArrayIndexOutOfBoundsException
      // or ArrayStoreException
      env->DeleteLocalRef(jname_array);
      env->DeleteLocalRef(jvalue_array);
      env->DeleteLocalRef(jname);
      env->DeleteLocalRef(jvalue);
      return nullptr;
    }
    env->SetObjectArrayElement(jvalue_array, static_cast<jsize>(i), jvalue);
    if (env->ExceptionCheck()) {
      // exception thrown: ArrayIndexOutOfBoundsException
      // or ArrayStoreException
      env->DeleteLocalRef(jname_array);
      env->DeleteLocalRef(jvalue_array);
      env->DeleteLocalRef(jname);
      env->DeleteLocalRef(jvalue);
      return nullptr;
    }

    env->DeleteLocalRef(jname);
    env->DeleteLocalRef(jvalue);
  }

  jobjectArray jobj_array = env->NewObjectArray(2, env->FindClass("java/lang/Object"), nullptr);
  if (jobj_array == nullptr) {
    // exception thrown: OutOfMemoryError
    env->DeleteLocalRef(jname_array);
    env->DeleteLocalRef(jvalue_array);
    return nullptr;
  }

  env->SetObjectArrayElement(jobj_array, 0, jname_array);
  if (env->ExceptionCheck()) {
    // exception thrown: ArrayIndexOutOfBoundsException
    // or ArrayStoreException
    env->DeleteLocalRef(jname_array);
    env->DeleteLocalRef(jvalue_array);
    env->DeleteLocalRef(jobj_array);
    return nullptr;
  }
  env->SetObjectArrayElement(jobj_array, 1, jvalue_array);
  if (env->ExceptionCheck()) {
    // exception thrown: ArrayIndexOutOfBoundsException
    // or ArrayStoreException
    env->DeleteLocalRef(jname_array);
    env->DeleteLocalRef(jvalue_array);
    env->DeleteLocalRef(jobj_array);
    return nullptr;
  }

  return jobj_array;
}