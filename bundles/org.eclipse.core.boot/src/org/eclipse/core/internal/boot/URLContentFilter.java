package org.eclipse.core.internal.boot;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
		if (!entry.equals("")) {
			isExported = true;
			if (entry.equals("*")) isPublic = true;
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

	int i = name.lastIndexOf(".");
	if (i!=-1 && filterTable.get(name.substring(0,i)+".*")!=null) return true;
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

	int i = name.lastIndexOf("/");
	String tmp = name.replace('/','.');
	if (i!=-1 && filterTable.get(tmp.substring(0,i)+".*")!=null) return true;
	else if(filterTable.get(tmp)!=null) return true;
	else return false;
}
public String toString() {
	if (isPublic) return "*";
	if (!isExported) return "<private>";
	Enumeration keys = filterTable.keys();
	String mask = "";
	String sep = "";
	while(keys.hasMoreElements()) {
		mask += sep+(String)keys.nextElement();
		sep = " + ";
	}
	return mask;
}
}
