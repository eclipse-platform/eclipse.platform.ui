package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.update.core.*;
import org.eclipse.update.core.ICategory;
import org.eclipse.update.core.IImport;
import org.xml.sax.SAXException;
/**
 * Abstract Class that implements most of the behavior of a feature
 * A feature ALWAYS belongs to an ISite
 */
public abstract class AbstractFeature implements IFeature {

	/**
	 * 
	 */
	public static final String FEATURE_XML = "feature.xml";

	/**
	 * Identifier of the Feature
	 */
	private VersionedIdentifier versionIdentifier;

	/**
	 * Site in which teh feature resides
	 */
	private ISite site;

	/**
	 * User label fo the Feature
	 */
	private String label;

	/**
	 * reference to the feature inside the site.
	 * This URL can be a Jar file, a directory or any URL that is understood by the 
	 * Subclass of AbstractFeature.
	 */
	private URL url;

	/**
	 * Url and label of site where update of this feature can ve found
	 */
	private IInfo updateInfo;

	/**
	 * Url and label of site where other informations related to this feature can be found
	 */
	private List discoveryInfos;

	/**
	 * provider of the Feature
	 */
	private String provider;

	/**
	 * Short description and url for long description of this feature
	 */
	private IInfo description;

	/**	
	 * Short copyright and url for long copyright of this feature
	 */
	private IInfo copyright;

	/**
	 * Short license and url for long license of this feature
	 */
	private IInfo license;

	/**
	 * Image (shoudl be either GIF or JPG)
	 */
	private URL image;

	private String nl;
	private String os;
	private String ws;

	/**
	 * category String; From teh XML file
	 */
	private List categoryString;

	/**
	 * category : delegate to teh site
	 */
	private List categories;

	/**
	 * List of plugin entries teh feature contains
	 * read from teh xml file
	 */
	private List pluginEntries;

	/**
	 * List of plugin the feature require
	 * to be installed in the site before this feature
	 * can be installed
	 */
	private List requires;

	/**
	 * private internal
	 * used for lazy instantiation and 
	 * hydration with the XML file
	 */
	private boolean isInitialized = false;

	/**
	 * Copy constructor
	 */
	public AbstractFeature(IFeature sourceFeature, ISite targetSite) throws CoreException {
		this(sourceFeature.getURL(), targetSite);
		this.versionIdentifier = sourceFeature.getIdentifier();
		this.label = sourceFeature.getLabel();
		this.url = sourceFeature.getURL();
		this.updateInfo = sourceFeature.getUpdateInfo();
		this.setDiscoveryInfos(sourceFeature.getDiscoveryInfos());
		this.provider = sourceFeature.getProvider();
		this.description = sourceFeature.getDescription();
		this.copyright = sourceFeature.getCopyright();
		this.license = sourceFeature.getLicense();
		this.setPluginEntries(sourceFeature.getPluginEntries());
		this.isInitialized = true;
	}

	/**
	 * Constructor
	 */
	public AbstractFeature(URL url, ISite targetSite) {
		this.site = targetSite;
		this.url = url;
	}

	/**
	 * @see IFeature#getIdentifier()
	 */
	public VersionedIdentifier getIdentifier() {
		if (versionIdentifier == null && !isInitialized)
			logNotInitialized();
		return versionIdentifier;
	}

	/**
	 * @see IFeature#getSite()
	 * Do not hydrate, value set ins constructor
	 */
	public ISite getSite() {
		return site;
	}

	/**
	 * @see IFeature#getLabel()
	 */
	public String getLabel() {
		if (label == null && !isInitialized)
			logNotInitialized();
		return label;
	}

	/**
	 * @see IFeature#getURL()
	 * Do not hydrate. Initialization will not populate the url.
	 * If the URL is null, then the creation hasn't set the URL, so return null.
	 * It has to be set at creation time or using the set method 
	 * Usually done from the site when creating the Feature. 
	 * 
	 * The DefaultSiteParser is setting it at creation time
	 */
	public URL getURL() {
		return url;
	}

	/**
	 * @see IFeature#getRootURL()
	 * In general, the Root URL is the URL of teh Feature
	 * 
	 * The RootURL is used to calculate relative URL for teh feature
	 * In case of a file feature, you can just append teh relative path
	 * to the URL of teh feature
	 * 
	 * In case of a JAR file, you cannot *just* append the file 
	 * You have to transfrom the URL
	 * 
	 * Can be overriden 
	 */
	public URL getRootURL() throws MalformedURLException {
		return url;
	}

	/**
	 * @see IFeature#getUpdateInfo()
	 */
	public IInfo getUpdateInfo() {
		if (updateInfo == null && !isInitialized)
			logNotInitialized();
		return updateInfo;
	}

	/**
	 * @see IFeature#getDiscoveryInfos()
	 */
	public IInfo[] getDiscoveryInfos() {
		IInfo[] result = new IInfo[0];
		if (discoveryInfos == null && !isInitialized)
			logNotInitialized();
		if (!(discoveryInfos == null || discoveryInfos.isEmpty())) {
			result = new IInfo[discoveryInfos.size()];
			discoveryInfos.toArray(result);
		}
		return result;
	}

	/**
	 * @see IFeature#getProvider()
	 */
	public String getProvider() {
		if (provider == null && !isInitialized)
			logNotInitialized();
		return provider;
	}

	/**
	 * @see IFeature#getDescription()
	 */
	public IInfo getDescription() {
		if (description == null && !isInitialized)
			logNotInitialized();
		return description;
	}

	/**
	 * @see IFeature#getCopyright()
	 */
	public IInfo getCopyright() {
		if (copyright == null && !isInitialized)
			logNotInitialized();
		return copyright;
	}

	/**
	 * @see IFeature#getLicense()
	 */
	public IInfo getLicense() {
		if (license == null && !isInitialized)
			logNotInitialized();
		return license;
	}

	/**
	 * @see IFeature#getImage()
	 */
	public URL getImage() {
		if (image == null && !isInitialized)
			logNotInitialized();
		return image;
	}
	/**
	 * @see IFeature#getNL()
	 */
	public String getNL() {
		if (nl == null && !isInitialized)
			logNotInitialized();
		return nl;
	}

	/**
	 * @see IFeature#getOS()
	 */
	public String getOS() {
		if (os == null && !isInitialized)
			logNotInitialized();
		return os;
	}

	/**
	 * @see IFeature#getWS()
	 */
	public String getWS() {
		if (ws == null && !isInitialized)
			logNotInitialized();
		return ws;
	}

	/**
	 * Gets the categoryString
	 * @return Returns a String
	 */
	private List getCategoryString() {
		if (categoryString == null && !isInitialized)
			logNotInitialized();
		return categoryString;
	}

	/**
	 * Sets the site
	 * @param site The site to set
	 */
	public void setSite(ISite site) {
		this.site = site;
	}

	/**
	 * Sets the identifier
	 * @param identifier The identifier to set
	 */
	public void setIdentifier(VersionedIdentifier identifier) {
		this.versionIdentifier = identifier;
	}

	/**
	 * Sets the label
	 * @param label The label to set
	 */
	public void setLabel(String label) {
		this.label = label;
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
		if (discoveryInfos == null)
			discoveryInfos = new ArrayList(0);
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
	 * Sets the image
	 * @param image The image to set
	 */
	public void setImage(URL image) {
		this.image = image;
	}

	/**
	 * Sets the nl
	 * @param nl The nl to set
	 */
	public void setNL(String nl) {
		this.nl = nl;
	}

	/**
	 * Sets the os
	 * @param os The os to set
	 */
	public void setOS(String os) {
		this.os = os;
	}

	/**
	 * Sets the ws
	 * @param ws The ws to set
	 */
	public void setWS(String ws) {
		this.ws = ws;
	}

	/**
	 * @see IPluginContainer#getDownloadSize(IPluginEntry)
	 */
	public int getDownloadSize(IPluginEntry entry) {
		Assert.isTrue(entry instanceof PluginEntry);
		return ((PluginEntry) entry).getDownloadSize();
	}

	/**
	 * @see IPluginContainer#getInstallSize(IPluginEntry)
	 */
	public int getInstallSize(IPluginEntry entry) {
		Assert.isTrue(entry instanceof PluginEntry);
		return ((PluginEntry) entry).getInstallSize();
	}
	/**
	 * If one plug-in entry has an unknown size.
	 * then the download size is unknown.
	 * 
	 * @see IFeature#getDownloadSize(ISite)
	 * 
	 */
	public long getDownloadSize(ISite site) throws CoreException {
		int result = 0;
		IPluginEntry[] featureEntries = this.getPluginEntries();
		IPluginEntry[] siteEntries = site.getPluginEntries();
		IPluginEntry[] entriesToInstall = intersection(featureEntries, siteEntries);
		if (entriesToInstall == null || entriesToInstall.length == 0) {
			result = -1;
		} else {
			int pluginSize = 0;
			int i = 0;
			while (i < entriesToInstall.length && pluginSize != -1) {
				pluginSize = getDownloadSize(entriesToInstall[i]);
				result = pluginSize == -1 ? -1 : result + pluginSize;
				i++;
			}
		}
		return result;
	}
	/**
	 * If one plug-in entry has an unknown size.
	 * then the install size is unknown.
	 * 
	 * @see IFeature#getInstallSize(ISite)
	 */
	public long getInstallSize(ISite site) throws CoreException {
		int result = 0;
		IPluginEntry[] featureEntries = this.getPluginEntries();
		IPluginEntry[] siteEntries = site.getPluginEntries();
		IPluginEntry[] entriesToInstall = intersection(featureEntries, siteEntries);
		if (entriesToInstall == null || entriesToInstall.length == 0) {
			result = -1;
		} else {
			int pluginSize = 0;
			int i = 0;
			while (i < entriesToInstall.length && pluginSize != -1) {
				pluginSize = getInstallSize(entriesToInstall[i]);
				result = pluginSize == -1 ? -1 : result + pluginSize;
				i++;
			}
		}
		return result;
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
	 * @see IFeature#install(IFeature)
	 * 
	 */
	public void install(IFeature targetFeature) throws CoreException {
		try {
			IPluginEntry[] sourceFeaturePluginEntries = getPluginEntries();
			IPluginEntry[] targetSitePluginEntries = targetFeature.getSite().getPluginEntries();
			AbstractSite tempSite = (AbstractSite) SiteManager.getTempSite();

			// determine list of plugins to install
			// find the intersection between the two arrays of IPluginEntry...
			// The one teh site contains and teh one the feature contains
			IPluginEntry[] pluginsToInstall = intersection(sourceFeaturePluginEntries, targetSitePluginEntries);

			// private abstract - Determine list of content references id /archives id /bundles id that 
			// map the list of plugins to install
			String[] archiveIDToInstall = getContentReferenceToInstall(pluginsToInstall);

			// optmization, may be private to implementation
			// copy *blobs/content references/archives/bundles* in TEMP space
			if (((AbstractSite) getSite()).optimize()) {
				if (archiveIDToInstall != null) {
					URL sourceURL;
					String newFile;
					URL newURL;
					for (int i = 0; i < archiveIDToInstall.length; i++) {
							
						// transform the id by asking the site to map them to real URL inside the SITE	
						sourceURL  = ((AbstractSite) getSite()).getURL(archiveIDToInstall[i]);						
						// the name of the file in the temp directory
						// should be the regular plugins/pluginID_ver as the Temp site is OUR site
						newFile = AbstractSite.DEFAULT_PLUGIN_PATH + archiveIDToInstall[i];
						newURL = UpdateManagerUtils.resolveAsLocal(sourceURL, newFile);
		
						// transfer the possible mapping to the temp site						
						tempSite.addArchive(new Info(archiveIDToInstall[i],newURL));
					}
				}

				// the site of this feature now becomes the TEMP directory
				// FIXME: make sure there is no other issue
				// like asking for stuff that hasn't been copied
				// or reusing this feature
				// of having an un-manageable temp site

				this.setSite(tempSite);
			}
			
			
			// obtain the list of *Streamable Storage Unit*
			// from the archive
			if (pluginsToInstall != null) {
				InputStream inStream = null;
				for (int i = 0; i < pluginsToInstall.length; i++) {
					String[] names = getStorageUnitNames(pluginsToInstall[i]);
					if (names != null) {
						for (int j = 0; j < names.length; j++) {
							if ((inStream = getInputStreamFor(pluginsToInstall[i], names[j])) != null)
								targetFeature.store(pluginsToInstall[i], names[j], inStream);
						}
					}
				}
			}

			// install the Feature info
			InputStream inStream = null;
			String[] names = getStorageUnitNames();
			if (names != null) {
				for (int j = 0; j < names.length; j++) {
					if ((inStream = getInputStreamFor(names[j])) != null)
						 ((AbstractSite) targetFeature.getSite()).storeFeatureInfo(getIdentifier(), names[j], inStream);
				}
			}

		//Everything went fine, clean up TEMP drive
		UpdateManagerUtils.removeFromFileSystem(new File(tempSite.getURL().getPath()));

		} catch (IOException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error during Install", e);
			throw new CoreException(status);
		}
	}

	/** 
	 * initialize teh feature by reading the feature.xml if it exists
	 */
	public void initializeFeature() throws CoreException {
		try {
			new DefaultFeatureParser(getFeatureInputStream(), this);
		} catch (IOException e) {
			//FIXME: if we cannot find the feature and or the feature.xml
			// is it an error or a warning ???
			// I do not believe we should stop the execution for that...
			// but we must Log it all the time, not only when debugging...
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, "Error opening feature.xml in the feature archive:"+url.toExternalForm(), e);
			UpdateManagerPlugin.getPlugin().getLog().log(status);
		} catch (SAXException e){
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, "Error parsing feature.xml in the feature archive:"+url.toExternalForm(), e);
			throw new CoreException(status);			
		}
	}

	/**
	 * Logs that an attempt to read a non initialize variable has been made
	 */
	private void logNotInitialized() {
		Exception trace = new Exception("Attempt to read uninitialized variable");
		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, "the program is reading a variable of Feature before loading it", trace);
		UpdateManagerPlugin.getPlugin().getLog().log(status);
	}

	/**
	 * Returns the intersection between two array of PluginEntries.
	 */
	private IPluginEntry[] intersection(IPluginEntry[] array1, IPluginEntry[] array2) {
		if (array1 == null || array1.length == 0) {
			return array2;
		}
		if (array2 == null || array2.length == 0) {
			return array1;
		}

		List list1 = Arrays.asList(array1);
		List result = new ArrayList(0);
		for (int i = 0; i < array2.length; i++) {
			if (!list1.contains(array2[i]))
				result.add(array2[i]);
		}
		return (IPluginEntry[]) result.toArray();
	}

	/**
	 * @see IPluginContainer#getPluginEntries()
	 */
	public IPluginEntry[] getPluginEntries() {
		IPluginEntry[] result = new IPluginEntry[0];
		if (pluginEntries == null && !isInitialized)
			logNotInitialized();
		if (!(pluginEntries == null || pluginEntries.isEmpty())) {
			result = new IPluginEntry[pluginEntries.size()];
			pluginEntries.toArray(result);
		}
		return result;
	}

	/**
	 * @see IFeature#getCategories()
	 */
	public ICategory[] getCategories() {

		if (categories == null) {
			categories = new ArrayList();
			List categoriesAsString = getCategoryString();
			if (categoriesAsString != null && !categoriesAsString.isEmpty()) {
				Iterator iter = categoriesAsString.iterator();
				while (iter.hasNext()) {
					categories.add(((AbstractSite) getSite()).getCategory((String) iter.next()));
				}
			}
		}

		ICategory[] result = new ICategory[0];

		if (!(categories == null || categories.isEmpty())) {
			result = new ICategory[categories.size()];
			categories.toArray(result);
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
	 * @see IFeature#getImports()
	 */
	public IImport[] getImports() {
		IImport[] result = new IImport[0];
		if (!(requires == null || requires.isEmpty())) {
			result = new IImport[requires.size()];
			requires.toArray(result);
		}
		return result;
	}

	/**
	 * Sets the pluginEntries
	 * @param pluginEntries The pluginEntries to set
	 */
	public void setPluginEntries(IPluginEntry[] pluginEntries) {
		if (pluginEntries != null) {
			for (int i = 0; i < pluginEntries.length; i++) {
				addPluginEntry(pluginEntries[i]);
			}
		}
	}

	/**
	 * Sets the categoryString
	 * @param categoryString The categoryString to set
	 */
	public void setCategoryString(String[] categoryString) {
		if (categoryString != null) {
			for (int i = 0; i < categoryString.length; i++) {
				addCategoryString(categoryString[i]);
			}
		}
	}

	/**
	 * Sets the import
	 * @param imports The imports to set
	 */
	public void setImports(IImport[] imports) {
		if (imports != null) {
			for (int i = 0; i < imports.length; i++) {
				addImport(imports[i]);
			}
		}
	}

	/**
	 * @see IPluginContainer#addPluginEntry(IPluginEntry)
	 */
	public void addPluginEntry(IPluginEntry pluginEntry) {
		if (pluginEntries == null)
			pluginEntries = new ArrayList(0);
		pluginEntries.add(pluginEntry);
	}

	/**
	 * Adds a categoryString
	 * @param categoryString The categoryString to add
	 */
	public void addCategoryString(String categoryString) {
		if (this.categoryString == null)
			this.categoryString = new ArrayList(0);
		this.categoryString.add(categoryString);
	}

	/**
	 * Adds an import
	 * @param anImport The import to add
	 */
	public void addImport(IImport anImport) {
		if (this.requires == null)
			this.requires = new ArrayList(0);
		this.requires.add(anImport);
	}

	/**
	 * @see IPluginContainer#store(IPluginEntry, String, InputStream)
	 */
	public void store(IPluginEntry pluginEntry, String contentKey, InputStream inStream) throws CoreException {
		// check if pluginEntry already exists before passing to the site
		// anything else ?
		boolean found = false;
		int i = 0;
		IPluginEntry[] entries = getPluginEntries();
		while (i < entries.length && !found) {
			if (entries[i].equals(pluginEntry)) {
				found = true;
			}
			i++;
		}
		if (!found) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "The plugin:" + pluginEntry.getIdentifier().toString() + " is not part of the plugins of the feature:" + this.getIdentifier().toString(), null);
			throw new CoreException(status);
		}
		getSite().store(pluginEntry, contentKey, inStream);
	}

	/**
	 * returns the Stream corresponding to the feature.xml file
	 */
	protected InputStream getFeatureInputStream() throws CoreException, IOException {

		InputStream resultStream = null;

		// get the stream inside the Feature
		URL insideURL = null;
		URL rootURL = null;
		try {
			rootURL = getRootURL();
			insideURL = new URL(rootURL, FEATURE_XML);
		} catch (MalformedURLException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error creating the URL for" + rootURL.toExternalForm(), e);
			throw new CoreException(status);
		}

		resultStream = insideURL.openStream();
		return resultStream;
	};

	/**
	 * @see IFeature#getContentReferences()
	 * Private implementation of the feature. return the list of ID.
	 * Call the site with the ID to get the URL of the contentReference of the Site
	 */
	public abstract String[] getContentReferences();

	/**
	 * return the list of FILE to be transfered for a Plugin
	 */
	protected abstract String[] getStorageUnitNames(IPluginEntry pluginEntry) throws CoreException;

	/**
	 * return the list of FILE to be transfered from within the Feature
	 */
	protected abstract String[] getStorageUnitNames() throws CoreException;

	/**
	 * return the Stream of the FILE to be transfered for a Plugin
	 */
	protected abstract InputStream getInputStreamFor(IPluginEntry pluginEntry, String name) throws CoreException;

	/**
	 * return the Stream of FILE to be transfered from within the Feature
	 */
	protected abstract InputStream getInputStreamFor(String name) throws CoreException;

	/**
	 * returns the list of archive to transfer/install
	 * in order to install the list of plugins
	 * 
	 * @param pluginsToInstall list of plugin to install 
	 */
	protected abstract String[] getContentReferenceToInstall(IPluginEntry[] pluginsToInstall);

}