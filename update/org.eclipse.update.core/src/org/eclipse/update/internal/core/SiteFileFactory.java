package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.xml.sax.SAXException;

public class SiteFileFactory extends BaseSiteFactory {


	// private when parsing file system
	private Site site;
	private URL url;

	/*
	 * @see ISiteFactory#createSite(URL)
	 */
	public ISite createSite(URL url) throws CoreException, InvalidSiteTypeException {

		Site site = null;
		URL siteXML = null;		
		InputStream siteStream = null;
		
		try {		
			SiteFileContentProvider contentProvider = new SiteFileContentProvider(url);
					
			try {
				siteXML = new URL(contentProvider.getURL(),Site.SITE_XML);
				siteStream = siteXML.openStream();
				SiteModelFactory factory = (SiteModelFactory) this;
				site = (Site)factory.parseSite(siteStream);	
			} catch (IOException e) {
				site = parseSite(url);
			}
			
			site.setSiteContentProvider(contentProvider);
			contentProvider.setSite(site);
			site.resolve(url, getResourceBundle(url));
			site.markReadOnly();			
			
		} catch (IOException e) {

			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, "WARNING: cannot open site.xml in the site:" + url.toExternalForm(), e);
			throw new CoreException(status);
		} catch (Exception e) {
			
			if (e instanceof SAXException){
				SAXException exception = (SAXException) e;
				if(exception.getException() instanceof InvalidSiteTypeException){
					throw (InvalidSiteTypeException)exception.getException();
				}
			}
			
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, "Error parsing site.xml in the site:" + url.toExternalForm(), e);
			throw new CoreException(status);
		} finally {
			try {
				siteStream.close();
			} catch (Exception e) {
			}
		}
		return site;
	}
	
	/**
	 * Method parseSite.
	 */
	public Site parseSite(URL url) throws CoreException {

		this.url = url;
		this.site = (Site)createSiteMapModel();	
				
		String path = UpdateManagerUtils.getPath(this.url);
		String pluginPath = path + Site.DEFAULT_PLUGIN_PATH;
		String fragmentPath = path + Site.DEFAULT_FRAGMENT_PATH;
		PluginRegistryModel model = new PluginRegistryModel();	

		//PACKAGED
		parsePackagedFeature(); // in case it contains JAR files

		parsePackagedPlugins(pluginPath);
		
		parsePackagedPlugins(fragmentPath);		

		// EXECUTABLE	
		parseExecutableFeature();
		
		model = parsePlugins(pluginPath);
		addParsedPlugins(model.getPlugins());

		// FIXME: fragments
		model = parsePlugins(fragmentPath);
		addParsedPlugins(model.getFragments());
		
		return (Site)site;

	}
	
	/**
	 * Method parseFeature.
	 * @throws CoreException
	 */
	private void parseExecutableFeature() throws CoreException {
		
		String path = UpdateManagerUtils.getPath(this.url);
		String featurePath = path + Site.INSTALL_FEATURE_PATH;
		
		
		File featureDir = new File(featurePath);
		if (featureDir.exists()) {
			String[] dir;
			FeatureReferenceModel featureRef;
			URL featureURL;
			String newFilePath = null;
		
			try {
				// handle teh installed featuresConfigured under featuresConfigured subdirectory
				dir = featureDir.list();
				for (int index = 0; index < dir.length; index++) {

				SiteFileFactory archiveFactory = new SiteFileFactory();							
					// the URL must ends with '/' for the bundle to be resolved
					newFilePath = featurePath + dir[index] + "/";
					featureURL = new URL("file", null, newFilePath);						
					IFeature newFeature = createFeature(featureURL);
					
					featureRef = archiveFactory.createFeatureReferenceModel();
					featureRef.setSiteModel(site);
					featureRef.setURLString(featureURL.toExternalForm());
					((Site)site).addFeatureReferenceModel(featureRef);										
				}
			} catch (MalformedURLException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error creating file URL for:" + newFilePath, e);
				throw new CoreException(status);
			}
		}
	}
	
		/**
	 * Method parseFeature.
	 * @throws CoreException
	 */
	private void parsePackagedFeature() throws CoreException {
		
		String path = UpdateManagerUtils.getPath(this.url);
		String featurePath = path + Site.DEFAULT_FEATURE_PATH;
		
		// FEATURES
		File featureDir = new File(featurePath);
		if (featureDir.exists()) {
			String[] dir;
			FeatureReferenceModel featureRef;
			URL featureURL;
			String newFilePath = null;
		
			try {
				// handle teh installed featuresConfigured under featuresConfigured subdirectory
				dir = featureDir.list(FeaturePackagedContentProvider.filter);
				for (int index = 0; index < dir.length; index++) {
					
					SiteFileFactory archiveFactory = new SiteFileFactory();							
					newFilePath = featurePath + dir[index];
					featureURL = new URL("file", null, newFilePath);						
					IFeature newFeature = createFeature(featureURL);
					
					featureRef = archiveFactory.createFeatureReferenceModel();
					featureRef.setSiteModel(site);
					featureRef.setURLString(featureURL.toExternalForm());
					((Site)site).addFeatureReferenceModel(featureRef);					
		
				}
			} catch (MalformedURLException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error creating file URL for:" + newFilePath, e);
				throw new CoreException(status);
			}
		}
	}
	
	/**
	 * Method parsePlugins.
	 * 
	 * look into each plugin/fragment directory, crack the plugin.xml open (or fragment.xml ???)
	 * get id and version, calculate URL...	
	 * 
	 * @return PluginRegistryModel
	 * @throws CoreException
	 */
	private PluginRegistryModel parsePlugins(String path) throws CoreException {
		PluginRegistryModel model;
		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();		
		MultiStatus parsingStatus = new MultiStatus(id, IStatus.WARNING, "Error parsing plugin.xml in " + path, new Exception());
		Factory factory = new Factory(parsingStatus);
		
		try {
			URL pluginURL = new URL("file", null, path);
			model = Platform.parsePlugins(new URL[] { pluginURL }, factory);
		} catch (MalformedURLException e) {
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error creating file URL for :" + path, e);
			throw new CoreException(status);
		}
				
		if (factory.getStatus().getChildren().length != 0) {
			throw new CoreException(parsingStatus);
		}
		
		return model;
	}

	/**
	 * Method addParsedPlugins.
	 * @param model
	 * @throws CoreException
	 */
	private void addParsedPlugins(PluginModel[] plugins) throws CoreException {
		
		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		
		// tranform each Plugin and Fragment in an Archive fro the Site
		String location = null;
		try {
			if (plugins.length > 0) {
				URLEntry info;
				for (int index = 0; index < plugins.length; index++) {
					SiteFileFactory archiveFactory = new SiteFileFactory();							
					// the id is plugins\<pluginid>_<ver>.jar as per the specs
					String pluginID = Site.DEFAULT_PLUGIN_PATH+new VersionedIdentifier(plugins[index].getId(), plugins[index].getVersion()).toString() + FeaturePackagedContentProvider.JAR_EXTENSION;
					ArchiveReferenceModel archive = archiveFactory.createArchiveReferenceModel();		
					archive.setPath(pluginID);
					location = plugins[index].getLocation();
					URL url = new URL(location);
					archive.setURLString(url.toExternalForm());
					((Site)site).addArchiveReferenceModel(archive);					
				}
			}
		} catch (MalformedURLException e) {
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error creating file URL for plugin:" + location, e);
			throw new CoreException(status);
		}
	}

	/**
	 * 
	 */
	private void parsePackagedPlugins(String pluginPath) throws CoreException { 
			
		File pluginDir = new File(pluginPath);
		File file = null;
		ZipFile zipFile = null;
		ZipEntry entry = null;
		String[] dir;	
		URL pluginURL=null;
		
		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		PluginRegistryModel registryModel;
		MultiStatus parsingStatus = new MultiStatus(id, IStatus.WARNING, "Error parsing plugin.xml", new Exception());
		Factory factory = new Factory(parsingStatus);
		
		String tempDir = System.getProperty("java.io.tmpdir");
		if (!tempDir.endsWith(File.separator)) tempDir += File.separator;
					
		try {
		if (pluginDir.exists()) {
			dir = pluginDir.list(FeaturePackagedContentProvider.filter);
			for (int i = 0; i < dir.length; i++) {
				file = new File(pluginPath,dir[i]);
				zipFile = new ZipFile(file);
				entry = zipFile.getEntry("plugin.xml");
				if (entry==null) entry = zipFile.getEntry("fragment.xml"); //FIXME: fragments
				if (entry!=null){
					pluginURL=UpdateManagerUtils.copyToLocal(zipFile.getInputStream(entry),tempDir+entry.getName(),null);
					registryModel = Platform.parsePlugins(new URL[] { pluginURL }, factory);					
					if (registryModel!=null) {
						PluginModel[] models = null;
						if (entry.getName().equals("plugin.xml")){
							models = registryModel.getPlugins();
						} else {
							models = registryModel.getFragments();
						}
						for (int index = 0; index < models.length; index++) {
							SiteFileFactory archiveFactory = new SiteFileFactory();							
							// the id is plugins\<pluginid>_<ver>.jar as per the specs
							String pluginID = Site.DEFAULT_PLUGIN_PATH+new VersionedIdentifier(models[index].getId(), models[index].getVersion()).toString() + FeaturePackagedContentProvider.JAR_EXTENSION;
							ArchiveReferenceModel archive = archiveFactory.createArchiveReferenceModel();		
							archive.setPath(pluginID);
							archive.setURLString(file.toURL().toExternalForm());
							((Site)site).addArchiveReferenceModel(archive);
						}
					}
				}
				zipFile.close();		
			}	
		}
		}
		//catch (MalformedURLException m){throw new CoreException(new Status(IStatus.ERROR, id, IStatus.OK, "Error accessing plugin.xml in file :" + file, m));}		
		//catch (ZipException z){throw new CoreException(new Status(IStatus.ERROR, id, IStatus.OK, "Error accessing plugin.xml in file :" + file, z));}		
		catch (IOException e){ throw new CoreException(new Status(IStatus.ERROR, id, IStatus.OK, "Error accessing plugin.xml in file :" + file, e));}
		 finally {try {zipFile.close();} catch (Exception e) {}}
		 
	}

	/**
	 * 
	 */
	private IFeature createFeature(URL url) throws CoreException {
		FeatureReference ref = new FeatureReference();
		ref.setSite(site);
		ref.setURL(url);
		return ref.getFeature();
	}

		

	/*
	 * @see SiteModelFactory#createSiteMapModel()
	 */
	public SiteMapModel createSiteMapModel() {
		return new SiteFile();
	}


	/*
	 * @see SiteModelFactory#canParseSiteType(String)
	 */
	public boolean canParseSiteType(String type) {
		return (super.canParseSiteType(type) || SiteFileContentProvider.SITE_TYPE.equalsIgnoreCase(type));
	}

}
