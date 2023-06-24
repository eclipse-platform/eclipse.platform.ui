/*******************************************************************************
 * Copyright (c) 2016 Brian de Alwis and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brian de Alwis - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.application;

import java.nio.file.InvalidPathException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

/**
 * @since 3.3
 *
 */

/**
 * Decode paths with line and column location information. Formats supported
 * include:
 * <ul>
 * <li>filepath:line</li>
 * <li>filepath:line:col</li>
 * <li>filepath:line+col</li>
 * <li>filepath+line</li>
 * <li>filepath+line:col</li>
 * <li>filepath+line+col</li>
 * </ul>
 * Certain OS/WS interpret colons differently (see bug 496845)
 *
 * @see #resolve(String)
 */
public class FileLocationDetails {

	private static final Pattern lPattern = Pattern.compile("^(?<path>.*?)[+:](?<line>\\d+)$"); //$NON-NLS-1$
	private static final Pattern lcPattern = Pattern.compile("^(?<path>.*?)[+:](?<line>\\d+)[:+](?<column>\\d+)$"); //$NON-NLS-1$

	private static String getPath(Matcher m) {
		return m.group("path"); //$NON-NLS-1$
	}

	private static int getValue(String name, Matcher m) {
		return Integer.parseInt(m.group(name));
	}

	// vars are package protected for access from DelayedEventsProcessor
	IPath path;
	IFileStore fileStore;
	IFileInfo fileInfo;

	int line = -1;
	int column = -1;

	/**
	 * Check if path exists with optional encoded line and/or column specification
	 *
	 * @param path the possibly-encoded file path with optional line/column details
	 * @return the location details or {@code null} if the file doesn't exist
	 */
	public static FileLocationDetails resolve(String path) {
		// Ideally we'd use a regex, except that we need to be greedy in matching.
		// For example, we're trying to open /tmp/foo:3:3
		// and there is an actual file named /tmp/foo:3
		if (isValidPath(path)) {
			FileLocationDetails details = checkLocation(path, -1, -1);
			if (details != null) {
				return details;
			}
		}
		Matcher m = lPattern.matcher(path);
		if (m.matches()) {
			try {
				String matchedPath = getPath(m);
				if (isValidPath(matchedPath)) {
					FileLocationDetails details = checkLocation(matchedPath, getValue("line", m), -1); //$NON-NLS-1$
					if (details != null) {
						return details;
					}
				}
			} catch (NumberFormatException e) {
				// shouldn't happen
			}

		}
		m = lcPattern.matcher(path);
		if (m.matches()) {
			try {
				FileLocationDetails details = checkLocation(getPath(m), getValue("line", m), getValue("column", m)); //$NON-NLS-1$//$NON-NLS-2$
				if (details != null) {
					return details;
				}
			} catch (NumberFormatException e) {
				// shouldn't happen invalid line or column
			}
		}
		// no matches on line or line+column
		return null;
	}

	private static boolean isValidPath(String path) {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			try { // On windows : is forbidden in filenames
				java.nio.file.Path.of(path);
			} catch (InvalidPathException e) {
				return false;
			}
		}
		return true;
	}

	/** Return details if {@code path} exists */
	private static FileLocationDetails checkLocation(String path, int line, int column) {
		FileLocationDetails spec = new FileLocationDetails();
		spec.path = IPath.fromOSString(path);
		spec.fileStore = EFS.getLocalFileSystem().getStore(spec.path);
		spec.fileInfo = spec.fileStore.fetchInfo();
		spec.line = line;
		spec.column = column;
		return spec.fileInfo.exists() ? spec : null;
	}
}
