/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */


# include <update.h>
# include <windows.h>
typedef BOOL(WINAPI * P_GDFSE) 
		(LPCTSTR,
		PULARGE_INTEGER,
		PULARGE_INTEGER,
		PULARGE_INTEGER);

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

	// Windows Parameters
	__int64 i64FreeBytesAvailableToCaller;
	__int64 i64TotalNumberOfBytes;
	__int64 i64TotalNumberOfFreeBytes;
	P_GDFSE pFunction = NULL; // pointer to function if it exists

	// the result
	jlong result = org_eclipse_update_configuration_LocalSystemInfo_SIZE_UNKNOWN;

	// first, obtain the Path from the java.io.File parameter
	cls = jnienv -> GetObjectClass(file);
	id = jnienv -> GetMethodID(cls, "getAbsolutePath", "()Ljava/lang/String;");
	obj = jnienv -> CallObjectMethod(file, id);
	lpDirectoryName = jnienv -> GetStringUTFChars((jstring) obj, 0);

	/*
	Not available on early version of Win95
	GetDiskFreeSpaceEx(
			IN LPCSTR lpDirectoryName,
			OUT PULARGE_INTEGER lpFreeBytesAvailableToCaller,
			OUT PULARGE_INTEGER lpTotalNumberOfBytes,
			OUT PULARGE_INTEGER lpTotalNumberOfFreeBytes);*/

	pFunction =
		(P_GDFSE) GetProcAddress(GetModuleHandle("kernel32.dll"),
			"GetDiskFreeSpaceExA");

	if (pFunction) {
		int err =
			GetDiskFreeSpaceEx(
				lpDirectoryName,
				(PULARGE_INTEGER) & i64FreeBytesAvailableToCaller,
				(PULARGE_INTEGER) & i64TotalNumberOfBytes,
				(PULARGE_INTEGER) & i64TotalNumberOfFreeBytes);

		if (err) {
			result = (jlong) i64FreeBytesAvailableToCaller;
		}
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

	jstring result;
	// Make sure we have a String of the Form: <letter>:
	if (':' == lpDirectoryName[1]) {
		char driveLetter[4]; // i.e. -> C:\\
		char buf[128];
		memcpy(driveLetter, lpDirectoryName, 2);
		strcpy(driveLetter + 2, "\\");

		/*
		 * Get the volume name.
		 */
		GetVolumeInformation(
			driveLetter,
			buf,
			sizeof(buf) - 1,
			NULL,
			NULL,
			NULL,
			NULL,
			0);
		result = jnienv -> NewStringUTF(buf);
	} else {
		result = (jstring) "wrong";
	}

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
	// Make sure we have a String of the Form: <letter>:
	if (':' == lpDirectoryName[1]) {
		char driveLetter[4]; //C:\\
		memcpy(driveLetter, lpDirectoryName, 2);
		strcpy(driveLetter + 2, "\\");

		switch (GetDriveType(driveLetter)) {
			case DRIVE_REMOVABLE :
				result = org_eclipse_update_configuration_LocalSystemInfo_VOLUME_REMOVABLE;
				break;
			case DRIVE_CDROM :
				result = org_eclipse_update_configuration_LocalSystemInfo_VOLUME_CDROM;
				break;
			case DRIVE_FIXED :
				result = org_eclipse_update_configuration_LocalSystemInfo_VOLUME_FIXED;
				break;
			case DRIVE_REMOTE :
				result = org_eclipse_update_configuration_LocalSystemInfo_VOLUME_REMOTE;
				break;
			case DRIVE_NO_ROOT_DIR :
				result = org_eclipse_update_configuration_LocalSystemInfo_VOLUME_INVALID_PATH;
				break;
			case DRIVE_RAMDISK :
			case DRIVE_UNKNOWN :
			default :
				result = org_eclipse_update_configuration_LocalSystemInfo_VOLUME_UNKNOWN;
				break;
		}
	} else {
		result = org_eclipse_update_configuration_LocalSystemInfo_VOLUME_INVALID_PATH;
	}

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
	DWORD logDrives;
	UINT drive;
	char driveName[100];
	jobjectArray returnArray;
	int nDrive = 0;

	// 
	jclass stringClass;
	jobject empty;
	int index = 0;
	jobject str;

	logDrives = GetLogicalDrives();
	for (drive = 0; drive < 32; drive++) {
		if (logDrives & (1 << drive)) {
			nDrive++;
		}
	}

	stringClass = jnienv -> FindClass("java/lang/String");
	empty = jnienv -> NewStringUTF("");
	returnArray = jnienv -> NewObjectArray(nDrive, stringClass, empty);

	for (drive = 0; drive < 32; drive++) {
		if (logDrives & (1 << drive)) {
			sprintf(driveName, "%c:\\", drive + 'A');
			str = jnienv -> NewStringUTF(driveName);
			jnienv -> SetObjectArrayElement(returnArray, index, str);
			index++;
		}
	}

	return returnArray;
}