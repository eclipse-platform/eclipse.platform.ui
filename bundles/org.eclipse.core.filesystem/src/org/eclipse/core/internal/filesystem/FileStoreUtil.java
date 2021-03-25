/*******************************************************************************
 * Copyright (c) 2021  Joerg Kubitz and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Joerg Kubitz - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.filesystem;

import java.net.URI;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Provides internal utility functions for comparing FileStores and paths
 */
public final class FileStoreUtil {

	private FileStoreUtil() {
		// Not to be instantiated
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
}
