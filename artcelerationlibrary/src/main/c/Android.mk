LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := artTransform-lib
LOCAL_SRC_FILES := artTransform-lib.c
LOCAL_LDLIBS    := -llog -ljnigraphics

include $(BUILD_SHARED_LIBRARY)
