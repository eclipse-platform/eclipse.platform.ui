package org.eclipse.update.internal.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.eclipse.update.core.AbstractFeature;
import org.eclipse.update.core.Assert;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.core.VersionedIdentifier;

public class DefaultPackagedFeature extends AbstractFeature {
	
	private JarFile currentOpenJarFile=null;

	/**
	 * Constructor for DefaultPackagedFeature
	 */
	public DefaultPackagedFeature(IFeature sourceFeature, ISite targetSite) {
		super(sourceFeature, targetSite);
	}

	/**
	 * Constructor for DefaultPackagedFeature
	 */
	public DefaultPackagedFeature(
		VersionedIdentifier identifier,
		ISite targetSite) {
		super(identifier, targetSite);
	}

	/**
	 * @see AbstractFeature#getContentReferenceToInstall(IPluginEntry[])
	 */
	public String[] getContentReferenceToInstall(IPluginEntry[] pluginsToInstall) {
		String[] names = new String[pluginsToInstall.length];
		for (int i=0; i<pluginsToInstall.length;i++){
			names[i] = pluginsToInstall[i].getIdentifier().toString()+".jar";
		}
		return names;
	}

	/**
	 * @see AbstractFeature#getInputStreamFor(String)
	 */
	public InputStream getInputStreamFor(IPluginEntry pluginEntry,String name) {
		URL siteURL = getSite().getURL();
		InputStream result = null;
		try {
			String filePath = siteURL.getPath()+pluginEntry.getIdentifier().toString()+".jar";
			if (currentOpenJarFile!=null){
					if (!currentOpenJarFile.getName().equals(filePath)){
						currentOpenJarFile.close();
						currentOpenJarFile = new JarFile(filePath);						
					} else {
						// same file do nothing
					}
			} else {
				currentOpenJarFile = new JarFile(filePath);											
			}
			
			
			if (!(new File(filePath)).exists()) throw new IOException("The File:"+filePath+"does not exist.");
			ZipEntry entry = currentOpenJarFile.getEntry(name);
			result = currentOpenJarFile.getInputStream(entry);
		} catch (MalformedURLException e){
			//FIXME:
			e.printStackTrace();
		} catch (IOException e){
			//FIXME:
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * @see AbstractFeature#getStorageUnitNames(IPluginEntry)
	 */
	public String[] getStorageUnitNames(IPluginEntry pluginEntry) {
		URL siteURL = getSite().getURL();
		JarFile jarFile = null;
		String[] result = null;
		try {
			String jarPath = pluginEntry.getIdentifier().toString()+".jar";
			URL jarURL= new URL(siteURL,jarPath);			
			jarFile = new JarFile(jarURL.getPath());
			result = new String[jarFile.size()];
			Enumeration enum = jarFile.entries();
			int loop = 0;
			while (enum.hasMoreElements()){
				ZipEntry nextEntry = (ZipEntry)enum.nextElement();
				result[loop] = (String)nextEntry.getName();
				loop++;
			}
			jarFile.close();
		} catch (MalformedURLException e){
			//FIXME:
			e.printStackTrace();
		} catch (IOException e){
			//FIXME:
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * @see AbstractFeature#getFeatureInputStream()
	 */
	public InputStream getFeatureInputStream() throws IOException {
		// we know the feature url is pointing at the JAR
		// download the JAR in the TEMP dir and get the feature.xml
		
		
		// optmization, may be private to implementation
		// copy *blobs* in TEMP space
		InputStream result = null;
		InputStream sourceContentReferenceStream = getURL().openStream();
		if (sourceContentReferenceStream!=null){
			String newFile = SiteManager.getTempSite().getURL().getPath()+getIdentifier().toString()+".jar";
			FileOutputStream localContentReferenceStream = new FileOutputStream(newFile);
			transferStreams(sourceContentReferenceStream,localContentReferenceStream);
			this.setURL(new URL("jar:file://"+newFile+"!/"));				
		} else {
			throw new IOException("Couldn\'t find the file: "+getURL().toExternalForm());
		}
		
		// create a new JAR url around the file
		URL insideURL = null;
		try {
			String newURLString = getURL()+"feature.xml";
			insideURL = new URL(newURLString);
		} catch (MalformedURLException e){
			//FIXME:
			e.printStackTrace();
		}
		
		return insideURL.openStream();
	}

/**
 * This method also closes both streams.
 * Taken from FileSystemStore
 */
private void transferStreams(InputStream source, OutputStream destination) throws IOException {
	
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

