package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
public interface IPluginContainer {
	IPluginEntry [] getPluginEntries();
	int getPluginEntryCount();
	int getDownloadSize(IPluginEntry entry);
	int getInstallSize(IPluginEntry entry);
}

