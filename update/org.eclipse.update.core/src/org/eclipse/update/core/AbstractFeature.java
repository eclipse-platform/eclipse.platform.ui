package org.eclipse.update.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class AbstractFeature implements IFeature {


	private VersionedIdentifier versionIdentifier;
	private ISite site;
	private String label;
	private URL updateURL;
	private URL infoURL;
	private URL[] discoveryURLs;	
	private String provider;
	private String description;
	private String[] contentReferences;
	private IPluginEntry[] pluginEntries;
	
	
	/**
	 * Copy constructor
	 */
	public AbstractFeature(IFeature sourceFeature, ISite targetSite){
		this(sourceFeature.getIdentifier(),targetSite);
		this.label 			= sourceFeature.getLabel();
		this.updateURL 		= sourceFeature.getUpdateURL();
		this.infoURL		= sourceFeature.getInfoURL();
		this.discoveryURLs	= sourceFeature.getDiscoveryURLs();
		this.provider 		= sourceFeature.getProvider();
		this.description 	= sourceFeature.getDescription();
		this.pluginEntries	= sourceFeature.getPluginEntries();
	}
	
	/**
	 * Constructor
	 */
	public AbstractFeature(VersionedIdentifier identifier,ISite targetSite){
		this.site = targetSite;
		this.versionIdentifier = identifier;
	}
	
	/**
	 * @see IFeature#getIdentifier()
	 */
	public VersionedIdentifier getIdentifier() {
		return versionIdentifier;
	}

	/**
	 * @see IFeature#getSite()
	 */
	public ISite getSite() {
		return site;
	}

	/**
	 * @see IFeature#getLabel()
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @see IFeature#getUpdateURL()
	 */
	public URL getUpdateURL() {
		return updateURL;
	}

	/**
	 * @see IFeature#getInfoURL()
	 */
	public URL getInfoURL() {
		return infoURL;
	}

	/**
	 * @see IFeature#getDiscoveryURLs()
	 */
	public URL[] getDiscoveryURLs() {
		return discoveryURLs;
	}

	/**
	 * @see IFeature#getProvider()
	 */
	public String getProvider() {
		return provider;
	}

	/**
	 * @see IFeature#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the site
	 * @param site The site to set
	 */
	public void setSite(ISite site) {
		this.site = site;
	}

	/**
	 * Sets the label
	 * @param label The label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Sets the updateURL
	 * @param updateURL The updateURL to set
	 */
	public void setUpdateURL(URL updateURL) {
		this.updateURL = updateURL;
	}

	/**
	 * Sets the infoURL
	 * @param infoURL The infoURL to set
	 */
	public void setInfoURL(URL infoURL) {
		this.infoURL = infoURL;
	}

	/**
	 * Sets the discoveryURLs
	 * @param discoveryURLs The discoveryURLs to set
	 */
	public void setDiscoveryURLs(URL[] discoveryURLs) {
		this.discoveryURLs = discoveryURLs;
	}

	/**
	 * Sets the provider
	 * @param provider The provider to set
	 */
	public void setProvider(String provider) {
		this.provider = provider;
	}

	/**
	 * Sets the description
	 * @param description The description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @see IPluginContainer#getDownloadSize(IPluginEntry)
	 */
	public int getDownloadSize(IPluginEntry entry) {
		return 0;
	}

	/**
	 * @see IPluginContainer#getInstallSize(IPluginEntry)
	 */
	public int getInstallSize(IPluginEntry entry) {
		return 0;
	}
	/**
	 * @see IFeature#getDownloadSize(ISite)
	 */
	public long getDownloadSize(ISite site) {
		return 0;
	}


	/**
	 * @see IFeature#getInstallSize(ISite)
	 */
	public long getInstallSize(ISite site) {
		return 0;
	}


	/**
	 * @see IFeature#isExecutable()
	 */
	public boolean isExecutable() {
		return false;
	}


	/**
	 * @see IFeature#isInstallable()
	 */
	public boolean isInstallable() {
		return false;
	}


	/**
	 * @see IFeature#getContentReferences()
	 */
	public String[] getContentReferences() {
		return contentReferences;
	}


	/**
	 * @see IFeature#install(IFeature)
	 */
	public void install(IFeature targetFeature) {
		try {
		IPluginEntry[] sourceFeaturePluginEntries = getPluginEntries();
		IPluginEntry[] targetSitePluginEntries = targetFeature.getSite().getPluginEntries();
		
		// determine list of plugins to install
		// find the intersection between teh two arrays...
		IPluginEntry[] pluginsToInstall = intersection(sourceFeaturePluginEntries,targetSitePluginEntries);
		
		// private abstract - Determine list of content references that 
		// map teh list of plugins to install
		String[] contentReferenceToInstall = getContentReferenceToInstall(pluginsToInstall);
		
		// optmization, may be private to implementation
		// copy *blobs* in TEMP space
		for (int i=0;i<contentReferenceToInstall.length;i++){
			InputStream sourceContentReferenceStream = ((AbstractSite)getSite()).getInputStream(this,contentReferenceToInstall[i]);
			String newFile = SiteManager.getTempSite().getURL().getPath()+contentReferenceToInstall[i];
			FileOutputStream localContentReferenceStream = new FileOutputStream(newFile);
			transferStreams(sourceContentReferenceStream,localContentReferenceStream);
		}
		this.setSite(SiteManager.getTempSite());
		
		// obtain the list of *Streamable Storage Unit*
		for (int i=0;i<pluginsToInstall.length;i++){
			String[] names = getStorageUnitNames(pluginsToInstall[i]);
			for (int j= 0; j<names.length;j++){
				targetFeature.store(pluginsToInstall[i],names[j],getInputStreamFor(pluginsToInstall[i],names[j]));
			}
			
		}
		} catch (IOException e){
			//FIXME: implement serviceability
			e.printStackTrace();
		}
	}
/**
 * This method also closes both streams.
 * Taken from FileSystemStore
 */
private void transferStreams(InputStream source, OutputStream destination) throws IOException {
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


	/**
	 * Returns the intersection between two array of PluginEntries.
	 */
	private IPluginEntry[] intersection(IPluginEntry[] array1, IPluginEntry[] array2) {
		List list1 = Arrays.asList(array1);
		List result = new ArrayList(0);
		for (int i=0;i<array2.length;i++){
			if (!list1.contains(array2[i]))
				result.add(array2[i]);
		}
		return (IPluginEntry[])result.toArray();
	}


	/**
	 * @see IPluginContainer#getPluginEntries()
	 */
	public IPluginEntry[] getPluginEntries() {
		return pluginEntries;
	}


	/**
	 * @see IPluginContainer#getPluginEntryCount()
	 */
	public int getPluginEntryCount() {
		return getPluginEntries().length;
	}


	/**
	 * @see IPluginContainer#addPluginEntry(IPluginEntry)
	 */
	public void addPluginEntry(IPluginEntry pluginEntry) {
	}


	/**
	 * @see IPluginContainer#store(IPluginEntry, String, InputStream)
	 */
	public void store(IPluginEntry pluginEntry,String contentKey,InputStream inStream) {
		// check if pluginEntry already exists before passing to the site
		// anything else ?
		boolean found = false;
		int i = 0;
		while (i<getPluginEntries().length && !found){
			if (getPluginEntries()[i].equals(pluginEntry)) found=true;
		}
		if (!found){
			//FIXME: throw execption
		}
		getSite().store(pluginEntry,contentKey,inStream);
	}

	/**
	 * 
	 */
	protected abstract String[] getStorageUnitNames(IPluginEntry pluginEntry);
	
	/**
	 * 
	 */
	protected abstract InputStream getInputStreamFor(IPluginEntry pluginEntry,String name);
	
	/**
	 * 
	 */
	protected abstract String[] getContentReferenceToInstall(IPluginEntry[] pluginsToInstall);

}

