/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat Incorporated - get/setResourceAttribute code
 *******************************************************************************/
#include <jni.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <utime.h>
#include <stdlib.h>
#include <string.h>
#include "../localfile.h"
#include <os_custom.h>

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
	memcpy(result, temp, n * sizeof(jbyte));
	result[n] = '\0';
	(*env)->ReleaseByteArrayElements(env, target, temp, 0);
	return result;
}

/*
 * Class:     org_eclipse_core_internal_filesystem_local_LocalFileNatives
 * Method:    internalIsUnicode
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_filesystem_local_LocalFileNatives_internalIsUnicode
  (JNIEnv *env, jclass clazz) {
  	// no specific support for Unicode-based file names on *nix
	return JNI_FALSE;
}

/*
 * Converts a stat structure to IFileInfo 
 */
jboolean convertStatToFileInfo (JNIEnv *env, struct stat info, jobject fileInfo) {
    jclass cls;
    jmethodID mid;

    cls = (*env)->GetObjectClass(env, fileInfo);
    if (cls == 0) return JNI_FALSE;

	// select interesting information
	//exists
    mid = (*env)->GetMethodID(env, cls, "setExists", "(Z)V");
    if (mid == 0) return JNI_FALSE;
    (*env)->CallVoidMethod(env, fileInfo, mid, JNI_TRUE);
	
	// last modified
    mid = (*env)->GetMethodID(env, cls, "setLastModified", "(J)V");
    if (mid == 0) return JNI_FALSE;
    (*env)->CallVoidMethod(env, fileInfo, mid, ((jlong) info.st_mtime) * 1000); /* lower bits */

	// file length
    mid = (*env)->GetMethodID(env, cls, "setLength", "(J)V");
    if (mid == 0) return JNI_FALSE;
    (*env)->CallVoidMethod(env, fileInfo, mid, (jlong)info.st_size);

	// folder or file?
	if ((info.st_mode & S_IFDIR) == S_IFDIR) {
	    mid = (*env)->GetMethodID(env, cls, "setAttribute", "(IZ)V");
	    if (mid == 0) return JNI_FALSE;
	    (*env)->CallVoidMethod(env, fileInfo, mid, ATTRIBUTE_DIRECTORY, JNI_TRUE);
    }

	// read-only?
	if ((info.st_mode & S_IWRITE) != S_IWRITE) {
	    mid = (*env)->GetMethodID(env, cls, "setAttribute", "(IZ)V");
	    if (mid == 0) return JNI_FALSE;
	    (*env)->CallVoidMethod(env, fileInfo, mid, ATTRIBUTE_READ_ONLY, JNI_TRUE);
    }

	// executable?
    if ((info.st_mode & S_IXUSR) == S_IXUSR) {
	    mid = (*env)->GetMethodID(env, cls, "setAttribute", "(IZ)V");
	    if (mid == 0) return JNI_FALSE;
	    (*env)->CallVoidMethod(env, fileInfo, mid, ATTRIBUTE_EXECUTABLE, JNI_TRUE);
    }

	// sym link?
//    if ((info.st_mode & S_IFLNK) == S_IFLNK) {
//	    mid = (*env)->GetMethodID(env, cls, "setAttribute", "(IZ)V");
//	    if (mid == 0) return JNI_FALSE;
//	    (*env)->CallVoidMethod(env, fileInfo, mid, ATTRIBUTE_LINK, JNI_TRUE);
//    }
	return JNI_TRUE;
}

/*
 * Class:     org_eclipse_core_internal_filesystem_local_LocalFileNatives
 * Method:    internalGetFileInfo
 * Signature: ([CLorg/eclipse/core/filesystem/IFileInfo;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_filesystem_local_LocalFileNatives_internalGetFileInfo
   (JNIEnv *env, jclass clazz, jbyteArray target, jobject fileInfo) {
	struct stat info;
	jlong result;
	jint code;
	jbyte *name;

	/* get stat */
	name = getByteArray(env, target);
	code = stat((const char*)name, &info);
	free(name);

	/* test if an error occurred */
	if (code == -1)
	  return 0;
	return convertStatToFileInfo(env, info, fileInfo);
}

/*
 * Class:     org_eclipse_core_internal_filesystem_local_LocalFileNatives
 * Method:    internalGetFileInfoW
 * Signature: ([CLorg/eclipse/core/filesystem/IFileInfo;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_filesystem_local_LocalFileNatives_internalGetFileInfoW
   (JNIEnv *env, jclass clazz, jcharArray target, jobject fileInfo) {
	// shouldn't ever be called - there is no Unicode-specific calls on *nix
	return JNI_FALSE;
}

/*
 * Class:     org_eclipse_core_internal_filesystem_local_LocalFileNatives
 * Method:    internalCopyAttributes
 * Signature: ([B[BZ)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_filesystem_local_LocalFileNatives_internalCopyAttributes
   (JNIEnv *env, jclass clazz, jbyteArray source, jbyteArray destination, jboolean copyLastModified) {

  struct stat info;
  struct utimbuf ut;
  jbyte *sourceFile, *destinationFile;
  jint code;

  sourceFile = getByteArray(env, source);
  destinationFile = getByteArray(env, destination);

  code = stat((const char*)sourceFile, &info);
  if (code == 0) {
    code = chmod((const char*)destinationFile, info.st_mode);
    if (code == 0 && copyLastModified) {
      ut.actime = info.st_atime;
      ut.modtime = info.st_mtime;
      code = utime((const char*)destinationFile, &ut);
    }
  }

  free(sourceFile);
  free(destinationFile);
  return code != -1;
}

/*
 * Class:     org_eclipse_core_internal_filesystem_local_LocalFileNatives
 * Method:    internalCopyAttributesW
 * Signature: ([C[CZ)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_filesystem_local_LocalFileNatives_internalCopyAttributesW
  (JNIEnv *env, jclass clazz, jcharArray source, jcharArray destination, jboolean copyLastModified) {
	// shouldn't ever be called - there is no Unicode-specific calls on *nix
	return JNI_FALSE;
}  

/*
 * Class:     org_eclipse_core_internal_filesystem_local_LocalFileNatives
 * Method:    internalSetFileInfo
 * Signature: ([BLorg/eclipse/core/filesystem/IFileInfo;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_filesystem_local_LocalFileNatives_internalSetFileInfo
  (JNIEnv *env, jclass clazz, jcharArray target, jobject obj) {

    int mask;
    struct stat info;
    jbyte *name;
    jint code = -1;
    jmethodID mid;
    jboolean executable, readOnly;
    jclass cls;

    /* find out if we need to set the readonly bit */
    cls = (*env)->GetObjectClass(env, obj);
    mid = (*env)->GetMethodID(env, cls, "getAttribute", "(I)Z");
    if (mid == 0) goto fail;
    readOnly = (*env)->CallBooleanMethod(env, obj, mid, ATTRIBUTE_READ_ONLY);

    /* find out if we need to set the executable bit */
    executable = (*env)->CallBooleanMethod(env, obj, mid, ATTRIBUTE_EXECUTABLE);

    /* get the current permissions */
    name = getByteArray(env, target);
    code = stat((const char*)name, &info);
    
    /* create the mask */
    mask = S_IRUSR |
	       S_IWUSR |
	       S_IXUSR |
           S_IRGRP |
           S_IWGRP |
           S_IXGRP |
           S_IROTH |
           S_IWOTH |
           S_IXOTH;
    mask &= info.st_mode;
    if (executable)
	    mask |= S_IXUSR;
    else
	    mask &= ~(S_IXUSR | S_IXGRP | S_IXOTH);
	if (readOnly)
	    mask &= ~(S_IWUSR | S_IWGRP | S_IWOTH);
	else
	    mask |= (S_IRUSR | S_IWUSR);
    
    /* write the permissions */
    code = chmod((const char*)name, mask);

fail:
	if (name) free(name);
    return code != -1;
}

/*
 * Class:     org_eclipse_core_internal_filesystem_local_LocalFileNatives
 * Method:    internalSetFileInfoW
 * Signature: ([BLorg/eclipse/core/filesystem/IFileInfo;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_filesystem_local_LocalFileNatives_internalSetFileInfoW
  (JNIEnv *env, jclass clazz, jcharArray target, jobject obj, jint options) {
	// shouldn't ever be called - there is no Unicode-specific calls on *nix
	return JNI_FALSE;
}
