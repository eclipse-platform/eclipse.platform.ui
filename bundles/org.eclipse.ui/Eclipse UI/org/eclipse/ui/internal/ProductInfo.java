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
 * Returns a new image which can be shown in an "about" dialog for this product.
 * Products designed to run "headless" typically would not have such an image.
 * <p>
 * Clients are responsible for ensuring that the returned image is properly
 * disposed after it has served its purpose.
 * </p>
 * 
 * @return the about image, or <code>null</code> if none
 */
public Image getAboutImage() {
	return aboutImage == null ? null : aboutImage.createImage();
}
/**
 * Returns the app name or <code>null</code>. 
 * <p>
 * On Motif, for example, this can be used
 * to set the name used for resource lookup.
 * </p>
 *
 * @return the app name
 */
public String getAppName() {
	return appName;
}
/**
 * Returns the build id for this product.
 * <p>
 * The build id represents any builds or updates made in support of a major
 * release. Development teams typically may produce many builds only a subset
 * of which get shipped to customers.
 * </p>
 *
 * @return the build id
 */
public String getBuildID() {
	return (buildID == null ? "" : buildID); //$NON-NLS-1$
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
 * Returns the copyright notice for this product.
 * <p>
 * The copyright notice is typically shown in the product's "about" dialog.
 * </p>
 *
 * @return the copyright notice
 */
public String getCopyright() {
	return (copyright == null ? "" : copyright); //$NON-NLS-1$
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
 * Returns the full name of this product.
 * <p>
 * The full name also includes additional information about the particular
 * variant of the product.
 * </p>
 *
 * @return the full name of this product
 */
public String getDetailedName() {
	return (detailedName == null ? "" : detailedName); //$NON-NLS-1$
}
/**
 * Returns the base information set identifiers for this product.
 * <p>
 * This value will be used to determine the ordering of the online
 * information sets provided by the product.
 * </p>
 * 
 * @return a comma-separated list of information set identifiers, 
 * or <code>null</code> if none specified
 */
public String getInformationSetIds() {
	return baseInfosets;
}
/**
 * Returns the name of this product.
 *
 * @return the name of this product
 */
public String getName() {
	return (name == null ? "" : name); //$NON-NLS-1$
}
/**
 * Returns an image descriptor for this product's icon.
 * Products designed to run "headless" typically would not have such an image.
 * <p>
 * The image is typically used in the top left corner of all windows associated
 * with the product.
 * </p>
 *
 * @return an image descriptor for the product's icon, or <code>null</code> if
 *  none
 */
public ImageDescriptor getProductImageDescriptor() {
	return productImage;
}
/**
 * Returns the URL for this product's main page on the world wide web.
 *
 * @return the product URL
 */
public String getProductURL() {
	return (productURL == null ? "" : productURL); //$NON-NLS-1$
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
/**
 * Returns the version number of this product.
 * <p>
 * The recommended format is <it>X</it>.<it>Y</it> where <it>X</it> and 
 * <it>Y</it> are the major and minor version numbers, respectively; for
 * example: 5.02. However, arbitrary strings are allowed.
 * </p>
 *
 * @return the product version number
 */
public String getVersion() {
	return (version == null ? "" : version); //$NON-NLS-1$
}
/**
 * Returns a <code>URL</code> for the welcome page.
 * Products designed to run "headless" typically would not have such an page.
 * 
 * @return the welcome page, or <code>null</code> if none
 */
public URL getWelcomePageURL() {
	return welcomePageURL;
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
	if ((copyright = (String) ini.get("copyright") ) == null)//$NON-NLS-1$
		reportINIFailure(null, "Product info file "+iniURL+" missing 'copyright'");//$NON-NLS-2$//$NON-NLS-1$
	copyright = getResourceString(copyright, bundle);

	if ((name = (String) ini.get("name") ) == null)//$NON-NLS-1$
		reportINIFailure(null, "Product info file "+iniURL+" missing 'name'");//$NON-NLS-2$//$NON-NLS-1$
	name = getResourceString(name, bundle);
	if ((detailedName = (String) ini.get("detailedName") ) == null)//$NON-NLS-1$
		reportINIFailure(null, "Product info file "+iniURL+" missing 'detailedName'");//$NON-NLS-2$//$NON-NLS-1$
	detailedName = getResourceString(detailedName, bundle);
			
	if ((version = (String) ini.get("version") ) == null)//$NON-NLS-1$
		reportINIFailure(null, "Product info file "+iniURL+" missing 'version'");//$NON-NLS-2$//$NON-NLS-1$
		
	if ((buildID = (String) ini.get("buildID") ) == null)//$NON-NLS-1$
		reportINIFailure(null, "Product info file "+iniURL+" missing 'buildID'");//$NON-NLS-2$//$NON-NLS-1$
				
	if ((productURL = (String) ini.get("productURL") ) == null)//$NON-NLS-1$
		reportINIFailure(null, "Product info file "+iniURL+" missing 'productURL'");//$NON-NLS-2$//$NON-NLS-1$
	productURL = getResourceString(productURL, bundle);
	appName = (String) ini.get("appName"); //$NON-NLS-1$
	if (appName != null) {
		appName = getResourceString(appName, bundle);
	}

	String welcomePageFileName = (String) ini.get("welcomePage"); //$NON-NLS-1$
	if (welcomePageFileName != null) {
		welcomePageURL = null;
		try {
			welcomePageURL = getDescriptor().getPlugin().find(new Path(welcomePageFileName));
			if (welcomePageURL != null)
				welcomePageURL = Platform.resolve(welcomePageURL);
		} catch (CoreException e) {
			// null check below
		} catch (IOException e) {
			// null check below
		}
		if (welcomePageURL == null) {
			reportINIFailure(null, "Cannot access welcome page " + welcomePageFileName); //$NON-NLS-1$
		}
	}
		
	URL url = null;
	String fileName = (String) ini.get("image");//$NON-NLS-1$
	if (fileName != null) {
		try {
			url = getDescriptor().getPlugin().find(new Path(fileName));
			if (url != null)
				url = Platform.resolve(url);
		} catch (CoreException e) {
			reportINIFailure(null, "Cannot access image " + fileName); //$NON-NLS-1$
		} catch (IOException e) {
			reportINIFailure(null, "Cannot access image " + fileName); //$NON-NLS-1$
		}
		if (url != null)
			productImage = ImageDescriptor.createFromURL(url);
	}

	fileName = (String) ini.get("aboutImage");//$NON-NLS-1$
	if (fileName != null) {
		try {
			url = getDescriptor().getPlugin().find(new Path(fileName));
			if (url != null)
				url = Platform.resolve(url);
		} catch (CoreException e) {
			reportINIFailure(null, "Cannot access about image " + fileName); //$NON-NLS-1$
		} catch (IOException e) {
			reportINIFailure(null, "Cannot access about image " + fileName); //$NON-NLS-1$
		}
		if (url != null)
			aboutImage = ImageDescriptor.createFromURL(url);
	}

	if ((fileName = (String) ini.get("splashImage") ) != null) {//$NON-NLS-1$
		try {
			url = getDescriptor().getPlugin().find(new Path(fileName));
			if (url != null)
				url = Platform.resolve(url);
		} catch (CoreException e) {
			reportINIFailure(null, "Cannot access splash image " + fileName); //$NON-NLS-1$
		} catch (IOException e) {
			reportINIFailure(null, "Cannot access splash image " + fileName); //$NON-NLS-1$
		}
		if (url != null)
			splashImage = ImageDescriptor.createFromURL(url);
	}

	if ((defaultPerspId = (String) ini.get("defaultPerspectiveId") ) == null) {//$NON-NLS-1$
		defaultPerspId = IWorkbenchConstants.DEFAULT_LAYOUT_ID;
	}


	baseInfosets = (String) ini.get("baseInfosets"); //$NON-NLS-1$

	configurationPreferences= readConfigurationPreferences();
}
}
