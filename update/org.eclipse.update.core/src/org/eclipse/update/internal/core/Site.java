package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;

public abstract class Site implements ISite {

	/**
	 * default path under the site where plugins will be installed
	 */
	public static final String DEFAULT_PLUGIN_PATH = "plugins/";
	/**
	 * default path under the site where plugins will be installed
	 */
	//FIXME: framgment
	public static final String DEFAULT_FRAGMENT_PATH = "fragments/";


	/**
	 * default path, under site, where features will be installed
	 */
	public static final String DEFAULT_FEATURE_PATH = "features/";

	public static final String SITE_FILE = "site";
	public static final String SITE_XML= SITE_FILE+".xml";
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
	private URL infoURL;
	private List features;
	private Set categories;
	private List archives;

	/**
	 * Constructor for AbstractSite
	 */
	public Site(URL siteReference) throws CoreException {
		super();
		this.siteURL = siteReference;
		// FIXME: not lazy... upfront loading
		initializeSite();
	}
	
	/**
	 * Initializes the site by reading the site.xml file
	 * 
	 */
	private void initializeSite() throws CoreException {
		try {
			URL siteXml = new URL(siteURL,SITE_XML);
			parser = new SiteParser(siteXml.openStream(),this);
			isManageable = true;
				 	
		} catch (FileNotFoundException e){
			//attempt to parse the site if possible
			parseSite();
			// log not manageable site
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS){
				UpdateManagerPlugin.getPlugin().debug(siteURL.toExternalForm()+" is not manageable by Update Manager: Couldn't find the site.xml file.");
			}
		} catch (Exception e){
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR,id,IStatus.OK,"Error during parsing of the site XML",e);
			throw new CoreException(status);
		} finally {
    		 isInitialized = true;
		}
	}
	
	/**
	 * Logs that an attempt to read a non initialize variable has been made
	 */
	private void logNotInitialized(){
		Exception trace = new Exception("Attempt to read uninitialized variable");
		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		IStatus status = new Status(IStatus.WARNING,id,IStatus.OK,"the program is reading a variable of Site before loading it",trace);
		UpdateManagerPlugin.getPlugin().getLog().log(status);
	}
	
	/**
	 * @see ISite#addSiteChangedListener(ISiteChangedListener)
	 */
	public void addSiteChangedListener(ISiteChangedListener listener) {
		synchronized (listeners){
			listeners.add(listener);
		}
	}
	
	/**
	 * @see ISite#removeSiteChangedListener(ISiteChangedListener)
	 */
	public void removeSiteChangedListener(ISiteChangedListener listener) {
		synchronized (listeners){
			listeners.remove(listener);
		}
	}	

	/**
	 * @see ISite#install(IFeature, IProgressMonitor)
	 */
	public void install(IFeature sourceFeature, IProgressMonitor monitor) throws CoreException {
		// should start Unit Of Work and manage Progress Monitor
		Feature localFeature = createExecutableFeature(sourceFeature);
		((Feature)sourceFeature).install(localFeature,monitor);
		this.addFeatureReference(new FeatureReference(this,localFeature.getURL()));
		
		// notify listeners
		Object[] siteListeners = listeners.getListeners();
		for (int i =0; i<siteListeners.length;i++){
			((ISiteChangedListener)siteListeners[i]).featureInstalled(localFeature);
		}
	}
	
	/**
	 * @see ISite#remove(IFeature, IProgressMonitor)
	 */
	public void remove(IFeature feature, IProgressMonitor monitor)
		throws CoreException {
			
		// notify listeners
		ISiteChangedListener[] siteListeners = (ISiteChangedListener[])listeners.getListeners();
		for (int i =0; i<siteListeners.length;i++){
			siteListeners[i].featureUninstalled(feature);
		}
	}	

	/**
	 * 
	 */
	public abstract Feature createExecutableFeature(IFeature sourceFeature)throws CoreException ;
	
	/**
	 * store Feature files/ Fetaures info into the Site
	 */
	protected abstract void storeFeatureInfo(VersionedIdentifier featureIdentifier,String contentKey,InputStream inStream) throws CoreException ;

	/**
	 * return the URL of the archive ID
	 */
	public abstract URL getURL  (String archiveID) throws CoreException;
	/**
	 * returns the default prefered feature for this site
	 */
	public abstract IFeature getDefaultFeature(URL featureURL);

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
	public boolean optimize(){
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
	public ResourceBundle getResourceBundle()  throws IOException, CoreException {
		ResourceBundle bundle = null;
		try {
			ClassLoader l = new URLClassLoader(new URL[] {this.getURL()}, null);
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
	 * Gets the features
	 * @return Returns a IFeatureReference[]
	 */
	public IFeatureReference[] getFeatureReferences() {
		IFeatureReference[] result = new IFeatureReference[0];
			if (!(features==null || features.isEmpty())){
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
		if (features==null){
			features = new ArrayList(0);
		}
		this.features.add(feature);
	}

	/**
	 * @see ISite#getArchives()
	 */
	public IInfo[] getArchives() {
		IInfo[] result = new IInfo[0];
			if (archives==null && !isInitialized) logNotInitialized();
			if (!(archives==null || archives.isEmpty())){
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
	public URL getArchiveURLfor(String archiveId){
		URL result = null;
		boolean found = false;		
		
		if (!(archiveId==null || archiveId.equals("") || archives==null || archives.isEmpty())){
			Iterator iter = archives.iterator();
			IInfo info;
			while (iter.hasNext() && !found){
				info = (IInfo)iter.next();
				if (archiveId.trim().equalsIgnoreCase(info.getText())){
					result = info.getURL();
					found = true;
				}
			}
		}
		
		//DEBUG:
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_INSTALL){
			String debugString = "Searching archive ID:"+archiveId+" in Site:"+getURL().toExternalForm()+"...";
			if (found) {
				debugString+="found , pointing to:"+result.toExternalForm();
			} else {
				debugString+="NOT FOUND";
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
		if (archives==null){
			archives = new ArrayList(0);
		}
		if (getArchiveURLfor(archive.getText())!=null){
			// DEBUG:		
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS){
				UpdateManagerPlugin.getPlugin().debug("The Archive with ID:"+archive.getText()+" already exist on the site.");		
			}
		}else {
			this.archives.add(archive);
		}
	}

	/**
	 * Sets the archives
	 * @param archives The archives to set
	 */
	public void setArchives(IInfo[] _archives) {
		if (_archives!=null){
			for (int i=0;i<_archives.length;i++){
				this.addArchive(_archives[i]);
			}		
		}
	}


	/**
	 * @see ISite#getInfoURL()
	 */
	public URL getInfoURL() {
		if (isManageable){
			if (infoURL==null && !isInitialized) logNotInitialized();
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
			if (categories==null && !isInitialized)	logNotInitialized();
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
		if (this.categories==null){
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
			if (categories == null)	logNotInitialized();
				Iterator iter = categories.iterator();
				ICategory currentCategory;
				while (iter.hasNext() && !found){
					currentCategory = (ICategory)iter.next();
					if (currentCategory.getName().equals(key)){
						result= currentCategory;
						found = true;
					}
				}
		}
		
		//DEBUG:
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS && !found){
			UpdateManagerPlugin.getPlugin().debug("Cannot find:"+key+" category in site:"+this.getURL().toExternalForm());
			if (!isManageable)UpdateManagerPlugin.getPlugin().debug("The Site is not manageable. Does not contain ste.xml");
			if (categories==null || categories.isEmpty())UpdateManagerPlugin.getPlugin().debug("The Site does not contain any categories.");
		}
		
		return result;
	}

}