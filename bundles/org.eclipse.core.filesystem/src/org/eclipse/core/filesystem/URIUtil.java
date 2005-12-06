/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.filesystem;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.core.runtime.*;

/**
 * This class provides utility methods for comparing, inspecting, and manipulating
 * URIs.
 * 
 * @since org.eclipse.core.filesystem 1.0
 */
public class URIUtil {
	/**
	 * Replaces any colon characters in the provided string with their equivalent
	 * URI escape sequence.
	 */
	private static String escapeColons(String string) {
		final String COLON_STRING = "%3A"; //$NON-NLS-1$
		if (string.indexOf(':') == -1)
			return string;
		int length = string.length();
		StringBuffer result = new StringBuffer(length);
		for (int i = 0; i < length; i++) {
			char c = string.charAt(i);
			if (c == ':')
				result.append(COLON_STRING);
			else
				result.append(c);
		}
		return result.toString();
	}

	/**
	 * Returns an {@link IPath} representing this {@link URI}
	 * in the local file system, or <code>null</code> if this URI does
	 * not represent a file in the local file system.
	 * 
	 * @param uri The URI to convert
	 * @return The path representing the provided URI, <code>null</code>
	 */
	public static IPath toPath(URI uri) {
		Assert.isNotNull(uri);
		if (EFS.SCHEME_FILE.equals(uri.getScheme()))
			return new Path(uri.getSchemeSpecificPart());
		return null;
	}

	/**
	 * Converts an {@link IPath} representing a local file system path to a {@link URI}.
	 * 
	 * @param path The path to convert
	 * @return The URI representing the provided path
	 */
	public static URI toURI(IPath path) {
		if (path == null)
			return null;
		if (path.isAbsolute())
			return toURI(path.toFile().getAbsolutePath());
		try {
			//try to preserve the path as a relative path
			return new URI(escapeColons(path.toString()));
		} catch (URISyntaxException e) {
			return toURI(path.toFile().getAbsolutePath());
		}
	}

	/**
	 * Converts a String representing a local file system path to a {@link URI}.
	 * For example, this method can be used to create a URI from the output
	 * of {@link File#getAbsolutePath()}.
	 * 
	 * @param pathString The path string to convert
	 * @return The URI representing the provided path string
	 */
	public static URI toURI(String pathString) {
		if (File.separatorChar != '/')
			pathString = pathString.replace(File.separatorChar, '/');
		final int length = pathString.length();
		StringBuffer pathBuf = new StringBuffer(length + 1);
		//There must be a leading slash in a hierarchical URI
		if (length > 0 && (pathString.charAt(0) != '/'))
			pathBuf.append('/');
		//additional double-slash for UNC paths to distinguish from host separator
		if (pathString.startsWith("//")) //$NON-NLS-1$
			pathBuf.append('/').append('/');
		pathBuf.append(pathString);
		try {
			return new URI(EFS.SCHEME_FILE, null, pathBuf.toString(), null);
		} catch (URISyntaxException e) {
			//try java.io implementation
			return new File(pathString).toURI();
		}
	}
}
