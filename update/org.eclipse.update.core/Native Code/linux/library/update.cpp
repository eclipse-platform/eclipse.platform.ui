/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

# include <sys/types.h>
# include <sys/statfs.h>
# include <update.h>

/*
 * Class:     org_eclipse_update_configuration_LocalSystemInfo
 * Method:    nativeGetFreeSpace
 * Signature: (Ljava/io/File;)J
 */
JNIEXPORT jlong JNICALL Java_org_eclipse_update_configuration_LocalSystemInfo_nativeGetFreeSpace(
	JNIEnv * jnienv,
	jclass javaClass,
	jobject file) {

	// to retrive the String
	jclass cls;
	jmethodID id;
	jobject obj;

	// java.io.File.getAbsolutePath()
	const char * lpDirectoryName;

	// Linux Parameters
	struct statfs buffer;

	// the result
	jlong result = org_eclipse_update_configuration_LocalSystemInfo_SIZE_UNKNOWN;

	// first, obtain the Path from the java.io.File parameter
	cls = jnienv -> GetObjectClass(file);
	id = jnienv -> GetMethodID(cls, "getAbsolutePath", "()Ljava/lang/String;");
	obj = jnienv -> CallObjectMethod(file, id);
	lpDirectoryName = jnienv -> GetStringUTFChars((jstring) obj, 0);

	// cast one argument as jlong to have a jlong result
	int err = statfs(lpDirectoryName,&buffer);
	if (err==0){
		long size = buffer.f_bsize;
		jlong free = buffer.f_bfree;
		result = size*free;
	}

	return result;
}

/*
 * Class:     org_eclipse_update_configuration_LocalSystemInfo
 * Method:    nativeGetLabel
 * Signature: (Ljava/io/File;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_eclipse_update_configuration_LocalSystemInfo_nativeGetLabel(
	JNIEnv * jnienv,
	jclass javaClass,
	jobject file) {

	// to retrive the String
	jclass cls;
	jmethodID id;
	jobject obj;

	// java.io.File.getAbsolutePath()
	const char * lpDirectoryName;

	// obtain the String from the parameter
	cls = jnienv -> GetObjectClass(file);
	id = jnienv -> GetMethodID(cls, "getAbsolutePath", "()Ljava/lang/String;");
	obj = jnienv -> CallObjectMethod(file, id);
	lpDirectoryName = jnienv -> GetStringUTFChars((jstring) obj, 0);

	jstring result = NULL;

	// Linux implementation following

	return result;
}

/*
 * Class:     org_eclipse_update_configuration_LocalSystemInfo
 * Method:    nativeGetType
 * Signature: (Ljava/io/File;)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_update_configuration_LocalSystemInfo_nativeGetType(
	JNIEnv * jnienv,
	jclass javaClass,
	jobject file) {

	// to retrive the String
	jclass cls;
	jmethodID id;
	jobject obj;

	// java.io.File.getAbsolutePath()
	const char * lpDirectoryName;

	// obtain the String from the parameter
	cls = jnienv -> GetObjectClass(file);
	id = jnienv -> GetMethodID(cls, "getAbsolutePath", "()Ljava/lang/String;");
	obj = jnienv -> CallObjectMethod(file, id);
	lpDirectoryName = jnienv -> GetStringUTFChars((jstring) obj, 0);

	int result;
	
	// Linux implemantation

	result = org_eclipse_update_configuration_LocalSystemInfo_VOLUME_INVALID_PATH;
	return result;
}

/*
 * Class:     org_eclipse_update_configuration_LocalSystemInfo
 * Method:    nativeListMountPoints
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_org_eclipse_update_configuration_LocalSystemInfo_nativeListMountPoints(
	JNIEnv * jnienv,
	jclass javaClass) {

	//
	int drive;
	char driveName[100];
	jobjectArray returnArray;
	int nDrive = 0;

	// 
	jclass stringClass;
	jobject empty;
	int index = 0;
	jobject str;

	// Linux implementation
	// find mount points

	drive = 0;
	stringClass = jnienv -> FindClass("java/lang/String");
	empty = jnienv -> NewStringUTF("");
	//returnArray = jnienv -> NewObjectArray(nDrive, stringClass, empty);
	// for now return null as method is not implemented
	returnArray = NULL;

	for (int i = 0; i < drive; i++) {
		// Linux implementation, create String for each mount point

		str = jnienv -> NewStringUTF(driveName);
		jnienv -> SetObjectArrayElement(returnArray, index, str);
		index++;
	}

	return returnArray;
}
