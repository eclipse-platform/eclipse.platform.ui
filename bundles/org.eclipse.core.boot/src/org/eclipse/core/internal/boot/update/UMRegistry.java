package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
import org.eclipse.core.internal.boot.LaunchInfo;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;

public class UMRegistry extends UMRegistryModel implements IUMRegistry {



/**
 * UMRegistry constructor comment.
 */
public UMRegistry() {
	super();
}
/**
 * Returns all component descriptors known to this registry, entries can
 * have duplicate component IDs but at different versions.
 * Returns an empty array if there are no installed components.
 *
 * @return all component descriptors known to this registry
 */
public IComponentDescriptor[] getAllComponentDescriptors() {
	Vector comp_list = _getAllComponents();

	int size;
	if (comp_list == null) size = 0;
	else size = comp_list.size();
	if(size == 0) return new IComponentDescriptor[0];
	
	IComponentDescriptor[] array = new IComponentDescriptor[size];
	Enumeration list = comp_list.elements();
		for(int i=0; list.hasMoreElements(); i++) {
			array[i] = (IComponentDescriptor) list.nextElement();
		}
	
	return array;
	
}
/**
 * Returns all plugin entry descriptors known to this registry, entries can
 * have duplicate plugin IDs but at different versions.
 * Returns an empty array if there are no installed plugins.
 *
 * @return all plugin descriptors, including all versions, known to this registry
 */
public IPluginEntryDescriptor[] getAllPluginEntryDescriptors() {
	Vector plugins_list = _getAllPluginEntries();

	int size;
	if (plugins_list == null) size = 0;
	else size = plugins_list.size();
	if(size == 0) return new IPluginEntryDescriptor[0];
	
	IPluginEntryDescriptor[] array = new IPluginEntryDescriptor[size];
	Enumeration list = plugins_list.elements();
		for(int i=0; list.hasMoreElements(); i++) {
			array[i] = (IPluginEntryDescriptor) list.nextElement();
		}
	
	return array;
}
/**
 * Returns all product descriptors known to this registry, entries can
 * have duplicate product IDs but at different versions.
 * Returns an empty array if there are no installed products.
 *
 * @return all product descriptors, including all versions, known to this registry
 */
public IProductDescriptor[] getAllProductDescriptors() {
	Vector prod_list = _getAllProducts();

	int size;
	if (prod_list == null) size = 0;
	else size = prod_list.size();
	if(size == 0) return new IProductDescriptor[0];
	
	IProductDescriptor[] array = new IProductDescriptor[size];
	Enumeration list = prod_list.elements();
		for(int i=0; list.hasMoreElements(); i++) {
			array[i] = (IProductDescriptor) list.nextElement();
		}
	
	return array;
	
}
/**
 * Returns the component descriptor with the given component identifier
 * at the latest version number in this registry, or <code>null</code> 
 * if there is no such component.
 *
 * @param componentId the unique identifier of the component (e.g. <code>"SGD8-TR62-872F-AFCD"</code>).
 * @return the component descriptor, or <code>null</code>
 */
public IComponentDescriptor getComponentDescriptor(java.lang.String componentId) {
	return getComponentDescriptor(componentId, null);
}
/**
 * Returns the component descriptor with the given component identifier
 * and version number in this registry, or <code>null</code> if there is no 
 * such component.   If a version number is not specified (null), the latest
 * version of such component will be returned
 *
 * @param compId the unique identifier of the component (e.g. <code>"SGD8-TR62-872F-AFCD"</code>).
 * @return the component descriptor at the specified version number, or <code>null</code>
 */
public IComponentDescriptor getComponentDescriptor(String compId, String version) {
	return (IComponentDescriptor)_lookupComponentDescriptor(compId,version);
}
/**
 * Returns all component descriptors known to this registry.
 * Due to duplicate component IDs, the latest version of each descriptor
 * is returned.
 * Returns an empty array if there are no installed components.
 *
 * @return the component descriptors at their latest version known to this registry
 */
public IComponentDescriptor[] getComponentDescriptors() {
	Hashtable comp_list = _getComponentsAtLatestVersion();

	int size;
	if (comp_list == null) size = 0;
	else size = comp_list.size();
	if(size == 0) return new IComponentDescriptor[0];
	
	IComponentDescriptor[] array = new IComponentDescriptor[size];
	Enumeration list = comp_list.elements();
		for(int i=0; list.hasMoreElements(); i++) {
			array[i] = (IComponentDescriptor) list.nextElement();
		}
	
	return array;
	
}
/**
 * Returns an array of manifests whose conditions state that the given
 * manifest should not be installed.  Conditions such as upgradable are checked.
 *
 * @return org.eclipse.core.internal.boot.update.IManifestDescriptor[]
 * @param manifestDescriptor org.eclipse.core.internal.boot.update.IManifestDescriptor
 */
public IManifestDescriptor[] getConflictingManifests(IManifestDescriptor manifestDescriptor) {
	Vector naySayers = new Vector();
	int result = UpdateManagerConstants.OK_TO_INSTALL;
	
	if (manifestDescriptor instanceof IProductDescriptor) {
		IProductDescriptor currentProd = getProductDescriptor(manifestDescriptor.getUniqueIdentifier());
		result = ((IProductDescriptor)manifestDescriptor).isInstallable(currentProd);
		if ((result == UpdateManagerConstants.NOT_NEWER) || (result == UpdateManagerConstants.NOT_COMPATIBLE))
			naySayers.addElement(currentProd);
		if (result == UpdateManagerConstants.NOT_UPDATABLE)
			; // none for Products right now
	
	} else if (manifestDescriptor instanceof IComponentDescriptor) {
		IComponentDescriptor currentComp = getComponentDescriptor(manifestDescriptor.getUniqueIdentifier());
		result = ((IComponentDescriptor)manifestDescriptor).isInstallable(currentComp);
		if ((result == UpdateManagerConstants.NOT_NEWER) || (result == UpdateManagerConstants.NOT_COMPATIBLE))
			naySayers.addElement(currentComp);
		if (result == UpdateManagerConstants.NOT_UPDATABLE)
			; // LINDA - look up which product not allowing this
	}

	IManifestDescriptor[] array = new IManifestDescriptor[naySayers.size()];
	naySayers.copyInto(array);
	return array;

}
// return a list of components that don't belong to any products

public IComponentDescriptor[] getDanglingComponents() {

	Vector comp_list = new Vector();

	Enumeration list = _getAllComponents().elements();
	for(int i=0; list.hasMoreElements(); i++) {
		IComponentDescriptor comp = (IComponentDescriptor) list.nextElement();
		LaunchInfo.VersionedIdentifier vid = new LaunchInfo.VersionedIdentifier(comp.getUniqueIdentifier(), comp.getVersionStr());
		if (LaunchInfo.getCurrent().isDanglingComponent(vid)) {
			comp_list.add(comp);
		} else {	// sometimes the LaunchInfo list might be out-of-sync
			IProductDescriptor[] prod = comp.getContainingProducts();
			if (prod.length == 0) {
				comp_list.add(comp);
				LaunchInfo.getCurrent().isDanglingComponent(vid, true); //add
			}
		}
	}


			
	IComponentDescriptor[] array = new IComponentDescriptor[comp_list.size()];
	comp_list.copyInto(array);
	return array;
	
}
/**
 * Returns the plug-in entry descriptor with the given plug-in identifier
 * at the latest version number in this  registry, or <code>null</code> if there is no such
 * plug-in.
 *
 * @param pluginId the unique identifier of the plug-in (e.g. <code>"com.example.myplugin"</code>).
 * @return the plug-in entry descriptor, or <code>null</code>
 */
public IPluginEntryDescriptor getPluginEntryDescriptor(String pluginId) {
	return getPluginEntryDescriptor(pluginId, null);
}
/**
 * Returns the plug-in entry descriptor with the given plug-in identifier
 * and version number in this registry, or <code>null</code> if there is no such
 * plug-in.   If a version number is not specified (null), the latest
 * version of such plug-in will be returned
 *
 * @param pluginId the unique identifier of the plug-in (e.g. <code>"com.example.myplugin"</code>).
 * @param version the version number
 * @return the plug-in entry descriptor at the specified version number, or <code>null</code>
 */
public IPluginEntryDescriptor getPluginEntryDescriptor(java.lang.String pluginId, java.lang.String version) {
	return (IPluginEntryDescriptor)_lookupPluginEntryDescriptor(pluginId,version);
}
/**
 * Returns all plug-in entry descriptors known to this registry.
 * Due to duplicate plugin IDs, the latest version of each descriptor
 * is returned.
 * Returns an empty array if there are no installed plug-ins.
 *
 * @return the plug-in entry descriptors at their latest versions known to this registry
 */
public IPluginEntryDescriptor[] getPluginEntryDescriptors() {
	Hashtable plugins_list = _getPluginEntriesAtLatestVersion();

	int size;
	if (plugins_list == null) size = 0;
	else size = plugins_list.size();
	if(size == 0) return new IPluginEntryDescriptor[0];
	
	IPluginEntryDescriptor[] array = new IPluginEntryDescriptor[size];
	Enumeration list = plugins_list.elements();
		for(int i=0; list.hasMoreElements(); i++) {
			array[i] = (IPluginEntryDescriptor) list.nextElement();
		}
	
	return array;
	
}
/**
 * Returns the Product descriptor with the given Product identifier
 * at the latest version number in this registry, or <code>null</code> if there is no such
 * Product.
 *
 * @param prodId the unique identifier of the Product (e.g. <code>"SGD8-TR62-AABB-AFCD"</code>).
 * @return the Product descriptor, or <code>null</code>
 */
public IProductDescriptor getProductDescriptor(String prodId) {
	return getProductDescriptor(prodId, null);
}
/**
 * Returns the product descriptor with the given product identifier
 * and version number in this registry, or <code>null</code> if there is no 
 * such product.   If a version number is not specified (null), the latest
 * version of such product will be returned
 *
 * @param prodId the unique identifier of the product (e.g. <code>"SGD8-TR62-872F-AFCD"</code>).
 * @param version the version number
 * @return the product descriptor at the specified version number, or <code>null</code>
 */
public IProductDescriptor getProductDescriptor(java.lang.String prodId, java.lang.String version) {
	return (IProductDescriptor)_lookupProductDescriptor(prodId,version);
}
/**
 * Returns all product descriptors known to this registry.
 * Due to duplicate product IDs, the latest version of each descriptor
 * is returned.
 * Returns an empty array if there are no installed products.
 *
 * @return the product descriptors at their latest versions known to this registry
 */
public IProductDescriptor[] getProductDescriptors() {
	Hashtable prod_list = _getProductsAtLatestVersion();

	int size;
	if (prod_list == null) size = 0;
	else size = prod_list.size();
	if(size == 0) return new IProductDescriptor[0];
	
	IProductDescriptor[] array = new IProductDescriptor[size];
	Enumeration list = prod_list.elements();
		for(int i=0; list.hasMoreElements(); i++) {
			array[i] = (IProductDescriptor) list.nextElement();
		}
	
	return array;
	
	
}
/**
 * Returns the base URL of the eclipse tree this registry is representing
 *
 * @return the base URL of this eclipse tree
 */
public java.net.URL getRegistryBaseURL() {
	return _getRegistryBase();
}
}
