package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.Site;
import org.eclipse.update.core.SiteManager;

public class UpdateManagerUtils {
	
	
	public static int AVAILABLE = 1000;
	
	/**
	 * 
	 */
	private static IOException CANCEL_EXCEPTION;


	/**
	 * Static block to initialize the possible CANCEL ERROR
	 * thrown when the USER cancels teh operation
	 */
	static {
		//	in case we throw a cancel exception
		CANCEL_EXCEPTION = new IOException("Install has been cancelled");
	}

	/**
	 * return the urlString if it is a absolute URL
	 * otherwise, return the default URL if the urlString is null
	 * The defaultURL may point ot a file, create a file URL then
	 * if teh urlString or the default URL are relatives, prepend the rootURL to it
	 */
	public static URL getURL(URL rootURL, String urlString, String defaultURL) throws MalformedURLException {
		URL url = null;

		// if no URL , provide Default
		if (urlString == null || urlString.trim().equals("")) {

			// no URL, no default, return right now...
			if (defaultURL == null || defaultURL.trim().equals(""))
				return null;
			else
				urlString = defaultURL;
		}

		// URL can be relative or absolute	
		if (urlString.startsWith("/") && urlString.length() > 1)
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
		String rootString = rootURL.toExternalForm();

		// if no URL , return null
		if (url != null) {
			String urlString = url.toExternalForm();
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
	 * Resolve a URL as a local file URL
	 * if the URL is not a file URL, transfer the stream to teh temp directory 
	 * and return the new URL
	 */
	public static URL resolveAsLocal(URL remoteURL) throws MalformedURLException, IOException, CoreException {
		return resolveAsLocal(remoteURL, null,null);
	}

	/**
	 * Resolve a URL as a local file URL
	 * if the URL is not a file URL, transfer the stream to teh temp directory 
	 * and return the new URL
	 */
	public static URL resolveAsLocal(URL remoteURL, String localName, IProgressMonitor monitor) throws MalformedURLException, IOException, CoreException {
		URL result = remoteURL;

		if (!(remoteURL == null || remoteURL.getProtocol().equals("file"))) {
			InputStream sourceContentReferenceStream = remoteURL.openStream();
			if (sourceContentReferenceStream != null) {

				Site tempSite = (Site) SiteManager.getTempSite();
				String newFile = UpdateManagerUtils.getPath(tempSite.getURL());							
				if (localName == null || localName.trim().equals("")) {
					newFile = newFile + getLocalRandomIdentifier("");
				} else {
					newFile = newFile + localName;

				}

				result = copyToLocal(sourceContentReferenceStream, newFile,monitor);
			} else {
				throw new IOException("Couldn\'t find the file: " + remoteURL.toExternalForm());
			}
			
			// DEBUG:
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_INSTALL) {
				UpdateManagerPlugin.getPlugin().debug("Transfered URL:" + remoteURL.toExternalForm() + " to:" + result.toExternalForm());
			}
		}
		return result;
	}

	/**
	 * 
	 */
	public static URL copyToLocal(InputStream sourceContentReferenceStream, String localName, IProgressMonitor monitor) throws MalformedURLException, IOException {
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
			FileOutputStream localContentReferenceStream = new FileOutputStream(localFile);
			transferStreams(sourceContentReferenceStream, localContentReferenceStream,monitor);
		}
		result = new URL("file", null, localFile.getPath());

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
	public static String getLocalRandomIdentifier(String remotePath) {
		int dotIndex = remotePath.lastIndexOf(".");
		int fileIndex = remotePath.lastIndexOf(File.separator);
		
		// if there is a separator after the dot
		// do not consider it as an extension
		// FIXME: LINUX ???
		//String ext = (dotIndex != -1 && fileIndex < dotIndex) ? "." + remotePath.substring(dotIndex) : "";
		String ext = (dotIndex != -1 && fileIndex < dotIndex) ? remotePath.substring(dotIndex) : "";
		
		// the name is teh string between the separator and the dot
		// if there is no separator, it is the string up to the dot		
		// if there is no dot, go to the end of the string 
		if (fileIndex==-1) fileIndex=0;
		if (dotIndex==-1) dotIndex=remotePath.length();
		// if I have a separator and no dot: /a/b/c -> c
		// if my separator is the last /a/b/c/, fileIndex and dotIndex are the same, so it will return teh default temp name
		String name = (fileIndex < dotIndex) ? remotePath.substring(fileIndex, dotIndex) : "Eclipse_Update_TMP_";

		Date date = new Date();
		String result = name + date.getTime() + ext;
		
		return result;
	}

	/**
	 * This method also closes both streams.
	 * Taken from FileSystemStore
	 */
	private static void transferStreams(InputStream source, OutputStream destination, IProgressMonitor monitor) throws IOException {

		Assert.isNotNull(source);
		Assert.isNotNull(destination);
		
		if (monitor != null) {
				monitor.beginTask("downloading...",AVAILABLE);
		}

		try {
			int total = 0;
			long loaded = 0;
			byte[] buffer = new byte[8192];
			while (true) {
				int bytesRead = source.read(buffer);
				if (bytesRead == -1)
					break;
				destination.write(buffer, 0, bytesRead);
				if (monitor != null) {
					monitor.worked(1);
					loaded = loaded + bytesRead;
					if (monitor.isCanceled()) {
							throw CANCEL_EXCEPTION; 
					}
					if (++total==AVAILABLE){
						monitor.beginTask("downloading...",AVAILABLE);
						total = 0;						
					}
					monitor.setTaskName("loaded:"+loaded);
				}
			}
		} finally {
			try {
				source.close();
			} catch (IOException e) {}
			try {
				destination.close();
			} catch (IOException e) {}
		}
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
			IStatus status = new Status(IStatus.WARNING,id,IStatus.OK,"cannot remove: " + file.getPath()+" from the filesystem",new Exception());
			UpdateManagerPlugin.getPlugin().getLog().log(status);
		}
	}
	
	/**
	 * Method to return the PATH of the URL.
	 * The path is the file of a URL before any <code>#</code> or <code>?</code>
	 * This code removes the fragment or the query of the URL file
	 * A URL is of the form: <code>protocol://host/path#ref</code> or <code> protocol://host/path?query</code>
	 * 
	 * @return the path of the URL
	 */
	public static String getPath(URL url){
		/*String result = null;
		if (url!=null){
			String file = url.getFile();
			int index;
			if ((index = (file.indexOf("#")))!=-1) file = file.substring(0,index);
			if ((index = (file.indexOf("?")))!=-1) file = file.substring(0,index);
			result = file;
		}
		return result;*/
		
		if (url==null) return null;
		return url.getPath();
	}
	
	/**
	 * Returns teh runtime configuration from core boot
	 */
	public static IPlatformConfiguration getRuntimeConfiguration() throws CoreException {
		IPlatformConfiguration result = null;	
		result = BootLoader.getCurrentPlatformConfiguration();
		return result;
	}
	

}