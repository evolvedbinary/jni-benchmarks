#include <jni.h>
#include <vector>

#include "com_evolvedbinary_jnibench_common_array_AllocateInJavaGetArray.h"
#include "FooObject.h"

/*
 * Class:     com_evolvedbinary_jnibench_common_array_AllocateInJavaGetArray
 * Method:    getArraySize
 * Signature: (J)J
 */
jlong Java_com_evolvedbinary_jnibench_common_array_AllocateInJavaGetArray_getArraySize
  (JNIEnv *, jclass, jlong handle) {
  const auto& cpp_array = *reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  return static_cast<jlong>(cpp_array.size());
}

/*
 * Class:     com_evolvedbinary_jnibench_common_array_AllocateInJavaGetArray
 * Method:    getArrays
 * Signature: (J[Ljava/lang/String;[J)V
 */
void Java_com_evolvedbinary_jnibench_common_array_AllocateInJavaGetArray_getArrays
  (JNIEnv *env, jclass, jlong handle, jobjectArray name_array, jlongArray value_array) {
  jlong* value_array_ptr = env->GetLongArrayElements(value_array, nullptr);
  if (value_array_ptr == nullptr) {
    // exception thrown: OutOfMemoryError
    return;
  }

  auto* cpp_array = reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  for (jsize i = 0; i < env->GetArrayLength(name_array); i++) {
    jnibench::FooObject foo_obj = (*cpp_array)[i];

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

    value_array_ptr[i] = static_cast<jlong>(foo_obj.GetValue());
  }

  env->ReleaseLongArrayElements(value_array, value_array_ptr, JNI_COMMIT);
}