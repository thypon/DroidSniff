LOCAL_PATH := $(call my-dir)
MY_LOCAL_PATH := $(LOCAL_PATH)
include $(CLEAR_VARS)

LOCAL_MODULE := droidsniff

LOCAL_SRC_FILES:=\
    droidsniff.c
                   
APP_OPTIM := release
LOCAL_C_INCLUDES := libpcap libnet/include include
LOCAL_STATIC_LIBRARIES := libpcap libnet
include $(BUILD_EXECUTABLE)