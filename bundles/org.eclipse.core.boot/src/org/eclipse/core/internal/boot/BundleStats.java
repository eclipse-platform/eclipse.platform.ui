/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.boot;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * BundleStats is used to represent information about loaded bundle. A
 * bundlestats instance represents only one bundle.
 */

public class BundleStats {
	private String pluginId;	// the plugin loading this bundle
	private String fileName;	// the filename of the bundle 
	private int keyCount = 0;	// number of keys in the bundle
	private int keySize = 0;	// size of the keys in the bundle
	private int valueSize = 0;	// size of the values in the bundle 
	private long hashSize = 0;	// size of the hashtable
	private long fileSize = 0;

	private static int sizeOf(String value) {
		return 44 + (2 * value.length());
	}

	private static int sizeOf(Properties value) {
		return (int)Math.round(44 + (16 + (value.size() * 1.25 * 4)) + (24 * value.size()));
	}

	public BundleStats(String pluginId, String fileName, InputStream input) {
		this.pluginId = pluginId;
		this.fileName = fileName;
		initialize(input);
	}

	public BundleStats(String pluginId, String fileName, ResourceBundle bundle) {
		this.pluginId = pluginId;
		this.fileName = fileName;
		initialize(bundle);
	}

	/**
	 * Compute the size of bundle
	 */
	private void initialize(ResourceBundle bundle) { 
		for (Enumeration enum = bundle.getKeys(); enum.hasMoreElements();) {
			String key = (String) enum.nextElement();
			keySize += sizeOf(key);
			valueSize += sizeOf(bundle.getString(key));
			keyCount++;
		}
	}

	/**
	 * Compute the size of stream which represents a property file
	 */
	private void initialize(InputStream stream) {

		Properties props = new Properties();
		try {
			try {
				fileSize = stream.available();
				props.load(stream);
				for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
					String key = (String) iter.next();
					keySize += sizeOf(key);
					valueSize += sizeOf(props.getProperty(key));
					keyCount++;
				}
				hashSize = sizeOf(props);
			} finally {
				stream.close();
			}
		} catch (IOException e) {
			// ignore exceptions as they will be handled when the stream 
			// is loaded for real.   See callers.
		}
	}

	public long getHashSize() {
		return hashSize;
	}

	public int getKeyCount() {
		return keyCount;
	}

	public String getPluginId() {
		return pluginId;
	}

	public int getKeySize() {
		return keySize;
	}

	public int getValueSize() {
		return valueSize;
	}

	public long getTotalSize() {
		return keySize + valueSize + hashSize;
	}

	public String getFileName() {
		return fileName;
	}

	public long getFileSize() {
		return fileSize;
	}
}
