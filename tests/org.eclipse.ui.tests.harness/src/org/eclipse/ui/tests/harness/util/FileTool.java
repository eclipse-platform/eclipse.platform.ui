/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 *******************************************************************************/

package org.eclipse.ui.tests.harness.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Plugin;


public class FileTool {

	/**
	 * Unzips the given zip file to the given destination directory
	 * extracting only those entries the pass through the given
	 * filter.
	 *
	 * @param zipFile the zip file to unzip
	 * @param dstDir the destination directory
	 */
	public static void unzip(ZipFile zipFile, File dstDir) throws IOException {
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		try {
			while(entries.hasMoreElements()){
				ZipEntry entry = entries.nextElement();
				if(entry.isDirectory()){
					continue;
				}
				String entryName = entry.getName();
				if (!new File(dstDir, entryName).toPath().normalize().startsWith(dstDir.toPath().normalize())) {
					throw new RuntimeException("Bad zip entry: " + entryName); //$NON-NLS-1$
				}
				File file = new File(dstDir, changeSeparator(entryName, '/', File.separatorChar));
				file.getParentFile().mkdirs();
				try (InputStream src = zipFile.getInputStream(entry); OutputStream dst= new FileOutputStream(file)){
					src.transferTo(dst);
				}
			}
		} finally {
			try {
				zipFile.close();
			} catch(IOException e){
			}
		}
	}
	/**
	 * Returns the given file path with its separator
	 * character changed from the given old separator to the
	 * given new separator.
	 *
	 * @param path a file path
	 * @param oldSeparator a path separator character
	 * @param newSeparator a path separator character
	 * @return the file path with its separator character
	 * changed from the given old separator to the given new
	 * separator
	 */
	public static String changeSeparator(String path, char oldSeparator, char newSeparator){
		return path.replace(oldSeparator, newSeparator);
	}
	/**
	 * Copies all bytes in the given source file to
	 * the given destination file.
	 *
	 * @param source the given source file
	 * @param destination the given destination file
	 */
	public static void transferData(File source, File destination) throws IOException {
		destination.getParentFile().mkdirs();
		try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(destination)) {
			is.transferTo(os);
		}
	}

	/**
	 * Copies the given source file to the given destination file.
	 *
	 * @param src the given source file
	 * @param dst the given destination file
	 */
	public static void copy(File src, File dst) throws IOException {
		if(src.isDirectory()){
			String[] srcChildren = src.list();
			if (srcChildren == null) {
				throw new IOException("Content from directory '" + src.getAbsolutePath() + "' can not be listed."); //$NON-NLS-1$ //$NON-NLS-2$
			}
			for (String srcChildPathName : srcChildren) {
				File srcChild = new File(src, srcChildPathName);
				File dstChild = new File(dst, srcChildPathName);
				copy(srcChild, dstChild);
			}
		} else
			transferData(src, dst);
	}

	public static File getFileInPlugin(Plugin plugin, IPath path) {
		try {
			URL installURL= plugin.getBundle().getEntry(path.toString());
			URL localURL = FileLocator.toFileURL(installURL);
			return new File(localURL.getFile());
		} catch (IOException e) {
			return null;
		}
	}

	public static StringBuilder readToBuilder(Reader reader) throws IOException {
		StringBuilder s = new StringBuilder();
		try {
			char[] buffer = new char[8196];
			int chars = reader.read(buffer);
			while (chars != -1) {
				s.append(buffer, 0, chars);
				chars = reader.read(buffer);
			}
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}
		return s;
	}

}
