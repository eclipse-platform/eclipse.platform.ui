package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.xml.sax.SAXException;
/**
 * Abstract Class that implements most of the behavior of a feature
 * A feature ALWAYS belongs to an ISite
 */
public abstract class Feature implements IFeature {

	/**
	 * 
	 */
	private static CoreException CANCEL_EXCEPTION;

	/**
	 * 
	 */
	public static final String FEATURE_FILE = "feature";

	/**
	 * 
	 */
	public static final String FEATURE_XML = FEATURE_FILE + ".xml";

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
	 * Static block to initialize the possible CANCEL ERROR
	 * thrown when the USER cancels teh operation
	 */
	static {
		//	in case we throw a cancel exception
		String pluginId = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		IStatus cancelStatus = new Status(IStatus.ERROR, pluginId, IStatus.OK, "Install has been Cancelled", null);
		CANCEL_EXCEPTION = new CoreException(cancelStatus);
	}

	/**
	 * Copy constructor
	 */
	public Feature(IFeature sourceFeature, ISite targetSite) throws CoreException {
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
	public Feature(URL url, ISite targetSite) throws CoreException {
		this.site = targetSite;
		this.url = url;
		initializeFeature();
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
	public URL getRootURL() throws MalformedURLException, IOException, CoreException {
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
	 * returns the download size
	 * of the feature to be installed on the site.
	 * If the site is <code>null</code> returns the maximum size
	 * 
	 * If one plug-in entry has an unknown size.
	 * then the download size is unknown.
	 * 
	 * @see IFeature#getDownloadSize(ISite)
	 * 
	 */
	public long getDownloadSize(ISite site) throws CoreException {
		int result = 0;
		IPluginEntry[] entriesToInstall = this.getPluginEntries();
		if (site!=null){
			IPluginEntry[] siteEntries = site.getPluginEntries();
			entriesToInstall = intersection(entriesToInstall, siteEntries);
		}
			
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
	 * returns the install size
	 * of the feature to be installed on the site.
	 * If the site is <code>null</code> returns the maximum size
	 * 
	 * If one plug-in entry has an unknown size.
	 * then the install size is unknown.
	 * 
	 * @see IFeature#getInstallSize(ISite)
	 */
	public long getInstallSize(ISite site) throws CoreException {
		int result = 0;
		IPluginEntry[] entriesToInstall = this.getPluginEntries();
		if (site!=null){
			IPluginEntry[] siteEntries = site.getPluginEntries();
			entriesToInstall = intersection(entriesToInstall, siteEntries);
		}
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
	 * Method install.
	 * @param targetFeature
	 * @param monitor
	 * @throws CoreException
	 */
	public void install(IFeature targetFeature, IProgressMonitor monitor) throws CoreException {

		IPluginEntry[] sourceFeaturePluginEntries = getPluginEntries();
		IPluginEntry[] targetSitePluginEntries = targetFeature.getSite().getPluginEntries();
		Site tempSite = (Site) SiteManager.getTempSite();

		// determine list of plugins to install
		// find the intersection between the two arrays of IPluginEntry...
		// The one teh site contains and teh one the feature contains
		IPluginEntry[] pluginsToInstall = intersection(sourceFeaturePluginEntries, targetSitePluginEntries);

		// private abstract - Determine list of content references id /archives id /bundles id that 
		// map the list of plugins to install
		String[] archiveIDToInstall = getContentReferenceToInstall(pluginsToInstall);

		try {
			// optmization, may be private to implementation
			// copy *blobs/content references/archives/bundles* in TEMP space
			if (((Site) getSite()).optimize()) {
				if (archiveIDToInstall != null) {
					downloadArchivesLocally(tempSite, archiveIDToInstall, monitor);
				}
			}

			// obtain the list of *Streamable Storage Unit*
			// from the archive
			if (monitor != null) {
				int total = pluginsToInstall == null ? 1 : pluginsToInstall.length + 1;
				monitor.beginTask("Install feature " + getLabel(), total);
			}
			if (pluginsToInstall != null) {
				InputStream inStream = null;
				for (int i = 0; i < pluginsToInstall.length; i++) {
					if (monitor != null) {
						monitor.subTask("Installing plug-in: " + pluginsToInstall[i]);
						if (monitor.isCanceled()) {
							throw CANCEL_EXCEPTION;
						}
					}

					open(pluginsToInstall[i]);
					String[] names = getStorageUnitNames(pluginsToInstall[i]);
					if (names != null) {
						for (int j = 0; j < names.length; j++) {
							if ((inStream = getInputStreamFor(pluginsToInstall[i], names[j])) != null)
								targetFeature.store(pluginsToInstall[i], names[j], inStream);
						}
					}
					close(pluginsToInstall[i]);
					if (monitor != null) {
						monitor.worked(1);
						if (monitor.isCanceled()) {
							throw CANCEL_EXCEPTION;
						}
					}

				}
			}

			// install the Feature info
			InputStream inStream = null;
			String[] names = getStorageUnitNames();
			if (names != null) {
				openFeature();
				if (monitor != null) {
					monitor.subTask("Installing Feature information");
					if (monitor.isCanceled()) {
						throw CANCEL_EXCEPTION;
					}
				}

				for (int j = 0; j < names.length; j++) {
					if ((inStream = getInputStreamFor(names[j])) != null)
						 ((Site) targetFeature.getSite()).storeFeatureInfo(getIdentifier(), names[j], inStream);
				}
				closeFeature();
				if (monitor != null) {
					monitor.worked(1);
					if (monitor.isCanceled()) {
						throw CANCEL_EXCEPTION;
					}
				}

			}

		} catch (IOException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error during Install", e);
			throw new CoreException(status);
		} finally {
			// clean up TEMP drive
			String path = UpdateManagerUtils.getPath(tempSite.getURL());
			UpdateManagerUtils.removeFromFileSystem(new File(path));
		}
	}

	/** 
	 * initialize teh feature by reading the feature.xml if it exists
	 */
	public void initializeFeature() throws CoreException {
		if (!isInitialized) {
			isInitialized = true;
			InputStream featureStream = null;
			try {
				featureStream = getInputStreamFor(FEATURE_XML);
				new FeatureParser(featureStream, this);
			} catch (IOException e) {
				//FIXME: if we cannot find the feature and or the feature.xml
				// is it an error or a warning ???
				// I do not believe we should stop the execution for that...
				// but we must Log it all the time, not only when debugging...
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, "Error opening feature.xml in the feature archive:" + url.toExternalForm(), e);
				UpdateManagerPlugin.getPlugin().getLog().log(status);
			} catch (SAXException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, "Error parsing feature.xml in the feature archive:" + url.toExternalForm(), e);
				throw new CoreException(status);
			} finally {
				try {
					featureStream.close();
				} catch (Exception e) {}
				try {
					closeFeature();
				} catch (Exception e) {}
			}
		}
	}

	/**
	 */
	private void downloadArchivesLocally(ISite tempSite, String[] archiveIDToInstall, IProgressMonitor monitor) throws CoreException, IOException {

		URL sourceURL;
		String newFile;
		URL newURL;
		if (monitor != null) {
			monitor.beginTask("Download archives bundles to Temporary Space", archiveIDToInstall.length);
		}
		for (int i = 0; i < archiveIDToInstall.length; i++) {

			// transform the id by asking the site to map them to real URL inside the SITE	
			sourceURL = ((Site) getSite()).getURL(archiveIDToInstall[i]);
			if (monitor != null) {
				monitor.subTask("..." + archiveIDToInstall[i]);
			}
			// the name of the file in the temp directory
			// should be the regular plugins/pluginID_ver as the Temp site is OUR site
			newFile = Site.DEFAULT_PLUGIN_PATH + archiveIDToInstall[i];
			newURL = UpdateManagerUtils.resolveAsLocal(sourceURL, newFile,monitor);

			// transfer the possible mapping to the temp site						
			 ((Site) tempSite).addArchive(new Info(archiveIDToInstall[i], newURL));
			if (monitor != null) {
				monitor.worked(1);
				if (monitor.isCanceled()) {
					throw CANCEL_EXCEPTION;
				}
			}
		}

		// the site of this feature now becomes the TEMP directory
		// FIXME: make sure there is no other issue
		// like asking for stuff that hasn't been copied
		// or reusing this feature
		// of having an un-manageable temp site

		this.setSite(tempSite);

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
	 * perform pre processing before opening a plugin archive
	 * @param entry the plugin about to be opened
	 */
	protected void open(IPluginEntry entry) {};

	/**
	 * perform post processing to close a plugin archive
	 * @param entry the plugin about to be closed
	 */
	protected void close(IPluginEntry entry) throws IOException {};

	/**
	 * perform pre processing before opening the feature archive
	 */
	protected void openFeature() {};

	/**
	 * perform post processing to close a feature archive
	 */
	public void closeFeature() throws IOException {};

	/**
	 * return the appropriate resource bundle for this feature
	 */
	public ResourceBundle getResourceBundle() throws IOException, CoreException {
		ResourceBundle bundle = null;
		try {
			ClassLoader l = new URLClassLoader(new URL[] { this.getURL()}, null);
			bundle = ResourceBundle.getBundle(FEATURE_FILE, Locale.getDefault(), l);
		} catch (MissingResourceException e) {
			//ok, there is no bundle, keep it as null
			//DEBUG:
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(e.getLocalizedMessage() + ":" + this.getURL().toExternalForm());
			}
		}
		return bundle;
	}

	/**
	 * @see IFeature#getArchives()
	 * Private implementation of the feature. return the list of ID.
	 * Call the site with the ID to get the URL of the contentReference of the Site
	 */
	public abstract String[] getArchives();

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
	protected abstract InputStream getInputStreamFor(String name) throws IOException, CoreException;

	/**
	 * returns the list of archive to transfer/install
	 * in order to install the list of plugins
	 * 
	 * @param pluginsToInstall list of plugin to install 
	 */
	protected abstract String[] getContentReferenceToInstall(IPluginEntry[] pluginsToInstall);

}