package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
 
import java.net.*;
import java.util.*;

public interface IComponentDescriptor extends IManifestDescriptor {
/**
 * A Component descriptor contains information about a component
 * obtained from the component's jar manifest file.
 * <p>
 * Component descriptors are stored in the UM Registry created for an
 * install tree.
 * 
 */


 public Object clone() throws CloneNotSupportedException; 
/**
 * Return +1 if I am newer than the argument component
 * Return  0 if I am equal to   the argument component
 * Return -1 if I am older than the argument component
 */
public int compare(IComponentDescriptor comp);
/**
 * Returns whether the component or configuration described by this descriptor
 * is the same as the one passed in.  The following are checked:
 * 
 * - they have the same version identifier, AND
 * - they have the same unique identifier
 *
 * @return <code>true</code> if this component is the same as the one passed in, and
 *   <code>false</code> otherwise
 */
public boolean equals(IComponentDescriptor comp);
/**
 * Returns the list of URL-Name pairs where new items related to this Component can be found
 * Included are discovery URL-Name pairs specified by the products this component is
 * part of.
 *
 * @return the discovery URL-Name pairs of this component, including those specified by containing products
 */
public IURLNamePair[] getAllDiscoveryURLs();
/**
 * Returns the list of URL-Name pairs where updates to this component can be found.
 * Included are update URL-Name pairs specified by the products this component is
 * part of.
 *
 * @return the update URL-Name pairs of this component, including those specified by containing products
 */
public IURLNamePair[] getAllUpdateURLs();
/**
 * Returns a list of products that shipped this component.
 * A component can be shipped with one or more products, and the user
 * may install products that contain components that are already installed.
 *
 * @return an array of products that shipped this component
 */
public IProductDescriptor[] getContainingProducts();
/**
 * Returns a description of this component
 * Returns the empty string if no label for this component
 * is specified in its jar manifest file.
 *
 * @return a description of this component, can be an empty string
 */
public String getDescription();
/**
 * Returns the name of the directory of this component in install/components
 * The format is compid_version
 *
 *
 * @return the component's directory name in install/components
 */
public String getDirName();
/**
 * Returns the URL of this component's jar
 *
 * @return the URL of this component's jar
 */

public URL getDownloadURL() ;
/**
 * Returns a list of fragments shipped in this component.
 * These are the entries specified in the component jar manifest file
 *
 * @return an array of fragments shipped in this component
 */
public IFragmentEntryDescriptor[] getFragmentEntries();
/**
 * Returns a list of plug-ins shipped in this component.
 * These are the entries specified in the component jar manifest file
 *
 * @return an array of plug-ins shipped in this component
 */
public IPluginEntryDescriptor[] getPluginEntries();
/**
 * Returns the name of the vendor of this component.
 * Returns the empty string if no vendor name is specified in 
 * the manifest file.
 * <p> Note that any translation specified in the manifest
 * file is automatically applied.
 * </p>
 *
 * @see #getResourceString 
 *
 * @return the name of the vendor, possibly the empty string
 */
public String getProviderName();
/**
 * Returns the Registry this component belongs to
 *
 * @return the Registry this component belongs to
 */

public IUMRegistry getUMRegistry() ;
/**
 * Returns whether the component described by this descriptor
 * belongs to a product or not.
 *
 * @return <code>true</code> if this component does not belong to any 
 * product configuration, and
 *   <code>false</code> otherwise
 */
public boolean isDanglingComponent();
/**
 * Checks all the upgrade rules to determine if this remote component can be
 * downloaded and installed:
 *
 * - isUpdateable() - checking that all containing products allow this component to be upgraded
 * - if compInstalled is a dangling component 
 *       compare(IComponentDescriptor compInstalled) >= 0
 *   else
 *       isCompatibleWith(compInstalled.getVersionIdentifier())
 *
 * @return whether this remote component can be downloaded and installed.
 */
public int isInstallable(IComponentDescriptor comp);
/**
 * Returns whether the component or configuration described by this descriptor
 * can be removed or not.  The following condition is checked:
 * 
 * - It has no other containing products/configurations 
 *
 * @return <code>true</code> if this component is removable, and
 *   <code>false</code> otherwise
 */
public boolean isRemovable();
/**
 * Returns whether the component or configuration described by this descriptor
 * can be removed or not.  The following condition is checked:
 * 
 * - It has no other containing products/configurations 
 *
 * @param containingProd the product being removed causing this component to be
 *        a candidate for removal as well
 * @return <code>true</code> if this component is removable, and
 *   <code>false</code> otherwise
 */
public boolean isRemovable(IProductDescriptor containingPrid);
/**
 * Returns whether the component described by this descriptor
 * can be updated or not.  The following condition is checked:
 * 
 * - All its containing products allow the component to be updated
 *
 * @return <code>true</code> if this component is updateable, and
 *   <code>false</code> otherwise
 */
public boolean isUpdateable();
}
