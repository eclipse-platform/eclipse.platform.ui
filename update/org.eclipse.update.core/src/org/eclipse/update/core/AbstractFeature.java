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
import org.eclipse.update.internal.core.DefaultFeatureParser;
/**
 * Abstract Class that implements most of the behavior of a feature
 * A feature ALWAYS belongs to an ISite
 */


public abstract class AbstractFeature implements IFeature {

private VersionedIdentifier versionIdentifier;
	private ISite site;
	private String label;
	private URL url;
	private IInfo updateInfo;
	private URL infoURL;
	private List discoveryInfos;	
	private String provider;
	private IInfo description;
	private IInfo copyright;
	private IInfo license;
	private String[] contentReferences;
	private List pluginEntries;
	private boolean isInitialized = false;
	
	
	/**
	 * Copy constructor
	 */
	public AbstractFeature(IFeature sourceFeature, ISite targetSite){
		this(sourceFeature.getIdentifier(),targetSite);
		this.label 			= sourceFeature.getLabel();
		this.url			= sourceFeature.getURL();
		this.updateInfo 	= sourceFeature.getUpdateInfo();
		this.setDiscoveryInfos(sourceFeature.getDiscoveryInfos());
		this.provider 		= sourceFeature.getProvider();
		this.description 	= sourceFeature.getDescription();
		this.copyright      = sourceFeature.getCopyright();
		this.license		= sourceFeature.getLicense();
		this.pluginEntries  = Arrays.asList(sourceFeature.getPluginEntries());
		this.isInitialized 	= true;
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
		//if (!isInitialized)init();
		// site is donein constructor.. avoid recursive pattern.
		return site;
	}

	/**
	 * @see IFeature#getLabel()
	 */
	public String getLabel() {
		if (label==null && !isInitialized)init();
		return label;
	}
	
	/**
	 * @see IFeature#getURL()
	 * Do not initialize. Initialization will not populate the url.
	 * It has to be set at creation time or using the set method 
	 * Usually done from the site when creating the Feature. 
	 */
	public URL getURL() {
		return url;
	}
	
	

	/**
	 * @see IFeature#getUpdateInfo()
	 */
	public IInfo getUpdateInfo() {
		if (updateInfo==null && !isInitialized)init();		
		return updateInfo;
	}

	/**
	 * @see IFeature#getInfoURL()
	 * @deprecated use getDescription().getURL()
	 */
	public URL getInfoURL() {
		return getDescription().getURL();
	}

	/**
	 * @see IFeature#getDiscoveryInfos()
	 */
	public IInfo[] getDiscoveryInfos() {
		if (discoveryInfos==null && !isInitialized)init();		
		
		IInfo[] result = null;
		// FIXME:
		if (discoveryInfos!=null && !discoveryInfos.isEmpty()){
			result = (IInfo[])discoveryInfos.toArray(new IInfo[discoveryInfos.size()]);
		}
		return result;
	}

	/**
	 * @see IFeature#getProvider()
	 */
	public String getProvider() {
		if (provider == null && !isInitialized)init();		
		return provider;
	}

	/**
	 * @see IFeature#getDescription()
	 */
	public IInfo getDescription() {
		if (description==null && !isInitialized)init();		
		return description;
	}
	
	/**
	 * @see IFeature#getCopyright()
	 */
	public IInfo getCopyright() {
		if (copyright==null && !isInitialized)init();		
		return copyright;
	}
	
	/**
	 * @see IFeature#getLicense()
	 */
	public IInfo getLicense() {
		if (license==null && !isInitialized)init();		
		return license;
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
	 * Sets the infoURL
	 * @param infoURL The infoURL to set
	 * @deprecated use setDescription
	 */
	public void setInfoURL(URL infoURL) {
		setDescription(new org.eclipse.update.internal.core.Info(infoURL));
	}	
	
	/**
	 * Sets the url
	 * @param url The url to set
	 */
	public void setURL(URL url) {
		this.url = url;
	}

	/**
	 * Sets the updateInfo
	 * @param updateInfo The updateInfo to set
	 */
	public void setUpdateInfo(IInfo updateInfo) {
		this.updateInfo = updateInfo;
	}

	

	/**
	 * Sets the discoveryInfos
	 * @param discoveryInfos The discoveryInfos to set
	 */
	public void setDiscoveryInfos(IInfo[] discoveryInfos) {
		if (discoveryInfos != null) {
			this.discoveryInfos = (new ArrayList());
			for (int i = 0; i < discoveryInfos.length; i++) {
				this.discoveryInfos.add(discoveryInfos[i]);
			}
		}
	}
	
	
	
	/**
	 * Adds a discoveryInfo
	 * @param discoveryInfo The discoveryInfo to add
	 */
	public void addDiscoveryInfo(IInfo discoveryInfo) {
		if (discoveryInfos == null) discoveryInfos = new ArrayList(0);
		discoveryInfos.add(discoveryInfo);
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
	public void setDescription(IInfo description) {
		this.description = description;
	}
	
	/**
	 * Sets the copyright
	 * @param copyright The copyright to set
	 */
	public void setCopyright(IInfo copyright) {
		this.copyright = copyright;
	}
	
	/**
	 * Sets the license
	 * @param license The license to set
	 */
	public void setLicense(IInfo license) {
		this.license = license;
	}
	
	/**
	 * @see IPluginContainer#getDownloadSize(IPluginEntry)
	 */
	public int getDownloadSize(IPluginEntry entry) {
		//TODO:
		return 0;
	}

	/**
	 * @see IPluginContainer#getInstallSize(IPluginEntry)
	 */
	public int getInstallSize(IPluginEntry entry) {
		//TODO:
		return 0;
	}
	/**
	 * @see IFeature#getDownloadSize(ISite)
	 */
	public long getDownloadSize(ISite site) {
		//TODO:
		return 0;
	}


	/**
	 * @see IFeature#getInstallSize(ISite)
	 */
	public long getInstallSize(ISite site) {
		//TODO:
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
		// find the intersection between the two arrays of IPluginEntry...
		IPluginEntry[] pluginsToInstall = intersection(sourceFeaturePluginEntries,targetSitePluginEntries);
		
		// private abstract - Determine list of content references that 
		// map the list of plugins to install
		String[] contentReferenceToInstall = getContentReferenceToInstall(pluginsToInstall);
		
		// optmization, may be private to implementation
		// copy *blobs* in TEMP space
		for (int i=0;i<contentReferenceToInstall.length;i++){
			InputStream sourceContentReferenceStream = ((AbstractSite)getSite()).getInputStream(this,contentReferenceToInstall[i]);
			if (sourceContentReferenceStream!=null){
				String newFile = SiteManager.getTempSite().getURL().getPath()+contentReferenceToInstall[i];
				FileOutputStream localContentReferenceStream = new FileOutputStream(newFile);
				transferStreams(sourceContentReferenceStream,localContentReferenceStream);
			} else {
				throw new IOException("Couldn\'t find the file: "+contentReferenceToInstall[i]+" on the site:"+getSite().getURL().toExternalForm());
			}
		}
		this.setSite(SiteManager.getTempSite());
		
		// obtain the list of *Streamable Storage Unit*
		InputStream inStream = null;
		for (int i=0;i<pluginsToInstall.length;i++){
			String[] names = getStorageUnitNames(pluginsToInstall[i]);
			for (int j= 0; j<names.length;j++){
				if ((inStream=getInputStreamFor(pluginsToInstall[i],names[j]))!=null)
					targetFeature.store(pluginsToInstall[i],names[j],inStream);
			}
			
		}
		} catch (IOException e){
			//FIXME: implement serviceability
			e.printStackTrace();
		}
	}
	
	
	/** 
	 * initialize teh feature by reading the feature.xml if it exists
	 */
	private void init(){
		//TODO:
		if (url!=null){ 
			try {
			DefaultFeatureParser parser = new DefaultFeatureParser(getFeatureInputStream(),this);
			} catch (IOException e){
				//FIXME:
				e.printStackTrace();
			} catch (org.xml.sax.SAXException e){
				//FIXME:
				e.printStackTrace();
			}
		}
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


	/**
	 * Returns the intersection between two array of PluginEntries.
	 */
	private IPluginEntry[] intersection(IPluginEntry[] array1, IPluginEntry[] array2) {
		if (array1==null){return array2;}
		if (array2==null){return array1;}
		
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
	 * Does not return null
	 */
	public IPluginEntry[] getPluginEntries() {	
		IPluginEntry[] result = null;
		//FIXME: 
		// especially teh ' does nto return null'
		if (!pluginEntries.isEmpty()) {
			result = (IPluginEntry[])(pluginEntries.toArray(new IPluginEntry[pluginEntries.size()]));
		} else {
			result = new IPluginEntry[0];
		}
		return result;
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
		if (pluginEntries==null)
			pluginEntries = new ArrayList(0);
		pluginEntries.add(pluginEntry);
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
	protected abstract InputStream getFeatureInputStream() throws IOException ; 
	
	/**
	 * 
	 */
	protected abstract String[] getContentReferenceToInstall(IPluginEntry[] pluginsToInstall);

	

	
	/**
	 * Sets the updateURL
	 * @param updateURL The updateURL to set
	 * @deprecated use setUpdateInfo
	 */
	public void setUpdateURL(URL updateURL) {
		setUpdateInfo(new org.eclipse.update.internal.core.Info(updateURL));
	}
	
	/**
	 * @deprecated use getUpdateInfo().getURL()
	 */
	public URL getUpdateURL() {
		return getUpdateInfo().getURL();
	}
	
	/**
	 * @deprecated use getDiscoveryInfos and obtain the URL for each
	 */
	public URL[] getDiscoveryURLs() {
		return new URL[0];
	}
	
	/**
	 * Sets the discoveryURLs
	 * @param discoveryURLs The discoveryURLs to set
	 * @deprecated use setDiscoveryInfos
	 */
	public void setDiscoveryURLs(URL[] discoveryURLs) {
		//
	}
	
	
	
	

	

	

}

