package org.eclipse.core.internal.boot;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.URL;
 
/**
 * This is the singleton platform loader. It searches jars and
 * directories containing the base runtime support.
 */
public final class PlatformClassLoader extends DelegatingURLClassLoader {
	private static PlatformClassLoader singleton = null;
/**
 * Creates the default instance of PlatformClassLoader according to the
 * given classpath, filters and parent.
 */
public PlatformClassLoader(URL[] searchPath, URLContentFilter[] filters, ClassLoader parent, URL base) {
	super(searchPath, filters, null, null, parent);
	this.base = base;
	if (singleton == null)
		singleton = this;
	debugConstruction(); // must have initialized loader
}
protected String debugId() {
	return "PLATFORM";
}
/**
 * Finds and loads the class with the specified name from the URL search
 * path. Any URLs referring to JAR files are loaded and opened as needed
 * until the class is found. Only our own URL search path and that of our parent
 * is used.  This method consults this loader's parent first but only if <code>checkParents</code>
 * is <code>true</code>.  Following that, this loader's own search path is checked. 
 * <code>null</code> is returned if the class cannot be found.
 
 * @param name the name of the class
 * @param resolve whether to resolve any loaded class
 * @param requestor class loader originating the request
 * @param checkParents whether to check the parent loader
 * @return the resulting class
 */
protected Class findClassParentsSelf(final String name, boolean resolve, DelegatingURLClassLoader requestor, boolean checkParents) {
	Class result = null;
	synchronized (this) {
		// check the cache.  If we find something, check to see if its visible.
		// If it is, return it.  If not, return null if we are not checking parents.  There is
		// no point in looking in self as the class was already in the cache.
		result = findLoadedClass(name);
		if (result != null) {
			result = checkClassVisibility(result, requestor, true);
			if (result != null || !checkParents)
				return result;
		}

		// if it wasn't in the cache or was not visible, check the parents (if requested)
		if (checkParents) {
			result = findClassParents(name, resolve);
			if (result != null)
				return result;
		}
		try {
			result = super.findClass(name);
			// If the class is loaded in this classloader register it with
			// the hot swap support.  Need to do this regardless of visibility
			// because the class was actually loaded.
			if (result == null)
				return null;
			enableHotSwap(this, result);
			return checkClassVisibility(result, requestor, false);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}
/**
 * Returns the default/singleton instance of PlatformClassLoader.
 * The class loader must have been explicitly created previously
 * otherwise this method returns <code>null</code>.
 */
public static PlatformClassLoader getDefault() {
	return singleton;	
}
/**
 * Sets the list of imported loaders.  If the supplied list is <code>null</code>
 * then this loader's list of imports is cleared.
 */
public synchronized void setImports(DelegatingURLClassLoader[] loaders) {
	if (loaders == null) {
		imports = null;
		return;
	}
	DelegateLoader[] delegates = new DelegateLoader[loaders.length];
	for (int i = 0; i < loaders.length; i++)
		delegates[i] = new DelegateLoader(loaders[i], false);
	imports = delegates;
}
}
