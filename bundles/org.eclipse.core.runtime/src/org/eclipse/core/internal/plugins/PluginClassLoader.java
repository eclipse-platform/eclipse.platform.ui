/*******************************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/

package org.eclipse.core.internal.plugins;

import org.eclipse.core.runtime.*;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.boot.*;
import org.eclipse.core.internal.runtime.Policy;
import java.io.File;
import java.util.*;
import java.net.URL;

/**
 * Plugin class loader.
 *
 * Handle loading of classes from a plugin. Configures load path based
 * on the plugin <runtime> specification. Configures delegate loaders
 * based on the <requires> specification. Configures
 * the parent loader to be the default instance of 
 * PlatformClassLoader (assumes to be initialized by this point).
 *
 */

public final class PluginClassLoader extends DelegatingURLClassLoader {
	private PluginDescriptor descriptor;
	private boolean pluginActivationInProgress = false;
	
	private static ThreadLocal pluginsToActivate= new ThreadLocal();
	
public PluginClassLoader(URL[] codePath, URLContentFilter[] codeFilters, URL[] resourcePath, URLContentFilter[] resourceFilters, ClassLoader parent, PluginDescriptor descriptor) {
	// create a class loader with the given classpath and filters.  Also, the parent
	// should be the parent of the platform class loader.  This allows us to decouple standard
	// parent loading from platform loading.
	super(codePath, codeFilters, resourcePath, resourceFilters, parent);
	this.descriptor = descriptor;
	base = descriptor.getInstallURL();
	debugConstruction(); // must have initialized loader

	//	Note: initializeImportedLoaders() is called by PluginDescriptor.getPluginClassLoader().
	//	The split between construction and initialization is needed
	//	to correctly handle the case where the user defined loops in 
	//	the prerequisite definitions.
}
protected void activatePlugin(String name) {
	try {
		// pluginActivationInProgress = true;
		// the in-progress flag is set when we detect that activation will be required.
		// be sure to unset it here.
		if (DEBUG && DEBUG_SHOW_ACTIVATE && debugLoader())
			debug("Attempting to activate " + descriptor.getUniqueIdentifier());
		descriptor.doPluginActivation();
	} catch (CoreException e) {
		if (DEBUG && DEBUG_SHOW_ACTIVATE && debugLoader())
			debug("Activation failed for " + descriptor.getUniqueIdentifier());
		throw new DelegatingLoaderException("Plugin " + descriptor.getUniqueIdentifier() + " activation failed while loading class " + name, e);
	} finally {
		if (DEBUG && DEBUG_SHOW_ACTIVATE && debugLoader())
			debug("Exit activation for " + descriptor.getUniqueIdentifier());
		pluginActivationInProgress = false;
	}
}
public String debugId() {
	return descriptor.toString();
}
/**
 * Finds and loads the class with the specified name from the URL search
 * path. Any URLs referring to JAR files are loaded and opened as needed
 * until the class is found.   Search on the parent chain and then self.
 *
 * Subclasses should implement this method.
 *
 * @param name the name of the class
 * @param resolve whether or not to resolve the class if found
 * @param requestor class loader originating the request
 * @param checkParents whether the parent of this loader should be consulted
 * @return the resulting class
 */
protected Class findClassParentsSelf(final String name, boolean resolve, DelegatingURLClassLoader requestor, boolean checkParents) {
	Class result = null;
	synchronized (this) {
		// check the cache.  If we find something, check to see if its visible.
		// If it is, return it.  If not, return null if we are not checking parents.  There is
		// no point in looking in self as the class was already in the cache.
		result = findLoadedClass(name);
		if (result != null) {
			result = checkClassVisibility(result, requestor, true);
			if (result != null || !checkParents)
				return result;
		}

		// if it wasn't in the cache or was not visible, check the parents (if requested)
		if (checkParents) {
			result = findClassParents(name, resolve);
			if (result != null)
				return result;
		}

		// if activation is not going to be required, try the load here.  This is
		// a short circuit so we don't fall through to the other sync block and do
		// more work.  Note that the order of the tests is important, since 
		//descriptor.isPluginActivated() blocks while activation in progress,
		//thus creating a potential deadlock situation.
		if (pluginActivationInProgress || descriptor.isPluginActivated()) {
			try {
				result = super.findClass(name);
			} catch (ClassNotFoundException e) {
				return null;
			}
			// If the class is loaded in this classloader register it with
			// the hot swap support.  Need to do this regardless of visibility
			// because the class was actually loaded.
			enableHotSwap(this, result);

			return checkClassVisibility(result, requestor, false);
		}
		// Check to see if we would find the class if we looked.  If so,
		// activation is required.  If not, don't bother, just return null
		if (shouldLookForClass(name))
			// leave a dropping to discourage others from trying to do activation.
			// This flag will be cleared once activation is complete.
			pluginActivationInProgress = true;
		else
			return null;
	}

	// If we will find the class and the plugin is not yet activated, go ahead and do it now.
	// Note that this MUST be done outside the sync block to avoid deadlock if
	// plugin activaion forks threads etc.
	List plugins= (List)pluginsToActivate.get();
	boolean shouldActivate= false;
	if (plugins == null) {
		plugins= new ArrayList(5);
		pluginsToActivate.set(plugins);
		shouldActivate= true;
	}	
	plugins.add(0, name);
		
	// By now the plugin is activated and we need to sycn and retry the
	// class load.
	synchronized (this) {
		result = findLoadedClass(name);
		if (result != null) {
			result= checkClassVisibility(result, requestor, true);
		} else {
	
			// do search/load in this class loader
			try {
				result = super.findClass(name);
				// If the class is loaded in this classloader register it with
				// the hot swap support.  Need to do this regardless of visibility
				// because the class was actually loaded.
				if (result != null) {
					enableHotSwap(this, result);
					result= checkClassVisibility(result, requestor, false);
				}
			} catch (ClassNotFoundException e) {
				result= null;
			}
		}
	}
	
	if (shouldActivate) {
		pluginsToActivate.set(null);
		for(int i= 0, length= plugins.size(); i < length; i++) {
			activatePlugin((String)plugins.get(i));
		}
	}

	return result;
}
public PluginDescriptor getPluginDescriptor() {
	return descriptor;
}
public void initializeImportedLoaders() {
	PluginDescriptor desc = getPluginDescriptor();
	IPluginPrerequisite[] prereqs = desc.getPluginPrerequisites();
	if (prereqs.length == 0)
		return;

	PluginRegistry registry = desc.getPluginRegistry();
	ArrayList require = new ArrayList();
	for (int i = 0; i < prereqs.length; i++) {
		String prereqId = prereqs[i].getUniqueIdentifier();
		// skip over the runtime and boot plugins if they were specified.  They are automatically included
		// as the platfrom and parent respectively.
		if (!prereqId.equalsIgnoreCase(Platform.PI_RUNTIME) && !prereqId.equalsIgnoreCase(BootLoader.PI_BOOT)) {
			desc = (PluginDescriptor) registry.getPluginDescriptor(prereqId, prereqs[i].getResolvedVersionIdentifier());
			// can be null if the prereq was optional and did not exst.
			if (desc != null)
				require.add(new DelegateLoader((DelegatingURLClassLoader) desc.getPluginClassLoader(true), prereqs[i].isExported()));
		}
	}

	if (require.isEmpty())
		return;
	setImportedLoaders((DelegateLoader[]) require.toArray(new DelegateLoader[require.size()]));
}
public void setPluginDescriptor(PluginDescriptor value) {
	descriptor = value;
}
protected boolean shouldLookForClass(String name) {
	// check if requested class is in loader search path
	// Note: this check is suboptimal. It results in additional
	// loader overhead until the plugin is activated. The reason
	// the check is performed here is because
	// (1) plug-in activation needs to be performed prior to
	//     the requested class load (done in findClass(String))
	// (2) the check cannot be added to the "right" spot inside private
	//     implementation of URLClassLoader
	String resource = name.replace('.', '/');
	if (findClassResource(resource + ".class") == null)
		return false;

	// check if plugin is permanently deactivated
	if (descriptor.isPluginDeactivated()) {
		String message = Policy.bind("plugin.deactivatedLoad", name, descriptor.getUniqueIdentifier());
		throw new DelegatingLoaderException(message);
	}
	return true;
}
}