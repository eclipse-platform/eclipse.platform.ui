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
package org.eclipse.core.internal.registry;

import java.lang.reflect.Array;
import java.util.*;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

/**
 * An OSGi-free implementation for the extension registry API.
 */
public class ExtensionRegistry extends NestedRegistryModelObject implements IExtensionRegistry {

	private final static class ExtensionEventDispatcherJob extends Job {
		// an "identy rule" that forces extension events to be queued		
		private final static ISchedulingRule EXTENSION_EVENT_RULE = new ISchedulingRule() {
			public boolean contains(ISchedulingRule rule) {
				return rule == this;
			}

			public boolean isConflicting(ISchedulingRule rule) {
				return rule == this;
			}
		};
		private Map deltas;
		private Object[] listenerInfos;

		public ExtensionEventDispatcherJob(Object[] listenerInfos, Map deltas) {
			super("RegistryChangeEventDispatcherJob"); //$NON-NLS-1$
			this.listenerInfos = listenerInfos;
			this.deltas = deltas;
			// all extension event dispatching jobs use this rule
			setRule(EXTENSION_EVENT_RULE);
		}

		public IStatus run(IProgressMonitor monitor) {
			MultiStatus result = new MultiStatus(Platform.PI_RUNTIME, IStatus.OK, Policy.bind("plugin.eventListenerError"), null); //$NON-NLS-1$			
			for (int i = 0; i < listenerInfos.length; i++) {
				ListenerInfo listenerInfo = (ListenerInfo) listenerInfos[i];
				if (listenerInfo.filter != null && !deltas.containsKey(listenerInfo.filter))
					continue;
				try {
					listenerInfo.listener.registryChanged(new RegistryChangeEvent(deltas, listenerInfo.filter));
				} catch (RuntimeException re) {
					String message = re.getMessage() == null ? "" : re.getMessage(); //$NON-NLS-1$
					result.add(new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.OK, message, re));
				}
			}
			return result;
		}
	}

	class ListenerInfo {
		String filter;
		IRegistryChangeListener listener;

		public ListenerInfo(IRegistryChangeListener listener, String filter) {
			this.listener = listener;
			this.filter = filter;
		}

		/**
		 * Used by ListenerList to ensure uniqueness.
		 */
		public boolean equals(Object another) {
			return another instanceof ListenerInfo && ((ListenerInfo) another).listener == this.listener;
		}
	}

	public static boolean DEBUG;

	private static final String OPTION_DEBUG_EVENTS_EXTENSION = "org.eclipse.core.runtime/registry/debug/events/extension"; //$NON-NLS-1$	

	// used to enforce concurrent access policy for readers/writers
	private ReadWriteMonitor access = new ReadWriteMonitor();
	// key: host name, value: set of fragment names
	private Map allFragmentNames = new HashMap(11);
	// deltas not broadcasted yet
	private transient Map deltas = new HashMap(11);
	// an id->element mapping
	private Map elements = new HashMap(11);
	private transient boolean isDirty = false;
	// all registry change listeners
	private transient ListenerList listeners = new ListenerList();
	// extensions without extension point
	private Map orphanExtensions = new HashMap(11);
	private transient RegistryCacheReader reader = null;

	public ExtensionRegistry() {
		String debugOption = InternalPlatform.getDefault().getOption(OPTION_DEBUG_EVENTS_EXTENSION);
		DEBUG = debugOption == null ? false : debugOption.equalsIgnoreCase("true"); //$NON-NLS-1$		
		if (DEBUG)
			addRegistryChangeListener(new IRegistryChangeListener() {
				public void registryChanged(IRegistryChangeEvent event) {
					System.out.println(event);
				}
			});
	}

	/**
	 * Adds and resolves all extensions and extension points provided by the
	 * plug-in.
	 * <p>
	 * A corresponding IRegistryChangeEvent will be broadcast to all listeners
	 * interested on changes in the given plug-in.
	 * </p>
	 */
	public void add(Namespace element) {
		access.enterWrite();
		try {
			isDirty = true;
			basicAdd(element, true);
			fireRegistryChangeEvent();
		} finally {
			access.exitWrite();
		}
	}

	public void add(Namespace[] elements) {
		access.enterWrite();
		try {
			isDirty = true;
			for (int i = 0; i < elements.length; i++)
				basicAdd(elements[i], true);
			fireRegistryChangeEvent();
		} finally {
			access.exitWrite();
		}
	}

	/* Utility method to help with array concatenations */
	private Object addArrays(Object a, Object b) {
		Object[] result = (Object[]) Array.newInstance(a.getClass().getComponentType(), Array.getLength(a) + Array.getLength(b));
		System.arraycopy(a, 0, result, 0, Array.getLength(a));
		System.arraycopy(b, 0, result, Array.getLength(a), Array.getLength(b));
		return result;
	}

	private void addExtension(IExtension extension) {
		IExtensionPoint extPoint = basicGetExtensionPoint(extension.getExtensionPointUniqueIdentifier());
		//orphan extension
		if (extPoint == null) {
			// are there any other orphan extensions
			IExtension[] existingOrphanExtensions = (IExtension[]) orphanExtensions.get(extension.getExtensionPointUniqueIdentifier());
			if (existingOrphanExtensions != null) {
				// just add
				IExtension[] newOrphanExtensions = new IExtension[existingOrphanExtensions.length + 1];
				System.arraycopy(existingOrphanExtensions, 0, newOrphanExtensions, 0, existingOrphanExtensions.length);
				newOrphanExtensions[newOrphanExtensions.length - 1] = extension;
				orphanExtensions.put(extension.getExtensionPointUniqueIdentifier(), newOrphanExtensions);
			} else
				// otherwise this is the first one
				orphanExtensions.put(extension.getExtensionPointUniqueIdentifier(), new IExtension[] {extension});
			return;
		}
		// otherwise, link them
		IExtension[] newExtensions;
		IExtension[] existingExtensions = extPoint.getExtensions();
		if (existingExtensions.length == 0)
			newExtensions = new IExtension[] {extension};
		else {
			newExtensions = new IExtension[existingExtensions.length + 1];
			System.arraycopy(existingExtensions, 0, newExtensions, 0, existingExtensions.length);
			newExtensions[newExtensions.length - 1] = extension;
		}
		link(extPoint, newExtensions);
		recordChange(extPoint, extension, IExtensionDelta.ADDED);
	}

	/**
	 * Looks for existing orphan extensions to connect to the given extension
	 * point. If none is found, there is nothing to do. Otherwise, link them.
	 */
	private void addExtensionPoint(IExtensionPoint extPoint) {
		IExtension[] orphans = (IExtension[]) orphanExtensions.remove(extPoint.getUniqueIdentifier());
		if (orphans == null)
			return;
		// otherwise, link them
		IExtension[] newExtensions;
		IExtension[] existingExtensions = extPoint.getExtensions();
		if (existingExtensions.length == 0)
			newExtensions = orphans;
		else {
			newExtensions = new IExtension[existingExtensions.length + orphans.length];
			System.arraycopy(existingExtensions, 0, newExtensions, 0, existingExtensions.length);
			System.arraycopy(orphans, 0, newExtensions, existingExtensions.length, orphans.length);
		}
		link(extPoint, newExtensions);
		recordChange(extPoint, orphans, IExtensionDelta.ADDED);
	}

	private void addExtensionsAndExtensionPoints(Namespace element) {
		// now add and resolve extensions and extension points
		IExtensionPoint[] extPoints = element.getExtensionPoints();
		for (int i = 0; i < extPoints.length; i++)
			this.addExtensionPoint(extPoints[i]);
		IExtension[] extensions = element.getExtensions();
		for (int i = 0; i < extensions.length; i++)
			this.addExtension(extensions[i]);
	}

	/*
	 * Creates an association between a fragment and a master element.
	 */
	private void addFragmentTo(String fragmentName, String masterName) {
		Set fragmentNames = (Set) this.allFragmentNames.get(masterName);
		if (fragmentNames == null)
			allFragmentNames.put(masterName, fragmentNames = new HashSet());
		fragmentNames.add(fragmentName);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#addRegistryChangeListener(org.eclipse.core.runtime.IRegistryChangeListener)
	 */
	public void addRegistryChangeListener(IRegistryChangeListener listener) {
		// this is just a convenience API - no need to do any sync'ing here		
		addRegistryChangeListener(listener, null);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#addRegistryChangeListener(org.eclipse.core.runtime.IRegistryChangeListener, java.lang.String)
	 */
	public void addRegistryChangeListener(IRegistryChangeListener listener, String filter) {
		synchronized (listeners) {
			listeners.add(new ListenerInfo(listener, filter));
		}
	}

	void basicAdd(Namespace element, boolean link) {
		// ignore anonymous namespaces
		if (element.getUniqueIdentifier() == null)
			return;
		if (elements.containsKey(element.getUniqueIdentifier()))
			// this could be caused by a bug on removal
			throw new IllegalArgumentException("Element already added: " + element.getUniqueIdentifier()); //$NON-NLS-1$
		elements.put(element.getUniqueIdentifier(), element);
		element.setParent(this);
		if (!link)
			return;
		if (element.isFragment()) {
			addFragmentTo(element.getUniqueIdentifier(), element.getHostIdentifier());
			// if the master is not present yet, don't add anything
			if (!elements.containsKey(element.getHostIdentifier()))
				return;
		} else {
			Collection fragmentNames = getFragmentNames(element.getUniqueIdentifier());
			for (Iterator iter = fragmentNames.iterator(); iter.hasNext();) {
				Namespace fragment = (Namespace) elements.get(iter.next());
				addExtensionsAndExtensionPoints(fragment);
			}
		}
		addExtensionsAndExtensionPoints(element);
	}

	private IExtensionPoint basicGetExtensionPoint(String xptUniqueId) {
		int lastdot = xptUniqueId.lastIndexOf('.');
		if (lastdot == -1)
			return null;
		return basicGetExtensionPoint(xptUniqueId.substring(0, lastdot), xptUniqueId.substring(lastdot + 1));
	}

	private IExtensionPoint basicGetExtensionPoint(String elementName, String xpt) {
		Namespace element = (Namespace) elements.get(elementName);
		if (element == null)
			return null;
		IExtensionPoint extPoint = element.getExtensionPoint(xpt);
		if (extPoint != null)
			return extPoint;
		// could not find it... try orphan fragments
		Collection fragmentNames = getFragmentNames(elementName);
		for (Iterator iter = fragmentNames.iterator(); iter.hasNext();) {
			extPoint = ((Namespace) elements.get(iter.next())).getExtensionPoint(xpt);
			if (extPoint != null)
				return extPoint;
		}
		return null;
	}

	private IExtensionPoint[] basicGetExtensionPoints() {
		ArrayList extensionPoints = new ArrayList();
		for (Iterator iter = elements.values().iterator(); iter.hasNext();) {
			Namespace model = (Namespace) iter.next();
			IExtensionPoint[] toAdd = model.getExtensionPoints();
			for (int i = 0; i < toAdd.length; i++)
				extensionPoints.add(toAdd[i]);
		}
		return (IExtensionPoint[]) extensionPoints.toArray(new IExtensionPoint[extensionPoints.size()]);
	}

	private IExtensionPoint[] basicGetExtensionPoints(String elementName) {
		Namespace element = (Namespace) elements.get(elementName);
		if (element == null)
			return new IExtensionPoint[0];
		Collection fragmentNames = getFragmentNames(elementName);
		IExtensionPoint[] allExtensionPoints = element.getExtensionPoints();
		for (Iterator iter = fragmentNames.iterator(); iter.hasNext();) {
			Namespace fragment = (Namespace) elements.get(iter.next());
			allExtensionPoints = (IExtensionPoint[]) addArrays(allExtensionPoints, fragment.getExtensionPoints());
		}
		return allExtensionPoints;
	}

	private IExtension[] basicGetExtensions(String elementName) {
		Namespace element = (Namespace) elements.get(elementName);
		if (element == null)
			return new IExtension[0];
		Collection fragmentNames = getFragmentNames(elementName);
		IExtension[] allExtensions = element.getExtensions();
		for (Iterator iter = fragmentNames.iterator(); iter.hasNext();) {
			Namespace fragment = (Namespace) elements.get(iter.next());
			allExtensions = (IExtension[]) addArrays(allExtensions, fragment.getExtensions());
		}
		return allExtensions;
	}

	Namespace basicGetNamespace(String elementId) {
		return (Namespace) elements.get(elementId);
	}

	String[] basicGetNamespaces() {
		return (String[]) elements.keySet().toArray(new String[elements.size()]);
	}

	private boolean basicRemove(String elementName, long bundleId) {
		// ignore anonymous bundles
		if (elementName == null)
			return false;
		Namespace element = (Namespace) elements.get(elementName);
		if (element == null) {
			if (DEBUG)
				System.out.println("********* Element unknown: " + elementName + " - not removed."); //$NON-NLS-1$//$NON-NLS-2$
			return false;
		}
		if (element.getId() != bundleId)
			return false;

		isDirty = true;
		if (element.isFragment()) {
			// if the master is not present yet, bail out
			if (!elements.containsKey(element.getHostIdentifier())) {
				removeFragmentFrom(elementName, element.getHostIdentifier());
				elements.remove(elementName);
				return true;
			}
		} else {
			Collection fragmentNames = getFragmentNames(element.getUniqueIdentifier());
			for (Iterator iter = fragmentNames.iterator(); iter.hasNext();) {
				Namespace fragment = (Namespace) elements.get(iter.next());
				removeExtensionsAndExtensionPoints(fragment);
			}
		}
		removeExtensionsAndExtensionPoints(element);
		// remove link between master and fragment
		removeFragmentFrom(elementName, element.getHostIdentifier());

		// remove it in the end
		elements.remove(elementName);
		// ensure we free the removed namespace from the registry
		element.setParent(null);
		return true;
	}

	// allow other objects in the registry to use the same lock
	void enterRead() {
		access.enterRead();
	}

	// allow other objects in the registry to use the same lock	
	void exitRead() {
		access.exitRead();
	}

	/**
	 * Broadcasts (asynchronously) the event to all interested parties.
	 */
	private void fireRegistryChangeEvent() {
		// if there is nothing to say, just bail out
		if (deltas.isEmpty() || listeners.isEmpty())
			return;
		// for thread safety, create tmp collections
		Object[] tmpListeners = listeners.getListeners();
		Map tmpDeltas = new HashMap(this.deltas);
		// the deltas have been saved for notification - we can clear them now
		deltas.clear();
		// do the notification asynchronously
		new ExtensionEventDispatcherJob(tmpListeners, tmpDeltas).schedule();
	}

	RegistryCacheReader getCacheReader() {
		return reader;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#getConfigurationElementsFor(java.lang.String)
	 */
	public IConfigurationElement[] getConfigurationElementsFor(String extensionPointId) {
		// this is just a convenience API - no need to do any sync'ing here		
		int lastdot = extensionPointId.lastIndexOf('.');
		if (lastdot == -1)
			return new IConfigurationElement[0];
		return getConfigurationElementsFor(extensionPointId.substring(0, lastdot), extensionPointId.substring(lastdot + 1));
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#getConfigurationElementsFor(java.lang.String, java.lang.String)
	 */
	public IConfigurationElement[] getConfigurationElementsFor(String pluginId, String extensionPointSimpleId) {
		// this is just a convenience API - no need to do any sync'ing here
		IExtensionPoint extPoint = this.getExtensionPoint(pluginId, extensionPointSimpleId);
		if (extPoint == null)
			return new IConfigurationElement[0];
		return extPoint.getConfigurationElements();
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#getConfigurationElementsFor(java.lang.String, java.lang.String, java.lang.String)
	 */
	public IConfigurationElement[] getConfigurationElementsFor(String pluginId, String extensionPointName, String extensionId) {
		// this is just a convenience API - no need to do any sync'ing here		
		IExtension extension = this.getExtension(pluginId, extensionPointName, extensionId);
		if (extension == null)
			return new IConfigurationElement[0];
		return extension.getConfigurationElements();
	}

	private RegistryDelta getDelta(String elementName) {
		// is there a delta for the plug-in?
		RegistryDelta existingDelta = (RegistryDelta) deltas.get(elementName);
		if (existingDelta != null)
			return existingDelta;

		//if not, create one
		RegistryDelta delta = new RegistryDelta(elementName);
		deltas.put(elementName, delta);
		return delta;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#getExtension(java.lang.String)
	 */
	public IExtension getExtension(String extensionId) {
		int lastdot = extensionId.lastIndexOf('.');
		if (lastdot == -1)
			return null;
		String namespace = extensionId.substring(0, lastdot);
		// sync'ing while retrieving namespace is enough 
		Namespace element = getNamespace(namespace);
		return element.getExtension(extensionId.substring(lastdot + 1));
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#getExtension(java.lang.String, java.lang.String)
	 */
	public IExtension getExtension(String extensionPointId, String extensionId) {
		// this is just a convenience API - no need to do any sync'ing here		
		int lastdot = extensionPointId.lastIndexOf('.');
		if (lastdot == -1)
			return null;
		return getExtension(extensionPointId.substring(0, lastdot), extensionPointId.substring(lastdot + 1), extensionId);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#getExtension(java.lang.String, java.lang.String, java.lang.String)
	 */
	public IExtension getExtension(String pluginId, String extensionPointName, String extensionId) {
		// this is just a convenience API - no need to do any sync'ing here		
		IExtensionPoint extPoint = getExtensionPoint(pluginId, extensionPointName);
		if (extPoint != null)
			return extPoint.getExtension(extensionId);
		return null;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#getExtensionPoint(java.lang.String)
	 */
	public IExtensionPoint getExtensionPoint(String xptUniqueId) {
		// this is just a convenience API - no need to do any sync'ing here
		int lastdot = xptUniqueId.lastIndexOf('.');
		if (lastdot == -1)
			return null;
		return getExtensionPoint(xptUniqueId.substring(0, lastdot), xptUniqueId.substring(lastdot + 1));
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#getExtensionPoint(java.lang.String, java.lang.String)
	 */
	public IExtensionPoint getExtensionPoint(String elementName, String xpt) {
		access.enterRead();
		try {
			return basicGetExtensionPoint(elementName, xpt);
		} finally {
			access.exitRead();
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#getExtensionPoints()
	 */
	public IExtensionPoint[] getExtensionPoints() {
		access.enterRead();
		try {
			return basicGetExtensionPoints();
		} finally {
			access.exitRead();
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#getExtensionPoints(java.lang.String)
	 */
	public IExtensionPoint[] getExtensionPoints(String elementName) {
		access.enterRead();
		try {
			return basicGetExtensionPoints(elementName);
		} finally {
			access.exitRead();
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#getExtensions(java.lang.String)
	 */
	public IExtension[] getExtensions(String elementName) {
		access.enterRead();
		try {
			return basicGetExtensions(elementName);
		} finally {
			access.exitRead();
		}
	}

	/*
	 * Returns a collection of fragments for a master element.
	 */
	private Collection getFragmentNames(String masterName) {
		Collection fragmentNames = (Collection) allFragmentNames.get(masterName);
		return fragmentNames == null ? Collections.EMPTY_SET : fragmentNames;
	}

	/* public to allow access from tests */
	public Namespace getNamespace(String elementId) {
		access.enterRead();
		try {
			return basicGetNamespace(elementId);
		} finally {
			access.exitRead();
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#getNamespaces()
	 */
	public String[] getNamespaces() {
		access.enterRead();
		try {
			return basicGetNamespaces();
		} finally {
			access.exitRead();
		}
	}

	ExtensionRegistry getRegistry() {
		return this;
	}

	public boolean isDirty() {
		return isDirty;
	}

	private void link(IExtensionPoint extPoint, IExtension[] extensions) {
		ExtensionPoint xpm = (ExtensionPoint) extPoint;
		if (extensions == null || extensions.length == 0) {
			xpm.setExtensions(null);
			return;
		}
		xpm.setExtensions(extensions);
	}

	/*
	 * Records an extension addition/removal.
	 */
	private void recordChange(IExtensionPoint extPoint, IExtension extension, int kind) {
		// avoid computing deltas when there are no listeners
		if (listeners.isEmpty())
			return;
		ExtensionDelta extensionDelta = new ExtensionDelta();
		extensionDelta.setExtension(extension);
		extensionDelta.setExtensionPoint(extPoint);
		extensionDelta.setKind(kind);
		getDelta(extPoint.getNamespace()).addExtensionDelta(extensionDelta);
	}

	/*
	 * Records a set of extension additions/removals.
	 */
	private void recordChange(IExtensionPoint extPoint, IExtension[] extensions, int kind) {
		if (listeners.isEmpty())
			return;
		if (extensions.length == 0)
			return;
		RegistryDelta pluginDelta = getDelta(extPoint.getNamespace());
		for (int i = 0; i < extensions.length; i++) {
			ExtensionDelta extensionDelta = new ExtensionDelta();
			extensionDelta.setExtension(extensions[i]);
			extensionDelta.setExtensionPoint(extPoint);
			extensionDelta.setKind(kind);
			pluginDelta.addExtensionDelta(extensionDelta);
		}
	}

	/**
	 * Unresolves and removes all extensions and extension points provided by
	 * the plug-in.
	 * <p>
	 * A corresponding IRegistryChangeEvent will be broadcast to all listeners
	 * interested on changes in the given plug-in.
	 * </p>
	 */
	public boolean remove(String elementName, long bundleId) {
		access.enterWrite();
		try {
			if (!basicRemove(elementName, bundleId))
				return false;
			fireRegistryChangeEvent();
			return true;
		} finally {
			access.exitWrite();
		}
	}

	private void removeExtension(IExtension extension) {
		IExtensionPoint extPoint = basicGetExtensionPoint(extension.getExtensionPointUniqueIdentifier());
		if (extPoint == null) {
			// not found - maybe it was an orphan extension
			IExtension[] existingOrphanExtensions = (IExtension[]) orphanExtensions.get(extension.getExtensionPointUniqueIdentifier());
			if (existingOrphanExtensions == null)
				// nope, this extension is unknown
				return;
			// yes, so just remove it from the orphans list
			IExtension[] newOrphanExtensions = new IExtension[existingOrphanExtensions.length - 1];
			for (int i = 0, j = 0; i < existingOrphanExtensions.length; i++)
				if (extension != existingOrphanExtensions[i])
					newOrphanExtensions[j++] = existingOrphanExtensions[i];
			orphanExtensions.put(extension.getExtensionPointUniqueIdentifier(), newOrphanExtensions);
			return;
		}
		// otherwise, unlink the extension from the extension point
		IExtension[] existingExtensions = extPoint.getExtensions();
		IExtension[] newExtensions = null;
		if (existingExtensions.length >= 1) {
			newExtensions = new IExtension[existingExtensions.length - 1];
			for (int i = 0, j = 0; i < existingExtensions.length; i++)
				if (existingExtensions[i] != extension)
					newExtensions[j++] = existingExtensions[i];
		}
		link(extPoint, newExtensions);
		recordChange(extPoint, extension, IExtensionDelta.REMOVED);
	}

	private void removeExtensionPoint(IExtensionPoint extPoint) {
		IExtension[] existingExtensions = extPoint.getExtensions();
		if (existingExtensions.length == 0)
			return;
		orphanExtensions.put(extPoint.getUniqueIdentifier(), existingExtensions);
		link(extPoint, null);
		recordChange(extPoint, existingExtensions, IExtensionDelta.REMOVED);
	}

	private void removeExtensionsAndExtensionPoints(Namespace element) {
		// remove extensions
		IExtension[] extensions = element.getExtensions();
		for (int i = 0; i < extensions.length; i++)
			this.removeExtension(extensions[i]);
		// remove extension points
		IExtensionPoint[] extPoints = element.getExtensionPoints();
		for (int i = 0; i < extPoints.length; i++)
			this.removeExtensionPoint(extPoints[i]);
	}

	/*
	 * Removes an association between a fragment and a master element.
	 */
	private void removeFragmentFrom(String fragmentName, String masterName) {
		Set fragmentNames = (Set) this.allFragmentNames.get(masterName);
		if (fragmentNames == null)
			return;
		fragmentNames.remove(fragmentName);
		if (fragmentNames.isEmpty())
			allFragmentNames.remove(masterName);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#removeRegistryChangeListener(org.eclipse.core.runtime.IRegistryChangeListener)
	 */
	public void removeRegistryChangeListener(IRegistryChangeListener listener) {
		synchronized (listeners) {
			listeners.remove(new ListenerInfo(listener, null));
		}
	}

	void setCacheReader(RegistryCacheReader value) {
		reader = value;
	}

	void setDirty(boolean value) {
		isDirty = value;
	}
}