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
		private Hashtable plugin_proxys_list = null;

//		private Set extra_updateURLs = null;	// specified by users in UI
//		private Vector _programPaths = null;
		private URL _registryBase;		// Base of the Eclipse tree of this registry
		private boolean _initialStartup = true;
		private boolean _filtered = false; // whether this registry reflects LaunchInfo
		
		private long _lastRefreshed = 0;

		// holds different versions of an entity (prod, comp or plug-in) that have the same id
		class Proxy {
			private String _id;
			private Map _versions ;
			Proxy(String id)      { 
				_id = id;
				_versions = null;
			}
			public void _addToVersionsRel(Object o, String key) {
				if (_versions == null)  _versions = Collections.synchronizedMap(new TreeMap(new VersionComparator()));	
				_versions.put(key,o);
			}
			public Object _lookupVersion(String key) {
	
				if(key == null) return null;
				if (_versions == null) return null;
				return _versions.get(key);
			}
			public Object _getEarliestVersion() {
				TreeMap tm = new TreeMap(_versions);
				String key = tm.firstKey().toString();
				return _versions.get(key);
			}
			public Object _getLatestVersion() {
				TreeMap tm = new TreeMap(_versions);
				String key = tm.lastKey().toString();
				return _versions.get(key);
			}
			public Map _getVersionsRel() {
		
				return _versions;
	
			}
		}

/**
 * UMRegistryModel constructor comment.
 */
public UMRegistryModel() {
	super();

}
// add a new component
public void _addToComponentsRel(Object o) {

	if (comp_proxys_list == null) comp_proxys_list = new Hashtable();
	String key = ((ComponentDescriptorModel)o)._getId();
	String version = ((ComponentDescriptorModel)o)._getVersion();
	
	if (comp_proxys_list.containsKey(key)) { // a different version?  
		Proxy proxy = (Proxy) comp_proxys_list.get(key);
		Map versions = proxy._getVersionsRel();
		if (versions.containsKey(version))	
			; // LINDA - error condition - version collision
		else {
			proxy._addToVersionsRel(o, version);
		}
	} else {
		Proxy proxy = new Proxy(key);
		proxy._addToVersionsRel(o, version);
		comp_proxys_list.put(key, proxy);
	}
}
public void _addToDanglingComponentIVPsRel(Object o) {

	LaunchInfo.VersionedIdentifier vid = (LaunchInfo.VersionedIdentifier) o;
	LaunchInfo.getCurrent().isDanglingComponent(vid, true); // add
}
// add a new component
public void _addToPluginsRel(Object o) {

	if (plugin_proxys_list == null) plugin_proxys_list = new Hashtable();
	String key = ((PluginEntryDescriptorModel)o)._getId();
	String version = ((PluginEntryDescriptorModel)o)._getVersion();
	
	if (plugin_proxys_list.containsKey(key)) { // a different version?  
		Proxy proxy = (Proxy) plugin_proxys_list.get(key);
		Map versions = proxy._getVersionsRel();
		if (versions.containsKey(version))	
			; // LINDA - error condition - version collision
		else {
			proxy._addToVersionsRel(o, version);
		}
	} else {
		Proxy proxy = new Proxy(key);
		proxy._addToVersionsRel(o, version);
		plugin_proxys_list.put(key, proxy);
	}
}
public void _addToProductsRel(Object o) {

	if (product_proxys_list == null) product_proxys_list = new Hashtable();
	String key = ((ProductDescriptorModel)o)._getId();
	String version = ((ProductDescriptorModel)o)._getVersion();
	
	if (product_proxys_list.containsKey(key)) { // a different version?  
		Proxy proxy = (Proxy) product_proxys_list.get(key);
		Map versions = proxy._getVersionsRel();
		if (versions.containsKey(version))	
			; // error condition - version collision
		else {
			proxy._addToVersionsRel(o, version);
		}
	} else {
		Proxy proxy = new Proxy(key);
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
public void _copyPluginProxysRelInto(Object[] array) {

	if (plugin_proxys_list != null) {
		Enumeration list = plugin_proxys_list.elements();
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
public Enumeration _enumeratePluginProxysRel() {

	if (plugin_proxys_list == null) return (new Vector()).elements();
	else return plugin_proxys_list.elements();
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
		Map m = ((Proxy)list.nextElement())._getVersionsRel();
		for (Iterator i=m.entrySet().iterator(); i.hasNext(); ) {
			  Map.Entry me = (Map.Entry) i.next();
			  cd = (ComponentDescriptorModel) me.getValue();
			  all_comp.addElement(cd);
			  
	    }
	}
	return all_comp;
}
public Vector _getAllPluginEntries() {

	Vector all_plugins = new Vector();
	if (plugin_proxys_list == null) return all_plugins;
	
	PluginEntryDescriptorModel ped;
	Enumeration list = plugin_proxys_list.elements();
	
	while(list.hasMoreElements()) {
		Map m = ((Proxy)list.nextElement())._getVersionsRel();
		for (Iterator i=m.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry me = (Map.Entry) i.next();
			ped = (PluginEntryDescriptorModel) me.getValue();
			all_plugins.addElement(ped);
	    }
	}
	return all_plugins;
}
public Vector _getAllProducts() {

	Vector all_prod = new Vector();
	if (product_proxys_list == null) return all_prod;
		
	ProductDescriptorModel pd;
	Enumeration list = product_proxys_list.elements();
	
	while(list.hasMoreElements()) {
		Map m = ((Proxy)list.nextElement())._getVersionsRel();
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
		cd = (ComponentDescriptorModel) ((Proxy)list.nextElement())._getLatestVersion();
		_comp_list.put(cd._getId(), cd);
	}
	return _comp_list;
}
public long _getLastRefreshed() {
	return _lastRefreshed;
}
public Hashtable _getPluginEntriesAtLatestVersion() {
	
	Hashtable _plugins_list = new Hashtable();
	if (plugin_proxys_list == null) return _plugins_list;

	PluginEntryDescriptorModel ped;
	Enumeration list = plugin_proxys_list.elements();
	while(list.hasMoreElements()) {
		ped = (PluginEntryDescriptorModel) ((Proxy)list.nextElement())._getLatestVersion();
		_plugins_list.put(ped._getId(), ped);
	}
	return _plugins_list;
}
public Hashtable _getPluginProxysRel() {
	
	return plugin_proxys_list;
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
		pd = (ProductDescriptorModel) ((Proxy)list.nextElement())._getLatestVersion();
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
public int _getSizeOfPluginProxysRel() {

	if (plugin_proxys_list == null) return 0;
	else return plugin_proxys_list.size();
}
public int _getSizeOfProductProxysRel() {

	if (product_proxys_list == null) return 0;
	else return product_proxys_list.size();
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
public void _loadComponentManifest(String compDir, String dirName, IUMFactory factory) {

	try {	
		ComponentDescriptorModel cd = (ComponentDescriptorModel) factory.createComponentDescriptor();
		cd._setDirName(dirName);
		cd._setInstallURL(compDir + dirName);
		cd._loadManifest(new URL(cd._getInstallManifestURL()), this, factory);
	} catch (java.net.MalformedURLException e) {
	}

}
public void _loadManifests(URL url, IUMFactory factory) {
	
	_loadManifests(url, factory, false);

}
public void _loadManifests(URL url, IUMFactory factory, boolean filtered) {

	_filtered = filtered;
	_registryBase = url;
	long startTime = (new java.util.Date()).getTime();
		
	URL installURLs[] = UMEclipseTree.getDirectoriesInChain(url);
	
	// Components load first, so the componentEntries in products can be resolved later
	for (int i = 0; i < installURLs.length; i++) {
		URL componentPath = UMEclipseTree.getComponentURL(installURLs[i]);
		if (filtered) {
			LaunchInfo.VersionedIdentifier[] ivps = LaunchInfo.getCurrent().getComponents();
			for (int j = 0; j < ivps.length; j++) {
				_loadComponentManifest(componentPath.toString(), ivps[j].toString(), factory);
			}
		} else {
			// get all components
			String[] members = UMEclipseTree.getPathMembers(componentPath);
			for (int j = 0; j < members.length; j++) {
				if (members[j].equals(IManifestAttributes.INSTALL_INDEX)) continue;
				_loadComponentManifest(componentPath.toString(), members[j] , factory);
			}
		}

		// Products
		//---------
		URL productPath = UMEclipseTree.getProductURL(installURLs[i]);
		if (filtered) {
			LaunchInfo.VersionedIdentifier[] ivps = LaunchInfo.getCurrent().getConfigurations();
			for (int j = 0; j < ivps.length; j++) {
				_loadProductManifest(productPath.toString(), ivps[j].toString(), factory);
			}
		} else {
			// get all products
			String[] members = UMEclipseTree.getPathMembers(productPath);
			for (int j = 0; j < members.length; j++) {
				if (members[j].equals(IManifestAttributes.INSTALL_INDEX)) continue;
				_loadProductManifest(productPath.toString(), members[j] , factory);		
			}
		}
	}
	_lastRefreshed = startTime; 

}
/* load product manifest in the directory prodDir/dirName
 * which is of the form ...../install/configurations/prod_dirname
 */
public void _loadProductManifest(String prodDir, String dirName, IUMFactory factory) {

	try {	
		ProductDescriptorModel pd = (ProductDescriptorModel) factory.createProductDescriptor();
		pd._setDirName(dirName);
		pd._setInstallURL(prodDir + dirName);
		pd._loadManifest(new URL(pd._getInstallManifestURL()), this, factory);
	} catch (java.net.MalformedURLException e) {
	}

}
public ComponentDescriptorModel _lookupComponentDescriptor(String compId, String version) {

	if(compId == null) return null;
	if (comp_proxys_list == null) return null;
	Proxy proxy = (Proxy) _lookupComponentProxy(compId);
	if (proxy == null) return null;
	if (version == null)
		return (ComponentDescriptorModel)proxy._getLatestVersion();
	return (ComponentDescriptorModel)proxy._lookupVersion(version);
	
}
public Proxy _lookupComponentProxy(String key) {

	if(key == null) return null;
	if (comp_proxys_list == null) return null;
	return (Proxy) comp_proxys_list.get(key);
}
public PluginEntryDescriptorModel _lookupPluginEntryDescriptor(String plugId, String version) {

	if(plugId == null) return null;
	if (plugin_proxys_list == null) return null;
	Proxy proxy = (Proxy) _lookupPluginProxy(plugId);
	if (proxy == null) return null;
	if (version == null)
		return (PluginEntryDescriptorModel)proxy._getLatestVersion();
	return (PluginEntryDescriptorModel)proxy._lookupVersion(version);
}
public Proxy _lookupPluginProxy(String key) {

	if(key == null) return null;
	if (plugin_proxys_list == null) return null;
	return (Proxy)plugin_proxys_list.get(key);
}
public ProductDescriptorModel _lookupProductDescriptor(String prodId, String version) {

	if(prodId == null) return null;
	if (product_proxys_list == null) return null;
	Proxy proxy = (Proxy) _lookupProductProxy(prodId);
	if (proxy == null) return null;
	if (version == null)
		return (ProductDescriptorModel)proxy._getLatestVersion();
	return (ProductDescriptorModel)proxy._lookupVersion(version);
	
}
public Proxy _lookupProductProxy(String key) {

	if(key == null) return null;
	if (product_proxys_list == null) return null;
	return (Proxy)product_proxys_list.get(key);
}
}
