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
package org.eclipse.core.internal.properties;

import java.util.*;
import org.eclipse.core.internal.events.ILifecycleListener;
import org.eclipse.core.internal.events.LifecycleEvent;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

/**
 * @see org.eclipse.core.internal.properties.IPropertyManager
 */
public class PropertyManager implements IManager, ILifecycleListener, IPropertyManager {
	protected Workspace workspace;

	public PropertyManager(Workspace workspace) {
		this.workspace = workspace;
	}

	public void closePropertyStore(IResource target) throws CoreException {
		PropertyStore store = getPropertyStoreOrNull(target);
		if (store == null)
			return;
		synchronized (store) {
			store.shutdown(null);
			setPropertyStore(target, null);
		}
	}

	/**
	 * Copy all the properties of one resource to another. Both resources
	 * must have a property store available.
	 */
	public void copy(IResource source, IResource destination, int depth) throws CoreException {
		// cache stores to avoid problems in concurrency
		PropertyStore sourceStore = getPropertyStore(source);
		PropertyStore destinationStore = getPropertyStore(destination);
		synchronized (sourceStore) {
			assertRunning(source, sourceStore);
			synchronized (destinationStore) {
				assertRunning(destination, destinationStore);
				copyProperties(source, destination, depth);
				sourceStore.commit();
				destinationStore.commit();
			}
		}
	}

	/**
	 * Throws an exception if the store has been shut down
	 */
	private void assertRunning(IResource target, PropertyStore store) throws CoreException {
		if (!store.isRunning()) {
			//if the store is not running then the resource is in the process of being deleted, 
			//so report the error as if the resource was not found
			String message = NLS.bind(CompatibilityMessages.resources_mustExist, target.getFullPath());
			throw new ResourceException(IResourceStatus.RESOURCE_NOT_FOUND, target.getFullPath(), message, null);
		}
	}

	private void copyProperties(IResource source, IResource destination, int depth) throws CoreException {
		PropertyStore sourceStore = getPropertyStore(source);
		PropertyStore destStore = getPropertyStore(destination);
		ResourceName sourceName = getPropertyKey(source);
		ResourceName destName = getPropertyKey(destination);
		QueryResults results = sourceStore.getAll(sourceName, depth);
		for (Enumeration resources = results.getResourceNames(); resources.hasMoreElements();) {
			ResourceName resourceName = (ResourceName) resources.nextElement();
			List properties = results.getResults(resourceName);
			if (properties.isEmpty())
				continue;
			StoredProperty[] propsArray = new StoredProperty[properties.size()];
			propsArray = (StoredProperty[]) properties.toArray(propsArray);
			int segmentsToDrop = source.getProjectRelativePath().matchingFirstSegments(resourceName.getPath());
			IPath path = destName.getPath().append(resourceName.getPath().removeFirstSegments(segmentsToDrop));
			resourceName = new ResourceName(resourceName.getQualifier(), path);
			destStore.set(resourceName, propsArray, IResource.DEPTH_ZERO, PropertyStore.SET_UPDATE);
		}
	}

	public void deleteProperties(IResource target, int depth) throws CoreException {
		switch (target.getType()) {
			case IResource.FILE :
			case IResource.FOLDER :
				PropertyStore store = getPropertyStore(target);
				synchronized (store) {
					assertRunning(target, store);
					store.removeAll(getPropertyKey(target), depth);
					store.commit();
				}
				break;
			case IResource.PROJECT :
			case IResource.ROOT :
				deletePropertyStore(target, true);
		}
	}

	/**
	 * The resource is being deleted so permanently erase its properties.
	 * In the case of projects, this means the property store will not be
	 * accessible again.
	 */
	public void deleteResource(IResource target) throws CoreException {
		switch (target.getType()) {
			case IResource.FILE :
			case IResource.FOLDER :
			case IResource.ROOT :
				deleteProperties(target, IResource.DEPTH_INFINITE);
				break;
			case IResource.PROJECT :
				//permanently delete the store
				deletePropertyStore(target, false);
		}
	}

	private void deletePropertyStore(IResource target, boolean restart) throws CoreException {
		PropertyStore store = getPropertyStoreOrNull(target);
		if (store == null)
			return;
		synchronized (store) {
			store.shutdown(null);
			workspace.getMetaArea().getPropertyStoreLocation(target).toFile().delete();
			//if we want to allow restart, null the store and it will be recreated lazily
			if (restart) {
				ResourceInfo info = getPropertyHost(target).getResourceInfo(false, false);
				if (info != null)
					info.setPropertyStore(null);
			}
		}
	}

	/**
	 * Returns the value of the identified property on the given resource as
	 * maintained by this store.
	 */
	public String getProperty(IResource target, QualifiedName name) throws CoreException {
		PropertyStore store = getPropertyStore(target);
		synchronized (store) {
			assertRunning(target, store);
			StoredProperty result = store.get(getPropertyKey(target), name);
			return result == null ? null : result.getStringValue();
		}
	}

	/**
	 * Returns the resource which hosts the property store
	 * for the given resource.
	 */
	private Resource getPropertyHost(IResource target) {
		return (Resource) (target.getType() == IResource.ROOT ? target : target.getProject());
	}

	/**
	 * Returns the key to use in the property store when accessing
	 * the properties of the given resource.
	 */
	private ResourceName getPropertyKey(IResource target) {
		return new ResourceName("", target.getProjectRelativePath()); //$NON-NLS-1$
	}

	PropertyStore getPropertyStore(IResource target) throws CoreException {
		return getPropertyStore(target, true);
	}

	/**
	 * Returns the property store to use when storing a property for the 
	 * given resource.  
	 * @throws CoreException if the store could not be obtained for any reason.
	 */
	PropertyStore getPropertyStore(IResource target, boolean createIfNeeded) throws CoreException {
		try {
			Resource host = getPropertyHost(target);
			ResourceInfo info = host.getResourceInfo(false, false);
			if (info == null) {
				String message = NLS.bind(CompatibilityMessages.properties_storeNotAvailable, target.getFullPath());
				throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, target.getFullPath(), message, null);
			}
			PropertyStore store = (PropertyStore) info.getPropertyStore();
			if (store == null)
				store = openPropertyStore(host, createIfNeeded);
			return store;
		} catch (Exception e) {
			if (e instanceof CoreException)
				throw (CoreException) e;
			String message = NLS.bind(CompatibilityMessages.properties_storeNotAvailable, target.getFullPath());
			throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, target.getFullPath(), message, e);
		}
	}

	/**
	 * Returns the property store to use when storing a property for the 
	 * given resource, or null if the store is not available.  
	 */
	private PropertyStore getPropertyStoreOrNull(IResource target) {
		Resource host = getPropertyHost(target);
		ResourceInfo info = host.getResourceInfo(false, false);
		if (info != null) {
			PropertyStore store = (PropertyStore) info.getPropertyStore();
			if (store != null) {
				//sync on the store in case of concurrent deletion
				synchronized (store) {
					if (store.isRunning())
						return store;
				}
			}
		}
		return null;
	}

	public void handleEvent(LifecycleEvent event) throws CoreException {
		if (event.kind == LifecycleEvent.PRE_PROJECT_CLOSE)
			closePropertyStore(event.resource);
	}

	private PropertyStore openPropertyStore(IResource target, boolean createIfNeeded) {
		int type = target.getType();
		Assert.isTrue(type != IResource.FILE && type != IResource.FOLDER);
		IPath location = workspace.getMetaArea().getPropertyStoreLocation(target);
		java.io.File storeFile = location.toFile();
		if (!createIfNeeded && !storeFile.isFile())
			return null;
		storeFile.getParentFile().mkdirs();
		PropertyStore store = new PropertyStore(location);
		setPropertyStore(target, store);
		return store;
	}

	public void setProperty(IResource target, QualifiedName key, String value) throws CoreException {
		PropertyStore store = getPropertyStore(target);
		synchronized (store) {
			assertRunning(target, store);
			if (value == null) {
				store.remove(getPropertyKey(target), key);
			} else {
				StoredProperty prop = new StoredProperty(key, value);
				store.set(getPropertyKey(target), prop);
			}
			store.commit();
		}
	}

	private void setPropertyStore(IResource target, PropertyStore value) {
		// fetch the info but don't bother making it mutable even though we are going
		// to modify it.  We don't know whether or not the tree is open and it really doesn't
		// matter as the change we are doing does not show up in deltas.
		ResourceInfo info = getPropertyHost(target).getResourceInfo(false, false);
		if (info.getType() == IResource.PROJECT)
			((ProjectInfo) info).setPropertyStore(value);
		else
			((RootInfo) info).setPropertyStore(value);
	}

	public void shutdown(IProgressMonitor monitor) throws CoreException {
		closePropertyStore(workspace.getRoot());
	}

	public void startup(IProgressMonitor monitor) throws CoreException {
		workspace.addLifecycleListener(this);
	}

	public Map getProperties(IResource resource) throws CoreException {
		PropertyStore store = getPropertyStore(resource);
		if (store == null)
			return Collections.EMPTY_MAP;
		// retrieves the properties for the selected resource
		IPath path = resource.getProjectRelativePath();
		ResourceName resourceName = new ResourceName("", path); //$NON-NLS-1$
		QueryResults results = store.getAll(resourceName, 1);
		List projectProperties = results.getResults(resourceName);
		int listSize = projectProperties.size();
		if (listSize == 0)
			return Collections.EMPTY_MAP;
		Map properties = new HashMap();
		for (int i = 0; i < listSize; i++) {
			StoredProperty prop = (StoredProperty) projectProperties.get(i);
			properties.put(prop.getName(), prop.getStringValue());
		}
		return properties;
	}

}
