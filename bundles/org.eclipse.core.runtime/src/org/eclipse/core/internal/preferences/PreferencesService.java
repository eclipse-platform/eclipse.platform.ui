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

	// cheat here and add "project" even though we really shouldn't know about it
	// because of plug-in dependancies and it being defined in the resources plug-in
	private static final String[] DEFAULT_DEFAULT_LOOKUP_ORDER = new String[]{"project", //$NON-NLS-1$ 
			InstanceScope.SCOPE, //
			ConfigurationScope.SCOPE, //
			DefaultScope.SCOPE};
	private static final char EXPORT_ROOT_PREFIX = '!';
	private static final float EXPORT_VERSION = 3;
	private static final String VERSION_KEY = "file_export_version"; //$NON-NLS-1$

	private static IPreferencesService instance = null;
	private static final RootPreferences root = new RootPreferences();
	private static final Map defaultsRegistry = Collections.synchronizedMap(new HashMap());
	private static final Map scopeRegistry = Collections.synchronizedMap(new HashMap());

	private static IStatus createStatusError(String message, Exception e) {
		return new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, e);
	}

	private static IStatus createStatusWarning(String message, Exception e) {
		return new Status(IStatus.WARNING, Platform.PI_RUNTIME, IStatus.WARNING, message, e);
	}

	public static IPreferencesService getDefault() {
		if (instance == null)
			instance = new PreferencesService();
		return instance;
	}

	/**
	 * See who is plugged into the extension point.
	 */
	private static void initializeScopes() {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(Platform.PI_RUNTIME, Platform.PT_PREFERENCES);
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
	 * Abstracted into a separate method to prepare for dynamic awareness.
	 */
	private static void scopeAdded(IConfigurationElement element) {
		String key = element.getAttribute("scope"); //$NON-NLS-1$
		if (key == null) {
			String message = Policy.bind("preferences.missingScopeAttribute", element.getDeclaringExtension().getUniqueIdentifier()); //$NON-NLS-1$
			log(createStatusWarning(message, null));
			return;
		}
		scopeRegistry.put(key, element);
		root.addChild(key, null);
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

	private PreferencesService() {
		super();
		initializeScopes();
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#applyPreferences(org.eclipse.core.runtime.preferences.IExportedPreferences)
	 */
	public IStatus applyPreferences(IExportedPreferences preferences) throws CoreException {
		if (preferences == null)
			throw new IllegalArgumentException();

		if (InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Applying exported preferences: " + ((ExportedPreferences) preferences).toDeepDebugString()); //$NON-NLS-1$

		final MultiStatus result = new MultiStatus(Platform.PI_RUNTIME, IStatus.OK, "Problems applying preference changes.", null);

		// create a visitor to apply the given set of preferences
		IPreferenceNodeVisitor visitor = new IPreferenceNodeVisitor() {
			public boolean visit(IEclipsePreferences node) throws BackingStoreException {
				IEclipsePreferences globalNode;
				if (node.parent() == null)
					globalNode = root;
				else
					globalNode = (IEclipsePreferences) root.node(node.absolutePath());
				ExportedPreferences epNode = (ExportedPreferences) node;

				// if this node is an export root then we need to remove 
				// it from the global preferences before continuing.
				boolean removed = false;
				if (epNode.isExportRoot()) {
					if (InternalPlatform.DEBUG_PREFERENCES)
						Policy.debug("Found export root: " + epNode.absolutePath()); //$NON-NLS-1$
					// TODO should only have to do this if any of my children have properties to set
					globalNode.removeNode();
					removed = true;
				}

				// iterate over the preferences in this node and set them
				// in the global space.
				if (epNode.properties != null && !epNode.properties.isEmpty()) {
					// if this node was removed then we need to create a new one
					if (removed)
						globalNode = (IEclipsePreferences) root.node(node.absolutePath());
					for (Iterator i = epNode.properties.keySet().iterator(); i.hasNext();) {
						String key = (String) i.next();
						// intern strings we import because some people
						// in their property change listeners use identity
						// instead of equals. See bug 20193 and 20534.
						key = key.intern();
						String value = node.get(key, null);
						if (value != null) {
							if (InternalPlatform.DEBUG_PREFERENCES)
								Policy.debug("Setting: " + globalNode.absolutePath() + '/' + key + '=' + value); //$NON-NLS-1$
							globalNode.put(key, value);
						}
					}
				}

				// keep visiting children
				return true;
			}
		};

		try {
			// start by visiting the root
			preferences.accept(visitor);
		} catch (BackingStoreException e) {
			String message = "Problems applying preferences.";
			throw new CoreException(createStatusError(message, e));
		}

		// save the prefs
		try {
			getRootNode().node(preferences.absolutePath()).flush();
		} catch (BackingStoreException e) {
			String message = "Problems saving preferences.";
			throw new CoreException(createStatusError(message, e));
		}

		if (InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Current list of all settings: " + ((EclipsePreferences) getRootNode()).toDeepDebugString()); //$NON-NLS-1$

		return result;
	}

	/*
	 * Convert the given properties file from legacy format to 
	 * one which is Eclipse 3.0 compliant. 
	 * 
	 * Convert the plug-in version indicator entries to export roots.
	 */
	private Properties convertFromLegacy(Properties properties) {
		Properties result = new Properties();
		IPath prefix = new Path(Plugin.PLUGIN_PREFERENCE_SCOPE).makeAbsolute();
		for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String value = properties.getProperty(key);
			if (value != null) {
				IPath path = new Path(key);
				if (path.segmentCount() == 1)
					result.put(EXPORT_ROOT_PREFIX + prefix.append(key).toString(), ""); //$NON-NLS-1$
				else
					result.put(prefix.append(path).toString(), value);
			}
		}
		return result;
	}

	/*
	 * Convert the given properties file into a node hierarchy suitable for
	 * importing.
	 */
	private IExportedPreferences convertFromProperties(Properties properties) {
		IExportedPreferences result = new ExportedPreferences(null, ""); //$NON-NLS-1$
		for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
			String pathString = (String) i.next();
			if (pathString.charAt(0) == EXPORT_ROOT_PREFIX) {
				pathString = pathString.substring(1, pathString.length());
				ExportedPreferences current = (ExportedPreferences) result.node(pathString);
				current.setExportRoot();
			} else {
				IPath path = new Path(pathString);
				IExportedPreferences current = (IExportedPreferences) result.node(path.removeLastSegments(1));
				String key = path.lastSegment();
				String value = properties.getProperty(pathString);
				current.put(key, value);
			}
		}
		if (InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Converted preferences file to IExportedPreferences tree: " + ((ExportedPreferences) result).toDeepDebugString()); //$NON-NLS-1$
		return result;
	}

	/*
	 * excludesList is guarenteed not to be null
	 */
	private Properties convertToProperties(IEclipsePreferences preferences, final String[] excludesList) throws BackingStoreException {
		final Properties result = new Properties();

		// create a visitor to do the export
		IPreferenceNodeVisitor visitor = new IPreferenceNodeVisitor() {
			public boolean visit(IEclipsePreferences node) throws BackingStoreException {
				// don't store defaults
				String pathString = node.absolutePath();
				IPath path = new Path(pathString);
				if (path.segmentCount() > 0 && DefaultScope.SCOPE.equals(path.segment(0)))
					return false;
				// check the excludes list to see if this node should be considered
				for (int i = 0; i < excludesList.length; i++) {
					if (pathString.startsWith(excludesList[i]))
						return false;
				}
				// check the excludes list for each preference
				String[] keys = node.keys();
				for (int i = 0; i < keys.length; i++) {
					String key = keys[i];
					String fullKeyPath = path.append(key).toString();
					boolean ignore = false;
					for (int j = 0; !ignore && j < excludesList.length; j++)
						if (fullKeyPath.startsWith(excludesList[j]))
							ignore = true;
					if (!ignore) {
						String value = node.get(key, null);
						if (value != null)
							result.put(fullKeyPath, value);
					}
				}
				return true;
			}
		};

		// start by visiting the root that we were passed in
		preferences.accept(visitor);

		// return the properties object
		return result;
	}

	protected IEclipsePreferences createNode(String name) {
		IScope scope = null;
		Object value = scopeRegistry.get(name);
		if (value instanceof IConfigurationElement) {
			try {
				scope = (IScope) ((IConfigurationElement) value).createExecutableExtension("class"); //$NON-NLS-1$
				scopeRegistry.put(name, scope);
			} catch (ClassCastException e) {
				String message = Policy.bind("preferences.classCast"); //$NON-NLS-1$
				log(createStatusError(message, e));
				return new EclipsePreferences(root, name);
			} catch (CoreException e) {
				log(e.getStatus());
				return new EclipsePreferences(root, name);
			}
		} else
			scope = (IScope) value;
		return scope.create(root, name);
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#exportPreferences(org.eclipse.core.runtime.preferences.IEclipsePreferences, java.io.OutputStream, java.lang.String[])
	 */
	public IStatus exportPreferences(IEclipsePreferences node, OutputStream output, String[] excludesList) throws CoreException {
		if (node == null || output == null)
			throw new IllegalArgumentException();
		Properties properties = null;
		if (excludesList == null)
			excludesList = new String[0];
		try {
			properties = convertToProperties(node, excludesList);
			properties.put(VERSION_KEY, Float.toString(EXPORT_VERSION));
			properties.put(EXPORT_ROOT_PREFIX + node.absolutePath(), ""); //$NON-NLS-1$
		} catch (BackingStoreException e) {
			throw new CoreException(createStatusError(e.getMessage(), e));
		}
		try {
			properties.store(output, null);
		} catch (IOException e) {
			String message = Policy.bind("preferences.exportProblems"); //$NON-NLS-1$
			throw new CoreException(createStatusError(message, e));
		}
		return Status.OK_STATUS;
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
		// if there wasn't an exact match based on both qualifier and simple name
		// then do a lookup based only on the qualifier
		if (order == null && key != null)
			order = getDefaultLookupOrder(qualifier, null);
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
		if (InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Importing preferences..."); //$NON-NLS-1$
		return applyPreferences(readPreferences(input));
	}

	/*
	 * Returns a boolean value indicating whether or not the given Properties
	 * object is the result of a preference export previous to Eclipse 3.0.
	 * 
	 * Check the contents of the file. In Eclipse 3.0 we printed out a file
	 * version key.
	 */
	private boolean isLegacy(Properties properties) {
		return properties.getProperty(VERSION_KEY) == null;
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#readPreferences(java.io.InputStream)
	 */
	public IExportedPreferences readPreferences(InputStream input) throws CoreException {
		if (input == null)
			throw new IllegalArgumentException();

		if (InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Reading preferences from stream..."); //$NON-NLS-1$

		// read the file into a properties object
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

		// manipulate the file if it from a legacy preference export
		if (isLegacy(properties)) {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Read legacy preferences file, converting to 3.0 format..."); //$NON-NLS-1$
			properties = convertFromLegacy(properties);
		} else {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Read preferences file."); //$NON-NLS-1$
			properties.remove(VERSION_KEY);
		}

		// convert the Properties object into an object to return
		return convertFromProperties(properties);
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
}