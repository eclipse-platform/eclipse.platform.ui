/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.boot;

import java.util.*;

/**
 * Represents class name filter for a given classpath entry. The filter
 * elements use the mask convention used by Java import statements.
 * Mask element "*" implies the full content is public.
 */
public class URLContentFilter {
	private Hashtable filterTable = null;
	private boolean isPublic = false;
	private boolean isExported = false;
public URLContentFilter(String[] filter) {
	if (filter==null || filter.length==0) return;

	String entry;
	for(int i=0; i<filter.length; i++) {
		entry = filter[i].trim();
		if (!entry.equals("")) { //$NON-NLS-1$
			isExported = true;
			if (entry.equals("*")) isPublic = true; //$NON-NLS-1$
			else addMask(entry);
		}
	}			
}
public URLContentFilter(boolean isPublic) {
	if (isPublic) {
		this.isExported = true;
		this.isPublic = true;
	}
}
private void addMask(String name) {

	if (filterTable==null) filterTable = new Hashtable();
	filterTable.put(name,this);
}
private boolean classMatchesFilter(String name) {

	int i = name.lastIndexOf("."); //$NON-NLS-1$
	if (i!=-1 && filterTable.get(name.substring(0,i)+".*")!=null) return true; //$NON-NLS-1$
	else if(filterTable.get(name)!=null) return true;
	else return false;
}
boolean isClassVisible(Class clazz, DelegatingURLClassLoader current, DelegatingURLClassLoader requestor) {
	return isClassVisible(clazz.getName(), current, requestor);
}
boolean isClassVisible(String className, DelegatingURLClassLoader current, DelegatingURLClassLoader requestor) {
	if (requestor==current) return true;		// request from own loader ... full access
	if (isPublic) return true;					// public library ... full access from other loaders
	if (!isExported) return false;				// private library ... no access from other loaders
	else return classMatchesFilter(className);	// exported library ... match against filters
}
boolean isResourceVisible(String resName, DelegatingURLClassLoader current, DelegatingURLClassLoader requestor) {
	if (requestor==current) return true;		// request from own loader ... full access
	if (isPublic) return true;					// public library ... full access from other loaders
	if (!isExported) return false;				// private library ... no access from other loaders
	else return resourceMatchesFilter(resName);	// exported library ... match against filters
}
private boolean resourceMatchesFilter(String name) {

	int i = name.lastIndexOf("/"); //$NON-NLS-1$
	String tmp = name.replace('/','.');
	if (i!=-1 && filterTable.get(tmp.substring(0,i)+".*")!=null) return true; //$NON-NLS-1$
	else if(filterTable.get(tmp)!=null) return true;
	else return false;
}
public String toString() {
	if (isPublic) return "*"; //$NON-NLS-1$
	if (!isExported) return "<private>"; //$NON-NLS-1$
	Enumeration keys = filterTable.keys();
	String mask = ""; //$NON-NLS-1$
	String sep = ""; //$NON-NLS-1$
	while(keys.hasMoreElements()) {
		mask += sep+(String)keys.nextElement();
		sep = " + "; //$NON-NLS-1$
	}
	return mask;
}
}
