package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.*;

/**
 * A PluginContainer manages plug-in archives.
 * A feature and a site are plugin container as they both
 * logically or physically manage the archives that contain
 * plug-ins.
 * 
 */
//FIXME: javadoc

public interface IPluginContainer extends IAdaptable {
	/**
	 * Returns an array of plug-ins managed by the container
	 * 
	 * @return the accessible plug-ins. Returns an empty array
	 * if there are no plug-ins.
	 * @since 2.0 
	 */

	IPluginEntry [] getPluginEntries()  ;
	
	/**
	 * Returns the number of managed plug-ins
	 * @return the number of plug-ins
	 * @since 2.0 
	 */

	int getPluginEntryCount() ;
	
	/**
	 * Size of the archives in Kilo-Bytes
	 * @return the size of the archive to be downloaded
	 * @since 2.0 
	 */

	long getDownloadSize(IPluginEntry entry) ;
	
	/**
	 * Size of the plug-in in KiloBytes
	 * @return the size of the plug-in when installed
	 * @since 2.0 
	 */

	long getInstallSize(IPluginEntry entry) ;

	/**
	 * Adds a pluginEntry to the list of managed pluginEntry
	 * 
	 * @param entry the plugin entry
	 * @since 2.0 
	 */
	
	void addPluginEntry(IPluginEntry pluginEntry);
	
	/**
	 * Create a file in a plugin 
	 * 
	 * @param entry the plugin entry
	 * @param name the file to be created in the plugin
	 * @param inStream the content of the remote file to be transfered in the new file
	 * @param the progress monitor
	 * @since 2.0 
	 */

	void store(IPluginEntry entry, String name, InputStream inStream,IProgressMonitor monitor) throws CoreException;
	

		
	
	
}

