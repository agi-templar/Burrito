#include <jni.h>
#include <time.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define  LOG_TAG    "libArtTransform"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

typedef struct
{
    uint8_t alpha;
    uint8_t red;
    uint8_t green;
    uint8_t blue;
} argb;

static int rgb_clamp(int value) {
    if(value > 255) {
        return 255;
    }
    if(value < 0) {
        return 0;
    }
    return value;
}



JNIEXPORT void JNICALL Java_edu_dartmouth_cs65_artcelerationlibrary_artcelerationService_ArtTransformHandler_convertToGray(JNIEnv * env, jobject  obj, jobject bitmapcolor,jobject bitmapgray)
{
    AndroidBitmapInfo  infocolor;
    void*              pixelscolor;
    AndroidBitmapInfo  infogray;
    void*              pixelsgray;
    int                ret;
    int 			y;
    int             x;
    int red, green, blue;


    LOGI("convertToGray");
    if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }


    if ((ret = AndroidBitmap_getInfo(env, bitmapgray, &infogray)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmapcolor, &pixelscolor)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmapgray, &pixelsgray)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    // modify pixels with image processing algorithm

    for (y=0;y<(&infocolor)->height;y++) {
        argb * line = (argb *) pixelscolor;
        argb * grayline = (argb *) pixelsgray;
        for (x=0;x<(&infocolor)->width;x++) {

            grayline[x].red = rgb_clamp(0.3 * line[x].red + 0.59 * line[x].green + 0.11*line[x].blue);
            grayline[x].green = rgb_clamp(0.8 * line[x].red + 0.39 * line[x].green + 0.71*line[x].blue);
            grayline[x].blue = rgb_clamp(0.3 * line[x].red + 0.79 * line[x].green + 0.61*line[x].blue);

    }

        pixelscolor = (char *)pixelscolor + infocolor.stride;
        pixelsgray = (char *) pixelsgray + infogray.stride;
    }

    LOGI("unlocking pixels");
    AndroidBitmap_unlockPixels(env, bitmapcolor);
    AndroidBitmap_unlockPixels(env, bitmapgray);

}
