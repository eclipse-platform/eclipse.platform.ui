package org.eclipse.update.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.eclipse.update.core.AbstractFeature;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.ISite;
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
			names[i] = pluginsToInstall[i].getIdentifier().toString();
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
			String filePath = siteURL.getPath()+pluginEntry.toString()+".jar";
			if (currentOpenJarFile!=null & !currentOpenJarFile.getName().equals(filePath)){
				currentOpenJarFile.close();
				currentOpenJarFile = new JarFile(filePath);
			}			
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
			jarFile = new JarFile(siteURL.getPath()+pluginEntry.toString()+".jar");
			result = new String[jarFile.size()];
			Enumeration enum = jarFile.entries();
			int loop = 0;
			while (enum.hasMoreElements()){
				result[loop] = (String)enum.nextElement();
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

}

