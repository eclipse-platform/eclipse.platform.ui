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
import java.nio.file.Paths;
import java.util.List;

/**
 * Productive implementation of {@link IFileProvider}
 */
public class FileProvider implements IFileProvider {

	@Override
	public List<String> readAllLines(String path) throws IOException {
		Path filePath = Paths.get(path);
		return Files.readAllLines(filePath);
	}

	@Override
	public void write(String path, byte[] bytes) throws IOException {
		Path filePath = Paths.get(path);
		Files.write(filePath, bytes);
	}

	@Override
	public Reader newReader(String path) throws IOException {
		Path filePath = Paths.get(path);
		return Files.newBufferedReader(filePath);
	}

	@Override
	public Writer newWriter(String path) throws IOException {
		Path filePath = Paths.get(path);
		return Files.newBufferedWriter(filePath);
	}

	@Override
	public boolean fileExists(String path) {
		Path filePath = Paths.get(path);
		return Files.exists(filePath);
	}

	@Override
	public boolean isDirectory(String path) {
		Path filePath = Paths.get(path);
		return Files.isDirectory(filePath);
	}

	@Override
	public String getFilePath(URL url) {
		return new File(url.getPath()).getPath();
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(String path, String glob) throws IOException {
		Path dirPath = Paths.get(path);
		return Files.newDirectoryStream(dirPath, glob);
	}
}