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
package org.eclipse.ui.ide;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * IDEEncoding is a utility class for managing encoding information that
 * includes user preferences from the IDE and core resources.
 * 
 * @see org.eclipse.ui.WorkbenchEncoding
 * @see org.eclipse.core.resources.ResourcesPlugin
 */
public class IDEEncoding {

	//The preference for the user entered encodings.
	private static String IDE_ENCODINGS_PREFERENCE = "IDE_ENCODINGS_PREFERENCE"; //$NON-NLS-1$

	private static String PREFERENCE_SEPARATOR = "'"; //$NON-NLS-1$

	/**
	 * Display constant for the UTF 8 byte order marker for resources.
	 */
	public static String BOM_UTF_8 = "UTF-8 (BOM)";//$NON-NLS-1$

	/**
	 * Display constant for the UTF 16 big endian byte order marker for
	 * resources.
	 */
	public static String BOM_UTF_16BE = "UTF-16 Big-Endian (BOM)";//$NON-NLS-1$

	/**
	 * Display constant for the UTF 16 little endian byte order marker for
	 * resources.
	 */
	public static String BOM_UTF_16LE = "UTF-16 Little-Endian (BOM)";//$NON-NLS-1$

	/**
	 * Get all of the available encodings including any that were saved as a
	 * preference in the IDE or in core resources.
	 * 
	 * @return List of String
	 */
	public static List getIDEEncodings() {
		List encodings = WorkbenchEncoding.getStandardEncodings();

		String[] userEncodings = getIDEEncodingsPreference();
		for (int i = 0; i < userEncodings.length; i++) {
			encodings.add(userEncodings[i]);

		}
		
		String enc = getResourceEncoding();

		if (!(enc == null || encodings.contains(enc))) 
			encodings.add(enc);
		
		Collections.sort(encodings);
		return encodings;
	}

	/**
	 * Get the current value of the encoding preference. If the value is not set
	 * return <code>null</code>.
	 * 
	 * @return String
	 */
	public static String getResourceEncoding() {
		String preference = ResourcesPlugin.getPlugin().getPluginPreferences()
				.getString(ResourcesPlugin.PREF_ENCODING);
		if (preference == null || preference.length() == 0)
			return null;
		return preference;
	}

	/**
	 * Set the resource encoding to be value. Add the encoding to the list of
	 * encodings stored by the workbench.
	 * 
	 * @param value
	 *            String or <code>null</code> if the preference is to be reset
	 *            to the default.
	 */
	public static void setResourceEncoding(String value) {

		// set the workspace text file encoding
		Preferences resourcePrefs = ResourcesPlugin.getPlugin()
				.getPluginPreferences();
		if (value == null)
			resourcePrefs.setToDefault(ResourcesPlugin.PREF_ENCODING);
		else {
			resourcePrefs.setValue(ResourcesPlugin.PREF_ENCODING, value);
			addIDEEncoding(value);
		}

		ResourcesPlugin.getPlugin().savePluginPreferences();

	}

	/**
	 * Add value to the list of workbench encodings.
	 * 
	 * @param value
	 */
	public static void addIDEEncoding(String value) {
		
		if(WorkbenchEncoding.getStandardEncodings().contains(value))
			return;
		
		String[] currentEncodings = getIDEEncodingsPreference();

		boolean addValue = true;

		StringBuffer result = new StringBuffer();
		for (int i = 0; i < currentEncodings.length; i++) {
			String string = currentEncodings[i];
			result.append(string);
			result.append(PREFERENCE_SEPARATOR);
			if (string.equals(value))
				addValue = false;
		}

		if (addValue)
			result.append(value);

		IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(
				IDE_ENCODINGS_PREFERENCE, result.toString());

	}

	/**
	 * Get the value of the encodings preference.
	 * 
	 * @return List
	 */
	private static String[] getIDEEncodingsPreference() {
		String encodings = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getString(IDE_ENCODINGS_PREFERENCE);

		if (encodings == null || encodings.length() == 0)
			return new String[0];

		return encodings.split(PREFERENCE_SEPARATOR);//$NON-NLS-1$
	}

	/**
	 * Clear the IDE encodings preference.
	 */
	public static void clearUserEncodings() {
		IDEWorkbenchPlugin.getDefault().getPreferenceStore().setToDefault(
				IDE_ENCODINGS_PREFERENCE);
	}

	/**
	 * Get the displayable string for the byte order marking from the supplied
	 * file description.
	 * 
	 * @param description
	 *            The description to query. May be <code>null</code>.
	 * @return String or <code>null</code> if the byte order mark cannot be
	 *         found or the description is <code>null</code>.
	 * @see IContentDescription#getProperty(org.eclipse.core.runtime.QualifiedName)
	 */
	public static String getByteOrderMarkLabel(IContentDescription description) {

		if (description == null)
			return null;

		byte[] bom = (byte[]) description
				.getProperty(IContentDescription.BYTE_ORDER_MARK);
		if (bom == null)
			return (String) description
					.getProperty(IContentDescription.CHARSET);
		if (bom == IContentDescription.BOM_UTF_8)
			return IDEEncoding.BOM_UTF_8;
		if (bom == IContentDescription.BOM_UTF_16BE)
			return IDEEncoding.BOM_UTF_16BE;
		if (bom == IContentDescription.BOM_UTF_16LE)
			return IDEEncoding.BOM_UTF_16LE;

		return null;
	}

}