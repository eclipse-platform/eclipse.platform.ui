package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */

import java.net.*;
import java.util.*;

/**
 * Get/Set of elements from jar manifest
 */
public class ManifestDescriptorModel implements IManifestAttributes {

	// persistent properties (marshaled)
	private String _manifestVersion = null;
	private String _manifestType = null;

	private String _installURL = null;
	private Vector _updateURLs = null;
	private Vector _discoveryURLs = null;

	// transient properties (not marshaled)
	private UMRegistryModel _umregistry = null;



public void _addToDiscoveryURLsRel(Object o) {
	
	if (_discoveryURLs == null) _discoveryURLs = new Vector();
	_discoveryURLs.addElement(o);
	
}
public void _addToUpdateURLsRel(Object o) {
	
	if (_updateURLs == null) _updateURLs = new Vector();
	_updateURLs.addElement(o);
	
}
public void _copyDiscoveryURLsRelInto(Object[] array) {

	if (_discoveryURLs != null) _discoveryURLs.copyInto(array);
}
public void _copyUpdateURLsRelInto(Object[] array) {

	if (_updateURLs != null) _updateURLs.copyInto(array);
}
public Enumeration _enumerateDiscoveryURLsRel() {

	if (_discoveryURLs == null) return (new Vector()).elements();
	return _discoveryURLs.elements();
}
public Enumeration _enumerateUpdateURLsRel() {

	if (_updateURLs == null) return (new Vector()).elements();
	return _updateURLs.elements();
}
public Vector _getDiscoveryURLsRel() {
	
	return _discoveryURLs;
}
public String _getInstallManifestURL() {
	return  _installURL + "/" + INSTALL_MANIFEST;
}
public String _getInstallURL() {

	return _installURL;
}
public String _getManifestDirURL() {
	return _installURL + "/" + MANIFEST_DIR ;

}
public String _getManifestType() {
	
	return _manifestType;
}
public String _getManifestURL() {
	return _getManifestDirURL() + "/" + MANIFEST_FILE;
}
public String _getManifestVersion() {
	
	return _manifestVersion;
}
public int _getSizeOfDiscoveryURLsRel() {

	if (_discoveryURLs == null) return 0;
	else return _discoveryURLs.size();
}
public int _getSizeOfUpdateURLsRel() {

	if (_updateURLs == null) return 0;
	else return _updateURLs.size();
}
public UMRegistryModel _getUMRegistry() {
	return _umregistry;
}
public Vector _getUpdateURLsRel() {
	
	return _updateURLs;
}
public URLNamePairModel _lookupDiscoveryURL(URL key) {

	if(key == null) return null;
	
	Enumeration list = _enumerateDiscoveryURLsRel();
	URLNamePairModel urlNamePair;
	while(list.hasMoreElements()) {
		urlNamePair = (URLNamePairModel) list.nextElement();
		if(key.equals(urlNamePair._getURL())) return urlNamePair;
	}
	
	return null;
}
public URLNamePairModel _lookupUpdateURL(URL key) {

	if(key == null) return null;
	
	Enumeration list = _enumerateUpdateURLsRel();
	URLNamePairModel urlNamePair;
	while(list.hasMoreElements()) {
		urlNamePair = (URLNamePairModel) list.nextElement();
		if(key.equals(urlNamePair)) return urlNamePair;
	}
	
	return null;
}
public void _removeFromDiscoveryURLsRel(Object o) {

	if (o==null || _discoveryURLs == null) return;
	_discoveryURLs.removeElement(o);		
}
public void _removeFromUpdateURLsRel(Object o) {

	if (o==null || _updateURLs == null) return;
	_updateURLs.removeElement(o);		
}
public void _setInstallURL(String url) {
	_installURL = url;
}
public void _setManifestType(String type) {
	 _manifestType = type;
}
public void _setManifestVersion(String ver) {
	 _manifestVersion = ver;
}
public void _setUMRegistry(UMRegistryModel registry) {
	_umregistry = registry;
}
 public Object clone() throws CloneNotSupportedException{
	try {
		ManifestDescriptorModel clone = (ManifestDescriptorModel)super.clone();
		clone._discoveryURLs = (Vector) _discoveryURLs.clone();
		clone._updateURLs = (Vector) _updateURLs.clone();
		return clone;
	} catch (CloneNotSupportedException e) {
		return null;
	}
}
/**
 * convert a list of comma-separated tokens into an array
 */
private static URL[] getURLArrayFromList(String prop) throws MalformedURLException{
	if (prop == null || prop.trim().equals(""))
		return new URL[0];
	Vector list = new Vector();
	StringTokenizer tokens = new StringTokenizer(prop, ",");
	while (tokens.hasMoreTokens()) {
		String token = tokens.nextToken().trim();
		if (!token.equals(""))
			list.addElement(new URL(token));
	}
	return list.isEmpty() ? new URL[0] : (URL[]) list.toArray(new URL[0]);
}
}
