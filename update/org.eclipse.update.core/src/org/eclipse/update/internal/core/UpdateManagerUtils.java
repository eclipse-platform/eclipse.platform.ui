package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.InstallMonitor;
import org.eclipse.update.core.Utilities;

/**
 * 
 */
public class UpdateManagerUtils {
	
	/**
	 * return the urlString if it is a absolute URL
	 * otherwise, return the default URL if the urlString is null
	 * The defaultURL may point ot a file, create a file URL then
	 * if teh urlString or the default URL are relatives, prepend the rootURL to it
	 */
	public static URL getURL(URL rootURL, String urlString, String defaultURL) throws MalformedURLException {
		URL url = null;

		// if no URL , provide Default
		if (urlString == null || urlString.trim().equals("")) { //$NON-NLS-1$

			// no URL, no default, return right now...
			if (defaultURL == null || defaultURL.trim().equals("")) //$NON-NLS-1$
				return null;
			else
				urlString = defaultURL;
		}

		// URL can be relative or absolute	
		if (urlString.startsWith("/") && urlString.length() > 1) //$NON-NLS-1$
			urlString = urlString.substring(1);
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			// the url is not an absolute URL
			// try relative
			url = new URL(rootURL,urlString);
		}
		return url;
	}

	/**
	 * return the url a relative String if the url contains the base url
	 * 
	 */
	public static String getURLAsString(URL rootURL, URL url) {
		String result = null;

		// if no URL , return null
		if (url != null) {
			result = url.toExternalForm();
			
			if (rootURL.getHost()!=null && !rootURL.getHost().equals(url.getHost()))
				return result;
			
			if (rootURL.getProtocol()!=null && !rootURL.getProtocol().equals(url.getProtocol()))
				return result;
			
			if (rootURL.getPort()!=url.getPort())
				return result;
			
			String rootString = url.getFile();
			rootString.replace(File.separatorChar,'/');
			if (!rootString.endsWith("/")){
				int index = rootString.lastIndexOf('/');
				if (index!=-1){
					rootString = rootString.substring(0,index);
				}
			}
			String urlString = url.getFile();
						
			if (urlString.indexOf(rootString)!=-1){
				result = urlString.substring(rootString.length());
			} else {
				result = urlString;
			}
		}
		
		return result;			
	}


	/**
	 * returns a translated String
	 */
	public static String getResourceString(String infoURL, ResourceBundle bundle) {
		String result = null;
		if (infoURL != null) {
			result = UpdateManagerPlugin.getPlugin().getDescriptor().getResourceString(infoURL, bundle);
		}
		return result;
	};

	
	
	/**
	 * 
	 */
	public static URL copyToLocal(InputStream sourceContentReferenceStream, String localName, InstallMonitor monitor) throws MalformedURLException, IOException {
		URL result = null;

		// create the Dir is they do not exist
		// get the path from the File to resolve File.separator..
		// do not use the String as it may contain URL like separator
		File localFile = new File(localName);
		int index = localFile.getPath().lastIndexOf(File.separator);
		if (index != -1) {
			File dir = new File(localFile.getPath().substring(0, index));
			if (!dir.exists())
				dir.mkdirs();
		}

		// transfer teh content of the File
		if (!localFile.isDirectory()) {
			OutputStream localContentReferenceStream = new FileOutputStream(localFile);
			Utilities.copy(sourceContentReferenceStream, localContentReferenceStream,monitor);
			localContentReferenceStream.close();
		}
		result = localFile.toURL();

		return result;
	}

	/**
	 * Returns a random file name for the local system
	 * attempt to conserve the extension if there is a '.' in the path
	 * and no File.Seperator after the '.'
	 * 
	 * \a\b\c.txt -> c987659385.txt
	 * c.txt -> c3854763.txt
	 * c	-> c953867549
	 */
	public static String getLocalRandomIdentifier(String remotePath, Date date) {
		int dotIndex = remotePath.lastIndexOf("."); //$NON-NLS-1$
		int fileIndex = remotePath.lastIndexOf(File.separator);
		
		// if there is a separator after the dot
		// do not consider it as an extension
		// FIXME: LINUX ???
		//String ext = (dotIndex != -1 && fileIndex < dotIndex) ? "." + remotePath.substring(dotIndex) : "";
		String ext = (dotIndex != -1 && fileIndex < dotIndex) ? remotePath.substring(dotIndex) : ""; //$NON-NLS-1$
		
		// the name is the string between the separator and the dot
		// if there is no separator, it is the string up to the dot		
		// if there is no dot, go to the end of the string 
		if (fileIndex==-1) fileIndex=0;
		if (dotIndex==-1) dotIndex=remotePath.length();
		// if I have a separator and no dot: /a/b/c -> c
		// if my separator is the last /a/b/c/, fileIndex and dotIndex are the same, so it will return teh default temp name
		String name = (fileIndex < dotIndex) ? remotePath.substring(fileIndex, dotIndex) : "Eclipse_Update_TMP_"; //$NON-NLS-1$

		String result = name + date.getTime() + ext;
		
		return result;
	}

	
	/**
	 * remove a file or directory from the file system.
	 * used to clean up install
	 */
	public static void removeFromFileSystem(File file) {
		if (!file.exists())
			return;
			
		if (file.isDirectory()) {
			String[] files = file.list();
			if (files != null) // be careful since file.list() can return null
				for (int i = 0; i < files.length; ++i)
					removeFromFileSystem(new File(file, files[i]));
		}
		if (!file.delete()) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.WARNING,id,IStatus.OK,Policy.bind("UpdateManagerUtils.UnableToRemoveFile", file.getAbsolutePath()),new Exception()); //$NON-NLS-1$ //$NON-NLS-2$
			UpdateManagerPlugin.getPlugin().getLog().log(status);
		}
	}

	/**
	 * Returns the plugin entries that are in source array and
	 * not in target array
	 */
	public static IPluginEntry[] diff(IPluginEntry[] sourceArray, IPluginEntry[] targetArray) {

		// No pluginEntry to Install, return Nothing to instal
		if (sourceArray == null || sourceArray.length == 0) {
			return new IPluginEntry[0];
		}

		// No pluginEntry installed, Install them all
		if (targetArray == null || targetArray.length == 0) {
			return sourceArray;
		}

		// if a IPluginEntry from sourceArray is NOT in
		// targetArray, add it to the list
		List list1 = Arrays.asList(targetArray);
		List result = new ArrayList(0);
		for (int i = 0; i < sourceArray.length; i++) {
			if (!list1.contains(sourceArray[i]))
				result.add(sourceArray[i]);
		}

		IPluginEntry[] resultEntry = new IPluginEntry[result.size()];
		if (result.size() > 0)
			result.toArray(resultEntry);

		return resultEntry;
	}

	/**
	 * Returns the parent URL of the given URL, or <code>null</code> if the
	 * given URL is the root.
	 * <table>
	 * <caption>Example</caption>
	 * <tr>
	 *   <th>Given URL</th>
	 *   <th>Parent URL</th>
	 * <tr>
	 *   <td>"http://hostname/"</td>
	 *   <td>null</td>
	 * <tr>
	 *   <td>"http://hostname/folder/file</td>
	 *   <td>"http://hostname/folder/</td>
	 * </table>
	 *
	 * @param url a URL
	 * @return    the parent of the given URL
	 */
	public static URL getParent(URL url) {
		String file = url.getFile();
		int len = file.length();
		if (len == 0 || len == 1 && file.charAt(0) == '/')
			return null;
		int lastSlashIndex = -1;
		for (int i = len - 2; lastSlashIndex == -1 && i >= 0; --i){
			if (file.charAt(i) == '/')
				lastSlashIndex = i;
		}
		if (lastSlashIndex == -1)
			file = "";
		else
			file = file.substring(0, lastSlashIndex + 1);
	
		try {
			url = new URL(url.getProtocol(), url.getHost(), url.getPort(), file);
		} catch(MalformedURLException e){
			Assert.isTrue(false, e.getMessage());
		}
		return url;
	}

	public static CoreException newCoreException(String s, Throwable e) throws CoreException {
		if (e != null && e instanceof CoreException)
			return (CoreException)e;
		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		return new CoreException(new Status(IStatus.ERROR,id,0,s,e));
	}	
}