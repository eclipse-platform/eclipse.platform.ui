package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
 
import java.io.*;
import java.util.*;

import java.net.*;


public class UMRegistryManagerModel   {
		// persistent properties (marshaled)
	private Vector _prod_download_list;
	private Vector _comp_download_list;

		// transient properties (not marshaled)

/**
 * UMRegistryModel constructor comment.
 */
public UMRegistryManagerModel() {
	super();
}
public void _addToComponentsToDownloadRel(Object o) {

	if (_comp_download_list == null) _comp_download_list = new Vector();
	_comp_download_list.addElement(o);
}
public void _addToProductsToDownloadRel(Object o) {

	if (_prod_download_list == null) _prod_download_list = new Vector();
	_prod_download_list.addElement(o);
}
public void _copyComponentsToDownloadRelInto(Object[] array) {

	if (_comp_download_list != null) _comp_download_list.copyInto(array);
}
public void _copyProductsToDownloadRelInto(Object[] array) {

	if (_prod_download_list != null) _prod_download_list.copyInto(array);
}
public Enumeration _enumerateComponentsToDownloadRel() {

	if (_comp_download_list == null) return (new Vector()).elements();
	else return _comp_download_list.elements();
}
public Enumeration _enumerateProductsToDownloadRel() {

	if (_prod_download_list == null) return (new Vector()).elements();
	else return _prod_download_list.elements();
}
public Vector _getComponentsToDownloadRel() {
	
	return _comp_download_list;
}
public Vector _getProductsToDownloadRel() {
	
	return _prod_download_list;
}
public int _getSizeOfComponentsToDownloadRel() {

	if (_comp_download_list == null) return 0;
	else return _comp_download_list.size();
}
public int _getSizeOfProductsToDownloadRel() {

	if (_prod_download_list == null) return 0;
	else return _prod_download_list.size();
}
}
