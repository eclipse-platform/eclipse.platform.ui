package org.eclipse.core.internal.boot;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.net.*;
import java.util.*;
import java.io.*;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import org.eclipse.core.boot.BootLoader;
import com.ibm.oti.vm.VM;

public abstract class DelegatingURLClassLoader extends URLClassLoader {

	// loader base
	protected URL base;

	// delegation chain
	protected DelegateLoader[] imports = null;

	// extra resource class loader
	protected URLClassLoader resourceLoader = null;

	// filter table
	private Hashtable filterTable = new Hashtable();

	// development mode class path additions
	public static String devClassPath = null;

	// control class load tracing
	public static boolean DEBUG = false;
	public static boolean DEBUG_SHOW_CREATE = true;
	public static boolean DEBUG_SHOW_ACTIVATE = true;
	public static boolean DEBUG_SHOW_ACTIONS = true;
	public static boolean DEBUG_SHOW_SUCCESS = true;
	public static boolean DEBUG_SHOW_FAILURE = true;
	public static String[] DEBUG_FILTER_CLASS = new String[0];
	public static String[] DEBUG_FILTER_LOADER = new String[0];
	public static String[] DEBUG_FILTER_RESOURCE = new String[0];
	public static String[] DEBUG_FILTER_NATIVE = new String[0];
	
	public static final String PLUGIN = "plugin";

	private static boolean isHotSwapEnabled = InternalBootLoader.inDevelopmentMode() & ((VM.class.getModifiers() & java.lang.reflect.Modifier.ABSTRACT) == 0);
	
	private static String[] JAR_VARIANTS = buildJarVariants();
	private static String[] LIBRARY_VARIANTS = buildLibraryVariants();

	// DelegateLoader. Represents a single class loader this loader delegates to.
	protected static class DelegateLoader {

		private DelegatingURLClassLoader loader;
		private boolean isExported;

		public DelegateLoader(DelegatingURLClassLoader loader, boolean isExported) {
			this.loader = loader;
			this.isExported = isExported;
		}

		public Class loadClass(String name, DelegatingURLClassLoader current, DelegatingURLClassLoader requestor, Vector seen) {
			if (isExported || current == requestor)
				return loader.loadClass(name, false, requestor, seen, false);
			else
				return null;
		}

		public URL findResource(String name, DelegatingURLClassLoader current, DelegatingURLClassLoader requestor, Vector seen) {
			if (isExported || current == requestor)
				return loader.findResource(name, requestor, seen);
			else
				return null;
		}
		public Enumeration findResources(String name, DelegatingURLClassLoader current, DelegatingURLClassLoader requestor, Vector seen) {
			if (isExported || current == requestor)
				return loader.findResources(name, requestor, seen);
			else
				return null;
		}
	}

	// unchecked DelegatingLoaderException
	protected static class DelegatingLoaderException extends RuntimeException {
		Exception e = null;

		public DelegatingLoaderException() {
			super();
		}

		public DelegatingLoaderException(String message) {
			super(message);
		}

		public DelegatingLoaderException(String message, Exception e) {
			super(message);
			this.e = e;
		}

		public Throwable getException() {
			return e;
		}
		
		public void printStackTrace() {
			printStackTrace(System.err);
		}
		
		public void printStackTrace(PrintStream output) {
			synchronized (output) {
				if (e != null) {
					output.print("org.eclipse.core.internal.boot.DelegatingLoaderException: ");
					e.printStackTrace(output);
				} else
					super.printStackTrace(output);
			}
		}
		
		public void printStackTrace(PrintWriter output) {
			synchronized (output) {
				if (e != null) {
					output.print("org.eclipse.core.internal.boot.DelegatingLoaderException: ");
					e.printStackTrace(output);
				} else
					super.printStackTrace(output);
			}
		}
	}
	
private static String[] buildJarVariants() {
	ArrayList result = new ArrayList();
	
	result.add("ws/" + InternalBootLoader.getWS() + "/");
	result.add("os/" + InternalBootLoader.getOS() + "/" + InternalBootLoader.getOSArch() + "/");
	result.add("os/" + InternalBootLoader.getOS() + "/");
	String nl = InternalBootLoader.getNL();
	nl = nl.replace('_', '/');
	while (nl.length() > 0) {
		result.add("nl/" + nl + "/");
		int i = nl.lastIndexOf('/');
		nl = (i < 0) ? "" : nl.substring(0, i);
	}
	result.add("");
	return (String[])result.toArray(new String[result.size()]);
}

private static String[] buildLibraryVariants() {
	ArrayList result = new ArrayList();
	
	result.add("ws/" + InternalBootLoader.getWS() + "/");
	result.add("os/" + InternalBootLoader.getOS() + "/" + InternalBootLoader.getOSArch() + "/");
	result.add("os/" + InternalBootLoader.getOS() + "/");
	String nl = InternalBootLoader.getNL();
	nl = nl.replace('_', '/');
	while (nl.length() > 0) {
		result.add("nl/" + nl + "/");
		int i = nl.lastIndexOf('/');
		nl = (i < 0) ? "" : nl.substring(0, i);
	}
	result.add ("");
	return (String[])result.toArray(new String[result.size()]);
}

public DelegatingURLClassLoader(URL[] codePath, URLContentFilter[] codeFilters, URL[] resourcePath, URLContentFilter[] resourceFilters, ClassLoader parent) {

//	Instead of constructing the loader with supplied classpath, create loader
//	with empty path, "fix up" jar entries and then explicitly add the classpath
//	to the newly constructed loader

	super(mungeJarURLs (codePath), parent);
	resourcePath = mungeJarURLs(resourcePath);

	if (resourcePath != null && resourcePath.length > 0)
		resourceLoader = new ResourceLoader(resourcePath);

	if (codePath != null) {
		if (codeFilters == null || codeFilters.length != codePath.length)
			throw new DelegatingLoaderException();
		setHotSwapPath(this, codePath);
		for (int i = 0; i < codePath.length; i++) {
			if (codeFilters[i] != null)
				filterTable.put(codePath[i], codeFilters[i]);
		}
	}
	if (resourcePath != null) {
		if (resourceFilters == null || resourceFilters.length != resourcePath.length)
			throw new DelegatingLoaderException();
		for (int i = 0; i < resourcePath.length; i++) {
			if (resourceFilters[i] != null)
				filterTable.put(resourcePath[i], resourceFilters[i]);
		}
	}
}

/**
 * This method is to be used internally only for adding the proper class path and resource path
 * entries to the class loaders for Runtime and Xerces. They are special cases since they need
 * to be brought up before everything else. (and before the registry is loaded)
 */
public void addURLs(URL[] codePath, URLContentFilter[] codeFilters, URL[] resourcePath, URLContentFilter[] resourceFilters) {
	Set keys = filterTable.keySet();

	codePath = mungeJarURLs(codePath);
	resourcePath = mungeJarURLs(resourcePath);
	if (resourcePath != null && resourcePath.length > 0)
		resourceLoader = new ResourceLoader(resourcePath);

	if (codePath != null) {
		if (codeFilters == null || codeFilters.length != codePath.length)
			throw new DelegatingLoaderException();
		setHotSwapPath(this, codePath);
		for (int i=0; i<codePath.length; i++) {
			URL path = codePath[i];
			if (!keys.contains(path)) {
				addURL(path);
				filterTable.put(path, codeFilters[i]);
			}
		}
	}
	
	if (resourcePath != null) {
		if (resourceFilters == null || resourceFilters.length != resourcePath.length)
			throw new DelegatingLoaderException();
		for (int i = 0; i < resourcePath.length; i++) {
			URL path = resourcePath[i];
			if (resourceFilters[i] != null && !keys.contains(path))
				filterTable.put(path, resourceFilters[i]);
		}
	}
}

/**
 * strip-off jar: protocol
 */ 
private static URL mungeJarURL(URL url) {
	if (url.getProtocol().equals("jar")) {
		String file = url.getFile();
		if (file.startsWith("file:")) {
			int ix = file.indexOf("!/");
			if (ix != -1) file = file.substring(0,ix);
			try {
				url = new URL(file);
			} catch (MalformedURLException e) {
				// just use the original if we cannot create a new one
			}
		}
	}
	return url;
}

private static URL[] mungeJarURLs(URL[] urls) {
	if (urls == null) 
		return null;
	for (int i = 0; i < urls.length; i++) 
		urls[i] = mungeJarURL(urls[i]);
	return urls;
}

/**
 * Returns the given class or <code>null</code> if the class is not visible to the
 * given requestor.  The <code>inCache</code> flag controls how this action is
 * reported if in debug mode.
 */
protected Class checkClassVisibility(Class result, DelegatingURLClassLoader requestor, boolean inCache) {
	if (result == null)
		return null;
	if (isClassVisible(result, requestor)) {
		if (DEBUG && DEBUG_SHOW_SUCCESS && debugClass(result.getName()))
			debug("found " + result.getName() + " in " + (inCache ? "cache" : getURLforClass(result).toExternalForm()));
	} else {
		if (DEBUG && DEBUG_SHOW_ACTIONS && debugClass(result.getName()))
			debug("skip " + result.getName() + " in " + (inCache ? "cache" : getURLforClass(result).toExternalForm()));
		return null;
	}
	return result;
}
/**
 * Returns the given resource URL or <code>null</code> if the resource is not visible to the
 * given requestor.  
 */
protected URL checkResourceVisibility(String name, URL result, DelegatingURLClassLoader requestor) {
	if (result == null)
		return null;
	if (isResourceVisible(name, result, requestor)) {
		if (DEBUG && DEBUG_SHOW_SUCCESS && debugResource(name))
			debug("found " + result);
	} else {
		if (DEBUG && DEBUG_SHOW_ACTIONS && debugResource(name))
			debug("skip " + result);
		result = null;
	}
	return result;
}
protected void debug(String s) {

	System.out.println(toString()+"^"+Integer.toHexString(Thread.currentThread().hashCode())+" "+s);
}
protected boolean debugClass(String name) {
	
	if (debugLoader()) {
		return debugMatchesFilter(name,DEBUG_FILTER_CLASS);
	}
	return false;
}
protected void debugConstruction() {
	if (DEBUG && DEBUG_SHOW_CREATE && debugLoader()) {
		URL[] urls = getURLs();
		debug("Class Loader Created");
		debug("> baseURL=" + base);
		if (urls == null || urls.length == 0)
			debug("> empty search path");
		else {
			URLContentFilter filter;
			for (int i = 0; i < urls.length; i++) {
				debug("> searchURL=" + urls[i].toString());
				filter = (URLContentFilter) filterTable.get(urls[i]);
				if (filter != null)
					debug(">    export=" + filter.toString());
			}
		}
	}
}
protected String debugId() {
	return "";
}
protected boolean debugLoader() {
	
	return debugMatchesFilter(debugId(),DEBUG_FILTER_LOADER);
}
private boolean debugMatchesFilter(String name, String[] filter) {

	if (filter.length==0) return false;
	
	for (int i=0; i<filter.length; i++) {
		if (filter[i].equals("*")) return true;
		if (name.startsWith(filter[i])) return true;
	}
	return false;
}
protected boolean debugNative(String name) {
	
	if (debugLoader()) {
		return debugMatchesFilter(name,DEBUG_FILTER_NATIVE);
	}
	return false;
}
protected boolean debugResource(String name) {
	
	if (debugLoader()) {
		return debugMatchesFilter(name,DEBUG_FILTER_RESOURCE);
	}
	return false;
}
protected void enableHotSwap(ClassLoader cl, Class clazz) {
	if (isHotSwapEnabled)
		VM.enableClassHotSwap(clazz);
}
/**
 * Looks for the requested class in the parent of this loader using
 * standard Java protocols.  If the parent is null then the system class
 * loader is consulted.  <code>null</code> is returned if the class could
 * not be found.
 */
protected Class findClassParents(String name, boolean resolve) {
	try {
		ClassLoader parent = getParent();
		if (parent == null)
			return findSystemClass(name);
		return parent.loadClass(name);
	} catch (ClassNotFoundException e) {
	}
	return null;
}
/**
 * Finds and loads the class with the specified name from the URL search
 * path. Any URLs referring to JAR files are loaded and opened as needed
 * until the class is found.   Search on the parent chain and then self.
 *
 * Subclasses should implement this method.
 *
 * @param name the name of the class
 * @param resolve whether or not to resolve the class if found
 * @param requestor class loader originating the request
 * @param checkParents whether the parent of this loader should be consulted
 * @return the resulting class
 */
protected abstract Class findClassParentsSelf(String name, boolean resolve, DelegatingURLClassLoader requestor, boolean checkParents);
/**
 * Finds and loads the class with the specified name from the URL search
 * path. Any URLs referring to JAR files are loaded and opened as needed
 * until the class is found.  This method consults only the platform class loader.
 *
 * @param name the name of the class
 * @param resolve whether or not to resolve the class if found
 * @param requestor class loader originating the request
 * @param checkParents whether the parent of this loader should be consulted
 * @return the resulting class
 */
protected Class findClassPlatform(String name, boolean resolve, DelegatingURLClassLoader requestor, boolean checkParents) {
	DelegatingURLClassLoader platform = PlatformClassLoader.getDefault();
	if (this == platform)
		return null;
	return platform.findClassParentsSelf(name, resolve, requestor, false);
}
/**
 * Finds and loads the class with the specified name from the URL search
 * path. Any URLs referring to JAR files are loaded and opened as needed
 * until the class is found.  This method considers only the classes loadable
 * by its explicit prerequisite loaders.
 *
 * @param name the name of the class
 * @param requestor class loader originating the request
 * @param seen list of delegated class loaders already searched
 * @return the resulting class
 */
protected Class findClassPrerequisites(final String name, DelegatingURLClassLoader requestor, Vector seen) {
	if (imports == null)
		return null;
	if (seen == null)
		seen = new Vector(); // guard against delegation loops
	seen.addElement(this);
	// Grab onto the imports value to protect against concurrent write.
	DelegateLoader[] loaders = imports;
	for (int i = 0; i < loaders.length; i++) {
		Class result = loaders[i].loadClass(name, this, requestor, seen);
		if (result != null)
			return result;
	}
	return null;
}
/**
 * Finds the resource with the specified name on the URL search path.
 * This method is used specifically to find the file containing a class to verify
 * that the class exists without having to load it.
 * Returns a URL for the resource.  Searches only this loader's classpath.
 * <code>null</code> is returned if the resource cannot be found.
 *
 * @param name the name of the resource
 */
protected URL findClassResource(String name) {
	return super.findResource(name);
}
/**
 * Returns the absolute path name of a native library. The VM
 * invokes this method to locate the native libraries that belong
 * to classes loaded with this class loader. If this method returns
 * <code>null</code>, the VM searches the library along the path
 * specified as the <code>java.library.path</code> property.
 *
 * @param      libname   the library name
 * @return     the absolute path of the native library
 */
protected String findLibrary(String libName) {
	if (libName.length() == 0)
		return null;
	if (libName.charAt(0) == '/' || libName.charAt(0) == '\\')
		libName = libName.substring(1);
	libName = System.mapLibraryName(libName);

	if (DEBUG && DEBUG_SHOW_ACTIONS && debugNative(libName))
		debug("findLibrary(" + libName + ")");
	if (base == null)
		return null;
	String libFileName = null;
	if (base.getProtocol().equals(PlatformURLHandler.FILE)) {
		// directly access library	
		libFileName = (base.getFile() + libName).replace('/', File.separatorChar);
	} else {
		if (base.getProtocol().equals(PlatformURLHandler.PROTOCOL)) {
			URL[] searchList = getSearchURLs (base);
			if ((searchList != null) && (searchList.length != 0)) {
				URL foundPath = searchVariants(searchList, LIBRARY_VARIANTS, libName);
				if (foundPath != null) 
					libFileName = foundPath.getFile();
			}
		}
	}

	if (libFileName == null)
		return null;
		
	return new File(libFileName).getAbsolutePath();
}
/**
 * Finds the resource with the specified name on the URL search path.
 * Returns a URL for the resource. If resource is not found in own 
 * URL search path, delegates search to prerequisite loaders.
 * Null is returned if none of the loaders find the resource.
 *
 * @param name the name of the resource
 */
public URL findResource(String name) {
	return findResource(name, this, null);
}
/**
 * Delegated resource access call. 
 * Does not check prerequisite loader parent chain.
 */
protected URL findResource(String name, DelegatingURLClassLoader requestor, Vector seen) {
	// guard against delegation loops
	if (seen != null && seen.contains(this))
		return null;

	if (DEBUG && DEBUG_SHOW_ACTIONS && debugResource(name))
		debug("findResource(" + name + ")");

	// check the normal class path for self
	URL result = super.findResource(name);
	result = checkResourceVisibility(name, result, requestor);
	if (result != null)
		return result;

	// check our extra resource path if any
	if (resourceLoader != null) {
		result = resourceLoader.findResource(name);
		result = checkResourceVisibility(name, result, requestor);
		if (result != null)
			return result;
	}

	// delegate down the prerequisite chain if we haven't found anything yet.
	if (imports != null) {
		if (seen == null)
			seen = new Vector(); // guard against delegation loops
		seen.addElement(this);
		for (int i = 0; i < imports.length && result == null; i++)
			result = imports[i].findResource(name, this, requestor, seen);
	}
	return result;
}
/**
 * Returns an Enumeration of URLs representing all of the resources
 * on the URL search path having the specified name.
 *
 * @param name the resource name
 */
public Enumeration findResources(String name) throws IOException {
	return findResources(name, this, null);
}
/**
 * Delegated call to locate all named resources. 
 * Does not check prerequisite loader parent chain.
 */
private Enumeration findResources(String name, DelegatingURLClassLoader requestor, Vector seen) {
	// guard against delegation loops
	if (seen != null && seen.contains(this))
		return null;

	if (DEBUG && DEBUG_SHOW_ACTIONS && debugResource(name))
		debug("findResources(" + name + ")");

	// check own URL search path
	Enumeration e = null;
	try {
		e = super.findResources(name);
	} catch (IOException ioe) {
		//fall through and search prerequisites
	}
	ResourceEnumeration result = new ResourceEnumeration(name, e, this, requestor);

	// delegate down the prerequisite chain
	if (imports != null) {
		if (seen == null)
			seen = new Vector(); // guard against delegation loops
		seen.addElement(this);
		for (int i = 0; i < imports.length; i++)
			result.add(imports[i].findResources(name, this, requestor, seen));
	}

	return result;
}
protected String getFileFromURL(URL target) {
	try {
		URL url = InternalBootLoader.resolve(target);
		String protocol = url.getProtocol();
		// check only for the file protocol here.  Not interested in Jar files.
		if (protocol.equals(PlatformURLHandler.FILE))
			return url.getFile();
	} catch (IOException e) {
		//couldn't resolve the target - return null
	}
	return null;
}

private URL[] getSearchURLs (URL base) {
	URL[] auxList = null;
	ArrayList result = new ArrayList();

	PlatformURLConnection c = null;
	try {
		c = (PlatformURLConnection) base.openConnection();
		result.add(c.getURLAsLocal());
	} catch (IOException e) {
		// Catch intentionally left empty.  Skip 
		// poorly formed URLs
	}

	try {
		auxList = c.getAuxillaryURLs();
		int auxLength = (auxList == null) ? 0 : auxList.length;
	
		// Now add the fragment URLs to the result
		for (int i = 0; i < auxLength; i++) {
			try {
				c = (PlatformURLConnection) auxList[i].openConnection();
				result.add(c.getURLAsLocal());
			} catch (IOException e) {
				// Catch intentionally left empty.  Skip 
				// poorly formed URLs
			}
		}
	} catch (IOException e) {
		// Catch intentionally left empty.  Skip 
		// poorly formed URLs
	}
	
	return (URL[])result.toArray(new URL[result.size()]);
}

private URL searchVariants (URL[] basePaths, String[] variants, String path) {
	// This method assumed basePaths are 'resolved' URLs
	for (int i = 0; i < variants.length; i++) {
		for (int j = 0; j < basePaths.length; j++) {
			String fileName = basePaths[j].getFile() + variants[i] + path;
			File file = new File(fileName);
			if (!file.exists()) {
				if (DEBUG && DEBUG_SHOW_FAILURE)
					debug("not found " + file.getAbsolutePath());
			} else {	
				if (DEBUG && DEBUG_SHOW_SUCCESS)
					debug("found " + path + " as " + file.getAbsolutePath());
				try {
					return new URL ("file:" + fileName);
				} catch (MalformedURLException e) {
					// Intentionally ignore this exception
					// so we continue looking for a matching
					// URL.
				}
			}		
		}
	}
	return null;
}
public URL getResource(String name) {
	if (DEBUG && DEBUG_SHOW_ACTIONS && debugResource(name))
		debug("getResource(" + name + ")");

	URL result = super.getResource(name);
	if (result == null) {
		if (DEBUG && DEBUG_SHOW_FAILURE && debugResource(name))
			debug("not found " + name);
	}
	return result;
}
private URL getURLforClass(Class clazz) {
	ProtectionDomain pd = clazz.getProtectionDomain();
	if (pd != null) {
		CodeSource cs = pd.getCodeSource();
		if (cs != null)
			return cs.getLocation();
	}
	if (DEBUG && DEBUG_SHOW_ACTIONS && debugClass(clazz.getName()))
		debug("*** " + clazz.getName());
	return null;
}
public void initializeImportedLoaders() {
}
/**
 * check to see if class is visible (exported)
 */
boolean isClassVisible(Class clazz, DelegatingURLClassLoader requestor) {
	URL lib = getURLforClass(clazz);
	if (lib == null)
		return true; // have a system class (see comment below)

	URLContentFilter filter = (URLContentFilter) filterTable.get(lib);
	if (filter == null) {
		// This code path is being executed because some VMs (eg. Sun JVM)
		// return from the class cache classes that were not loaded
		// by this class loader. Consequently we do not find the 
		// corresponding jar filter. This appears to be a performance
		// optimization that we are defeating with our filtering scheme.
		// We return the class if it is a system class (see above). Otherwise
		// we reject the class which caused the load to be
		// delegated down the prerequisite chain until we find the
		// correct loader.
		if (DEBUG && DEBUG_SHOW_ACTIONS && debugClass(clazz.getName()))
			debug("*** Unable to find library filter for " + clazz.getName() + " from " + lib);
		return false;
	} else
		return filter.isClassVisible(clazz, this, requestor);
}
/**
 * check to see if resource is visible (exported)
 */
boolean isResourceVisible(String name, URL resource, DelegatingURLClassLoader requestor) {
	URL lib = null;
	String file = resource.getFile();
	try {
		lib = new URL(resource.getProtocol(), resource.getHost(), file.substring(0, file.length() - name.length()));
	} catch (MalformedURLException e) {
		if (DEBUG)
			debug("Unable to determine resource lib for " + name + " from " + resource);
		return false;
	}

	URLContentFilter filter = (URLContentFilter) filterTable.get(lib);
	// retry with non-jar URL if necessary
	if (filter == null) filter = (URLContentFilter) filterTable.get(mungeJarURL(lib));
	if (filter == null) {
		if (DEBUG)
			debug("Unable to find library filter for " + name + " from " + lib);
		return false;
	} else
		return filter.isResourceVisible(name, this, requestor);
}
/**
 * Non-delegated load call.  This method is not synchronized.  Implementations of
 * findClassParentsSelf, and perhaps others, should synchronize themselves as
 * required.  Synchronizing this method is too coarse-grained.   It results in plugin
 * activation being synchronized and may cause deadlock.
 */
protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
	if (DEBUG && DEBUG_SHOW_ACTIONS && debugClass(name))
		debug("loadClass(" + name + ")");
	Class result = loadClass(name, resolve, this, null, true);
	if (result == null) {
		if (DEBUG && DEBUG_SHOW_FAILURE && debugClass(name))
			debug("not found " + name);
		throw new ClassNotFoundException(name);
	}
	return result;
}
/**
 * Delegated load call.  This method is not synchronized.  Implementations of
 * findClassParentsSelf, and perhaps others, should synchronize themselves as
 * required.  Synchronizing this method is too coarse-grained.   It results in plugin
 * activation being synchronized and may cause deadlock.
 */
private Class loadClass(String name, boolean resolve, DelegatingURLClassLoader requestor, Vector seen, boolean checkParents) {
	// guard against delegation loops
	if (seen != null && seen.contains(this))
		return null;

	// look in the parents and self
	Class result = findClassParentsSelf(name, resolve, requestor, checkParents);

	// search platform
	if (result == null)
		result = findClassPlatform(name, resolve, requestor, false);

	// search prerequisites
	if (result == null)
		result = findClassPrerequisites(name, requestor, seen);

	// if we found a class, consider resolving it
	if (result != null && resolve)
		resolveClass(result);

	return result;
}
private void setHotSwapPath(ClassLoader cl, URL[] urls) {
	if (!isHotSwapEnabled)
		return;
	StringBuffer path = new StringBuffer();
	for(int i = 0; i < urls.length; i++) {
		String file = getFileFromURL (urls[i]);
		if (file != null) {
			if (file.charAt(0) == '/')
				file = file.substring(1, file.length());
			if (file.charAt(file.length() - 1) == '/')
				file = file.substring(0, file.length() - 1);
			if (path.length() > 0)
				path.append(";");
			path.append(file);
		}
	}
	if (path.length() > 0)
		VM.setClassPathImpl(cl, path.toString());
}
protected void setImportedLoaders(DelegateLoader[] loaders) {
	
	imports = loaders;
	
	if(DEBUG && DEBUG_SHOW_CREATE && debugLoader()) {
		debug("Imports");
		if (imports==null || imports.length==0) debug("> none");
		else {
			for (int i=0; i<imports.length; i++) {
				debug("> " + imports[i].loader.toString() + " export=" + imports[i].isExported);
			}
		}
	}
}
public String toString() {
	return "Loader [" + debugId() + "]";
}
}
