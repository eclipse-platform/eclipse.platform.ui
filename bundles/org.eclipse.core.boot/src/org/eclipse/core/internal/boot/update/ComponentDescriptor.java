package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.internal.boot.LaunchInfo;
import java.net.*;
import java.util.*;

public class ComponentDescriptor extends ComponentDescriptorModel implements IComponentDescriptor {

	private NLResourceHelper fNLHelper = null;

	private URL				_downloadURL;

/**
 * ComponentDescriptor constructor comment.
 */
public ComponentDescriptor() {
	super();
	
}
 public Object clone() throws CloneNotSupportedException {
	ComponentDescriptor clone = (ComponentDescriptor)super.clone();

	return clone;

}
public int compare(IComponentDescriptor comp) {
	
	// Return +1 if I am newer than the argument component
	// Return  0 if I am equal to   the argument component
	// Return -1 if I am older than the argument component
	//----------------------------------------------------
	return new VersionComparator().compare(getVersionStr(), comp.getVersionStr());
}
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
public boolean equals(IComponentDescriptor comp) {

	String id = comp.getUniqueIdentifier();

	if ((compare(comp) == 0) && id.equals(this.getUniqueIdentifier()))
		return true;
	return false;
}
/**
 * Returns the list of URLs where new items related to this Product or Component can be found
 * Included are discovery URLs specified by the products this component is
 * part of.
 *
 * @return the discovery URL sites of this component, including those specified by containing products
 */
public IURLNamePair[] getAllDiscoveryURLs() {
	Vector entries = new Vector(_getDiscoveryURLsRel());
	

	Enumeration list = _enumerateContainingProductsRel();
	while(list.hasMoreElements()) {
			IURLNamePair[] urlNPs = ((ProductDescriptor)list.nextElement()).getDiscoveryURLs();
			entries.addAll(Arrays.asList(urlNPs));
	}

		
	IURLNamePair[] discoveryURLs = new IURLNamePair[entries.size()];
	entries.copyInto(discoveryURLs);
	return discoveryURLs;
}
/**
 * Returns the list of URLs where updates to this component can be found.
 * Included are update URLs specified by the products this component is
 * part of.
 *
 * @return the update URL sites of this component, including those specified by
 * the containing products
 */
public IURLNamePair[] getAllUpdateURLs() {
	Vector entries = new Vector(_getUpdateURLsRel());
	

	Enumeration list = _enumerateContainingProductsRel();
	while(list.hasMoreElements()) {
			IURLNamePair[] urlNPs = ((ProductDescriptor)list.nextElement()).getUpdateURLs();
			entries.addAll(Arrays.asList(urlNPs));
	}

		
	IURLNamePair[] updateURLs = new IURLNamePair[entries.size()];
	entries.copyInto(updateURLs);
	return updateURLs;
}
public IProductDescriptor[] getContainingProducts() {
		
	int size = _getSizeOfContainingProductsRel();	
	if(size == 0) return new IProductDescriptor[0];
	
	IProductDescriptor[] list = new IProductDescriptor[size];
	_copyContainingProductsRelInto(list);
	return list;
	
}
public String getDescription() {

	String s = _getDescription();
	if (fNLHelper == null)
		fNLHelper = new NLResourceHelper( DEFAULT_BUNDLE_NAME,getInstallURL());
	return s==null ? "" : fNLHelper.getResourceString(s);
}
public String getDirName() {

	String s = _getDirName();
	return s==null ? "" : s;
}
/**
 * Returns the Discovery URL that matches the  parameter,
 * or <code>null</code> if there is no such Discovery URL.
 *
 * @param url the Discovery URL to search for (e.g. <code>"http://www.example.com"</code>).
 * @return the Discovery URL, or <code>null</code> if not found
 */
public IURLNamePair getDiscoveryURL(java.net.URL url) {
	return	(IURLNamePair) _lookupDiscoveryURL(url);
}
/**
 * Returns the list of URLs where updates to this component can be found.
 *
 * @return the update URL sites of this component
 */
public IURLNamePair[] getDiscoveryURLs() {
	int size = _getSizeOfDiscoveryURLsRel();	
	if(size == 0) return new IURLNamePair[0];
	
	IURLNamePair[] list = new IURLNamePair[size];
	_copyDiscoveryURLsRelInto(list);
	return list;
	
}
/**
 * Returns the URL of this component's jar
 *
 * @return the URL of this component's jar
 */
public URL getDownloadURL() {
	try {
		URL download_url = new URL(UMEclipseTree.appendTrailingSlash(getInstallURL()), getDirName() + ".jar");
		return download_url;
	} catch (MalformedURLException e) {
		throw new IllegalStateException(); // unchecked
	}
}
public IFragmentEntryDescriptor[] getFragmentEntries() {
		
	int size = _getSizeOfFragmentEntriesRel();	
	if(size == 0) return new IFragmentEntryDescriptor[0];
	
	IFragmentEntryDescriptor[] list = new IFragmentEntryDescriptor[size];
	_copyFragmentEntriesRelInto(list);
	return list;
	
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
public URL getInstallURL() {
	try {
		return UMEclipseTree.appendTrailingSlash(_getInstallURL());
		
	} catch (MalformedURLException e) {
		throw new IllegalStateException(); // unchecked
	}
}
public String getLabel() {

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
public IPluginEntryDescriptor[] getPluginEntries() {
		
	int size = _getSizeOfPluginEntriesRel();	
	if(size == 0) return new IPluginEntryDescriptor[0];
	
	IPluginEntryDescriptor[] list = new IPluginEntryDescriptor[size];
	_copyPluginEntriesRelInto(list);
	return list;
	
}
public String getProviderName() {

	String s = _getProviderName();
	if (fNLHelper == null)
		fNLHelper = new NLResourceHelper( DEFAULT_BUNDLE_NAME,getInstallURL());
	return s==null ? "" : fNLHelper.getResourceString(s);
}
/**
 * Returns the list of products that disallow this component to be updated 
 *
 * @return an array of products that do not allow this component to be updated
 */
public IProductDescriptor[] getRestrainingProducts() {
	Vector products = new Vector();

	String id = _getId();
	Enumeration list = _enumerateContainingProductsRel();
	while(list.hasMoreElements()) {
		IProductDescriptor prod = (IProductDescriptor)list.nextElement();
		IComponentEntryDescriptor comp =  prod.getComponentEntry(id);
		if ((comp !=null) && !comp.isAllowedToUpgrade())
			products.add(prod);
	}

	IProductDescriptor[] naySayers  = new IProductDescriptor[products.size()];
	products.copyInto(naySayers);
	return naySayers;
}
public IUMRegistry getUMRegistry() {
	
	return (IUMRegistry)_getUMRegistry();
}
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
	return	(IURLNamePair) _lookupUpdateURL(url);
}
/**
 * Returns the list of URLs where updates to this component can be found.
 *
 * @return the update URL sites of this component
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
public VersionIdentifier getVersionIdentifier() {

	try {
		return new VersionIdentifier(getVersion());
	} catch (Exception e) {
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
 * Returns whether the component described by this descriptor
 * belongs to a product or not.
 *
 * @return <code>true</code> if this component does not belong to any 
 * product configuration, and
 *   <code>false</code> otherwise
 */
public boolean isDanglingComponent() {	
	LaunchInfo.VersionedIdentifier vid = new LaunchInfo.VersionedIdentifier(getUniqueIdentifier(), getVersionStr());
	return LaunchInfo.getCurrent().isDanglingComponent(vid);
}
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
public int isInstallable(IComponentDescriptor compInstalled) {

	// Ok if component is not currently installed
	//-------------------------------------------
	if (compInstalled == null)
		return UpdateManagerConstants.OK_TO_INSTALL;

	VersionIdentifier newVer = getVersionIdentifier();
	
	// Check whether the installed component's parent products allow it to be upgraded
	// if not upgradeable, can still install service
	//--------------------------------------------------------------------------------
	if (!compInstalled.isUpdateable()) {
		if (!newVer.isEquivalentTo(compInstalled.getVersionIdentifier()))
			return UpdateManagerConstants.NOT_UPDATABLE;
	}
	
	// Check version against installed component
	//------------------------------------------
	IProductDescriptor[] containingProds = compInstalled.getContainingProducts();
	if (containingProds.length == 0) {		// not constrained by a product
		if (this.compare(compInstalled) > 0)	// newer
			return UpdateManagerConstants.OK_TO_INSTALL;
		return UpdateManagerConstants.NOT_NEWER;
	} else {
		// Check version compatibility
		//----------------------------
		if (!newVer.isCompatibleWith(compInstalled.getVersionIdentifier())) // same major, newer minor or service
			return UpdateManagerConstants.NOT_COMPATIBLE;

		if (compare(compInstalled)== 0)
			return UpdateManagerConstants.NOT_NEWER;
		
	}
	return UpdateManagerConstants.OK_TO_INSTALL;
}
/**
 * Returns whether the component described by this descriptor
 * belongs to a product or not.   Used only by remote
 * registries.
 *
 * @return <code>true</code> if this component does not belong to any 
 * product configuration, and
 *   <code>false</code> otherwise
 */
public boolean isLoose() {
	return false;
}
/**
 * Returns whether the component or configuration described by this descriptor
 * can be removed or not.  The following condition is checked:
 * 
 * - It has no other containing products/configurations 
 *
 * @return <code>true</code> if this component is removable, and
 *   <code>false</code> otherwise
 */
public boolean isRemovable() {
	return isRemovable(null);
}
/**
 * Returns whether the component or configuration described by this descriptor
 * can be removed or not.  The following condition is checked:
 * 
 * - It has no other containing products/configurations 
 * @param containingProd the product being removed causing this component to be
 *        a candidate for removal as well
 * @return <code>true</code> if this component is removable, and
 *   <code>false</code> otherwise
 */
public boolean isRemovable(IProductDescriptor containingProd) {
	int count = _getSizeOfContainingProductsRel();
	if (containingProd == null)  {  // remove by itself
		if (count == 0)
			return true;
	} else {						// remove as part of a product remove
		if (isDanglingComponent())
			return false;
		if (count == 1) {
			IProductDescriptor prod = (IProductDescriptor) _getContainingProductsRel().firstElement();
			if (prod.equals(containingProd)) { 
				return true;
			}
		} else if (count == 0) {
			return true; 	// error condition, but return anyway
		}
	}
	return false;
}
/**
 * Returns whether the component described by this descriptor
 * can be updated or not.  The following condition is checked:
 *
 * - All its containing products allow the component to be updated
 */
public boolean isUpdateable() {

	String id = _getId();
	Enumeration list = _enumerateContainingProductsRel();
	while(list.hasMoreElements()) {
		IComponentEntryDescriptor comp =  ((IProductDescriptor)list.nextElement()).getComponentEntry(id);
		if (!comp.isAllowedToUpgrade())
			return false;
	}
	return true;
}
/**
 * Remove the given product from the list of products that
 * contains this component
 * @param prod org.eclipse.core.internal.boot.update.IProductDescriptor
 */
public void removeContainingProduct(IProductDescriptor prod) {
	_removeFromContainingProductsRel(prod);
}
public String toString() {
	return getUniqueIdentifier()+VERSION_SEPARATOR_OPEN+getVersionIdentifier().toString()+VERSION_SEPARATOR_CLOSE;
}
}
