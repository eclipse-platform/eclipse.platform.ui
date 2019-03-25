/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.osgi.service.datalocation.Location;

/**
 *
 * Interface for file handling. E.g. read and write files.
 *
 */
public interface IFileProvider {

	/**
	 * @see Files#readAllLines(java.nio.file.Path)
	 */
	@SuppressWarnings("javadoc")
	List<String> readAllLines(String path) throws IOException;

	/**
	 * @see Files#write(java.nio.file.Path, byte[], java.nio.file.OpenOption...)
	 */
	@SuppressWarnings("javadoc")
	void write(String path, byte[] bytes) throws IOException;

	/**
	 * @see Files#newBufferedReader(java.nio.file.Path)
	 */
	@SuppressWarnings("javadoc")
	Reader newReader(String path) throws IOException;

	/**
	 * @see Files#newBufferedWriter(java.nio.file.Path, java.nio.file.OpenOption...)
	 */
	@SuppressWarnings("javadoc")
	Writer newWriter(String path) throws IOException;

	/**
	 * @see Files#exists(java.nio.file.Path, java.nio.file.LinkOption...)
	 */
	@SuppressWarnings("javadoc")
	boolean fileExists(String path);

	/**
	 * @see Files#isDirectory(java.nio.file.Path, java.nio.file.LinkOption...)
	 */
	@SuppressWarnings("javadoc")
	boolean isDirectory(String path);

	/**
	 * @see Files#newDirectoryStream(Path, String)
	 */
	@SuppressWarnings("javadoc")
	DirectoryStream<Path> newDirectoryStream(String path, String glob) throws IOException;

	/**
	 * Uses {@link File#File(String)} and {@link File#getPath()} to get the path of
	 * an URL object that was created using the deprecated {@link File#toURL}.
	 * <p>
	 * This is required to normalize file paths retrieved from e.g. a
	 * {@link Location} URL or system properties like
	 * <code>eclipse.home.location</code>.
	 * <p>
	 * This uses the <code>java.io</code> instead of the <code>java.nio</code> API,
	 * because <code>java.nio</code> doesn't like paths such as
	 * <code>/c:/some/dir/</code> which where created by the deprecated
	 * {@link File#toURL}.
	 *
	 * @param url the file URL
	 * @return the normalized file path according to the operating system file
	 *         system.
	 */
	String getFilePath(URL url);

}
