package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
import java.io.Serializable;
public class URLNamePairModel implements IManifestAttributes, Serializable {
	private String _name = null;
	private String _url = null;

	private String _installURL = null;		// where prod/component is installed
/**
 * URLNamePairModel constructor comment.
 */
public URLNamePairModel() {
	super();
}
public String _getInstallURL() {
	
	return _installURL;
}
public String _getName() {
	
	return _name;
}
public String _getURL() {
	
	return _url;
}
public void _setInstallURL(String installURL) {
	_installURL = installURL;
}
public void _setName(String name) {
	_name = name;
}
public void _setURL(String url) {
	_url = url;
}
}
