package org.eclipse.core.internal.localstore;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.internal.resources.ResourceStatus;
import org.eclipse.core.internal.utils.Convert;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import java.io.File;

public abstract class CoreFileSystemLibrary {

	/** Indicates whether or not this FS is case sensitive */
	private static final boolean caseSensitive = new File("a").compareTo(new File("A")) != 0;
	
	/**
	 * The following masks are used to represent the bits
	 * returned by the getStat() and internalGetStat() methods.
	 * The idea is to save JNI calls. So internalGetStat() is a native
	 * that grabs as many important information as it cans and put in
	 * a long variable.
	 * The lower bits represent the last modified timestamp of the
	 * given file and the higher bits represent some relevant flags.
	 */

	/** reserved, should not be used */
	private static final long STAT_RESERVED 	= 0x8000000000000000l;
	/** indicates if this is a valid stat or some problem happened when 
		retrieving the information */
	private static final long STAT_VALID 		= 0x4000000000000000l;
	/** indicates if the resource is a folder or a file */
	private static final long STAT_FOLDER 		= 0x2000000000000000l;
	/** indicates if the resource is marked as read-only */
	private static final long STAT_READ_ONLY 	= 0x1000000000000000l;
	/** used to extract the last modified timestamp */
	private static final long STAT_LASTMODIFIED = ~(STAT_RESERVED | STAT_VALID | STAT_FOLDER | STAT_READ_ONLY);

	/** instance of this library */
	// The name convention is to get the plugin version at the time
	// the library is changed. Remember that ant.core uses the same
	// library.
	private static final String LIBRARY_NAME = "core_2_0_5";
	private static boolean hasNatives = false;
	
	static {
		try {
			System.loadLibrary(LIBRARY_NAME);
			hasNatives = true;
		} catch (UnsatisfiedLinkError e) {
			logMissingNativeLibrary(e);
		}
	}

public static long getLastModified(long stat) {
	return (stat & STAT_LASTMODIFIED);
}
public static long getLastModified(String fileName) {
	if (hasNatives)
		return getLastModified(getStat(fileName));

	// inlined (no native) implementation
	return new File(fileName).lastModified();
}
public static long getStat(String fileName) {
	if (hasNatives)
		return internalGetStat(Convert.toPlatformBytes(fileName));

	// inlined (no native) implementation
	File target = new File(fileName);
	long result = target.lastModified();
	if (result == 0) // non-existing
		return result;
	result |= STAT_VALID;
	if (target.isDirectory())
		result |= STAT_FOLDER;
	if (!(new File(fileName).canWrite()))
		result |= STAT_READ_ONLY;
	return result;
}
private static void logMissingNativeLibrary(UnsatisfiedLinkError e) {
	String libName = System.mapLibraryName(LIBRARY_NAME);
	String message = Policy.bind("localstore.couldNotLoadLibrary", libName);
	ResourceStatus status = new ResourceStatus(IResourceStatus.WARNING, null, message, e);
	ResourcesPlugin.getPlugin().getLog().log(status);
}
/**
 * Returns the stat information for the specified filename in a long (64 bits). We just
 * retrieve the stat information we consider necessary and store everything in one long
 * to save some JNI calls.
 */
private static final native long internalGetStat(byte[] fileName);
private static final native boolean internalSetReadOnly(byte[] fileName, boolean readOnly);
public static boolean isFile(long stat) {
	return isSet(stat, STAT_VALID) && !isSet(stat, STAT_FOLDER);
}
public static boolean isFolder(long stat) {
	return isSet(stat, STAT_VALID) && isSet(stat, STAT_FOLDER);
}
public static boolean isReadOnly(String fileName) {
	if (hasNatives) 
		return isSet(getStat(fileName), STAT_READ_ONLY);
	// inlined (no native) implementation
	return !(new File(fileName).canWrite());
}
public static boolean isReadOnly(long stat) {
	return isSet(stat, STAT_READ_ONLY);
}
private static boolean isSet(long stat, long mask) {
	return (stat & mask) != 0;
}
public static boolean setReadOnly(String fileName, boolean readOnly) {
	if (hasNatives)
		return internalSetReadOnly(Convert.toPlatformBytes(fileName), readOnly);

	// inlined (no native) implementation
	if (!readOnly)
		return false; // not supported
	return new File(fileName).setReadOnly();
}
public static boolean isCaseSensitive() {
	return caseSensitive;
}
/**
 * Copies file attributes from source to destination. The copyLastModified attribute
 * indicates whether the lastModified attribute should be copied.
 */
public static boolean copyAttributes(String source, String destination, boolean copyLastModified) {
	if (hasNatives)
		return internalCopyAttributes(Convert.toPlatformBytes(source), Convert.toPlatformBytes(destination), false);
	return false; // not supported
}
/**
 * Copies file attributes from source to destination. The copyLastModified attribute
 * indicates whether the lastModified attribute should be copied.
 */
private static final native boolean internalCopyAttributes(byte[] source, byte[] destination, boolean copyLastModified);
}