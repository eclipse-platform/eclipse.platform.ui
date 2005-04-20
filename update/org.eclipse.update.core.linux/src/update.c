/**********************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 **********************************************************************/

/* bug 82520 : need to include stdlib.h */
# include <stdlib.h>
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

	cls = (*jnienv) -> GetObjectClass(jnienv, file);
	id = (*jnienv) -> GetMethodID(jnienv, cls, "getAbsolutePath", "()Ljava/lang/String;");
	obj = (*jnienv) -> CallObjectMethod(jnienv, file, id);
	lpDirectoryName = (*jnienv) -> GetStringUTFChars(jnienv, (jstring) obj, 0);

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
	cls = (*jnienv) -> GetObjectClass(jnienv, file);
	id = (*jnienv) -> GetMethodID(jnienv, cls, "getAbsolutePath", "()Ljava/lang/String;");
	obj = (*jnienv) -> CallObjectMethod(jnienv, file, id);
	lpDirectoryName = (*jnienv) -> GetStringUTFChars(jnienv, (jstring) obj, 0);

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
	cls = (*jnienv) -> GetObjectClass(jnienv, file);
	id = (*jnienv) -> GetMethodID(jnienv, cls, "getAbsolutePath", "()Ljava/lang/String;");
	obj = (*jnienv) -> CallObjectMethod(jnienv, file, id);
	lpDirectoryName = (*jnienv) -> GetStringUTFChars(jnienv, (jstring) obj, 0);

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
	stringClass = (*jnienv) -> FindClass(jnienv, "java/lang/String");
	empty = (*jnienv) -> NewStringUTF(jnienv, "");
	//returnArray = (*jnienv) -> NewObjectArray(jnienv, nDrive, stringClass, empty);

	// for now return null as method is not implemented
	returnArray = NULL;

	int i;
	for (i = 0; i < drive; i++) {
		// Linux implementation, create String for each mount point

		str = (*jnienv) -> NewStringUTF(jnienv, driveName);
		(*jnienv) -> SetObjectArrayElement(jnienv, returnArray, index, str);

		index++;
	}

	return returnArray;
}
