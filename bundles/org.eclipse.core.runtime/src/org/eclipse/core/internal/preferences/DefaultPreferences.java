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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * @since 3.0
 */
public class DefaultPreferences extends EclipsePreferences {
	// cache which nodes have been loaded from disk
	private static Set loadedNodes = new HashSet();
	private static final String ELEMENT_INITIALIZER = "initializer"; //$NON-NLS-1$
	private static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
	private static boolean initialized = false;
	private static final String KEY_PREFIX = "%"; //$NON-NLS-1$
	private static final String KEY_DOUBLE_PREFIX = "%%"; //$NON-NLS-1$
	private static final IPath NL_DIR = new Path("$nl$"); //$NON-NLS-1$

	// declared in org.eclipse.ui.branding.IProductConstants
	public static final String PRODUCT_KEY = "preferenceCustomization"; //$NON-NLS-1$
	private static final String LEGACY_PRODUCT_CUSTOMIZATION_FILENAME = "plugin_customization.ini"; //$NON-NLS-1$
	private static final String PROPERTIES_FILE_EXTENSION = "properties"; //$NON-NLS-1$
	private EclipsePreferences loadLevel;

	// cached values
	private String qualifier;
	private int segmentCount;
	private Plugin plugin;

	/**
	 * Default constructor for this class.
	 */
	public DefaultPreferences() {
		this(null, null);
	}

	private DefaultPreferences(IEclipsePreferences parent, String name, Plugin context) {
		this(parent, name);
		this.plugin = context;
	}

	private DefaultPreferences(IEclipsePreferences parent, String name) {
		super(parent, name);

		if (parent instanceof DefaultPreferences)
			this.plugin = ((DefaultPreferences) parent).plugin;

		// get the children
		initializeChildren();

		// cache the segment count
		String path = absolutePath();
		segmentCount = getSegmentCount(path);
		if (segmentCount < 2)
			return;

		// cache the qualifier
		qualifier = getSegment(path, 1);
	}

	/*
	 * Apply the values set in the bundle's install directory.
	 * 
	 * In Eclipse 2.1 this is equivalent to:
	 *		/eclipse/plugins/<pluginID>/prefs.ini
	 */
	private void applyBundleDefaults() {
		Bundle bundle = Platform.getBundle(name());
		if (bundle == null)
			return;
		URL url = Platform.find(bundle, new Path(Plugin.PREFERENCES_DEFAULT_OVERRIDE_FILE_NAME));
		if (url == null) {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Preference default override file not found for bundle: " + bundle.getSymbolicName()); //$NON-NLS-1$
			return;
		}
		URL transURL = Platform.find(bundle, NL_DIR.append(Plugin.PREFERENCES_DEFAULT_OVERRIDE_BASE_NAME).addFileExtension(PROPERTIES_FILE_EXTENSION));
		if (transURL == null && InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Preference translation file not found for bundle: " + bundle.getSymbolicName()); //$NON-NLS-1$
		applyDefaults(name(), loadProperties(url), loadProperties(transURL));
	}

	/*
	 * Apply the default values as specified in the file
	 * as an argument on the command-line.
	 */
	private void applyCommandLineDefaults() {
		String filename = InternalPlatform.pluginCustomizationFile;
		if (filename == null) {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Command-line preferences customization file not specified."); //$NON-NLS-1$
			return;
		}
		if (InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Using command-line preference customization file: " + filename); //$NON-NLS-1$
		applyDefaults(null, loadProperties(filename), null);
	}

	/*
	 * If the qualifier is null then the file is of the format:
	 * 	pluginID/key=value
	 * otherwise the file is of the format:
	 * 	key=value
	 */
	private void applyDefaults(String id, Properties defaultValues, Properties translations) {
		for (Enumeration e = defaultValues.keys(); e.hasMoreElements();) {
			String fullKey = (String) e.nextElement();
			String value = defaultValues.getProperty(fullKey);
			if (value == null)
				continue;
			IPath childPath = new Path(fullKey);
			String key = childPath.lastSegment();
			childPath = childPath.removeLastSegments(1);
			String localQualifier = id;
			if (id == null) {
				localQualifier = childPath.segment(0);
				childPath = childPath.removeFirstSegments(1);
			}
			if (name().equals(localQualifier)) {
				value = translatePreference(value, translations);
				if (InternalPlatform.DEBUG_PREFERENCES)
					Policy.debug("Setting default preference: " + (new Path(absolutePath()).append(childPath).append(key)) + '=' + value); //$NON-NLS-1$
				((EclipsePreferences) internalNode(childPath.toString(), false, null)).internalPut(key, value);
			}
		}
	}

	private void runInitializer(IConfigurationElement element) {
		AbstractPreferenceInitializer initializer = null;
		try {
			initializer = (AbstractPreferenceInitializer) element.createExecutableExtension(ATTRIBUTE_CLASS);
		} catch (ClassCastException e) {
			String message = Policy.bind("preferences.invalidExtensionSuperclass"); //$NON-NLS-1$
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, e);
			log(status);
		} catch (CoreException e) {
			log(e.getStatus());
		}
		initializer.initializeDefaultPreferences();
	}

	public IEclipsePreferences node(String childName, Plugin context) {
		return internalNode(childName, true, context);
	}

	/*
	 * Runtime defaults are the ones which are specified in code at runtime. 
	 * 
	 * In the Eclipse 2.1 world they were the ones which were specified in the
	 * over-ridden Plugin#initializeDefaultPluginPreferences() method.
	 * 
	 * In Eclipse 3.0 they are set in the code which is indicated by the
	 * extension to the plug-in default customizer extension point.
	 */
	private void applyRuntimeDefaults() {
		// access the extension point
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(Platform.PI_RUNTIME, Platform.PT_PREFERENCES);
		if (point == null) {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("No extensions for " + Platform.PI_RUNTIME + '.' + Platform.PT_PREFERENCES + " extension point. Skipping runtime default preference customization."); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		IExtension[] extensions = point.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++)
				if (ELEMENT_INITIALIZER.equals(elements[j].getName())) {
					if (name().equals(elements[j].getDeclaringExtension().getNamespace())) {
						if (InternalPlatform.DEBUG_PREFERENCES)
							Policy.debug("Running default preference customization as defined by: " + elements[j].getDeclaringExtension().getDeclaringPluginDescriptor()); //$NON-NLS-1$
						runInitializer(elements[j]);
						return;
					}
				}
		}

		// No extension exists. Get the plug-in object and call #initializeDefaultPluginPreferences()
		if (plugin == null)
			plugin = Platform.getPlugin(name());
		if (plugin == null) {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("No plug-in object available to set plug-in default preference overrides for:" + name()); //$NON-NLS-1$
			return;
		}
		if (InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Applying plug-in default preference overrides for plug-in: " + plugin.getDescriptor().getUniqueIdentifier()); //$NON-NLS-1$
		plugin.internalInitializeDefaultPluginPreferences();
	}

	/*
	 * Apply the default values as specified by the file
	 * in the product extension.
	 * 
	 * In Eclipse 2.1 this is equivalent to the plugin_customization.ini
	 * file in the primary feature's plug-in directory.
	 */
	private void applyProductDefaults() {
		IProduct product = Platform.getProduct();
		if (product == null) {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Product not available to set product default preference overrides."); //$NON-NLS-1$
			return;
		}
		String id = product.getId();
		if (id == null) {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Product ID not available to apply product-level preference defaults."); //$NON-NLS-1$
			return;
		}
		Bundle bundle = product.getDefiningBundle();
		if (bundle == null) {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Bundle not available to apply product-level preference defaults for product id: " + id); //$NON-NLS-1$
			return;
		}
		String value = product.getProperty(PRODUCT_KEY);
		URL url = null;
		URL transURL = null;
		if (value == null) {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Product : " + id + " does not define preference customization file. Using legacy file: plugin_customization.ini"); //$NON-NLS-1$//$NON-NLS-2$
			value = LEGACY_PRODUCT_CUSTOMIZATION_FILENAME;
			url = Platform.find(bundle, new Path(LEGACY_PRODUCT_CUSTOMIZATION_FILENAME));
			transURL = Platform.find(bundle, NL_DIR.append(value).removeFileExtension().addFileExtension(PROPERTIES_FILE_EXTENSION));
		} else {
			// try to convert the key to a URL
			try {
				url = new URL(value);
			} catch (MalformedURLException e) {
				// didn't work so treat it as a filename
				url = Platform.find(bundle, new Path(value));
				if (url != null)
					transURL = Platform.find(bundle, NL_DIR.append(value).removeFileExtension().addFileExtension(PROPERTIES_FILE_EXTENSION));
			}
		}
		if (url == null) {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Product preference customization file: " + value + " not found for bundle: " + id); //$NON-NLS-1$//$NON-NLS-2$
			return;
		}
		if (transURL == null && InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("No preference translations found for product/file: " + bundle.getSymbolicName() + '/' + value); //$NON-NLS-1$
		applyDefaults(null, loadProperties(url), loadProperties(transURL));
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#flush()
	 */
	public void flush() {
		// default values are not persisted
	}

	protected IEclipsePreferences getLoadLevel() {
		if (loadLevel == null) {
			if (qualifier == null)
				return null;
			// Make it relative to this node rather than navigating to it from the root.
			// Walk backwards up the tree starting at this node.
			// This is important to avoid a chicken/egg thing on startup.
			EclipsePreferences node = this;
			for (int i = 2; i < segmentCount; i++)
				node = (EclipsePreferences) node.parent();
			loadLevel = node;
		}
		return loadLevel;
	}

	protected void initializeChildren() {
		if (initialized || parent == null)
			return;
		try {
			synchronized (this) {
				BundleContext context = InternalPlatform.getDefault().getBundleContext();
				Bundle[] bundles = context.getBundles();
				for (int i = 0; i < bundles.length; i++) {
					String childName = bundles[i].getSymbolicName();
					if (childName != null)
						addChild(childName, null);
				}
			}
		} finally {
			initialized = true;
		}
	}

	protected EclipsePreferences internalCreate(IEclipsePreferences nodeParent, String nodeName, Plugin context) {
		return new DefaultPreferences(nodeParent, nodeName, context);
	}

	protected boolean isAlreadyLoaded(IEclipsePreferences node) {
		return loadedNodes.contains(node.name());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.preferences.EclipsePreferences#load()
	 */
	protected void load() {
		loadDefaults();
	}

	private void loadDefaults() {
		applyRuntimeDefaults();
		applyBundleDefaults();
		applyProductDefaults();
		applyCommandLineDefaults();
	}

	private Properties loadProperties(URL url) {
		Properties result = new Properties();
		if (url == null)
			return result;
		InputStream input = null;
		try {
			input = Platform.resolve(url).openStream();
			result.load(input);
		} catch (IOException e) {
			if (InternalPlatform.DEBUG_PREFERENCES) {
				Policy.debug("Problem opening stream to preference customization file: " + url); //$NON-NLS-1$
				e.printStackTrace();
			}
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					// ignore
				}
		}
		return result;
	}

	private Properties loadProperties(String filename) {
		Properties result = new Properties();
		InputStream input = null;
		try {
			input = new BufferedInputStream(new FileInputStream(filename));
			result.load(input);
		} catch (FileNotFoundException e) {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Preference customization file not found: " + filename); //$NON-NLS-1$
		} catch (IOException e) {
			String message = Policy.bind("preferences.loadException", filename); //$NON-NLS-1$
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, e);
			InternalPlatform.getDefault().log(status);
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					// ignore
				}
		}
		return result;
	}

	protected void loaded() {
		loadedNodes.add(name());
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#sync()
	 */
	public void sync() {
		// default values are not persisted
	}

	/**
	 * Takes a preference value and a related resource bundle and
	 * returns the translated version of this value (if one exists).
	 */
	private String translatePreference(String value, Properties props) {
		value = value.trim();
		if (props == null || value.startsWith(KEY_DOUBLE_PREFIX))
			return value;
		if (value.startsWith(KEY_PREFIX)) {
			int ix = value.indexOf(" "); //$NON-NLS-1$
			String key = ix == -1 ? value.substring(1) : value.substring(1, ix);
			String dflt = ix == -1 ? value : value.substring(ix + 1);
			return props.getProperty(key, dflt);
		}
		return value;
	}
}