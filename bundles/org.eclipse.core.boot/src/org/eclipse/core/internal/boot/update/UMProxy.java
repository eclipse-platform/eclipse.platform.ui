package org.eclipse.core.internal.boot.update;

// holds different versions of an entity (prod, comp or plug-in) that have the same id
import java.util.*;
public class UMProxy {
	private String _id;
	private Map _versions ;	// sorted versions
/**
 * UMProxy constructor comment.
 */
public UMProxy(String id)      { 
	_id = id;
	_versions = null;
}
/**
 * Insert the method's description here.
 * Creation date: (5/2/01 1:58:01 PM)
 * @param o java.lang.Object
 * @param key java.lang.String
 */
public void _addToVersionsRel(Object o, String key) {
	if (_versions == null)  
		_versions = Collections.synchronizedMap(new TreeMap(new VersionComparator()));	
	_versions.put(key,o);
}
public Object _getEarliestVersion() {
	TreeMap tm = new TreeMap(_versions);
	String key = tm.firstKey().toString();
	return _versions.get(key);
}
public Object _getLatestVersion() {
	TreeMap tm = new TreeMap(_versions);
	String key = tm.lastKey().toString();
	return _versions.get(key);
}
public Map _getVersionsRel() {
	return _versions;
}
public Object _lookupVersion(String key) {
	if(key == null) return null;
	if (_versions == null) return null;
	return _versions.get(key);
}
}
