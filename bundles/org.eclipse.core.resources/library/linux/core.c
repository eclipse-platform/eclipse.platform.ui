#include <jni.h>
#include <sys/io.h>
#include <sys/stat.h>
#include "core.h"

/*
 * Class:     org_eclipse_core_internal_localstore_CoreFileSystemLibrary
 * Method:    internalGetStat
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalGetStat
   (JNIEnv *env, jclass clazz, jstring target) {

	struct stat info;
	jlong result;
	// get stat
	const char *fileName = (*env)->GetStringUTFChars(env, target, 0);
	jint code = stat(fileName, &info);
	(*env)->ReleaseStringUTFChars(env, target, fileName);

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
 * Signature: (Ljava/lang/String;Z)V
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_core_internal_localstore_CoreFileSystemLibrary_internalSetReadOnly
   (JNIEnv *env, jclass clazz, jstring target, jboolean readOnly) {

	int mask;
	struct stat info;
	const char *fileName = (*env)->GetStringUTFChars(env, target, 0);
	jint code = stat(fileName, &info);
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

	code = chmod(fileName, mask);
	(*env)->ReleaseStringUTFChars(env, target, fileName);
	return code != -1;
}
