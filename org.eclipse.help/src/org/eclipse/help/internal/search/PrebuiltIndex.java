package org.eclipse.help.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.URL;
import java.util.zip.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;
// REVISIT THIS!
/**
 * Manages indexing and search for all infosets
 */
public class PrebuiltIndex {
	private String locale;
	private static int DATA_READ_SIZE = 4096;
	private static final String PREBUILT_INDEX_EXTENSION_POINT =
		"com.ibm.help.search.prebuiltIndex";
	/**
	 * Constructor.
	 */
	public PrebuiltIndex(String locale) {
		super();
		this.locale = locale;
	}
	/**
	 * Returns true if the prebuilt index exists
	 */
	public boolean exists() {
		boolean exists = false;
		try {
			InputStream stream = openPrebuiltIndex();
			exists = stream != null;
			if (exists)
				stream.close();
		} catch (IOException e) {
		}
		return exists;
	}
	/**
	 * Unzips prebuilt index
	 * preserving directory structures inside the zip.
	 * @param locale locale used
	 * @return true if index copied, false if error occured
	 */
	public boolean install() {
		InputStream indexStream = openPrebuiltIndex();
		if (indexStream == null)
			return false;
		boolean successfulCopy = false;
		byte[] buf = new byte[DATA_READ_SIZE];
		ZipInputStream zis = null;
		String helpStatePath = HelpPlugin.getDefault().getStateLocation().toOSString();
		File destDir =
			new File(helpStatePath + File.separator + "nl" + File.separator + locale);
		// remove the existing index directory (should not exist yet)
		deleteDirectory(destDir);
		zis = new ZipInputStream(indexStream);
		FileOutputStream fos = null;
		try {
			ZipEntry zEntry;
			while ((zEntry = zis.getNextEntry()) != null) {
				// if it is empty directory, create it
				if (zEntry.isDirectory()) {
					new File(destDir, zEntry.getName()).mkdirs();
					continue;
				}
				// if it is a file, extract it
				String filePath = zEntry.getName();
				int lastSeparator = filePath.lastIndexOf("/");
				String fileDir = "";

				if (lastSeparator >= 0) {
					fileDir = filePath.substring(0, lastSeparator);
				}
				//create directory for a file
				new File(destDir, fileDir).mkdirs();
				//write file
				File outFile = new File(destDir, filePath);
				fos = new FileOutputStream(outFile);
				int n = 0;
				while ((n = zis.read(buf)) >= 0) {
					fos.write(buf, 0, n);
				}
				fos.close();
				if (Logger.DEBUG)
					Logger.logDebugMessage(
						"PrebuiltIndex",
						"Prebuilt index copied to " + outFile + ".");
			}
			successfulCopy = true;
		} catch (IOException ioe) {
			Logger.logError(Resources.getString("ES11"), ioe);
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ioe2) {
				}
			}
			deleteDirectory(destDir);
		} finally {
			try {
				indexStream.close();
				if (zis != null)
					zis.close();
			} catch (IOException ioe) {
			}
			// reopen index, because file system has changed
			SearchIndex index = HelpSystem.getSearchManager().getIndex(locale);
			return successfulCopy;
		}
	}
	private void deleteDirectory(File dir) {
		String[] files = dir.list();
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++) {
			File f = new File(dir, files[i]);
			if (f.isFile())
				f.delete();
			else if (f.isDirectory())
				deleteDirectory(f);
		}
		dir.delete();
	}
	/**
	 * Obtains File descriptor for prebilt index
	 * @return InputStream
	 * @param locale String
	 */
	private InputStream openPrebuiltIndex() {
		try
		{
		// obtain searchengine configuration from registry
		IPluginRegistry registry = Platform.getPluginRegistry();
		IExtensionPoint xpt =
			registry.getExtensionPoint(PREBUILT_INDEX_EXTENSION_POINT);
		if (xpt == null)
			return null;
		IExtension[] extList = xpt.getExtensions();
		if (extList.length == 0)
			return null;
		for (int i = 0; i < extList.length; i++) {
			IPluginDescriptor plugind = extList[i].getDeclaringPluginDescriptor();
			// create the help url to the index file
			try {
				URL indexURL =
					new URL("help:/temp/" + plugind.getUniqueIdentifier() + "/index."					///+ infoSet
	+".zip" + "?lang=" + locale);
				InputStream indexStream = indexURL.openStream();
				if (indexStream != null)
					return indexStream;
			} catch (Exception e) {
				continue;
			}
		}
		return null;
		}
		catch(Exception ex)
		{
			return null;
		}
	}
}