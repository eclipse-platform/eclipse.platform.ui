package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */

import java.io.*;
import java.net.*;
import java.util.*;

/** 
 *
 * Model for i/o with jar manifests for Component and Component Delta
 */

public class ComponentDescriptorModel extends ManifestDescriptorModel {

	// properties in install manifest file
	private String _id = null;
	private String _name = null;
	private String _version = null;
	private String _vendorName = null;
	private String _description = null;

	// properties determined at UM run time
	private String _dirName = null;			// dir name in .install/.components/
	private boolean _isLoose = true;	
	private Vector _containingProducts = null;
	private Vector _pluginEntries = null;
	private Vector _fragmentEntries = null;
/**
 * ComponentDescriptorModel constructor comment.
 */
public ComponentDescriptorModel() {
	super();
}
public void _addToContainingProductsRel(Object o) {
	
	if (_containingProducts == null) _containingProducts = new Vector();
	if (!_containingProducts.contains(o))
		_containingProducts.addElement(o);
	
}
public void _addToFragmentEntriesRel(Object o) {
	
	if (_fragmentEntries == null) _fragmentEntries = new Vector();
	_fragmentEntries.addElement(o);
	
}
public void _addToPluginEntriesRel(Object o) {
	
	if (_pluginEntries == null) _pluginEntries = new Vector();
	_pluginEntries.addElement(o);
	
}
public void _copyContainingProductsRelInto(Object[] array) {

	if (_containingProducts != null) _containingProducts.copyInto(array);
}
public void _copyFragmentEntriesRelInto(Object[] array) {

	if (_fragmentEntries != null) _fragmentEntries.copyInto(array);
}
public void _copyPluginEntriesRelInto(Object[] array) {

	if (_pluginEntries != null) _pluginEntries.copyInto(array);
}
public Enumeration _enumerateContainingProductsRel() {

	if (_containingProducts == null) return (new Vector()).elements();
	return _containingProducts.elements();
}
public Enumeration _enumerateFragmentEntriesRel() {

	if (_fragmentEntries == null) return (new Vector()).elements();
	return _fragmentEntries.elements();
}
public Enumeration _enumeratePluginEntriesRel() {

	if (_pluginEntries == null) return (new Vector()).elements();
	return _pluginEntries.elements();
}
public Vector _getContainingProductsRel() {
	
	return _containingProducts;
}
public String _getDescription() {
	
	return _description;
}
/* Directory name in .install/.components, usually made up of
 * compid_version
 */
public String _getDirName() {

		return _dirName;

}
public Vector _getFragmentEntriesRel() {
	
	return _fragmentEntries;
}
public String _getId() {
	
	return _id;
}
public String _getName() {
	
	return _name;
}
public Vector _getPluginEntriesRel() {
	
	return _pluginEntries;
}
public String _getProviderName() {
	
	return _vendorName;
}
public int _getSizeOfContainingProductsRel() {

	if (_containingProducts == null) return 0;
	else return _containingProducts.size();
}
public int _getSizeOfFragmentEntriesRel() {

	if (_fragmentEntries == null) return 0;
	else return _fragmentEntries.size();
}
public int _getSizeOfPluginEntriesRel() {

	if (_pluginEntries == null) return 0;
	else return _pluginEntries.size();
}
public String _getVersion() {
	
	return _version;
}
/*
 * Loose and Dangling are the same idea - a component that can and has been
 * decided to, exist on its own.  However, the term loose is used on the
 * server side (managed by install.index), and dangling is used on the 
 * local side (managed by LaunchInfo).  The two must not mix, and thus are named differently.
 */
public boolean _isLoose() {
	
	return _isLoose;
}
public void _isLoose(boolean loose) {
	
	_isLoose = loose;
}
public void _loadManifest(URL url, UMRegistryModel parent, IUMFactory factory) {

	// Parse component xml file
	//-------------------------
	XmlLite parser = new XmlLite();

	try {
		parser.load(url);
	}
	catch (XmlLiteException ex) {
		return;
	}

	XmlLiteElement elementComponent = parser.getChildElement(COMPONENT);

	if( elementComponent == null )
	{
	    return;
	}
	
	// Set component attributes
	//-------------------------
	_setManifestType(COMPONENT);

	XmlLiteAttribute attribute = null;

	attribute = elementComponent.getAttribute(COMPONENT_NAME);
	if (attribute != null)
		_setName(attribute.getValue());

	String componentID = null;
		
	attribute = elementComponent.getAttribute(COMPONENT_ID);
	if (attribute != null)
		_setId(componentID = attribute.getValue());

	attribute = elementComponent.getAttribute(COMPONENT_VERSION);
	if (attribute != null) {
		try {
			new VersionIdentifier(attribute.getValue());
		} catch (Exception ex) {
			return;
		}
		_setVersion(attribute.getValue());
	}

	attribute = elementComponent.getAttribute(PROVIDER);
	if (attribute != null)
		_setProviderName(attribute.getValue());


	XmlLiteElement element = elementComponent.getChildElement(DESCRIPTION);
	if (element != null) {
		element = element.getChildElement("#text");
		if (element != null) {
			attribute = element.getAttribute("text");
			if (attribute != null) {
				_setDescription(attribute.getValue());
			}
		}
	}

	XmlLiteElement elementUrl = elementComponent.getChildElement(URL);
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
		
	// Plugins
	//--------
	XmlLiteElement[] elementPlugins = elementComponent.getChildElements(PLUGIN);
	
	PluginEntryDescriptorModel pluginDescriptor = null;
	
	for (int i = 0; i < elementPlugins.length; ++i) {
		
		pluginDescriptor = factory.createPluginEntryDescriptor();
		
		attribute = elementPlugins[i].getAttribute(PLUGIN_ID);
		if( attribute != null )
			pluginDescriptor._setId( attribute.getValue() );

		attribute = elementPlugins[i].getAttribute(PLUGIN_NAME);
		if (attribute != null)
			pluginDescriptor._setName(attribute.getValue());
			
		attribute = elementPlugins[i].getAttribute(PLUGIN_VERSION);
		if (attribute != null) {
			try {
				new VersionIdentifier(attribute.getValue());
			} catch (Exception ex) {
				return;
			}
			pluginDescriptor._setVersion( attribute.getValue() );
		}	       
		
		pluginDescriptor._setComponentId( componentID );
		pluginDescriptor._setCompInstallURL(_getInstallURL());
		_addToPluginEntriesRel(pluginDescriptor);

		// for each plugin, check to see if it's installed (can be found on path)
		URL path = UMEclipseTree.getPluginsURL(parent._getRegistryBase());
		String plugin_dir = pluginDescriptor._getId() + "_" + pluginDescriptor._getVersion();
		
//		pluginDescriptor._isInstalled(true);

		// where to install it locally (if selected for download)
		pluginDescriptor._setInstallURL(UMEclipseTree.getBaseInstallURL().toString() + plugin_dir + "/");	
	}
	
	// Fragments
	//----------
	XmlLiteElement[] elementFragments = elementComponent.getChildElements(FRAGMENT);
	
	FragmentEntryDescriptorModel fragmentDescriptor = null;
	
	for (int i = 0; i < elementFragments.length; ++i) {
		
		fragmentDescriptor = factory.createFragmentEntryDescriptor();
		
		attribute = elementFragments[i].getAttribute(FRAGMENT_ID);
		if( attribute != null )
			fragmentDescriptor._setId( attribute.getValue() );

		attribute = elementFragments[i].getAttribute(FRAGMENT_NAME);
		if (attribute != null)
			fragmentDescriptor._setName(attribute.getValue());
			
		attribute = elementFragments[i].getAttribute(FRAGMENT_VERSION);
		if (attribute != null) {
			try {
				new VersionIdentifier(attribute.getValue());
			} catch (Exception ex) {
				return;
			}
			fragmentDescriptor._setVersion( attribute.getValue() );
		}	  
		
		fragmentDescriptor._setComponentId( componentID );
		fragmentDescriptor._setCompInstallURL(_getInstallURL());
		_addToFragmentEntriesRel(fragmentDescriptor);

		// for each fragment, check to see if it's installed (can be found on path)
		URL path = UMEclipseTree.getPluginsURL(parent._getRegistryBase());
		String plugin_dir = fragmentDescriptor._getId() + "_" + fragmentDescriptor._getVersion();
//		fragmentDescriptor._isInstalled(true);

		// where to install it locally (if selected for download)
		fragmentDescriptor._setInstallURL(UMEclipseTree.getBaseInstallURL().toString() + plugin_dir + "/");			
	}

	// Register
	//---------
	_setUMRegistry(parent);
	parent._addToComponentProxysRel(this);

}
public ProductDescriptorModel _lookupContainingProducts(String key) {

	if(key == null) return null;
	
	Enumeration list = _enumerateContainingProductsRel();
	ProductDescriptorModel prod;
	while(list.hasMoreElements()) {
		prod = (ProductDescriptorModel) list.nextElement();
		if(key.equals(prod._getId())) return prod;
	}
	
	return null;
}
public FragmentEntryDescriptorModel _lookupFragmentEntries(String key) {

	if(key == null) return null;
	
	Enumeration list = _enumerateFragmentEntriesRel();
	FragmentEntryDescriptorModel plug;
	while(list.hasMoreElements()) {
		plug = (FragmentEntryDescriptorModel) list.nextElement();
		if(key.equals(plug._getId())) return plug;
	}
	
	return null;
}
public PluginEntryDescriptorModel _lookupPluginEntries(String key) {

	if(key == null) return null;
	
	Enumeration list = _enumeratePluginEntriesRel();
	PluginEntryDescriptorModel plug;
	while(list.hasMoreElements()) {
		plug = (PluginEntryDescriptorModel) list.nextElement();
		if(key.equals(plug._getId())) return plug;
	}
	
	return null;
}
public void _removeFromContainingProductsRel(Object o) {

	if (o==null || _containingProducts == null) return;
	_containingProducts.removeElement(o);		
}
public void _removeFromFragmentEntriesRel(Object o) {

	if (o==null || _fragmentEntries == null) return;
	_fragmentEntries.removeElement(o);		
}
public void _removeFromPluginEntriesRel(Object o) {

	if (o==null || _pluginEntries == null) return;
	_pluginEntries.removeElement(o);		
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
public void _setVersion(String version) {
	_version = version;
}
 public Object clone() throws CloneNotSupportedException{
	try {
		ComponentDescriptorModel clone = (ComponentDescriptorModel)super.clone();

		clone._containingProducts = (Vector) _containingProducts.clone();
		clone._pluginEntries = (Vector) _pluginEntries.clone();

		return clone;
	} catch (CloneNotSupportedException e) {
		return null;
	}
}
}
