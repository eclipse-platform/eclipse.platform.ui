/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text.formatter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Default implementation of <code>IFormattingContext</code>.
 *
 * @since 3.0
 */
public class FormattingContext implements IFormattingContext {

	/** Map to store the properties */
	private final Map fMap= new HashMap();

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#dispose()
	 */
	public void dispose() {
		fMap.clear();
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#getPreferenceKeys()
	 */
	public String[] getPreferenceKeys() {
		return new String[] {
		};
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#getProperty(java.lang.Object)
	 */
	public Object getProperty(Object key) {
		return fMap.get(key);
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#isBooleanPreference(java.lang.String)
	 */
	public boolean isBooleanPreference(String key) {
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#isDoublePreference(java.lang.String)
	 */
	public boolean isDoublePreference(String key) {
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#isFloatPreference(java.lang.String)
	 */
	public boolean isFloatPreference(String key) {
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#isIntegerPreference(java.lang.String)
	 */
	public boolean isIntegerPreference(String key) {
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#isLongPreference(java.lang.String)
	 */
	public boolean isLongPreference(String key) {
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#isStringPreference(java.lang.String)
	 */
	public boolean isStringPreference(String key) {
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#mapToStore(java.util.Map, org.eclipse.jface.preference.IPreferenceStore)
	 */
	public void mapToStore(Map map, IPreferenceStore store) {

		final String[] preferences= getPreferenceKeys();

		String result= null;
		String preference= null;

		for (int index= 0; index < preferences.length; index++) {

			preference= preferences[index];
			result= (String)map.get(preference);

			if (result != null) {

				try {
					if (isBooleanPreference(preference)) {
						store.setValue(preference, result.equals(IPreferenceStore.TRUE));
					} else if (isIntegerPreference(preference)) {
						store.setValue(preference, Integer.parseInt(result));
					} else if (isStringPreference(preference)) {
						store.setValue(preference, result);
					} else if (isDoublePreference(preference)) {
						store.setValue(preference, Double.parseDouble(result));
					} else if (isFloatPreference(preference)) {
						store.setValue(preference, Float.parseFloat(result));
					} else if (isLongPreference(preference)) {
						store.setValue(preference, Long.parseLong(result));
					}
				} catch (NumberFormatException exception) {
					// Do nothing
				}
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#setProperty(java.lang.Object, java.lang.Object)
	 */
	public void setProperty(Object key, Object property) {
		fMap.put(key, property);
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#storeToMap(org.eclipse.jface.preference.IPreferenceStore, java.util.Map, boolean)
	 */
	public void storeToMap(IPreferenceStore store, Map map, boolean useDefault) {

		final String[] preferences= getPreferenceKeys();

		String preference= null;
		for (int index= 0; index < preferences.length; index++) {

			preference= preferences[index];

			if (isBooleanPreference(preference)) {
				map.put(preference, (useDefault ? store.getDefaultBoolean(preference) : store.getBoolean(preference)) ? IPreferenceStore.TRUE : IPreferenceStore.FALSE);
			} else if (isIntegerPreference(preference)) {
				map.put(preference, String.valueOf(useDefault ? store.getDefaultInt(preference) : store.getInt(preference)));
			} else if (isStringPreference(preference)) {
				map.put(preference, useDefault ? store.getDefaultString(preference) : store.getString(preference));
			} else if (isDoublePreference(preference)) {
				map.put(preference, String.valueOf(useDefault ? store.getDefaultDouble(preference) : store.getDouble(preference)));
			} else if (isFloatPreference(preference)) {
				map.put(preference, String.valueOf(useDefault ? store.getDefaultFloat(preference) : store.getFloat(preference)));
			} else if (isLongPreference(preference)) {
				map.put(preference, String.valueOf(useDefault ? store.getDefaultLong(preference) : store.getLong(preference)));
			}
		}
	}
}
