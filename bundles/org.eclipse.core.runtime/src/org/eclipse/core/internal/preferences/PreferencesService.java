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
package org.eclipse.core.internal.preferences;

import java.io.*;
import java.util.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.*;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @since 3.0
 */
public class PreferencesService implements IPreferencesService {

	public static final String PT_PREFERENCES = "preferences"; //$NON-NLS-1$

	// cheat here and add "project" even though we really shouldn't know about it
	// because of plug-in dependancies and it being defined in the resources plug-in
	private static final String[] DEFAULT_DEFAULT_LOOKUP_ORDER = new String[]{"project", //$NON-NLS-1$ 
			InstanceScope.SCOPE, //
			ConfigurationScope.SCOPE, //
			UserScope.SCOPE, //
			DefaultScope.SCOPE};
	private static IPreferencesService instance = new PreferencesService();
	private static Map defaultsRegistry = new HashMap();
	private static Map scopeRegistry = new HashMap();
	private static RootPreferences root = new RootPreferences();
	private static IStatus statusOK;
	private static final String VERSION_KEY = "file_export_version"; //$NON-NLS-1$
	private static final float EXPORT_VERSION = 3;

	static {
		initializeScopes();
	}

	public static IPreferencesService getDefault() {
		return instance;
	}

	/*
	 * Abstracted into a separate method to prepare for dynamic awareness.
	 */
	private static void scopeAdded(IConfigurationElement element) {
		String key = element.getAttribute("scope"); //$NON-NLS-1$
		if (key == null) {
			String message = Policy.bind("preferences.missingScopeAttribute", element.getDeclaringExtension().getUniqueIdentifier()); //$NON-NLS-1$
			log(createStatusWarning(message, null));
			return;
		}
		try {
			IScope scopeInstance = (IScope) element.createExecutableExtension("class"); //$NON-NLS-1$
			scopeRegistry.put(key, scopeInstance);
			IEclipsePreferences child = scopeInstance.create(root, key);
			root.addChild(child);
		} catch (ClassCastException e) {
			String message = Policy.bind("preferences.classCast"); //$NON-NLS-1$
			log(createStatusError(message, e));
		} catch (CoreException e) {
			log(e.getStatus());
		}
	}

	/*
	 * Abstracted into a separate method to prepare for dynamic awareness.
	 */
	private static void scopeRemoved(String key) throws BackingStoreException {
		IEclipsePreferences node = (IEclipsePreferences) root.node(key);
		node.removeNode();
		root.removeNode(node);
		scopeRegistry.remove(key);
	}

	/**
	 * See who is plugged into the extension point.
	 */
	private static void initializeScopes() {
		// TODO: make the cache dynamic aware
		IExtensionPoint point = Platform.getPluginRegistry().getExtensionPoint(Platform.PI_RUNTIME, PT_PREFERENCES);
		if (point == null)
			return;
		IExtension[] extensions = point.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++)
				if ("preferences".equalsIgnoreCase(elements[j].getName())) //$NON-NLS-1$
					scopeAdded(elements[j]);
		}
	}

	private static void log(IStatus status) {
		InternalPlatform.getDefault().log(status);
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#get(java.lang.String, java.lang.String, org.osgi.service.prefs.Preferences[])
	 */
	public String get(String key, String defaultValue, Preferences[] nodes) {
		if (nodes == null)
			return defaultValue;
		for (int i = 0; i < nodes.length; i++) {
			Preferences node = nodes[i];
			if (node != null) {
				String result = node.get(key, null);
				if (result != null)
					return result;
			}
		}
		return defaultValue;
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#getBoolean(java.lang.String, java.lang.String, boolean, org.eclipse.core.runtime.preferences.IScope[])
	 */
	public boolean getBoolean(String qualifier, String key, boolean defaultValue, IScopeContext[] scopes) {
		String result = get(key, null, getNodes(qualifier, key, scopes));
		return result == null ? defaultValue : Boolean.getBoolean(result);
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#getByteArray(java.lang.String, java.lang.String, byte[], org.eclipse.core.runtime.preferences.IScope[])
	 */
	public byte[] getByteArray(String qualifier, String key, byte[] defaultValue, IScopeContext[] scopes) {
		String result = get(key, null, getNodes(qualifier, key, scopes));
		return result == null ? defaultValue : result.getBytes();
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#getDefaultLookupOrder(java.lang.String, java.lang.String)
	 */
	public String[] getDefaultLookupOrder(String qualifier, String key) {
		LookupOrder order = (LookupOrder) defaultsRegistry.get(getRegistryKey(qualifier, key));
		return order == null ? null : order.getOrder();
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#setDefaultLookupOrder(java.lang.String, java.lang.String, java.lang.String[])
	 */
	public void setDefaultLookupOrder(String qualifier, String key, String[] order) {
		String registryKey = getRegistryKey(qualifier, key);
		if (order == null)
			defaultsRegistry.remove(registryKey);
		else {
			LookupOrder obj = new LookupOrder(qualifier, key, order);
			defaultsRegistry.put(registryKey, obj);
		}
	}

	/*
	 * Convert the given qualifier and key into a key to use in the look-up registry.
	 */
	private String getRegistryKey(String qualifier, String key) {
		if (qualifier == null)
			return key;
		if (key == null)
			return qualifier;
		return qualifier + '/' + key;
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#getDouble(java.lang.String, java.lang.String, double, org.eclipse.core.runtime.preferences.IScope[])
	 */
	public double getDouble(String qualifier, String key, double defaultValue, IScopeContext[] scopes) {
		String value = get(key, null, getNodes(qualifier, key, scopes));
		if (value == null)
			return defaultValue;
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#getFloat(java.lang.String, java.lang.String, float, org.eclipse.core.runtime.preferences.IScope[])
	 */
	public float getFloat(String qualifier, String key, float defaultValue, IScopeContext[] scopes) {
		String value = get(key, null, getNodes(qualifier, key, scopes));
		if (value == null)
			return defaultValue;
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#getInt(java.lang.String, java.lang.String, int, org.eclipse.core.runtime.preferences.IScope[])
	 */
	public int getInt(String qualifier, String key, int defaultValue, IScopeContext[] scopes) {
		String value = get(key, null, getNodes(qualifier, key, scopes));
		if (value == null)
			return defaultValue;
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#getLong(java.lang.String, java.lang.String, long, org.eclipse.core.runtime.preferences.IScope[])
	 */
	public long getLong(String qualifier, String key, long defaultValue, IScopeContext[] scopes) {
		String value = get(key, null, getNodes(qualifier, key, scopes));
		if (value == null)
			return defaultValue;
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#getLookupOrder(java.lang.String, java.lang.String)
	 */
	public String[] getLookupOrder(String qualifier, String key) {
		String[] order = getDefaultLookupOrder(qualifier, key);
		if (order == null)
			order = DEFAULT_DEFAULT_LOOKUP_ORDER;
		return order;
	}

	private Preferences[] getNodes(String qualifier, String key, IScopeContext[] contexts) {
		String[] order = getLookupOrder(qualifier, key);
		ArrayList result = new ArrayList();
		for (int i = 0; i < order.length; i++) {
			String scopeString = order[i];
			boolean found = false;
			for (int j = 0; contexts != null && j < contexts.length; j++) {
				IScopeContext context = contexts[j];
				if (context != null && context.getName().equals(scopeString)) {
					Preferences node = context.getNode(qualifier);
					if (node != null) {
						found = true;
						result.add(node);
					}
				}
			}
			if (!found)
				result.add(getRootNode().node(scopeString).node(qualifier));
			found = false;
		}
		return (Preferences[]) result.toArray(new Preferences[result.size()]);
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#getRootNode()
	 */

	public IEclipsePreferences getRootNode() {
		return root;
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#getString(java.lang.String, java.lang.String, java.lang.String, org.eclipse.core.runtime.preferences.IScope[])
	 */
	public String getString(String qualifier, String key, String defaultValue, IScopeContext[] scopes) {
		return get(key, defaultValue, getNodes(qualifier, key, scopes));
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#importPreferences(java.io.InputStream)
	 */
	public IStatus importPreferences(InputStream input) throws CoreException {
		Properties properties = new Properties();
		try {
			properties.load(input);
		} catch (IOException e) {
			String message = Policy.bind("preferences.importProblems"); //$NON-NLS-1$
			throw new CoreException(createStatusError(message, e));
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				// ignore
			}
		}

		Preferences localRoot = root;

		// import legacy properties file
		if (isLegacy(properties)) {
			localRoot = root.node(Plugin.PLUGIN_PREFERENCE_SCOPE);
			// TODO version verification
			// strip out version ids
			properties = removeVersions(properties);
			for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
				String key = (String) i.next();
				if (new Path(key).segmentCount() < 2)
					properties.remove(key);
			}
		}

		for (Enumeration e = properties.keys(); e.hasMoreElements();) {
			String fullPath = (String) e.nextElement();
			String value = properties.getProperty(fullPath);
			IPath path = new Path(fullPath);
			String key = path.lastSegment();
			path = path.removeLastSegments(1);
			Preferences node = localRoot.node(path.toString());
			node.put(key, value);
		}
		return createStatusOK();
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#exportPreferences(org.eclipse.core.runtime.preferences.IEclipsePreferences, java.io.OutputStream)
	 */
	public IStatus exportPreferences(IEclipsePreferences node, OutputStream output) throws CoreException {
		Properties properties = null;
		try {
			properties = convertToProperties(node);
			properties.put(VERSION_KEY, Float.toString(EXPORT_VERSION));
		} catch (BackingStoreException e) {
			throw new CoreException(createStatusError(e.getMessage(), e));
		}
		try {
			properties.store(output, null);
		} catch (IOException e) {
			String message = Policy.bind("preferences.exportProblems"); //$NON-NLS-1$
			throw new CoreException(createStatusError(message, e));
		}
		return createStatusOK();
	}

	private static IStatus createStatusError(String message, Exception e) {
		return new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, e);
	}

	private static IStatus createStatusWarning(String message, Exception e) {
		return new Status(IStatus.WARNING, Platform.PI_RUNTIME, IStatus.WARNING, message, e);
	}

	private static IStatus createStatusOK() {
		if (statusOK == null)
			statusOK = new Status(IStatus.OK, Platform.PI_RUNTIME, IStatus.OK, "OK", null); //$NON-NLS-1$
		return statusOK;
	}

	/* 
	 * Helper method to do the recursion
	 */
	private static void recursiveAdd(Properties properties, Preferences settings, IPath prefix) throws BackingStoreException {
		// add the key/value pairs from this node
		String[] keys = settings.keys();
		for (int i = 0; i < keys.length; i++) {
			String value = settings.get(keys[i], null);
			if (value != null)
				properties.put(prefix.append(keys[i]).toString(), value);
		}

		// recursively add the child information
		String[] children = settings.childrenNames();
		for (int i = 0; i < children.length; i++) {
			IPath key = prefix.append(children[i]);
			Preferences child = settings.node(children[i]);
			recursiveAdd(properties, child, key);
		}
	}

	private Properties convertToProperties(Preferences node) throws BackingStoreException {
		Properties result = new Properties();
		recursiveAdd(result, node, new Path(node.absolutePath()));
		return result;
	}

	/**
	 * Returns a boolean value indicating whether or not the given Properties
	 * object is the result of a preference export previous to Eclipse 3.0.
	 * <p>
	 * Check the contents of the file. In Eclipse 3.0 we printed out a file
	 * version key.
	 */
	private boolean isLegacy(Properties properties) {
		return properties.getProperty(VERSION_KEY) == null;
	}

	private Properties removeVersions(Properties properties) {
		Properties result = new Properties();
		for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String value = properties.getProperty(key);
			if (value != null) {
				IPath path = new Path(key);
				if (path.segmentCount() != 1)
					result.put(key, value);
			}
		}
		return result;
	}

	/**
	 * Return the IScope from the registry which defines the scope 
	 * of the given preferences object. Return <code>null</code> if
	 * there is no scope defined or if it cannot be determined.
	 */
	public IScope getScope(Preferences node) {
		IPath path = new Path(node.absolutePath());
		if (path.segmentCount() < 1)
			return null;
		String key = path.segment(0);
		return (IScope) scopeRegistry.get(key);
	}
}
