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
package org.eclipse.help.internal.toc;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;
/** 
 * Toc created from files in a extra directory in a plugin.
 */
public class DirectoryToc {
	private String dir;
	private ITopic[] extraTopics;
	private String locale;
	/**
	 * Constructor.
	 */
	protected DirectoryToc(TocFile tocFile) {
		this(tocFile.getPluginID(), tocFile.getLocale(), tocFile.getExtraDir());
	}
	private DirectoryToc(String pluginID, String locale, String directory) {
		this.locale = locale;
		// Obtain extra search directory if provided
		this.dir = HrefUtil.normalizeDirectoryHref(pluginID, directory);

	}
	/**
	 * This public method is to be used after the build of TOCs
	 * is finished.
	 * With assumption that TOC model is not modifiable
	 * after the build, this method caches topics in an array
	 * and releases objects used only during build.
	 * @return ITopic[]
	 */
	public ITopic[] getExtraTopics() {
		if (extraTopics == null) {
			Collection col = createExtraTopics();
			extraTopics = (ITopic[]) col.toArray(new ITopic[col.size()]);
			// for memory foot print, release TocFile and dir
			dir = null;
		}

		return extraTopics;
	}
	/**
	 * Obtains URLs of all documents inside given directory.
	 * @param extraDir directory for the topics in the form
	 *  <em>/pluginID/directory/containing/docs<em>
	 *  or
	 *  <em>/pluginID<em> for all documents in a given plugin
	 * @return Collection of ITopic
	 */
	private Collection createExtraTopics() {
		Collection ret = new HashSet();
		String pluginID = HrefUtil.getPluginIDFromHref(dir);
		if (pluginID == null) {
			return ret;
		}
		IPluginDescriptor pluginDesc =
			Platform.getPluginRegistry().getPluginDescriptor(pluginID);
		if (pluginDesc == null)
			return ret;
		String directory = HrefUtil.getResourcePathFromHref(dir);
		if (directory == null) {
			// the root - all files in a zip should be indexed
			directory = "";
		}
		// Find doc.zip file
		IPath iPath = new Path("$nl$/doc.zip");
		Map override = new HashMap(1);
		override.put("$nl$", locale);
		URL url = null;
		try {
			url = pluginDesc.getPlugin().find(iPath, override);
			if (url == null) {
				url = pluginDesc.getPlugin().find(new Path("doc.zip"));
			}
		} catch (CoreException ce) {
			HelpPlugin.logError(
				Resources.getString("E034", "/" + pluginID + "/doc.zip"),
				ce);
		}
		if (url != null) {
			// collect topics from doc.zip file
			ret.addAll(createExtraTopicsFromZip(pluginID, directory, url));
		}
		// Find directory on the filesystem
		iPath = new Path("$nl$/" + directory);
		url = null;
		try {
			url = pluginDesc.getPlugin().find(iPath, override);
			if (url == null) {
				if (directory.length() == 0) {
					// work around NPE in plugin.find()
					url = pluginDesc.getInstallURL();
				} else {
					url = pluginDesc.getPlugin().find(new Path(directory));
				}
			}
		} catch (CoreException ce) {
			HelpPlugin.logError(
				Resources.getString("E035", "/" + pluginID + "/" + directory),
				ce);
		}
		if (url != null) {
			// collect topics from doc.zip file
			ret.addAll(
				createExtraTopicsFromDirectory(pluginID, directory, url));
		}
		return ret;

	}
	/**
	 * @param directory path in the form "segment1/segment2...",
	 *  "" will return names of all files in a zip
	 */
	private Collection createExtraTopicsFromZip(
		String pluginID,
		String directory,
		URL url) {
		Collection ret = new ArrayList(0);
		URL realZipURL;
		try {
			realZipURL = Platform.resolve(url);
		} catch (IOException ioe) {
			HelpPlugin.logError(
				Resources.getString("E036", url.toString()),
				ioe);
			return new ArrayList(0);
		}
		ZipFile zipFile;
		try {
			zipFile = new ZipFile(realZipURL.getFile());
			ret = createExtraTopicsFromZipFile(pluginID, zipFile, directory);
			zipFile.close();
		} catch (IOException ioe) {
			HelpPlugin.logError(
				Resources.getString("E037", realZipURL.getFile()),
				ioe);
			return new ArrayList(0);
		}

		return ret;

	}

	/**
	* Obtains names of files in a zip file that given directory in their path.
	* Files in subdirectories are included as well.
	* @param directory path in the form "segment1/segment2...",
	*  "" will return names of all files in a zip
	* @return Collection of ITopic
	*/
	private Collection createExtraTopicsFromZipFile(
		String pluginID,
		ZipFile zipFile,
		String directory) {
		String constantHrefSegment = "/" + pluginID + "/";
		Collection ret = new ArrayList();
		for (Enumeration enum = zipFile.entries(); enum.hasMoreElements();) {
			ZipEntry zEntry = (ZipEntry) enum.nextElement();
			if (zEntry.isDirectory()) {
				continue;
			}
			String docName = zEntry.getName();
			int l = directory.length();
			if (l == 0
				|| docName.length() > l
				&& docName.charAt(l) == '/'
				&& directory.equals(docName.substring(0, l))) {
				ret.add(new ExtraTopic(constantHrefSegment + docName));
			}
		}
		return ret;
	}
	/**
	 * @param directory path in the form "segment1/segment2...",
	 *  "" will return names of all files in a directory
	 */
	private Collection createExtraTopicsFromDirectory(
		String pluginID,
		String directory,
		URL url) {
		Collection col = new ArrayList();
		URL realURL;
		try {
			realURL = Platform.resolve(url);
		} catch (IOException ioe) {
			HelpPlugin.logError(
				Resources.getString("E038", url.toString()),
				ioe);
			return col;
		}
		File dirFile = new File(realURL.getFile());
		if (dirFile.exists() && dirFile.isDirectory()) {
			String prefix;
			if (directory.length() > 0) {
				prefix = "/" + pluginID + "/" + directory;
			} else {
				prefix = "/" + pluginID;
			}
			createExtraTopicsFromDirectoryFile(prefix, dirFile, col);
		}
		return col;

	}
	/**
	 * @prefix /pluginID/segment1/segment2
	 */
	private Collection createExtraTopicsFromDirectoryFile(
		String prefix,
		File dir,
		Collection col) {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			String href = prefix + "/" + files[i].getName();
			if (files[i].isDirectory()) {
				createExtraTopicsFromDirectoryFile(href, files[i], col);
			} else {
				col.add(new ExtraTopic(href));
			}
		}
		return col;
	}
	class ExtraTopic implements ITopic {
		private String topicHref;
		public ExtraTopic(String href) {
			this.topicHref = href;
		}

		public String getHref() {
			return topicHref;
		}
		public String getLabel() {
			return topicHref;
		}
		public ITopic[] getSubtopics() {
			return new ITopic[0];
		}
	}
}
