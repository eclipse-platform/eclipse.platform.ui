package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;

public interface IPluginContainer {
	IPluginEntry [] getPluginEntries();
	int getPluginEntryCount();
	int getDownloadSize(IPluginEntry entry);
	int getInstallSize(IPluginEntry entry);
	
	void addPluginEntry(IPluginEntry pluginEntry);
	void store(IPluginEntry pluginEntry, String contentKey, InputStream inStream);
}

