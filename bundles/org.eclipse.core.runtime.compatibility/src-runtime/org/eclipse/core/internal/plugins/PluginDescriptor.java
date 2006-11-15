/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.plugins;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;
import org.eclipse.core.internal.boot.PlatformURLHandler;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.*;


/**
 * @deprecated Marking as deprecated to remove the warnings
 */
public class PluginDescriptor implements IPluginDescriptor {

	private static final String PLUGIN_CLASS = "Plugin-Class"; //$NON-NLS-1$
	protected Plugin pluginObject = null; // plugin object
	private Bundle bundleOsgi;

	//The three following fields can't be replaced by a test to the bundle state.
	private boolean active = false; // plugin is active
	private volatile boolean activePending = false; // being activated
	private boolean deactivated = false; // plugin deactivated due to startup errors
	private ResourceBundle resources = null; 
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
			return new URL(PLUGIN_URL + toString() + '/');
		} catch (IOException e) {
			throw new IllegalStateException(); // unchecked
		}
	}

	/**
	 * @see IPluginDescriptor
	 */
	public String getLabel() {
		return (String) bundleOsgi.getHeaders().get(Constants.BUNDLE_NAME);
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

	public PluginRegistry getPluginRegistry() {
		return (PluginRegistry) org.eclipse.core.internal.plugins.InternalPlatform.getPluginRegistry();
	}

	/**
	 * @see IPluginDescriptor
	 */
	public String getProviderName() {
		return (String) bundleOsgi.getHeaders().get(Constants.BUNDLE_VENDOR);
	}

	/**
	 * @see IPluginDescriptor
	 */
	public ResourceBundle getResourceBundle() throws MissingResourceException {
		if (resources==null)
			resources = ResourceTranslator.getResourceBundle(bundleOsgi);
		return resources;
	}

	/**
	 * @see IPluginDescriptor
	 */
	public String getResourceString(String value) {
		return ResourceTranslator.getResourceString(bundleOsgi, value);
	}

	/**
	 * @see IPluginDescriptor
	 */
	public String getResourceString(String value, ResourceBundle b) {
		return ResourceTranslator.getResourceString(bundleOsgi, value, b);
	}

	/**
	 * @see IPluginDescriptor
	 */
	public ILibrary[] getRuntimeLibraries() {
		Bundle[] allBundles;
		Bundle[] fragments = InternalPlatform.getDefault().getFragments(bundleOsgi);
		if (fragments != null) {
			allBundles = new Bundle[fragments.length + 1];
			allBundles[0] = bundleOsgi;
			System.arraycopy(fragments, 0, allBundles, 1, fragments.length);
		} else
			allBundles = new Bundle[] {bundleOsgi};
		ArrayList allLibraries = new ArrayList();
		// keep track of whether or not we have already added a "." to this classpath
		boolean addedDot = false;
		for (int i = 0; i < allBundles.length; i++)
			try {
				ManifestElement[] classpathElements = ManifestElement.parseHeader(Constants.BUNDLE_CLASSPATH, (String) allBundles[i].getHeaders("").get(Constants.BUNDLE_CLASSPATH)); //$NON-NLS-1$
				// if there is no bundle classpath header, then we have to 
				// add "." to the classpath
				if (classpathElements == null) {
					if (addedDot) 
						continue; 
					allLibraries.add(new Library(".")); //$NON-NLS-1$
					addedDot = true;
				} else
					for (int j = 0; j < classpathElements.length; j++)
						allLibraries.add(new Library(classpathElements[j].getValue()));
			} catch (BundleException e) {
				//Ignore because by the time we get here the errors will have already been logged.
			}
		return (ILibrary[]) allLibraries.toArray(new ILibrary[allLibraries.size()]);
	}

	/**
	 * @see IPluginDescriptor
	 */
	public String getUniqueIdentifier() {
		return getId();
	}

	/**
	 * @see #toString()
	 */
	public static String getUniqueIdentifierFromString(String pluginString) {
		int ix = pluginString.indexOf(VERSION_SEPARATOR);
		return ix == -1 ? pluginString : pluginString.substring(0, ix);
	}

	/**
	 * @see IPluginDescriptor
	 */
	public PluginVersionIdentifier getVersionIdentifier() {
		String version = (String) bundleOsgi.getHeaders("").get(Constants.BUNDLE_VERSION); //$NON-NLS-1$
		try {
			return new PluginVersionIdentifier(version);
		} catch (Exception e) {
			return new PluginVersionIdentifier("1.0.0"); //$NON-NLS-1$
		}
	}

	/**
	 * @see #toString()
	 */
	public static PluginVersionIdentifier getVersionIdentifierFromString(String pluginString) {
		return new PluginVersionIdentifier(pluginString);
	}

	public IPluginPrerequisite[] getPluginPrerequisites() {
		BundleDescription description = Platform.getPlatformAdmin().getState(false).getBundle(bundleOsgi.getBundleId());
		BundleSpecification[] specs = description.getRequiredBundles();

		IPluginPrerequisite[] resolvedPrerequisites = new IPluginPrerequisite[specs.length];
		for (int j = 0; j < specs.length; j++)
			resolvedPrerequisites[j] = new PluginPrerequisite(specs[j]);
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
		return bundleOsgi.getState() == Bundle.ACTIVE;
	}

	/*
	 * NOTE: This method is not synchronized because it is called from within a
	 * sync block in PluginClassLoader.
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
			String errorMsg = NLS.bind(Messages.plugin_pluginDisabled, getId());
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
	 * @see #getUniqueIdentifierFromString(String)
	 * @see #getVersionIdentifierFromString(String)
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
		//This class is only called when getPlugin() is invoked.
		// It needs to handle the case where it is called multiple times during the activation
		// processing itself (as a result of other classes from this
		// plugin being directly referenced by the plugin class)

		// NOTE: there is a remote scenario where the plugin class can
		// deadlock, if it starts separate thread(s) within its
		// constructor or startup() method, and waits on those
		// threads before returning (ie. calls join()).

		// sanity checking
		if ((bundleOsgi.getState() & (Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE)) == 0)
			throw new IllegalArgumentException();
		try {
			// ensure the bundle has been activated
			InternalPlatform.start(bundleOsgi);
		} catch (BundleException e) {
			throwException(NLS.bind(Messages.plugin_startupProblems, e), e);
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
		return (String) bundleOsgi.getHeaders("").get(PLUGIN_CLASS); //$NON-NLS-1$
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
			if (pluginClassName == null || pluginClassName.equals("")) {//$NON-NLS-1$
				runtimeClass = DefaultPlugin.class;
				pluginClassName = DefaultPlugin.class.getName();
			}
			else
				runtimeClass = bundleOsgi.loadClass(pluginClassName);
		} catch (ClassNotFoundException e) {
			errorMsg = NLS.bind(Messages.plugin_loadClassError, getId(), pluginClassName);
			throwException(errorMsg, e);
		}

		// find the correct constructor
		Constructor construct = null;
		try {
			construct = runtimeClass.getConstructor(new Class[] {IPluginDescriptor.class});
		} catch (NoSuchMethodException eNoConstructor) {
			errorMsg = NLS.bind(Messages.plugin_instantiateClassError, getId(), pluginClassName);
			throwException(errorMsg, eNoConstructor);
		}

		// create a new instance
		try {
			pluginObject = (Plugin) construct.newInstance(new Object[] {this});
		} catch (ClassCastException e) {
			errorMsg = NLS.bind(Messages.plugin_notPluginClass, pluginClassName);
			throwException(errorMsg, e);
		} catch (Exception e) {
			errorMsg = NLS.bind(Messages.plugin_instantiateClassError, getId(), pluginClassName);
			throwException(errorMsg, e);
		}
	}

	public PluginDescriptor(org.osgi.framework.Bundle b) {
		bundleOsgi = b;
		if ((b.getState() & Bundle.ACTIVE) != 0)
			active = true;
	}

	public Bundle getBundle() {
		return bundleOsgi;
	}

	public void setPlugin(Plugin object) {
		pluginObject = object;
	}

	public synchronized void setActive() {
		this.active = true;
	}
	
	public boolean hasPluginObject() {
		return pluginObject!=null;
	}

	public void markAsDeactivated() {
		deactivated = true;
	}
}
