/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

/*
 * Utility methods for finding resources.
 */
public class ResourceFinder {

	/*
	 * Finds the specified file in the given plugin and returns a URL to it.
	 */
	public static URL findFile(Plugin plugin, String path) {
		String fullLocation = plugin.getBundle().getLocation();
		String location = fullLocation.substring(fullLocation.indexOf('@') + 1);
		IPath fullPath = new Path(location).append(path);
		File file = fullPath.toFile();
		
		/*
		 * If it's a relative path, append it to the install location.
		 */
		if (!file.exists()) {
			fullPath = new Path(Platform.getInstallLocation().getURL().toString().substring("file:".length()) + fullPath);
			file = fullPath.toFile();
		}

		try {
			return file.toURL();
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * Finds and returns URLs to all files in the plugin directory under the given
	 * folder with the given suffix. Can also recursively traverse all subfolders.
	 */
	public static URL[] findFiles(Plugin plugin, String folder, String suffix, boolean recursive) {
		String fullLocation = plugin.getBundle().getLocation();
		String location = fullLocation.substring(fullLocation.indexOf('@') + 1);
		IPath path = new Path(location).append(folder);
		File file = path.toFile();

		/*
		 * If it's a relative path, append it to the install location.
		 */
		if (!file.exists()) {
			path = new Path(Platform.getInstallLocation().getURL().toString().substring("file:".length()) + path);
			file = path.toFile();
		}
		
		File[] files = path.toFile().listFiles();
		return findFiles(files, suffix, recursive);
	}
	
	/*
	 * Finds and returns URLs to all files in the given list that have the given suffix, and
	 * recursively traverses subdirectories if requested.
	 */
	private static URL[] findFiles(File[] files, String suffix, boolean recursive) {
		List list = new ArrayList();
		if (files != null) {
			for (int i=0;i<files.length;++i) {
				if (files[i].isDirectory()) {
					if (recursive) {
						list.addAll(Arrays.asList(findFiles(files[i].listFiles(), suffix, recursive)));
					}
				}
				else {
					try {
						URL url = files[i].toURL();
						if (url.toString().endsWith(suffix)) {
							list.add(url);
						}
					}
					catch (MalformedURLException e) {
					}
				}
			}
		}
		
		URL[] array = new URL[list.size()];
		list.toArray(array);
		return array;
	}
}
