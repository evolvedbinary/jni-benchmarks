#include <jni.h>
#include <vector>

#include "com_evolvedbinary_jnibench_common_array_JniGetArrayList.h"
#include "FooObject.h"
#include "Portal.h"

/*
 * Class:     com_evolvedbinary_jnibench_common_array_JniGetArrayList
 * Method:    getArrayList
 * Signature: (J)Ljava/util/List;
 */
jobject Java_com_evolvedbinary_jnibench_common_array_JniGetArrayList_getArrayList
  (JNIEnv *env, jclass, jlong handle) {

  //TODO(AR) move into Portal.h
  const jclass clazz = env->FindClass("com/evolvedbinary/jnibench/common/array/FooObject");
  const jmethodID ctor = env->GetMethodID(clazz, "<init>", "(Ljava/lang/String;J)V");
  if (ctor == nullptr) {
    // exception occurred accessing method
    return nullptr;
  }

  const jclass clazz_array_list = ListJni::getArrayListClass(env);
  const jmethodID ctor_array_list = ListJni::getArrayListConstructorMethodId(env);
  if (ctor_array_list == nullptr) {
    // exception occurred accessing method
    return nullptr;
  }

  const jmethodID add_mid = ListJni::getListAddMethodId(env);
  if (add_mid == nullptr) {
    // exception occurred accessing method
    return nullptr;
  }

  const auto& cpp_array = *reinterpret_cast<std::vector<jnibench::FooObject>*>(handle);
  const jsize len = static_cast<jsize>(cpp_array.size());

  // create new java.util.ArrayList
  const jobject jlist = env->NewObject(clazz_array_list, ctor_array_list,
              static_cast<jint>(len));
  if (env->ExceptionCheck()) {
    // exception occurred constructing object
    if (jlist != nullptr) {
      env->DeleteLocalRef(jlist);
    }
    return nullptr;
  }

  for (auto foo_obj : cpp_array) {
    const jstring jname = env->NewStringUTF(foo_obj.GetName().c_str());
    if (jname == nullptr) {
      env->DeleteLocalRef(jlist);
      return nullptr;
    }

    const jlong jvalue = static_cast<jlong>(foo_obj.GetValue());

    // create java FooObject
    const jobject jfoo_obj = env->NewObject(clazz, ctor,
            jname,
            jvalue);
    if (env->ExceptionCheck()) {
      // exception occurred constructing object
      env->DeleteLocalRef(jlist);
      env->DeleteLocalRef(jname);
      if (jfoo_obj != nullptr) {
        env->DeleteLocalRef(jfoo_obj);
      }
      return nullptr;
    }

    // add to list
    const jboolean rs = env->CallBooleanMethod(jlist, add_mid, jfoo_obj);
    if (env->ExceptionCheck() || rs == JNI_FALSE) {
      // exception occurred calling method, or could not add
      env->DeleteLocalRef(jlist);
      env->DeleteLocalRef(jname);
      env->DeleteLocalRef(jfoo_obj);
      return nullptr;
    }
  }

  return jlist;
}