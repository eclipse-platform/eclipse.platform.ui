package org.eclipse.core.internal.localstore;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.internal.resources.ResourceStatus;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;

public abstract class CoreFileSystemLibrary {

	/** The following masks are used to represent the bits
		returned by the FileSystemStore.getStat() method.
		The method is a C call to the stat function. But it
		only returns the information relevant to this implementation.
		The lower bits represent the last modified timestamp of the
		given file and the higher bits represent some relevant flags.
	*/
	/** reserved, should not be used */
	protected static final long STAT_RESERVED = 0x8000000000000000l;
	/** indicates if this is a valid stat or some problem happened when 
		retrieving the information */
	protected static final long STAT_VALID = 0x4000000000000000l;
	/** indicates if the resource is a folder or a file */
	protected static final long STAT_FOLDER = 0x2000000000000000l;
	/** indicates if the resource is marked as read-only */
	protected static final long STAT_READ_ONLY = 0x1000000000000000l;
	/** used to extract the last modified timestamp */
	protected static final long STAT_LASTMODIFIED = ~(STAT_RESERVED | STAT_VALID | STAT_FOLDER | STAT_READ_ONLY);

	/** instance of this library */
	private static CoreFileSystemLibrary instance;

	/** OS constants */
	public static final int OS_UNDEFINED 	= 0;
	public static final int OS_UNKNOWN 		= 1;
	public static final int OS_WINDOWS_98 	= 2;
	public static final int OS_WINDOWS_NT 	= 3;
	public static final int OS_WINDOWS_2000 = 4;
	public static final int OS_LINUX 		= 5;

	/** os type */
	private static int os = OS_UNDEFINED;

	static {
		try {
			switch (getOS()) {
				case OS_WINDOWS_98 :
				case OS_WINDOWS_NT :
				case OS_WINDOWS_2000 :
					instance = new CoreFileSystemLibraryWindows();
					break;
				case OS_LINUX :
					instance = new CoreFileSystemLibraryLinux();
					break;
				default :
					instance = new CoreFileSystemLibraryPureJava();
					break;
			}
		} catch (UnsatisfiedLinkError e) {
			instantiateDefaultLibrary(e);
		}
	}
public static long getLastModified(long stat) {
	return (stat & STAT_LASTMODIFIED);
}
public static long getLastModified(String fileName) {
	return instance.internalGetLastModified(fileName);
}
public static int getOS() {
	if (os != OS_UNDEFINED)
		return os;
	String name = System.getProperty("os.name");
	if (name.equals("Windows 98"))
		return setOS(OS_WINDOWS_98);
	if (name.equals("Windows NT"))
		return setOS(OS_WINDOWS_NT);
	if (name.equals("Windows 2000"))
		return setOS(OS_WINDOWS_2000);
	if (name.equals("Linux"))
		return setOS(OS_LINUX);
	return setOS(OS_UNKNOWN);
}
public static long getStat(String fileName) {
	return instance.internalGetStat(fileName);
}
private static void instantiateDefaultLibrary(UnsatisfiedLinkError e) {
	String libName = System.mapLibraryName("core102");
	String message = "Could not find library " + libName;
	ResourceStatus status = new ResourceStatus(IResourceStatus.WARNING, null, message, e);
	ResourcesPlugin.getPlugin().getLog().log(status);
	instance = new CoreFileSystemLibraryPureJava();
}
protected abstract long internalGetLastModified(String fileName);
protected abstract long internalGetStat(String fileName);
protected abstract boolean internalIsReadOnly(String fileName);
protected abstract boolean internalSetReadOnly(String fileName, boolean readOnly);
public static boolean isFile(long stat) {
	return isSet(stat, STAT_VALID) && !isSet(stat, STAT_FOLDER);
}
public static boolean isFolder(long stat) {
	return isSet(stat, STAT_VALID) && isSet(stat, STAT_FOLDER);
}
public static boolean isReadOnly(String fileName) {
	return instance.internalIsReadOnly(fileName);
}
protected static boolean isSet(long stat, long mask) {
	return (stat & mask) != 0;
}
private static int setOS(int os) {
	CoreFileSystemLibrary.os = os;
	return os;
}
public static boolean setReadOnly(String fileName, boolean readOnly) {
	return instance.internalSetReadOnly(fileName, readOnly);
}
}
