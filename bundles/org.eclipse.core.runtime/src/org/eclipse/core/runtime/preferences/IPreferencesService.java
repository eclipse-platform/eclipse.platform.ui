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
package org.eclipse.core.runtime.preferences;

import java.io.InputStream;
import java.io.OutputStream;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.osgi.service.prefs.Preferences;

/**
 * The preference service provides facilities for dealing with the default scope
 * precedence lookup order, querying the preference store for values using this order,
 * accessing the root of the preference store node hierarchy, and importing/exporting
 * preferences.
 * 
 * @since 3.0
 */
public interface IPreferencesService {

	/**
	 * String constant (value of <code>"instance"</code>) used for the 
	 * scope name for the instance preference scope.
	 */
	public static final String SCOPE_INSTANCE = "instance"; //$NON-NLS-1$

	/**
	 * String constant (value of <code>"configuration"</code>) used for the 
	 * scope name for the configuration preference scope.
	 */
	public static final String SCOPE_CONFIGURATION = "configuration"; //$NON-NLS-1$

	/**
	 * String constant (value of <code>"user"</code>) used for the 
	 * scope name for the user preference scope.
	 */
	public static final String SCOPE_USER = "user"; //$NON-NLS-1$

	/**
	 * String constant (value of <code>"default"</code>) used for the 
	 * scope name for the default preference scope.
	 */
	public static final String SCOPE_DEFAULT = "default"; //$NON-NLS-1$

	/**
	 * Lookup the given key in the specified preference nodes in the given order.
	 * Return the set value from the first node the key is found in. If the key is not
	 * defined in any of the given nodes, then return the specified default value.
	 * Immediately returns the default value if the node list is <code>null</code>.
	 * 
	 * @param key the preference key
	 * @param defaultValue the default value
	 * @param nodes the list of nodes to search
	 * @return the stored preference value or the specified default value 
	 * @see org.osgi.service.prefs.Preferences
	 */
	public String get(String key, String defaultValue, Preferences[] nodes);

	/**
	 * Return the value stored in the preference store for the given key. 
	 * If the key is not defined then return the specified default value. 
	 * Use the canonical scope lookup order for finding the preference value. 
	 * <p>
	 * The semantics of this method are to calculate the appropriate 
	 * <code>Preference</code> nodes in the preference hierarchy to use
	 * and then call the <code>get(String, String, Preferences[])</code> 
	 * method. The order of the nodes is calculated by consulting the default 
	 * scope lookup order as set by <code>setDefaultLookupOrder(String, String)</code>.
	 * </p><p>
	 * Callers may specify an array of scope context objects to aid in the 
	 * determination of the correct nodes. For each entry in the lookup 
	 * order, the array of contexts is consulted and if one matching the 
	 * scope exists, then it is used to calculate the node. Otherwise a
	 * default calculation algorithm is used. 
	 * </p><p>
	 * An example of a qualifier for an Eclipse 2.1 preference is the
	 * plug-in identifier. (e.g. "org.eclipse.core.resources" for "description.autobuild")
	 * </p>
	 * @param qualifier a namespace qualifier for the preference
	 * @param key the name of the preference
	 * @param defaultValue the value to use if the preference is not defined
	 * @param contexts optional context objects to help scopes determine which nodes to search, or <code>null</code>
	 * @return the value of the preference or the given default value
	 * @see IScopeContext
	 * @see #get(java.lang.String, java.lang.String, org.osgi.service.prefs.Preferences[])
	 * @see #getLookupOrder(java.lang.String, java.lang.String)
	 * @see #getDefaultLookupOrder(java.lang.String, java.lang.String)
	 */
	public boolean getBoolean(String qualifier, String key, boolean defaultValue, IScopeContext[] contexts);

	/**
	 * Return the value stored in the preference store for the given key. 
	 * If the key is not defined then return the specified default value. 
	 * Use the canonical scope lookup order for finding the preference value. 
	 * <p>
	 * The semantics of this method are to calculate the appropriate 
	 * <code>Preference</code> nodes in the preference hierarchy to use
	 * and then call the <code>get(String, String, Preferences[])</code> 
	 * method. The order of the nodes is calculated by consulting the default 
	 * scope lookup order as set by <code>setDefaultLookupOrder(String, String)</code>.
	 * </p><p>
	 * Callers may specify an array of scope context objects to aid in the 
	 * determination of the correct nodes. For each entry in the lookup 
	 * order, the array of contexts is consulted and if one matching the 
	 * scope exists, then it is used to calculate the node. Otherwise a
	 * default calculation algorithm is used. 
	 * </p><p>
	 * An example of a qualifier for an Eclipse 2.1 preference is the
	 * plug-in identifier. (e.g. "org.eclipse.core.resources" for "description.autobuild")
	 * </p>
	 * @param qualifier a namespace qualifier for the preference
	 * @param key the name of the preference
	 * @param defaultValue the value to use if the preference is not defined
	 * @param contexts optional context objects to help scopes determine which nodes to search, or <code>null</code>
	 * @return the value of the preference or the given default value
	 * @see IScopeContext
	 * @see #get(java.lang.String, java.lang.String, org.osgi.service.prefs.Preferences[])
	 * @see #getLookupOrder(java.lang.String, java.lang.String)
	 * @see #getDefaultLookupOrder(java.lang.String, java.lang.String)
	 */
	public byte[] getByteArray(String qualifier, String key, byte[] defaultValue, IScopeContext[] contexts);

	/**
	 * Return the value stored in the preference store for the given key. 
	 * If the key is not defined then return the specified default value. 
	 * Use the canonical scope lookup order for finding the preference value. 
	 * <p>
	 * The semantics of this method are to calculate the appropriate 
	 * <code>Preference</code> nodes in the preference hierarchy to use
	 * and then call the <code>get(String, String, Preferences[])</code> 
	 * method. The order of the nodes is calculated by consulting the default 
	 * scope lookup order as set by <code>setDefaultLookupOrder(String, String)</code>.
	 * </p><p>
	 * Callers may specify an array of scope context objects to aid in the 
	 * determination of the correct nodes. For each entry in the lookup 
	 * order, the array of contexts is consulted and if one matching the 
	 * scope exists, then it is used to calculate the node. Otherwise a
	 * default calculation algorithm is used. 
	 * </p><p>
	 * An example of a qualifier for an Eclipse 2.1 preference is the
	 * plug-in identifier. (e.g. "org.eclipse.core.resources" for "description.autobuild")
	 * </p>
	 * @param qualifier a namespace qualifier for the preference
	 * @param key the name of the preference
	 * @param defaultValue the value to use if the preference is not defined
	 * @param contexts optional context objects to help scopes determine which nodes to search, or <code>null</code>
	 * @return the value of the preference or the given default value
	 * @see IScopeContext
	 * @see #get(java.lang.String, java.lang.String, org.osgi.service.prefs.Preferences[])
	 * @see #getLookupOrder(java.lang.String, java.lang.String)
	 * @see #getDefaultLookupOrder(java.lang.String, java.lang.String)
	 */
	public double getDouble(String qualifier, String key, double defaultValue, IScopeContext[] contexts);

	/**
	 * Return the value stored in the preference store for the given key. 
	 * If the key is not defined then return the specified default value. 
	 * Use the canonical scope lookup order for finding the preference value. 
	 * <p>
	 * The semantics of this method are to calculate the appropriate 
	 * <code>Preference</code> nodes in the preference hierarchy to use
	 * and then call the <code>get(String, String, Preferences[])</code> 
	 * method. The order of the nodes is calculated by consulting the default 
	 * scope lookup order as set by <code>setDefaultLookupOrder(String, String)</code>.
	 * </p><p>
	 * Callers may specify an array of scope context objects to aid in the 
	 * determination of the correct nodes. For each entry in the lookup 
	 * order, the array of contexts is consulted and if one matching the 
	 * scope exists, then it is used to calculate the node. Otherwise a
	 * default calculation algorithm is used. 
	 * </p><p>
	 * An example of a qualifier for an Eclipse 2.1 preference is the
	 * plug-in identifier. (e.g. "org.eclipse.core.resources" for "description.autobuild")
	 * </p>
	 * @param qualifier a namespace qualifier for the preference
	 * @param key the name of the preference
	 * @param defaultValue the value to use if the preference is not defined
	 * @param contexts optional context objects to help scopes determine which nodes to search, or <code>null</code>
	 * @return the value of the preference or the given default value
	 * @see IScopeContext
	 * @see #get(java.lang.String, java.lang.String, org.osgi.service.prefs.Preferences[])
	 * @see #getLookupOrder(java.lang.String, java.lang.String)
	 * @see #getDefaultLookupOrder(java.lang.String, java.lang.String)
	 */
	public float getFloat(String qualifier, String key, float defaultValue, IScopeContext[] contexts);

	/**
	 * Return the value stored in the preference store for the given key. 
	 * If the key is not defined then return the specified default value. 
	 * Use the canonical scope lookup order for finding the preference value. 
	 * <p>
	 * The semantics of this method are to calculate the appropriate 
	 * <code>Preference</code> nodes in the preference hierarchy to use
	 * and then call the <code>get(String, String, Preferences[])</code> 
	 * method. The order of the nodes is calculated by consulting the default 
	 * scope lookup order as set by <code>setDefaultLookupOrder(String, String)</code>.
	 * </p><p>
	 * Callers may specify an array of scope context objects to aid in the 
	 * determination of the correct nodes. For each entry in the lookup 
	 * order, the array of contexts is consulted and if one matching the 
	 * scope exists, then it is used to calculate the node. Otherwise a
	 * default calculation algorithm is used. 
	 * </p><p>
	 * An example of a qualifier for an Eclipse 2.1 preference is the
	 * plug-in identifier. (e.g. "org.eclipse.core.resources" for "description.autobuild")
	 * </p>
	 * @param qualifier a namespace qualifier for the preference
	 * @param key the name of the preference
	 * @param defaultValue the value to use if the preference is not defined
	 * @param contexts optional context objects to help scopes determine which nodes to search, or <code>null</code>
	 * @return the value of the preference or the given default value
	 * @see IScopeContext
	 * @see #get(java.lang.String, java.lang.String, org.osgi.service.prefs.Preferences[])
	 * @see #getLookupOrder(java.lang.String, java.lang.String)
	 * @see #getDefaultLookupOrder(java.lang.String, java.lang.String)
	 */
	public int getInt(String qualifier, String key, int defaultValue, IScopeContext[] contexts);

	/**
	 * Return the value stored in the preference store for the given key. 
	 * If the key is not defined then return the specified default value. 
	 * Use the canonical scope lookup order for finding the preference value. 
	 * <p>
	 * The semantics of this method are to calculate the appropriate 
	 * <code>Preference</code> nodes in the preference hierarchy to use
	 * and then call the <code>get(String, String, Preferences[])</code> 
	 * method. The order of the nodes is calculated by consulting the default 
	 * scope lookup order as set by <code>setDefaultLookupOrder(String, String)</code>.
	 * </p><p>
	 * Callers may specify an array of scope context objects to aid in the 
	 * determination of the correct nodes. For each entry in the lookup 
	 * order, the array of contexts is consulted and if one matching the 
	 * scope exists, then it is used to calculate the node. Otherwise a
	 * default calculation algorithm is used. 
	 * </p><p>
	 * An example of a qualifier for an Eclipse 2.1 preference is the
	 * plug-in identifier. (e.g. "org.eclipse.core.resources" for "description.autobuild")
	 * </p>
	 * @param qualifier a namespace qualifier for the preference
	 * @param key the name of the preference
	 * @param defaultValue the value to use if the preference is not defined
	 * @param contexts optional context objects to help scopes determine which nodes to search, or <code>null</code>
	 * @return the value of the preference or the given default value
	 * @see IScopeContext
	 * @see #get(java.lang.String, java.lang.String, org.osgi.service.prefs.Preferences[])
	 * @see #getLookupOrder(java.lang.String, java.lang.String)
	 * @see #getDefaultLookupOrder(java.lang.String, java.lang.String)
	 */
	public long getLong(String qualifier, String key, long defaultValue, IScopeContext[] contexts);

	/**
	 * Return the value stored in the preference store for the given key. 
	 * If the key is not defined then return the specified default value. 
	 * Use the canonical scope lookup order for finding the preference value. 
	 * <p>
	 * The semantics of this method are to calculate the appropriate 
	 * <code>Preference</code> nodes in the preference hierarchy to use
	 * and then call the <code>get(String, String, Preferences[])</code> 
	 * method. The order of the nodes is calculated by consulting the default 
	 * scope lookup order as set by <code>setDefaultLookupOrder(String, String)</code>.
	 * </p><p>
	 * Callers may specify an array of scope context objects to aid in the 
	 * determination of the correct nodes. For each entry in the lookup 
	 * order, the array of contexts is consulted and if one matching the 
	 * scope exists, then it is used to calculate the node. Otherwise a
	 * default calculation algorithm is used. 
	 * </p><p>
	 * An example of a qualifier for an Eclipse 2.1 preference is the
	 * plug-in identifier. (e.g. "org.eclipse.core.resources" for "description.autobuild")
	 * </p>
	 * @param qualifier a namespace qualifier for the preference
	 * @param key the name of the preference
	 * @param defaultValue the value to use if the preference is not defined
	 * @param contexts optional context objects to help scopes determine which nodes to search, or <code>null</code>
	 * @return the value of the preference or the given default value
	 * @see IScopeContext
	 * @see #get(java.lang.String, java.lang.String, org.osgi.service.prefs.Preferences[])
	 * @see #getLookupOrder(java.lang.String, java.lang.String)
	 * @see #getDefaultLookupOrder(java.lang.String, java.lang.String)
	 */
	public String getString(String qualifier, String key, String defaultValue, IScopeContext[] contexts);

	/**
	 * Return the root node of the Eclipse preference hierarchy.
	 * 
	 * @return the root of the hierarchy
	 */
	public IEclipsePreferences getRootNode();

	/**
	 * Exports all preferences for the given preference node and all its children to the specified
	 * output stream. It is the responsibility of the client to close the given output stream.
	 * <p>
	 * If the given node is the root node, then do nothing.
	 * </p><p>
	 * The values stored in the resulting stream are suitable for later being read by the
	 * by <code>importPreferences</code> method.
	 * </p>
	 * @param node the node to treat as the root of the export
	 * @param output the stream to write to
	 * @return a status object describing success or detailing failure reasons
	 * @exception CoreException if there was a problem exporting the preferences
	 * @see #importPreferences(java.io.OutputStream)
	 */
	public IStatus exportPreferences(IEclipsePreferences node, OutputStream output) throws CoreException;

	/**
	 * Loads preferences from the given file and stores them in the preferences store.
	 * Existing values are over-ridden by those from the stream. The stream is closed
	 * upon return from this method.
	 * <p>
	 * This file must have been written by the <code>exportPreferences</code> 
	 * method.
	 * </p>
	 * @param input the stream to load the preferences from
	 * @return a status object describing success or detailing failure reasons
	 * @exception CoreException if there are problems importing the preferences
	 * @see exportPreferences(org.eclipse.core.runtime.preferences.IEclipsePreferences, java.io.OutputStream)
	 */
	public IStatus importPreferences(InputStream input) throws CoreException;

	/**
	 * Return an array with the default lookup order for the preference keyed by the given
	 * qualifier and simple name. Return <code>null</code> if no default has been set.
	 * 
	 * @param qualifier the namespace qualifier for the preference
	 * @param key the preference name
	 * @return the scope order or <code>null</code>
	 * @see #setDefaultLookupOrder(String, String, String[])
	 */
	public String[] getDefaultLookupOrder(String qualifier, String key);

	/**
	 * Return an array with the lookup order for the preference keyed by the given
	 * qualifier and simple name. Return the default-default order as defined by the
	 * platform if no order has been set.
	 * 
	 * @param qualifier the namespace qualifier for the preference
	 * @param key the preference name
	 * @return the scope order 
	 * @see #getDefaultLookupOrder(String, String)
	 * @see #setDefaultLookupOrder(String, String, String[])
	 */
	public String[] getLookupOrder(String qualifier, String key);

	/**
	 * Set the default scope lookup order for the preference keyed by the given
	 * qualifier and simple name. If the order is <code>null</code> then the default
	 * ordering (if it exists) is removed.
	 * 
	 * @param qualifier the namespace qualifier for the preference
	 * @param key the preference name
	 * @param order the lookup order or <code>null</code>
	 * @see #getDefaultLookupOrder(String, String)
	 */
	public void setDefaultLookupOrder(String qualifier, String key, String[] order);

}
