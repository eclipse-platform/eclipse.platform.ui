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
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @since 3.0
 */
public class PreferencesService implements IPreferencesService, IRegistryChangeListener {

	// cheat here and add "project" even though we really shouldn't know about it
	// because of plug-in dependancies and it being defined in the resources plug-in
	private static final String[] DEFAULT_DEFAULT_LOOKUP_ORDER = new String[] {"project", //$NON-NLS-1$ 
			InstanceScope.SCOPE, //
			ConfigurationScope.SCOPE, //
			DefaultScope.SCOPE};
	private static final char EXPORT_ROOT_PREFIX = '!';
	private static final char BUNDLE_VERSION_PREFIX = '@';
	private static final float EXPORT_VERSION = 3;
	private static final String VERSION_KEY = "file_export_version"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	private static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
	private static final String ELEMENT_SCOPE = "scope"; //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private static IPreferencesService instance;
	static final RootPreferences root = new RootPreferences();
	private static final Map defaultsRegistry = Collections.synchronizedMap(new HashMap());
	private static final Map scopeRegistry = Collections.synchronizedMap(new HashMap());

	/*
	 * Create and return an IStatus object with ERROR severity and the
	 * given message and exception.
	 */
	private static IStatus createStatusError(String message, Exception e) {
		return new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, e);
	}

	/*
	 * Create and return an IStatus object with WARNING severity and the
	 * given message and exception.
	 */
	private static IStatus createStatusWarning(String message, Exception e) {
		return new Status(IStatus.WARNING, Platform.PI_RUNTIME, IStatus.WARNING, message, e);
	}

	/*
	 * Return the instance.
	 */
	public static IPreferencesService getDefault() {
		if (instance == null)
			instance = new PreferencesService();
		return instance;
	}

	/**
	 * See who is plugged into the extension point.
	 */
	private void initializeScopes() {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(Platform.PI_RUNTIME, Platform.PT_PREFERENCES);
		if (point == null)
			return;
		IExtension[] extensions = point.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++)
				if (ELEMENT_SCOPE.equalsIgnoreCase(elements[j].getName()))
					scopeAdded(elements[j]);
		}
		Platform.getExtensionRegistry().addRegistryChangeListener(this, Platform.PI_RUNTIME);
	}

	static void log(IStatus status) {
		InternalPlatform.getDefault().log(status);
	}

	/*
	 * Abstracted into a separate method to prepare for dynamic awareness.
	 */
	static void scopeAdded(IConfigurationElement element) {
		String key = element.getAttribute(ATTRIBUTE_NAME);
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
	static void scopeRemoved(String key) {
		IEclipsePreferences node = (IEclipsePreferences) root.node(key);
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

		final MultiStatus result = new MultiStatus(Platform.PI_RUNTIME, IStatus.OK, Policy.bind("preferences.applyProblems"), null); //$NON-NLS-1$

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
			String message = Policy.bind("preferences.applyProblems"); //$NON-NLS-1$
			throw new CoreException(createStatusError(message, e));
		}

		// save the prefs
		try {
			getRootNode().node(preferences.absolutePath()).flush();
		} catch (BackingStoreException e) {
			String message = Policy.bind("preferences.saveProblems"); //$NON-NLS-1$
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
		String prefix = IPath.SEPARATOR + Plugin.PLUGIN_PREFERENCE_SCOPE + IPath.SEPARATOR;
		for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String value = properties.getProperty(key);
			if (value != null) {
				int index = key.indexOf(IPath.SEPARATOR);
				if (index == -1) {
					result.put(BUNDLE_VERSION_PREFIX + key, value);
					result.put(EXPORT_ROOT_PREFIX + prefix + key, EMPTY_STRING);
				} else {
					String path = key.substring(0, index);
					key = key.substring(index + 1);
					result.put(EclipsePreferences.encodePath(prefix + path, key), value);
				}
			}
		}
		return result;
	}

	/*
	 * Convert the given properties file into a node hierarchy suitable for
	 * importing.
	 */
	private IExportedPreferences convertFromProperties(Properties properties) {
		IExportedPreferences result = ExportedRootPreferences.newRoot();
		for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
			String path = (String) i.next();
			String value = properties.getProperty(path);
			if (path.charAt(0) == EXPORT_ROOT_PREFIX) {
				ExportedPreferences current = (ExportedPreferences) result.node(path.substring(1));
				current.setExportRoot();
			} else if (path.charAt(0) == BUNDLE_VERSION_PREFIX) {
				ExportedPreferences current = (ExportedPreferences) result.node(InstanceScope.SCOPE).node(path.substring(1));
				current.setVersion(value);
			} else {
				String[] decoded = EclipsePreferences.decodePath(path);
				path = decoded[0] == null ? EMPTY_STRING : decoded[0];
				ExportedPreferences current = (ExportedPreferences) result.node(path);
				String key = decoded[1];
				current.put(key, value);
			}
		}
		if (InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Converted preferences file to IExportedPreferences tree: " + ((ExportedPreferences) result).toDeepDebugString()); //$NON-NLS-1$
		return result;
	}

	/*
	 * Return the string which is the scope for the given path.
	 * Return the empty string if it cannot be determined.
	 */
	String getScope(String path) {
		if (path == null || path.length() == 0)
			return EMPTY_STRING;
		int startIndex = path.indexOf(IPath.SEPARATOR);
		if (startIndex == -1)
			return path;
		if (path.length() == 1)
			return EMPTY_STRING;
		int endIndex = path.indexOf(IPath.SEPARATOR, startIndex + 1);
		if (endIndex == -1)
			endIndex = path.length();
		return path.substring(startIndex + 1, endIndex);
	}

	/*
	 * excludesList is guarenteed not to be null
	 */
	private Properties convertToProperties(IEclipsePreferences preferences, final String[] excludesList) throws BackingStoreException {
		final Properties result = new Properties();
		final int baseLength = preferences.absolutePath().length();

		// create a visitor to do the export
		IPreferenceNodeVisitor visitor = new IPreferenceNodeVisitor() {
			public boolean visit(IEclipsePreferences node) throws BackingStoreException {
				// don't store defaults
				String absolutePath = node.absolutePath();
				String scope = getScope(absolutePath);
				if (DefaultScope.SCOPE.equals(scope))
					return false;
				String path = absolutePath.length() <= baseLength ? EMPTY_STRING : EclipsePreferences.makeRelative(absolutePath.substring(baseLength));
				// check the excludes list to see if this node should be considered
				for (int i = 0; i < excludesList.length; i++) {
					String exclusion = EclipsePreferences.makeRelative(excludesList[i]);
					if (path.startsWith(exclusion))
						return false;
				}
				boolean needToAddVersion = InstanceScope.SCOPE.equals(scope);
				// check the excludes list for each preference
				String[] keys = node.keys();
				for (int i = 0; i < keys.length; i++) {
					String key = keys[i];
					boolean ignore = false;
					for (int j = 0; !ignore && j < excludesList.length; j++)
						if (EclipsePreferences.encodePath(path, key).startsWith(EclipsePreferences.makeRelative(excludesList[j])))
							ignore = true;
					if (!ignore) {
						String value = node.get(key, null);
						if (value != null) {
							if (needToAddVersion) {
								String bundle = getBundleName(absolutePath);
								if (bundle != null) {
									String version = getBundleVersion(bundle);
									if (version != null)
										result.put(BUNDLE_VERSION_PREFIX + bundle, version);
								}
								needToAddVersion = false;
							}
							result.put(EclipsePreferences.encodePath(absolutePath, key), value);
						}
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
				scope = (IScope) ((IConfigurationElement) value).createExecutableExtension(ATTRIBUTE_CLASS);
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
			if (properties.isEmpty())
				return Status.OK_STATUS;
			properties.put(VERSION_KEY, Float.toString(EXPORT_VERSION));
			properties.put(EXPORT_ROOT_PREFIX + node.absolutePath(), EMPTY_STRING);
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
		String result = get(EclipsePreferences.decodePath(key)[1], null, getNodes(qualifier, key, scopes));
		return result == null ? defaultValue : Boolean.valueOf(result).booleanValue();
	}

	/*
	 * Return the version for the bundle with the given name. Return null if it
	 * is not known or there is a problem.
	 */
	String getBundleVersion(String bundleName) {
		Bundle bundle = Platform.getBundle(bundleName);
		if (bundle != null) {
			Object version = bundle.getHeaders(EMPTY_STRING).get(Constants.BUNDLE_VERSION);
			if (version != null && version instanceof String)
				return (String) version;
		}
		return null;
	}

	/*
	 * Return the name of the bundle from the given path.
	 * It is assumed that that path is:
	 * - absolute
	 * - in the instance scope
	 */
	String getBundleName(String path) {
		if (path.length() == 0 || path.charAt(0) != IPath.SEPARATOR)
			return null;
		int first = path.indexOf(IPath.SEPARATOR, 1);
		if (first == -1)
			return null;
		int second = path.indexOf(IPath.SEPARATOR, first + 1);
		return second == -1 ? path.substring(first + 1) : path.substring(first + 1, second);
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IPreferencesService#getByteArray(java.lang.String, java.lang.String, byte[], org.eclipse.core.runtime.preferences.IScope[])
	 */
	public byte[] getByteArray(String qualifier, String key, byte[] defaultValue, IScopeContext[] scopes) {
		String result = get(EclipsePreferences.decodePath(key)[1], null, getNodes(qualifier, key, scopes));
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
		String value = get(EclipsePreferences.decodePath(key)[1], null, getNodes(qualifier, key, scopes));
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
		String value = get(EclipsePreferences.decodePath(key)[1], null, getNodes(qualifier, key, scopes));
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
		String value = get(EclipsePreferences.decodePath(key)[1], null, getNodes(qualifier, key, scopes));
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
		String value = get(EclipsePreferences.decodePath(key)[1], null, getNodes(qualifier, key, scopes));
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
		String childPath = EclipsePreferences.makeRelative(EclipsePreferences.decodePath(key)[0]);
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
						if (childPath != null)
							node = node.node(childPath);
						result.add(node);
					}
				}
			}
			if (!found) {
				Preferences node = getRootNode().node(scopeString).node(qualifier);
				if (childPath != null)
					node = node.node(childPath);
				result.add(node);
			}
			found = false;
		}
		return (Preferences[]) result.toArray(new Preferences[result.size()]);
	}

	/*
	 * Convert the given qualifier and key into a key to use in the look-up registry.
	 */
	private String getRegistryKey(String qualifier, String key) {
		if (qualifier == null)
			throw new IllegalArgumentException();
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
		return get(EclipsePreferences.decodePath(key)[1], defaultValue, getNodes(qualifier, key, scopes));
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

	public void registryChanged(IRegistryChangeEvent event) {
		IExtensionDelta[] deltas = event.getExtensionDeltas(Platform.PI_RUNTIME, Platform.PT_PREFERENCES);
		for (int i = 0; i < deltas.length; i++) {
			IConfigurationElement[] elements = deltas[i].getExtension().getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				switch (deltas[i].getKind()) {
					case IExtensionDelta.ADDED :
						scopeAdded(elements[j]);
						break;
					case IExtensionDelta.REMOVED :
						String scope = elements[j].getAttribute(ATTRIBUTE_NAME);
						if (scope != null)
							scopeRemoved(scope);
						break;
				}
			}
		}
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

	public IStatus validateVersions(IPath path) {
		String message = Policy.bind("preferences.validate"); //$NON-NLS-1$
		final MultiStatus result = new MultiStatus(Platform.PI_RUNTIME, IStatus.INFO, message, null);
		IPreferenceNodeVisitor visitor = new IPreferenceNodeVisitor() {
			public boolean visit(IEclipsePreferences node) {
				if (!(node instanceof ExportedPreferences))
					return false;

				// calculate the version in the file
				ExportedPreferences realNode = (ExportedPreferences) node;
				String version = realNode.getVersion();
				if (version == null || !PluginVersionIdentifier.validateVersion(version).isOK())
					return true;
				PluginVersionIdentifier versionInFile = new PluginVersionIdentifier(version);

				// calculate the version of the installed bundle
				String bundleName = getBundleName(node.absolutePath());
				if (bundleName == null)
					return true;
				String stringVersion = getBundleVersion(bundleName);
				if (stringVersion == null || !PluginVersionIdentifier.validateVersion(stringVersion).isOK())
					return true;
				PluginVersionIdentifier versionInMemory = new PluginVersionIdentifier(stringVersion);

				// verify the versions based on the matching rules
				IStatus verification = validatePluginVersions(bundleName, versionInFile, versionInMemory);
				if (verification != null)
					result.add(verification);

				return true;
			}
		};

		InputStream input = null;
		try {
			input = new BufferedInputStream(new FileInputStream(path.toFile()));
			IExportedPreferences prefs = readPreferences(input);
			prefs.accept(visitor);
		} catch (FileNotFoundException e) {
			// ignore...if the file does not exist then all is OK
		} catch (CoreException e) {
			message = Policy.bind("preferences.validationException"); //$NON-NLS-1$
			result.add(createStatusError(message, e));
		} catch (BackingStoreException e) {
			message = Policy.bind("preferences.validationException"); //$NON-NLS-1$
			result.add(createStatusError(message, e));
		}
		return result;
	}

	/**
	 * Compares two plugin version identifiers to see if their preferences
	 * are compatible.  If they are not compatible, a warning message is 
	 * added to the given multistatus, according to the following rules:
	 * 
	 * - plugins that differ in service number: no status
	 * - plugins that differ in minor version: WARNING status
	 * - plugins that differ in major version:
	 * 	- where installed plugin is newer: WARNING status
	 * 	- where installed plugin is older: ERROR status
	 * @param bundle the name of the bundle
	 * @param pref The version identifer of the preferences to be loaded
	 * @param installed The version identifier of the installed plugin
	 */
	IStatus validatePluginVersions(String bundle, PluginVersionIdentifier pref, PluginVersionIdentifier installed) {
		if (installed.getMajorComponent() == pref.getMajorComponent() && installed.getMinorComponent() == pref.getMinorComponent())
			return null;
		int severity;
		if (installed.getMajorComponent() < pref.getMajorComponent())
			severity = IStatus.ERROR;
		else
			severity = IStatus.WARNING;
		String msg = Policy.bind("preferences.incompatible", new String[] {pref.toString(), bundle, installed.toString()}); //$NON-NLS-1$
		return new Status(severity, Platform.PI_RUNTIME, 1, msg, null);
	}
}