package org.eclipse.core.internal.localstore;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
public class CoreFileSystemLibraryWindows extends CoreFileSystemLibrary {
	static {
		System.loadLibrary("core102");
	}
/**
 * 
 */
public CoreFileSystemLibraryWindows() {}
public long internalGetLastModified(String fileName) {
	return getLastModified(getStat(fileName));
}
/**
 * Returns the stat information for the specified filename in a long (64 bits). We just
 * retrieve the stat information we consider necessary and store everything in one long
 * to avoid JNI calls.
 */
protected final native long internalGetStat(String fileName);
protected boolean internalIsReadOnly(String fileName) {
	return isSet(getStat(fileName), STAT_READ_ONLY);
}
protected final native boolean internalSetReadOnly(String fileName, boolean readOnly);
}
