/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * WorkbenchEncoding is a utility class for plug-ins that want to use the list
 * of encodings defined by default in the workbench.
 */
public class WorkbenchEncoding {

	/**
	 * Workbench constant for ISO-8859-1
	 */
	public static String ISO_8859_1 = "ISO-8859-1";//$NON-NLS-1$

	/**
	 * Workbench constant for UTF-8
	 */
	public static String UTF_8 = "UTF-8";//$NON-NLS-1$

	/**
	 * Workbench constant for UTF-16
	 */
	public static String UTF_16 = "UTF-16";//$NON-NLS-1$

	/**
	 * Workbench constant for UTF-16 big endian
	 */
	public static String UTF_16_BIG_ENDIAN = "UTF-16BE";//$NON-NLS-1$

	/**
	 * Workbench constant for UTF-16 little endian
	 */
	public static String UTF_16_LITTLE_ENDIAN = "UTF-16LE";//$NON-NLS-1$

	/**
	 * Workbench constant for US ASCII
	 */
	public static String US_ASCII = "US-ASCII";//$NON-NLS-1$

	/**
	 * Get the default encoding from the virtual machine.
	 * 
	 * @return String
	 */
	public static String getWorkbenchDefaultEncoding() {
		return System.getProperty("file.encoding", "UTF-8");//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Return the list of encodings supplied as standard choices in the
	 * workbench. Include the default encoding as well.
	 * 
	 * @return List of String
	 */
	public static List getStandardEncodings() {
		ArrayList encodings = new ArrayList();

		encodings.add(US_ASCII);
		encodings.add(UTF_16);
		encodings.add(UTF_16_BIG_ENDIAN);
		encodings.add(UTF_16_LITTLE_ENDIAN);
		encodings.add(UTF_8);
		encodings.add(ISO_8859_1);

		String defaultEnc = getWorkbenchDefaultEncoding();

		if (!encodings.contains(defaultEnc)) {
			encodings.add(defaultEnc);
		}

		return encodings;
	}

}