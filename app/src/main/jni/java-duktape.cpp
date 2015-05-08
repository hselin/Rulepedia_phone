#include <string.h>
#include <jni.h>
#include <duktape.h>

extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_duktape_Duktape_createContext(JNIEnv *env,
                                           jobject thiz)
    {
        duk_context *ctx = duk_create_heap_default();
        return (jlong)ctx;
    }

    JNIEXPORT jlong JNICALL
    Java_org_duktape_Duktape_destroyContext(JNIEnv *env,
                                            jobject thiz,
                                            jlong   context)
    {
        duk_context *ctx = (duk_context*)ctx;
        duk_destroy_heap(ctx);
    }
}
