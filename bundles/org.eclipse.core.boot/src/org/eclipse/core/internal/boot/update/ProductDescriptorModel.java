package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
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

	private Vector _componentEntries = null;

/**
 * ComponentDescriptorModel constructor comment.
 */
public ProductDescriptorModel() {
	super();
}
public void _addToComponentEntriesRel(Object o) {
	
	if (_componentEntries == null) _componentEntries = new Vector();
	_componentEntries.addElement(o);
	
}
public void _copyComponentEntriesRelInto(Object[] array) {

	if (_componentEntries != null) _componentEntries.copyInto(array);
}
public Enumeration _enumerateComponentEntriesRel() {

	if (_componentEntries == null) return (new Vector()).elements();
	return _componentEntries.elements();
}
public String _getApplication() {
	
	return _application;
}
public Vector _getComponentEntriesRel() {

	return _componentEntries;
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
public int _getSizeOfComponentEntriesRel() {

	if (_componentEntries == null) return 0;
	else return _componentEntries.size();
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
		_addToComponentEntriesRel(componentEntryDescriptor);

		// see if it is installed physically 
		try {
			URL path = UMEclipseTree.getComponentURL(parent._getRegistryBase());
			String comp_dir = componentEntryDescriptor._getId()+"_"+componentEntryDescriptor._getVersion()+"/";
			path = new URL(path, comp_dir);
			File check = new File( path.getFile());
			if (check.exists()) {
				componentEntryDescriptor._isInstalled(true);
				componentEntryDescriptor._setCompInstallURL(path.toString());	
				// find the corresponding component descriptor and add this 
				// product to the list of containing products
				ComponentDescriptorModel comp = parent._lookupComponentDescriptor(componentEntryDescriptor._getId(), componentEntryDescriptor._getVersion());
				if (comp != null)
					comp._addToContainingProductsRel(this);
			}
			componentEntryDescriptor._setDirName(comp_dir);	
		} catch (java.net.MalformedURLException ex) {
					// LINDA -error condition		
		} 

	}

	// Register
	//---------
	_setUMRegistry(parent);
	parent._addToProductsRel(this);
}
public ComponentEntryDescriptorModel _lookupComponentEntry(String key) {

	if(key == null) return null;
	
	Enumeration list = _enumerateComponentEntriesRel();
	ComponentEntryDescriptorModel comp;
	while(list.hasMoreElements()) {
		comp = (ComponentEntryDescriptorModel) list.nextElement();
		if(key.equals(comp._getId())) return comp;
	}
	
	return null;
}
public void _removeFromComponentEntriesRel(Object o) {

	if (o==null || _componentEntries == null) return;
	_componentEntries.removeElement(o);		
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
		clone._componentEntries = (Vector) _componentEntries.clone();

		return clone;
	} catch (CloneNotSupportedException e) {
		return null;
	}
}
}
