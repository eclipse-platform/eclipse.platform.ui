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
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

/**
 * An OSGi-free implementation for the extension registry API.
 */
public class ExtensionRegistry extends RegistryModelObject implements IExtensionRegistry {

	private static final String OPTION_DEBUG_EVENTS_EXTENSION = "org.eclipse.core.runtime/registry/debug/events/extension"; //$NON-NLS-1$	
	public static boolean DEBUG;

	// an id->element mapping
	private Map elements = new HashMap(11);
	// all registry change listeners
	private transient ListenerList listeners = new ListenerList(10);
	// deltas not broadcasted yet
	private transient Map deltas = new HashMap(11);
	// extensions without extension point
	private Map orphanExtensions = new HashMap(11);
	private Map orphanFragments = new HashMap(11);
	private transient IExtensionLinker linker;
	private transient RegistryCacheReader reader = null;
	private transient boolean isDirty = false;

	class ListenerInfo {
		IRegistryChangeListener listener;
		String filter;

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

	public void add(IRegistryElement[] elements) {
		synchronized (this) {
			isDirty = true;
			for (int i = 0; i < elements.length; i++)
				basicAdd(elements[i], true);
			fireRegistryChangeEvent();
		}
	}

	/**
	 * Adds and resolves all extensions and extension points provided by the
	 * plug-in.
	 * <p>
	 * A corresponding IRegistryChangeEvent will be broadcast to all listeners
	 * interested on changes in the given plug-in.
	 * </p>
	 */
	public void add(IRegistryElement element) {
		synchronized (this) {
			isDirty = true;
			basicAdd(element, true);
			fireRegistryChangeEvent();
		}
	}

	void basicAdd(IRegistryElement element, boolean link) {
		//TODO: this should be logged
		if (elements.containsKey(element.getUniqueIdentifier())) {
			if (DEBUG)
				System.out.println("********* Element already added: " + element.getUniqueIdentifier() + " - ignored."); //$NON-NLS-1$//$NON-NLS-2$
			return;
		}
		elements.put(element.getUniqueIdentifier(), element);
		((BundleModel) element).setParent(this);
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
				IRegistryElement fragment = (IRegistryElement) elements.get(iter.next());
				addExtensionsAndExtensionPoints(fragment);
			}
		}
		addExtensionsAndExtensionPoints(element);
	}

	private void addExtensionsAndExtensionPoints(IRegistryElement element) {
		// now add and resolve extensions and extension points
		IExtensionPoint[] extPoints = element.getExtensionPoints();
		for (int i = 0; i < extPoints.length; i++)
			this.addExtensionPoint(extPoints[i]);
		IExtension[] extensions = element.getExtensions();
		for (int i = 0; i < extensions.length; i++)
			this.addExtension(extensions[i]);
	}

	/* Utility method to help with array concatenations */
	private Object addArrays(Object a, Object b) {
		Object[] result = (Object[]) Array.newInstance(a.getClass().getComponentType(), Array.getLength(a) + Array.getLength(b));
		System.arraycopy(a, 0, result, 0, Array.getLength(a));
		System.arraycopy(b, 0, result, Array.getLength(a), Array.getLength(b));
		return result;
	}

	/*
	 * Creates an association between a fragment and a master element.
	 */
	private void addFragmentTo(String fragmentName, String masterName) {
		Set fragmentNames = (Set) this.orphanFragments.get(masterName);
		if (fragmentNames == null)
			orphanFragments.put(masterName, fragmentNames = new HashSet());
		fragmentNames.add(fragmentName);
	}

	/*
	 * Removes an association between a fragment and a master element.
	 */
	private void removeFragmentFrom(String fragmentName, String masterName) {
		Set fragmentNames = (Set) this.orphanFragments.get(masterName);
		if (fragmentNames == null)
			return;
		fragmentNames.remove(fragmentName);
		if (fragmentNames.isEmpty())
			orphanFragments.remove(masterName);
	}

	/*
	 * Returns a collection of fragments for a master element.
	 */
	private Collection getFragmentNames(String masterName) {
		Collection fragmentNames = (Collection) orphanFragments.get(masterName);
		return fragmentNames == null ? Collections.EMPTY_SET : fragmentNames;
	}

	private void addExtension(IExtension extension) {
		IExtensionPoint extPoint = getExtensionPoint(extension.getExtensionPointUniqueIdentifier());
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
		if (existingExtensions == null)
			newExtensions = new IExtension[] {extension};
		else {
			newExtensions = new IExtension[existingExtensions.length + 1];
			System.arraycopy(existingExtensions, 0, newExtensions, 0, existingExtensions.length);
			newExtensions[newExtensions.length - 1] = extension;
		}
		linker.link(extPoint, newExtensions);
		recordChange(extPoint, extension, IExtensionDelta.ADDED);
	}

	/**
	 * Looks for existing orphan extensions to connect to the given extension
	 * point. If none is found, there is nothing to do. Otherwise, link them.
	 */
	private void addExtensionPoint(IExtensionPoint extPoint) {
		IExtension[] existingExtensions = (IExtension[]) orphanExtensions.remove(extPoint.getUniqueIdentifier());
		if (existingExtensions == null)
			return;
		// otherwise, link them
		linker.link(extPoint, existingExtensions);
		recordChange(extPoint, existingExtensions, IExtensionDelta.ADDED);
	}

	/**
	 * Adds the given listener for registry change events on the given plug-in.
	 */
	// TODO This should be thread safe code.  May get called by multiple threads.
	public void addRegistryChangeListener(IRegistryChangeListener listener, String filter) {
		this.listeners.add(new ListenerInfo(listener, filter));
	}

	/**
	 * Adds the given listener for registry change events.
	 */
	public void addRegistryChangeListener(IRegistryChangeListener listener) {
		addRegistryChangeListener(listener, null);
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

	public String[] getElementIdentifiers() {
		return getNamespaces();
	}

	public String[] getNamespaces() {
		return (String[]) elements.keySet().toArray(new String[elements.size()]);
	}

	public IConfigurationElement[] getConfigurationElementsFor(String extensionPointId) {
		int lastdot = extensionPointId.lastIndexOf('.');
		if (lastdot == -1)
			return new IConfigurationElement[0];
		return getConfigurationElementsFor(extensionPointId.substring(0, lastdot), extensionPointId.substring(lastdot + 1));
	}

	public IConfigurationElement[] getConfigurationElementsFor(String pluginId, String extensionPointSimpleId) {
		IExtensionPoint extPoint = this.getExtensionPoint(pluginId, extensionPointSimpleId);
		if (extPoint == null)
			return new IConfigurationElement[0];
		IExtension[] extensions = extPoint.getExtensions();
		if (extensions.length == 0)
			return new IConfigurationElement[0];
		Collection result = new ArrayList();
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] toAdd = extensions[i].getConfigurationElements();
			for (int j = 0; j < toAdd.length; j++)
				result.add(toAdd[j]);
		}
		return (IConfigurationElement[]) result.toArray(new IConfigurationElement[result.size()]);
	}

	public IConfigurationElement[] getConfigurationElementsFor(String pluginId, String extensionPointName, String extensionId) {
		IExtension extension = this.getExtension(pluginId, extensionPointName, extensionId);
		if (extension == null)
			return new IConfigurationElement[0];
		return extension.getConfigurationElements();
	}

	public IExtension[] getExtensions(String elementName) {
		IRegistryElement element = (IRegistryElement) elements.get(elementName);
		if (element == null)
			return new IExtension[0];
		Collection fragmentNames = getFragmentNames(elementName);
		IExtension[] allExtensions = element.getExtensions();
		for (Iterator iter = fragmentNames.iterator(); iter.hasNext();) {
			IRegistryElement fragment = (IRegistryElement) elements.get(iter.next());
			allExtensions = (IExtension[]) addArrays(allExtensions, fragment.getExtensions());
		}
		return allExtensions;
	}

	public IExtension getExtension(String extensionPointId, String extensionId) {
		int lastdot = extensionPointId.lastIndexOf('.');
		if (lastdot == -1)
			return null;
		return getExtension(extensionPointId.substring(0, lastdot), extensionPointId.substring(lastdot + 1), extensionId);
	}

	public IExtension getExtension(String pluginId, String extensionPointName, String extensionId) {
		IExtensionPoint extPoint = getExtensionPoint(pluginId, extensionPointName);
		if (extPoint != null)
			return extPoint.getExtension(extensionId);
		return null;
	}

	public IExtensionPoint[] getExtensionPoints(String elementName) {
		IRegistryElement element = (IRegistryElement) elements.get(elementName);
		if (element == null)
			return new IExtensionPoint[0];
		Collection fragmentNames = getFragmentNames(elementName);
		IExtensionPoint[] allExtensionPoints = element.getExtensionPoints();
		for (Iterator iter = fragmentNames.iterator(); iter.hasNext();) {
			IRegistryElement fragment = (IRegistryElement) elements.get(iter.next());
			allExtensionPoints = (IExtensionPoint[]) addArrays(allExtensionPoints, fragment.getExtensionPoints());
		}
		return allExtensionPoints;
	}

	public IExtensionPoint[] getExtensionPoints() {
		ArrayList extensionPoints = new ArrayList();
		for (Iterator iter = elements.values().iterator(); iter.hasNext();) {
			IRegistryElement model = (IRegistryElement) iter.next();
			IExtensionPoint[] toAdd = model.getExtensionPoints();
			for (int i = 0; i < toAdd.length; i++)
				extensionPoints.add(toAdd[i]);
		}
		return (IExtensionPoint[]) extensionPoints.toArray(new IExtensionPoint[extensionPoints.size()]);
	}

	public IExtensionPoint getExtensionPoint(String xptUniqueId) {
		int lastdot = xptUniqueId.lastIndexOf('.');
		if (lastdot == -1)
			return null;
		return getExtensionPoint(xptUniqueId.substring(0, lastdot), xptUniqueId.substring(lastdot + 1));
	}

	public IExtensionPoint getExtensionPoint(String elementName, String xpt) {
		IRegistryElement element = (IRegistryElement) elements.get(elementName);
		if (element == null)
			return null;
		IExtensionPoint extPoint = element.getExtensionPoint(xpt);
		if (extPoint != null)
			return extPoint;
		Collection fragmentNames = getFragmentNames(elementName);
		for (Iterator iter = fragmentNames.iterator(); iter.hasNext();) {
			extPoint = ((IRegistryElement) elements.get(iter.next())).getExtensionPoint(xpt);
			if (extPoint != null)
				return extPoint;
		}
		return null;
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
		synchronized (this) {
			IRegistryElement element = (IRegistryElement) elements.get(elementName);
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
					IRegistryElement fragment = (IRegistryElement) elements.get(iter.next());
					removeExtensionsAndExtensionPoints(fragment);
				}
			}
			removeExtensionsAndExtensionPoints(element);
			// remove link between master and fragment
			removeFragmentFrom(elementName, element.getHostIdentifier());

			// remove it in the end
			elements.remove(elementName);
			fireRegistryChangeEvent();
		}
		return true;
	}

	private void removeExtensionsAndExtensionPoints(IRegistryElement element) {
		// remove extensions
		IExtension[] extensions = element.getExtensions();
		for (int i = 0; i < extensions.length; i++)
			this.removeExtension(extensions[i]);
		// remove extension points
		IExtensionPoint[] extPoints = element.getExtensionPoints();
		for (int i = 0; i < extPoints.length; i++)
			this.removeExtensionPoint(extPoints[i]);
	}

	private void removeExtensionPoint(IExtensionPoint extPoint) {
		if (extPoint.getExtensions() != null) {
			IExtension[] existingExtensions = extPoint.getExtensions();
			orphanExtensions.put(extPoint.getUniqueIdentifier(), existingExtensions);
			linker.link(extPoint, null);
			recordChange(extPoint, existingExtensions, IExtensionDelta.REMOVED);
		}
	}

	private void removeExtension(IExtension extension) {
		IExtensionPoint extPoint = getExtensionPoint(extension.getExtensionPointUniqueIdentifier());
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
		linker.link(extPoint, newExtensions);
		recordChange(extPoint, extension, IExtensionDelta.REMOVED);
	}

	/**
	 * Adds the given listener for registry change events related to this
	 * plug-in's extension points.
	 */
	// TODO This should be thread safe code.  May get called by multiple threads.
	public void removeRegistryChangeListener(IRegistryChangeListener listener) {
		this.listeners.remove(new ListenerInfo(listener, null));
	}

	public ExtensionRegistry(IExtensionLinker extensionLinker) {
		linker = extensionLinker;
		String debugOption = InternalPlatform.getDefault().getOption(OPTION_DEBUG_EVENTS_EXTENSION);
		DEBUG = debugOption == null ? false : debugOption.equalsIgnoreCase("true"); //$NON-NLS-1$		
		if (DEBUG)
			addRegistryChangeListener(new IRegistryChangeListener() {
				public void registryChanged(IRegistryChangeEvent event) {
					System.out.println(event);
				}
			});

	}

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
		private Object[] listenerInfos;
		private Map deltas;

		public ExtensionEventDispatcherJob(Object[] listenerInfos, Map deltas) {
			super("RegistryChangeEventDispatcherJob"); //$NON-NLS-1$
			this.listenerInfos = listenerInfos;
			this.deltas = deltas;
			// all extension event dispatching jobs use this rule
			setRule(EXTENSION_EVENT_RULE);
		}

		public IStatus run(IProgressMonitor monitor) {
			MultiStatus result = new MultiStatus(IPlatform.PI_RUNTIME, IStatus.OK, Policy.bind("plugin.eventListenerError"), null); //$NON-NLS-1$			
			for (int i = 0; i < listenerInfos.length; i++) {
				ListenerInfo listenerInfo = (ListenerInfo) listenerInfos[i];
				if (listenerInfo.filter != null && !deltas.containsKey(listenerInfo.filter))
					continue;
				try {
					listenerInfo.listener.registryChanged(new RegistryChangeEvent(deltas, listenerInfo.filter));
				} catch (RuntimeException re) {
					String message = re.getMessage() == null ? "" : re.getMessage(); //$NON-NLS-1$
					result.add(new Status(IStatus.ERROR, IPlatform.PI_RUNTIME, IStatus.OK, message, re));
				}
			}
			return result;
		}
	}

	public IRegistryElement getElement(String elementId) {
		return (IRegistryElement) elements.get(elementId);
	}

	public void setCacheReader(RegistryCacheReader value) {
		reader = value;
	}

	public RegistryCacheReader getCacheReader() {
		return reader;
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void setDirty(boolean value) {
		isDirty = value;
	}
}