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
#include <assert.h>
#include <jni.h>

class JavaClass {
 public:
  /**
   * Gets and initializes a Java Class
   *
   * @param env A pointer to the Java environment
   * @param jclazz_name The fully qualified JNI name of the Java Class
   *     e.g. "java/lang/String"
   *
   * @return The Java Class or nullptr if one of the
   *     ClassFormatError, ClassCircularityError, NoClassDefFoundError,
   *     OutOfMemoryError or ExceptionInInitializerError exceptions is thrown
   */
  static jclass getJClass(JNIEnv* env, const char* jclazz_name) {
    jclass jclazz = env->FindClass(jclazz_name);
    assert(jclazz != nullptr);
    return jclazz;
  }
};

class FooObjectJni : public JavaClass {
 public:

  /**
   * Get the Java Class com.evolvedbinary.jnibench.common.array.FooObject
   *
   * @param env A pointer to the Java environment
   *
   * @return The Java Class or nullptr if one of the
   *     ClassFormatError, ClassCircularityError, NoClassDefFoundError,
   *     OutOfMemoryError or ExceptionInInitializerError exceptions is thrown
   */
  static jclass getJClass(JNIEnv* env) {
    return JavaClass::getJClass(env, "com/evolvedbinary/jnibench/common/array/FooObject");
  }

  static jmethodID getConstructor(JNIEnv* env, jclass jclazz) {
      return env->GetMethodID(jclazz, "<init>", "(Ljava/lang/String;J)V");
  }

  /**
   * Create a new Java com.evolvedbinary.jnibench.common.array.FooObject object
   * with the same properties as * the provided C++ jnibench::FooObject object.
   *
   * @param jclazz
   * @param env A pointer to the Java environment
   * @param status The jnibench::FooObject object
   *
   * @return A reference to a com.evolvedbinary.jnibench.common.array.FooObject object,
   *     or nullptr if an an exception occurs
   */
  static jobject construct(JNIEnv* env, jclass jclazz, const jnibench::FooObject& foo_object) {
    jmethodID mid = getConstructor(env, jclazz);
    if (mid == nullptr) {
      // exception thrown: NoSuchMethodException or OutOfMemoryError
      return nullptr;
    }

    jstring jname = env->NewStringUTF(foo_object.GetName().c_str());
    if (env->ExceptionCheck()) {
      if (jname != nullptr) {
        env->DeleteLocalRef(jname);
      }
      return nullptr;
    }

    jobject jfoo_object = env->NewObject(jclazz, mid, jname, static_cast<jlong>(foo_object.GetValue()));

    if (env->ExceptionCheck()) {
      // exception occurred
      if (jname != nullptr) {
        env->DeleteLocalRef(jname);
      }
      return nullptr;
    }

    env->DeleteLocalRef(jname);

    return jfoo_object;
  }
};

class LongJni : public JavaClass {
 public:

  /**
   * Get the Java Class java.lang.Long
   *
   * @param env A pointer to the Java environment
   *
   * @return The Java Class or nullptr if one of the
   *     ClassFormatError, ClassCircularityError, NoClassDefFoundError,
   *     OutOfMemoryError or ExceptionInInitializerError exceptions is thrown
   */
  static jclass getJClass(JNIEnv* env) {
    return JavaClass::getJClass(env, "java/lang/Long");
  }

  static jmethodID getConstructor(JNIEnv* env, jclass jclazz) {
    return env->GetMethodID(jclazz, "<init>", "(J)V");
  }

  static jobject construct(JNIEnv* env, jclass jclazz, const int64_t& value) {
    jmethodID mid = getConstructor(env, jclazz);
    if (mid == nullptr) {
      // exception thrown: NoSuchMethodException or OutOfMemoryError
      return nullptr;
    }

    jobject jvalue = env->NewObject(jclazz, mid, static_cast<jlong>(value));
    if (env->ExceptionCheck()) {
      // exception occurred
      if (jvalue != nullptr) {
        env->DeleteLocalRef(jvalue);
      }
      return nullptr;
    }

    return jvalue;
  }
};

class StringJni : public JavaClass {
 public:
  /**
   * Get the Java Class java.lang.String
   *
   * @param env A pointer to the Java environment
   *
   * @return The Java Class or nullptr if one of the
   *     ClassFormatError, ClassCircularityError, NoClassDefFoundError,
   *     OutOfMemoryError or ExceptionInInitializerError exceptions is thrown
   */
  static jclass getJClass(JNIEnv* env) {
    return JavaClass::getJClass(env, "java/lang/String");
  }
};

class ListJni : public JavaClass {
 public:
  /**
   * Get the Java Class java.util.List
   *
   * @param env A pointer to the Java environment
   *
   * @return The Java Class or nullptr if one of the
   *     ClassFormatError, ClassCircularityError, NoClassDefFoundError,
   *     OutOfMemoryError or ExceptionInInitializerError exceptions is thrown
   */
  static jclass getListClass(JNIEnv* env) {
    return JavaClass::getJClass(env, "java/util/List");
  }

  /**
   * Get the Java Class java.util.ArrayList
   *
   * @param env A pointer to the Java environment
   *
   * @return The Java Class or nullptr if one of the
   *     ClassFormatError, ClassCircularityError, NoClassDefFoundError,
   *     OutOfMemoryError or ExceptionInInitializerError exceptions is thrown
   */
  static jclass getArrayListClass(JNIEnv* env) {
    return JavaClass::getJClass(env, "java/util/ArrayList");
  }

  /**
   * Get the Java Method: ArrayList constructor
   *
   * @param env A pointer to the Java environment
   *
   * @return The Java Method ID or nullptr if the class or method id could not
   *     be retrieved
   */
  static jmethodID getArrayListConstructorMethodId(JNIEnv* env) {
    jclass jarray_list_clazz = getArrayListClass(env);
    if(jarray_list_clazz == nullptr) {
      // exception occurred accessing class
      return nullptr;
    }
    static jmethodID mid =
        env->GetMethodID(jarray_list_clazz, "<init>", "(I)V");
    assert(mid != nullptr);
    return mid;
  }

  /**
   * Get the Java Method: List#add
   *
   * @param env A pointer to the Java environment
   *
   * @return The Java Method ID or nullptr if the class or method id could not
   *     be retieved
   */
  static jmethodID getListAddMethodId(JNIEnv* env) {
    jclass jlist_clazz = getListClass(env);
    if(jlist_clazz == nullptr) {
      // exception occurred accessing class
      return nullptr;
    }

    static jmethodID mid =
        env->GetMethodID(jlist_clazz, "add", "(Ljava/lang/Object;)Z");
    assert(mid != nullptr);
    return mid;
  }
};