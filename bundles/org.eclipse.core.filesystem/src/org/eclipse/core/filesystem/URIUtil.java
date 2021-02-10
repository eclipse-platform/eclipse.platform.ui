/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
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
package org.eclipse.core.filesystem;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.core.runtime.*;

/**
 * This class provides utility methods for comparing, inspecting, and manipulating
 * URIs.  More specifically, this class is useful for dealing with URIs that represent
 * file systems represented by the <code>org.eclipse.core.filesystem.filesystems</code>
 * extension point. For such URIs the file system implementation can be consulted
 * to interpret the URI in a way that is not possible at a generic level.
 *
 * @since org.eclipse.core.filesystem 1.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class URIUtil {

	/**
	 * Tests two URIs for equality.  This method delegates equality testing
	 * to the registered file system for the URIs.  If either URI does not
	 * have a registered file system, the default {@link URI#equals(Object)}
	 * method is used.
	 *
	 * @param one The first URI to test for equality
	 * @param two The second URI to test for equality
	 * @return <code>true</code> if the first URI is equal to the second,
	 * as defined by the file systems for those URIs, and <code>false</code> otherwise.
	 */
	public static boolean equals(URI one, URI two) {
		try {
			return EFS.getStore(one).equals(EFS.getStore(two));
		} catch (CoreException e) {
			// fall back to default equality test
			return one.equals(two);
		}
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
		// Special treatment for LocalFileSystem. For performance only.
		if (EFS.SCHEME_FILE.equals(uri.getScheme()))
			return new Path(uri.getSchemeSpecificPart());
		// Relative path
		if (uri.getScheme() == null)
			return new Path(uri.getPath());
		// General case
		try {
			IFileStore store = EFS.getStore(uri);
			if (store == null)
				return null;
			File file = store.toLocalFile(EFS.NONE, null);
			if (file == null)
				return null;
			return new Path(file.getAbsolutePath());
		} catch (CoreException e) {
			// Fall through to return null.
		}
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
			return toURI(path.toFile().getAbsolutePath(), true);
		//Must use the relativize method to properly construct a relative URI
		URI base = toURI(Path.ROOT.setDevice(path.getDevice()));
		return base.relativize(toURI(path.makeAbsolute()));
	}

	/**
	 * Converts a String representing a local file system path to a {@link URI}.
	 * For example, this method can be used to create a URI from the output
	 * of {@link File#getAbsolutePath()}. The provided path string is always treated
	 * as an absolute path.
	 *
	 * @param pathString The absolute path string to convert
	 * @return The URI representing the provided path string
	 */
	public static URI toURI(String pathString) {
		IPath path = new Path(pathString);
		return toURI(path);
	}

	/**
	 * Converts a String representing a local file system path to a {@link URI}.
	 * For example, this method can be used to create a URI from the output
	 * of {@link File#getAbsolutePath()}.
	 * <p>
	 * The <code>forceAbsolute</code> flag controls how this method handles
	 * relative paths.  If the value is <code>true</code>, then the input path
	 * is always treated as an absolute path, and the returned URI will be an
	 * absolute URI.  If the value is <code>false</code>, then a relative path
	 * provided as input will result in a relative URI being returned.
	 *
	 * @param pathString The path string to convert
	 * @param forceAbsolute if <code>true</code> the path is treated as an
	 * absolute path
	 * @return The URI representing the provided path string
	 * @since org.eclipse.core.filesystem 1.2
	 */
	public static URI toURI(String pathString, boolean forceAbsolute) {
		if (File.separatorChar != '/')
			pathString = pathString.replace(File.separatorChar, '/');
		final int length = pathString.length();
		StringBuilder pathBuf = new StringBuilder(length + 1);
		//mark if path is relative
		if (length > 0 && (pathString.charAt(0) != '/') && forceAbsolute) {
			pathBuf.append('/');
		}
		//additional double-slash for UNC paths to distinguish from host separator
		if (pathString.startsWith("//")) //$NON-NLS-1$
			pathBuf.append('/').append('/');
		pathBuf.append(pathString);
		try {
			String scheme = null;
			if (length > 0 && (pathBuf.charAt(0) == '/')) {
				scheme = EFS.SCHEME_FILE;
			}
			return new URI(scheme, null, pathBuf.toString(), null);
		} catch (URISyntaxException e) {
			//try java.io implementation
			return new File(pathString).toURI();
		}
	}

	/**
	 * Returns a string representation of the URI in a form suitable for human consumption.
	 *
	 * <p>
	 * The string returned by this method is equivalent to that returned by the
	 * {@link URI#toString()} method except that all sequences of escaped octets are decoded.
	 * </p>
	 *
	 * @param uri The URI to return in string form
	 * @return the string form of the URI
	 * @since org.eclipse.core.filesystem 1.2
	 */
	public static String toDecodedString(URI uri) {
		String scheme = uri.getScheme();
		String part = uri.getSchemeSpecificPart();
		if (scheme == null)
			return part;
		return scheme + ':' + part;
	}

	/**
	 * Compares URIs by normalized IPath
	 * This is the old slow implementation and has a memory hotspot see bug 570896. Prefer to use compareNormalisedUri! 
	 * @since org.eclipse.core.filesystem 1.9
	 */
	public static int comparePathUri(URI uri1, URI uri2) {
		if (uri1 == null && uri2 == null)
			return 0;
		int compare;
		// Fixed compare contract sgn(compare(x, y)) == -sgn(compare(y, x))
		// in case of Exceptions:
		if ((compare = nullsLast(uri1, uri2)) != 0)
			return compare;
		// compare hosts
		compare = compareStringOrNull(uri1.getHost(), uri2.getHost());
		if (compare != 0)
			return compare;
		// compare user infos
		compare = compareStringOrNull(uri1.getUserInfo(), uri2.getUserInfo());
		if (compare != 0)
			return compare;
		// compare ports
		int port1 = uri1.getPort();
		int port2 = uri2.getPort();
		if (port1 != port2)
			return port1 - port2;

		IPath path1 = new Path(uri1.getPath());
		IPath path2 = new Path(uri2.getPath());
		// compare devices
		compare = compareStringOrNull(path1.getDevice(), path2.getDevice());
		if (compare != 0)
			return compare;
		// compare segments
		int segmentCount1 = path1.segmentCount();
		int segmentCount2 = path2.segmentCount();
		for (int i = 0; (i < segmentCount1) && (i < segmentCount2); i++) {
			compare = path1.segment(i).compareTo(path2.segment(i));
			if (compare != 0)
				return compare;
		}
		//all segments are equal, so compare based on number of segments
		compare = segmentCount1 - segmentCount2;
		if (compare != 0)
			return compare;
		//same number of segments, so compare query
		return compareStringOrNull(uri1.getQuery(), uri2.getQuery());
	}

	/**
	 * Compares already normalized URIs 
	 * This is a fast implementation without memory allocation (Bug 570896). 
	 * note: if pathes contain different segment count this is != uri1.compareNormalisedUri(uri2)
	 * @since org.eclipse.core.filesystem 1.9
	 */
	public static int compareNormalisedUri(URI uri1, URI uri2) {
		if (uri1 == null && uri2 == null)
			return 0;
		int c;
		if ((c = nullsLast(uri1, uri2)) != 0)
			return c;
		// avoid to use IPath here due to high ephemeral memory allocation (Bug 570896)
		if ((c = compareStringOrNull(uri1.getAuthority(), uri2.getAuthority())) != 0)
			return c;
		if ((c = compareStringOrNull(uri1.getScheme(), uri2.getScheme())) != 0)
			return c;
		if ((c = comparePathSegments(uri1.getPath(), uri2.getPath())) != 0)
			return c;
		if ((c = compareStringOrNull(uri1.getQuery(), uri2.getQuery())) != 0)
			return c;
		return c;
	}

	static int nullsLast(Object c1, Object c2) {
		if (c1 == null) {
			if (c2 == null)
				return 0;
			return 1;
		}
		if (c2 == null)
			return -1;
		return 0;
	}

	static int comparePathSegments(String p1, String p2) {
		int compare;
		compare = compareSlashFirst(p1, p2);
		if (compare != 0)
			return compare;
		// all segments are equal, so compare based on number of segments
		int segmentCount1 = countCharButNotAtEnd(p1, '/');
		int segmentCount2 = countCharButNotAtEnd(p2, '/');
		compare = segmentCount1 - segmentCount2;
		return compare;
	}

	static int compareSlashFirst(String value, String other) {
		int len1 = value.length();
		int len2 = other.length();
		int lim = Math.min(len1, len2);
		for (int k = 0; k < lim; k++) {
			char c1 = value.charAt(k);
			char c2 = other.charAt(k);
			if (c1 != c2) {
				// '/' first
				if (c1 == '/')
					return -1;
				if (c2 == '/')
					return 1;
				return c1 - c2;
			}
		}
		// ignore "/" at the end
		if (value.endsWith("/")) //$NON-NLS-1$
			len1 -= 1;
		if (other.endsWith("/")) //$NON-NLS-1$
			len2 -= 1;
		return len1 - len2;
	}

	static int countCharButNotAtEnd(String str, char c) {
		int count = 0;
		for (int i = 0; i < str.length() - 1; i++) {
			if (str.charAt(i) == c)
				count++;
		}
		return count;
	}

	/**
	 * Compares two strings that are possibly null.
	 * @since org.eclipse.core.filesystem 1.9
	 */
	public static int compareStringOrNull(String string1, String string2) {
		if (string1 == null) {
			if (string2 == null)
				return 0;
			return 1;
		}
		if (string2 == null)
			return -1;
		return string1.compareTo(string2);
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private URIUtil() {
		super();
	}
}
