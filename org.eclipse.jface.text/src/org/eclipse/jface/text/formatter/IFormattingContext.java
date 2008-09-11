/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text.formatter;

import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Formatting context used in formatting strategies implementing interface
 * <code>IFormattingStrategyExtension</code>.
 *
 * @see IFormattingStrategyExtension
 * @since 3.0
 */
public interface IFormattingContext {

	/**
	 * Dispose of the formatting context.
	 * <p>
	 * Must be called after the formatting context has been used in a formatting process.
	 */
	void dispose();

	/**
	 * Returns the preference keys used for the retrieval of formatting preferences.
	 *
	 * @return The preference keys for formatting
	 */
	String[] getPreferenceKeys();

	/**
	 * Retrieves the property <code>key</code> from the formatting context
	 *
	 * @param key the key of the property to store in the context
	 * @return the property <code>key</code> if available, <code>null</code> otherwise
	 */
	Object getProperty(Object key);

	/**
	 * Is this preference key for a boolean preference?
	 *
	 * @param key the preference key to query its type
	 * @return <code>true</code> iff this key is for a boolean preference, <code>false</code>
	 *         otherwise.
	 */
	boolean isBooleanPreference(String key);

	/**
	 * Is this preference key for a double preference?
	 *
	 * @param key the preference key to query its type
	 * @return <code>true</code> iff this key is for a double preference, <code>false</code>
	 *         otherwise.
	 */
	boolean isDoublePreference(String key);

	/**
	 * Is this preference key for a float preference?
	 *
	 * @param key The preference key to query its type
	 * @return <code>true</code> iff this key is for a float preference, <code>false</code>
	 *         otherwise.
	 */
	boolean isFloatPreference(String key);

	/**
	 * Is this preference key for an integer preference?
	 *
	 * @param key The preference key to query its type
	 * @return <code>true</code> iff this key is for an integer preference, <code>false</code>
	 *         otherwise.
	 */
	boolean isIntegerPreference(String key);

	/**
	 * Is this preference key for a long preference?
	 *
	 * @param key The preference key to query its type
	 * @return <code>true</code> iff this key is for a long preference, <code>false</code>
	 *         otherwise.
	 */
	boolean isLongPreference(String key);

	/**
	 * Is this preference key for a string preference?
	 *
	 * @param key The preference key to query its type
	 * @return <code>true</code> iff this key is for a string preference, <code>false</code>
	 *         otherwise.
	 */
	boolean isStringPreference(String key);

	/**
	 * Stores the preferences from a map to a preference store.
	 * <p>
	 * Note that the preference keys returned by {@link #getPreferenceKeys()} must not be used in
	 * the preference store. Otherwise the preferences are overwritten.
	 * </p>
	 *
	 * @param map Map to retrieve the preferences from
	 * @param store Preference store to store the preferences in
	 */
	void mapToStore(Map map, IPreferenceStore store);

	/**
	 * Stores the property <code>key</code> in the formatting context.
	 *
	 * @param key Key of the property to store in the context
	 * @param property Property to store in the context. If already present, the new property
	 *            overwrites the present one.
	 */
	void setProperty(Object key, Object property);

	/**
	 * Retrieves the preferences from a preference store in a map.
	 * <p>
	 * Note that the preference keys returned by {@link #getPreferenceKeys()} must not be used in
	 * the map. Otherwise the preferences are overwritten.
	 * </p>
	 *
	 * @param store Preference store to retrieve the preferences from
	 * @param map Map to store the preferences in
	 * @param useDefault <code>true</code> if the default preferences should be used,
	 *            <code>false</code> otherwise
	 */
	void storeToMap(IPreferenceStore store, Map map, boolean useDefault);
}
