/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ui.editors.text;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.Platform;

/**
 * The EncodingDefinitionManager is a class that reads the encodings
 * defined by the encodings extension and stores them as well as
 * any user defined encodings.
 */
public class EncodingDefinitionManager {

	private static List fgEncodings = null;
	private static final String USER_ENCODING_ID_PREFIX = "org.eclipse.ui.user."; //$NON-NLS-1$
	private static final String DEFAULT_ENCODING_ID = "org.eclipse.ui.systemDefaultEncoding"; //$NON-NLS-1$

	/**
	 * Get all of the currently defined encodings in the system.
	 * @return Collection of EncodingDefinition
	 */

	public static Collection getEncodings() {
		if (fgEncodings == null) {
			fgEncodings =
				new EncodingRegistryReader().readRegistry(
					Platform.getPluginRegistry());

			//Add the system default as well
			String defaultEnc = getDefaultEncoding();
			if (!hasEncodingWithValue(defaultEnc))
				fgEncodings.add(
					new EncodingDefinition(
						DEFAULT_ENCODING_ID,
						defaultEnc,
						defaultEnc));
		}
		return fgEncodings;
	}

	/**
	 * Get all of the currently defined encodings in the system
	 * sorted in order of thier labels.
	 * @return Collection of EncodingDefinition
	 */

	public static SortedSet getLabelSortedEncodings() {

		Comparator comparator = new Comparator() {

			/*
			 * @see Comparator.compare(Object,Object)
			 */
			public int compare(Object object1, Object object2) {
				EncodingDefinition def1 = (EncodingDefinition) object1;
				EncodingDefinition def2 = (EncodingDefinition) object2;
				return def1.getLabel().compareTo(def2.getLabel());
			}
			
			/*
			 * @see Comparator.equals(Object,Object)
			 */
			public boolean equals(Object object1, Object object2) {
				EncodingDefinition def1 = (EncodingDefinition) object1;
				EncodingDefinition def2 = (EncodingDefinition) object2;
				return def1.getLabel().equals(def2.getLabel());
			}

		};

		SortedSet result = new TreeSet(comparator);
		result.addAll(getEncodings());
		return result;

	}

	/**
	 * Return the current default encoding.
	 * @return String - the system value of the default encoding.
	 */
	public static String getDefaultEncoding() {
		return System.getProperty("file.encoding", "UTF-8"); //$NON-NLS-1$
	}

	/**
	 * Return whether or not there is currently an encoding with the
	 * supplied value.
	 * @param String - the value we are checking for
	 * @return boolean - true if there is a defined encoding with that value.
	 */
	private static boolean hasEncodingWithValue(String encodingString) {
		Iterator iterator = getEncodings().iterator();

		while (iterator.hasNext()) {
			EncodingDefinition next = (EncodingDefinition) iterator.next();
			if (next.getValue().equals(encodingString)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Add a new encoding for the encodingString value to the list
	 * of encodings.
	 * @param String encodingString - the value of the new encoding
	 */
	public static void addEncoding(String encodingString) {
		if (!hasEncodingWithValue(encodingString))
			fgEncodings.add(
				new EncodingDefinition(
					USER_ENCODING_ID_PREFIX + encodingString,
					encodingString,
					encodingString));
	}

}
