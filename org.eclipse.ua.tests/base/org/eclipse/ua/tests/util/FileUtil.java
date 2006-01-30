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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.osgi.framework.Bundle;

/*
 * Utility methods for working with files.
 */
public class FileUtil {

	/**
	 * Gets the contents of the file with the given relative path in the given bundle,
	 * as a String (file must
	 * be encoded as UTF-8).
	 */
	public static String getContents(Bundle bundle, String relativePath) throws IOException {
		return readString(bundle.getEntry(relativePath).openStream());
	}

	/**
	 * Gets the contents of the file with the given absolute path as a String (file must
	 * be encoded as UTF-8).
	 */
	public static String getContents(String absolutePath) throws IOException {
		return readString(new FileInputStream(absolutePath));
	}
	
	/**
	 * Reads the contents of the input stream as UTF-8 and constructs and returns
	 * as a String.
	 */
	private static String readString(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int num;
		while ((num = in.read(buffer)) > 0) {
			out.write(buffer, 0, num);
		}
		return new String(out.toByteArray(), "UTF-8");
	}
}
