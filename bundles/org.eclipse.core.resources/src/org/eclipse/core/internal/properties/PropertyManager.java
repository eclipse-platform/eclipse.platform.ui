package org.eclipse.core.internal.properties;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.localstore.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.*;
import java.io.*;
import java.util.*;
/**
 *
 */
public class PropertyManager implements IManager {
	protected Workspace workspace;
public PropertyManager(Workspace workspace) {
	this.workspace = workspace;
}
public void changing(IProject target) {
}
public void closePropertyStore(IResource target) throws CoreException {
	Resource host = (Resource) getPropertyHost(target);
	ResourceInfo info = (ResourceInfo) host.getResourceInfo(false, false);
	if (info == null)
		return;
	PropertyStore store = info.getPropertyStore();
	if (store == null)
		return;
	store.shutdown(null);
	setPropertyStore(target, null);
}
public void closing(IProject target) throws CoreException {
	closePropertyStore(target);
}
/**
 * Copy all the properties of one resource to another. Both resources
 * must have a property store available.
 */
public void copy(IResource source, IResource destination, int depth) throws CoreException {
	copyProperties(source, destination, depth);
	// cache stores to avoid problems in concurrency
	PropertyStore sourceStore = getPropertyStore(source);
	PropertyStore destinationStore = getPropertyStore(destination);
	sourceStore.commit();
	destinationStore.commit();
}
protected void copyProperties(IResource source, IResource destination, int depth) throws CoreException {
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
public void deleteProperties(IResource target) throws CoreException {
	deleteProperties(target, IResource.DEPTH_INFINITE);
}
public void deleteProperties(IResource target, int depth) throws CoreException {
	switch (target.getType()) {
		case IResource.FILE :
		case IResource.FOLDER :
			PropertyStore store = getPropertyStore(target);
			store.removeAll(getPropertyKey(target), depth);
			store.commit();
			break;
		case IResource.PROJECT :
		case IResource.ROOT :
			deletePropertyStore(target);
	}
}
protected void deletePropertyStore(IResource target) throws CoreException {
	closePropertyStore(target);
	workspace.getMetaArea().getPropertyStoreLocation(target).toFile().delete();
}
public void deleting(IProject project) {
}
/**
 * Returns the value of the identified property on the given resource as
 * maintained by this store.
 */
public String getProperty(IResource target, QualifiedName name) throws CoreException {
	PropertyStore store = getPropertyStore(target);
	StoredProperty result = store.get(getPropertyKey(target), name);
	return result == null ? null : result.getStringValue();
}
/**
 * Returns the resource which hosts the property store
 * for the given resource.
 */
protected IResource getPropertyHost(IResource target) {
	return target.getType() == IResource.ROOT ? target : target.getProject();
}
/**
 * Returns the key to use in the property store when accessing
 * the properties of the given resource.
 */
protected ResourceName getPropertyKey(IResource target) {
	return new ResourceName("", target.getProjectRelativePath());
}
/**
 * Returns the property store to use when storing a property for the 
 * given resource.  
 * @throws CoreException if the store could not be obtained for any reason.
 */
protected PropertyStore getPropertyStore(IResource target) throws CoreException {
	try {
		Resource host = (Resource) getPropertyHost(target);
		ResourceInfo info = (ResourceInfo) host.getResourceInfo(false, false);
		if (info == null) {
			String message = Policy.bind("properties.storeNotAvaiable", target.getFullPath().toString());
			throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, target.getFullPath(), message, null);
		}
		PropertyStore store = info.getPropertyStore();
		if (store == null)
			store = openPropertyStore(host);
		return store;
	} catch (Exception e) {
		if (e instanceof CoreException)
			throw (CoreException)e;
		String message = Policy.bind("properties.storeNotAvaiable", target.getFullPath().toString());
		throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, target.getFullPath(), message, e);
	}
}
public void opening(IProject target) throws CoreException {
}
protected PropertyStore openPropertyStore(IResource target) {
	int type = target.getType();
	Assert.isTrue(type != IResource.FILE && type != IResource.FOLDER);
	IPath location = workspace.getMetaArea().getPropertyStoreLocation(target);
	java.io.File storeFile = location.toFile();
	new java.io.File(storeFile.getParent()).mkdirs();
	PropertyStore store = new PropertyStore(location);
	setPropertyStore(target, store);
	return store;
}
public void setProperty(IResource target, QualifiedName key, String value) throws CoreException {
	PropertyStore store = getPropertyStore(target);
	if (value == null) {
		store.remove(getPropertyKey(target), key);
	} else {
		StoredProperty prop = new StoredProperty(key, value);
		store.set(getPropertyKey(target), prop);
	}
	store.commit();
}
protected void setPropertyStore(IResource target, PropertyStore value) {
	// fetch the info but don't bother making it mutable even though we are going
	// to modify it.  We don't know whether or not the tree is open and it really doesn't
	// matter as the change we are doing does not show up in deltas.
	ResourceInfo info = ((Resource) getPropertyHost(target)).getResourceInfo(false, false);
	if (info.getType() == IResource.PROJECT)
		 ((ProjectInfo) info).setPropertyStore(value);
	else
		 ((RootInfo) info).setPropertyStore(value);
}
public void shutdown(IProgressMonitor monitor) throws CoreException {
	closePropertyStore(workspace.getRoot());
}
public void startup(IProgressMonitor monitor) throws CoreException {
}
}
