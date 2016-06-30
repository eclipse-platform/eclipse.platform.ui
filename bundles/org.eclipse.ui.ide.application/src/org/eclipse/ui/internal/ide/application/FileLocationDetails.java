package org.eclipse.ui.internal.ide.application;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;

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
	// vars are package protected for access from DelayedEventsProcessor
	Path path;
	IFileStore fileStore;
	IFileInfo fileInfo;

	int line = -1;
	int column = -1;

	/**
	 * Check if path exists with optional encoded line and/or column
	 * specification
	 *
	 * @param path
	 *            the possibly-encoded file path with optional line/column
	 *            details
	 * @return the location details or {@code null} if the file doesn't exist
	 */
	public static FileLocationDetails resolve(String path) {
		FileLocationDetails details = checkLocation(path, -1, -1);
		if (details != null) {
			return details;
		}
		// Ideally we'd use a regex, except that we need to be greedy
		// in matching. For example, we're trying to open /tmp/foo:3:3
		// and there is an actual file named /tmp/foo:3
		Pattern lPattern = Pattern.compile("^(?<path>.*?)[+:](?<line>\\d+)$"); //$NON-NLS-1$
		Pattern lcPattern = Pattern.compile("^(?<path>.*?)[+:](?<line>\\d+)[:+](?<column>\\d+)$"); //$NON-NLS-1$
		Matcher m = lPattern.matcher(path);
		if (m.matches()) {
			try {
				details = checkLocation(m.group("path"), Integer.parseInt(m.group("line")), -1); //$NON-NLS-1$//$NON-NLS-2$
				if (details != null) {
					return details;
				}
			} catch (NumberFormatException e) {
				// shouldn't happen
			}

		}
		m = lcPattern.matcher(path);
		if (m.matches()) {
			try {
				details = checkLocation(m.group("path"), Integer.parseInt(m.group("line")), //$NON-NLS-1$//$NON-NLS-2$
						m.group("column") != null ? Integer.parseInt(m.group("column")) : -1); //$NON-NLS-1$ //$NON-NLS-2$
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

	/** Return details if {@code path} exists */
	private static FileLocationDetails checkLocation(String path, int line, int column) {
		FileLocationDetails spec = new FileLocationDetails();
		spec.path = new Path(path);
		spec.fileStore = EFS.getLocalFileSystem().getStore(spec.path);
		spec.fileInfo = spec.fileStore.fetchInfo();
		spec.line = line;
		spec.column = column;
		return spec.fileInfo.exists() ? spec : null;
	}
}
