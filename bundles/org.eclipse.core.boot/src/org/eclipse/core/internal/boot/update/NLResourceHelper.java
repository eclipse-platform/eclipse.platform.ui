package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */



import java.net.URL;import java.net.URLClassLoader;import java.util.Locale;import java.util.MissingResourceException;import java.util.ResourceBundle;import org.eclipse.core.internal.boot.Policy;

public class NLResourceHelper implements INLResourceHelper {
	private ResourceBundle		fBundle=null;			// abc.properties
	private Locale              fLocale=null;           // bundle locale
	private String				fName = null;			// abc
	private URL					fLocation=null;			
	private boolean				notFound=false;			// marker to prevent unnecessary lookups
	
	public static final String KEY_PREFIX			= "%";
	public static final String KEY_DOUBLE_PREFIX	= "%%";
/**
 * NLResourceHelper constructor comment.
 */
public NLResourceHelper(String name, URL url) {

	fName = name;
	fLocation = url;
	
}
public java.util.ResourceBundle getResourceBundle() throws MissingResourceException {

	return getResourceBundle(Locale.getDefault());
}
public java.util.ResourceBundle getResourceBundle(Locale locale) throws MissingResourceException {

	// we cache the bundle for a single locale 
	if (fBundle != null && fLocale.equals(locale)) return fBundle;
	
	// check if we already tried and failed
	if (notFound) 
		throw new MissingResourceException(
			Policy.bind("update.resourceNotFound", fName + "_" + locale), 
			fName + "_" + locale, 
			"");

	// try to load bundle from this install directory
	ClassLoader resourceLoader = new URLClassLoader(new URL[] {fLocation},null);
	ResourceBundle bundle = null;
	try {
		bundle = ResourceBundle.getBundle(fName,locale,resourceLoader);
	}
	catch (MissingResourceException e) {
		notFound = true;
		throw e;
	}

	return bundle;
}
public String getResourceString(String value) {
	ResourceBundle b = null;
	try { b = getResourceBundle(); }
	catch (MissingResourceException e) {};
	return getResourceString(value, b);
}
public String getResourceString(String value, ResourceBundle b) {

	String s = value.trim();
	
	if (!s.startsWith(KEY_PREFIX)) return s;

	if (s.startsWith(KEY_DOUBLE_PREFIX)) return s.substring(1);

	int ix = s.indexOf(" ");
	String key = ix == -1 ? s : s.substring(0,ix);
	String dflt = ix == -1 ? s : s.substring(ix+1);

	if (b==null) return dflt;
	
	try { return b.getString(key.substring(1)); }
	catch(MissingResourceException e) { return dflt; }
}
}
