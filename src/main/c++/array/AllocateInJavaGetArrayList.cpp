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

#include "com_evolvedbinary_jnibench_common_array_AllocateInJavaGetArrayList.h"
#include "FooObject.h"
#include "Portal.h"

/*
 * Class:     com_evolvedbinary_jnibench_common_array_AllocateInJavaGetArrayList
 * Method:    getListSize
 * Signature: (J)J
 */
jlong Java_com_evolvedbinary_jnibench_common_array_AllocateInJavaGetArrayList_getListSize
  (JNIEnv *, jclass, jlong handle) {
  const auto& cpp_array = *reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  return static_cast<jlong>(cpp_array.size());
}

/*
 * Class:     com_evolvedbinary_jnibench_common_array_AllocateInJavaGetArrayList
 * Method:    getArrays
 * Signature: (JLjava/util/List;)V
 */
void Java_com_evolvedbinary_jnibench_common_array_AllocateInJavaGetArrayList_getList
  (JNIEnv *env, jclass, jlong handle, jobject jlist) {

  //TODO(AR) move into Portal.h
  const jclass clazz = env->FindClass("com/evolvedbinary/jnibench/common/array/FooObject");
  const jmethodID ctor = env->GetMethodID(clazz, "<init>", "(Ljava/lang/String;J)V");
  if (ctor == nullptr) {
    // exception occurred accessing method
    return;
  }

  const jmethodID add_mid = ListJni::getListAddMethodId(env);
  if (add_mid == nullptr) {
    // exception occurred accessing method
    return;
  }

  const auto& cpp_array = *reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  for (auto foo_obj : cpp_array) {
    const jstring jname = env->NewStringUTF(foo_obj.GetName().c_str());
    if (jname == nullptr) {
      return;
    }

    const jlong jvalue = static_cast<jlong>(foo_obj.GetValue());

    // create java FooObject
    const jobject jfoo_obj = env->NewObject(clazz, ctor,
            jname,
            jvalue);
    if (env->ExceptionCheck()) {
      // exception occurred constructing object
      env->DeleteLocalRef(jname);
      if (jfoo_obj != nullptr) {
        env->DeleteLocalRef(jfoo_obj);
      }
      return;
    }

    // add to list
    const jboolean rs = env->CallBooleanMethod(jlist, add_mid, jfoo_obj);
    if (env->ExceptionCheck() || rs == JNI_FALSE) {
      // exception occurred calling method, or could not add
      env->DeleteLocalRef(jname);
      env->DeleteLocalRef(jfoo_obj);
      return;
    }
  }
}