/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */


# include <update.h>
# include <windows.h>
# include <winioctl.h> // IOCTL codes: MediaType

// Windows Version
int WIN98 = 0;
int WINNT = 1;
int WINME = 2;
int WIN2000 = 3;
int WINXP = 4;

// FLOPPY
int FLOPPY_3 = 0;
int FLOPPY_5 = 1;

int DEBUG = 0;

typedef BOOL(WINAPI * P_GDFSE) 
		(LPCTSTR,
		PULARGE_INTEGER,
		PULARGE_INTEGER,
		PULARGE_INTEGER);

// GLOBAL METHODS
// ---------------


/*
 * calls GetVolumeInformation
 */
jstring getLabel(char driveLetter[],JNIEnv * jnienv){

	jstring result = NULL;
	char buf[128];	
	
	int err = GetVolumeInformation(
		driveLetter,
		buf,
		sizeof(buf) - 1,
		NULL,
		NULL,
		NULL,
		NULL,
		0);
	if (err){
		result = jnienv -> NewStringUTF(buf);
	}
	return result;
}

/*
 * returns the Version of Windows
 * int 0 WIN98;
 * int 1 WINNT;
 * int 2 WINME;
 * int 3 WIN2000;
 * int 4 WINXP;
 * returns -1 otherwise
 */
int getWindowsVersion(){
	OSVERSIONINFOEX osvi;
	int UNKNOWN = -1;
	
	ZeroMemory(&osvi, sizeof(OSVERSIONINFOEX));
	osvi.dwOSVersionInfoSize = sizeof(OSVERSIONINFOEX);
	
	if(!(GetVersionEx((OSVERSIONINFO *)&osvi))){
		if (DEBUG)
			printf("UNKNOWN VERSION: Cannot execute GetVersionEx\n");				 	
		return UNKNOWN;
	}
	
	switch(osvi.dwPlatformId){
		case VER_PLATFORM_WIN32_NT:
			if (DEBUG)
				printf("VERSION NT: Maj %i Min %i\n",osvi.dwMajorVersion,osvi.dwMinorVersion);				 	
			if(osvi.dwMajorVersion<=4)
				return WINNT;
			if(osvi.dwMajorVersion==5){
				if (osvi.dwMinorVersion==0)
					return WIN2000;
				if (osvi.dwMinorVersion==1)
					return WINXP;
			} else {
				return UNKNOWN;
			};
			break;
		case VER_PLATFORM_WIN32_WINDOWS:
			if (DEBUG)
				printf("VERSION Non NT: Maj %i Min %i\n",osvi.dwMajorVersion,osvi.dwMinorVersion);				 	
			if(osvi.dwMajorVersion==4){
				if (osvi.dwMinorVersion==10)
					return WIN98;
				if (osvi.dwMinorVersion==90)
					return WINME;
			} else {
				return UNKNOWN;
			}
			break;
		default:
			if (DEBUG)
				printf("VERSION UNKNOWN: Maj %i Min %i\n",osvi.dwMajorVersion,osvi.dwMinorVersion);				 	
			return UNKNOWN;
	}
} 

/*
 * Returns the size of the Drive as a label
 * Returns FLOPPY_3,FLOPPY_5 or -1 if Not found
 */
int getFloppy(char driveLetter[]){

	TCHAR floppyPath[8];
	HANDLE handle;
	DISK_GEOMETRY geometry[20];
	DWORD dw;
	int UNKNOWN = -1;

	if ((int)getWindowsVersion()<0){
		// windows 95 or other
		return UNKNOWN;
	} else {
		sprintf(floppyPath, "\\\\.\\%c:", driveLetter[0]);
		if (DEBUG)
			printf("Path %s\n",floppyPath);
		handle=CreateFile(floppyPath,
			0,FILE_SHARE_WRITE,0,OPEN_EXISTING,0,0);
		if (handle==INVALID_HANDLE_VALUE){
			if (DEBUG)
				printf("Invalid Handle %s\n",floppyPath);
			return UNKNOWN;
		} else {
			if(DeviceIoControl(handle,
				IOCTL_DISK_GET_MEDIA_TYPES,0,0,
				geometry,sizeof(geometry),&dw,0) 
				&& dw>0) {
				switch(geometry[0].MediaType){
				 case F5_160_512:
				 case F5_180_512:
				 case F5_320_512:
				 case F5_320_1024:
				 case F5_360_512:
				 case F5_1Pt2_512:
					if (DEBUG)
						printf("Found 5 1/4 Drive\n");				 	
				 	return FLOPPY_5;
				 case F3_720_512:			 				 				 				 				 
				 case F3_1Pt44_512:
				 case F3_2Pt88_512:
				 case F3_20Pt8_512:				 				 				 
					if (DEBUG)
						printf("Found 3 1/2 Drive\n");				 	
				 	return FLOPPY_3;
				}
			}
		}
	}
	return UNKNOWN;	
}

/*
 * 
 */
 char[] getRemoteNetworkName(char driveLetter[],JNIEnv * jnienv){
 	
 }


// JNI METHODS
// ---------------

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

	//
	jstring result = NULL;
	int floppy;
	
	// Make sure we have a String of the Form: <letter>:
	if (':' == lpDirectoryName[1]) {
		char driveLetter[4]; // i.e. -> C:\\
		memcpy(driveLetter, lpDirectoryName, 2);
		strcpy(driveLetter + 2, "\\");
		switch (GetDriveType(driveLetter)) {
			case DRIVE_REMOVABLE :
				// check 3.5 or 5.25	
				if (DEBUG)
					printf("Floppy Drive");
				floppy = getFloppy(driveLetter);					
				if (floppy==FLOPPY_3) return jnienv -> NewStringUTF("3 1/2");
				if (floppy==FLOPPY_5) return jnienv -> NewStringUTF("5 1/4");				
				return NULL;
			case DRIVE_REMOTE :
				// check name of machine and path of remote
				if (DEBUG)
					printf("Remote Drive");
				result = getLabel(driveLetter,jnienv);				
				break;
			default :
				if (DEBUG)
					printf("Another Drive at %s", driveLetter);
				result = getLabel(driveLetter,jnienv);
				break;
		} 
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
				result = org_eclipse_update_configuration_LocalSystemInfo_VOLUME_RAMDISK;
				break;			
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

