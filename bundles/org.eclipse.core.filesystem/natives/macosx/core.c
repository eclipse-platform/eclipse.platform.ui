/*
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * 	  Andre Weinand (OTI Labs)
 */
#include <jni.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <utime.h>
#include <stdlib.h>
#include <string.h>
#include "core.h"

#include <CoreServices/CoreServices.h>

#define USE_IMMUTABLE_FLAG 1
#define USE_ARCHIVE_FLAG 0

/*
 * Get a null-terminated byte array from a java char array.
 * The byte array contains UTF8 encoding.
 * The returned bytearray needs to be freed when not used
 * anymore. Use free(result) to do that.
 */
static jbyte* getUTF8ByteArray(JNIEnv *env, jcharArray target) {

	jchar *temp= (*env)->GetCharArrayElements(env, target, 0);
	jsize n= (*env)->GetArrayLength(env, target);
	
	CFStringRef sr= CFStringCreateWithCharacters(kCFAllocatorDefault, temp, n); 
	CFIndex argStringSize= CFStringGetMaximumSizeForEncoding(n, kCFStringEncodingUTF8) + 1;
	jbyte *result= (jbyte*) calloc(argStringSize, sizeof(jbyte));
	CFStringGetCString(sr, result, argStringSize, kCFStringEncodingUTF8);
	CFRelease(sr);
	
	(*env)->ReleaseCharArrayElements(env, target, temp, 0);
	
	return result;
}


/*
 * Class:	 org_eclipse_core_internal_localstore_CoreFileSystemLibrary
 * Method:	internalGetStatW
 * Signature: ([C)J
 */
JNIEXPORT jlong JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalGetStatW
	(JNIEnv *env, jclass clazz, jcharArray target) {

	char *name= getUTF8ByteArray(env, target);
	struct stat info;
	jlong result= 0;

	/* test if an error occurred */
	if (stat(name, &info) == 0) {
		/* filter interesting bits */
		
		/* lastModified */
		result= ((jlong) info.st_mtime) * 1000; /* lower bits */
		
		/* valid stat */
		result |= STAT_VALID;
		
		/* is folder? */
		if ((info.st_mode & S_IFDIR) == S_IFDIR)
			result |= STAT_FOLDER;
			
		/* is read-only? */
		if ((info.st_mode & S_IWRITE) != S_IWRITE)
			result |= STAT_READ_ONLY;
#if USE_IMMUTABLE_FLAG
		else if ((info.st_flags & (UF_IMMUTABLE | SF_IMMUTABLE)) != 0)
			result |= STAT_READ_ONLY;
#endif
	}
	free(name);
	return result;
}

/*
 * Class:	 org_eclipse_core_internal_localstore_CoreFileSystemLibrary
 * Method:	internalGetStat
 * Signature: ([B)J
 */
JNIEXPORT jlong JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalGetStat
	(JNIEnv *env, jclass clazz, jbyteArray target) {
	
	// shouldn't ever be called - there is only a Unicode-specific call on MacOS X
	return JNI_FALSE;
}

/*
 * Class:	 org_eclipse_core_internal_localstore_CoreFileSystemLibrary
 * Method:	internalIsUnicode
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalIsUnicode
	(JNIEnv *env, jclass clazz) {
	
	return JNI_TRUE;	// MacOS X supports Unicode-based file names in UTF-8 encoding
}

/*
 * Class:	 org_eclipse_core_internal_localstore_CoreFileSystemLibrary
 * Method:	internalCopyAttributes
 * Signature: ([B[BZ)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalCopyAttributes
(JNIEnv *env, jclass clazz, jbyteArray source, jbyteArray destination, jboolean copyLastModified) {

	// shouldn't ever be called - there is only a Unicode-specific call on MacOS X
	return JNI_FALSE;   
}

/*
 * Copies the file's 'mode', 'flags', and optionally the access and modification dates 
 * from source to destination.
 * Arguments 'sourceFile' and 'destinationFile' are expected to be in UTF8 encoding.
 * Arguments 'sourceFile' and 'destinationFile' are released with 'free()'.
 * Returns true on success.
 *
 * Class:	 org_eclipse_core_internal_localstore_CoreFileSystemLibrary
 * Method:	internalCopyAttributesW
 * Signature: ([C[CZ)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalCopyAttributesW
  (JNIEnv *env, jclass clazz, jcharArray source, jcharArray destination, jboolean copyLastModified) {
  
	char *sourceFile= getUTF8ByteArray(env, source);
	char *destinationFile= getUTF8ByteArray(env, destination);

	struct stat info;
	struct utimbuf ut;
	int code;

	code= stat(sourceFile, &info);
	if (code == 0) {
		code= chmod(destinationFile, info.st_mode);
		if (code == 0) {
			chflags(destinationFile, info.st_flags);	// ignore return code
			if (copyLastModified) {
	  			ut.actime= info.st_atime;
	  			ut.modtime= info.st_mtime;
	  			code= utime(destinationFile, &ut);
	  		}
		}
  	}
  	
	free(sourceFile);
	free(destinationFile);

	return code == 0;
}

/*
 * Class:	 org_eclipse_ant_core_EclipseProject
 * Method:	internalCopyAttributes
 * Signature: ([B[BZ)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_ant_core_EclipseFileUtils_internalCopyAttributes
   (JNIEnv *env, jclass clazz, jbyteArray source, jbyteArray destination, jboolean copyLastModified) {

	/* use the same implementation for both methods */
	return Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalCopyAttributes(env, clazz, source, destination, copyLastModified);
}

/*
 * Class:	 org_eclipse_core_internal_localstore_CoreFileSystemLibrary
 * Method:	internalSetResourceAttributesW
 * Signature: ([CLorg/eclipse/core/internal/resources/ResourceAttributes;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalSetResourceAttributesW
  (JNIEnv *env, jclass clazz, jcharArray target, jobject obj) {
  
	jclass cls= (*env)->GetObjectClass(env, obj);
	
	/* find out if we need to set the execute bit */
	jmethodID mid= (*env)->GetMethodID(env, cls, "isExecutable", "()Z");
	if (mid == 0)
		return JNI_FALSE;
	jboolean executable= (*env)->CallBooleanMethod(env, obj, mid);
	
	/* find out if we need to set the readonly bits */
	mid= (*env)->GetMethodID(env, cls, "isReadOnly", "()Z");
	if (mid == 0)
		return JNI_FALSE;
	jboolean readOnly= (*env)->CallBooleanMethod(env, obj, mid);
	
#if USE_ARCHIVE_FLAG
	/* find out if we need to set the archive bit */
	mid= (*env)->GetMethodID(env, cls, "isArchive", "()Z");
	if (mid == 0)
		return JNI_FALSE;
	jboolean archive= (*env)->CallBooleanMethod(env, obj, mid);
#endif
	
	/* get the current permissions */
	jbyte *name= getUTF8ByteArray(env, target);
	struct stat info;
	int result= stat(name, &info);
	if (result == 0) {
		/* create the mask for the relevant bits */
		int mask= info.st_mode & (S_IRWXU | S_IRWXG | S_IRWXO);
		int oldmask= mask;
		int flags= info.st_flags;
			
#if USE_ARCHIVE_FLAG
		if (archive)
			flags |= SF_ARCHIVED;						// set archive bit
		else
			flags &= ~SF_ARCHIVED;					// clear archive bit
#endif

		if (executable)
			mask |= S_IXUSR;							// set 'x' only for user
		else
			mask &= ~(S_IXUSR | S_IXGRP | S_IXOTH);	// clear all 'x'
			
		if (readOnly) {
			mask &= ~(S_IWUSR | S_IWGRP | S_IWOTH);	// clear all 'w'		
#if USE_IMMUTABLE_FLAG
			flags |= UF_IMMUTABLE;					// set immutable flag for usr
#endif
		} else {
			mask |= (S_IRUSR | S_IWUSR);				// set 'r' and 'w' for user
#if USE_IMMUTABLE_FLAG
			flags &= ~UF_IMMUTABLE;					// try to clear immutable flags for usr
#endif
		}	

		/* call chmod & chflags syscalls in correct order */
		if (readOnly) {
			if (mask != oldmask)
				result |= chmod(name, mask);
			if (flags != info.st_flags)
				result |= chflags(name, flags);
		} else {
			if (flags != info.st_flags)
				result |= chflags(name, flags);
			if (mask != oldmask)
				result |= chmod(name, mask);
		}	
	}
	
	free(name);
	return result == 0;
}

/*
 * Class:	 org_eclipse_core_internal_localstore_CoreFileSystemLibrary
 * Method:	internalSetResourceAttributes
 * Signature: ([BLorg/eclipse/core/internal/resources/ResourceAttributes;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalSetResourceAttributes
  (JNIEnv *env, jclass clazz, jbyteArray target, jobject obj) {
  
	// shouldn't ever be called - there is only a Unicode-specific call on MacOS X
	return JNI_FALSE;   
}

/*
 * Class:	 org_eclipse_core_internal_localstore_CoreFileSystemLibrary
 * Method:	internalGetResourceAttributesW
 * Signature: ([CLorg/eclipse/core/internal/resources/ResourceAttributes;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalGetResourceAttributesW
  (JNIEnv *env, jclass clazz, jcharArray target, jobject obj) {

	struct stat info;
	jboolean success= JNI_FALSE;
	
	/* get the current permissions */
	jbyte *name= getUTF8ByteArray(env, target);
	if (stat(name, &info) == 0) {
		jboolean archive, executable, readOnly;

#if USE_ARCHIVE_FLAG
		/* is archive? */
		archive= JNI_FALSE;
		if ((info.st_flags & SF_ARCHIVED) == SF_ARCHIVED)
			archive= JNI_TRUE;
#endif

		/* is executable? */
		executable= JNI_FALSE;
		if ((info.st_mode & S_IXUSR) == S_IXUSR)
			executable= JNI_TRUE;
		
		/* is read-only? */
		readOnly= JNI_FALSE;
		if ((info.st_mode & S_IWUSR) != S_IWUSR)
			readOnly= JNI_TRUE;
#if USE_IMMUTABLE_FLAG
		else if ((info.st_flags & (UF_IMMUTABLE | SF_IMMUTABLE)) != 0)
			readOnly= JNI_TRUE;
#endif

		success= JNI_TRUE;
		
		/* set the values in ResourceAttribute */
		jclass cls= (*env)->GetObjectClass(env, obj);
		
		jmethodID mid= (*env)->GetMethodID(env, cls, "setExecutable", "(Z)V");
		if (mid == 0)
			success= JNI_FALSE;
		else
			(*env)->CallVoidMethod(env, obj, mid, executable);
		
		mid= (*env)->GetMethodID(env, cls, "setReadOnly", "(Z)V");
		if (mid == 0)
			success= JNI_FALSE;
		else
			(*env)->CallVoidMethod(env, obj, mid, readOnly);
			
#if USE_ARCHIVE_FLAG
		mid= (*env)->GetMethodID(env, cls, "setArchive", "(Z)V");
		if (mid == 0)
			success= JNI_FALSE;
		else
			(*env)->CallVoidMethod(env, obj, mid, archive);
#endif
	}
	
	free(name);
	return success;
}

/*
 * Class:	 org_eclipse_core_internal_localstore_CoreFileSystemLibrary
 * Method:	internalGetResourceAttributes
 * Signature: ([BLorg/eclipse/core/internal/resources/ResourceAttributes;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalGetResourceAttributes
  (JNIEnv *env, jclass clazz, jbyteArray target, jobject obj) {
  
	// shouldn't ever be called - there is only a Unicode-specific call on MacOS X
	return JNI_FALSE;
}
