/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.search.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.osgi.framework.Bundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Plugin;


public class FileTool {

	private final static int MAX_RETRY= 5;

	/**
	 * A buffer.
	 */
	private static byte[] buffer = new byte[8192];

	/**
	 * Unzips the given zip file to the given destination directory
	 * extracting only those entries the pass through the given
	 * filter.
	 *
	 * @param zipFile the zip file to unzip
	 * @param dstDir the destination directory
	 * @throws IOException in case of problem
	 */
	public static void unzip(ZipFile zipFile, File dstDir) throws IOException {
		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		while (entries.hasMoreElements()) {
			ZipEntry entry= entries.nextElement();
			if (entry.isDirectory()) {
				continue;
			}
			String entryName= entry.getName();
			if (!new File(dstDir, entryName).toPath().normalize().startsWith(dstDir.toPath().normalize())) {
				throw new RuntimeException("Bad zip entry: " + entryName); //$NON-NLS-1$
			}
			File file= new File(dstDir, changeSeparator(entryName, '/', File.separatorChar));
			file.getParentFile().mkdirs();

			try (
					InputStream src= zipFile.getInputStream(entry);
					OutputStream dst= new FileOutputStream(file)) {
				transferData(src, dst);
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
	 * @throws IOException in case of error
	 */
	public static void transferData(File source, File destination) throws IOException {
		destination.getParentFile().mkdirs();
		try (
				InputStream  is = new FileInputStream(source);
				OutputStream os = new FileOutputStream(destination)
			) {

			transferData(is, os);
		}
	}

	/**
	 * Copies all bytes in the given source stream to
	 * the given destination stream. Neither streams
	 * are closed.
	 *
	 * @param source the given source stream
	 * @param destination the given destination stream
	 * @throws IOException in case of error
	 */
	public static void transferData(InputStream source, OutputStream destination) throws IOException {
		int bytesRead = 0;
		while(bytesRead != -1){
			bytesRead = source.read(buffer, 0, buffer.length);
			if(bytesRead != -1){
				destination.write(buffer, 0, bytesRead);
			}
		}
	}

	/**
	 * Copies the given source file to the given destination file.
	 *
	 * @param src the given source file
	 * @param dst the given destination file
	 * @throws IOException in case of error
	 */
	public static void copy(File src, File dst) throws IOException {
		if(src.isDirectory()){
			String[] srcChildren = src.list();
			if (srcChildren == null) {
				throw new IOException("Content from directory '" + src.getAbsolutePath() + "' can not be listed."); //$NON-NLS-1$ //$NON-NLS-2$
			}
			for (String srcChild2 : srcChildren) {
				File srcChild= new File(src, srcChild2);
				File dstChild= new File(dst, srcChild2);
				copy(srcChild, dstChild);
			}
		} else
			transferData(src, dst);
	}

	public static File getFileInPlugin(Plugin plugin, IPath path) {
		try {
			URL installURL= plugin.getBundle().getEntry(path.toString());
			URL localURL= FileLocator.toFileURL(installURL);
			return new File(localURL.getFile());
		} catch (IOException e) {
			return null;
		}
	}


	public static File getFileInBundle(Bundle bundle, IPath path) {
		try {
			URL installURL= bundle.getEntry(path.toString());
			URL localURL= FileLocator.toFileURL(installURL);
			return new File(localURL.getFile());
		} catch (IOException e) {
			return null;
		}
	}


	public static File createTempFileInPlugin(Plugin plugin, IPath path) {
		IPath stateLocation= plugin.getStateLocation();
		stateLocation= stateLocation.append(path);
		return stateLocation.toFile();
	}

	public static void delete(File file) {
		if (file.exists()) {
			for (int i= 0; i < MAX_RETRY; i++) {
				if (file.delete())
					i= MAX_RETRY;
				else {
					try {
						Thread.sleep(1000); // sleep a second
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}
}
