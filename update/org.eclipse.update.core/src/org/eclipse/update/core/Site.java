package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.URL;
import java.util.*;

import org.eclipse.core.internal.boot.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.core.Writer;

public class Site extends SiteMapModel implements ISite, IWritable {



	/**
	 * plugin entries 
	 */
	private List pluginEntries = new ArrayList(0);

	/**
	 * default path under the site where features will be installed
	 */
	public static final String INSTALL_FEATURE_PATH = "install/features/"; //$NON-NLS-1$

	/**
	 * default path under the site where plugins will be installed
	 */
	public static final String DEFAULT_PLUGIN_PATH = "plugins/"; //$NON-NLS-1$
	/**
	 * default path under the site where plugins will be installed
	 */
	//FIXME: fragment
	public static final String DEFAULT_FRAGMENT_PATH = "fragments/"; //$NON-NLS-1$

	/**
	 * default path, under site, where featuresConfigured will be installed
	 */
	public static final String DEFAULT_FEATURE_PATH = "features/"; //$NON-NLS-1$

	public static final String SITE_FILE = "site"; //$NON-NLS-1$
	public static final String SITE_XML = SITE_FILE + ".xml"; //$NON-NLS-1$

	private ListenersList listeners = new ListenersList();
	/**
	 * The content consumer of the Site
	 */
	private ISiteContentConsumer contentConsumer;

	/**
	 * The content provider of the Site
	 */
	private ISiteContentProvider siteContentProvider;

	
	/**
	 * Constructor for Site
	 */
	public Site() {
		super();
	}


	public boolean equals(Object obj) {
		if (!(obj instanceof ISite))
			return false;
		if (getURL()==null)
			return false;
		ISite otherSite = (ISite)obj;
		
		return getURL().equals(otherSite.getURL());
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
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("Site.CannotSaveSiteInto", file.getAbsolutePath()), e); //$NON-NLS-1$
			throw new CoreException(status);
		}
	}

	/*
	 * @see ISite#addSiteChangedListener(ISiteChangedListener)
	 */
	public void addSiteChangedListener(ISiteChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/*
	 * @see ISite#removeSiteChangedListener(ISiteChangedListener)
	 */
	public void removeSiteChangedListener(ISiteChangedListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/*
	 * @see ISite#install(IFeature, IProgressMonitor)
	 */
	public IFeatureReference install(IFeature sourceFeature, IProgressMonitor progress) throws CoreException {

		// make sure we have an InstallMonitor		
		InstallMonitor monitor;
		if (progress == null)
			monitor = null;
		else if (progress instanceof InstallMonitor)
			monitor = (InstallMonitor) progress;
		else
			monitor = new InstallMonitor(progress);

		// create new executable feature and install source content into it
		IFeature localFeature = createExecutableFeature(sourceFeature);
		IFeatureReference localFeatureReference = sourceFeature.install(localFeature, monitor);
		if (localFeature instanceof FeatureModel) ((FeatureModel)localFeature).markReadOnly();
		this.addFeatureReference(localFeatureReference);
		

		// notify listeners
		Object[] siteListeners = listeners.getListeners();
		for (int i = 0; i < siteListeners.length; i++) {
			((ISiteChangedListener) siteListeners[i]).featureInstalled(localFeature);
		}
		return localFeatureReference;
	}

	
	/*
	 * @see ISite#remove(IFeature, IProgressMonitor)
	 */
	public void remove(IFeature feature, IProgressMonitor progress) throws CoreException {

		// make sure we have an InstallMonitor		
		InstallMonitor monitor;
		if (progress == null)
			monitor = null;
		else if (progress instanceof InstallMonitor)
			monitor = (InstallMonitor) progress;
		else
			monitor = new InstallMonitor(progress);

		// remove the feature and the plugins if they are not used and not activated
		// get the plugins from the feature
		IPluginEntry[] pluginsToRemove = SiteManager.getLocalSite().getUnusedPluginEntries(feature);

		//finds the contentReferences for this IPluginEntry
		for (int i = 0; i < pluginsToRemove.length; i++) {
			remove(feature, pluginsToRemove[i], monitor);
		}

		// remove the feature content
		ContentReference[] references = feature.getFeatureContentProvider().getFeatureEntryArchiveReferences(monitor);
		for (int i = 0; i < references.length; i++) {
			try {
				UpdateManagerUtils.removeFromFileSystem(references[i].asFile());
			} catch (IOException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("Site.CannotRemoveFeature", feature.getVersionIdentifier().getIdentifier(),getURL().toExternalForm()), e); //$NON-NLS-1$
				throw new CoreException(status);
			}
		}

		// remove feature reference from the site
		IFeatureReference[] featureReferences = getFeatureReferences();
		if (featureReferences != null) {
			for (int indexRef = 0; indexRef < featureReferences.length; indexRef++) {
				IFeatureReference element = featureReferences[indexRef];
				if (element.equals(feature)) {
					removeFeatureReferenceModel((FeatureReferenceModel) element);
					break;
				}
			}
		}

		// notify listeners

		Object[] siteListeners = listeners.getListeners();
		for (int i = 0; i < siteListeners.length; i++) {
			((ISiteChangedListener) siteListeners[i]).featureUninstalled(feature);
		}

	}


	
	

	/*
	 * @see ISite#getURL()
	 */
	public URL getURL() {
		URL url = null;
		try {
			url = getSiteContentProvider().getURL();
		} catch (CoreException e) {
			UpdateManagerPlugin.getPlugin().getLog().log(e.getStatus());
		}
		return url;
	}

	/*
	 * @see ISite#getFeatureReferences()
	 */
	public IFeatureReference[] getFeatureReferences() {
		FeatureReferenceModel[] result = getFeatureReferenceModels();
		if (result.length == 0)
			return new IFeatureReference[0];
		else
			return (IFeatureReference[]) result;
	}

	/*
	 * @see ISite#getArchives()
	 */
	public IArchiveReference[] getArchives() {
		ArchiveReferenceModel[] result = getArchiveReferenceModels();
		if (result.length == 0)
			return new IArchiveReference[0];
		else
			return (IArchiveReference[]) result;
	}

	/*
	 * @see ISite#getInfoURL()
	 */
	public URL getInfoURL() {
		URLEntryModel description = getDescriptionModel();
		if (description == null)
			return null;
		return description.getURL();
	}

	/*
	 * @see ISite#getCategories()
	 */
	public ICategory[] getCategories() {
		SiteCategoryModel[] result = getCategoryModels();
		if (result.length == 0)
			return new ICategory[0];
		else
			return (ICategory[]) result;
	}

	/*
	 * @see ISite#getCategory(String)
	 */
	public ICategory getCategory(String key) {
		ICategory result = null;
		boolean found = false;
		int length = getCategoryModels().length;

		for (int i = 0; i < length; i++) {
			if (getCategoryModels()[i].getName().equals(key)) {
				result = (ICategory) getCategoryModels()[i];
				found = true;
				break;
			}
		}

		//DEBUG:
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS && !found) {
			UpdateManagerPlugin.getPlugin().debug(Policy.bind("Site.CannotFindCategory", key , this.getURL().toExternalForm())); //$NON-NLS-1$ //$NON-NLS-2$
			if (getCategoryModels().length <= 0)
				UpdateManagerPlugin.getPlugin().debug(Policy.bind("Site.NoCategories")); //$NON-NLS-1$
		}

		return result;
	}

	/**
	 * @see IPluginContainer#getPluginEntries()
	 */
	public IPluginEntry[] getPluginEntries() {
		IPluginEntry[] result = new IPluginEntry[0];
		if (!(pluginEntries == null || pluginEntries.isEmpty())) {
			result = new IPluginEntry[pluginEntries.size()];
			pluginEntries.toArray(result);
		}
		return result;
	}

	/**
	 * Adds a plugin entry 
	 * Either from parsing the file system or 
	 * installing a feature
	 * 
	 * We cannot figure out the list of plugins by reading the Site.xml as
	 * the archives tag are optionals
	 */
	public void addPluginEntry(IPluginEntry pluginEntry) {
		pluginEntries.add(pluginEntry);
	}


	/*
	 * @see ISite#getContentConsumer(IFeature)
	 */
	public ISiteContentConsumer createSiteContentConsumer(IFeature feature) throws CoreException {
		return null;
	}

	/*
	 * @see ISite#setSiteContentProvider(ISiteContentProvider)
	 */
	public void setSiteContentProvider(ISiteContentProvider siteContentProvider) {
		this.siteContentProvider = siteContentProvider;
	}

	/*
	 * @see ISite#getSiteContentProvider()
	 */
	public ISiteContentProvider getSiteContentProvider() throws CoreException {
		if (siteContentProvider == null) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("Site.NoContentProvider"), null); //$NON-NLS-1$
			throw new CoreException(status);
		}
		return siteContentProvider;
	}

	/*
	 * @see IWritable#write(int, PrintWriter)
	 */
	public void write(int indent, PrintWriter w) {

		String gap = ""; //$NON-NLS-1$
		for (int i = 0; i < indent; i++)
			gap += " "; //$NON-NLS-1$
		String increment = ""; //$NON-NLS-1$
		for (int i = 0; i < IWritable.INDENT; i++)
			increment += " "; //$NON-NLS-1$

		w.print(gap + "<site "); //$NON-NLS-1$
		// site type 
		if (getType() != null) {
			w.print("type=\"" + Writer.xmlSafe(getType()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			w.print(" "); //$NON-NLS-1$
		}		
		
		// Site URL
		String URLInfoString = null;
		if (getInfoURL() != null) {
			URLInfoString = UpdateManagerUtils.getURLAsString(this.getURL(), getInfoURL());
			w.print("url=\"" + Writer.xmlSafe(URLInfoString) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		w.println(">"); //$NON-NLS-1$
		w.println(""); //$NON-NLS-1$

		IFeatureReference[] refs = getFeatureReferences();
		for (int index = 0; index < refs.length; index++) {
			FeatureReference element = (FeatureReference) refs[index];
			element.write(indent, w);
		}
		w.println(""); //$NON-NLS-1$

		IArchiveReference[] archives = getArchives();
		for (int index = 0; index < archives.length; index++) {
			IArchiveReference element = (IArchiveReference) archives[index];
			URLInfoString = UpdateManagerUtils.getURLAsString(this.getURL(), element.getURL());
			w.println(gap + "<archive " + "path = \"" + Writer.xmlSafe(element.getPath()) + "\" url=\"" + Writer.xmlSafe(URLInfoString) + "\"/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		w.println(""); //$NON-NLS-1$

		ICategory[] categories = getCategories();
		for (int index = 0; index < categories.length; index++) {
			Category element = (Category) categories[index];
			w.println(gap + "<category-def " + "label = \"" + Writer.xmlSafe(element.getLabel()) + "\" name=\"" + Writer.xmlSafe(element.getName()) + "\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

			IURLEntry info = element.getDescription();
			if (info != null) {
				w.print(gap + increment + "<description "); //$NON-NLS-1$
				URLInfoString = null;
				if (info.getURL() != null) {
					URLInfoString = UpdateManagerUtils.getURLAsString(this.getURL(), info.getURL());
					w.print("url=\"" + Writer.xmlSafe(URLInfoString) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
				}
				w.println(">"); //$NON-NLS-1$
				if (info.getAnnotation() != null) {
					w.println(gap + increment + increment + Writer.xmlSafe(info.getAnnotation()));
				}
				w.print(gap + increment + "</description>"); //$NON-NLS-1$
			}
			w.println(gap + "</category-def>"); //$NON-NLS-1$

		}
		w.println(""); //$NON-NLS-1$
		// end
		w.println("</site>"); //$NON-NLS-1$

	}

	/*
	 * @see IPluginContainer#getPluginEntryCount()
	 */
	public int getPluginEntryCount() {
		return getPluginEntries().length;
	}
	/**
	 * returns the download size
	 * of the feature to be installed on the site.
	 * If the site is <code>null</code> returns the maximum size
	 * 
	 * If one plug-in entry has an unknown size.
	 * then the download size is unknown.
	 * 
	 * @see ISite#getDownloadSize(IFeature)
	 * 
	 */
	public long getDownloadSize(IFeature feature) {
		long result = 0;
		IPluginEntry[] entriesToInstall = feature.getPluginEntries();
		IPluginEntry[] siteEntries = this.getPluginEntries();
		entriesToInstall = UpdateManagerUtils.intersection(entriesToInstall, siteEntries);

		if (entriesToInstall == null || entriesToInstall.length == 0) {
			result = -1;
		} else {
			long pluginSize = 0;
			int i = 0;
			while (i < entriesToInstall.length && pluginSize != -1) {
				pluginSize = ((PluginEntry)entriesToInstall[i]).getDownloadSize();
				result = pluginSize == -1 ? -1 : result + pluginSize;
				i++;
			}
		}
		return result;
	}

	/**
	 * returns the download size
	 * of the feature to be installed on the site.
	 * If the site is <code>null</code> returns the maximum size
	 * 
	 * If one plug-in entry has an unknown size.
	 * then the download size is unknown.
	 * 
	 * @see ISite#getDownloadSize(IFeature)
	 * 
	 */
	public long getInstallSize(IFeature feature) {
		long result = 0;
		IPluginEntry[] entriesToInstall = feature.getPluginEntries();
		IPluginEntry[] siteEntries = this.getPluginEntries();
		entriesToInstall = UpdateManagerUtils.intersection(entriesToInstall, siteEntries);

		if (entriesToInstall == null || entriesToInstall.length == 0) {
			result = -1;
		} else {
			long pluginSize = 0;
			int i = 0;
			while (i < entriesToInstall.length && pluginSize != -1) {
				pluginSize = ((PluginEntry)entriesToInstall[i]).getInstallSize();
				result = pluginSize == -1 ? -1 : result + pluginSize;
				i++;
			}
		}
		return result;
	}



	/*
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	/*
	 * @see ISite#getDefaultExecutableFeatureType()
	 */
	public String getDefaultExecutableFeatureType() {
		return null;
	}

	/*
	 * @see ISite#getDefaultInstallableFeatureType()
	 */
	public String getDefaultInstallableFeatureType() {
		String pluginID = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier() + "."; //$NON-NLS-1$
		return pluginID + IFeatureFactory.INSTALLABLE_FEATURE_TYPE;
	}

	/*
	 * @see ISite#getFeatureReference(IFeature)
	 */
	public IFeatureReference getFeatureReference(IFeature feature) {
		IFeatureReference result = null;
		IFeatureReference[] references = getFeatureReferences();
		boolean found = false;
		for (int i = 0; i < references.length &&!found; i++) {
			if (references[i].getURL().equals(feature.getURL())){
				result = references[i];
				found = true;
			}
		}
		return result;
	}

	/*
	 * @see ISite#getDescription()
	 */
	public IURLEntry getDescription() {
		return (IURLEntry)getDescriptionModel();
	}

	/**
	 * adds a feature reference
	 * @param feature The feature reference to add
	 */
	private void addFeatureReference(IFeatureReference feature) {
		addFeatureReferenceModel((FeatureReferenceModel) feature);
	}

	/**
	 * 
	 */
	private void remove(IFeature feature, IPluginEntry pluginEntry, InstallMonitor monitor) throws CoreException {

		if (pluginEntry == null)
			return;

		ContentReference[] references = feature.getFeatureContentProvider().getPluginEntryArchiveReferences(pluginEntry, monitor);
		for (int i = 0; i < references.length; i++) {
			try {
				UpdateManagerUtils.removeFromFileSystem(references[i].asFile());
			} catch (IOException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("Site.CannotRemovePlugin",pluginEntry.getVersionIdentifier().toString(),getURL().toExternalForm()), e); //$NON-NLS-1$
				throw new CoreException(status);
			}
		}
	}


	/**
	 * 
	 */
	private IFeature createExecutableFeature(IFeature sourceFeature) throws CoreException {
		String executableFeatureType = getDefaultExecutableFeatureType();
		IFeature result = null;
		if (executableFeatureType != null) {
			IFeatureFactory factory = FeatureTypeFactory.getInstance().getFactory(executableFeatureType);
			result = factory.createFeature(/*URL*/null,this);

			// at least set the version identifier to be the same
			 ((FeatureModel) result).setFeatureIdentifier(sourceFeature.getVersionIdentifier().getIdentifier());
			((FeatureModel) result).setFeatureVersion(sourceFeature.getVersionIdentifier().getVersion().toString());
		}
		return result;
	}


	

}