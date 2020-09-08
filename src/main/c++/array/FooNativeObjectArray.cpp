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