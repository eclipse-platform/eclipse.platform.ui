package org.eclipse.core.internal.boot;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Platform URL support
 */

import java.net.*;
import java.io.*;
import java.util.*;
 
public abstract class PlatformURLConnection extends URLConnection {

	// URL access
	private boolean isInCache = false;
	private boolean isJar = false;
	
//	protected URL url;				// declared in super (platform: URL)
	private URL resolvedURL = null;	// resolved file URL (eg. http: URL)
	private URL cachedURL = null;	// file URL in cache (file: URL)

	private URLConnection connection = null; // actual connection

	// local cache
	private static Properties cacheIndex = new Properties();
	private static String cacheLocation;
	private static String indexName;
	private static String filePrefix;

	// constants	
	private static final int BUF_SIZE = 32768;
	private static final Object NOT_FOUND = new Object();	// marker
	private static final String CACHE_PROP = ".cache.properties";
	private static final String CACHE_LOCATION_PROP = "location";
	private static final String CACHE_INDEX_PROP = "index";
	private static final String CACHE_PREFIX_PROP = "prefix";
	private static final String CACHE_INDEX = ".index.properties";
	private static final String CACHE_DIR = PlatformURLHandler.PROTOCOL + File.separator;

	// debug tracing
	public static boolean DEBUG = false;
	public static boolean DEBUG_CONNECT = true;
	public static boolean DEBUG_CACHE_LOOKUP = true;
	public static boolean DEBUG_CACHE_COPY = true;
protected PlatformURLConnection(URL url) {
	super(url);
}
protected boolean allowCaching() {
	return false;
}
public void connect() throws IOException {
	connect(false);
}
private synchronized void connect(boolean asLocal) throws IOException {
	if (!connected) {
		if (shouldCache(asLocal)) {
			try {				
				URL inCache = getURLInCache();
				if (inCache!=null) connection = inCache.openConnection();
			} catch(IOException e) {
				// failed to cache ... will use resolved URL instead
			}
		}

		// use resolved URL
		if (connection==null) connection = resolvedURL.openConnection();
		connected = true;
		if (DEBUG && DEBUG_CONNECT)
			debug("Connected as "+connection.getURL());
	}
}
private void copyToCache() throws IOException {

	if (isInCache | cachedURL==null) return;
	String tmp;
	int ix;
		
	// cache entry key
	String key;
	if (isJar) {
		tmp = url.getFile();
		ix = tmp.lastIndexOf(PlatformURLHandler.JAR_SEPARATOR);
		if (ix!=-1) tmp = tmp.substring(0,ix);
		key = tmp;
	}
	else key = url.getFile();
		
	// source url
	URL src;
	if (isJar) {
		tmp = resolvedURL.getFile();
		ix = tmp.lastIndexOf(PlatformURLHandler.JAR_SEPARATOR);
		if (ix!=-1) tmp = tmp.substring(0,ix);
		src = new URL(tmp);
	}
	else src = resolvedURL;
	InputStream srcis = null;
	
	// cache target
	String tgt;
	if (isJar) {
		tmp = cachedURL.getFile();
		ix = tmp.indexOf(PlatformURLHandler.PROTOCOL_SEPARATOR);
		if (ix!=-1) tmp = tmp.substring(ix+1);
		ix = tmp.lastIndexOf(PlatformURLHandler.JAR_SEPARATOR);
		if (ix!=-1) tmp = tmp.substring(0,ix);
		tgt = tmp;
	}
	else tgt = cachedURL.getFile();	
	File tgtFile = null;
	FileOutputStream tgtos = null;
	
	boolean error = false;
	long total = 0;

	try {
		if (DEBUG && DEBUG_CACHE_COPY) {
			if (isJar) debug ("Caching jar as "+tgt);
			else debug("Caching as "+tgt);
		}
			
		srcis = src.openStream();
		byte[] buf = new byte[BUF_SIZE];
		int count = srcis.read(buf);

		tgtFile = new File(tgt);
		tgtos = new FileOutputStream(tgtFile);
		
		while(count!=-1) {
			total += count;
			tgtos.write(buf,0,count);
			count = srcis.read(buf);
		}

		srcis.close();
		srcis=null;
		tgtos.close();
		tgtos=null;

		// add cache entry
		cacheIndex.put(key,tgt);
		isInCache = true;
	}
	catch(IOException e) {
		error = true;
		cacheIndex.put(key,NOT_FOUND);	// mark cache entry for this execution
		if (DEBUG && DEBUG_CACHE_COPY)
			debug("Failed to cache due to "+e);
		throw e;		
	}
	finally {		
		if (!error && DEBUG && DEBUG_CACHE_COPY)
			debug(total + " bytes copied");
		if (srcis!=null) srcis.close();
		if (tgtos!=null) tgtos.close();
	}
}
protected void debug(String s) {

	System.out.println("URL "+getURL().toString()+"^"+Integer.toHexString(Thread.currentThread().hashCode())+" "+s);
}
private static void debugStartup(String s) {

	System.out.println("URL "+s);
}
public URL[] getAuxillaryURLs () throws IOException {
	return null;
}
public synchronized InputStream getInputStream() throws IOException {
	if (!connected) connect();
	return connection.getInputStream();
}
public URL getResolvedURL() {
	return resolvedURL;
}
public URL getURLAsLocal() throws IOException {
	connect(true);	// connect and force caching if necessary
	URL u = connection.getURL();
	String up = u.getProtocol();
	if (!up.equals(PlatformURLHandler.FILE) && !up.equals(PlatformURLHandler.JAR) && !up.equals(PlatformURLHandler.VA)) throw new IOException("Unable to access URL as local "+url.toString());
	return u;
}
private URL getURLInCache() throws IOException {

	if (!allowCaching()) return null;	// target should not be cached
	
	if (isInCache) return cachedURL;

	if (cacheLocation==null | cacheIndex==null) return null;	// not caching
	
	// check if we are dealing with a .jar/ .zip
	String file = "";
	String jarEntry = null;
	if (isJar) {
		file = url.getFile();
		int ix = file.lastIndexOf(PlatformURLHandler.JAR_SEPARATOR);
		if (ix!=-1) {
			jarEntry = file.substring(ix+PlatformURLHandler.JAR_SEPARATOR.length());
			file = file.substring(0,ix);
		}
	}
	else {
		file = url.getFile();
		jarEntry = null;
	}
	
	// check for cached entry
	String tmp = (String)cacheIndex.get(file);

	// check for "not found" marker
	if (tmp!=null && tmp==NOT_FOUND) throw new IOException();

	// validate cache entry
	if (tmp!=null && !(new File(tmp)).exists()) {
		tmp = null;
		cacheIndex.remove(url.getFile());
	}

	// found in cache
	if (tmp!=null) {
		if (isJar) {
			if (DEBUG && DEBUG_CACHE_LOOKUP)
				debug("Jar located in cache as "+tmp);
			tmp = PlatformURLHandler.FILE + PlatformURLHandler.PROTOCOL_SEPARATOR + tmp + PlatformURLHandler.JAR_SEPARATOR + jarEntry;
			cachedURL = new URL(PlatformURLHandler.JAR,null,-1,tmp);
		}
		else {
			if (DEBUG && DEBUG_CACHE_LOOKUP)
				debug("Located in cache as "+tmp);
			cachedURL = new URL(PlatformURLHandler.FILE,null,-1,tmp);
		}
		isInCache = true;
	}
	else {
		// attemp to cache
		int ix = file.lastIndexOf("/");
		tmp = file.substring(ix+1);
		tmp = cacheLocation + filePrefix + Long.toString((new java.util.Date()).getTime()) + "_" + tmp;
		tmp = tmp.replace(File.separatorChar,'/');
		if (isJar) {
			tmp = PlatformURLHandler.FILE + PlatformURLHandler.PROTOCOL_SEPARATOR + tmp + PlatformURLHandler.JAR_SEPARATOR + jarEntry;
			cachedURL = new URL(PlatformURLHandler.JAR,null,-1,tmp);
		}
		else cachedURL = new URL(PlatformURLHandler.FILE,null,-1,tmp);
		copyToCache();
	}
	
	return cachedURL;
}
/*
 * to be implemented by subclass
 * @return URL resolved URL
 */
 
protected URL resolve() throws IOException {
	throw new IOException();
}

private String resolvePath(String spec) {
	if (spec.length() == 0 || spec.charAt(0) != '$')
		return spec;
	int i = spec.indexOf('/', 1);
	String first = "";
	String rest = "";
	if (i == -1)
		first = spec;
	else {	
		first = spec.substring(0, i);
		rest = spec.substring(i);
	}
	if (first.equalsIgnoreCase("$ws$"))
		return "ws/" + InternalBootLoader.getWS() + rest;
	if (first.equalsIgnoreCase("$os$"))
		return "os/" + InternalBootLoader.getOS() + rest;
	if (first.equalsIgnoreCase("$nl$")) {
		String nl = InternalBootLoader.getNL();
		nl = nl.replace('_', '/');
		return "nl/" + nl + rest;
	}
	return spec;
}

protected String getId(String spec) {
	int i = spec.lastIndexOf('_');
	return i >= 0 ? spec.substring(0, i) : spec;
}

protected String getVersion(String spec) {
	int i = spec.lastIndexOf('_');
	return i >= 0 ? spec.substring(i + 1, spec.length()) : "";
}

void setResolvedURL(URL url) throws IOException {
	if (resolvedURL==null) {
		int ix = url.getFile().lastIndexOf(PlatformURLHandler.JAR_SEPARATOR);
		isJar = -1 != ix;
		// Resolved URLs containing !/ separator are assumed to be jar URLs.
		// If the resolved protocol is not jar, new jar URL is created.
		if (isJar && !url.getProtocol().equals(PlatformURLHandler.JAR)) 
			url = new URL(PlatformURLHandler.JAR,"",-1,url.toExternalForm());
		resolvedURL=url;
	}
}
private boolean shouldCache(boolean asLocal) {
	
	// don't cache files that are known to be local
	String rp = resolvedURL.getProtocol();
	String rf = resolvedURL.getFile();
	if (rp.equals(PlatformURLHandler.FILE) || rp.equals(PlatformURLHandler.VA)) return false;
	if (rp.equals(PlatformURLHandler.JAR) && (rf.startsWith(PlatformURLHandler.FILE) || rf.startsWith(PlatformURLHandler.VA) )) return false;

	// for other files force caching if local connection was requested
	if (asLocal) return true;
	
	// for now cache all files
	// XXX: add cache policy support
	return true;
}
static void shutdown() {
	if (indexName!=null && cacheLocation!=null) {
		// weed out "not found" entries
		Enumeration keys = cacheIndex.keys();
		String key;
		Object value;
		while (keys.hasMoreElements()) {
			key = (String) keys.nextElement();
			value = cacheIndex.get(key);
			if (value==NOT_FOUND) cacheIndex.remove(key);
		}
		//if the cache index is empty we don't need to save it
		if (cacheIndex.size() == 0)
			return;
		try {
			// try to save cache index
			FileOutputStream fos = null;
			fos = new FileOutputStream(cacheLocation+indexName);
			try {
				cacheIndex.store(fos,null);
			} finally {
				fos.close();
			}
		}
		catch(IOException e) {
			// failed to store cache index ... ignore
		}
	}	
}
static void startup(String location) {

	
	verifyLocation(location); // check for platform location, ignore errors
	String cacheProps = location.trim();
	if (!cacheProps.endsWith(File.separator)) cacheProps += File.separator;
	cacheProps += CACHE_PROP;
	File cachePropFile = new File(cacheProps);
	Properties props = null;	
	FileInputStream fis;
	
	if (cachePropFile.exists()) {
		// load existing properties	
		try {
			props = new Properties();
			fis = new FileInputStream(cachePropFile);
			try {
				props.load(fis);
			}
			finally {
				fis.close();
			}
		}
		catch(IOException e) {
			props = null;
		}
	}

	if (props==null) {
		// first time up, or failed to load previous settings
		props = new Properties();
		String tmp = System.getProperty("java.io.tmpdir");
		if (!tmp.endsWith(File.separator)) tmp += File.separator;
		tmp += CACHE_DIR;
		props.put(CACHE_LOCATION_PROP,tmp);
		
		tmp = Long.toString((new java.util.Date()).getTime());
		props.put(CACHE_PREFIX_PROP,tmp);
			
		tmp += CACHE_INDEX;
		props.put(CACHE_INDEX_PROP,tmp);

		// save for next time around
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(cachePropFile);
			try {
				props.store(fos,null);
			}
			finally {
				fos.close();
			}
		}
		catch(IOException e) {
			// failed to store cache location metadata ... ignore
		}
	}

	// remember settings for shutdown processing
	filePrefix = (String)props.get(CACHE_PREFIX_PROP);
	indexName = (String)props.get(CACHE_INDEX_PROP);
	cacheLocation = (String)props.get(CACHE_LOCATION_PROP);
	
	if (DEBUG) {
		debugStartup("Cache location: " + cacheLocation);
		debugStartup("Cache index: " + indexName);
		debugStartup("Cache file prefix: " + filePrefix);
	}

	// create cache directory structure if needed
	if (!verifyLocation(cacheLocation)) {
		indexName = null;
		cacheLocation = null;	
		if (DEBUG)
			debugStartup("Failed to create cache directory structure. Caching suspended");
		return;
	}

	// attempt to initialize cache index
	if (cacheLocation!=null && indexName!=null) {
		try {
			fis = new FileInputStream(cacheLocation+indexName);
			try {
				cacheIndex.load(fis);
			}
			finally {
				fis.close();
			}
		}
		catch(IOException e) {				
			if (DEBUG) 
				debugStartup("Failed to initialize cache");
		}
	}
}
private static boolean verifyLocation(String location) {
	// verify cache directory exists. Create if needed
	File cacheDir = new File(location);
	if (cacheDir.exists())
		return true;
	return cacheDir.mkdirs();
}
}
