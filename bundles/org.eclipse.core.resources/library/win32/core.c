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
 * Get a null-terminated byte array from a java byte array.
 * The returned bytearray needs to be freed whe not used
 * anymore. Use free(result) to do that.
 */
jbyte* getByteArray(JNIEnv *env, jbyteArray target) {
	jsize n;
	jbyte *temp, *result;
	
	temp = (*env)->GetByteArrayElements(env, target, 0);
	n = (*env)->GetArrayLength(env, target);
	result = malloc((n+1) * sizeof(jbyte));
	memcpy(result, temp, n);
	result[n] = '\0';
	(*env)->ReleaseByteArrayElements(env, target, temp, 0);
	return result;
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
	jlong result = 0; // 0 = failed
	jbyte *name;

	name = getByteArray(env, target);
	handle = FindFirstFile(name, &info);

	if (handle != INVALID_HANDLE_VALUE) {
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
	}

	free(name);
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
	jbyte *name;

	name = getByteArray(env, target);
	if (readOnly)
		mode = S_IREAD;
	else
		mode = S_IWRITE;
	code = chmod(name, mode);

	free(name);
	return code != -1;
}

/*
 * Class:     org_eclipse_core_internal_localstore_CoreFileSystemLibrary
 * Method:    internalCopyAttributes
 * Signature: ([B[BZ)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalCopyAttributes
   (JNIEnv *env, jclass clazz, jbyteArray source, jbyteArray destination, jboolean copyLastModified) {

	HANDLE handle;
	WIN32_FIND_DATA info;
	jbyte *sourceFile, *destinationFile;
	int success = 1;

	sourceFile = getByteArray(env, source);
	destinationFile = getByteArray(env, destination);

	handle = FindFirstFile(sourceFile, &info);
	if (handle != INVALID_HANDLE_VALUE) {
		success = SetFileAttributes(destinationFile, info.dwFileAttributes);
		if (success != 0 && copyLastModified) {
			success = SetFileTime(destinationFile, &info.ftCreationTime, &info.ftLastAccessTime, &info.ftLastWriteTime);
		}
	} else {
		success = 0;
	}

	free(sourceFile);
	free(destinationFile);
	FindClose(handle);
	return success;
}

/*
 * Class:     org_eclipse_ant_core_EclipseProject
 * Method:    internalCopyAttributes
 * Signature: ([B[BZ)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_ant_core_EclipseFileUtils_internalCopyAttributes
   (JNIEnv *env, jclass clazz, jbyteArray source, jbyteArray destination, jboolean copyLastModified) {

	// use the same implementation for both methods
	return Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalCopyAttributes
			(env, clazz, source, destination, copyLastModified);
}