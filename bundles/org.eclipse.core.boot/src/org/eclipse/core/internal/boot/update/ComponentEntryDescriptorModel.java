package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

 
import java.io.*;
import java.net.*;
import java.util.*;

public class ComponentEntryDescriptorModel  implements IManifestAttributes {
	
	// persistent properties (marshaled)
	private String _compInstallURL = null;		// actual component directory
	private String _prodInstallURL = null;
	private String _id = null;
	private String _name = null;
	private String _version = null;
	
	private String _dirName = null;			// dir name in install/components/
	private boolean _upgradeable = false;
	private boolean _selectable = false;
	private boolean _optional = false;

	private boolean _installed = false;
	private ProductDescriptorModel _containingProduct = null;


	// transient properties (not marshaled)
	private UMRegistryModel _umregistry = null;
/**
 * ComponentEntryDescriptorModel constructor comment.
 */
public ComponentEntryDescriptorModel() {
	super();
}
public String _getCompInstallURL() {
	
	return _compInstallURL;
}
public ProductDescriptorModel _getContainingProduct() {
	
	return _containingProduct;
}
/* Directory name in .install/.components, usually made up of
 * compid_version
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
public String _getProdInstallURL() {
	
	return _prodInstallURL;
}
public UMRegistryModel _getUMRegistry() {
	return _umregistry;
}
public String _getVersion() {
	
	return _version;
}
public boolean _isInstalled() {
	
	return _installed;
}
public void _isInstalled(boolean installed) {
	
	_installed = installed;
}
public boolean _isOptional() {
	
	return _optional;
}
public boolean _isUpgradeable() {
	
	return _upgradeable;
}
public void _setCompInstallURL(String path) {
	_compInstallURL = path;
}
public void _setContainingProduct(ProductDescriptorModel prod) {
	_containingProduct = prod;
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
public void _setOptional(String _option) {

	if (_option.equals(TRUE))
		_optional = true;
	
}
public void _setProdInstallURL(String path) {
	_prodInstallURL = path;
}
public void _setUMRegistry(UMRegistryModel registry) {
	_umregistry = registry;
}
public void _setUpgradeable(String _upgrade) {

	if (_upgrade.equals(TRUE))
		_upgradeable = true;
	
}
public void _setVersion(String version) {
	_version = version;
}
}
