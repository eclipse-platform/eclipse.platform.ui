package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
import java.net.URL;

/**
 * See IPluginRegistry for original
 *
 * The UM registry holds the master list of all
 * installed plug-ins, components, and products
 * in an Eclipse tree.
 * <p>
 * The UM registry can be queried, by name, for 
 * plug-ins, components, and products.
 * </p>
 */
public interface IUMRegistry {
/**
 * Returns all component descriptors known to this registry, entries can
 * have duplicate component IDs but at different versions.
 * Returns an empty array if there are no installed components.
 *
 * @return all component descriptors, including all versions, known to this registry
 */
public IComponentDescriptor[] getAllComponentDescriptors();
/**
 * Returns all plugin entry descriptors known to this registry, entries can
 * have duplicate plugin IDs but at different versions.
 * Returns an empty array if there are no installed plugins.
 *
 * @return all plugin descriptors, including all versions, known to this registry
 */
public IPluginEntryDescriptor[] getAllPluginEntryDescriptors();
/**
 * Returns all product descriptors known to this registry, entries can
 * have duplicate product IDs but at different versions.
 * Returns an empty array if there are no installed products.
 *
 * @return all product descriptors, including all versions, known to this registry
 */
public IProductDescriptor[] getAllProductDescriptors();
/**
 * Returns the component descriptor with the given component identifier
 * at the latest version number in this registry, or <code>null</code> 
 * if there is no such component.
 *
 * @param componentId the unique identifier of the component
 * @return the component descriptor, or <code>null</code>
 */
public IComponentDescriptor getComponentDescriptor(String componentId);
/**
 * Returns the component descriptor with the given component identifier
 * and version number in this registry, or <code>null</code> if there is no 
 * such component.   If a version number is not specified (null), the latest
 * version of such component will be returned
 *
 * @param compId the unique identifier of the component 
 * @param version the version number
 * @return the component descriptor at the specified version number, or <code>null</code>
 */
public IComponentDescriptor getComponentDescriptor(String compId, String version);
/**
 * Returns all component descriptors known to this registry.
 * Due to duplicate component IDs, the latest version of each descriptor
 * is returned.
 * Returns an empty array if there are no installed components.
 *
 * @return the component descriptors at their latest version known to this registry
 */
public IComponentDescriptor[] getComponentDescriptors();
/**
 * Returns an array of manifests whose conditions state that the given
 * manifest should not be installed.
 * @return org.eclipse.core.internal.boot.update.IManifestDescriptor[]
 * @param manifestDescriptor org.eclipse.core.internal.boot.update.IManifestDescriptor
 */
IManifestDescriptor[] getConflictingManifests(IManifestDescriptor manifestDescriptor);
// return a list of components that don't belong to any products

public IComponentDescriptor[] getDanglingComponents() ;
/**
 * Returns the plug-in entry descriptor with the given plug-in identifier
 * at the latest version number in this registry, or <code>null</code> if there is no such
 * plug-in.
 *
 * @param pluginId the unique identifier of the plug-in (e.g. <code>"com.example.myplugin"</code>).
 * @return the plug-in entry descriptor, or <code>null</code>
 */
public IPluginEntryDescriptor getPluginEntryDescriptor(String pluginId);
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
public IPluginEntryDescriptor getPluginEntryDescriptor(String pluginId, String version);
/**
 * Returns all plug-in entry descriptors known to this registry.
 * Due to duplicate plugin IDs, the latest version of each descriptor
 * is returned.
 * Returns an empty array if there are no installed plug-ins.
 *
 * @return the plug-in entry descriptors at their latest version known to this registry
 */
public IPluginEntryDescriptor[] getPluginEntryDescriptors();
/**
 * Returns the Product descriptor with the given Product identifier
 * at the latest version number in this registry, or <code>null</code> if there is no such
 * Product.
 *
 * @param productId the unique identifier of the Product .
 * @return the Product descriptor, or <code>null</code>
 */
public IProductDescriptor getProductDescriptor(String productId);
/**
 * Returns the product descriptor with the given product identifier
 * and version number in this registry, or <code>null</code> if there is no 
 * such product.   If a version number is not specified (null), the latest
 * version of such product will be returned
 *
 * @param prodId the unique identifier of the product .
 * @param version the version number
 * @return the product descriptor at the specified version number, or <code>null</code>
 */
public IProductDescriptor getProductDescriptor(String prodId, String version);
/**
 * Returns all Product descriptors known to this registry.
 * Due to duplicate product IDs, the latest version of each descriptor
 * is returned.
 * Returns an empty array if there are no installed Products.
 *
 * @return the Product descriptors at their latest version known to this registry
 */
public IProductDescriptor[] getProductDescriptors();
/**
 * Returns the base URL of the eclipse tree this registry is representing
 *
 * @return the base URL of this eclipse tree
 */
public URL getRegistryBaseURL();
}
