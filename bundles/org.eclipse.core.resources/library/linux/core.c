/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
#include <jni.h>
#include <sys/io.h>
#include <sys/stat.h>
#include "core.h"

/*
 * Class:     org_eclipse_core_internal_localstore_CoreFileSystemLibrary
 * Method:    internalGetStat
 * Signature: ([B)J
 */
JNIEXPORT jlong JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalGetStat
   (JNIEnv *env, jclass clazz, jbyteArray target) {

	struct stat info;
	jlong result;
	jint code;
	jbyte *fileName, *name;
	jsize n;

	// get stat
	fileName = (*env)->GetByteArrayElements(env, target, 0);
	
	n = (*env)->GetArrayLength(env, target);
	name = malloc((n+1) * sizeof(jbyte));
	memcpy(name, fileName, n);
	name[n] = '\0';

	code = stat(name, &info);
	(*env)->ReleaseByteArrayElements(env, target, fileName, 0);
	free(name);

	// test if an error occurred
	if (code == -1)
	  return 0;

	// filter interesting bits
	// lastModified
	result = ((jlong) info.st_mtime) * 1000; // lower bits
	// valid stat
	result |= STAT_VALID;
	// is folder?
	if ((info.st_mode & S_IFDIR) == S_IFDIR)
		result |= STAT_FOLDER;
	// is read-only?
	if ((info.st_mode & S_IWRITE) != S_IWRITE)
		result |= STAT_READ_ONLY;

	return result;
}

/*
 * Class:     org_eclipse_core_internal_localstore_CoreFileSystemLibrary
 * Method:    internalSetReadOnly
 * Signature: ([BZ)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalSetReadOnly
   (JNIEnv *env, jclass clazz, jbyteArray target, jboolean readOnly) {

	int mask;
	struct stat info;
	jbyte *fileName, *name;
	jsize n;
	jint code;

        fileName = (*env)->GetByteArrayElements(env, target, 0);

	n = (*env)->GetArrayLength(env, target);
	name = malloc((n+1) * sizeof(jbyte));
	memcpy(name, fileName, n);
	name[n] = '\0';

	code = stat(name, &info);
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

	if (readOnly)
		mask &= ~(S_IWUSR | S_IWGRP | S_IWOTH);
	else
		mask |= (S_IRUSR | S_IWUSR);

	code = chmod(name, mask);
	(*env)->ReleaseByteArrayElements(env, target, fileName, 0);
	free(name);
	return code != -1;
}













































