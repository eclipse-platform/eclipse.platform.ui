/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.core.internal.boot.PlatformURLHandler;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.osgi.framework.*;

public class PluginDescriptor implements IPluginDescriptor {

	private static final String PLUGIN_CLASS = "Plugin-Class"; //$NON-NLS-1$
	private static final String LEGACY = "Legacy"; //$NON-NLS-1$
	private boolean active = false; // plugin is active
	private volatile boolean activePending = false; // being activated
	private boolean deactivated = false; // plugin deactivated due to startup errors
	protected Plugin pluginObject = null; // plugin object
	private Bundle bundleOsgi;
	
	private PluginClassLoader classLoader;
	
	// constants
	static final String PLUGIN_URL = PlatformURLHandler.PROTOCOL + PlatformURLHandler.PROTOCOL_SEPARATOR + "/" + PlatformURLPluginConnection.PLUGIN + "/"; //$NON-NLS-1$ //$NON-NLS-2$
	static final String VERSION_SEPARATOR = "_"; //$NON-NLS-1$
	
	synchronized public void doPluginDeactivation() {
		pluginObject = null;
		active = false;
		activePending = false;
		deactivated = false;
	}
	/**
	 * convert a list of comma-separated tokens into an array
	 *TODO This method is not used. 
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
	public IExtensionPoint getExtensionPoint(String extensionPointId) {	//TODO This code only works if the underlying bundle as a symbolicName
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
	//TODO this can be private
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
		String key = (String) bundleOsgi.getHeaders().get(Constants.BUNDLE_NAME);
		if (key == null)
			return "";
		return getResourceString(key);
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

	//TODO This does not seems to be used
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

	public PluginRegistry getPluginRegistry() {
		return (PluginRegistry) org.eclipse.core.internal.plugins.InternalPlatform.getPluginRegistry();
	}
	/**
	 * @see IPluginDescriptor
	 */
	public String getProviderName() {
		String providerName = (String) bundleOsgi.getHeaders().get(Constants.BUNDLE_VENDOR);
		if (providerName == null)
			return ""; //$NON-NLS-1$
		 return getResourceString(providerName);
	}
	/**
	 * @see IPluginDescriptor
	 */
	public ResourceBundle getResourceBundle() throws MissingResourceException {
		return InternalPlatform.getDefault().getResourceBundle(bundleOsgi);
	}

	/**
	 * @see IPluginDescriptor
	 */
	public String getResourceString(String value) {
		return InternalPlatform.getDefault().getResourceString(bundleOsgi, value);
	}
	/**
	 * @see IPluginDescriptor
	 */
	public String getResourceString(String value, ResourceBundle b) {
		return InternalPlatform.getDefault().getResourceString(bundleOsgi, value, b);
	}
	/**
	 * @see IPluginDescriptor
	 */
	public ILibrary[] getRuntimeLibraries() {
		if (!isLegacy())		//TODO Remove this check
			return new ILibrary[0];

		ArrayList allLibraries = new ArrayList();
		ArrayList allBundes = new ArrayList();
		allBundes.add(bundleOsgi);
		Bundle[] fragments = InternalPlatform.getDefault().getFragments(bundleOsgi);
		if (fragments != null)
			allBundes.addAll(Arrays.asList(fragments));
		
		for (Iterator iter = allBundes.iterator(); iter.hasNext();) {
			Bundle element = (Bundle) iter.next();
			String classpath = (String) element.getHeaders().get(Constants.BUNDLE_CLASSPATH);
			if (classpath != null)
				allLibraries.addAll(splitClasspath(classpath));		//TODO This should use ManifestElement. If this is done then splitClasspath can be removed
		}
		return (ILibrary[]) allLibraries.toArray(new ILibrary[allLibraries.size()]);
	}
	
	private ArrayList splitClasspath(String classpath) {
		StringTokenizer tokens = new StringTokenizer(classpath, ",");	//$NON-NLS-1$
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
		String version = (String) bundleOsgi.getHeaders().get(Constants.BUNDLE_VERSION);
		if (version == null)
			return new PluginVersionIdentifier("1.0.0"); //$NON-NLS-1$	//TODO Why do we have 1.0.0 as default  value ?
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

	
	public IPluginPrerequisite[] getPluginPrerequisites() {
		if (!isLegacy())	//TODO Remove this check
			return new IPluginPrerequisite[0];

		BundleDescription description = Platform.getPlatformAdmin().getState(false).getBundle(bundleOsgi.getBundleId());
		BundleSpecification[] specs = description.getRequiredBundles();
		
		IPluginPrerequisite[] resolvedPrerequisites = new IPluginPrerequisite[specs.length];
		for (int j = 0; j < specs.length; j++) {
			resolvedPrerequisites[j] = new PluginPrerequisite(specs[j]);
		}
		return resolvedPrerequisites;
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
	 * sync block in PluginClassLoader.	//TODO This is no longer true
	 */
	public boolean isPluginDeactivated() {
		return deactivated;
	}
	private void logError(IStatus status) {
		InternalPlatform.getDefault().getLog(org.eclipse.core.internal.runtime.InternalPlatform.getDefault().getBundleContext().getBundle()).log(status);
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
		URL result = FindSupport.find(bundleOsgi, path);
		if (result != null)
			try {
				result = Platform.resolve(result);
			} catch (IOException e) {
				// if the URL cannot be resolved for some reason, return the original result.
			}
		return result;
	}
	/**
	 * @see IPluginDescriptor
	 */
	public final URL find(IPath path, Map override) {
		URL result = FindSupport.find(bundleOsgi, path, override);
		if (result != null)
			try {
				result = Platform.resolve(result);
			} catch (IOException e) {
				// if the URL cannot be resolved for some reason, return the original result.
			}
		return result;
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
		// plugin being directly referenced by the plugin class)	//TODO Invalid comment

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
				throwException(Policy.bind("plugin.startupProblems", e.toString()), e); //$NON-NLS-1$
			}
		if (pluginObject != null) 
			return;
		boolean errorExit = true;			
		//	check if already activated or pending		
		if (pluginActivationEnter()) { 
			try {
				internalDoPluginActivation();
				errorExit = false;
			} finally {
				pluginActivationExit(errorExit);
			}
		} else {
			//Create a fake plugin object for all new bundles that do not use the Plugin class in their activator hierarchy
			if (active && pluginObject == null) {
				active = false;
				pluginObject = new DefaultPlugin(this);
				active = true;
			}
		}
		
}

	private String getPluginClass() {
		return (String) bundleOsgi.getHeaders().get(PLUGIN_CLASS);
	}

	private String getId() {
		return bundleOsgi.getSymbolicName();
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
		return new Boolean((String) bundleOsgi.getHeaders().get(LEGACY)).booleanValue(); //$NON-NLS-1$
	}

	public Bundle getBundle() {
		return bundleOsgi;
	}
	
	public String getLocation() {
		return getInstallURLInternal().toExternalForm();
	}
	
	public void setPlugin(Plugin object) { 
		pluginObject = object;
	}
	
	public synchronized void setActive() {
		this.active = true;
	}
}
