/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.osgi.framework.Bundle;

/**
 * An object which represents the user-defined contents of a bundle model
 * in a extensions manifest.
 */
public class BundleModel extends RegistryModelObject implements IRegistryElement {
	private final static String NO_EXTENSION_MUNGING = "eclipse.noExtensionMunging"; //$NON-NLS-1$ //System property
	private final static String DEFAULT_BUNDLE_NAME = "plugin"; //$NON-NLS-1$
	private static final String KEY_PREFIX = "%"; //$NON-NLS-1$
	private static final String KEY_DOUBLE_PREFIX = "%%"; //$NON-NLS-1$	
	private static String[] NL_JAR_VARIANTS = buildNLVariants(InternalPlatform.getDefault().getNL());

	private String hostId;
	private IExtensionPoint[] extensionPoints;
	private IExtension[] extensions;

	private transient Object locale;
	private transient boolean bundleNotFound;
	private transient ResourceBundle resourceBundle;

	private long bundleId; //Introduced to fix #46308 

	//Introduced for UI backward compatibility
	private static Map renamedUIextensionPoints;
	private String schemaVersion;

	public String getUniqueIdentifier() {
		return getName();
	}
	public void setUniqueIdentifier(String value) {
		setName(value);
	}
	public void setExtensions(IExtension[] value) {
		assertIsWriteable();
		extensions = value;
		fixRenamedExtensionPoints();
	}
	public IExtension getExtension(String id) {
		if (id == null)
			return null;
		IExtension[] list = getExtensions();
		if (list == null)
			return null;
		for (int i = 0; i < list.length; i++) {
			if (id.equals(list[i].getUniqueIdentifier()))
				return list[i];
		}
		return null;
	}
	public IExtension[] getExtensions() {
		return extensions == null ? new IExtension[0] : extensions;
	}
	public void markReadOnly() {
		super.markReadOnly();
		if (extensionPoints != null)
			for (int i = 0; i < extensionPoints.length; i++)
				 ((ExtensionPoint) extensionPoints[i]).markReadOnly();
		if (extensions != null)
			for (int i = 0; i < extensions.length; i++)
				 ((Extension) extensions[i]).markReadOnly();
	}
	public void setExtensionPoints(IExtensionPoint[] value) {
		assertIsWriteable();
		extensionPoints = value;
	}
	public IExtensionPoint getExtensionPoint(String xpt) {
		if (xpt == null)
			return null;
		IExtensionPoint[] list = getExtensionPoints();
		if (list == null)
			return null;
		for (int i = 0; i < list.length; i++) {
			if (xpt.equals(list[i].getSimpleIdentifier()))
				return list[i];
		}
		return null;
	}
	public IExtensionPoint[] getExtensionPoints() {
		return extensionPoints == null ? new IExtensionPoint[0] : extensionPoints;
	}
	public ResourceBundle getResourceBundle() throws MissingResourceException {
		return getResourceBundle(Locale.getDefault());
	}
	private static String[] buildNLVariants(String nl) {
		ArrayList result = new ArrayList();
		int lastSeparator;

		while ((lastSeparator = nl.lastIndexOf('_')) != -1) {
			result.add(nl);
			if (lastSeparator != -1) {
				nl = nl.substring(0, lastSeparator);
			}
		}
		result.add(nl);

		return (String[]) result.toArray(new String[result.size()]);
	}
	//Search for properties file the same way the ResourceBundle algorithm does it
	private URL findProperties(Bundle bundle, String path) {
		String[] nlVariants = NL_JAR_VARIANTS;
		URL result = null;
		for (int i = 0; i < nlVariants.length; i++) {
			String filePath = path.concat("_" + nlVariants[i] + ".properties");
			result = findInPlugin(bundle, filePath);
			if (result != null)
				return result;
			result = findInFragments(bundle, filePath);
			if (result != null)
				return result;
		}
		// If we get to this point, we haven't found it yet.
		// Look in the plugin and fragment root directories
		result = findInPlugin(bundle, path + ".properties");
		if (result != null)
			return result;
		return findInFragments(bundle, path + ".properties");
	}
	private URL findInPlugin(Bundle bundle, String filePath) {
		return bundle.getEntry(filePath);
	}
	private URL findInFragments(Bundle bundle, String filePath) {
		Bundle[] fragments = InternalPlatform.getDefault().getFragments(bundle);
		URL fileURL = null;
		int i = 0;
		while (fragments != null && i < fragments.length && fileURL == null) {
			fileURL = fragments[i].getEntry(filePath);
			i++;
		}
		return fileURL;
	}
	public ResourceBundle getResourceBundle(Locale targetLocale) throws MissingResourceException {
		// we cache the bundle for a single locale 
		if (resourceBundle != null && targetLocale.equals(locale))
			return resourceBundle;

		// check if we already tried and failed
		if (bundleNotFound)
			throw new MissingResourceException(Policy.bind("plugin.bundleNotFound", getName(), DEFAULT_BUNDLE_NAME + "_" + targetLocale), DEFAULT_BUNDLE_NAME + "_" + targetLocale, ""); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		Bundle bundle = InternalPlatform.getDefault().getBundle(this.getName());

		URL resourceURL = null;
		//TODO Need to make sure that the NL fragments are provided flat.
		resourceURL = findProperties(bundle, DEFAULT_BUNDLE_NAME);

		if (resourceURL == null) {
			bundleNotFound = true;
			resourceBundle = null;
			throw new MissingResourceException(Policy.bind("plugin.bundleNotFound", getName(), DEFAULT_BUNDLE_NAME + "_" + targetLocale), DEFAULT_BUNDLE_NAME + "_" + targetLocale, ""); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		};

		InputStream resourceStream = null;
		try {
			resourceStream = resourceURL.openStream();
			resourceBundle = new PropertyResourceBundle(resourceStream);
		} catch (IOException e2) {
			if (resourceStream != null) {
				try {
					resourceStream.close();
				} catch (IOException e3) {
					//Ignore exception
				}
			}
			bundleNotFound = true;
			throw new MissingResourceException(Policy.bind("plugin.bundleNotFound", getName(), DEFAULT_BUNDLE_NAME + "_" + targetLocale), DEFAULT_BUNDLE_NAME + "_" + targetLocale, ""); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		locale = targetLocale;

		return resourceBundle;
	}
	public String getResourceString(String value) {
		return getResourceString(value, null);
	}
	public String getResourceString(String value, ResourceBundle bundle) {

		String s = value.trim();
		if (!s.startsWith(KEY_PREFIX))
			return s;
		if (s.startsWith(KEY_DOUBLE_PREFIX))
			return s.substring(1);

		int ix = s.indexOf(" "); //$NON-NLS-1$
		String key = ix == -1 ? s : s.substring(0, ix);
		String dflt = ix == -1 ? s : s.substring(ix + 1);

		if (bundle == null) {
			try {
				bundle = getResourceBundle();
			} catch (MissingResourceException e) {
				// just return the default (dflt)
			}
		}

		if (bundle == null)
			return dflt;

		try {
			return bundle.getString(key.substring(1));
		} catch (MissingResourceException e) {
			//this will avoid requiring a bundle access on the next lookup
			return "%" + dflt; //$NON-NLS-1$
		}
	}
	public void setHostIdentifier(String value) {
		hostId = value;
	}
	public String getHostIdentifier() {
		return hostId;
	}
	public boolean isFragment() {
		return hostId != null;
	}
	public String toString() {
		return "BundleModel: " + getName();
	}
	/**
	 * Fixes up the extension declarations in the given pre-3.0 plug-in or fragment to compensate
	 * for extension points that were renamed between release 2.1 and 3.0.
	 * 
	 * @param plugin the pre-3.0 plug-in or fragment
	 * @param factory the factory for creating new model objects
	 * @since 3.0
	 */
	private void fixRenamedExtensionPoints() {
		if (extensions == null || (schemaVersion != null && schemaVersion.equals("3.0")) || System.getProperties().get(NO_EXTENSION_MUNGING) != null)
			return;
		if (renamedUIextensionPoints == null) {
			// lazily initialize 
			final Map t = new HashMap(13);
			t.put("org.eclipse.ui.markerImageProvider", "org.eclipse.ui.ide.markerImageProvider"); //$NON-NLS-1$ //$NON-NLS-2$
			t.put("org.eclipse.ui.markerHelp", "org.eclipse.ui.ide.markerHelp"); //$NON-NLS-1$ //$NON-NLS-2$
			t.put("org.eclipse.ui.markerImageProviders", "org.eclipse.ui.ide.markerImageProviders"); //$NON-NLS-1$ //$NON-NLS-2$
			t.put("org.eclipse.ui.markerResolution", "org.eclipse.ui.ide.markerResolution"); //$NON-NLS-1$ //$NON-NLS-2$
			t.put("org.eclipse.ui.projectNatureImages", "org.eclipse.ui.ide.projectNatureImages"); //$NON-NLS-1$ //$NON-NLS-2$
			t.put("org.eclipse.ui.resourceFilters", "org.eclipse.ui.ide.resourceFilters"); //$NON-NLS-1$ //$NON-NLS-2$
			t.put("org.eclipse.ui.markerUpdaters", "org.eclipse.ui.editors.markerUpdaters"); //$NON-NLS-1$ //$NON-NLS-2$
			t.put("org.eclipse.ui.documentProviders", "org.eclipse.ui.editors.documentProviders"); //$NON-NLS-1$ //$NON-NLS-2$
			t.put("org.eclipse.ui.workbench.texteditor.markerAnnotationSpecification", "org.eclipse.ui.editors.markerAnnotationSpecification"); //$NON-NLS-1$ //$NON-NLS-2$
			t.put("org.eclipse.help.browser", "org.eclipse.help.base.browser"); //$NON-NLS-1$ //$NON-NLS-2$
			t.put("org.eclipse.help.luceneAnalyzer", "org.eclipse.help.base.luceneAnalyzer"); //$NON-NLS-1$ //$NON-NLS-2$
			t.put("org.eclipse.help.webapp", "org.eclipse.help.base.webapp"); //$NON-NLS-1$ //$NON-NLS-2$
			t.put("org.eclipse.help.support", "org.eclipse.ui.helpSupport"); //$NON-NLS-1$ //$NON-NLS-2$
			renamedUIextensionPoints = t;
		}
		for (int i = 0; i < extensions.length; i++) {
			Extension extension = (Extension) extensions[i];
			String oldPointId = extension.getExtensionPointIdentifier();
			String newPointId = (String) renamedUIextensionPoints.get(oldPointId);
			if (newPointId != null) {
				extension.setExtensionPointIdentifier(newPointId);
			}
		}
	}

	protected void setSchemaVersion(String schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	public long getId() {
		return bundleId;
	}
	public void setId(long value) {
		bundleId = value;
	}
}