MY_OLD_PATH := $(call my-dir)
LOCAL_PATH := $(MY_OLD_PATH)

include $(LOCAL_PATH)/libnet/Android.mk

LOCAL_PATH := $(MY_OLD_PATH)

include $(LOCAL_PATH)/libpcap/Android.mk

LOCAL_PATH := $(MY_OLD_PATH)

include $(LOCAL_PATH)/droidsniff/Android.mk

LOCAL_PATH := $(MY_OLD_PATH)

include $(LOCAL_PATH)/arpspoof/Android.mk