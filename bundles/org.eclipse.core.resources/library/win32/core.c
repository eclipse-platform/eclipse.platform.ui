/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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
 * Signature: ([B)J
 */
JNIEXPORT jlong JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalGetStat
   (JNIEnv *env, jclass clazz, jbyteArray target) {

	HANDLE handle;
	WIN32_FIND_DATA info;
	jlong result;
	jbyte *fileName, *name;
	jsize n;

	fileName = (*env)->GetByteArrayElements(env, target, 0);

	n = (*env)->GetArrayLength(env, target);
	name = malloc((n+1) * sizeof(jbyte));
	memcpy(name, fileName, n);
	name[n] = '\0';

	handle = FindFirstFile(name, &info);
	if (handle == INVALID_HANDLE_VALUE) {
		FindClose(handle);
		return 0;
	}
	(*env)->ReleaseByteArrayElements(env, target, fileName, 0);

	free(name);
	
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
 * Signature: ([BZ)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalSetReadOnly
   (JNIEnv *env, jclass clazz, jbyteArray target, jboolean readOnly) {

	int code, mode;
	jbyte *fileName, *name;
	jsize n;
	
	fileName = (*env)->GetByteArrayElements(env, target, 0);

	n = (*env)->GetArrayLength(env, target);
	name = malloc((n+1) * sizeof(jbyte));
	memcpy(name, fileName, n);
	name[n] = '\0';

	if (readOnly)
		mode = S_IREAD;
	else
		mode = S_IWRITE;
	code = chmod(name, mode);
	(*env)->ReleaseByteArrayElements(env, target, fileName, 0);
	free(name);
			
	return code != -1;
}