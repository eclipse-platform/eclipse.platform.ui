/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.plugins;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.boot.PlatformURLHandler;
import org.eclipse.core.internal.registry.BundleModel;
import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.osgi.framework.*;

public class PluginDescriptor implements IPluginDescriptor {

	private boolean active = false; // plugin is active
	private volatile boolean activePending = false; // being activated
	private boolean deactivated = false; // plugin deactivated due to startup errors
	protected Plugin pluginObject = null; // plugin object
	private ResourceBundle bundle = null; // plugin.properties
	private Locale locale = null; // bundle locale
	private boolean bundleNotFound = false; // marker to prevent unnecessary lookups
	private Object[] cachedClasspath = null; // cached value of class loader's classpath
	private org.osgi.framework.Bundle bundleOsgi;
	
	static final String PLUGIN_URL = PlatformURLHandler.PROTOCOL + PlatformURLHandler.PROTOCOL_SEPARATOR + "/" + PlatformURLPluginConnection.PLUGIN + "/"; //$NON-NLS-1$ //$NON-NLS-2$

	// constants
	static final String VERSION_SEPARATOR = "_"; //$NON-NLS-1$

	private static final String DEFAULT_BUNDLE_NAME = "plugin"; //$NON-NLS-1$
	private static final String KEY_PREFIX = "%"; //$NON-NLS-1$
	private static final String KEY_DOUBLE_PREFIX = "%%"; //$NON-NLS-1$

	private PluginClassLoader classLoader;

	synchronized public void doPluginDeactivation() {
		pluginObject = null;
		active = false;
		activePending = false;
		deactivated = false;
	}
	/**
	 * convert a list of comma-separated tokens into an array
	 */
	private static String[] getArrayFromList(String prop) {
		if (prop == null || prop.trim().equals("")) //$NON-NLS-1$
			return new String[0];
		Vector list = new Vector();
		StringTokenizer tokens = new StringTokenizer(prop, ","); //$NON-NLS-1$
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken().trim();
			if (!token.equals("")) //$NON-NLS-1$
				list.addElement(token);
		}
		return list.isEmpty() ? new String[0] : (String[]) list.toArray(new String[0]);
	}
	/**
	 * @see IPluginDescriptor
	 */
	public IExtension getExtension(String id) {
		IExtension[] exts = getExtensions();
		for (int i = 0; i < exts.length; i++) {
			if (exts[i].getSimpleIdentifier().equals(id))
				return exts[i];
		}
		return null;
	}
	/**
	 * @see IPluginDescriptor
	 */
	public IExtensionPoint getExtensionPoint(String extensionPointId) {
		return InternalPlatform.getDefault().getRegistry().getExtensionPoint(getId(), extensionPointId);
	}
	/**
	 * @see IPluginDescriptor
	 */
	public IExtensionPoint[] getExtensionPoints() {
		return InternalPlatform.getDefault().getRegistry().getExtensionPoints(getId());
	}

	/**
	 * @see IPluginDescriptor
	 */
	public IExtension[] getExtensions() {
		return org.eclipse.core.internal.runtime.InternalPlatform.getDefault().getRegistry().getExtensions(getId());
	}

	/**
	 * @see IPluginDescriptor
	 */
	public URL getInstallURL() {
		try {
			return new URL(PLUGIN_URL + toString() + "/");
		} catch (IOException e) {
			throw new IllegalStateException(); // unchecked
		}
	}
	/**
	 * @return a URL to the install location that does not need to be resolved.
	 */
	public URL getInstallURLInternal() {
		try {
			return InternalPlatform.getDefault().resolve(getInstallURL());
		} catch (IOException ioe) {
			try {
				return new URL(bundleOsgi.getEntry("plugin.xml"), ".");
			} catch (IOException io) {
				io.printStackTrace();
				return getInstallURL();
			}
		}
	}
	/**
	 * @see IPluginDescriptor
	 */
	public String getLabel() {
		return getId(); //TODO Need to be fixed to do the real thing, ie getting the string from the translation of the manifest.nf
	}

	/**
	 * @see IPluginDescriptor
	 */
	public ClassLoader getPluginClassLoader() {
		synchronized (this) {
			if (classLoader == null)
				classLoader = new PluginClassLoader(this);
		}
		return classLoader;
	}

	public String getFileFromURL(URL target) {
		String protocol = target.getProtocol();
		if (protocol.equals(PlatformURLHandler.FILE))
			return target.getFile();
		if (protocol.equals(PlatformURLHandler.JAR)) {
			// strip off the jar separator at the end of the url then do a recursive call
			// to interpret the sub URL.
			String file = target.getFile();
			file = file.substring(0, file.length() - PlatformURLHandler.JAR_SEPARATOR.length());
			try {
				return getFileFromURL(new URL(file));
			} catch (MalformedURLException e) {
				// ignore bad URLs
			}
		}
		return null;
	}

	/**
	 * @see IPluginDescriptor
	 */
	public IPluginPrerequisite[] getPluginPrerequisites() {
		// only surface resolved pre-requisites because the 
		// PluginPrerequisite implementation needs an existing  
		// bundle to get its info from
		return getPluginResolvedPrerequisites();
	}

	public PluginRegistry getPluginRegistry() {
		return (PluginRegistry) org.eclipse.core.internal.plugins.InternalPlatform.getPluginRegistry();
	}
	/**
	 * @see IPluginDescriptor
	 */
	public String getProviderName() {
		String providerName = (String) bundleOsgi.getHeaders().get("Bundle-Vendor"); //TODO To fix to do the real thing, ie getting the string from the translation of the manifest.nf
		return providerName == null ? "" : providerName;
		//	String s = super.getProviderName();
		//	if (s == null)
		//		return ""; //$NON-NLS-1$
		//	 String localized = getResourceString(s);
		//	 if (localized != s)
		//		setLocalizedProviderName(localized);
		//	 return localized;
	}
	/**
	 * @see IPluginDescriptor
	 */
	public ResourceBundle getResourceBundle() throws MissingResourceException {
		return getResourceBundle(Locale.getDefault());
	}
	public ResourceBundle getResourceBundle(Locale targetLocale) throws MissingResourceException {
		// we cache the bundle for a single locale 
		if (bundle != null && targetLocale.equals(locale))
			return bundle;

		try {
			BundleModel bundleModel = (BundleModel) ((ExtensionRegistry) InternalPlatform.getDefault().getRegistry()).getElement(bundleOsgi.getGlobalName());
			bundle = bundleModel.getResourceBundle(targetLocale);
		} catch (MissingResourceException e) {
			bundleNotFound = true;
			throw e;
		}
		return bundle;
	}
	/**
	 * @see IPluginDescriptor
	 */
	public String getResourceString(String value) {
		return getResourceString(value, null);
	}
	/**
	 * @see IPluginDescriptor
	 */
	public String getResourceString(String value, ResourceBundle b) {
		String s = value.trim();
		if (!s.startsWith(KEY_PREFIX))
			return s;
		if (s.startsWith(KEY_DOUBLE_PREFIX))
			return s.substring(1);

		int ix = s.indexOf(" "); //$NON-NLS-1$
		String key = ix == -1 ? s : s.substring(0, ix);
		String dflt = ix == -1 ? s : s.substring(ix + 1);

		if (b == null) {
			try {
				b = getResourceBundle();
			} catch (MissingResourceException e) {
				// just return the default (dflt)
			}
		}

		if (b == null)
			return dflt;

		try {
			return b.getString(key.substring(1));
		} catch (MissingResourceException e) {
			//this will avoid requiring a bundle access on the next lookup
			return "%" + dflt; //$NON-NLS-1$
		}
	}
	/**
	 * @see IPluginDescriptor
	 */
	public ILibrary[] getRuntimeLibraries() {
		if (!isLegacy())
			return new ILibrary[0];

		ArrayList allLibraries = new ArrayList();
		ArrayList allBundes = new ArrayList();
		allBundes.add(bundleOsgi);
		Bundle[] fragments = bundleOsgi.getFragments();
		if (fragments != null)
			allBundes.addAll(Arrays.asList(fragments));
		
		for (Iterator iter = allBundes.iterator(); iter.hasNext();) {
			Bundle element = (Bundle) iter.next();
			String classpath = (String) element.getHeaders().get("Bundle-Classpath");
			if (classpath != null)
				allLibraries.addAll(splitClasspath(classpath));	
		}
		return (ILibrary[]) allLibraries.toArray(new ILibrary[allLibraries.size()]);
	}
	
	private ArrayList splitClasspath(String classpath) {
		StringTokenizer tokens = new StringTokenizer(classpath, ",");
		ArrayList libraries = new ArrayList(tokens.countTokens());
		while (tokens.hasMoreElements()) {
			String element = (String) tokens.nextElement();
			libraries.add(new Library(element.trim()));
		}
		return libraries;
	}
	
	/**
	 * @see IPluginDescriptor
	 */
	public String getUniqueIdentifier() {
		return getId();
	}
	/**
	 * @see #toString
	 */
	public static String getUniqueIdentifierFromString(String pluginString) {
		int ix = pluginString.indexOf(VERSION_SEPARATOR);
		return ix == -1 ? pluginString : pluginString.substring(0, ix);
	}
	/**
	 * @see IPluginDescriptor
	 */
	public PluginVersionIdentifier getVersionIdentifier() {
		String version = (String) bundleOsgi.getHeaders().get("Bundle-Version");
		if (version == null)
			return new PluginVersionIdentifier("1.0.0"); //$NON-NLS-1$
		try {
			return new PluginVersionIdentifier(version);
		} catch (Exception e) {
			return new PluginVersionIdentifier("1.0.0"); //$NON-NLS-1$
		}
	}
	/**
	 * @see #toString
	 */
	public static PluginVersionIdentifier getVersionIdentifierFromString(String pluginString) {
		return new PluginVersionIdentifier(pluginString);
	}
	/**
	 * Returns all pre-requisites that have been properly resolved, excluding any
	 * redundant references to Platform.PI_RUNTIME and BootLoader.PI_BOOT.
	 */
	public IPluginPrerequisite[] getPluginResolvedPrerequisites() {
		if (!isLegacy())
			return new IPluginPrerequisite[0];

		ArrayList resolvedPrerequisites = null;
		// TODO Who calls this?  It does not do the right thing as it is so perhaps it is not used?
		String prereqs = (String) bundleOsgi.getHeaders().get(Constants.REQUIRE_BUNDLE);
		if (prereqs == null)
			return new IPluginPrerequisite[0];

		StringTokenizer tokens = new StringTokenizer(prereqs, ",");
		resolvedPrerequisites = new ArrayList(tokens.countTokens());
		while (tokens.hasMoreElements()) {
			String prereqId = tokens.nextToken().trim();
			if (prereqId.equalsIgnoreCase(Platform.PI_RUNTIME) || prereqId.equalsIgnoreCase(BootLoader.PI_BOOT))
				continue;
			Bundle prereqBundle = org.eclipse.core.internal.runtime.InternalPlatform.getDefault().getBundleContext().getBundle(prereqId);
			if (prereqBundle != null)
				resolvedPrerequisites.add(new PluginPrerequisite(prereqBundle));
			else if (BootLoader.inDebugMode())
				System.out.println("Plugin " + this.getId() + " has unknown prerequisite: " + prereqId);
		}
		return (IPluginPrerequisite[]) resolvedPrerequisites.toArray(new IPluginPrerequisite[resolvedPrerequisites.size()]);
	}

	/**
	 * Returns true if the plugin is active or is currently in the process of being 
	 * activated, and false otherwse.
	 * NOTE: This method is not synchronized because it is called from within a
	 * sync block in PluginClassLoader.
	 */
	boolean hasActivationStarted() {
		return activePending || active;
	}
	/**
	 * @see IPluginDescriptor
	 */
	public synchronized boolean isPluginActivated() {
		//note that this method is synchronized for good reason.  
		//During plugin activation, neither true nor false would be valid
		//return values for this method, so it must block until activation
		//completes.  For example, returning false during activation
		//would break the registry shutdown procedure, because a
		//plugin being activated during shutdown would never be shut down.
		return active;
	}
	/*
	 * NOTE: This method is not synchronized because it is called from within a
	 * sync block in PluginClassLoader.
	 */
	public boolean isPluginDeactivated() {
		return deactivated;
	}
	private void logError(IStatus status) {
		InternalPlatform.getDefault().getLog(org.eclipse.core.internal.runtime.InternalPlatform.getDefault().getBundle("org.eclipse.core.runtime")).log(status);
	}
	/**
	 * Returns <code>true</code> if we should continue with the plugin activation.
	 */
	private boolean pluginActivationEnter() throws CoreException {
		if (deactivated) {
			// had permanent error on startup
			String errorMsg = Policy.bind("plugin.pluginDisabled", getId()); //$NON-NLS-1$
			throwException(errorMsg, null);
		}
		if (active || activePending) {
			// already up and running 
			return false;
		}
		activePending = true;
		// go ahead and try to activate
		return true;
	}
	private void pluginActivationExit(boolean errorExit) {
		if (errorExit) {
			active = false;
			deactivated = true;
		} else
			active = true;
		// we are done with the activation
		activePending = false;
	}
	private void throwException(String message, Throwable exception) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, Platform.PLUGIN_ERROR, message, exception);
		logError(status);
		throw new CoreException(status);
	}
	/**
	 * @see #getUniqueIdentifierFromString
	 * @see #getVersionIdentifierFromString
	 */
	public String toString() {
		return getUniqueIdentifier() + VERSION_SEPARATOR + getVersionIdentifier().toString();
	}
	/**
	 * @see IPluginDescriptor
	 */
	public final URL find(IPath path) {
		return FindSupport.find(bundleOsgi, path);
	}
	/**
	 * @see IPluginDescriptor
	 */
	public final URL find(IPath path, Map override) {
		return FindSupport.find(bundleOsgi, path, override);
	}

	/**
	 * @see IPluginDescriptor
	 */
	public Plugin getPlugin() throws CoreException {
		if (pluginObject == null)
			doPluginActivation();
		return pluginObject;
	}

	synchronized void doPluginActivation() throws CoreException {
		// this method is called by the class loader just prior 
		// to getting a class. It needs to handle the
		// case where it is called multiple times during the activation
		// processing itself (as a result of other classes from this
		// plugin being directly referenced by the plugin class)

		// NOTE: there is a remote scenario where the plugin class can
		// deadlock, if it starts separate thread(s) within its
		// constructor or startup() method, and waits on those
		// threads before returning (ie. calls join()).

		// sanity checking
		if ((bundleOsgi.getState() & (Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE)) == 0)
			throw new IllegalArgumentException();
		// plug-in hasn't been activated yet - start bundle
		if (bundleOsgi.getState() == Bundle.RESOLVED)
			try {
				bundleOsgi.start();
			} catch (BundleException e) {
				throwException(Policy.bind("plugin.startupProblems", e.toString()), e);
			}
		if (pluginObject != null) 
			return;
		boolean errorExit = true;			
		//	check if already activated or pending		
		if (pluginActivationEnter()) 
			try {
				internalDoPluginActivation();
				errorExit = false;
			} finally {
				pluginActivationExit(errorExit);
			}	
}

	private String getPluginClass() {
		return (String) bundleOsgi.getHeaders().get("Plugin-class");
	}

	private String getId() {
		return bundleOsgi.getGlobalName();
	}

	private void internalDoPluginActivation() throws CoreException {
		String errorMsg;
		// load the runtime class 
		String pluginClassName = getPluginClass();
		Class runtimeClass = null;
		try {
			if (pluginClassName == null || pluginClassName.equals("")) //$NON-NLS-1$
				runtimeClass = DefaultPlugin.class;
			else
				runtimeClass = bundleOsgi.loadClass(pluginClassName);
		} catch (ClassNotFoundException e) {
			errorMsg = Policy.bind("plugin.loadClassError", getId(), pluginClassName); //$NON-NLS-1$
			throwException(errorMsg, e);
		}

		// find the correct constructor
		Constructor construct = null;
		try {
			construct = runtimeClass.getConstructor(new Class[]{IPluginDescriptor.class});
		} catch (NoSuchMethodException eNoConstructor) {
			errorMsg = Policy.bind("plugin.instantiateClassError", getId(), pluginClassName); //$NON-NLS-1$
			throwException(errorMsg, eNoConstructor);
		}

		// create a new instance
		try {
			pluginObject = (Plugin) construct.newInstance(new Object[]{this});
		} catch (ClassCastException e) {
			errorMsg = Policy.bind("plugin.notPluginClass", pluginClassName); //$NON-NLS-1$
			throwException(errorMsg, e);
		} catch (Exception e) {
			errorMsg = Policy.bind("plugin.instantiateClassError", getId(), pluginClassName); //$NON-NLS-1$
			throwException(errorMsg, e);
		}
	}

	public PluginDescriptor(org.osgi.framework.Bundle b) {
		bundleOsgi = b;
		if( (b.getState() & Bundle.ACTIVE) != 0 )
			active = true;
	}
	public boolean isLegacy() {
		return new Boolean((String) bundleOsgi.getHeaders().get("Legacy")).booleanValue();
	}

	public Bundle getBundle() {
		return bundleOsgi;
	}
	/** @see PluginModel#getLocation() */
	public String getLocation() {
		return getInstallURLInternal().toExternalForm();
	}
	
	public void setPlugin(Plugin object) { 
		pluginObject = object;
	}
}
