package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;
import org.eclipse.core.runtime.CoreException;

public interface IPluginContainer {
	IPluginEntry [] getPluginEntries() throws CoreException ;
	int getPluginEntryCount()throws CoreException ;
	int getDownloadSize(IPluginEntry entry) throws CoreException ;
	int getInstallSize(IPluginEntry entry) throws CoreException ;
	
	void addPluginEntry(IPluginEntry pluginEntry);
	void store(IPluginEntry pluginEntry, String contentKey, InputStream inStream) throws CoreException ;
}

