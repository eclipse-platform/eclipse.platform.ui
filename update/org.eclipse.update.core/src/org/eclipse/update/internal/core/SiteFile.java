package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.update.core.*;

/**
 * Site on the File System
 */
public class SiteFile extends SiteURL {
	
	private String path;
	
	public static final String INSTALL_FEATURE_PATH = "install/features/";

	/**
	 * Constructor for FileSite
	 */
	public SiteFile(URL siteReference) throws CoreException {
		super(siteReference);
	}

	/**
	 * @see AbstractSite#createExecutableFeature(IFeature)
	 */
	public Feature createExecutableFeature(IFeature sourceFeature) throws CoreException {
		return new FeatureExecutable(sourceFeature, this);
	}

	/**
	 * @see IPluginContainer#store(IPluginEntry, String, InputStream)
	 */
	public void store(IPluginEntry pluginEntry, String contentKey, InputStream inStream) throws CoreException {

		String path = UpdateManagerUtils.getPath(getURL());

		// FIXME: fragment code
		String pluginPath = null;
		if (pluginEntry.isFragment()) {
			pluginPath = path + DEFAULT_FRAGMENT_PATH + pluginEntry.getIdentifier().toString();
		} else {
			pluginPath = path + DEFAULT_PLUGIN_PATH + pluginEntry.getIdentifier().toString();
		}
		pluginPath += pluginPath.endsWith(File.separator) ? contentKey : File.separator + contentKey;

		try {
			UpdateManagerUtils.copyToLocal(inStream, pluginPath, null);

		} catch (IOException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error creating file:" + pluginPath, e);
			throw new CoreException(status);
		} finally {
			try {
				// close stream
				inStream.close();
			} catch (Exception e) {}
		}
	}

	/**
	 * store Feature files
	 * Store the inputStream into a file named contentKey in the install feature path of the feature
	 */
	public void storeFeatureInfo(VersionedIdentifier featureIdentifier, String contentKey, InputStream inStream) throws CoreException {

		String path = UpdateManagerUtils.getPath(getURL());
		String featurePath = path + INSTALL_FEATURE_PATH + featureIdentifier.toString();
		featurePath += featurePath.endsWith(File.separator) ? contentKey : File.separator + contentKey;
		try {
			UpdateManagerUtils.copyToLocal(inStream, featurePath, null);
		} catch (IOException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error creating file:" + featurePath, e);
			throw new CoreException(status);
		} finally {
			try {
				// close stream
				inStream.close();
			} catch (Exception e) {}
		}

	}

	/*
	 * @see Site#getDefaultFeature(URL)
	 */
	public IFeature getDefaultFeature(URL featureURL) throws CoreException {
		return new FeatureExecutable(featureURL, this);
	}

	/**
	 * We do not need to optimize the download
	 * As the archives are already available on the file system
	 */
	public boolean optimize() {
		return false;
	}
	
	

	/**
	 * Method parseSite.
	 */
	protected void parseSite() throws CoreException {

		String path = UpdateManagerUtils.getPath(getURL());
		String pluginPath = path + DEFAULT_PLUGIN_PATH;
		String fragmentPath = path + DEFAULT_FRAGMENT_PATH;
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
		
		System.out.print("");

	}
	
	/**
	 * Method parseFeature.
	 * @throws CoreException
	 */
	private void parseExecutableFeature() throws CoreException {
		
		String path = UpdateManagerUtils.getPath(getURL());
		String featurePath = path + INSTALL_FEATURE_PATH;
		
		
		File featureDir = new File(featurePath);
		if (featureDir.exists()) {
			String[] dir;
			FeatureReference featureRef;
			URL featureURL;
			String newFilePath = null;
		
			try {
				// handle teh installed features under features subdirectory
				dir = featureDir.list();
				for (int index = 0; index < dir.length; index++) {
					// teh URL must ends with '/' for teh bundle to be resolved
					newFilePath = featurePath + dir[index] + "/";
					featureURL = new URL("file", null, newFilePath);
					featureRef = new FeatureReference(this, featureURL);
					addFeatureReference(featureRef);
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
		
		String path = UpdateManagerUtils.getPath(getURL());
		String featurePath = path + DEFAULT_FEATURE_PATH;
		
		// FEATURES
		File featureDir = new File(featurePath);
		if (featureDir.exists()) {
			String[] dir;
			FeatureReference featureRef;
			URL featureURL;
			String newFilePath = null;
		
			try {
				// handle teh installed features under features subdirectory
				dir = featureDir.list(FeaturePackaged.filter);
				for (int index = 0; index < dir.length; index++) {
					newFilePath = featurePath + dir[index];
					featureURL = new URL("file", null, newFilePath);
					featureRef = new FeatureReference(this, featureURL);
					addFeatureReference(featureRef);
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
	 * @return PluginRegistryModel
	 * @throws CoreException
	 */
	private PluginRegistryModel parsePlugins(String path) throws CoreException {
		
		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		
		PluginRegistryModel model;
		//FIXME: handle the archives
		// look into each plugin/fragment directory, crack the plugin.xml open (or fragment.xml ???)
		// get id and version, calculate URL...
		
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
				Info info;
				for (int index = 0; index < plugins.length; index++) {
					String pluginID = new VersionedIdentifier(plugins[index].getId(), plugins[index].getVersion()).toString() + FeaturePackaged.JAR_EXTENSION;
					location = plugins[index].getLocation();
					URL url = new URL(location);
					info = new Info(pluginID, url);
					this.addArchive(info);
				}
			}
		} catch (MalformedURLException e) {
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error creating file URL for plugin:" + location, e);
			throw new CoreException(status);
		}
	}

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
			dir = pluginDir.list(FeaturePackaged.filter);
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
							// the id is plugins\<pluginid>_<ver> as per the specs
							String pluginID = Site.DEFAULT_PLUGIN_PATH+new VersionedIdentifier(models[index].getId(), models[index].getVersion()).toString() + FeaturePackaged.JAR_EXTENSION;
							URL url = new URL("file",null,file.getAbsolutePath());
							IInfo info = new Info(pluginID, url);
							this.addArchive(info);
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
	
}