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

public class PluginEntryDescriptorModel implements IManifestAttributes {

	// persistent properties (marshaled)
	private String _id = null;
	private String _name = null;
	private String _version = null;
	private String _installURL = null;		// where plug-in is installed
	private Vector _files = null;
	
	private String _componentId = null;
	private String _compInstallURL = null;					// component directory
	private boolean _installed = false;

	// transient properties (not marshaled)

/**
 * PluginEntryDescriptorModel constructor comment.
 */
public PluginEntryDescriptorModel() {
	super();
}
public void _addToFilesRel(Object o) {
	
	if (_files == null) _files = new Vector();
	_files.addElement(o);
	
}
public void _copyFilesRelInto(Object[] array) {

	if (_files != null) _files.copyInto(array);
}
public Enumeration _enumerateFilesRel() {

	if (_files == null) return (new Vector()).elements();
	return _files.elements();
}
public String _getCompInstallURL() {
	
	return _compInstallURL;
}
public String _getComponentId() {
	
	return _componentId;
}
public Vector _getFilesRel() {
	
	return _files;
}
public String _getId() {
	
	return _id;
}
public String _getInstallURL() {
	
	return _installURL;
}
public String _getName() {
	
	return _name;
}
public int _getSizeOfFilesRel() {

	if (_files == null) return 0;
	else return _files.size();
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
public String _lookupFiles(String key) {

	if(key == null) return null;
	
	Enumeration list = _enumerateFilesRel();
	String loc;
	while(list.hasMoreElements()) {
		loc = (String) list.nextElement();
		if(key.equals(loc)) return loc;
	}
	
	return null;
}
public void _removeFromFilesRel(Object o) {

	if (o==null || _files == null) return;
	_files.removeElement(o);		
}
public void _setCompInstallURL(String path) {
	_compInstallURL = path;
}
public void _setComponentId(String id) {
	_componentId = id;
}
public void _setId(String id) {
	 _id = id;
}
public void _setInstallURL(String installURL) {
	_installURL = installURL;
}
public void _setName(String name) {
	_name = name;
}
public void _setVersion(String version) {
	_version = version;
}
}
