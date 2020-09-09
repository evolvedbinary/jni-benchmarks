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