package org.eclipse.update.internal.core;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.core.*;
import org.xml.sax.SAXException;

public abstract class AbstractSite implements ISite {

	/**
	 * default path under the site where plugins will be installed
	 */
	public static final String DEFAULT_PLUGIN_PATH = "plugins/";

	/**
	 * default path, under site, where features will be installed
	 */
	public static final String DEFAULT_FEATURE_PATH = "features/";

	private static final String SITE_XML= "site.xml";
	private boolean isManageable = true;
	private DefaultSiteParser parser;
	
	/**
	 * the tool will create the directories on the file 
	 * system if needed.
	 */
	public static boolean CREATE_PATH = true;

	private ListenersList listeners = new ListenersList();
	private URL siteURL;
	private URL infoURL;
	private List features;
	private List categories;
	private List archives;

	/**
	 * Constructor for AbstractSite
	 */
	public AbstractSite(URL siteReference) {
		super();
		this.siteURL = siteReference;
		// initializeSite();
		// FIXME: should I initialize now or lazyly do it ?
		// Should I get only the name of teh site first ?
	}
	
	/**
	 * Initializes the site by reading the site.xml file
	 */
	private void initializeSite(){
		InputStream inStream = null;
		isManageable = false;		
		
		try {
			URL siteXml = new URL(siteURL,SITE_XML);
			parser = new DefaultSiteParser(siteXml.openStream(),this);
			isManageable = true;		 	
		} catch (org.xml.sax.SAXException e){
			//FIXME: who should handle XML exception  the parser ? 
			// ok but what should it do ? just put null in result ?
			// send and transform teh exception ?
			e.printStackTrace();
		} catch (FileNotFoundException e){
			// log not manageable site
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS){
				System.out.println(siteURL.toExternalForm()+" is not manageable by Update Manager: Couldn't find the site.xml file.");
			}
		} catch (MalformedURLException e){
			// FIXME:
			e.printStackTrace();
		} catch (IOException e){
			// FIXME:
			e.printStackTrace();
		} finally {
			try {
			 inStream.close();
			} catch (Exception e){}
		}
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
		// should start UOW and manage Progress Monitor
		AbstractFeature localFeature = createExecutableFeature(sourceFeature);
		sourceFeature.install(localFeature);
		this.addFeature(localFeature);
		
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
	public abstract AbstractFeature createExecutableFeature(IFeature sourceFeature);

	/**
	 * store Feature files/ Fetaures info into the Site
	 */
	protected abstract void storeFeatureInfo(VersionedIdentifier featureIdentifier,String contentKey,InputStream inStream);

	/**
	 *
	 */
	public abstract URL getURL(
		IFeature sourceFeature,
		String streamKey);

	/**
	 * Gets the siteURL
	 * @return Returns a URL
	 */
	public URL getURL() {
		return siteURL;
	}

	/**
	 * Gets the features
	 * @return Returns a IFeature[]
	 */
	public IFeature[] getFeatures() {
		IFeature[] result = null;
		if (isManageable){
			if (features==null) initializeSite();
			if (!(features==null || features.isEmpty())){
				result = new IFeature[features.size()];
				features.toArray(result);
			}
		}
		return result;
	}
	
	/**
	 * adds a feature
	 * The feature is considered already installed. It does not install it.
	 * @param feature The feature to add
	 */
	public void addFeature(IFeature feature) {
		if (features==null){
			features = new ArrayList(0);
		}
		this.features.add(feature);
	}

	/**
	 * @see ISite#getArchives()
	 */
	public IInfo[] getArchives(){
		IInfo[] result = null;
		if (isManageable){
			if (archives==null) initializeSite();
			if (!(archives==null || archives.isEmpty())){
				result = new IInfo[archives.size()];
				archives.toArray(result);
			}
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
		if (!(archiveId==null || archiveId.equals("") || archives==null || archives.isEmpty())){
			Iterator iter = archives.iterator();
			IInfo info;
			boolean found = false;
			while (iter.hasNext() && !found){
				info = (IInfo)iter.next();
				if (archiveId.trim().equalsIgnoreCase(info.getText())){
					result = info.getURL();
					found = true;
				}
			}
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
		if (getArchiveURLfor(archive.getText())!=null)
			Assert.isTrue(false,"The Archive with ID:"+archive.getText()+"already exist on the site.");
		else
			this.archives.add(archive);
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
			if (infoURL==null) initializeSite();
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
		ICategory[] result = null;
		if (isManageable) {
			if (categories == null)	initializeSite();
			//FIXME: I do not like this pattern.. List or Array ???
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
		if (categories==null){
			categories = new ArrayList(0);
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
			if (categories == null)	initializeSite();
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