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

import java.util.*;

import org.eclipse.core.internal.dependencies.*;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;

public class IncrementalRegistryResolver {

	private MultiStatus status;

	private boolean DEBUG_RESOLVE = false;
	private static final String OPTION_DEBUG_RESOLVE = "org.eclipse.core.runtime/registry/debug/resolve"; //$NON-NLS-1$

	private IPluginEventDispatcher eventDispatcher;

public IncrementalRegistryResolver(IPluginEventDispatcher eventDispatcher) {
	String debug = Platform.getDebugOption(OPTION_DEBUG_RESOLVE);
	DEBUG_RESOLVE = debug==null ? false : ( debug.equalsIgnoreCase("true") ? true : false ); //$NON-NLS-1$
	this.eventDispatcher = eventDispatcher;
}

/**
 * Copies the extensions from a fragment to its plugin.
 */
private void addExtensions(ExtensionModel[] extensions, PluginDescriptorModel plugin) {
	// Add all the extensions (presumably from a fragment) to plugin
	int extLength = extensions.length;
	for (int i = 0; i < extLength; i++) {
		extensions[i].setParentPluginDescriptor (plugin);
	}
	ExtensionModel[] list = plugin.getDeclaredExtensions();
	int listLength = (list == null ? 0 : list.length);
	ExtensionModel[] result = null;
	if (list == null)
		result = new ExtensionModel[extLength];
	else {
		result = new ExtensionModel[list.length + extLength];
		System.arraycopy(list, 0, result, 0, list.length);
	}
	System.arraycopy(extensions, 0, result, listLength, extLength); 
	plugin.setDeclaredExtensions(result);
}
/**
 * Copies the extension points from a fragment to its plugin.
 */
private void addExtensionPoints(ExtensionPointModel[] extensionPoints, PluginDescriptorModel plugin) {
	// Add all the extension points (presumably from a fragment) to plugin
	int extPtLength = extensionPoints.length;
	for (int i = 0; i < extPtLength; i++) {
		extensionPoints[i].setParentPluginDescriptor (plugin);
	}
	ExtensionPointModel[] list = plugin.getDeclaredExtensionPoints();
	int listLength = (list == null ? 0 : list.length);
	ExtensionPointModel[] result = null;
	if (list == null)
		result = new ExtensionPointModel[extPtLength];
	else {
		result = new ExtensionPointModel[list.length + extPtLength];
		System.arraycopy(list, 0, result, 0, list.length);
	}
	System.arraycopy(extensionPoints, 0, result, listLength, extPtLength); 
	plugin.setDeclaredExtensionPoints(result);
}
/**
 * Copies the libraries from a fragment to its plugin.
 */
private void addLibraries(LibraryModel[] libraries, PluginDescriptorModel plugin) {
	// Add all the libraries (presumably from a fragment) to plugin
	int libLength = libraries.length;
	LibraryModel[] list = plugin.getRuntime();
	LibraryModel[] result = null;
	int listLength = (list == null ? 0 : list.length);
	if (list == null)
		result = new LibraryModel[libLength];
	else {
		result = new LibraryModel[list.length + libLength];
		System.arraycopy(list, 0, result, 0, list.length);
	}
	System.arraycopy(libraries, 0, result, listLength, libLength); 
	plugin.setRuntime(result);
}
/**
 * Copies the pre-requisites from a fragment to its plugin.
 */
private void addPrerequisites(PluginPrerequisiteModel[] prerequisites, PluginDescriptorModel plugin) {
	// Add all the prerequisites (presumably from a fragment) to plugin
	int reqLength = prerequisites.length;
	PluginPrerequisiteModel[] list = plugin.getRequires();
	PluginPrerequisiteModel[] result = null;
	int listLength = (list == null ? 0 : list.length);
	if (list == null)
		result = new PluginPrerequisiteModel[reqLength];
	else {
		result = new PluginPrerequisiteModel[list.length + reqLength];
		System.arraycopy(list, 0, result, 0, list.length);
	}
	System.arraycopy(prerequisites, 0, result, listLength, reqLength); 
	plugin.setRequires(result);
}
private void debug(String s) {
	System.out.println("Registry Resolve: "+s); //$NON-NLS-1$
}
private void error(String message) {
	Status error = new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, message, null);
	status.add(error);
	if (InternalPlatform.DEBUG && DEBUG_RESOLVE)
		System.out.println(error.toString());
}
private void information(String message) {
	if (InternalPlatform.DEBUG && DEBUG_RESOLVE)
		System.out.println(message);
}
private PluginVersionIdentifier getVersionIdentifier(PluginModel model) {	
	if (PluginVersionIdentifier.validateVersion(model.getVersion()).getSeverity() != IStatus.OK)
		return new PluginVersionIdentifier("0.0.0"); //$NON-NLS-1$
	return new PluginVersionIdentifier(model.getVersion());
}
private boolean fragmentHasPrerequisites (PluginMap idmap,PluginFragmentModel fragment) {
	PluginPrerequisiteModel[] requires = fragment.getRequires();
	if (requires == null || requires.length == 0)
		return true;
	for (int i = 0; i < requires.length; i++) {
		// Use the idmap to determine if a plugin exists.  We know
		// that all plugins in this registry already have an entry
		// in the idmap.  If the right idmap entry doesn't exist,
		// this plugin is not in the registry.
		if (idmap.getAny(requires[i].getPlugin()) == null) {
			// We know this plugin doesn't exist
			error(Policy.bind("parse.badPrereqOnFrag", fragment.getName(), requires[i].getPlugin())); //$NON-NLS-1$
			return false;
		}
	}
	return true;
}
private void linkFragments(PluginMap idmap,PluginFragmentModel[] fragments) {
	/* For each fragment, find out which plugin descriptor it belongs
	 * to and add it to the list of fragments in this plugin.
	 */
	for (int i = 0; i < fragments.length; i++) {
		PluginFragmentModel fragment = fragments[i];
		if (!requiredFragment(fragment)) {
			// There is a required field missing on this fragment, so 
			// ignore it.
			String id, name;
			if ((id = fragment.getId()) != null)
				error (Policy.bind("parse.fragmentMissingAttr", id)); //$NON-NLS-1$
			else if ((name = fragment.getName()) != null)
				error (Policy.bind("parse.fragmentMissingAttr", name)); //$NON-NLS-1$
			else
				error (Policy.bind("parse.fragmentMissingIdName")); //$NON-NLS-1$
			continue;
		}
		if (!fragmentHasPrerequisites(idmap,fragment)) {
			// This fragment requires a plugin that does not 
			// exist.  Ignore the fragment.
			continue;
		}
		
		// Now find a plugin that fits the matching criteria specified for this fragment and
		// its related plugin
		PluginDescriptorModel plugin = null;
		List verList = idmap.getVersions(fragment.getPluginId());
		byte matchType = fragment.getMatch();
		if (verList != null) {
			for (Iterator list = verList.iterator(); list.hasNext() && plugin == null;) {
				PluginDescriptorModel pd = (PluginDescriptorModel) list.next();
				if (pd.getEnabled()) {
					// return the highest version that fits the matching criteria
					switch (matchType) {
						case PluginFragmentModel.FRAGMENT_MATCH_PERFECT:
							if (getVersionIdentifier(pd).isPerfect(new PluginVersionIdentifier(fragment.getPluginVersion())))
								plugin = pd;
							break;
						case PluginFragmentModel.FRAGMENT_MATCH_EQUIVALENT:
							if (getVersionIdentifier(pd).isEquivalentTo(new PluginVersionIdentifier(fragment.getPluginVersion())))
								plugin = pd;
							break;
						case PluginFragmentModel.FRAGMENT_MATCH_COMPATIBLE:
						case PluginFragmentModel.FRAGMENT_MATCH_UNSPECIFIED:
							if (getVersionIdentifier(pd).isCompatibleWith(new PluginVersionIdentifier(fragment.getPluginVersion())))
								plugin = pd;
							break;
						case PluginFragmentModel.FRAGMENT_MATCH_GREATER_OR_EQUAL:
							if (getVersionIdentifier(pd).isGreaterOrEqualTo(new PluginVersionIdentifier(fragment.getPluginVersion())))
								plugin = pd;
							break;
					}
				}
			}
		}

		if (plugin == null) {
			// We couldn't find this fragment's plugin
			error (Policy.bind("parse.missingFragmentPd", fragment.getPluginId(), fragment.getId())); //$NON-NLS-1$
			continue;
		}
		
		// Add this fragment to the list of fragments for this plugin descriptor
		PluginFragmentModel[] list = plugin.getFragments();
		PluginFragmentModel[] newList;
		if (list == null) {
			newList = new PluginFragmentModel[1];
			newList[0] = fragment;
		} else {
			newList = new PluginFragmentModel[list.length + 1];
			System.arraycopy(list, 0, newList, 0, list.length);
			newList[list.length] = fragment;
		}
		plugin.setFragments(newList);
	}
}
/**
 * @param plugins an array containing new plugins to be resolved
 * @param fragments an array containing new fragments to be merged with plugins
 * 
 * Plugins and fragments will be connected and plugins enablement 
 * status will be set.
 * 
 */
public MultiStatus resolve(PluginDescriptorModel[] newPlugins,PluginFragmentModel[] newFragments) {
	// does a resolution from scratch with no support for future incremental resolution.
	return resolve(newPlugins,newFragments,null,null,null);
}
/**
 * Input
 * Resolves an existing registry, adding new plugins and fragments.
 * The first resolution will mark some of the plugins as enabled,
 * some as disabled.
 * Fragments are always merged with their corresponding plugins (before
 * the actual resolution starts).
 * Connections between plugins and fragments may happen and plugins may have 
 * their enablement status changed.
 * 
 * @param newPlugins new plugins to be added to the registry
 * @param newFragments new fragments to be added to the registry
 * @param resolvedPlugins existing resolved plugins.  <code>null</code> 
 * if not an incremental resolution
 * @param unresolvedPlugins existing unresolved plugins. <code>null</code> 
 * if not an incremental resolution
 * @param pluginSystem a dependency system corresponding to all existing plugins
 *  (both enabled/disabled). <code>null</code> if not an incremental resolution or if 
 * no other subsequent resolutions will happen
 *   
 */
public synchronized MultiStatus resolve(PluginDescriptorModel[] newPlugins,PluginFragmentModel[] newFragments,PluginMap resolvedPlugins,PluginMap unresolvedPlugins,IDependencySystem pluginSystem) {

	// This is the entry point to the registry resolver.
	// Calling this method, with a valid registry will 
	// cause this registry to be 'resolved'.
	
	status = new MultiStatus(Platform.PI_RUNTIME, IStatus.OK, "", null); //$NON-NLS-1$	
	boolean incremental = resolvedPlugins != null;
	if (pluginSystem != null && pluginSystem.getElementCount() > 0) {
		Assert.isNotNull(resolvedPlugins);
		Assert.isNotNull(unresolvedPlugins);
		Assert.isTrue(pluginSystem.getElementCount() == (resolvedPlugins.size() + unresolvedPlugins.size())); 
	}
	
	// Start by putting each new plugin in the idmap.  We are
	// going to need this for the call to linkFragments.
	PluginMap idmap = new PluginMap(new HashMap());
	for (int i = 0; i < newPlugins.length; i++) {
		// Check to see if all the required fields exist
		if (!requiredPluginDescriptor(newPlugins[i])) {
			newPlugins[i].setEnabled(false);
			String id, name;
			if ((id = newPlugins[i].getId()) != null)
				error(Policy.bind("parse.pluginMissingAttr", id)); //$NON-NLS-1$
			else if ((name = newPlugins[i].getName()) != null)
				error(Policy.bind("parse.pluginMissingAttr", name)); //$NON-NLS-1$
			else
				error(Policy.bind("parse.pluginMissingIdName")); //$NON-NLS-1$				
		} else
			idmap.add(newPlugins[i]);
	}
	// Add all the new fragments to their associated plugin.
	// Note that this will check for all the required fields in
	// the fragment.
	linkFragments(idmap,newFragments);
	// Now we have to cycle through the plugin list again
	// to assimilate all the fragment information and 
	// check for 'required' fields.	
	for (int i = 0; i < newPlugins.length; i++) {
		// XXX: shouldn't this process just the enabled plugins?
		if (newPlugins[i].getFragments() != null) {
			// Take all the information in each fragment and
			// embed it in the plugin descriptor
			resolvePluginFragments(newPlugins[i]);
		}
		if (newPlugins[i].getEnabled())
			firePluginEvent(newPlugins[i], IPluginEvent.INSTALLED);		
	}

	// creates a new temporary dependency system if none was given 
	if (pluginSystem == null)		
		pluginSystem = RegistryDependencySystemHelper.createDependencySystem();	
	
	// add all new plugins as elements in the dependency system
	for (int i = 0; i < newPlugins.length; i++) {
		if (newPlugins[i].getEnabled())	
			pluginSystem.addElement(RegistryDependencySystemHelper.createElement(newPlugins[i], pluginSystem));		
		// disable every new plugin - only those resolved will be re-enabled later
		newPlugins[i].setEnabled(false);		
	}
	try {
		IResolutionDelta resolutionDelta = pluginSystem.resolve();
		IElementChange[] changes = resolutionDelta.getAllChanges();
		for (int i = 0; i < changes.length; i++) {
			String pluginId = (String) changes[i].getElement().getId();
			String versionId = changes[i].getElement().getVersionId().toString();
			PluginDescriptorModel pluginDescriptor;
			if (changes[i].getNewStatus() == IElementChange.RESOLVED) {
				// a plugin became resolved (it was either unresolved or unknown)
				boolean newPlugin = changes[i].getPreviousStatus() == IElementChange.UNKNOWN; 				
				if (!incremental || newPlugin) {
					pluginDescriptor = (PluginDescriptorModel) idmap.get(pluginId,versionId);
				} else {
					pluginDescriptor = (PluginDescriptorModel) unresolvedPlugins.remove(pluginId,versionId);
				}
				pluginDescriptor.setEnabled(true);
				if (incremental)
					resolvedPlugins.add(pluginDescriptor);
				// adjust its pre-requisite
				PluginPrerequisiteModel[] prereqs = pluginDescriptor.getRequires();
				if (prereqs != null)
					for (int j = 0; j < prereqs.length; j++) {
						IDependency prereq = changes[i].getElement().getDependency(prereqs[j].getPlugin());
						Object resolvedVersion = prereq.getResolvedVersionId();
						if (resolvedVersion != null) // if optional may be null
							prereqs[j].setResolvedVersion(resolvedVersion.toString());
					}										
				firePluginEvent(pluginDescriptor, IPluginEvent.RESOLVED);
				
				if (DEBUG_RESOLVE)
					debug("enabled " + pluginId + '_' + versionId); //$NON-NLS-1$
			} else if (changes[i].getPreviousStatus() == IElementChange.RESOLVED) {
				// a previously resolved plugin became unresolved
				pluginDescriptor = (PluginDescriptorModel) resolvedPlugins.remove(pluginId,versionId);				
				pluginDescriptor.setEnabled(false);
				// unresolve its pre-requisite
				PluginPrerequisiteModel[] prereqs = pluginDescriptor.getRequires();				
				if (prereqs != null)
					for (int j = 0; j < prereqs.length; j++)
						prereqs[j].setResolvedVersion(null);
				firePluginEvent(pluginDescriptor, IPluginEvent.UNRESOLVED);

				if (DEBUG_RESOLVE)
					debug("disabled " + changes[i].getElement().getId() + '_' + changes[i].getElement().getVersionId()); //$NON-NLS-1$					
				
			} else {// otherwise, transitioned from UNKNOWN to UNRESOLVED
				if (incremental) {
					pluginDescriptor = (PluginDescriptorModel) idmap.get(pluginId,versionId);				
					unresolvedPlugins.add(pluginDescriptor);
				}
			}			
		}  

	} catch (IDependencySystem.CyclicSystemException cse) {
		// cycle - no graceful handling for now
		error(Policy.bind("plugin.unableToResolve")); //$NON-NLS-1$
	}
	MultiStatus result = status;

	status = null;
	
	return result;
}
private void firePluginEvent(PluginDescriptorModel pluginDescriptor, int eventType) {
	if (eventDispatcher != null) {
		//XXX: this is wrong - we have a design issue here - registry resolver
		// should not know about IPluginDescriptors! 
		IPluginEvent event = new PluginEvent((IPluginDescriptor) pluginDescriptor,eventType);
		eventDispatcher.firePluginEvent(event);
	}
}

/**
 * Adds all fragment elements to the corresponding plugin.
 */
private void resolvePluginFragment(PluginFragmentModel fragment, PluginDescriptorModel plugin) {
	ExtensionModel[] extensions = fragment.getDeclaredExtensions();
	if (extensions != null)
		// Add all the fragment extensions to the plugin
		addExtensions(extensions, plugin);

	ExtensionPointModel[] points = fragment.getDeclaredExtensionPoints();
	if (points != null)
		// Add all the fragment extension points to the plugin
		addExtensionPoints(points, plugin);

	LibraryModel[] libraries = fragment.getRuntime();
	if (libraries != null)
		// Add all the fragment library entries to the plugin
		addLibraries(libraries, plugin);
			
	PluginPrerequisiteModel[] prerequisites = fragment.getRequires();
	if (prerequisites != null)
		// Add all the fragment prerequisites to the plugin
		addPrerequisites(prerequisites, plugin);
}
/**
 * Adds all elements from this plugin's fragments to the plugin.
 */
private void resolvePluginFragments(PluginDescriptorModel plugin) {
	/* For each fragment contained in the fragment list of this plugin, 
	 * apply all the fragment bits to the plugin (e.g. all of the fragment's
	 * extensions are added to the list of extensions in the plugin).  Be
	 * sure to use only the latest version of any given fragment (in case
	 * there are multiple versions of a given fragment id).  So note that,
	 * if there are multiple versions of a given fragment id, all but the
	 * latest version will be discarded.
	 */

	// The boolean 'dirty' will remain false if there is only one
	// version of every fragment id associated with this plugin
	boolean dirty = false;
	
	PluginFragmentModel[] fragmentList = plugin.getFragments();
	HashMap latestFragments = new HashMap(30);
	for (int i = 0; i < fragmentList.length; i++) {
		String fragmentId = fragmentList[i].getId();
		PluginFragmentModel  latestVersion = (PluginFragmentModel)latestFragments.get(fragmentId);
		if (latestVersion == null) {
			// We don't have any fragments with this id yet
			latestFragments.put(fragmentId, fragmentList[i]);
		} else {
			dirty = true;
			if (getVersionIdentifier(fragmentList[i]).equals(getVersionIdentifier(latestVersion)))
				// ignore duplicates
				error (Policy.bind("parse.duplicateFragment", fragmentId, fragmentList[i].getVersion())); //$NON-NLS-1$
			if (getVersionIdentifier(fragmentList[i]).isGreaterThan(getVersionIdentifier(latestVersion))) {
				latestFragments.put(fragmentId, fragmentList[i]);
			}
		}
	}
	
	// latestFragments now contains the latest version of each fragment
	// id for this plugin
	
	// Now add the latest version of each fragment to the plugin
	Set latestOnly = new HashSet();
	for (Iterator list = latestFragments.values().iterator(); list.hasNext();) {
		PluginFragmentModel latestFragment = (PluginFragmentModel)list.next();
		if (dirty)
			latestOnly.add(latestFragment);
		int numLibraries = latestFragment.getRuntime() == null ? 0 : latestFragment.getRuntime().length;
		resolvePluginFragment(latestFragment, plugin);
		// If this fragment added library entries, check to see if it
		// added a duplicate library entry.
		if (numLibraries != 0) {
			// Something got added
			LibraryModel[] libraries = plugin.getRuntime();
			// Put all the library names into a set as we know the set will not
			// have any duplicates.
			Set libNames = new HashSet();
			int setSize = libNames.size();
			for (int i = 0; i < libraries.length; i++) {
				libNames.add(libraries[i].getName());
				if (libNames.size() == setSize) {
					// We know this library name didn't get added to the set.
					// Ignore the duplicate but indicate an error
					String[] bindings = {latestFragment.getId(), plugin.getId(), libraries[i].getName()};
					error (Policy.bind("parse.duplicateLib", bindings)); //$NON-NLS-1$
				} else {
					setSize = libNames.size();
				}
			}
		}
	}
	// Currently the fragments on the plugin include all fragment 
	// versions.  Now strip off all but the latest version of each
	// fragment id (only if necessary).
	if (dirty)
		plugin.setFragments((PluginFragmentModel[]) latestOnly.toArray(new PluginFragmentModel[latestOnly.size()]));
	
}
/**
 * Does this plugin descriptor model have all required attributes?
 */
private boolean requiredPluginDescriptor(PluginDescriptorModel plugin) {
	boolean retValue = true;
	retValue = plugin.getName() != null &&
		plugin.getId() != null &&
		plugin.getVersion() != null;
	if (!retValue) 
		return retValue;
		
	PluginPrerequisiteModel[] requiresList = plugin.getRequires();
	ExtensionModel[] extensions = plugin.getDeclaredExtensions();
	ExtensionPointModel[] extensionPoints = plugin.getDeclaredExtensionPoints();
	LibraryModel[] libraryList = plugin.getRuntime();
	PluginFragmentModel[] fragments = plugin.getFragments();
	
	if (requiresList != null) {
		for (int i = 0; i < requiresList.length && retValue; i++) {
			retValue = retValue && requiredPrerequisite(requiresList[i]);
		}
	}
	if (extensions != null) {
		for (int i = 0; i < extensions.length && retValue; i++) {
			retValue = retValue && requiredExtension(extensions[i]);
		}
	}
	if (extensionPoints != null) {
		for (int i = 0; i < extensionPoints.length && retValue; i++) {
			retValue = retValue && requiredExtensionPoint(extensionPoints[i]);
		}
	}
	if (libraryList != null) {
		for (int i = 0; i < libraryList.length && retValue; i++) {
			retValue = retValue && requiredLibrary(libraryList[i]);
		}
	}
	if (fragments != null) {
		for (int i = 0; i < fragments.length && retValue; i++) {
			retValue = retValue && requiredFragment(fragments[i]);
		}
	}
	
	return retValue;
}
/**
 * Does this plugin prerequisite model have all required attributes?
 */
private boolean requiredPrerequisite (PluginPrerequisiteModel prerequisite) {
	return ((prerequisite.getPlugin() != null));
}
/**
 * Does this extension model have all required attributes?
 */
private boolean requiredExtension (ExtensionModel extension) {
	return (extension.getExtensionPoint() != null);
}
/**
 * Does this extension point model have all required attributes?
 */
private boolean requiredExtensionPoint (ExtensionPointModel extensionPoint) {
	return ((extensionPoint.getName() != null) &&
		(extensionPoint.getId() != null));
}
/**
 * Does this library model have all required attributes?
 */
private boolean requiredLibrary (LibraryModel library) {
	return (library.getName() != null);
}
/**
 * Does this plugin fragmeent model have all required attributes?
 */
private boolean requiredFragment (PluginFragmentModel fragment) {
	return ((fragment.getName() != null) &&
		(fragment.getId() != null) &&
		(fragment.getPlugin() != null) &&
		(fragment.getPluginVersion() != null) &&
		(fragment.getVersion() != null));
}
public void trimRegistry(PluginRegistryModel reg) {
	PluginDescriptorModel[] list = reg.getPlugins();
	for (int i = 0; i < list.length; i++) {
		PluginDescriptorModel pd = (PluginDescriptorModel) list[i];
		if (!pd.getEnabled()) {
			if (DEBUG_RESOLVE)
				debug("removing " + pd.toString()); //$NON-NLS-1$
			reg.removePlugin(pd.getId(), pd.getVersion());
		}
	}
}
}
