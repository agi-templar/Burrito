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
#ifndef NEON_INTRINSICS_H
#define NEON_INTRINSICS_H

#include <stdint.h>

typedef struct
{
    uint8_t alpha;
    uint8_t red;
    uint8_t green;
    uint8_t blue;
} argb;

void fir_filter_neon_intrinsics(short *output, const short* input, const short* kernel, int width, int kernelSize);
void line_pixel_processing_intrinsics(argb * new, argb * old, uint32_t width);

#endif /* NEON_INTRINSICS_H */
