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
import org.eclipse.swt.widgets.Display;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;

/**
 * The platform info class;
 * <p>
 * The information within this object is obtained from the platform "ini" file".
 * This file resides within an install configurations directory and must be a 
 * standard java property file.  Here are the properties as read from the file:
 * </p>
 * <p>
 * <ul>
 *  <li>brief platform name - <code>"name"</code> entry</li>
 *  <li>full platform name - <code>"detailedName"</code> entry</li>
 *  <li>app name - <code>"appName"</code> entry sets the application name 
 *      (on Motif, for example, this can be used to set the name used for resource lookup)</li>
 *  <li>version number - <code>"version"</code> entry</li>
 *  <li>platform build id - <code>"buildID"</code> entry</li>
 *  <li>copyright notice - <code>"copyright"</code> entry</li>
 *  <li>platform URL - <code>"platformURL"</code> entry</li>
 *  <li>splash image - <code>"splashImage"</code> entry contains file name</li>
 *  <li>about image - <code>"aboutImage"</code> entry contains file name</li>
 *  <li>welcome image - <code>"welcomeImage"</code> entry contains file name</li>
 *  <li>welcome page - <code>"welcomePageURL"</code> entry contains URL</li>
 *  <li>platform image - <code>"image"</code> entry contains file name</li>
 *  <li>default perspective id - <code>"defaultPerspectiveId"</code> entry</li>
 *  <li>perspective shortcuts - <code>"perspectiveShortcut.[x]"</code> entry</li>
 * </ul>
 * </p>
 */
public class PlatformInfo extends ConfigurationInfo {

	// -- variables
	private String copyright;
	private String buildID;
	private String version;
	private String name;
	private String appName;
	private String platformURL;
	private String detailedName;

/**
 * Create a new instance of the platform info
 */
public PlatformInfo() {
	super("platform.ini", "platform.properties"); //$NON-NLS-1$ //$NON-NLS-2$
}

/**
 * Returns the build id for this platform.
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
 * Returns the copyright notice for this platform.
 * <p>
 * The copyright notice is typically shown in the platform's "about" dialog.
 * </p>
 *
 * @return the copyright notice
 */
public String getCopyright() {
	return (copyright == null ? "" : copyright); //$NON-NLS-1$
}
/**
 * Returns the full name of this platform.
 * <p>
 * The full name also includes additional information about the particular
 * variant of the platform.
 * </p>
 *
 * @return the full name of this platform
 */
public String getDetailedName() {
	return (detailedName == null ? "" : detailedName); //$NON-NLS-1$
}
/**
 * Returns the name of this platform.
 *
 * @return the name of this platform
 */
public String getName() {
	return (name == null ? "" : name); //$NON-NLS-1$
}
/**
 * Returns the URL for this platform's main page on the world wide web.
 *
 * @return the platform URL
 */
public String getplatformURL() {
	return (platformURL == null ? "" : platformURL); //$NON-NLS-1$
}
/**
 * Returns the version number of this platform.
 * <p>
 * The recommended format is <it>X</it>.<it>Y</it> where <it>X</it> and 
 * <it>Y</it> are the major and minor version numbers, respectively; for
 * example: 5.02. However, arbitrary strings are allowed.
 * </p>
 *
 * @return the platform version number
 */
public String getVersion() {
	return (version == null ? "" : version); //$NON-NLS-1$
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
		reportINIFailure(e, "Cannot read platform info file " + iniURL);//$NON-NLS-1$
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
		reportINIFailure(null, "Platform info file "+iniURL+" missing 'copyright'");//$NON-NLS-2$//$NON-NLS-1$
	copyright = getResourceString(copyright, bundle);

	if ((name = (String) ini.get("name") ) == null)//$NON-NLS-1$
		reportINIFailure(null, "Platform info file "+iniURL+" missing 'name'");//$NON-NLS-2$//$NON-NLS-1$
	name = getResourceString(name, bundle);

	if ((detailedName = (String) ini.get("detailedName") ) == null)//$NON-NLS-1$
		reportINIFailure(null, "Platform info file "+iniURL+" missing 'detailedName'");//$NON-NLS-2$//$NON-NLS-1$
	detailedName = getResourceString(detailedName, bundle);
			
	if ((version = (String) ini.get("version") ) == null)//$NON-NLS-1$
		reportINIFailure(null, "Platform info file "+iniURL+" missing 'version'");//$NON-NLS-2$//$NON-NLS-1$
		
	if ((buildID = (String) ini.get("buildID") ) == null)//$NON-NLS-1$
		reportINIFailure(null, "Platform info file "+iniURL+" missing 'buildID'");//$NON-NLS-2$//$NON-NLS-1$
				
	if ((platformURL = (String) ini.get("platformURL") ) == null)//$NON-NLS-1$
		reportINIFailure(null, "Platform info file "+iniURL+" missing 'platformURL'");//$NON-NLS-2$//$NON-NLS-1$
	platformURL = getResourceString(platformURL, bundle);

}

}
