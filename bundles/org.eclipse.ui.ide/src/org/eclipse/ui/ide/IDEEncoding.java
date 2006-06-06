/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.ide;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * IDEEncoding is a utility class for managing encoding information that
 * includes user preferences from the IDE and core resources.
 * <p>
 * This class provides all its functionality via static methods.
 * It is not intended to be instantiated or subclassed.
 * </p>
 * 
 * @see org.eclipse.ui.WorkbenchEncoding
 * @see org.eclipse.core.resources.ResourcesPlugin
 * @since 3.1
 */
public final class IDEEncoding {
	
	private IDEEncoding () {
        // prevent instantiation
	}

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
		List encodings = getIDEEncodingsPreference();
		encodings.addAll(WorkbenchEncoding.getDefinedEncodings());

		String enc = getResourceEncoding();

		if (!(enc == null || encodings.contains(enc))) {
			encodings.add(enc);
		}

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
		String preference = ResourcesPlugin.getPlugin().getPluginPreferences().getString(
				ResourcesPlugin.PREF_ENCODING);
		if (preference == null || preference.length() == 0) {
			return null;
		}
		return preference;
	}

	/**
	 * Add value to the list of workbench encodings.
	 * 
	 * @param value
	 */
	public static void addIDEEncoding(String value) {

		if (WorkbenchEncoding.getDefinedEncodings().contains(value)) {
			return;
		}

		writeEncodingsPreference(value, getIDEEncodingsPreference());

	}

	/**
	 * Write the encodings preference. If value is not null
	 * and not already in the list of currentEncodings add
	 * it to the list.
	 * @param value String or <code>null</code>
	 * @param encodings The list of encodings to write
	 */
	private static void writeEncodingsPreference(String value, Collection encodings) {
		boolean addValue = (value != null);

		StringBuffer result = new StringBuffer();

		Iterator currentEncodings = encodings.iterator();

		while (currentEncodings.hasNext()) {
			String string = (String) currentEncodings.next();
			result.append(string);
			result.append(PREFERENCE_SEPARATOR);
			if (addValue && string.equals(value)) {
				addValue = false;
			}
		}

		if (addValue) {
			result.append(value);
		}

		IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(IDE_ENCODINGS_PREFERENCE,
				result.toString());
	}

	/**
	 * Get the value of the encodings preference.
	 * 
	 * @return List
	 */
	private static List getIDEEncodingsPreference() {
		
		boolean updateRequired = false;
		
		String encodings = IDEWorkbenchPlugin.getDefault().getPreferenceStore().getString(
				IDE_ENCODINGS_PREFERENCE);

		if (encodings == null || encodings.length() == 0) {
			return new ArrayList();
		}

		String[] preferenceEncodings = encodings.split(PREFERENCE_SEPARATOR);
		ArrayList result = new ArrayList();

		//Drop any encodings that are not valid
		for (int i = 0; i < preferenceEncodings.length; i++) {
			String string = preferenceEncodings[i];
			boolean isSupported;
			try {
				isSupported = Charset.isSupported(string);
			} catch (IllegalCharsetNameException e) {
				isSupported = false;
			}
			if (isSupported) {
				result.add(string);
			} else{
				WorkbenchPlugin.log(NLS.bind(IDEWorkbenchMessages.WorkbenchEncoding_invalidCharset, string));
				updateRequired = true;
			}
				
		}
		
		if(updateRequired) {
			writeEncodingsPreference(null, result);
		}
		return result;

	}

	/**
	 * Clear the IDE encodings preference.
	 */
	public static void clearUserEncodings() {
		IDEWorkbenchPlugin.getDefault().getPreferenceStore().setToDefault(IDE_ENCODINGS_PREFERENCE);
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

		if (description == null) {
			return null;
		}

		byte[] bom = (byte[]) description.getProperty(IContentDescription.BYTE_ORDER_MARK);
		if (bom == null) {
			return null;
		}
		if (bom == IContentDescription.BOM_UTF_8) {
			return IDEEncoding.BOM_UTF_8;
		}
		if (bom == IContentDescription.BOM_UTF_16BE) {
			return IDEEncoding.BOM_UTF_16BE;
		}
		if (bom == IContentDescription.BOM_UTF_16LE) {
			return IDEEncoding.BOM_UTF_16LE;
		}

		return null;
	}

}
