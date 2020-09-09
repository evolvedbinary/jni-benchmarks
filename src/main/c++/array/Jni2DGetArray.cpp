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