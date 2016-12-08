/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <jni.h>
#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <cpu-features.h>
#include <android/log.h>
#include <android/bitmap.h>
#include "neon-intrinsics.h"
#include <math.h>


#define  LOG_TAG    "libArtTransform"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


#define DEBUG 0

#if DEBUG
#include <android/log.h>
#  define  D(x...)  __android_log_print(ANDROID_LOG_INFO,"helloneon",x)
#else
#  define  D(...)  do {} while (0)
#endif


static int rgb_clamp(int value) {
    if(value > 255) {
        return 255;
    }
    if(value < 0) {
        return 0;
    }
    return value;
}

// Core function for the LOMO artTransform
void line_pixel_processing_lomo(argb *new, argb *old, uint32_t width) {
    int i;
    for (i=0;i<width;i++) {
        new[i].red = rgb_clamp(((((old[i].red / 255.0) - 0.5) * 0.6) + 0.5) * 255.0);
        new[i].green = rgb_clamp(((((old[i].green / 255.0) - 0.5) * 0.6) + 0.5) * 255.0);
        new[i].blue = rgb_clamp(((((old[i].blue / 255.0) - 0.5) * 0.6) + 0.5) * 255.0);
    }
}

// Core function for the Clarendon artTransform
void line_pixel_processing_Clarendon(argb *new, argb *old, uint32_t width) {
    int i;
    for (i = 0; i < width; i++) {
        new[i].red = rgb_clamp(300 * old[i].red/255 + 0.3 * old[i].green + 0.1 * old[i].blue);
        new[i].green = rgb_clamp(0.1 * old[i].red + 300 * old[i].green/255 + 0.3 * old[i].blue);
        new[i].blue = rgb_clamp(0.3 * old[i].red + 0.1 * old[i].green + 300 * old[i].blue/255);
    }
}

// Core function for the Noir artTransform
void line_pixel_processing_grey(uint8_t *new, argb *old, uint32_t width) {
    int i;
    for (i = 0; i < width; i++) {
        new[i] = rgb_clamp(0.5 * old[i].red + 0.1 * old[i].green + 0.6*old[i].blue);
    }
}


/*
lomo ArtTransform
Pixel operation
*/
JNIEXPORT void JNICALL Java_edu_asu_msrs_artcelerationlibrary_artcelerationService_ArtTransformHandler_lomo(JNIEnv * env, jobject  obj, jobject bitmapcolor,jobject bitmapgray)
{
    AndroidBitmapInfo  infocolor;
    void*              pixelscolor;
    AndroidBitmapInfo  infogray;
    void *pixelslomo;
    int                ret;
    int 			y;
    int             x;
    char buffer[512];


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

    if ((ret = AndroidBitmap_lockPixels(env, bitmapgray, &pixelslomo)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    // modify pixels with image processing algorithm

    for (y=0;y<(&infocolor)->height;y++) {
        argb * line = (argb *) pixelscolor;
        argb *grayline = (argb *) pixelslomo;

        // No Neon Version, do the LOMO artTransform
        line_pixel_processing_lomo(grayline, line, infocolor.width);


        pixelscolor = (char *)pixelscolor + infocolor.stride;
        pixelslomo = (char *) pixelslomo + infogray.stride;
    }

    LOGI("unlocking pixels");
    AndroidBitmap_unlockPixels(env, bitmapcolor);
    AndroidBitmap_unlockPixels(env, bitmapgray);


}

JNIEXPORT void JNICALL Java_edu_asu_msrs_artcelerationlibrary_artcelerationService_ArtTransformHandler_Clarendon(
        JNIEnv *env, jobject obj, jobject bitmapcolor, jobject bitmapgray) {
    AndroidBitmapInfo infocolor;
    void *pixelscolor;
    AndroidBitmapInfo infogray;
    void *pixelslomo;
    int ret;
    int y;
    int x;


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

    if ((ret = AndroidBitmap_lockPixels(env, bitmapgray, &pixelslomo)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    // modify pixels with image processing algorithm

    for (y = 0; y < (&infocolor)->height; y++) {
        argb *line = (argb *) pixelscolor;
        argb *grayline = (argb *) pixelslomo;

        // No Neon Version, do the filter artTransform
        line_pixel_processing_Clarendon(grayline, line, infocolor.width);

        pixelscolor = (char *) pixelscolor + infocolor.stride;
        pixelslomo = (char *) pixelslomo + infogray.stride;
    }

    LOGI("unlocking pixels");
    AndroidBitmap_unlockPixels(env, bitmapcolor);
    AndroidBitmap_unlockPixels(env, bitmapgray);


}

JNIEXPORT void JNICALL Java_edu_asu_msrs_artcelerationlibrary_artcelerationService_ArtTransformHandler_grey(
        JNIEnv *env, jobject obj, jobject bitmapcolor, jobject bitmapgray) {
    AndroidBitmapInfo infocolor;
    void *pixelscolor;
    AndroidBitmapInfo infogray;
    void *pixelslomo;
    int ret;
    int y;
    int x;


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

    if ((ret = AndroidBitmap_lockPixels(env, bitmapgray, &pixelslomo)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    // modify pixels with image processing algorithm

    for (y = 0; y < (&infocolor)->height; y++) {
        argb *line = (argb *) pixelscolor;
        uint8_t *grayline = (uint8_t *) pixelslomo;

        // No Neon Version, do the grey artTransform
        line_pixel_processing_grey(grayline, line, infocolor.width);

        pixelscolor = (char *) pixelscolor + infocolor.stride;
        pixelslomo = (char *) pixelslomo + infogray.stride;
    }

    LOGI("unlocking pixels");
    AndroidBitmap_unlockPixels(env, bitmapcolor);
    AndroidBitmap_unlockPixels(env, bitmapgray);


}

JNIEXPORT void JNICALL JNICALL Java_edu_asu_msrs_artcelerationlibrary_artcelerationService_ArtTransformHandler_findEdges(JNIEnv * env, jobject  obj, jobject bitmapgray,jobject bitmapedges) {
    AndroidBitmapInfo infogray;
    void *pixelsgray;
    AndroidBitmapInfo infoedges;
    void *pixelsedge;
    int ret;
    int y;
    int x;
    int sumX, sumY, sum;
    int i, j;
    int Gx[3][3];
    int Gy[3][3];
    uint8_t *graydata;
    uint8_t *edgedata;


    LOGI("findEdges running");

    Gx[0][0] = -1;Gx[0][1] = 0;Gx[0][2] = 1;
    Gx[1][0] = -2;Gx[1][1] = 0;Gx[1][2] = 2;
    Gx[2][0] = -1;Gx[2][1] = 0;Gx[2][2] = 1;


    Gy[0][0] = 1;Gy[0][1] = 2;Gy[0][2] = 1;
    Gy[1][0] = 0;Gy[1][1] = 0;Gy[1][2] = 0;
    Gy[2][0] = -1;Gy[2][1] = -2;Gy[2][2] = -1;


    if ((ret = AndroidBitmap_getInfo(env, bitmapgray, &infogray)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }


    if ((ret = AndroidBitmap_getInfo(env, bitmapedges, &infoedges)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }


    LOGI("gray image :: width is %d; height is %d; stride is %d; format is %d;flags is %d",
         infogray.width, infogray.height, infogray.stride, infogray.format, infogray.flags);
    if (infogray.format != ANDROID_BITMAP_FORMAT_A_8) {
        LOGE("Bitmap format is not A_8 !");
        return;
    }

    LOGI("color image :: width is %d; height is %d; stride is %d; format is %d;flags is %d",
         infoedges.width, infoedges.height, infoedges.stride, infoedges.format, infoedges.flags);
    if (infoedges.format != ANDROID_BITMAP_FORMAT_A_8) {
        LOGE("Bitmap format is not A_8 !");
        return;
    }


    if ((ret = AndroidBitmap_lockPixels(env, bitmapgray, &pixelsgray)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmapedges, &pixelsedge)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }


    // modify pixels with image processing algorithm
    LOGI("time to modify pixels....");

    graydata = (uint8_t *) pixelsgray;
    edgedata = (uint8_t *) pixelsedge;

    for (y = 0; y <= infogray.height - 1; y++) {
        for (x = 0; x < infogray.width - 1; x++) {
            sumX = 0;
            sumY = 0;
            // check boundaries
            if (y == 0 || y == infogray.height - 1) {
                sum = 0;
            } else if (x == 0 || x == infogray.width - 1) {
                sum = 0;
            } else {
                // calc X gradient
                for (i = -1; i <= 1; i++) {
                    for (j = -1; j <= 1; j++) {
                        sumX += (int) ((*(graydata + x + i + (y + j) * infogray.stride)) *
                                       Gx[i + 1][j + 1]);
                    }
                }

                // calc Y gradient
                for (i = -1; i <= 1; i++) {
                    for (j = -1; j <= 1; j++) {
                        sumY += (int) ((*(graydata + x + i + (y + j) * infogray.stride)) *
                                       Gy[i + 1][j + 1]);
                    }
                }

                sum = abs(sumX) + abs(sumY);

            }

            if (sum > 255) sum = 255;
            if (sum < 0) sum = 0;

            *(edgedata + x + y * infogray.width) = 255 - (uint8_t) sum;


        }
    }
}

/* return current time in milliseconds */
static double
now_ms(void)
{
    struct timespec res;
    clock_gettime(CLOCK_REALTIME, &res);
    return 1000.0*res.tv_sec + (double)res.tv_nsec/1e6;
}


/* this is a FIR filter implemented in C */
static void
fir_filter_c(short *output, const short* input, const short* kernel, int width, int kernelSize)
{
    int  offset = -kernelSize/2;
    int  nn;
    for (nn = 0; nn < width; nn++) {
        int sum = 0;
        int mm;
        for (mm = 0; mm < kernelSize; mm++) {
            sum += kernel[mm]*input[nn+offset+mm];
        }
        output[nn] = (short)((sum + 0x8000) >> 16);
    }
}

#define  FIR_KERNEL_SIZE   32
#define  FIR_OUTPUT_SIZE   2560
#define  FIR_INPUT_SIZE    (FIR_OUTPUT_SIZE + FIR_KERNEL_SIZE)
#define  FIR_ITERATIONS    600

static const short  fir_kernel[FIR_KERNEL_SIZE] = { 
    0x10, 0x20, 0x40, 0x70, 0x8c, 0xa2, 0xce, 0xf0, 0xe9, 0xce, 0xa2, 0x8c, 070, 0x40, 0x20, 0x10,
    0x10, 0x20, 0x40, 0x70, 0x8c, 0xa2, 0xce, 0xf0, 0xe9, 0xce, 0xa2, 0x8c, 070, 0x40, 0x20, 0x10 };

static short        fir_output[FIR_OUTPUT_SIZE];
static short        fir_input_0[FIR_INPUT_SIZE];
static const short* fir_input = fir_input_0 + (FIR_KERNEL_SIZE/2);
static short        fir_output_expected[FIR_OUTPUT_SIZE];

/* This is a trivial JNI example where we use a native method
 * to return a new VM String. See the corresponding Java source
 * file located at:
 *
 *   apps/samples/hello-neon/project/src/com/example/neon/HelloNeon.java
 */
jstring
Java_com_example_helloneon_HelloNeon_stringFromJNI( JNIEnv* env,
                                               jobject thiz )
{
    char*  str;
    AndroidCpuFamily family;
    uint64_t features;
    char buffer[512];
    char tryNeon = 0;
    double  t0, t1, time_c, time_neon;

    /* setup FIR input - whatever */
    {
        int  nn;
        for (nn = 0; nn < FIR_INPUT_SIZE; nn++) {
            fir_input_0[nn] = (5*nn) & 255;
        }
        fir_filter_c(fir_output_expected, fir_input, fir_kernel, FIR_OUTPUT_SIZE, FIR_KERNEL_SIZE);
    }

    /* Benchmark small FIR filter loop - C version */
    t0 = now_ms();
    {
        int  count = FIR_ITERATIONS;
        for (; count > 0; count--) {
            fir_filter_c(fir_output, fir_input, fir_kernel, FIR_OUTPUT_SIZE, FIR_KERNEL_SIZE);
        }
    }
    t1 = now_ms();
    time_c = t1 - t0;

    asprintf(&str, "FIR Filter benchmark:\nC version          : %g ms\n", time_c);
    strlcpy(buffer, str, sizeof buffer);
    free(str);

    strlcat(buffer, "Neon version   : ", sizeof buffer);

    family = android_getCpuFamily();
    if ((family != ANDROID_CPU_FAMILY_ARM) &&
        (family != ANDROID_CPU_FAMILY_X86))
    {
        strlcat(buffer, "Not an ARM and not an X86 CPU !\n", sizeof buffer);
        goto EXIT;
    }

    features = android_getCpuFeatures();
    if (((features & ANDROID_CPU_ARM_FEATURE_ARMv7) == 0) &&
        ((features & ANDROID_CPU_X86_FEATURE_SSSE3) == 0))
    {
        strlcat(buffer, "Not an ARMv7 and not an X86 SSSE3 CPU !\n", sizeof buffer);
        goto EXIT;
    }


    /* HAVE_NEON is defined in Android.mk ! */
#ifdef HAVE_NEON
    if (((features & ANDROID_CPU_ARM_FEATURE_NEON) == 0) &&
        ((features & ANDROID_CPU_X86_FEATURE_SSSE3) == 0))
    {
        strlcat(buffer, "CPU doesn't support NEON !\n", sizeof buffer);
        goto EXIT;
    }

    /* Benchmark small FIR filter loop - Neon version */
    t0 = now_ms();
    {
        int  count = FIR_ITERATIONS;
        for (; count > 0; count--) {
            fir_filter_neon_intrinsics(fir_output, fir_input, fir_kernel, FIR_OUTPUT_SIZE, FIR_KERNEL_SIZE);
        }
    }
    t1 = now_ms();
    time_neon = t1 - t0;
    asprintf(&str, "%g ms (x%g faster)\n", time_neon, time_c / (time_neon < 1e-6 ? 1. : time_neon));
    strlcat(buffer, str, sizeof buffer);
    free(str);

    /* check the result, just in case */
    {
        int  nn, fails = 0;
        for (nn = 0; nn < FIR_OUTPUT_SIZE; nn++) {
            if (fir_output[nn] != fir_output_expected[nn]) {
                if (++fails < 16)
                    D("neon[%d] = %d expected %d", nn, fir_output[nn], fir_output_expected[nn]);
            }
        }
        D("%d fails\n", fails);
    }
#else /* !HAVE_NEON */
    strlcat(buffer, "Program not compiled with ARMv7 support !\n", sizeof buffer);
#endif /* !HAVE_NEON */
EXIT:
    return (*env)->NewStringUTF(env, buffer);
}
