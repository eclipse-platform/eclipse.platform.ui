package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */

import org.eclipse.core.internal.boot.LaunchInfo;
import java.net.*;
import java.util.*;

public class ProductDescriptor extends ProductDescriptorModel implements IProductDescriptor {

	private NLResourceHelper fNLHelper = null;
	private URL				_downloadURL;
/**
 * ProductDescriptor constructor comment.
 */
public ProductDescriptor() {
	super();
	
}
 public Object clone() throws CloneNotSupportedException {
	ComponentDescriptor clone = (ComponentDescriptor)super.clone();

	return clone;

}
/**
 *
 * @param prod org.eclipse.update.internal.core.IProductDescriptor
 */
public int compare(IProductDescriptor prod) {
	
	// Return +1 if I am newer than the argument product
	// Return  0 if I am equal to   the argument product
	// Return -1 if I am older than the argument product
	//----------------------------------------------------
	return new VersionComparator().compare(getVersionStr(), prod.getVersionStr());
}
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
public boolean equals(IProductDescriptor prod) {
	String id = prod.getUniqueIdentifier();

	if ((compare(prod) == 0) && id.equals(this.getUniqueIdentifier()))
		return true;
	return false;
}
/**
 * Returns the application specified by this Product
 * Returns the empty string if no application for this Product
 * is specified in its install manifest file.
 *
 * @return the application specified by this Product, can be an empty string
 */
public java.lang.String getApplication() {
	String s = _getApplication();
	return s==null ? "" : s;
}
/**
 * Returns a list of component entries shipped in this product.  Entries can
 * have duplicate component IDs but at different versions.
 * These are the entries specified in the product install manifest file
 * Note that this does the same as getAllComponentEntries()
 *
 * @return an array of all components shipped in this product
 * 
 */
public IComponentEntryDescriptor[] getComponentEntries() {
	Vector compEntry_list = _getAllComponentEntries();

	int size;
	if (compEntry_list == null) size = 0;
	else size = compEntry_list.size();
	if(size == 0) return new IComponentEntryDescriptor[0];
	
	IComponentEntryDescriptor[] array = new IComponentEntryDescriptor[size];
	Enumeration list = compEntry_list.elements();
		for(int i=0; list.hasMoreElements(); i++) {
			array[i] = (IComponentEntryDescriptor) list.nextElement();
		}
	
	return array;
	
}
/**
 * Returns the component entry with the given identifier at the latest
 * version number shipped in  * this product, or <code>null</code> if 
 * there is no such component entry.
 *
 * @param id the identifier of the component entry (e.g. <code>""</code>).
 * @return the component entry, or <code>null</code>
 */
public IComponentEntryDescriptor getComponentEntry(java.lang.String id) {
	return getComponentEntry(id, null);
}
/**
 * Returns the component entry with the given identifier and
 * version number shipped in this product, or <code>null</code> if 
 * there is no such component entry.  If a version number is not 
 * specified (null), the latest version of such component entry will be returned
 *
 * @param compId the identifier of the component entry (e.g. <code>""</code>).
 * @return the component entry at the specified version number, or <code>null</code>
 */
public IComponentEntryDescriptor getComponentEntry(java.lang.String compId, String version) {
	return (IComponentEntryDescriptor)_lookupComponentEntry(compId,version);
}
/**
 * Returns a description of this Product
 * Returns the empty string if no label for this Product
 * is specified in its jar manifest file.
 *
 * @return a description of this Product, can be an empty string
 */
public java.lang.String getDescription() {
	String s = _getDescription();
	if (fNLHelper == null)
		fNLHelper = new NLResourceHelper( DEFAULT_BUNDLE_NAME,getInstallURL());
	return s==null ? "" : fNLHelper.getResourceString(s);
}
/**
 * Returns the name of the directory of this product in .install/.products
 * This is usually made up of prodid_label_version
 *
 *
 * @return the product's directory name in .install/.products
 */
public java.lang.String getDirName() {
	return _getDirName();
}
/**
 * Returns the Discovery URL that matches the  parameter,
 * or <code>null</code> if there is no such Discovery URL.
 *
 * @param url the Discovery URL to search for (e.g. <code>"http://www.example.com"</code>).
 * @return the Discovery URL, or <code>null</code> if not found
 */
public IURLNamePair getDiscoveryURL(URL url) {
	return	 (IURLNamePair) _lookupDiscoveryURL(url);
}
/**
 * Returns the list of URLs where updates to this Product can be found
 *
 * @return the update sites (URL) of this Product
 */
public IURLNamePair[] getDiscoveryURLs() {
	int size = _getSizeOfDiscoveryURLsRel();	
	if(size == 0) return new IURLNamePair[0];
	
	IURLNamePair[] list = new IURLNamePair[size];
	_copyDiscoveryURLsRelInto(list);
	return list;
}
/**
 * Returns the URL of this product's jar
 *
 * @return the URL of this product's jar
 */
public URL getDownloadURL() {
	try {
		URL download_url = new URL(UMEclipseTree.appendTrailingSlash(getInstallURL()), getDirName() + ".jar");
		return download_url;
	} catch (MalformedURLException e) {
		throw new IllegalStateException(); // unchecked
	}
}
/**
 * Returns the URL of this product or component's install manifest file. 
 * e.g. ..../.install/.components/compid_label_version/install.xml
 *
 * @return the URL of this product or component's install manifest file. 
 */
public java.net.URL getInstallManifestURL() {
	try {
		return UMEclipseTree.appendTrailingSlash(_getInstallManifestURL());
		
	} catch (MalformedURLException e) {
		throw new IllegalStateException(); // unchecked
	}
}
/**
 * Returns the URL of this product/component's install directory. 
 * This is the .install/.components/compid_label_version or
 * .install/.products/prodid_label_version directory where 
 * product and component manifest files are stored.
 *
 * @return the URL of this product or component's install directory
 */
public  URL getInstallURL() {
	try {
		return UMEclipseTree.appendTrailingSlash(_getInstallURL());
		
	} catch (MalformedURLException e) {
		throw new IllegalStateException(); // unchecked
	}
}
/**
 * Returns a displayable label (name) for this Product.
 * Returns the empty string if no label for this Product
 * is specified in its jar manifest file.
 * <p> Note that any translation specified in the jar manifest
 * file is automatically applied.  LINDA
 * </p>
 *
 * @see #getResourceString 
 *
 * @return a displayable string label for this Product,
 *    possibly the empty string
 */
public java.lang.String getLabel() {

	String s = _getName();
	if (fNLHelper == null)
		fNLHelper = new NLResourceHelper( DEFAULT_BUNDLE_NAME,getInstallURL());
	return s==null ? "" : fNLHelper.getResourceString(s);
}
public String getManifestType() {
	String s = _getManifestType();
	return s==null ? "" : s;
}
/**
 * Returns the URL of this component's manifest file
 *
 * @return the URL of this component's manifest file
 */

public URL getManifestURL() {
	URL manifestURL = null;
	try {
		manifestURL = new URL(_getManifestURL());
	} catch (java.net.MalformedURLException e) {
	}
	return manifestURL; 


	
}
public String getManifestVersion() {
	String s = _getManifestVersion();
	return s==null ? "" : s;
}
/**
 * Returns the name of the vendor of this Product.
 * Returns the empty string if no vendor name is specified in 
 * the manifest file.
 * <p> Note that any translation specified in the manifest
 * file is automatically applied. LINDA
 * </p>
 *
 * @see #getResourceString 
 *
 * @return the name of the vendor, possibly the empty string
 */
public java.lang.String getProviderName() {
	String s = _getProviderName();
	if (fNLHelper == null)
		fNLHelper = new NLResourceHelper( DEFAULT_BUNDLE_NAME,getInstallURL());
	return s==null ? "" : fNLHelper.getResourceString(s);
}
/**
 * getUMRegistry method comment.
 */
public IUMRegistry getUMRegistry() {
	return (IUMRegistry)_getUMRegistry();
}
/**
 * Returns the unique identifier of this product.
 * This identifier is a non-empty string and is unique 
 * within the UM registry.
 *
 * @return the unique identifier of the product (e.g. <code>"SGD8-TR62-AABB-AFCD"</code>)
 */
public String getUniqueIdentifier() {
	
	return _getId();
}
/**
 * Returns the Update URL that matches the  parameter,
 * or <code>null</code> if there is no such Update URL.
 *
 * @param url the Update URL to search for (e.g. <code>"http://www.example.com"</code>).
 * @return the Update URL, or <code>null</code> if not found
 */
public IURLNamePair getUpdateURL(java.net.URL url) {
	return	 (IURLNamePair) _lookupUpdateURL(url);
}
/**
 * Returns the list of URLs where updates to this Product can be found
 *
 * @return the update sites (URL) of this Product
 */
public IURLNamePair[] getUpdateURLs() {
	int size = _getSizeOfUpdateURLsRel();	
	if(size == 0) return new IURLNamePair[0];
	
	IURLNamePair[] list = new IURLNamePair[size];
	_copyUpdateURLsRelInto(list);
	return list;
}
public String getVersion() {

	String s = _getVersion();
	return s==null ? "" : s;
}
/**
 * Returns the Product version identifier.
 *
 * @return the Product version identifier
 */
public VersionIdentifier getVersionIdentifier() {
	try {
		return new VersionIdentifier(getVersion());
	} catch (Throwable e) {
		return new VersionIdentifier(0,0,0);
	}
}
/**
 * Returns the component version string.
 *
 * @return the component version string
 */
public java.lang.String getVersionStr() {
	return _getVersion();
}
/** 
 * Returns whether all nested children of this configuration is installed (as some could have
 * been omitted if they were optional and the user did not install them at the time)
 *
 * This method returns <code>true</code> if
 * all nested children of this configuration is installed 
 */
public boolean isAllInstalled() {
	Enumeration list = _getAllComponentEntries().elements();
	while(list.hasMoreElements()) {
		IComponentEntryDescriptor comp =  (IComponentEntryDescriptor)list.nextElement();
		if (!comp.isInstalled())
			return false;	
	}
	return true;
	
}
/**
 * Returns whether the product described by this descriptor
 * is the dominant application in the current LaunchInfo
 *
 * @return <code>true</code> if this product is the dominant application, and
 *  <code>false</code> otherwise
 * 
 */
public boolean isDominantApp() {
	String app = LaunchInfo.getCurrent().getApplicationConfigurationIdentifier();
	if (app.equals(getUniqueIdentifier()))
		return true;
	return false;
}
/**
 * Checks all the upgrade rules to determine if this remote product can be downloaded and installed
 * - isUpdateable()
 * - compare(prodInstalled) > 0
 * - since this is a top-level configuration, there is no restriction on upgrades as long as 
 *   the new prod is newer
 * @return whether this remote product can be downloaded and installed
 */
public int isInstallable(IProductDescriptor prodInstalled) {
	// Ok if product is not currently installed
	//-----------------------------------------
	if (prodInstalled == null)
		return UpdateManagerConstants.OK_TO_INSTALL;
		
	if (!isUpdateable())
		return UpdateManagerConstants.NOT_UPDATABLE;

	if (compare(prodInstalled) <= 0)
		return UpdateManagerConstants.NOT_NEWER;
	
	return UpdateManagerConstants.OK_TO_INSTALL;
}
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
public boolean isRemovable() {
	Enumeration list = _getAllComponentEntries().elements();
	IComponentDescriptor comp;
	while(list.hasMoreElements()) {
		comp = ((IComponentEntryDescriptor) list.nextElement()).getComponentDescriptor();
		if (comp == null) continue;
		if (!comp.isDanglingComponent() && !comp.isRemovable(this))
			return false;
	}
	return true;
}
/**
 * Returns whether the product described by this descriptor
 * can be updated or not.   Currently no condition is checked.
 *
 * @return <code>true</code> if this product is updateable, and
 *   <code>false</code> otherwise
 * @see #getUpdateURL
 */
public boolean isUpdateable() {

	return true;
}
}
