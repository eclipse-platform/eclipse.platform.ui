package org.eclipse.update.internal.core;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import org.eclipse.update.core.SiteManager;

public class UpdateManagerUtils {

	/**
	 * return the urlString if it is a absolute URL
	 * otherwise, return the default URL if the urlString is null
	 * if teh urlString or the defautl URL are relatives, prepend the rootURL to it
	 */
	public static URL getURL(URL rootURL, String urlString, String defaultURL) {
		URL url = null;
		try {
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
				url =
					new URL(
						rootURL.getProtocol(),
						rootURL.getHost(),
						rootURL.getPath() + urlString);
			}
		} catch (MalformedURLException e) {
			//FIXME:
			e.printStackTrace();
		}
		return url;
	}

	/**
	 * returns a translated String
	 */
	public static String getResourceString(String infoURL, ResourceBundle bundle) {
		String result = null;
		if (infoURL != null) {
			result =
				UpdateManagerPlugin.getPlugin().getDescriptor().getResourceString(
					infoURL,
					bundle);
		}
		return result;
	};

/**
	 * Resolve a URL as a local file URL
	 * if the URL is not a file URL, transfer the stream to teh temp directory 
	 * and return the new URL
	 */
	public static URL resolveAsLocal(URL remoteURL)
	throws MalformedURLException, IOException {
		return resolveAsLocal(remoteURL,null);
	}


	/**
	 * Resolve a URL as a local file URL
	 * if the URL is not a file URL, transfer the stream to teh temp directory 
	 * and return the new URL
	 */
	public static URL resolveAsLocal(URL remoteURL, String localName)
		throws MalformedURLException, IOException {
		URL result = remoteURL;

		if (!(remoteURL == null || remoteURL.getProtocol().equals("file"))) {
			InputStream sourceContentReferenceStream = remoteURL.openStream();
			if (sourceContentReferenceStream != null) {

				AbstractSite tempSite = (AbstractSite) SiteManager.getTempSite();
				String newFile = tempSite.getURL().getPath();
				if (localName == null || localName.trim().equals("")) {
					newFile = newFile + getLocalRandomIdentifier("");
				} else {
					newFile = newFile + localName;

				}

				result = copyToLocal(sourceContentReferenceStream, newFile);
			} else {
				throw new IOException("Couldn\'t find the file: " + remoteURL.toExternalForm());
			}
		}

		// DEBUG:
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_INSTALL) {
			System.out.println(
				"Transfered URL:"
					+ remoteURL.toExternalForm()
					+ " to:"
					+ result.toExternalForm());
		}

		return result;
	}

	/**
	 * 
	 */
	public static URL copyToLocal(InputStream sourceContentReferenceStream,String localName)
		throws MalformedURLException, IOException {
		URL result = null;

		// create the Dir is they do not exist
		// get the path from the File to resolve File.separator..
		// do not use the String as it may contain URL like separator
		File localFile = new File(localName);
		int index = localFile.getPath().lastIndexOf(File.separator);
		if (index!=-1){
			File dir = new File(localFile.getPath().substring(0,index));
			if (!dir.exists()) dir.mkdirs();
		}

		// transfer teh content of the File
		if (!localFile.isDirectory()){		
			FileOutputStream localContentReferenceStream = new FileOutputStream(localFile);
			transferStreams(sourceContentReferenceStream, localContentReferenceStream);
		}
		result = new URL("file", null, localFile.getPath());

		return result;
	}

	/**
	 * Returns a random fiel name for teh local system
	 */
	private static String getLocalRandomIdentifier(String remotePath) {
		int dotIndex = remotePath.lastIndexOf(".");
		int fileIndex = remotePath.lastIndexOf(File.separator);

		// if there is a separator after the dot
		// do not consider it as an extension
		// FIXME: LINUX ???
		String ext = (dotIndex != -1 && fileIndex<dotIndex) ? "." + remotePath.substring(dotIndex) : "";
		String name =
			(fileIndex != -1 && fileIndex < dotIndex)
				? remotePath.substring(fileIndex, dotIndex)
				: "";

		Date date = new Date();
		String result = name + date.getTime() + ext;

		return result;
	}

	/**
	 * This method also closes both streams.
	 * Taken from FileSystemStore
	 */
	private static void transferStreams(
		InputStream source,
		OutputStream destination)
		throws IOException {

		Assert.isNotNull(source);
		Assert.isNotNull(destination);

		try {
			byte[] buffer = new byte[8192];
			while (true) {
				int bytesRead = source.read(buffer);
				if (bytesRead == -1)
					break;
				destination.write(buffer, 0, bytesRead);
			}
		} finally {
			try {
				source.close();
			} catch (IOException e) {
			}
			try {
				destination.close();
			} catch (IOException e) {
			}
		}
	}

}