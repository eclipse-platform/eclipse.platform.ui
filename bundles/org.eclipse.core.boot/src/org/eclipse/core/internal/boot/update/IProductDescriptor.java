package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
import java.net.*;

public interface IProductDescriptor extends IManifestDescriptor{
/**
 * A Product descriptor contains information about a Product
 * obtained from the Product's jar manifest file.
 * <p>
 * Product descriptors are stored in the UM Registry created for an
 * install tree.
 * 
 */


 public Object clone() throws CloneNotSupportedException; 
/**
 * Return +1 if I am newer than the argument component
 * Return  0 if I am equal to   the argument component
 * Return -1 if I am older than the argument component
 *
 * @param prod org.eclipse.update.internal.core.IProductDescriptor
 */
int compare(IProductDescriptor prod);
/**
 * Returns whether the component or configuration described by this descriptor
 * is the same as the one passed in.  The following are checked:
 * 
 * - they have the same version identifier, AND
 * - they have the same unique identifier
 *
 * @return <code>true</code> if this product is the same as the one passed in, and
 *   <code>false</code> otherwise
 */
public boolean equals(IProductDescriptor comp);
/**
 * Returns the application specified by this Product
 * Returns the empty string if no application for this Product
 * is specified in its install manifest file.
 *
 * @return the application specified by this Product, can be an empty string
 */
public String getApplication();
/**
 * Returns a list of components shipped in this product.
 * These are the entries specified in the product jar manifest file
 *
 * @return an array of component shipped in this product
 */
public IComponentEntryDescriptor[] getComponentEntries();
/**
 * Returns the component entry with the given identifier shipped in
 * this product, or <code>null</code> if there is no component entry.
 *
 * @param id the identifier of the component (e.g. <code>""</code>).
 * @return the component entry, or <code>null</code>
 */
public IComponentEntryDescriptor getComponentEntry(String id);
/**
 * Returns a description of this Product
 * Returns the empty string if no label for this Product
 * is specified in its jar manifest file.
 *
 * @return a description of this Product, can be an empty string
 */
public String getDescription();
/**
 * Returns the name of the directory of this product in .install/.products
 * This is usually made up of prodid_label_version
 *
 *
 * @return the product's directory name in .install/.products
 */
public String getDirName();
/**
 * Returns the URL where a download of updates to this Product can be found
 *
 * @return the URL where a download of updates to this Product can be found
 */
public URL getDownloadURL() ;
/**
 * Returns the name of the vendor of this Product.
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
 * Returns whether all nested children of this configuration is installed (as some could have
 * been omitted if they were optional and the user did not install them at the time)
 *
 * This method returns <code>true</code> if
 * all nested children of this configuration is installed 
 */
public boolean isAllInstalled();
/**
 * Returns whether the product described by this descriptor
 * is the dominant application in the current LaunchInfo
 *
 * @return <code>true</code> if this product is the dominant application, and
 *  <code>false</code> otherwise
 * 
 */
public boolean isDominantApp();
/**
 * Checks all the upgrade rules to determine if this product can be upgraded
 * - isUpdateable() - checking that an updateURL is specified for it
 * - param product.isNewerThan(IProductDescriptor self)
 * - since this is a top-level configuration, there is no restriction on upgrades as long as 
 *   the new prod is newer
 * @return whether the product can be upgraded to a newer version.
 */
public int isInstallable(IProductDescriptor prod);
/**
 * Returns whether the component or configuration described by this descriptor
 * can be removed or not.  The following conditions are checked:
 *
 * - It cannot be the dominant application in the current LaunchInfo
 * - All component entries that are not dangling must have no other 
 *   product containing them
 *
 * @return <code>true</code> if this product is removable, and
 *   <code>false</code> otherwise
 */
public boolean isRemovable();
/**
 * Returns whether the product described by this descriptor
 * can be updated or not.   Currently no condition is checked.
 *
 * @return <code>true</code> if this product is updateable, and
 *   <code>false</code> otherwise
 * @see #getUpdateURL
 */
public boolean isUpdateable();
}
