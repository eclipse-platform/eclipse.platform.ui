#include <jni.h>
#include <io.h>
#include <sys/stat.h>
#include <windows.h>
#include "core.h"

/*
 * Converts a FILETIME in a java long (milliseconds).
 */
jlong fileTimeToMillis(FILETIME ft) {

	ULONGLONG millis = (((ULONGLONG) ft.dwHighDateTime) << 32) + ft.dwLowDateTime;
	millis = millis / 10000;
	// difference in milliseconds between
	// January 1, 1601 00:00:00 UTC (Windows FILETIME)
	// January 1, 1970 00:00:00 UTC (Java long)
	// = 11644473600000
	millis -= 11644473600000;
	return millis;
}

/*
 * Class:     org_eclipse_core_internal_localstore_CoreFileSystemLibrary
 * Method:    internalGetStat
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalGetStat
   (JNIEnv *env, jclass clazz, jstring target) {

	HANDLE handle;
	WIN32_FIND_DATA info;
	jlong result;

	const char *fileName = (*env)->GetStringUTFChars(env, target, NULL);
	handle = FindFirstFile(fileName, &info);
	if (handle == INVALID_HANDLE_VALUE) {
		FindClose(handle);
		return 0;
	}
	(*env)->ReleaseStringUTFChars(env, target, fileName);

	// select interesting information
	// lastModified
	result = fileTimeToMillis(info.ftLastWriteTime); // lower bits
	// valid stat
	result |= STAT_VALID;
	// folder or file?
	if (info.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
		result |= STAT_FOLDER;
	// read-only?
	if (info.dwFileAttributes & FILE_ATTRIBUTE_READONLY)
		result |= STAT_READ_ONLY;

	FindClose(handle);
	return result;
}

/*
 * Class:     org_eclipse_core_internal_localstore_CoreFileSystemLibrary
 * Method:    internalSetReadOnly
 * Signature: (Ljava/lang/String;Z)V
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalSetReadOnly
   (JNIEnv *env, jclass clazz, jstring target, jboolean readOnly) {

	int mode;
	int code; 
	const char *fileName = (*env)->GetStringUTFChars(env, target, 0);
	if (readOnly)
		mode = S_IREAD;
	else
		mode = S_IWRITE;
	code = chmod(fileName, mode);
	(*env)->ReleaseStringUTFChars(env, target, fileName);
	return code != -1;
}
