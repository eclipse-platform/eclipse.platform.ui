package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 *
 */
import java.io.File;import java.io.IOException;import java.io.InputStream;import java.io.InputStreamReader;import java.io.LineNumberReader;
import java.net.HttpURLConnection;import java.net.MalformedURLException;import java.net.URL;import java.util.Arrays;import java.util.Vector;
import org.eclipse.core.boot.*;public class UMEclipseTree  {
	private static URL fBaseInstallURL = BootLoader.getInstallURL();		// Base tree where Eclipse is running
	
	// Members of the Eclipse install URL directory
	public static final String INSTALL_DIR = "install";
	public static final String PRODUCTS_DIR = "configurations";
	public static final String COMPONENTS_DIR = "components";
	
	public static final String PLUGINS_DIR = "plugins";
	public static final String FRAGMENTS_DIR = "fragments";
	public static final String STAGING_DIR = "staging";
	public static final String BIN_DIR = "bin";

	public static final String URL_PROTOCOL_FILE = "file";
	public static final String DEVICE_SEPARATOR = ":";  // assuming windoze only - that we'll not run into dfs
/**
 * Returns the given URL with a trailing slash appended to it. If the URL
 * already has a trailing slash the URL is returned unchanged.
 * <table>
 * <caption>Example</caption>
 * <tr>
 *   <th>Given URL</th>
 *   <th>Returned URL</th>
 * <tr>
 *   <td>"http://hostname/folder"</td>
 *   <td>"http://hostname/folder/"</td>
 * <tr>
 *   <td>"http://hostname/folder/</td>
 *   <td>"http://hostname/folder/"</td>
 * </table>
 *
 * @param url a URL
 * @return    the given URL with a trailing slash
 * @throws    MalformedURLException if the given URL is malformed
 */
public static URL appendTrailingSlash(String url) throws MalformedURLException {
	return appendTrailingSlash(new URL(url));
}
/**
 * Returns the given <code>URL</code> with a trailing slash appended to
 * it. If the <code>URL</code> already has a trailing slash the
 * <code>URL</code> is returned unchanged.
 * <table>
 * <caption>Example</caption>
 * <tr>
 *   <th>Given URL</th>
 *   <th>Returned URL</th>
 * <tr>
 *   <td>"http://hostname/folder"</td>
 *   <td>"http://hostname/folder/"</td>
 * <tr>
 *   <td>"http://hostname/folder/</td>
 *   <td>"http://hostname/folder/"</td>
 * </table>
 *
 * @param url a URL
 * @return    the given URL with a trailing slash
 */
public static URL appendTrailingSlash(URL url){
	String file = url.getFile();

	if(file.endsWith("/")){
		return url;
	} else {
		try {
			return new URL(
				url.getProtocol(),
				url.getHost(),
				url.getPort(),
				file + "/");
		} catch(MalformedURLException e){
			// unchecked
		}
	}

	return null;
}
/**
 * Returns the chained install bases from this tree
 *
 * @return the chained install bases from this tree, can be empty 
 */
public static java.net.URL getBaseInstallURL() {
	return appendTrailingSlash(fBaseInstallURL);
}
/**
 * Returns the chained install bases from this tree
 *
 * @return the chained install bases from this tree, can be empty 
 */
private static java.net.URL[] getChainedInstallDirectories() {
	return getChainedInstallDirectories(fBaseInstallURL);
}
/**
 * Returns the chained install bases from this tree
 *
 * @return the chained install bases from this tree, can be empty 
 */
private static java.net.URL[] getChainedInstallDirectories(URL baseURL) {
	URL[] baseChain = new URL[1];	
	baseChain[0] = appendTrailingSlash(baseURL);
	
	return baseChain;
}
/**
 * Returns the components/ directory under install/
 *
 * @return the components/ directory under install/
 */
public static java.net.URL getComponentURL() {
	return getComponentURL(fBaseInstallURL);

}
/**
 * Returns the components/ directory under install/
 *
 * @return the components/ directory under install/
 */
public static URL getComponentURL(URL baseURL) {
	URL compInstallURL = null;
	try {
		compInstallURL = new URL(getInstallTreeURL(baseURL), COMPONENTS_DIR + "/");
	} catch (java.net.MalformedURLException e) {
	}
	return compInstallURL; 
}
/**
 * Returns an array containing the base install tree and the chained
 * install directories in URL format.  
 *
 * @return the base install tree and any chained install directories
 *  
 */
public static java.net.URL[] getDirectoriesInChain() {
	return getDirectoriesInChain(false);
}
/**
 * Returns an array containing the base install tree and the chained
 * install directories in URL format.  
 *
 * @return the base install tree and any chained install directories
 */
public static java.net.URL[] getDirectoriesInChain(URL base) {
	return getDirectoriesInChain(base, false);
}
/**
 * Returns an array containing the base install tree and the chained
 * install directories in URL format.  
 * If writeable is true, only the URLS that are writeable are returned
 *
 * @param writeable whether to return only writeable URLs or not
 * @return the base install tree and any chained install directories in URL
 */
public static java.net.URL[] getDirectoriesInChain(URL base, boolean writeable) {

		
	URL[] baseChain = getChainedInstallDirectories(appendTrailingSlash(base));

	Vector entries = null;
	if (!writeable) {
		entries = new Vector(Arrays.asList(baseChain));
	} else {
		entries = new Vector();
		for (int i=0; i<baseChain.length; i++) {
			String protocol = baseChain[i].getProtocol();
			if (protocol.equals(URL_PROTOCOL_FILE)) {
				// check if both .install/ and plugins/ writeable
				File check1 = new File(baseChain[i].getFile()+"/"+INSTALL_DIR+ "/" +".check");
				File check2 = new File(baseChain[i].getFile()+"/"+PLUGINS_DIR+ "/" +".check");
				if (check1.canWrite()&&check2.canWrite()) {
					entries.add(baseChain[i]);
				}
			}	
		}
	}


	// return updated chained path
	URL[] newPath = new URL[entries.size()];
	entries.copyInto(newPath);
	return newPath;
}
/**
 * Returns an array containing the base install tree and the chained
 * install directories in URL format.  
 * If writeable is true, only the URLS that are writeable are returned
 *
 * @param writeable whether to return only writeable URLs or not
 * @return the base install tree and any chained install directories in URL
 * 
 */
public static java.net.URL[] getDirectoriesInChain(boolean writeable) {
	return getDirectoriesInChain(fBaseInstallURL, writeable);
}
/**
 * Returns the path name of the getFile() portion of the url, in platform-specific form
 *
 * @return the path name of the getFile() portion of the url, in platform-specific for
 */
public static String getFileInPlatformString(URL url) {

	// Convert the URL to a string
	//----------------------------
	String strFilespec = url.getFile().replace('/',File.separatorChar);
	int k = strFilespec.indexOf(UMEclipseTree.DEVICE_SEPARATOR);
	if (k != -1 && strFilespec.startsWith(File.separator)) {
		strFilespec = strFilespec.substring(1);
	}
	return strFilespec;
}
/**
 * Returns the install/ directory 
 *
 * @return the install/ directory 
 */
public static java.net.URL getInstallTreeURL() {
	return getInstallTreeURL(fBaseInstallURL);
}
/**
 * Returns the install/ directory 
 *
 * @return the install/ directory 
 */
public static java.net.URL getInstallTreeURL(URL baseURL) {
	URL installTreeURL = null;
	try {
		installTreeURL = new URL(appendTrailingSlash(baseURL), INSTALL_DIR + "/");
	} catch (java.net.MalformedURLException e) {
	}
	return installTreeURL; 
}
/**
 * Returns the members (dirs, files, etc) of this URL path
 *
 * @return the members of this URL path 
 */
public static String[] getPathMembers(URL path) {

	String[] straList = null;
	URL url = null;

	String strURL = appendTrailingSlash(path).toExternalForm() + IManifestAttributes.INSTALL_INDEX;

	try {
		url = new URL(strURL);
	}
	catch (MalformedURLException ex) {
		url = null;
	}

	if (url != null) {
		// Read install.index
		//-------------------
		InputStream inputStream = null;

		try {
			BaseURLHandler.Response response = BaseURLHandler.open(url);
			if( response.getResponseCode() == HttpURLConnection.HTTP_OK ){
				inputStream = response.getInputStream();
			}
		}
		catch (IOException ex) {
		}

		if (inputStream != null) {
			Vector vectorStrings = new Vector();
			LineNumberReader reader = new LineNumberReader(new InputStreamReader(inputStream));

			String strLine = null;
			do {
				try {
					strLine = reader.readLine();
					if (strLine != null) {
						vectorStrings.add(strLine);
					}
				}
				catch (IOException ex) {
					strLine = null;
				}
			}
			while (strLine != null);


			straList = new String[vectorStrings.size()];
			vectorStrings.copyInto(straList);

			try{inputStream.close();} catch(Exception x) {}
		} else { 
			String protocol = path.getProtocol();
			if (protocol.equals("file") ||  protocol.equals("valoader")) {
				straList = (new File(path.getFile())).list();
			}
		} // if inputStream
	} // if url
	return straList == null ? new String[0] : straList;

}
/**
 * Returns the plugins directory 
 *
 * @return the plugins directory 
 */
public static java.net.URL getPluginsURL() {
	return getPluginsURL(fBaseInstallURL);
}
/**
 * Returns the plugins directory 
 *
 * @return the plugins directory 
 */
public static URL getPluginsURL(URL baseURL) {
	URL pluginsURL = null;
	try {
		pluginsURL = new URL(appendTrailingSlash(baseURL), PLUGINS_DIR + "/");
	} catch (java.net.MalformedURLException e) {
	}
	return pluginsURL; 
}
/**
 * Returns the configurations/ directory under install/
 *
 * @return the configurations/ directory under install/
 */
public static java.net.URL getProductURL() {
	return getProductURL(fBaseInstallURL);
}
/**
 * Returns the configurations/ directory under install/
 *
 * @return the configurations/ directory under install/
 */
public static URL getProductURL(URL baseURL) {
	URL prodInstallURL = null;
	try {
		prodInstallURL = new URL(getInstallTreeURL(baseURL), PRODUCTS_DIR + "/");
	} catch (java.net.MalformedURLException e) {
	}
	return prodInstallURL; 
}
/**
 * Returns the staging/ directory under install/
 *
 * @return the staging/ directory under install/
 */
public static java.net.URL getStagingArea() {
	return getStagingArea(fBaseInstallURL);
}
/**
 * Returns the staging/ directory under install/
 *
 * @return the staging/ directory under install/
 */
public static URL getStagingArea(URL baseURL) {
	URL staging = null;
	try {
		staging = new URL(getInstallTreeURL(baseURL), STAGING_DIR + "/");
	} catch (java.net.MalformedURLException e) {
	}
	return staging; 
}
/**
 * Returns whether the base url is writeable
 *
 * @return whether the base url is writeable
 */
public static boolean isEclipseTreeWriteable() {
	return isEclipseTreeWriteable(fBaseInstallURL);
}
/**
 * Returns whether the installable locations of the eclipse tree are all writeable
 *
 * @param url the URL
 * @return whether the installable locations of the tree are all writeable
 */
public static boolean isEclipseTreeWriteable(URL url) {
	String protocol = url.getProtocol();
	if (protocol.equals(URL_PROTOCOL_FILE) || protocol.equals("valoader")) {
		// check that install/components and install/products and plugins/ are writeable
		File pluginDir = new File(getPluginsURL(url).getFile());
		if (!pluginDir.isDirectory() || !pluginDir.canWrite())
			return false;
		
		File compDir = new File(getComponentURL(url).getFile());
		File prodDir = new File(getProductURL(url).getFile());
		if (!compDir.exists() || !prodDir.exists()) {
			// check one layer back
			File installDir = new File(getInstallTreeURL(url).getFile());
			if (installDir.exists()) { 	
				if (!installDir.isDirectory() || !installDir.canWrite())
					return false;
			} else {
				File baseDir = new File(url.getFile());
				if (baseDir.exists()) {
					if (!baseDir.isDirectory() || !baseDir.canWrite())
					return false;
				}
			}
		} 
		if (compDir.exists()) {
			if (!compDir.isDirectory() || !compDir.canWrite())
				return false;
		}
		if (prodDir.exists()) {
			if (!prodDir.isDirectory() || !prodDir.canWrite())
				return false;
		}
		
		return true;
	}	
	return false;
}
/**
 * Returns whether the base url is writeable
 *
 * @return whether the base url is writeable
 */
public static boolean isWriteable() {
	return isWriteable(fBaseInstallURL);
}
/**
 * Returns whether the url is writeable
 *
 * @param url the URL
 * @return whether the url is writeable
 */
public static boolean isWriteable(URL url) {
	String protocol = url.getProtocol();
	if (protocol.equals(URL_PROTOCOL_FILE)) {
		File file = new File(url.getFile());
		if (file.canWrite()) {
			return true;
		}
	}	
	return false;
}
}
