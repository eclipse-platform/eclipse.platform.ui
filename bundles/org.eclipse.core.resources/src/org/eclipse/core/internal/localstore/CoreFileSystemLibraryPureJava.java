package org.eclipse.core.internal.localstore;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import java.io.File;

public class CoreFileSystemLibraryPureJava extends CoreFileSystemLibrary {
/**
 * 
 */
public CoreFileSystemLibraryPureJava() {}
public long internalGetLastModified(String fileName) {
	return new File(fileName).lastModified();
}
public long internalGetStat(String fileName) {
	File target = new File(fileName);
	long result = target.lastModified();
	if (result == 0) // non-existing
		return result;
	result |= STAT_VALID;
	if (target.isDirectory())
		result |= STAT_FOLDER;
	return result;
}
protected boolean internalIsReadOnly(String fileName) {
	return !(new File(fileName).canWrite());
}
protected boolean internalSetReadOnly(String fileName, boolean readOnly) {
	if (!readOnly)
		return false; // unsuported
	return new File(fileName).setReadOnly();
}
}
