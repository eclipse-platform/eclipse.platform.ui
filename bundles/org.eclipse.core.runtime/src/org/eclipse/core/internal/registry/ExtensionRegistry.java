/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.adaptor.FileManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.Bundle;

/**
 * An implementation for the extension registry API.
 */
public class ExtensionRegistry implements IExtensionRegistry {
	private EclipseBundleListener pluginBundleListener;

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
			// name not NL'd since it is a system job
			super("Registry event dispatcher"); //$NON-NLS-1$
			setSystem(true);
			this.listenerInfos = listenerInfos;
			this.deltas = deltas;
			// all extension event dispatching jobs use this rule
			setRule(EXTENSION_EVENT_RULE);
		}

		public IStatus run(IProgressMonitor monitor) {
			MultiStatus result = new MultiStatus(Platform.PI_RUNTIME, IStatus.OK, Messages.plugin_eventListenerError, null);
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
			for (Iterator iter = deltas.values().iterator(); iter.hasNext();) {
				((RegistryDelta) iter.next()).getObjectManager().close();
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

	// deltas not broadcasted yet. Deltas are kept organized by bundle name (fragments go with their host)
	private transient Map deltas = new HashMap(11);

	//file manager associated with the registry
	private FileManager currentFileManager = null;
	
	// all registry change listeners
	private transient ListenerList listeners = new ListenerList();

	private RegistryObjectManager registryObjects = null;

	RegistryObjectManager getObjectManager() {
		return registryObjects;
	}
	
	/**
	 * Adds and resolves all extensions and extension points provided by the
	 * plug-in.
	 * <p>
	 * A corresponding IRegistryChangeEvent will be broadcast to all listeners
	 * interested on changes in the given plug-in.
	 * </p>
	 */
	public void add(Contribution element) {
		access.enterWrite();
		try {
			basicAdd(element, true);
			fireRegistryChangeEvent();
		} finally {
			access.exitWrite();
		}
	}
	
	public void add(Contribution[] elements) {
		access.enterWrite();
		try {
			for (int i = 0; i < elements.length; i++)
				basicAdd(elements[i], true);
			fireRegistryChangeEvent();
		} finally {
			access.exitWrite();
		}
	}

	/* Utility method to help with array concatenations */
	static Object concatArrays(Object a, Object b) {
		Object[] result = (Object[]) Array.newInstance(a.getClass().getComponentType(), Array.getLength(a) + Array.getLength(b));
		System.arraycopy(a, 0, result, 0, Array.getLength(a));
		System.arraycopy(b, 0, result, Array.getLength(a), Array.getLength(b));
		return result;
	}

	private String addExtension(int extension) {
		Extension addedExtension = (Extension) registryObjects.getObject(extension, RegistryObjectManager.EXTENSION);
		String extensionPointToAddTo = addedExtension.getExtensionPointIdentifier();
		ExtensionPoint extPoint = registryObjects.getExtensionPointObject(extensionPointToAddTo);
		//orphan extension
		if (extPoint == null) {
			registryObjects.addOrphan(extensionPointToAddTo, extension);
			return null;
		}
		// otherwise, link them
		int[] newExtensions;
		int[] existingExtensions = extPoint.getRawChildren();
		newExtensions = new int[existingExtensions.length + 1];
		System.arraycopy(existingExtensions, 0, newExtensions, 0, existingExtensions.length);
		newExtensions[newExtensions.length - 1] = extension;
		link(extPoint, newExtensions);
		return recordChange(extPoint, extension, IExtensionDelta.ADDED);
	}

	/**
	 * Looks for existing orphan extensions to connect to the given extension
	 * point. If none is found, there is nothing to do. Otherwise, link them.
	 */
	private String addExtensionPoint(int extPoint) {
		ExtensionPoint extensionPoint = (ExtensionPoint) registryObjects.getObject(extPoint, RegistryObjectManager.EXTENSION_POINT);
		int[] orphans = registryObjects.removeOrphans(extensionPoint.getUniqueIdentifier());
		if (orphans == null)
			return null;
		link(extensionPoint, orphans);
		return recordChange(extensionPoint, orphans, IExtensionDelta.ADDED);
	}

	private Set addExtensionsAndExtensionPoints(Contribution element) {
		// now add and resolve extensions and extension points
		Set affectedNamespaces = new HashSet();
		int[] extPoints = element.getExtensionPoints();
		for (int i = 0; i < extPoints.length; i++) {
			String namespace = this.addExtensionPoint(extPoints[i]);
			if (namespace != null)
				affectedNamespaces.add(namespace);
		}
		int[] extensions = element.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			String namespace = this.addExtension(extensions[i]);
			if (namespace != null)
				affectedNamespaces.add(namespace);
		}
		return affectedNamespaces;
	}

	public void addRegistryChangeListener(IRegistryChangeListener listener) {
		// this is just a convenience API - no need to do any sync'ing here		
		addRegistryChangeListener(listener, null);
	}

	public void addRegistryChangeListener(IRegistryChangeListener listener, String filter) {
		synchronized (listeners) {
			listeners.add(new ListenerInfo(listener, filter));
		}
	}

	private void basicAdd(Contribution element, boolean link) {
		// ignore anonymous namespaces
		if (element.getNamespace() == null)
			return;

		registryObjects.addContribution(element);
		if (!link)
			return;

		Set affectedNamespaces = addExtensionsAndExtensionPoints(element);
		setObjectManagers(affectedNamespaces, registryObjects.createDelegatingObjectManager(registryObjects.getAssociatedObjects(element.getContributingBundle().getBundleId())));
	}

	private void setObjectManagers(Set affectedNamespaces, IObjectManager manager) {
		for (Iterator iter = affectedNamespaces.iterator(); iter.hasNext();) {
			getDelta((String) iter.next()).setObjectManager(manager);
		}
	}
	
	private void basicRemove(long bundleId) {
		// ignore anonymous namespaces
		Set affectedNamespaces = removeExtensionsAndExtensionPoints(bundleId);
		Map associatedObjects = registryObjects.getAssociatedObjects(bundleId);
		registryObjects.removeObjects(associatedObjects);
		setObjectManagers(affectedNamespaces, registryObjects.createDelegatingObjectManager(associatedObjects));
		
		registryObjects.removeContribution(bundleId);
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

	private RegistryDelta getDelta(String namespace) {
		// is there a delta for the plug-in?
		RegistryDelta existingDelta = (RegistryDelta) deltas.get(namespace);
		if (existingDelta != null)
			return existingDelta;

		//if not, create one
		RegistryDelta delta = new RegistryDelta();
		deltas.put(namespace, delta);
		return delta;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#getExtension(java.lang.String)
	 */
	public IExtension getExtension(String extensionId) {
		if (extensionId == null)
			return null;
		int lastdot = extensionId.lastIndexOf('.');
		if (lastdot == -1)
			return null;
		String namespace = extensionId.substring(0, lastdot);

		Bundle[] allBundles = findAllBundles(namespace);
		for (int i = 0; i < allBundles.length; i++) {
			int[] extensions = registryObjects.getExtensionsFrom(allBundles[i].getBundleId());
			for (int j = 0; j < extensions.length; j++) {
				Extension ext = (Extension) registryObjects.getObject(extensions[j], RegistryObjectManager.EXTENSION);
				if (extensionId.equals(ext.getUniqueIdentifier()) && registryObjects.getExtensionPointObject(ext.getExtensionPointIdentifier()) != null) {
					return (IExtension) registryObjects.getHandle(extensions[j], RegistryObjectManager.EXTENSION);
				}
			}
			
		}
		return null;
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
		return registryObjects.getExtensionPointHandle(xptUniqueId);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#getExtensionPoint(java.lang.String, java.lang.String)
	 */
	public IExtensionPoint getExtensionPoint(String elementName, String xpt) {
		access.enterRead();
		try {
			return registryObjects.getExtensionPointHandle(elementName + '.' + xpt);
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
			return registryObjects.getExtensionPointsHandles();
		} finally {
			access.exitRead();
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#getExtensionPoints(java.lang.String)
	 */
	public IExtensionPoint[] getExtensionPoints(String namespace) {
		access.enterRead();
		try {
			Bundle[] correspondingBundles = findAllBundles(namespace);
			IExtensionPoint[] result = ExtensionPointHandle.EMPTY_ARRAY;
			for (int i = 0; i < correspondingBundles.length; i++) {
				result = (IExtensionPoint[]) concatArrays(result, registryObjects.getHandles(registryObjects.getExtensionPointsFrom(correspondingBundles[i].getBundleId()), RegistryObjectManager.EXTENSION_POINT));
			}
			return result;
		} finally {
			access.exitRead();
		}
	}

	//Return all the bundles that contributes to the given namespace
	private Bundle[] findAllBundles(String namespace) {
		Bundle correspondingHost = Platform.getBundle(namespace);
		if (correspondingHost == null)
			return new Bundle[0];
		Bundle[] fragments = Platform.getFragments(correspondingHost);
		if(fragments==null)
			return new Bundle[] { correspondingHost };
		Bundle[] result = new Bundle[fragments.length + 1];
		System.arraycopy(fragments, 0, result, 0, fragments.length);
		result[fragments.length] = correspondingHost;
		return result;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExtensionRegistry#getExtensions(java.lang.String)
	 */
	public IExtension[] getExtensions(String namespace) {
		access.enterRead();
		try {
			Bundle[] correspondingBundles = findAllBundles(namespace);
			List tmp = new ArrayList();
			for (int i = 0; i < correspondingBundles.length; i++) {
				Extension[] exts = (Extension[]) registryObjects.getObjects(registryObjects.getExtensionsFrom(correspondingBundles[i].getBundleId()), RegistryObjectManager.EXTENSION);
				for (int j = 0; j < exts.length; j++) {
					if (registryObjects.getExtensionPointObject(exts[j].getExtensionPointIdentifier()) != null)
						tmp.add(registryObjects.getHandle(exts[j].getObjectId(), RegistryObjectManager.EXTENSION));
				}
			}
			if (tmp.size() == 0)
				return ExtensionHandle.EMPTY_ARRAY;
			IExtension[] result = new IExtension[tmp.size()];
			return (IExtension[]) tmp.toArray(result);
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
			Set namespaces = registryObjects.getNamespaces();
			String[] result = new String[namespaces.size()];
			return (String[]) namespaces.toArray(result);
		} finally {
			access.exitRead();
		}
	}

	boolean hasNamespace(long name) {
		access.enterRead();
		try {
			return registryObjects.hasContribution(name);
		} finally {
			access.exitRead();
		}
	}

	private void link(ExtensionPoint extPoint, int[] extensions) {
		extPoint.setRawChildren(extensions);
		registryObjects.add(extPoint, true);
	}

	/*
	 * Records an extension addition/removal.
	 */
	private String recordChange(ExtensionPoint extPoint, int extension, int kind) {
		// avoid computing deltas when there are no listeners
		if (listeners.isEmpty())
			return null;
		ExtensionDelta extensionDelta = new ExtensionDelta();
		extensionDelta.setExtension(extension);
		extensionDelta.setExtensionPoint(extPoint.getObjectId());
		extensionDelta.setKind(kind);
		getDelta(extPoint.getNamespace()).addExtensionDelta(extensionDelta);
		return extPoint.getNamespace();
	}

	/*
	 * Records a set of extension additions/removals.
	 */
	private String recordChange(ExtensionPoint extPoint, int[] extensions, int kind) {
		if (listeners.isEmpty())
			return null;
		if (extensions == null || extensions.length == 0)
			return null;
		RegistryDelta pluginDelta = getDelta(extPoint.getNamespace());
		for (int i = 0; i < extensions.length; i++) {
			ExtensionDelta extensionDelta = new ExtensionDelta();
			extensionDelta.setExtension(extensions[i]);
			extensionDelta.setExtensionPoint(extPoint.getObjectId());
			extensionDelta.setKind(kind);
			pluginDelta.addExtensionDelta(extensionDelta);
		}
		return extPoint.getNamespace();
	}

//	private void recordRemoval(long removedBundleId) {
//		int[] exts = registryObjects.getExtensionsFrom(removedBundleId);
//		Extension[] aexts = (Extension[]) registryObjects.getObjects(exts, RegistryObjectManager.EXTENSION);
//		IObjectManager mgr = registryObjects.getRemovedObjects(removedBundleId);
//		for (int i = 0; i < aexts.length; i++) {
//			getDelta(registryObjects.getExtensionPointObject(aexts[i].getExtensionPointIdentifier()).getNamespace()).setObjectManager(mgr);
//		}
//	}

	/**
	 * Unresolves and removes all extensions and extension points provided by
	 * the plug-in.
	 * <p>
	 * A corresponding IRegistryChangeEvent will be broadcast to all listeners
	 * interested on changes in the given plug-in.
	 * </p>
	 */
	public void remove(long removedBundleId) {
		access.enterWrite();
		try {
			basicRemove(removedBundleId);
			fireRegistryChangeEvent();
		} finally {
			access.exitWrite();
		}
	}

	//Return the affected namespace
	private String removeExtension(int extensionId) {
		Extension extension = (Extension) registryObjects.getObject(extensionId, RegistryObjectManager.EXTENSION);
		String xptName = extension.getExtensionPointIdentifier();
		ExtensionPoint extPoint = registryObjects.getExtensionPointObject(xptName);
		if (extPoint == null) {
			registryObjects.removeOrphan(xptName, extensionId);
			return null;
		}
		// otherwise, unlink the extension from the extension point
		int[] existingExtensions = extPoint.getRawChildren();
		int[] newExtensions = RegistryObjectManager.EMPTY_INT_ARRAY;
		if (existingExtensions.length > 1) {
			if (existingExtensions.length == 1)
				newExtensions = RegistryObjectManager.EMPTY_INT_ARRAY;
			
			newExtensions = new int[existingExtensions.length - 1];
			for (int i = 0, j = 0; i < existingExtensions.length; i++)
				if (existingExtensions[i] != extension.getObjectId())
					newExtensions[j++] = existingExtensions[i];
		}
		link(extPoint, newExtensions);
		return recordChange(extPoint, extension.getObjectId(), IExtensionDelta.REMOVED);
	}

	private String removeExtensionPoint(int extPoint) {
		ExtensionPoint extensionPoint = (ExtensionPoint) registryObjects.getObject(extPoint, RegistryObjectManager.EXTENSION_POINT);
		int[] existingExtensions = extensionPoint.getRawChildren();
		if (existingExtensions == null || existingExtensions.length == 0) {
			return null;
		}
		//Remove the extension point from the registry object
		registryObjects.addOrphans(extensionPoint.getUniqueIdentifier(), existingExtensions);
		link(extensionPoint, RegistryObjectManager.EMPTY_INT_ARRAY);
		return recordChange(extensionPoint, existingExtensions, IExtensionDelta.REMOVED);
	}

	private Set removeExtensionsAndExtensionPoints(long bundleId) {
		Set affectedNamespaces = new HashSet();
		int[] extensions = registryObjects.getExtensionsFrom(bundleId);
		for (int i = 0; i < extensions.length; i++) {
			String namespace = this.removeExtension(extensions[i]);
			if (namespace != null)
				affectedNamespaces.add(namespace);
		}

		// remove extension points
		int[] extPoints = registryObjects.getExtensionPointsFrom(bundleId);
		for (int i = 0; i < extPoints.length; i++) {
			String namespace = this.removeExtensionPoint(extPoints[i]);
			if (namespace != null)
				affectedNamespaces.add(namespace);
		}
		return affectedNamespaces;
	}

	public void removeRegistryChangeListener(IRegistryChangeListener listener) {
		synchronized (listeners) {
			listeners.remove(new ListenerInfo(listener, null));
		}
	}

	public ExtensionRegistry() {
		boolean fromCache = false;
		registryObjects = new RegistryObjectManager();
		if (!"true".equals(System.getProperty(InternalPlatform.PROP_NO_REGISTRY_CACHE))) { //$NON-NLS-1$
			// Try to read the registry from the cache first. If that fails, create a new registry
			long start = 0;
			if (InternalPlatform.DEBUG)
				start = System.currentTimeMillis();

			//Find the cache in the local configuration area
			File cacheFile = null;
			try {
				currentFileManager = InternalPlatform.getDefault().getRuntimeFileManager();
				cacheFile = currentFileManager.lookup(TableReader.TABLE, false);
			} catch (IOException e) {
				//Ignore the exception. The registry will be rebuilt from the xml files.
			}
			//Find the cache in the shared configuration area
			if (cacheFile == null || !cacheFile.isFile()) {
				Location currentLocation = Platform.getConfigurationLocation();
				Location parentLocation = null;
				if (currentLocation != null && (parentLocation = currentLocation.getParentLocation()) != null) {
					try {
						currentFileManager = new FileManager(new File(parentLocation.getURL().getFile() + '/' + Platform.PI_RUNTIME), null, true);
						currentFileManager.open(false);
						cacheFile = currentFileManager.lookup(TableReader.TABLE, false);
					} catch (IOException e) {
						//Ignore the exception. The registry will be rebuilt from the xml files.
					}
				}
			}

			//The cache is made of several files, find the real names of these other files. If all files are found, try to initialize the objectManager
			if (cacheFile != null && cacheFile.isFile()) {
				TableReader.setTableFile(cacheFile);
				try {
					TableReader.setExtraDataFile(currentFileManager.lookup(TableReader.EXTRA, false));
					TableReader.setMainDataFile(currentFileManager.lookup(TableReader.MAIN, false));
					TableReader.setContributionsFile(currentFileManager.lookup(TableReader.CONTRIBUTIONS, false));
					TableReader.setOrphansFile(currentFileManager.lookup(TableReader.ORPHANS, false));
					fromCache = registryObjects.init(computeRegistryStamp());
				} catch (IOException e) {
					// Ignore the exception. The registry will be rebuilt from the xml files.
				}
			}

			if (InternalPlatform.DEBUG && fromCache)
				System.out.println("Reading registry cache: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$

			if (InternalPlatform.DEBUG_REGISTRY) {
				if (!fromCache)
					System.out.println("Reloading registry from manifest files..."); //$NON-NLS-1$
				else
					System.out.println("Using registry cache..."); //$NON-NLS-1$
			}
		}

		String debugOption = InternalPlatform.getDefault().getOption(OPTION_DEBUG_EVENTS_EXTENSION);
		DEBUG = debugOption == null ? false : debugOption.equalsIgnoreCase("true"); //$NON-NLS-1$	
		if (DEBUG)
			addRegistryChangeListener(new IRegistryChangeListener() {
				public void registryChanged(IRegistryChangeEvent event) {
					System.out.println(event);
				}
			});

		// register a listener to catch new bundle installations/resolutions.
		pluginBundleListener = new EclipseBundleListener(this);
		InternalPlatform.getDefault().getBundleContext().addBundleListener(pluginBundleListener);

		// populate the registry with all the currently installed bundles.
		// There is a small window here while processBundles is being
		// called where the pluginBundleListener may receive a BundleEvent 
		// to add/remove a bundle from the registry.  This is ok since
		// the registry is a synchronized object and will not add the
		// same bundle twice.
		if (!fromCache)
			pluginBundleListener.processBundles(InternalPlatform.getDefault().getBundleContext().getBundles());

		InternalPlatform.getDefault().getBundleContext().registerService(IExtensionRegistry.class.getName(), this, new Hashtable());

	}

	public void stop() {
		InternalPlatform.getDefault().getBundleContext().removeBundleListener(this.pluginBundleListener);
		if (!registryObjects.isDirty())
			return;
		if (currentFileManager == null)
			return;
		if (currentFileManager.isReadOnly()) {
			if (currentFileManager != InternalPlatform.getDefault().getRuntimeFileManager())
				currentFileManager.close();
			return;
		}
			
		File tableFile = null;
		File mainFile = null;
		File extraFile = null;
		File contributionsFile = null;
		File orphansFile = null;
		try {
			currentFileManager.lookup(TableReader.TABLE, true);
			currentFileManager.lookup(TableReader.MAIN, true);
			currentFileManager.lookup(TableReader.EXTRA, true);
			currentFileManager.lookup(TableReader.CONTRIBUTIONS, true);
			currentFileManager.lookup(TableReader.ORPHANS, true);
			tableFile = File.createTempFile(TableReader.TABLE, ".new", currentFileManager.getBase()); //$NON-NLS-1$
			mainFile = File.createTempFile(TableReader.MAIN, ".new", currentFileManager.getBase()); //$NON-NLS-1$
			extraFile = File.createTempFile(TableReader.EXTRA, ".new", currentFileManager.getBase()); //$NON-NLS-1$
			contributionsFile = File.createTempFile(TableReader.CONTRIBUTIONS, ".new", currentFileManager.getBase()); //$NON-NLS-1$
			orphansFile = File.createTempFile(TableReader.ORPHANS, ".new", currentFileManager.getBase()); //$NON-NLS-1$
			TableWriter.setTableFile(tableFile);
			TableWriter.setExtraDataFile(extraFile);
			TableWriter.setMainDataFile(mainFile);
			TableWriter.setContributionsFile(contributionsFile);
			TableWriter.setOrphansFile(orphansFile);
		} catch (IOException e) {
			return; //Ignore the exception since we can recompute the cache
		}
		try {
			if (new TableWriter().saveCache(registryObjects, computeRegistryStamp()))
				currentFileManager.update(new String[] {TableReader.TABLE, TableReader.MAIN, TableReader.EXTRA, TableReader.CONTRIBUTIONS, TableReader.ORPHANS}, new String[] {tableFile.getName(), mainFile.getName(), extraFile.getName(), contributionsFile.getName(), orphansFile.getName()});
		} catch (IOException e) {
			//Ignore the exception since we can recompute the cache
		}
		if (currentFileManager != InternalPlatform.getDefault().getRuntimeFileManager())
			currentFileManager.close();
	}

	/*
	 * Clear the registry cache files from the file manager so on next start-up we recompute it.
	 */
	public void clearRegistryCache() {
		String[] keys = new String[] {TableReader.TABLE, TableReader.MAIN, TableReader.EXTRA, TableReader.CONTRIBUTIONS, TableReader.ORPHANS};
		for (int i=0; i<keys.length; i++)
			try {
				currentFileManager.remove(keys[i]);
			} catch (IOException e) {
				InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, Messages.meta_registryCacheReadProblems, e));
			}
	}

	private long computeRegistryStamp() {
		// If the check config prop is false or not set then exit
		if (!"true".equalsIgnoreCase(System.getProperty(InternalPlatform.PROP_CHECK_CONFIG))) //$NON-NLS-1$  
			return 0;
		Bundle[] allBundles = InternalPlatform.getDefault().getBundleContext().getBundles();
		long result = 0;
		for (int i = 0; i < allBundles.length; i++) {
			URL pluginManifest = allBundles[i].getEntry("plugin.xml"); //$NON-NLS-1$
			if (pluginManifest == null)
				pluginManifest = allBundles[i].getEntry("fragment.xml"); //$NON-NLS-1$
			if (pluginManifest == null)
				continue;
			try {
				URLConnection connection = pluginManifest.openConnection();
				result ^= connection.getLastModified() + allBundles[i].getBundleId();
			} catch (IOException e) {
				return 0;
			}
		}
		return result;
	}
}
