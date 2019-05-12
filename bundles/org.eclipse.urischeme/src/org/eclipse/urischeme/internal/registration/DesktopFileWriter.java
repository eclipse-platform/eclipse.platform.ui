/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 * Used to change the MimeType property of a Linux .desktop file. Adds handler
 * entries for uri schemes like "x-scheme-handler/myScheme;". Can also remove
 * schemes.
 */
public class DesktopFileWriter {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
	private static final String EQUAL_SIGN = "="; //$NON-NLS-1$
	private static final String KEY_MIME_TYPE = "MimeType"; //$NON-NLS-1$
	private static final String KEY_EXEC = "Exec"; //$NON-NLS-1$
	private static final String EXEC_URI_PLACEHOLDER = " %u"; //$NON-NLS-1$
	private Map<String, String> properties;

	/**
	 * Creates an instance of the DekstopFileWriter. Throws an
	 * {@link IllegalStateException} if the given lines is not a .desktop file. E.g.
	 * no "[Desktop Entry]" at the beginning
	 *
	 * @param lines The lines of the .desktop file (e.g. read with java.nio.Files
	 *
	 * @throws IllegalStateException if lines cannot be understood as .desktop file
	 */
	public DesktopFileWriter(List<String> lines) {
		properties = getProperties(lines);
	}

	/**
	 * Checks if the given scheme is registered in this .desktop file
	 *
	 * @param scheme that should be checked for registration
	 * @return true if scheme is in the value of the MimeType property of given
	 *         .desktop file; false otherwise
	 */
	public boolean isRegistered(String scheme) {
		Util.assertUriSchemeIsLegal(scheme);
		String mimeType = properties.get(KEY_MIME_TYPE);
		if (mimeType == null || mimeType.isEmpty()) {
			return false;
		}
		return mimeType.contains(getHandlerPlusScheme(scheme));
	}

	/**
	 * Adds an entry "x-scheme-handler/givenScheme;" to the MimeType property of the
	 * .desktop file. Creates the MimeType property if not yet existing. Otherwise
	 * adds to the entry separated by ";".
	 *
	 * @param scheme The uri scheme which should be handled by the application
	 *               mentioned in the .desktop file.
	 *
	 * @throws IllegalArgumentException if the given scheme contains illegal
	 *                                  characters
	 *
	 * @see #removeScheme(String)
	 *
	 * @see <a href= "https://tools.ietf.org/html/rfc3986#section-3.1">Uniform
	 *      Resource Identifier (URI): Generic Syntax</a>
	 *
	 */
	public void addScheme(String scheme) {
		// check precondition
		Util.assertUriSchemeIsLegal(scheme);

		String handlerPlusScheme = getHandlerPlusScheme(scheme);

		if (properties.containsKey(KEY_MIME_TYPE)) {
			String mimeType = properties.get(KEY_MIME_TYPE);
			if (!mimeType.contains(handlerPlusScheme)) {
				mimeType += handlerPlusScheme;
				properties.put(KEY_MIME_TYPE, mimeType);
			}
		} else {
			properties.put(KEY_MIME_TYPE, handlerPlusScheme);
		}
	}

	/**
	 * Removes the corresponding handler ("x-scheme-handler/givenScheme;") for the
	 * given scheme from the MimeType property of the .desktop file. Removes the
	 * MimeType property completely if it is empty after removal.
	 *
	 * @param scheme The uri scheme which should not be handled anymore by the
	 *               application mentioned in the .desktop file.
	 *
	 * @throws IllegalArgumentException if the given scheme contains illegal
	 *                                  characters
	 *
	 * @see #addScheme(String)
	 *
	 * @see <a href=
	 *      "https://tools.ietf.org/html/rfc3986#section-3.1">https://tools.ietf.org/html/rfc3986#section-3.1</a>
	 *
	 */
	public void removeScheme(String scheme) {
		Util.assertUriSchemeIsLegal(scheme);

		if (properties.containsKey(KEY_MIME_TYPE)) {

			String handlerPlusScheme = getHandlerPlusScheme(scheme);

			String mimeType = properties.get(KEY_MIME_TYPE);
			mimeType = mimeType.replace(handlerPlusScheme, ""); //$NON-NLS-1$
			if (mimeType.isEmpty()) {
				properties.remove(KEY_MIME_TYPE);
			} else {
				properties.put(KEY_MIME_TYPE, mimeType);
			}
		}
	}

	/**
	 * Returns a byte array with all the properties and the changed MimeType
	 * property.
	 *
	 * @return the new file content as byte[]
	 */
	public byte[] getResult() {

		addUriPlaceholderToExecProperty();

		Function<Entry<String, String>, String> toList = (Entry<String, String> e) -> {
			if (e.getValue() == null) {
				return e.getKey();
			}
			return String.join(EQUAL_SIGN, e.getKey(), e.getValue());
		};
		String result = this.properties.entrySet().stream() //
				.map(toList) //
				.collect(joining(LINE_SEPARATOR));

		return result.getBytes();
	}

	/**
	 * Returns the minimal content for a .desktop file needed for registering mime
	 * types in Linux.<br />
	 *
	 * The caller can call {@link #DesktopFileWriter(List)} afterwards to create an
	 * instance.
	 *
	 * @param eclipseExecutableLocation the location of the eclipse executable in
	 *                                  the file system, spaces will be escaped
	 * @param productName               the name of the product as defined in the
	 *                                  branding
	 *
	 * @return The minimal file content as list (one entry = one file line)
	 */
	public static List<String> getMinimalDesktopFileContent(String eclipseExecutableLocation, String productName) {
		String executable = escapeSpaces(eclipseExecutableLocation);
		return Arrays.asList(//
				"[Desktop Entry]", //$NON-NLS-1$
				"Name=" + productName, //$NON-NLS-1$
				"Exec=" + executable, //$NON-NLS-1$
				"NoDisplay=true", //$NON-NLS-1$
				"Type=Application" //$NON-NLS-1$
		);
	}

	private static String escapeSpaces(String path) {
		return path.replace(" ", "\\ "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static String unescapeSpaces(String path) {
		return path.replace("\\ ", " "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void addUriPlaceholderToExecProperty() {
		if (this.properties.containsKey(KEY_EXEC)) {
			String execValue = this.properties.get(KEY_EXEC);
			if (!execValue.contains(EXEC_URI_PLACEHOLDER)) {
				this.properties.put(KEY_EXEC, execValue + EXEC_URI_PLACEHOLDER);
			}
		}

	}

	private Map<String, String> getProperties(List<String> lines) {
		assertLinesNotEmpty(lines);

		LinkedHashMap<String, String> props = new LinkedHashMap<>(); // keeps order
		for (String line : lines) {
			if (line.contains(EQUAL_SIGN)) {
				String[] split = line.split(EQUAL_SIGN);
				props.put(split[0], split[1]);
			} else {
				props.put(line, null);
			}
		}

		assertDesktopEntryPresent(props);

		return props;
	}

	private void assertDesktopEntryPresent(Map<String, String> props) {
		Iterator<Entry<String, String>> iterator = props.entrySet().iterator();
		String firstLine = iterator.next().getKey();
		if ("[Desktop Entry]".equals(firstLine) == false) { //$NON-NLS-1$
			throw new IllegalStateException("File seems not to be a 'desktop' file"); //$NON-NLS-1$
		}
	}

	private void assertLinesNotEmpty(List<?> lines) {
		if (lines.isEmpty()) {
			throw new IllegalStateException("inputStream is empty"); //$NON-NLS-1$
		}
	}

	private String getHandlerPlusScheme(String scheme) {
		return "x-scheme-handler/" + scheme + ";";//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @return the location of the Eclipse executable for the running Eclipse
	 */
	public String getExecutableLocation() {
		String executableLocation = properties.get(KEY_EXEC);
		executableLocation = executableLocation.replace(EXEC_URI_PLACEHOLDER, ""); //$NON-NLS-1$ // cut uri placeholder
		return unescapeSpaces(executableLocation);
	}
}