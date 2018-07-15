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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.List;

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
}
