package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

/**
 * Get/Set of elements from jar manifest
 */

public class ProductDescriptorModel extends ManifestDescriptorModel  {

	// persistent properties (marshaled)
	private String _id = null;
	private String _name = null;
	private String _version = null;
	private String _vendorName = null;
	private String _description = null;
	private String _application = null;

	private long   _stamp = 0;
	private String _dirName = null;			// dir name in .install/.products/

	private Hashtable compEntry_proxys_list = null; // component Entries

/**
 * ComponentDescriptorModel constructor comment.
 */
public ProductDescriptorModel() {
	super();
}
// add a new component entry
public void _addToComponentEntryProxysRel(Object o) {

	if (compEntry_proxys_list == null) compEntry_proxys_list = new Hashtable();
	String key = ((ComponentEntryDescriptorModel)o)._getId();
	String version = ((ComponentEntryDescriptorModel)o)._getVersion();
	
	if (compEntry_proxys_list.containsKey(key)) { // a different version?  
		UMProxy proxy = (UMProxy) compEntry_proxys_list.get(key);
		Map versions = proxy._getVersionsRel();
		if (!versions.containsKey(version))	{
			proxy._addToVersionsRel(o, version);
		}
	} else {
		UMProxy proxy = new UMProxy(key);
		proxy._addToVersionsRel(o, version);
		compEntry_proxys_list.put(key, proxy);
	}
}
public void _copyComponentEntryProxysRelInto(Object[] array) {

	if (compEntry_proxys_list != null) {
		Enumeration list = compEntry_proxys_list.elements();
		for(int i=0; list.hasMoreElements(); i++) {
			array[i] = list.nextElement();
		}
	}
}
public Enumeration _enumerateComponentProxysRel() {

	if (compEntry_proxys_list == null) return (new Vector()).elements();
	else return compEntry_proxys_list.elements();
}
public Vector _getAllComponentEntries() {

	Vector all_comp_entries = new Vector();
	if (compEntry_proxys_list == null) return all_comp_entries;
	ComponentEntryDescriptorModel ced;
	Enumeration list = compEntry_proxys_list.elements();
	
	while(list.hasMoreElements()) {
		Map m = ((UMProxy)list.nextElement())._getVersionsRel();
		for (Iterator i=m.entrySet().iterator(); i.hasNext(); ) {
			  Map.Entry me = (Map.Entry) i.next();
			  ced = (ComponentEntryDescriptorModel) me.getValue();
			  all_comp_entries.addElement(ced);
			  
	    }
	}
	return all_comp_entries;
}
public String _getApplication() {
	
	return _application;
}
public Hashtable _getComponentEntriesAtLatestVersion() {
	
	Hashtable _compEntry_list = new Hashtable();
	if (compEntry_proxys_list == null) return _compEntry_list;

	ComponentEntryDescriptorModel ced;
	Enumeration list = compEntry_proxys_list.elements();
	while(list.hasMoreElements()) {
		ced = (ComponentEntryDescriptorModel) ((UMProxy)list.nextElement())._getLatestVersion();
		_compEntry_list.put(ced._getId(), ced);
	}
	return _compEntry_list;
}
public Hashtable _getComponentEntryProxysRel() {
	
	return compEntry_proxys_list;
}
public String _getDescription() {
	
	return _description;
}
/* Directory name in .install/.products, usually made up of
 * prodid_label_version
 */
public String _getDirName() {
	return _dirName;
	
}
public String _getId() {
	
	return _id;
}
public String _getName() {
	
	return _name;
}
public String _getProviderName() {
	
	return _vendorName;
}
public int _getSizeOfComponentProxysRel() {

	if (compEntry_proxys_list == null) return 0;
	else return compEntry_proxys_list.size();
}
public long _getStamp() {
	return _stamp;
}
public String _getVersion() {
	
	return _version;
}
/**
 *
 */
public void _loadManifest(URL url, UMRegistryModel parent, IUMFactory factory) {

	// Parse configuration xml file
	//-----------------------------
	XmlLite parser = new XmlLite();

	try {
		parser.load(url);
	}
	catch (XmlLiteException ex) {
		return;
	}

	XmlLiteElement elementConfiguration = parser.getChildElement(CONFIGURATION);

	if (elementConfiguration == null) {
		return;
	}

	// Set configuration attributes
	//-----------------------------
	_setManifestType(PRODUCT);

	XmlLiteAttribute attribute = null;

	attribute = elementConfiguration.getAttribute(PRODUCT_NAME);
	if (attribute != null)
		_setName(attribute.getValue());

	String componentID = null;

	attribute = elementConfiguration.getAttribute(PRODUCT_ID);
	if (attribute != null)
		_setId(componentID = attribute.getValue());

	attribute = elementConfiguration.getAttribute(PRODUCT_VERSION);
	if (attribute != null) {
		try {
			new VersionIdentifier(attribute.getValue());
		} catch (Exception ex) {
			return;
		}
		_setVersion(attribute.getValue());
	}

	attribute = elementConfiguration.getAttribute(PROVIDER);
	if (attribute != null)
		_setProviderName(attribute.getValue());

	attribute = elementConfiguration.getAttribute(APPLICATION);
	if (attribute != null)
		_setApplication(attribute.getValue());

		XmlLiteElement element = elementConfiguration.getChildElement(DESCRIPTION);
	if (element != null) {
		element = element.getChildElement("#text");
		if (element != null) {
			attribute = element.getAttribute("text");
			if (attribute != null) {
				_setDescription(attribute.getValue());
			}
		}
	}
	
	XmlLiteElement elementUrl = elementConfiguration.getChildElement(URL);
	URLNamePairModel urlNamePair = null;
	if (elementUrl != null) {
		XmlLiteElement[] elementUpdates = elementUrl.getChildElements(UPDATE_URL);
		for (int i = 0; i < elementUpdates.length; ++i) {
			urlNamePair = factory.createURLNamePair();
			
			attribute = elementUpdates[i].getAttribute(URL_NAME);
			if (attribute != null) 
				urlNamePair._setName(attribute.getValue());
				
			attribute = elementUpdates[i].getAttribute(URL);
			if (attribute != null) {
				urlNamePair._setURL(attribute.getValue());		
				_addToUpdateURLsRel(urlNamePair);
			}
		}
		XmlLiteElement[] elementDiscoveries = elementUrl.getChildElements(DISCOVERY_URL);
		for (int i = 0; i < elementDiscoveries.length; ++i) {
			urlNamePair = factory.createURLNamePair();
			
			attribute = elementDiscoveries[i].getAttribute(URL_NAME);
			if (attribute != null) 
				urlNamePair._setName(attribute.getValue());		
			
				attribute = elementDiscoveries[i].getAttribute(URL);
			if (attribute != null) {
				urlNamePair._setURL(attribute.getValue());
				_addToDiscoveryURLsRel(urlNamePair);
			}
		}
	}
	// Components
	//-----------
	XmlLiteElement[] elementComponents = elementConfiguration.getChildElements(COMPONENT);

	ComponentEntryDescriptorModel componentEntryDescriptor = null;

	for (int i = 0; i < elementComponents.length; ++i) {

		componentEntryDescriptor = factory.createComponentEntryDescriptor();

		attribute = elementComponents[i].getAttribute(COMPONENT_NAME);
		if (attribute != null)
			componentEntryDescriptor._setName(attribute.getValue());

		attribute = elementComponents[i].getAttribute(COMPONENT_ID);
		if (attribute != null)
			componentEntryDescriptor._setId(attribute.getValue());

		attribute = elementComponents[i].getAttribute(COMPONENT_VERSION);
		if (attribute != null) {
			try {
				new VersionIdentifier(attribute.getValue());
			} catch (Exception ex) {
				return;
			}
			componentEntryDescriptor._setVersion(attribute.getValue());
		}
			

		attribute = elementComponents[i].getAttribute(ALLOW_UPGRADE);
		if (attribute != null)
			componentEntryDescriptor._setUpgradeable(attribute.getValue());

		attribute = elementComponents[i].getAttribute(OPTIONAL);
		if (attribute != null)
			componentEntryDescriptor._setOptional(attribute.getValue());

		componentEntryDescriptor._setProdInstallURL(_getInstallURL());
		componentEntryDescriptor._setContainingProduct(this);
		componentEntryDescriptor._setUMRegistry(parent);
		_addToComponentEntryProxysRel(componentEntryDescriptor);

		// see if it is installed physically 		
		URL path = UMEclipseTree.getComponentURL(parent._getRegistryBase());
		String comp_dir = componentEntryDescriptor._getId()+"_"+componentEntryDescriptor._getVersion();
			
		// load the corresponding component descriptor and add this 
		// product to the list of containing products
		ComponentDescriptorModel comp = parent._loadComponentManifest(UMEclipseTree.getComponentURL(parent._getRegistryBase()).toString(), comp_dir, factory);
		if (comp != null) {
			comp._addToContainingProductsRel(this);
			componentEntryDescriptor._isInstalled(true);
			componentEntryDescriptor._setCompInstallURL(path.toString());
		}
			
		componentEntryDescriptor._setDirName(comp_dir);	
	}

	// Register
	//---------
	_setUMRegistry(parent);
	parent._addToProductProxysRel(this);
}
public ComponentEntryDescriptorModel _lookupComponentEntry(String compId, String version) {


	if(compId == null) return null;
	if (compEntry_proxys_list == null) return null;
	UMProxy proxy = (UMProxy) _lookupComponentEntryProxy(compId);
	if (proxy == null) return null;
	if (version == null)
		return (ComponentEntryDescriptorModel)proxy._getLatestVersion();
	return (ComponentEntryDescriptorModel)proxy._lookupVersion(version);
	
}
public UMProxy _lookupComponentEntryProxy(String key) {

	if(key == null) return null;
	if (compEntry_proxys_list == null) return null;
	return (UMProxy) compEntry_proxys_list.get(key);
}
public void _removeFromComponentEntryProxysRel(Object o) {

	if (o==null || compEntry_proxys_list == null) return;
	String key = ((ComponentEntryDescriptorModel)o)._getId();
	String version = ((ComponentEntryDescriptorModel)o)._getVersion();
	
	if (compEntry_proxys_list.containsKey(key)) {  
		UMProxy proxy = (UMProxy) compEntry_proxys_list.get(key);
		Map versions = proxy._getVersionsRel();
		versions.remove(version);
		if (versions.size() ==0)	// no other versions of this id left
			compEntry_proxys_list.remove(key);
	} else {
		// error condition - component id doesn't exist
	}
}
public void _setApplication(String app) {
	 _application = app;
}
public void _setDescription(String desc) {
	 _description = desc;
}
public void _setDirName(String dir) {
	 _dirName = dir;
}
public void _setId(String id) {
	 _id = id;
}
public void _setName(String name) {
	_name = name;
}
public void _setProviderName(String vendorName) {
	_vendorName = vendorName;
}
public void _setStamp(long stamp) {
	_stamp = stamp;
}
public void _setVersion(String version) {
	_version = version;
}
 public Object clone() throws CloneNotSupportedException{
	try {
		ProductDescriptorModel clone = (ProductDescriptorModel)super.clone();
		clone.compEntry_proxys_list = (Hashtable) compEntry_proxys_list.clone();

		return clone;
	} catch (CloneNotSupportedException e) {
		return null;
	}
}
}
