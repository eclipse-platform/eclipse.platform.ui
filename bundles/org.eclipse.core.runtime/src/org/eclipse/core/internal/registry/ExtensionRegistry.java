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
package org.eclipse.core.internal.registry;

import java.lang.reflect.Array;
import java.util.*;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.registry.*;
import org.eclipse.core.runtime.registry.IConfigurationElement;
import org.eclipse.core.runtime.registry.IExtension;
import org.eclipse.core.runtime.registry.IExtensionPoint;

/**
 * An OSGi-free implementation for the extension registry API.
 */
public class ExtensionRegistry implements IExtensionRegistry {
	
	private static final String OPTION_DEBUG_EVENTS_EXTENSION = "org.eclipse.core.runtime/registry/debug/events/extension"; //$NON-NLS-1$	
	
	// a name->host mapping
	private Map hostsByName = new HashMap();
	// all registry change listeners 
	private Map listeners = new HashMap();
	// deltas not broadcasted yet
	private Map deltas = new HashMap();
	// extensions without extension point
	private Map orphanExtensions = new HashMap();
	// operation level == 0 means the current operation is top level
	private int operationLevel = 0;
	private IExtensionLinker linker;
	private boolean debug;

	public void add(IHost[] hosts) {
		synchronized (this) {
			try {
				operationLevel++;
				for (int i = 0; i < hosts.length; i++)
					add(hosts[i]);
			} finally {
				operationLevel--;
			}
		}
		fireRegistryChangeEvent();
	}
	/**
	 * Adds and resolves all extensions and extension points provided by the plug-in.
	 * <p>
	 * A corresponding IRegistryChangeEvent will be broadcast to all listeners interested 
	 * on changes in the given plug-in.
	 * </p>  
	 */
	public void add(IHost host) {
		synchronized (this) {
			operationLevel++;
			// add the plugin to the plugins map
			try {
				//TODO: this should be logged
				if (hostsByName.containsKey(host.getHostId())) {
					if (debug)
						System.out.println("********* Host already added: " + host.getHostId() + " - ignored.");  //$NON-NLS-1$//$NON-NLS-2$
					return;
				}
				hostsByName.put(host.getHostId(), host);
				addExtensionsAndExtensionPoints(host);
			} finally {
				operationLevel--;
			}
		}
		fireRegistryChangeEvent();
	}
	private void addExtensionsAndExtensionPoints(IHost host) {
		// now add and resolve extensions and extension points
		IExtensionPoint[] extPoints = host.getExtensionPoints();
		for (int i = 0; i < extPoints.length; i++)
			this.addExtensionPoint(extPoints[i]);
		IExtension[] extensions = host.getExtensions();
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
	private void addExtension(IExtension extension) {
		IExtensionPoint extPoint = getExtensionPoint(extension.getExtensionPointIdentifier());
		//orphan extension
		if (extPoint == null) {
			// are there any other orphan extensions
			IExtension[] existingOrphanExtensions = (IExtension[]) orphanExtensions.get(extension.getExtensionPointIdentifier());
			if (existingOrphanExtensions != null) {
				// just add					 
				IExtension[] newOrphanExtensions = new IExtension[existingOrphanExtensions.length + 1];
				System.arraycopy(existingOrphanExtensions, 0, newOrphanExtensions, 0, existingOrphanExtensions.length);
				newOrphanExtensions[newOrphanExtensions.length - 1] = extension;
				orphanExtensions.put(extension.getExtensionPointIdentifier(), newOrphanExtensions);
			} else
				// otherwise this is the first one
				orphanExtensions.put(extension.getExtensionPointIdentifier(), new IExtension[] { extension });
			return;
		}
		// otherwise, link them
		IExtension[] newExtensions;
		IExtension[] existingExtensions = extPoint.getExtensions();
		if (existingExtensions == null)
			newExtensions = new IExtension[] { extension };
		else {
			newExtensions = new IExtension[existingExtensions.length + 1];
			System.arraycopy(existingExtensions, 0, newExtensions, 0, existingExtensions.length);
			newExtensions[newExtensions.length - 1] = extension;
		}
		linker.link(extPoint,newExtensions);
		recordChange(extPoint, extension, IExtensionDelta.ADDED);
	}
	/**
	 * Looks for existing orphan extensions to connect to the given extension point. If 
	 * none is found, there is nothing to do. Otherwise, link them. 
	 */
	private void addExtensionPoint(IExtensionPoint extPoint) {
		IExtension[] existingExtensions = (IExtension[]) orphanExtensions.remove(extPoint.getUniqueIdentifier());
		if (existingExtensions == null)
			return;
		// otherwise, link them
		linker.link(extPoint,existingExtensions);
		recordChange(extPoint, existingExtensions, IExtensionDelta.ADDED);
	}
	/**
	* Adds the given listener for registry change events on the given plug-in.
	*/
	public void addRegistryChangeListener(IRegistryChangeListener listener, String filter) {
		this.listeners.put(listener, filter);
	}
	/**
	* Adds the given listener for registry change events.
	*/
	public void addRegistryChangeListener(IRegistryChangeListener listener) {
		this.listeners.put(listener, null);
	}
	/**
	 * Broadcasts the event to all interested parties. 
	 */
	private void fireRegistryChangeEvent() {
		Map tmpListeners;
		Map tmpDeltas;
		synchronized (this) {
			// if it is not a top-level operation...
			if (!shouldNotify())
				return;
			// or if there is nothing to say, just bail out
			if (deltas.isEmpty())
				return;
			// for thread safety, create tmp collections
			tmpListeners = new HashMap(listeners);
			tmpDeltas = new HashMap(this.deltas);
			// the deltas have been saved for notification - we can clear it now
			deltas.clear();
		}
		new ExtensionEventDispatcherJob(tmpListeners, tmpDeltas).schedule();
	}
	private HostDelta getHostDelta(String hostName) {
		// is there a delta for the plug-in? 
		HostDelta existingDelta = (HostDelta) deltas.get(hostName);
		if (existingDelta != null)
			return existingDelta;

		//if not, create one
		HostDelta delta = new HostDelta(hostName);
		deltas.put(hostName, delta);
		return delta;
	}
	public String[] getElementIdentifiers() {
		return (String[]) hostsByName.keySet().toArray(new String[hostsByName.size()]);
	}
	public IConfigurationElement[] getConfigurationElementsFor(String extensionPointId) {
		int lastdot = extensionPointId.lastIndexOf('.');
		if (lastdot == -1)
			return null;
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
	public IExtension[] getExtensions(String hostName) {
		IHost host = (IHost) hostsByName.get(hostName);
		if (host == null)
			return new IExtension[0];
		return host.getExtensions();
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
	public IExtensionPoint[] getExtensionPoints(String hostName) {
		IHost host = (IHost) hostsByName.get(hostName);
		if (host == null)
			return new IExtensionPoint[0];
		return host.getExtensionPoints();
	}
	public IExtensionPoint[] getExtensionPoints() {
		ArrayList extensionPoints = new ArrayList();
		for (Iterator iter = hostsByName.values().iterator(); iter.hasNext();) {
			IHost model = (IHost) iter.next();
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
	public IExtensionPoint getExtensionPoint(String hostName, String xpt) {
		IHost host = (IHost) hostsByName.get(hostName);
		if (host == null)
			return null;
		return host.getExtensionPoint(xpt);
	}
	/*
	 * Records an extension addition/removal.
	 */
	private void recordChange(IExtensionPoint extPoint, IExtension extension, int kind) {
		ExtensionDelta extensionDelta = new ExtensionDelta();
		extensionDelta.setExtension(extension);
		extensionDelta.setExtensionPoint(extPoint);
		extensionDelta.setKind(kind);
		getHostDelta(extPoint.getParentIdentifier()).addExtensionDelta(extensionDelta);
	}
	/*
	 * Records a set of extension additions/removals.
	 */
	private void recordChange(IExtensionPoint extPoint, IExtension[] extensions, int kind) {
		if (extensions.length == 0)
			return;
		HostDelta pluginDelta = getHostDelta(extPoint.getParentIdentifier());
		for (int i = 0; i < extensions.length; i++) {
			ExtensionDelta extensionDelta = new ExtensionDelta();
			extensionDelta.setExtension(extensions[i]);
			extensionDelta.setExtensionPoint(extPoint);
			extensionDelta.setKind(kind);
			pluginDelta.addExtensionDelta(extensionDelta);
		}
	}
	/**
	 * Unresolves and removes all extensions and extension points provided by the 
	 * plug-in.  
	 * <p>
	 * A corresponding IRegistryChangeEvent will be broadcast to all listeners interested 
	 * on changes in the given plug-in.
	 * </p> 
	 */
	public boolean remove(String hostName) {
		synchronized (this) {
			operationLevel++;
			try {
				IHost host = (IHost) hostsByName.get(hostName);
				if (host == null) {
					if (debug)
						System.out.println("********* Host unknown: " + hostName + " - not removed.");  //$NON-NLS-1$//$NON-NLS-2$
					return false;
				}
				removeExtensionsAndExtensionPoints(host);
				// remove it in the end
				hostsByName.remove(hostName);
			} finally {
				operationLevel--;
			}
		}
		fireRegistryChangeEvent();
		return true;
	}
	private void removeExtensionsAndExtensionPoints(IHost host) {
		// remove extensions
		IExtension[] extensions = host.getExtensions();
		for (int i = 0; i < extensions.length; i++)
			this.removeExtension(extensions[i]);
		// remove extension points
		IExtensionPoint[] extPoints = host.getExtensionPoints();
		for (int i = 0; i < extPoints.length; i++)
			this.removeExtensionPoint(extPoints[i]);
	}
	private void removeExtensionPoint(IExtensionPoint extPoint) {
		if (extPoint.getExtensions() != null) {
			IExtension[] existingExtensions = extPoint.getExtensions();
			orphanExtensions.put(extPoint.getUniqueIdentifier(), existingExtensions);
			linker.link(extPoint,null);
			recordChange(extPoint, existingExtensions, IExtensionDelta.REMOVED);
		}
	}
	private void removeExtension(IExtension extension) {
		IExtensionPoint extPoint = getExtensionPoint(extension.getExtensionPointIdentifier());
		if (extPoint == null) {
			// not found - maybe it was an orphan extension 				
			IExtension[] existingOrphanExtensions = (IExtension[]) orphanExtensions.get(extension.getExtensionPointIdentifier());
			if (existingOrphanExtensions == null)
				// nope, this extension is unknown
				return;
			// yes, so just remove it from the orphans list
			IExtension[] newOrphanExtensions = new IExtension[existingOrphanExtensions.length - 1];
			for (int i = 0, j = 0; i < existingOrphanExtensions.length; i++)
				if (extension != existingOrphanExtensions[i])
					newOrphanExtensions[j++] = existingOrphanExtensions[i];
			orphanExtensions.put(extension.getExtensionPointIdentifier(), newOrphanExtensions);
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
		linker.link(extPoint,newExtensions);
		recordChange(extPoint, extension, IExtensionDelta.REMOVED);
	}

	/**
	 * Adds the given listener for registry change events related to this plug-in's extension points.
	 */
	public void removeRegistryChangeListener(IRegistryChangeListener listener) {
		this.listeners.remove(listener);
	}
	/**
	 * Should notify only at top level (level == 0) operations.
	 */
	private boolean shouldNotify() {
		return operationLevel == 0;
	}
	public ExtensionRegistry(IExtensionLinker extensionLinker) {
		this.linker = extensionLinker;
		String debugOption = InternalPlatform.getDebugOption(OPTION_DEBUG_EVENTS_EXTENSION);
		this.debug = debugOption == null ? false : debugOption.equalsIgnoreCase("true"); //$NON-NLS-1$		
		if (this.debug)
			addRegistryChangeListener(new IRegistryChangeListener() {
			public void registryChanged(IRegistryChangeEvent event) {
				System.out.println(event);
			}
		});

	}
	public class ExtensionEventDispatcherJob extends Job {
		private Map listeners;
		private Map deltas;
		public ExtensionEventDispatcherJob(Map listeners, Map deltas) {
			super("RegistryChangeEventDispatcherJob"); //$NON-NLS-1$
			this.listeners = listeners;
			this.deltas = deltas;
		}
		public IStatus run(IProgressMonitor monitor) {
			MultiStatus result = new MultiStatus(Platform.PI_RUNTIME, IStatus.OK, Policy.bind("pluginEvent.errorListener"), null); //$NON-NLS-1$			
			for (Iterator iter = listeners.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry) iter.next();
				IRegistryChangeListener listener = (IRegistryChangeListener) entry.getKey();
				String filter = (String) entry.getValue();
				if (filter != null && !deltas.containsKey(filter))
					continue;
				try {
					listener.registryChanged(new RegistryChangeEvent(deltas, filter));
				} catch (RuntimeException re) {
					String message = re.getMessage() == null ? "" : re.getMessage(); //$NON-NLS-1$
					result.add(new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.OK, message, re));
				}
			}
			return result;
		}
	}
}