package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.boot.*;
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
 * This file resides within an install configurations directory and must be a 
 * standard java property file.  Here are the properties as read from the file:
 * </p>
 * <p>
 * <ul>
 *  <li>brief product name - <code>"name"</code> entry</li>
 *  <li>full product name - <code>"detailedName"</code> entry</li>
 *  <li>app name - <code>"appName"</code> entry sets the application name 
 *      (on Motif, for example, this can be used to set the name used for resource lookup)</li>
 *  <li>version number - <code>"version"</code> entry</li>
 *  <li>product build id - <code>"buildID"</code> entry</li>
 *  <li>copyright notice - <code>"copyright"</code> entry</li>
 *  <li>product URL - <code>"productURL"</code> entry</li>
 *  <li>splash image - <code>"splashImage"</code> entry contains file name</li>
 *  <li>about image - <code>"aboutImage"</code> entry contains file name</li>
 *  <li>welcome image - <code>"welcomeImage"</code> entry contains file name</li>
 *  <li>welcome page - <code>"welcomePageURL"</code> entry contains URL</li>
 *  <li>product image - <code>"image"</code> entry contains file name</li>
 *  <li>default perspective id - <code>"defaultPerspectiveId"</code> entry</li>
 *  <li>perspective shortcuts - <code>"perspectiveShortcut.[x]"</code> entry</li>
 * </ul>
 * </p>
 */
public class ProductInfo extends ConfigurationInfo {


	// -- variables
	private String copyright;
	private String buildID;
	private String version;
	private String name;
	private String appName;
	private URL welcomePageURL;
	private String productURL;
	private String detailedName;
	private String defaultPerspId;
	private String baseInfosets;
	private ImageDescriptor productImage = null;
	private ImageDescriptor splashImage = null;
	private ImageDescriptor aboutImage = null;
	private Hashtable configurationPreferences;
	
	/**
	 * The name of the default preference settings file (value
	 * <code>"preferences.ini"</code>).
	 */
	private static final String DEFAULT_PREFERENCES = "preferences.ini";//$NON-NLS-1$
	
/**
 * Create a new instance of the product info
 */
public ProductInfo() {
	super("product.ini", "product.properties"); //$NON-NLS-1$ //$NON-NLS-2$
}
	
	
/**
 * Returns the default preferences obtained from the configuration.
 *
 * @return the default preferences obtained from the configuration
 */
public Hashtable getConfigurationPreferences() {
	return configurationPreferences;
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
 * Returns a new image like the one that would have been shown in a "splash" 
 * screen when this product starts up. Products designed to run "headless" would
 * not need such an image.
 * <p>
 * Note: This is spec'd to return a new instance.
 * Clients are responsible for ensuring that the returned image is properly
 * disposed after it has served its purpose.
 * </p>
 * 
 * @return the splash screen image, or <code>null</code> if none
 */
public Image getSplashImage() {
	return splashImage == null ? null : splashImage.createImage();
}

private Hashtable readConfigurationPreferences() {
	Hashtable table= new Hashtable();
	URL preferenceURL= null;
	try {
		preferenceURL= new URL(getBaseURL(), DEFAULT_PREFERENCES);
	} catch(MalformedURLException e) {
		return table;
	}
	Properties ini = new Properties();
	InputStream is = null;
	try {
		is = preferenceURL.openStream();
		ini.load(is);
	}
	catch (IOException e) {
		return table;
	}
	finally {
		try { if (is != null) is.close(); } catch (IOException e) {}
	}
	
	Enumeration i= ini.propertyNames();
	while (i.hasMoreElements()) {
		String e= (String) i.nextElement();
		//System.out.println(e);
		int index = e.indexOf("/");//$NON-NLS-1$
		if (index == -1) {
			reportINIFailure(null, "Invalid preference name: " + e);//$NON-NLS-1$
		} else {
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
	}
	return table;
	
}

/**
 * Read the ini file.
 */
protected void readINIFile(URL iniURL, URL propertiesURL) throws CoreException {

	Properties ini = new Properties();
	InputStream is = null;
	try {
		is = iniURL.openStream();
		ini.load(is);
	}
	catch (IOException e) {
		reportINIFailure(e, "Cannot read product info file " + iniURL);//$NON-NLS-1$
		return;
	}
	finally {
		try { 
			if (is != null)
				is.close(); 
		} catch (IOException e) {}
	}

	PropertyResourceBundle bundle = null;

	if (propertiesURL != null) {
		InputStream bundleStream = null;
		try {
			bundleStream = propertiesURL.openStream();
			bundle = new PropertyResourceBundle(bundleStream);
		}
		catch (IOException e) {
			reportINIFailure(e, "Cannot read platform properties file " + propertiesURL);//$NON-NLS-1$
			bundle = null;
		}
		finally {
			try { 
				if (bundleStream != null)
					bundleStream.close(); 
			} catch (IOException e) {}
		}
	}
		

	if ((defaultPerspId = (String) ini.get("defaultPerspectiveId") ) == null) {//$NON-NLS-1$
		defaultPerspId = IWorkbenchConstants.DEFAULT_LAYOUT_ID;
	}

	URL url = null;
	String fileName = (String) ini.get("splashImage");//$NON-NLS-1$

	if (fileName != null) {
		url = getDescriptor().find(new Path(fileName));
		if (url == null)
			reportINIFailure(null, "Cannot access splash image " + fileName); //$NON-NLS-1$
		else
			splashImage = ImageDescriptor.createFromURL(url);
	}


	configurationPreferences= readConfigurationPreferences();
}
}
