package org.eclipse.core.internal.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
/**
 * Given a target and a temporary locations, it tries to read the contents
 * from the target. If a file does not exist at the target location, it tries
 * to read the contents from the temporary location.
 *
 * @see SafeFileOutputStream
 */
public class SafeFileInputStream extends FilterInputStream {
	protected static final String EXTENSION = ".bak";
public SafeFileInputStream(File file) throws IOException {
	this(file.getAbsolutePath(), null);
}
public SafeFileInputStream(String targetName) throws IOException {
	this(targetName, null);
}
/**
 * If targetPath is null, the file will be created in the default-temporary directory.
 */
public SafeFileInputStream(String targetPath, String tempPath) throws IOException {
	super(getInputStream(targetPath, tempPath));
}
private static InputStream getInputStream(String targetPath, String tempPath) throws IOException {
	File target = new File(targetPath);
	if (!target.exists()) {
		if (tempPath == null)
			tempPath = target.getAbsolutePath() + EXTENSION;
		target = new File(tempPath);
	}
	return new BufferedInputStream(new FileInputStream(target));
}
}
