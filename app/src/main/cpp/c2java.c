#include <jni.h>
#include <android/log.h>
#include <memory.h>

static const char* kTAG = "c2java";
#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, kTAG, __VA_ARGS__))

typedef struct jni_context {
    JavaVM  *javaVM;
    jclass   nativeNotifierClaz;
    jobject  nativeNotifierObj;
} JniContext;

JniContext gJniContext;

// javaVMをグローバル変数に保存する
// NativeNotifierのクラスIDを見つける
// NativeNotifierのインスタンスを作成する
// NativeNotifierのインスタンスを作成するのグローバル参照を作成する
//ここで割り当てられたすべてのリソースは、アプリケーションによって解放されない
//ペアリング関数JNI_OnUnload（）は呼び出されない
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    memset(&gJniContext, 0, sizeof(gJniContext));

    (void)reserved;

    LOGI("JNI_OnLoad\n");
    gJniContext.javaVM = vm;
    if ((*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR; // JNI version not supported.
    }

    // クラス情報を取得
    jclass  clz = (*env)->FindClass(env, "com/example/nonsugar/service/NativeNotifier");
    if (clz == NULL) {
        LOGI("clz is null...");
    }

    // クラスへの参照を取得してバッファに保存
    gJniContext.nativeNotifierClaz = (*env)->NewGlobalRef(env, clz);

    // コンストラクタを取得
    jmethodID  notifierCtor = (*env)->GetMethodID(env, gJniContext.nativeNotifierClaz, "<init>", "()V");

    // インスタンスを生成
    jobject notifier = (*env)->NewObject(env, gJniContext.nativeNotifierClaz, notifierCtor);
    if (notifier == NULL) {
        LOGI("notifier is null...");
    }
    // 生成したインスタンスへの参照を取得
    gJniContext.nativeNotifierObj = (*env)->NewGlobalRef(env, notifier);

    return  JNI_VERSION_1_6;
}


JNIEXPORT void JNICALL
Java_com_example_nonsugar_service_SampleService_callbackFromC(JNIEnv *env, jobject obj)
{
    (void)obj;
    // VMにスレッドをアタッチ
    (*gJniContext.javaVM)->AttachCurrentThread(gJniContext.javaVM, &env, NULL);

    // NativeDataのインスタンスを作成
    jclass  clz = (*env)->FindClass(env, "com/example/nonsugar/service/NativeData");
    jmethodID  ctor = (*env)->GetMethodID(env, clz, "<init>", "()V");
    jobject data = (*env)->NewObject(env, clz, ctor);
    if (data == NULL) {
        LOGI("data is null...");
    }

    // mIntValueのフィールドを取得
    LOGI("set int value\n");
    jfieldID intField = (*env)->GetFieldID(env, clz, "mIntValue", "I");
    if( intField == NULL) {
        LOGI("intField is null...");
        return;
    }
    // mIntValueに値をセット
    (*env)->SetIntField(env, data, intField, 6);

    // メソッドIDを取得する
    LOGI("get method ID\n");
    jmethodID methodId = (*env)->GetStaticMethodID(env, gJniContext.nativeNotifierClaz, "notifyFromNative", "(Lcom/example/nonsugar/service/BaseNativeData;)V");
    if (methodId == NULL) {
        LOGI("methodId is nul...");
        return;
    }

    // Javaに通知
    LOGI("call java method\n");
    (*env)->CallStaticVoidMethod(env, gJniContext.nativeNotifierClaz, methodId, data);
    (*env)->DeleteLocalRef(env, data);
}