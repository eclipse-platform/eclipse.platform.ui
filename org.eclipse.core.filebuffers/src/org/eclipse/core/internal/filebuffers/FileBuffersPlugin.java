/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.filebuffers;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Plugin;

import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.jface.text.Assert;


/**
 * The plug-in runtime class for the file buffers plug-in (id <code>"org.eclipse.core.filebuffers"</code>).
 * 
 * @since 3.0
 */
public class FileBuffersPlugin extends Plugin {
	
	public final static String PLUGIN_ID= "org.eclipse.core.filebuffers";  //$NON-NLS-1$
	
	/** The shared plug-in instance */
	private static FileBuffersPlugin fgPlugin;
	/** The resource bundle */
	private ResourceBundle fResourceBundle;
	/** The file buffer manager */
	private ITextFileBufferManager fTextFileBufferManager;
	
	/**
	 * Creates a plug-in instance.
	 */
	public FileBuffersPlugin() {
		Assert.isTrue(fgPlugin == null);
		fgPlugin= this;
		try {
			fResourceBundle= ResourceBundle.getBundle("org.eclipse.core.internal.filebuffers.FileBuffersPlugin");  //$NON-NLS-1$
		} catch (MissingResourceException x) {
			fResourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the default plug-in instance
	 */
	public static FileBuffersPlugin getDefault() {
		return fgPlugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 * 
	 * @param key the resource string key
	 * @return the resource string for the given key
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle= FileBuffersPlugin.getDefault().getResourceBundle();
		try {
			return (bundle!=null ? bundle.getString(key) : key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle.
	 * 
	 * @return the resource bundle
	 */
	private ResourceBundle getResourceBundle() {
		return fResourceBundle;
	}
	
	/**
	 * Returns the text file buffer manager of this plug-in.
	 * 
	 * @return the text file buffer manager of this plug-in
	 */
	public ITextFileBufferManager getFileBufferManager()  {
		if (fTextFileBufferManager == null)
			fTextFileBufferManager= new TextFileBufferManager();
		return fTextFileBufferManager;
	}
}
