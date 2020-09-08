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
  jstring init_name = env->NewStringUTF("name");
  jobject initial_obj = env->NewObject(clazz, ctor, init_name, static_cast<jlong>(0));
  jobjectArray java_array = env->NewObjectArray(length, clazz, initial_obj);
  for (size_t i = 0; i < cpp_array.size(); ++i) {
    const jnibench::FooObject& cpp_obj = cpp_array[i];
    jobject java_obj = env->NewObject(clazz, ctor,
        env->NewStringUTF(cpp_obj.GetName().c_str()),
        static_cast<jlong>(cpp_obj.GetValue()));
    env->SetObjectArrayElement(java_array, static_cast<jsize>(i), java_obj);
  }
  return java_array;
}