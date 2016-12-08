
cmake_minimum_required(VERSION 3.4.1)

add_library(cpufeatures STATIC
            ${ANDROID_NDK}/sources/android/cpufeatures/cpu-features.c)

# set up neon build flag for file using intrinsics
# name: helloneon-intrinsics.c (It is named EXACTLY as this on disk,
#                              just like a normal source file)
# then set up neon flag for neon files
# [This example only build for armeabi-v7a, x86 could be done the same way]
#
if (${ANDROID_ABI} STREQUAL "armeabi-v7a")
  # make a list of neon files and add neon compiling flags to them
  set(neon_SRCS src/main/c/neon-intrinsics.c)

  set_property(SOURCE ${neon_SRCS}
               APPEND_STRING PROPERTY COMPILE_FLAGS " -mfpu=neon")
  add_definitions("-DHAVE_NEON=1")
else ()
  set(neon_SRCS)
endif ()


add_library( artTransform-lib SHARED
             src/main/c/neon.c
             ${neon_SRCS} )


find_library( log-lib
                log )

find_library( jnigraphics-lib
                 jnigraphics )


target_include_directories(artTransform-lib PRIVATE
                           ${ANDROID_NDK}/sources/android/cpufeatures)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

# Links your native library against one or more other native libraries.
target_link_libraries( artTransform-lib
                       android
                       cpufeatures

                       # Links the log library to the target library.
                       ${log-lib}
                       ${jnigraphics-lib})