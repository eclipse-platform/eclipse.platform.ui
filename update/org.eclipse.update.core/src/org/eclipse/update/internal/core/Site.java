package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.xml.sax.SAXException;

public abstract class Site implements ISite, IWritable {

	/**
	 * default path under the site where plugins will be installed
	 */
	public static final String DEFAULT_PLUGIN_PATH = "plugins/";
	/**
	 * default path under the site where plugins will be installed
	 */
	//FIXME: fragment
	public static final String DEFAULT_FRAGMENT_PATH = "fragments/";

	/**
	 * default path, under site, where featuresConfigured will be installed
	 */
	public static final String DEFAULT_FEATURE_PATH = "features/";

	public static final String SITE_FILE = "site";
	public static final String SITE_XML = SITE_FILE + ".xml";
	private boolean isManageable = false;
	private boolean isInitialized = false;
	private SiteParser parser;

	/**
	 * the tool will create the directories on the file 
	 * system if needed.
	 */
	public static boolean CREATE_PATH = true;

	private ListenersList listeners = new ListenersList();
	private URL siteURL;
	private String siteType;
	private URL infoURL;
	private List features;
	private Set categories;
	private List archives;

	/**
	 * Constructor for AbstractSite
	 */
	public Site(URL siteReference) throws CoreException, InvalidSiteTypeException {
		super();
		this.siteURL = siteReference;
		initializeSite();
	}

	/**
	 * Initializes the site by reading the site.xml file
	 * 
	 */
	private void initializeSite() throws CoreException, InvalidSiteTypeException {
		try {
			URL siteXml = new URL(siteURL, SITE_XML);
			parser = new SiteParser(siteXml.openStream(), this);
			isManageable = true;
		} catch (FileNotFoundException e) {
			//attempt to parse the site if possible
			parseSite();
			// log not manageable site
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(siteURL.toExternalForm() + " is not manageable by Update Manager: Couldn't find the site.xml file.");
			}
		} catch (Exception e) {

			// is is an InvalidSiteTypeException meaning the type of the site is wrong ?
			if (e instanceof SAXException) {
				SAXException exception = (SAXException) e;
				if (exception.getException() instanceof InvalidSiteTypeException) {
					throw ((InvalidSiteTypeException) exception.getException());
				}
			}

			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error during parsing of the site XML", e);
			throw new CoreException(status);
		} finally {
			isInitialized = true;
		}
	}

	/**
	 * Saves the site into the site.xml
	 */
	public void save() throws CoreException {
		File file = new File(getURL().getFile() + SITE_XML);
		try {
			PrintWriter fileWriter = new PrintWriter(new FileOutputStream(file));
			Writer writer = new Writer();
			writer.writeSite(this, fileWriter);
			fileWriter.close();
		} catch (FileNotFoundException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot save site into " + file.getAbsolutePath(), e);
			throw new CoreException(status);
		}
	}

	/**
	 * Logs that an attempt to read a non initialize variable has been made
	 */
	private void logNotInitialized() {
		Exception trace = new Exception("Attempt to read uninitialized variable");
		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, "the program is reading a variable of Site before loading it", trace);
		UpdateManagerPlugin.getPlugin().getLog().log(status);
	}

	/**
	 * @see ISite#addSiteChangedListener(ISiteChangedListener)
	 */
	public void addSiteChangedListener(ISiteChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * @see ISite#removeSiteChangedListener(ISiteChangedListener)
	 */
	public void removeSiteChangedListener(ISiteChangedListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * @see ISite#install(IFeature, IProgressMonitor)
	 */
	public IFeatureReference install(IFeature sourceFeature, IProgressMonitor monitor) throws CoreException {
		// should start Unit Of Work and manage Progress Monitor
		Feature localFeature = createExecutableFeature(sourceFeature);
		((Feature) sourceFeature).install(localFeature, monitor);
		IFeatureReference localReference = new FeatureReference(this, localFeature.getURL());
		this.addFeatureReference(localReference);

		// notify listeners
		Object[] siteListeners = listeners.getListeners();
		for (int i = 0; i < siteListeners.length; i++) {
			((ISiteChangedListener) siteListeners[i]).featureInstalled(localFeature);
		}
		return localReference;
	}

	/**
	 * @see ISite#remove(IFeature, IProgressMonitor)
	 */
	public void remove(IFeature feature, IProgressMonitor monitor) throws CoreException {

		// remove the feature and the plugins if they are not used and not activated

		// get the plugins from the feature
		IPluginEntry[] entries = feature.getPluginEntries();

		if (entries != null) {

			// get all the other plugins from all the other features
			Set allPluginID = new HashSet();
			IConfigurationSite[] allConfiguredSites = SiteManager.getLocalSite().getCurrentConfiguration().getConfigurationSites();
			if (allConfiguredSites != null) {
				for (int indexSites = 0; indexSites < allConfiguredSites.length; indexSites++) {
					IFeatureReference[] features = allConfiguredSites[indexSites].getSite().getFeatureReferences();
					if (features != null) {
						for (int indexFeatures = 0; indexFeatures < features.length; indexFeatures++) {
							if (!features[indexFeatures].getURL().equals(feature.getURL())) {
								IPluginEntry[] pluginEntries = features[indexFeatures].getFeature().getPluginEntries();
								if (pluginEntries != null) {
									for (int indexEntries = 0; indexEntries < pluginEntries.length; indexEntries++) {
										allPluginID.add(entries[indexEntries].getIdentifier());
									}
								}
							}
						}
					}
				}
			}

			// create the delta with the plugins that may be still used by other configured or unconfigured feature
			List pluginsToRemove = new ArrayList();
			for (int indexPlugins = 0; indexPlugins < entries.length; indexPlugins++) {
				if (!allPluginID.contains(entries[indexPlugins].getIdentifier())) {
					pluginsToRemove.add(entries[indexPlugins]);
				}
			}

			// find if the plugins are activated
			boolean somePluginsAreRunning = false;
			Iterator pluginsIter = pluginsToRemove.iterator();
			while (pluginsIter.hasNext() && !somePluginsAreRunning) {
				IPluginEntry entry = (IPluginEntry) pluginsIter.next();
				VersionedIdentifier element = entry.getIdentifier();
				Plugin plugin = Platform.getPlugin(element.getIdentifier());
				if (plugin != null) {
					PluginVersionIdentifier elementVersion = new PluginVersionIdentifier(element.getVersion().getMajorComponent(), element.getVersion().getMinorComponent(), element.getVersion().getServiceComponent());
					if (plugin.getDescriptor().getVersionIdentifier().equals(elementVersion) && plugin.getDescriptor().isPluginActivated()) {
						somePluginsAreRunning = true;
					}
				}
			}

			// if some plugins are running, we have to unconfigure the feature
			// restart and remove the feature
			// otherwise, we can unconfigure the feature and remove it
			if (!somePluginsAreRunning) {

				// remove plugins
				Iterator pluginsIterator = pluginsToRemove.iterator();
				while (pluginsIterator.hasNext()) {
					IPluginEntry entry = (IPluginEntry) pluginsIterator.next();
					remove(entry, monitor);
				}

				// remove feature
				 ((Feature) feature).remove(monitor);

				// remove feature reference
				IFeatureReference[] featureReferences = getFeatureReferences();
				if (featureReferences != null) {
					for (int indexRef = 0; indexRef < featureReferences.length; indexRef++) {
						IFeatureReference element = featureReferences[indexRef];
						if (element.getURL().equals(feature.getURL())) {
							features.remove(element);
							break;
						}
					}
				}

				// notify listeners
				ISiteChangedListener[] siteListeners = (ISiteChangedListener[]) listeners.getListeners();
				for (int i = 0; i < siteListeners.length; i++) {
					siteListeners[i].featureUninstalled(feature);
				}

			} else {
				// FIXME: throw error ? shoudl notfiy user, also should log which pugin are running

			}

		}
	}

	/**
	 * remove a plugin entry from the site...
	 */
	public void remove(IPluginEntry pluginEntry, IProgressMonitor monitor) throws CoreException {
		//FIXME: hum... just delete the directory or the JAR ? delegate to subclass ?

	}

	/**
	 * 
	 */
	public abstract Feature createExecutableFeature(IFeature sourceFeature) throws CoreException;

	/**
	 * store Feature files/ Fetaures info into the Site
	 */
	protected abstract void storeFeatureInfo(VersionedIdentifier featureIdentifier, String contentKey, InputStream inStream) throws CoreException;

	/**
	 * return the URL of the archive ID
	 */
	public abstract URL getURL(String archiveID) throws CoreException;
	/**
	 * returns the default prefered feature type for this site
	 */
	public abstract String getDefaultFeatureType(URL featureURL) throws CoreException;

	/**
	 * parse the physical site to initialize the site object
	 * @throws CoreException
	 */
	protected abstract void parseSite() throws CoreException;

	/**
	 * returns true if we need to optimize the install by copying the 
	 * archives in teh TEMP directory prior to install
	 * Default is true
	 */
	public boolean optimize() {
		return true;
	}

	/**
	 * Gets the siteURL
	 * @return Returns a URL
	 */
	public URL getURL() {
		return siteURL;
	}

	/**
	 * return the appropriate resource bundle for this site
	 */
	public ResourceBundle getResourceBundle() throws IOException, CoreException {
		ResourceBundle bundle = null;
		try {
			ClassLoader l = new URLClassLoader(new URL[] { this.getURL()}, null);
			bundle = ResourceBundle.getBundle(SITE_FILE, Locale.getDefault(), l);
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
	 * Gets the featuresConfigured
	 * @return Returns a IFeatureReference[]
	 */
	public IFeatureReference[] getFeatureReferences() {
		IFeatureReference[] result = new IFeatureReference[0];
		if (!(features == null || features.isEmpty())) {
			result = new IFeatureReference[features.size()];
			features.toArray(result);
		}
		return result;
	}

	/**
	 * adds a feature
	 * The feature is considered already installed. It does not install it.
	 * @param feature The feature to add
	 */
	public void addFeatureReference(IFeatureReference feature) {
		if (features == null) {
			features = new ArrayList(0);
		}
		this.features.add(feature);
	}

	/**
	 * @see ISite#getArchives()
	 */
	public IInfo[] getArchives() {
		IInfo[] result = new IInfo[0];
		if (archives == null && !isInitialized)
			logNotInitialized();
		if (!(archives == null || archives.isEmpty())) {
			result = new IInfo[archives.size()];
			archives.toArray(result);
		}
		return result;
	}

	/**
	 * return the URL associated with the id of teh archive for this site
	 * return null if the archiveId is null, empty or 
	 * if teh list of archives on the site is null or empty
	 * of if there is no URL associated with the archiveID for this site
	 */
	public URL getArchiveURLfor(String archiveId) {
		URL result = null;
		boolean found = false;

		if (!(archiveId == null || archiveId.equals("") || archives == null || archives.isEmpty())) {
			Iterator iter = archives.iterator();
			IInfo info;
			while (iter.hasNext() && !found) {
				info = (IInfo) iter.next();
				if (archiveId.trim().equalsIgnoreCase(info.getText())) {
					result = info.getURL();
					found = true;
				}
			}
		}

		//DEBUG:
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_INSTALL) {
			String debugString = "Searching archive ID:" + archiveId + " in Site:" + getURL().toExternalForm() + "...";
			if (found) {
				debugString += "found , pointing to:" + result.toExternalForm();
			} else {
				debugString += "NOT FOUND";
			}
			UpdateManagerPlugin.getPlugin().debug(debugString);
		}

		return result;
	}

	/**
	 * adds an archive
	 * @param archive The archive to add
	 */
	public void addArchive(IInfo archive) {
		if (archives == null) {
			archives = new ArrayList(0);
		}
		if (getArchiveURLfor(archive.getText()) != null) {
			// DEBUG:		
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug("The Archive with ID:" + archive.getText() + " already exist on the site.");
			}
		} else {
			this.archives.add(archive);
		}
	}

	/**
	 * Sets the archives
	 * @param archives The archives to set
	 */
	public void setArchives(IInfo[] _archives) {
		if (_archives != null) {
			for (int i = 0; i < _archives.length; i++) {
				this.addArchive(_archives[i]);
			}
		}
	}

	/**
	 * @see ISite#getInfoURL()
	 */
	public URL getInfoURL() {
		if (isManageable) {
			if (infoURL == null && !isInitialized)
				logNotInitialized();
		}
		return infoURL;
	}

	/**
	 * Sets the infoURL
	 * @param infoURL The infoURL to set
	 */
	public void setInfoURL(URL infoURL) {
		this.infoURL = infoURL;
	}

	/**
	 * @see ISite#getCategories()
	 */
	public ICategory[] getCategories() {
		ICategory[] result = new ICategory[0];
		if (isManageable) {
			if (categories == null && !isInitialized)
				logNotInitialized();
			if (!categories.isEmpty()) {
				result = new ICategory[categories.size()];
				categories.toArray(result);
			}
		}
		return result;
	}

	/**
	 * adds a category
	 * @param category The category to add
	 */
	public void addCategory(ICategory category) {
		if (this.categories == null) {
			this.categories = new TreeSet(Category.getComparator());
		}
		this.categories.add(category);
	}

	/**
	 * returns the associated ICategory
	 */
	public ICategory getCategory(String key) {
		ICategory result = null;
		boolean found = false;

		if (isManageable) {
			if (categories == null)
				logNotInitialized();
			Iterator iter = categories.iterator();
			ICategory currentCategory;
			while (iter.hasNext() && !found) {
				currentCategory = (ICategory) iter.next();
				if (currentCategory.getName().equals(key)) {
					result = currentCategory;
					found = true;
				}
			}
		}

		//DEBUG:
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS && !found) {
			UpdateManagerPlugin.getPlugin().debug("Cannot find:" + key + " category in site:" + this.getURL().toExternalForm());
			if (!isManageable)
				UpdateManagerPlugin.getPlugin().debug("The Site is not manageable. Does not contain ste.xml");
			if (categories == null || categories.isEmpty())
				UpdateManagerPlugin.getPlugin().debug("The Site does not contain any categories.");
		}

		return result;
	}

	/*
	 * @see IWritable#write(int, PrintWriter)
	 */
	public void write(int indent, PrintWriter w) {

		String gap = "";
		for (int i = 0; i < indent; i++)
			gap += " ";
		String increment = "";
		for (int i = 0; i < IWritable.INDENT; i++)
			increment += " ";

		w.print(gap + "<" + SiteParser.SITE + " ");
		// FIXME: site type to implement
		// 
		// Site URL
		String URLInfoString = null;
		if (getInfoURL() != null) {
			URLInfoString = UpdateManagerUtils.getURLAsString(this.getURL(), getInfoURL());
			w.print("url=\"" + Writer.xmlSafe(URLInfoString) + "\"");
		}
		w.println(">");
		w.println("");

		IFeatureReference[] refs = getFeatureReferences();
		for (int index = 0; index < refs.length; index++) {
			FeatureReference element = (FeatureReference) refs[index];
			element.write(indent, w);
		}
		w.println("");

		IInfo[] archives = getArchives();
		for (int index = 0; index < archives.length; index++) {
			IInfo element = (IInfo) archives[index];
			URLInfoString = UpdateManagerUtils.getURLAsString(this.getURL(), element.getURL());
			w.println(gap + "<" + SiteParser.ARCHIVE + " id=\"" + Writer.xmlSafe(element.getText()) + "\" url=\"" + Writer.xmlSafe(URLInfoString) + "\"/>");
		}
		w.println("");

		ICategory[] categories = getCategories();
		for (int index = 0; index < categories.length; index++) {
			Category element = (Category) categories[index];
			w.println(gap + "<" + SiteParser.CATEGORY_DEF + " label=\"" + Writer.xmlSafe(element.getLabel()) + "\" name=\"" + Writer.xmlSafe(element.getName()) + "\">");

			IInfo info = element.getDescription();
			if (info != null) {
				w.print(gap + increment + "<" + SiteParser.DESCRIPTION + " ");
				URLInfoString = null;
				if (info.getURL() != null) {
					URLInfoString = UpdateManagerUtils.getURLAsString(this.getURL(), info.getURL());
					w.print("url=\"" + Writer.xmlSafe(URLInfoString) + "\"");
				}
				w.println(">");
				if (info.getText() != null) {
					w.println(gap + increment + increment + Writer.xmlSafe(info.getText()));
				}
				w.print(gap + increment + "</" + SiteParser.DESCRIPTION + ">");
			}
			w.println(gap + "</" + SiteParser.CATEGORY_DEF + ">");

		}
		w.println("");
		// end
		w.println("</" + SiteParser.SITE + ">");
	}

	/*
	 * @see IPluginContainer#getPluginEntries()
	 */
	public IPluginEntry[] getPluginEntries() {
		return null;
	}

	/*
	 * @see IPluginContainer#getPluginEntryCount()
	 */
	public int getPluginEntryCount() {
		return 0;
	}

	/*
	 * @see IPluginContainer#getDownloadSize(IPluginEntry)
	 */
	public int getDownloadSize(IPluginEntry entry) {
		return 0;
	}

	/*
	 * @see IPluginContainer#getInstallSize(IPluginEntry)
	 */
	public int getInstallSize(IPluginEntry entry) {
		return 0;
	}

	/*
	 * @see IPluginContainer#addPluginEntry(IPluginEntry)
	 */
	public void addPluginEntry(IPluginEntry pluginEntry) {
	}

	/*
	 * @see IPluginContainer#store(IPluginEntry, String, InputStream)
	 */
	public void store(IPluginEntry entry, String name, InputStream inStream) throws CoreException {
	}

	/*
	 * @see ISite#getType()
	 */
	public String getType() {
		return siteType;
	};

}