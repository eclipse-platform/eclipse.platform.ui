package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
 
import org.eclipse.core.internal.boot.LaunchInfo;
import java.io.*;
import java.util.*;
import java.net.*;

public class UMRegistryModel   {
		// persistent properties (marshaled)
		private Hashtable comp_proxys_list = null; // components
		private Hashtable product_proxys_list = null;
//		private Hashtable plugin_proxys_list = null;

//		private Set extra_updateURLs = null;	// specified by users in UI
//		private Vector _programPaths = null;
		private int _type;				// current, local, or remote(discovery)
		private URL _registryBase;		// Base of the Eclipse tree of this registry
		private boolean _initialStartup = true;
		private boolean _filtered = false; // whether this registry reflects LaunchInfo
		
		private long _lastRefreshed = 0;


/**
 * UMRegistryModel constructor comment.
 */
public UMRegistryModel() {
	super();

}
// add a new component
public void _addToComponentProxysRel(Object o) {

	if (comp_proxys_list == null) comp_proxys_list = new Hashtable();
	String key = ((ComponentDescriptorModel)o)._getId();
	String version = ((ComponentDescriptorModel)o)._getVersion();
	
	if (comp_proxys_list.containsKey(key)) { // a different version?  
		UMProxy proxy = (UMProxy) comp_proxys_list.get(key);
		Map versions = proxy._getVersionsRel();
		if (versions.containsKey(version))	
			; // LINDA - error condition - version collision
		else {
			proxy._addToVersionsRel(o, version);
		}
	} else {
		UMProxy proxy = new UMProxy(key);
		proxy._addToVersionsRel(o, version);
		comp_proxys_list.put(key, proxy);
	}
}
public void _addToDanglingComponentIVPsRel(Object o) {

	LaunchInfo.VersionedIdentifier vid = (LaunchInfo.VersionedIdentifier) o;
	LaunchInfo.getCurrent().isDanglingComponent(vid, true); // add
}
public void _addToProductProxysRel(Object o) {

	if (product_proxys_list == null) product_proxys_list = new Hashtable();
	String key = ((ProductDescriptorModel)o)._getId();
	String version = ((ProductDescriptorModel)o)._getVersion();
	
	if (product_proxys_list.containsKey(key)) { // a different version?  
		UMProxy proxy = (UMProxy) product_proxys_list.get(key);
		Map versions = proxy._getVersionsRel();
		if (versions.containsKey(version))	
			; // error condition - version collision
		else {
			proxy._addToVersionsRel(o, version);
		}
	} else {
		UMProxy proxy = new UMProxy(key);
		proxy._addToVersionsRel(o, version);
		product_proxys_list.put(key, proxy);
	}
}
public void _copyComponentProxysRelInto(Object[] array) {

	if (comp_proxys_list != null) {
		Enumeration list = comp_proxys_list.elements();
		for(int i=0; list.hasMoreElements(); i++) {
			array[i] = list.nextElement();
		}
	}
}
public void _copyProductProxysRelInto(Object[] array) {

	if (product_proxys_list != null) {
		Enumeration list = product_proxys_list.elements();
		for(int i=0; list.hasMoreElements(); i++) {
			array[i] = list.nextElement();
		}
	}
}
public Enumeration _enumerateComponentProxysRel() {

	if (comp_proxys_list == null) return (new Vector()).elements();
	else return comp_proxys_list.elements();
}
public Enumeration _enumerateProductProxysRel() {

	if (product_proxys_list == null) return (new Vector()).elements();
	else return product_proxys_list.elements();
}
public Vector _getAllComponents() {

	Vector all_comp = new Vector();
	if (comp_proxys_list == null) return all_comp;
	ComponentDescriptorModel cd;
	Enumeration list = comp_proxys_list.elements();
	
	while(list.hasMoreElements()) {
		Map m = ((UMProxy)list.nextElement())._getVersionsRel();
		for (Iterator i=m.entrySet().iterator(); i.hasNext(); ) {
			  Map.Entry me = (Map.Entry) i.next();
			  cd = (ComponentDescriptorModel) me.getValue();
			  all_comp.addElement(cd);
			  
	    }
	}
	return all_comp;
}
public Vector _getAllProducts() {

	Vector all_prod = new Vector();
	if (product_proxys_list == null) return all_prod;
		
	ProductDescriptorModel pd;
	Enumeration list = product_proxys_list.elements();
	
	while(list.hasMoreElements()) {
		Map m = ((UMProxy)list.nextElement())._getVersionsRel();
		for (Iterator i=m.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry me = (Map.Entry) i.next();
			pd = (ProductDescriptorModel) me.getValue();
			all_prod.addElement(pd);
	    }
	}
	return all_prod;
}
public Hashtable _getComponentProxysRel() {
	
	return comp_proxys_list;
}
public Hashtable _getComponentsAtLatestVersion() {
	
	Hashtable _comp_list = new Hashtable();
	if (comp_proxys_list == null) return _comp_list;

	ComponentDescriptorModel cd;
	Enumeration list = comp_proxys_list.elements();
	while(list.hasMoreElements()) {
		cd = (ComponentDescriptorModel) ((UMProxy)list.nextElement())._getLatestVersion();
		_comp_list.put(cd._getId(), cd);
	}
	return _comp_list;
}
public long _getLastRefreshed() {
	return _lastRefreshed;
}
public Hashtable _getProductProxysRel() {
	
	return product_proxys_list;
}
public Hashtable _getProductsAtLatestVersion() {
	
	Hashtable _prod_list = new Hashtable();
	if (product_proxys_list == null) return _prod_list;

	ProductDescriptorModel pd;
	Enumeration list = product_proxys_list.elements();
	while(list.hasMoreElements()) {
		pd = (ProductDescriptorModel) ((UMProxy)list.nextElement())._getLatestVersion();
		_prod_list.put(pd._getId(), pd);
	}
	return _prod_list;
}
public URL _getRegistryBase() {
	return _registryBase;
}
public int _getSizeOfComponentProxysRel() {

	if (comp_proxys_list == null) return 0;
	else return comp_proxys_list.size();
}
public int _getSizeOfProductProxysRel() {

	if (product_proxys_list == null) return 0;
	else return product_proxys_list.size();
}
public int _getType() {
	return _type;
}
public boolean _isFiltered() {
	
	return _filtered;
}
public boolean _isInitialStartup() {
	
	return _initialStartup;
}
public void _isInitialStartup(boolean initialStartup) {
	
	_initialStartup = initialStartup;
}
/* load component manifest in the directory compDir/dirName
 * which is of the form ...../install/components/comp_dirname
 */
public ComponentDescriptorModel _loadComponentManifest(String compDir, String dirName, IUMFactory factory) {

	ComponentDescriptorModel cd = null;
	try {	
		cd = (ComponentDescriptorModel) factory.createComponentDescriptor();
		cd._setDirName(dirName);
		cd._setInstallURL(compDir + dirName);
		cd._loadManifest(new URL(cd._getInstallManifestURL()), this, factory);
	} catch (java.net.MalformedURLException e) {
	}
	return cd;
}
public void _loadManifests(URL url, IUMFactory factory) {
	
	_loadManifests(url, factory, false);

}
/* The whole picture:
 * Products load first, which in turn load their componentEntries
 * for each componentEntry, look for actual component -
 *     if exists on filesys, then compEntries.installed=true, load component
 *     else compEntries.instaled = false
 *
 * Next we go to the components directory
 * Look for install.index, if it exists on filesys, load all the components listed
 * if component not already loaded (must be loose), load component 
 * if this is a remote reg, comp._isLoose(true);
 * for a local/current reg, see getDanglingComponents() to see how these are found
 */
public void _loadManifests(URL url, IUMFactory factory, boolean filtered) {

	_filtered = filtered;
	_registryBase = url;
	long startTime = (new java.util.Date()).getTime();
		
	URL installURLs[] = UMEclipseTree.getDirectoriesInChain(url);
	
	for (int i = 0; i < installURLs.length; i++) {
		// Products
		//---------
		URL productPath = UMEclipseTree.getProductURL(installURLs[i]);
		if (filtered) {		// load specific products according to LaunchInfo
			LaunchInfo.VersionedIdentifier[] ivps = LaunchInfo.getCurrent().getConfigurations();
			for (int j = 0; j < ivps.length; j++) {
				_loadProductManifest(productPath.toString(), ivps[j].toString(), factory);
			}
		} else {			// get all products
			String[] members = UMEclipseTree.getPathMembers(productPath);
			for (int j = 0; j < members.length; j++) {
				if (members[j].equals(IManifestAttributes.INSTALL_INDEX)) continue;
				_loadProductManifest(productPath.toString(), members[j] , factory);		
			}
		}
		
		// Loose Components  
		//-----------------
		URL componentPath = UMEclipseTree.getComponentURL(installURLs[i]);
		if (filtered) {		// load specific components according to LaunchInfo
			LaunchInfo.VersionedIdentifier[] ivps = LaunchInfo.getCurrent().getComponents();
			for (int j = 0; j < ivps.length; j++) {
				ComponentDescriptorModel cd = _lookupComponentDescriptor(ivps[j].getIdentifier(), ivps[j].getVersion());
				if (cd == null) { // these are the dangling ones
					_loadComponentManifest(componentPath.toString(), ivps[j].toString(), factory);
				}
			}
		} else {			// get all components in install.index or dir
			String[] members = UMEclipseTree.getPathMembers(componentPath);
			for (int j = 0; j < members.length; j++) {
				if (members[j].equals(IManifestAttributes.INSTALL_INDEX)) continue;
				LaunchInfo.VersionedIdentifier vid = new LaunchInfo.VersionedIdentifier(members[j]);
				ComponentDescriptorModel cd = _lookupComponentDescriptor(vid.getIdentifier(), vid.getVersion());
				if (cd == null) {
					cd = _loadComponentManifest(componentPath.toString(), members[j] , factory);
				}
				if (cd !=null) {
					if (_getType() == UpdateManagerConstants.REMOTE_REGISTRY)
						cd._isLoose(true);
				}
			}
		}
		




	}
	_lastRefreshed = startTime; 

}
/* load product manifest in the directory prodDir/dirName
 * which is of the form ...../install/configurations/prod_dirname
 */

public ProductDescriptorModel _loadProductManifest(String prodDir, String dirName, IUMFactory factory) {

	ProductDescriptorModel pd = null;
	try {	
		pd = (ProductDescriptorModel) factory.createProductDescriptor();
		pd._setDirName(dirName);
		pd._setInstallURL(prodDir + dirName);
		pd._loadManifest(new URL(pd._getInstallManifestURL()), this, factory);
	} catch (java.net.MalformedURLException e) {
	}
	return pd;
}
public ComponentDescriptorModel _lookupComponentDescriptor(String compId, String version) {

	if(compId == null) return null;
	if (comp_proxys_list == null) return null;
	UMProxy proxy = (UMProxy) _lookupComponentProxy(compId);
	if (proxy == null) return null;
	if (version == null)
		return (ComponentDescriptorModel)proxy._getLatestVersion();
	return (ComponentDescriptorModel)proxy._lookupVersion(version);
	
}
public UMProxy _lookupComponentProxy(String key) {

	if(key == null) return null;
	if (comp_proxys_list == null) return null;
	return (UMProxy) comp_proxys_list.get(key);
}
public ProductDescriptorModel _lookupProductDescriptor(String prodId, String version) {

	if(prodId == null) return null;
	if (product_proxys_list == null) return null;
	UMProxy proxy = (UMProxy) _lookupProductProxy(prodId);
	if (proxy == null) return null;
	if (version == null)
		return (ProductDescriptorModel)proxy._getLatestVersion();
	return (ProductDescriptorModel)proxy._lookupVersion(version);
	
}
public UMProxy _lookupProductProxy(String key) {

	if(key == null) return null;
	if (product_proxys_list == null) return null;
	return (UMProxy)product_proxys_list.get(key);
}
public void _removeFromComponentProxysRel(Object o) {

	if (o==null || comp_proxys_list == null) return;
	String key = ((ComponentDescriptorModel)o)._getId();
	String version = ((ComponentDescriptorModel)o)._getVersion();
	
	if (comp_proxys_list.containsKey(key)) {  
		UMProxy proxy = (UMProxy) comp_proxys_list.get(key);
		Map versions = proxy._getVersionsRel();
		versions.remove(version);
		if (versions.size() ==0)	// no other versions of this id left
			comp_proxys_list.remove(key);
	} else {
		// error condition - component id doesn't exist
	}
}
public void _removeFromDanglingComponentIVPsRel(Object o) {

	LaunchInfo.VersionedIdentifier vid = (LaunchInfo.VersionedIdentifier) o;
	LaunchInfo.getCurrent().isDanglingComponent(vid, false); // remove
}
public void _removeFromProductProxysRel(Object o) {

	if (o==null || product_proxys_list == null) return;
	String key = ((ProductDescriptorModel)o)._getId();
	String version = ((ProductDescriptorModel)o)._getVersion();
	
	if (product_proxys_list.containsKey(key)) {  
		UMProxy proxy = (UMProxy) product_proxys_list.get(key);
		Map versions = proxy._getVersionsRel();
		versions.remove(version);
		if (versions.size() ==0)	// no other versions of this id left
			product_proxys_list.remove(key);
	} else {
		// error condition - product id doesn't exist
	}
}
public void _setType(int type) {
	_type = type;
}
}
