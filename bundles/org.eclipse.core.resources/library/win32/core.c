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
 * The returned bytearray needs to be freed when not used
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
 * Method:    internalIsUnicode
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalIsUnicode
  (JNIEnv *env, jclass clazz) {
	HANDLE hModule;
  	OSVERSIONINFO osvi;
  	memset(&osvi, 0, sizeof(OSVERSIONINFO));
  	osvi.dwOSVersionInfoSize = sizeof(OSVERSIONINFO);
  	if (!GetVersionEx (&osvi)) 
    	return JNI_FALSE;
	// only Windows NT 4, Windows 2K and XP support Unicode API calls
    if (!(osvi.dwMajorVersion >= 5 || (osvi.dwPlatformId == VER_PLATFORM_WIN32_NT && osvi.dwMajorVersion == 4)))
		return JNI_FALSE;
	return JNI_TRUE;		
}

/*
 * Get a null-terminated short array from a java char array.
 * The returned short array needs to be freed when not used
 * anymore. Use free(result) to do that.
 */
jchar* getCharArray(JNIEnv *env, jcharArray target) {
	jsize n;
	jchar *temp, *result;
	
	temp = (*env)->GetCharArrayElements(env, target, 0);
	n = (*env)->GetArrayLength(env, target);
	result = malloc((n+1) * sizeof(jchar));
	memcpy(result, temp, n * 2);
	result[n] = 0;
	(*env)->ReleaseCharArrayElements(env, target, temp, 0);
	return result;
}

/*
 * Class:     org_eclipse_core_internal_localstore_CoreFileSystemLibrary
 * Method:    internalGetStatW
 * Signature: ([C)J
 */
JNIEXPORT jlong JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalGetStatW
   (JNIEnv *env, jclass clazz, jcharArray target) {
	int i;
	HANDLE handle;
	WIN32_FIND_DATAW info;
	jlong result = 0; // 0 = failed
	jchar *name;
	
	name = getCharArray(env, target);	
	
	handle = FindFirstFileW(name, &info);
	
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
 * Method:    internalSetReadOnlyW
 * Signature: ([CZ)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalSetReadOnlyW
   (JNIEnv *env, jclass clazz, jcharArray target, jboolean readOnly) {

	HANDLE handle;
	jchar *targetFile;
	int success = JNI_TRUE;
	DWORD attributes;

	targetFile = getCharArray(env, target);

	attributes = GetFileAttributesW(targetFile);

	if (readOnly)
		attributes = attributes | FILE_ATTRIBUTE_READONLY;
	else
		attributes = attributes & ~FILE_ATTRIBUTE_READONLY;
	
	success = SetFileAttributesW(targetFile, attributes);

	free(targetFile);
	return success;
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
 * Class:     org_eclipse_core_internal_localstore_CoreFileSystemLibrary
 * Method:    internalCopyAttributesW
 * Signature: ([C[CZ)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalCopyAttributesW
  (JNIEnv *env, jclass clazz, jcharArray source, jcharArray destination, jboolean copyLastModified) {

	HANDLE handle;
	WIN32_FIND_DATAW info;
	jchar *sourceFile, *destinationFile;
	int success = 1;

	sourceFile = getCharArray(env, source);
	destinationFile = getCharArray(env, destination);

	handle = FindFirstFileW(sourceFile, &info);
	
	if (handle != INVALID_HANDLE_VALUE) {
		success = SetFileAttributesW(destinationFile, info.dwFileAttributes);
		if (success != 0 && copyLastModified) {
			// does not honor copyLastModified
			// call to SetFileTime should pass file handle instead of file name
			// success = SetFileTime(destinationFile, &info.ftCreationTime, &info.ftLastAccessTime, &info.ftLastWriteTime);
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
