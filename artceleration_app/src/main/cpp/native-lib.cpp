#include <jni.h>
#include <string>
extern "C"
jstring
Java_edu_asu_msrs_artceleration_MainViewer_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
