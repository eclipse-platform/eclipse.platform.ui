/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	  Andre Weinand (OTI Labs)
 *******************************************************************************/

#include <jni.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <utime.h>
#include <stdlib.h>
#include <string.h>

#include "../localfile.h"

#include <CoreServices/CoreServices.h>

#define USE_IMMUTABLE_FLAG 1
#define USE_ARCHIVE_FLAG 0
#define EFS_SYMLINK_SUPPORT 1

mode_t process_umask;

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
	CFStringGetCString(sr, (char*) result, argStringSize, kCFStringEncodingUTF8);
	CFRelease(sr);
	
	(*env)->ReleaseCharArrayElements(env, target, temp, 0);
	
	return result;
}

/*
 * Class:     org_eclipse_core_internal_filesystem_local_LocalFileNatives
 * Method:    nativeAttributes
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_core_internal_filesystem_local_LocalFileNatives_nativeAttributes
  (JNIEnv *env, jclass clazz) {
#if defined(EFS_SYMLINK_SUPPORT)
    return ATTRIBUTE_READ_ONLY | ATTRIBUTE_EXECUTABLE | ATTRIBUTE_SYMLINK | ATTRIBUTE_LINK_TARGET;
#else
    return ATTRIBUTE_READ_ONLY | ATTRIBUTE_EXECUTABLE;
#endif
}

/*
 * Class:     org_eclipse_core_internal_filesystem_local_LocalFileNatives
 * Method:    internalIsUnicode
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_filesystem_local_LocalFileNatives_internalIsUnicode
  (JNIEnv *env, jclass clazz) {
	return JNI_TRUE;	// MacOS X supports Unicode-based file names in UTF-8 encoding
}

/*
 * Converts a stat structure to IFileInfo 
 */
jboolean convertStatToFileInfo (JNIEnv *env, struct stat info, jobject fileInfo) {
    jclass cls;
    jmethodID mid;
    jboolean readOnly;

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
	if (S_ISDIR(info.st_mode)) {
	    mid = (*env)->GetMethodID(env, cls, "setAttribute", "(IZ)V");
	    if (mid == 0) return JNI_FALSE;
	    (*env)->CallVoidMethod(env, fileInfo, mid, ATTRIBUTE_DIRECTORY, JNI_TRUE);
    }

	// read-only?
	readOnly = (info.st_mode & S_IWRITE) != S_IWRITE;
#if USE_IMMUTABLE_FLAG
	if (!readOnly && ((info.st_flags & (UF_IMMUTABLE | SF_IMMUTABLE)) != 0))
		readOnly = true;
#endif
	if (readOnly) {
	    mid = (*env)->GetMethodID(env, cls, "setAttribute", "(IZ)V");
	    if (mid == 0) return JNI_FALSE;
	    (*env)->CallVoidMethod(env, fileInfo, mid, ATTRIBUTE_READ_ONLY, JNI_TRUE);
    }

#if USE_ARCHIVE_FLAG
	// archive?
	if ((info.st_flags & SF_ARCHIVED) == SF_ARCHIVED) {
	    mid = (*env)->GetMethodID(env, cls, "setAttribute", "(IZ)V");
	    if (mid == 0) return JNI_FALSE;
	    (*env)->CallVoidMethod(env, fileInfo, mid, ATTRIBUTE_ARCHIVE, JNI_TRUE);
	}
#endif

	// executable?
    if ((info.st_mode & S_IXUSR) == S_IXUSR) {
	    mid = (*env)->GetMethodID(env, cls, "setAttribute", "(IZ)V");
	    if (mid == 0) return JNI_FALSE;
	    (*env)->CallVoidMethod(env, fileInfo, mid, ATTRIBUTE_EXECUTABLE, JNI_TRUE);
    }
	return JNI_TRUE;
}

#if defined(EFS_SYMLINK_SUPPORT)
/*
 * Set symbolic link information in IFileInfo 
 */
jboolean setSymlinkInFileInfo (JNIEnv *env, jobject fileInfo, jstring linkTarget) {
    jclass cls;
    jmethodID mid;

    cls = (*env)->GetObjectClass(env, fileInfo);
    if (cls == 0) return JNI_FALSE;

    // set symlink attribute
    mid = (*env)->GetMethodID(env, cls, "setAttribute", "(IZ)V");
    if (mid == 0) return JNI_FALSE;
    (*env)->CallVoidMethod(env, fileInfo, mid, ATTRIBUTE_SYMLINK, JNI_TRUE);

    // set link target
    mid = (*env)->GetMethodID(env, cls, "setStringAttribute", "(ILjava/lang/String;)V");
    if (mid == 0) return JNI_FALSE;
    (*env)->CallVoidMethod(env, fileInfo, mid, ATTRIBUTE_LINK_TARGET, linkTarget);

    return JNI_TRUE;
}
#endif

/*
 * Class:     org_eclipse_core_internal_filesystem_local_LocalFileNatives
 * Method:    internalGetFileInfo
 * Signature: ([CLorg/eclipse/core/filesystem/IFileInfo;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_filesystem_local_LocalFileNatives_internalGetFileInfo
   (JNIEnv *env, jclass clazz, jbyteArray target, jobject fileInfo) {
	// shouldn't ever be called - there is only a Unicode-specific call on MacOS X
	return JNI_FALSE;
}

/*
 * Class:     org_eclipse_core_internal_filesystem_local_LocalFileNatives
 * Method:    internalGetFileInfoW
 * Signature: ([CLorg/eclipse/core/filesystem/IFileInfo;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_filesystem_local_LocalFileNatives_internalGetFileInfoW
   (JNIEnv *env, jclass clazz, jcharArray target, jobject fileInfo) {

	struct stat info;
	jint code;
	jstring linkTarget = NULL;

	/* get stat */
	char *name= (char*) getUTF8ByteArray(env, target);
#if defined(EFS_SYMLINK_SUPPORT)
	//do an lstat first to see if it is a symbolic link
	code = lstat(name, &info);
	if (code == 0 && (S_ISLNK(info.st_mode))) {
		//symbolic link: read link target
		char buf[PATH_MAX+1];
		int len;
		len = readlink((const char*)name, buf, PATH_MAX);
		if (len>0) {
			buf[len]=0;
		} else {
			buf[0]=0;
		}
		// Mac OS encodes symlink target using UTF-8, ignoring platform default
		linkTarget = (*env)->NewStringUTF(env, buf);
		setSymlinkInFileInfo(env, fileInfo, linkTarget);

		//stat link target (will fail for broken links)
		code = stat((const char*)name, &info);
	}
#else
	code = stat(name, &info);
#endif
	free(name);

	/* test if an error occurred */
	if (code == -1)
	  return JNI_FALSE;
	return convertStatToFileInfo(env, info, fileInfo);
}

/*
 * Class:     org_eclipse_core_internal_filesystem_local_LocalFileNatives
 * Method:    internalCopyAttributes
 * Signature: ([B[BZ)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_filesystem_local_LocalFileNatives_internalCopyAttributes
   (JNIEnv *env, jclass clazz, jbyteArray source, jbyteArray destination, jboolean copyLastModified) {
	// shouldn't ever be called - there is only a Unicode-specific call on MacOS X
	return JNI_FALSE;   
}

/*
 * Class:     org_eclipse_core_internal_filesystem_local_LocalFileNatives
 * Method:    internalCopyAttributesW
 * Signature: ([C[CZ)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_filesystem_local_LocalFileNatives_internalCopyAttributesW
  (JNIEnv *env, jclass clazz, jcharArray source, jcharArray destination, jboolean copyLastModified) {

	struct stat info;
	struct utimbuf ut;
	int code;
	char *sourceFile= (char*) getUTF8ByteArray(env, source);
	char *destinationFile= (char*) getUTF8ByteArray(env, destination);

	code= stat(sourceFile, &info);
	if (code != 0) goto fail;
	code= chmod(destinationFile, info.st_mode);
	if (code != 0) goto fail;

	chflags(destinationFile, info.st_flags);	// ignore return code
	if (copyLastModified) {
		ut.actime= info.st_atime;
		ut.modtime= info.st_mtime;
		code= utime(destinationFile, &ut);
	}

fail:  	
	free(sourceFile);
	free(destinationFile);
	return code == 0;
}  

/*
 * Class:     org_eclipse_core_internal_filesystem_local_LocalFileNatives
 * Method:    internalSetFileInfo
 * Signature: ([BLorg/eclipse/core/filesystem/IFileInfo;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_filesystem_local_LocalFileNatives_internalSetFileInfo
  (JNIEnv *env, jclass clazz, jcharArray target, jobject obj) {
	// shouldn't ever be called - there is only a Unicode-specific call on MacOS X
	return JNI_FALSE;   

}

mode_t getumask() {
	// in case something goes wrong just return 63 which is 0077 in octals
	mode_t mask = 63;

	int fds[2];
	if (pipe(fds) == -1) {
		return mask;
	}

	pid_t child_pid;
	child_pid = fork();

	if (child_pid == 0) {
		// child process
		ssize_t bytes_written = 0;
		close(fds[0]);
		mask = umask(0);
		while (1) {
			ssize_t written = write(fds[1], &mask + bytes_written, sizeof(mask) - bytes_written);
			if (written == -1) {
				if (errno != EINTR) {
					break;
				}
			} else {
				bytes_written += written;
				if (bytes_written == sizeof(mask)) {
					break;
				}
			}
		}
		close(fds[1]);
		_exit(0);
	} else if (child_pid != -1) {
		// parent process, fork succeded
		int stat_loc;
		ssize_t bytes_read = 0;
		mode_t buf;
		close(fds[1]);
		while (1) {
			ssize_t b_read = read(fds[0], &buf + bytes_read, sizeof(buf) - bytes_read);
			if (b_read == -1) {
				if (errno != EINTR) {
					break;
				}
			} else {
				if (b_read == 0) {
					break;
				}
				bytes_read += b_read;
				if (bytes_read == sizeof(mask)) {
					break;
				}
			}
		}
		if (bytes_read == sizeof(mask)) {
			mask = buf;
		}
		close(fds[0]);
		waitpid(child_pid, &stat_loc, 0);
	} else {
		// parent process, fork failed
		close(fds[0]);
		close(fds[1]);
	}

	return mask;
}

/*
 * Class:     org_eclipse_core_internal_filesystem_local_LocalFileNatives
 * Method:    internalSetFileInfoW
 * Signature: ([BLorg/eclipse/core/filesystem/IFileInfo;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_filesystem_local_LocalFileNatives_internalSetFileInfoW
  (JNIEnv *env, jclass clazz, jcharArray target, jobject obj, jint options) {
    jint code = -1;
    jmethodID mid;
    jboolean executable, readOnly, archive;
    jclass cls;

    /* find out if we need to set the readonly bit */
    cls = (*env)->GetObjectClass(env, obj);
    mid = (*env)->GetMethodID(env, cls, "getAttribute", "(I)Z");
    if (mid == 0) goto fail;
    readOnly = (*env)->CallBooleanMethod(env, obj, mid, ATTRIBUTE_READ_ONLY);

    /* find out if we need to set the executable bit */
    executable = (*env)->CallBooleanMethod(env, obj, mid, ATTRIBUTE_EXECUTABLE);

    /* find out if we need to set the archive bit */
    archive = (*env)->CallBooleanMethod(env, obj, mid, ATTRIBUTE_ARCHIVE);

	/* get the current permissions */
	jbyte *name = getUTF8ByteArray(env, target);
    struct stat info;
	int result= stat((char*)name, &info);
	if (result != 0) goto fail;

	/* create the mask for the relevant bits */
	mode_t mask = info.st_mode & (S_IRWXU | S_IRWXG | S_IRWXO);
	mode_t oldmask = mask;
	int flags = info.st_flags;
			
#if USE_ARCHIVE_FLAG
	if (archive)
		flags |= SF_ARCHIVED;						// set archive bit
	else
		flags &= ~SF_ARCHIVED;					// clear archive bit
#endif

	if (executable)
		mask |= S_IXUSR | ((S_IXGRP | S_IXOTH) & ~process_umask);
	else
		mask &= ~(S_IXUSR | S_IXGRP | S_IXOTH);	// clear all 'x'
		
	if (readOnly) {
		mask &= ~(S_IWUSR | S_IWGRP | S_IWOTH);	// clear all 'w'		
#if USE_IMMUTABLE_FLAG
		flags |= UF_IMMUTABLE;					// set immutable flag for usr
#endif
	} else {
		mask |= S_IRUSR | S_IWUSR | ((S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH) & ~process_umask);
#if USE_IMMUTABLE_FLAG
		flags &= ~UF_IMMUTABLE;					// try to clear immutable flags for usr
#endif
	}	

	/* call chmod & chflags syscalls in correct order */
	if (readOnly) {
		if (mask != oldmask)
			result |= chmod((char*)name, mask);
		if (flags != info.st_flags)
			result |= chflags((char*)name, flags);
	} else {
		if (flags != info.st_flags)
			result |= chflags((char*)name, flags);
		if (mask != oldmask)
			result |= chmod((char*)name, mask);
	}	

fail:	
	free(name);
	return result == 0;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
	process_umask = getumask();
	return JNI_VERSION_1_1;
}
