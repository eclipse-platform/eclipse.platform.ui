package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;

/**
 * The product info class;
 * <p>
 * The information within this object is obtained from the product "ini" file".
 * This file resides within a plugin directory and must be a standard java
 * property file.  Here are the properties as read from the file:
 * </p>
 * <p>
 * <ul>
 *  <li>brief product name - <code>"name"</code> entry (required) </li>
 *  <li>full product name - <code>"detailedName"</code> entry (required) </li>
 *  <li>version number - <code>"version"</code> entry (required) </li>
 *  <li>product build id - <code>"buildID"</code> entry (required) </li>
 *  <li>copyright notice - <code>"copyright"</code> entry (required) </li>
 *  <li>product URL - <code>"productURL"</code> entry (required) </li>
 *  <li>splash image - <code>"splashImage"</code> entry contains file name 
 *      (optional) </li>
 *  <li>about image - <code>"aboutImage"</code> entry contains file name
 *      (optional) </li>
 *  <li>welcome image - <code>"welcomeImage"</code> entry contains file name
 *      (optional) </li>
 *  <li>product image - <code>"image"</code> entry contains file name
 *      (optional) </li>
 *  <li>default perspective id - <code>"defaultPerspectiveId"</code> entry
 *      (optional) </li>
 *  <li>perspective shortcuts - <code>"perspectiveShortcut.[x]"</code> entry
 *      (optional) </li>
 * </ul>
 * </p>
 */
public class ProductInfo {

	// -- variables
	private String copyright;
	private String buildID;
	private String version;
	private String name;
	private String productURL;
	private String detailedName;
	private String defaultPerspId;
	private String [] perspShortcuts;
	private ImageDescriptor productImage = null;
	private ImageDescriptor splashImage = null;
	private ImageDescriptor aboutImage = null;
	private ImageDescriptor welcomeImage = null;
	private URL baseURL;
	private Hashtable configurationPreferences;

	/**
	 * The name of the default preference settings file (value
	 * <code>"preferences.ini"</code>).
	 */
	private static final String DEFAULT_PREFERENCES = "preferences.ini";
/**
 * @see IProductInfo
 * Note: This spec'd to return a new instance.
 */
public Image getAboutImage() {
	return aboutImage == null ? null : aboutImage.createImage();
}
/**
 * @see IProductInfo
 */
public String getBuildID() {
	return buildID;
}
/**
 * @see IProductInfo
 */
public Hashtable getConfigurationPreferences() {
	return configurationPreferences;
}
/**
 * @see IProductInfo
 */
public String getCopyright() {
	return copyright;
}
/**
 * Returns the default perpective id.  This value will be used
 * as the default perspective for the product until the user overrides
 * it from the preferences.
 * 
 * @return the default perspective id, or <code>null</code> if none
 */
public String getDefaultPerspective() {
	return defaultPerspId;
}
/**
 * @see IProductInfo
 */
public String getDetailedName() {
	return detailedName;
}
/**
 * @see IProductInfo
 */
public String getName() {
	return name;
}
/**
 * Returns the perspective shortcuts.  These shortcuts are used to seed
 * the "Open New Perspective" and "Open New Window" menues.  
 * 
 * @return the perspective shortcuts
 */
public String [] getPerspectiveShortcuts() {
	return perspShortcuts;
}
/**
 * @see IProductInfo
 */
public ImageDescriptor getProductImageDescriptor() {
	return productImage;
}
/**
 * @see IProductInfo
 */
public String getProductURL() {
	return productURL;
}
/**
 * @see IProductInfo
 * Note: This is spec'd to return a new instance.
 */
public Image getSplashImage() {
	return splashImage == null ? null : splashImage.createImage();
}
/**
 * @see IProductInfo
 */
public String getVersion() {
	return version;
}
/**
 * @see IProductInfo
 * Note: This is spec'd to return a new instance.
 */
public Image getWelcomeImage() {
	return welcomeImage == null ? null : welcomeImage.createImage();
}
private Hashtable readConfigurationPreferences() {
	URL preferenceURL= null;
	try {
		preferenceURL= new URL(baseURL, DEFAULT_PREFERENCES);
	} catch(MalformedURLException e) {
		return null;
	}
	Properties ini = new Properties();
	InputStream is = null;
	try {
		is = preferenceURL.openStream();
		ini.load(is);
	}
	catch (IOException e) {
		return null;
	}
	finally {
		try { if (is != null) is.close(); } catch (IOException e) {}
	}
	
	Enumeration i= ini.propertyNames();
	Hashtable table= new Hashtable();
	while (i.hasMoreElements()) {
		String e= (String) i.nextElement();
		//System.out.println(e);
		int index = e.indexOf("/");
		if (index == 0) {
			// corrupt entry: log error and answer null
			return null;
		}
		String pluginName = e.substring(0, index);
		String propertyName = e.substring(index+1, e.length());
		Object entry= table.get(pluginName);
		if (entry == null) {
			entry= new String[] {propertyName , ini.getProperty(e)};
		} else {
			String[] old = (String[]) entry;
			int oldLength= old.length;
			String[] newEntry = new String[oldLength + 2];
			System.arraycopy(old, 0, newEntry, 0, oldLength);
			newEntry[oldLength]= propertyName;
			newEntry[oldLength+1]= ini.getProperty(e);
			entry= newEntry;
		}
		table.put(pluginName, entry);
	}
	return table;
	
}
/**
 * Read the ini file.
 */
public void readINIFile(URL baseURL) throws CoreException {
	
	this.baseURL= baseURL;
	URL iniURL= null;
	try {
			iniURL = new URL(baseURL, "product.ini");
	} catch (MalformedURLException e) {
		reportINIFailure(e, "Cannot access product.ini at " + baseURL);
	}

	Properties ini = new Properties();
	InputStream is = null;
	try {
		is = iniURL.openStream();
		ini.load(is);
	}
	catch (IOException e) {
		reportINIFailure(e, "Cannot read product info file " + iniURL);
	}
	finally {
		try { 
			if (is != null)
				is.close(); 
		} catch (IOException e) {}
	}

	if ((copyright = (String) ini.get("copyright") ) == null)
		reportINIFailure(null, "Product info file "+iniURL+" missing 'copyright'");

	if ((name = (String) ini.get("name") ) == null)
		reportINIFailure(null, "Product info file "+iniURL+" missing 'name'");

	if ((detailedName = (String) ini.get("detailedName") ) == null)
		reportINIFailure(null, "Product info file "+iniURL+" missing 'detailedName'");
			
	if ((version = (String) ini.get("version") ) == null)
		reportINIFailure(null, "Product info file "+iniURL+" missing 'version'");
		
	if ((buildID = (String) ini.get("buildID") ) == null)
		reportINIFailure(null, "Product info file "+iniURL+" missing 'buildID'");
				
	if ((productURL = (String) ini.get("productURL") ) == null)
		reportINIFailure(null, "Product info file "+iniURL+" missing 'productURL'");

	String fileName;
	URL url;

	if ((fileName = (String) ini.get("image") ) != null) {
		try {
			url = new URL(baseURL, fileName);
			productImage = ImageDescriptor.createFromURL(url);
		}
		catch (MalformedURLException e) {}
	}

	if ((fileName = (String) ini.get("aboutImage") ) != null) {
		try {
			url = new URL(baseURL, fileName);
			aboutImage = ImageDescriptor.createFromURL(url);
		}
		catch (MalformedURLException e) {}
	}

	if ((fileName = (String) ini.get("splashImage") ) != null) {
		try {
			url = new URL(baseURL, fileName);
			splashImage = ImageDescriptor.createFromURL(url);
		}
		catch (MalformedURLException e) {}
	}

	if ((fileName = (String) ini.get("welcomeImage") ) != null) {
		try {
			url = new URL(baseURL, fileName);
			welcomeImage = ImageDescriptor.createFromURL(url);
		}
		catch (MalformedURLException e) {}
	}
	
	if ((defaultPerspId = (String) ini.get("defaultPerspectiveId") ) == null) {
		defaultPerspId = IWorkbenchConstants.DEFAULT_LAYOUT_ID;
	}

	ArrayList perspList = new ArrayList(15);
	for (int nX = 1; nX <= 15; nX ++) {
		String key = "perspectiveShortcut." + Integer.toString(nX);
		String value = (String) ini.get(key);
		if (value != null)
			perspList.add(value);
		else
			break;
	}
	perspShortcuts = (String [])perspList.toArray(new String[perspList.size()]);
	configurationPreferences= readConfigurationPreferences();
}
private void reportINIFailure(Exception e, String message) throws CoreException {
	throw new CoreException(new Status(
		IStatus.ERROR,
		WorkbenchPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
		0,
		message,
		e));
}
}
