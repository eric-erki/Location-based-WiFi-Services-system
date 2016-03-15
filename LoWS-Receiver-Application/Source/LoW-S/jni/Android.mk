LOCAL_PATH := $(call my-dir) 
include $(CLEAR_VARS) 
LOCAL_MODULE := libnl_2
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_SRC_FILES := /home/sz/Android_Source/out/target/product/crespo/obj/STATIC_LIBRARIES/libnl_2_intermediates/libnl_2.a
include $(PREBUILT_STATIC_LIBRARY)

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := nlscanner
LOCAL_SRC_FILES := /home/sz/Dropbox/TKN-Beacon-Stuffing/eclipse/LoW-S/jni/nlscan.c 
LOCAL_CFLAGS += -I/home/sz/Android_Source/external/libnl-headers
LOCAL_STATIC_LIBRARIES += libnl_2
LOCAL_LDLIBS := -llog 
include $(BUILD_EXECUTABLE)
