package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.xml.sax.SAXException;

public class SiteFileFactory extends BaseSiteFactory {

	// private when parsing file system
	private Site site;
	private URL url;

	/**
	 * manages the versionedIdentifier and location of parsed plugins
	 */
	public class PluginIdentifier {
		private VersionedIdentifier id;
		private File location;
		public PluginIdentifier(VersionedIdentifier id, File location) {
			this.id = id;
			this.location = location;
		}

		public String getIdentifier() {
			if (id != null)
				return id.getIdentifier();
			return null;
		}

		public Version getVersion() {
			if (id != null)
				return id.getVersion();
			return null;
		}

		public File getLocation() {
			return location;
		}
		
		public String toString(){
			if (id!=null) return id.toString();
			return "";
		}
	}

	/*
	 * @see ISiteFactory#createSite(URL,boolean)
	 */
	public ISite createSite(URL url, boolean forceCreation) throws CoreException, InvalidSiteTypeException {

		Site site = null;
		URL siteXML = null;
		InputStream siteStream = null;

		try {
			// remove site.xml from the URL
			url = removeSiteXML(url);

			SiteFileContentProvider contentProvider = new SiteFileContentProvider(url);

			try {
				siteXML = new URL(contentProvider.getURL(), Site.SITE_XML);
				siteStream = siteXML.openStream();
				SiteModelFactory factory = (SiteModelFactory) this;
				site = (Site) factory.parseSite(siteStream);
			} catch (IOException e) {
				if (forceCreation)
					site = parseSite(url);
				else
					throw new InvalidSiteTypeException(null);
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

			if (e instanceof SAXException) {
				SAXException exception = (SAXException) e;
				if (exception.getException() instanceof InvalidSiteTypeException) {
					throw (InvalidSiteTypeException) exception.getException();
				}
			}

			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, "Error parsing site.xml in the site:" + url.toExternalForm(), e);
			throw new CoreException(status);
		} finally {
			try {
				if (siteStream!=null) siteStream.close();
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
		this.site = (Site) createSiteMapModel();

		String path = UpdateManagerUtils.decode(this.url);
		String pluginPath = path + Site.DEFAULT_PLUGIN_PATH;
		String fragmentPath = path + Site.DEFAULT_FRAGMENT_PATH;

		// FIXME: fragments
		//PACKAGED
		parsePackagedFeature(); // in case it contains JAR files

		parsePackagedPlugins(pluginPath);

		parsePackagedPlugins(fragmentPath);

		// EXECUTABLE	
		parseExecutableFeature();

		parseExecutablePlugin(pluginPath);

		parseExecutablePlugin(fragmentPath);

		return (Site) site;

	}

	/**
	 * Method parseFeature.
	 * @throws CoreException
	 */
	private void parseExecutableFeature() throws CoreException {

		String path = UpdateManagerUtils.decode(this.url);
		String featurePath = path + Site.INSTALL_FEATURE_PATH;

		File featureDir = new File(featurePath);
		if (featureDir.exists()) {
			String[] dir;
			FeatureReferenceModel featureRef;
			URL featureURL;
			String newFilePath = null;

			try {
				// handle the installed featuresConfigured under featuresConfigured subdirectory
				dir = featureDir.list();
				for (int index = 0; index < dir.length; index++) {

					SiteFileFactory archiveFactory = new SiteFileFactory();
					// the URL must ends with '/' for the bundle to be resolved
					newFilePath = featurePath + dir[index] + (dir[index].endsWith("/") ? "/" : "");
					featureURL = new File(newFilePath).toURL();
					IFeature newFeature = createFeature(featureURL);

					featureRef = archiveFactory.createFeatureReferenceModel();
					featureRef.setSiteModel(site);
					featureRef.setURLString(featureURL.toExternalForm());
					((Site) site).addFeatureReferenceModel(featureRef);
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

		String path = UpdateManagerUtils.decode(this.url);
		String featurePath = path + Site.DEFAULT_FEATURE_PATH;

		// FEATURES
		File featureDir = new File(featurePath);
		if (featureDir.exists()) {
			String[] dir;
			FeatureReferenceModel featureRef;
			URL featureURL;
			String newFilePath = null;

			try {
				// handle the installed featuresConfigured under featuresConfigured subdirectory
				dir = featureDir.list(FeaturePackagedContentProvider.filter);
				for (int index = 0; index < dir.length; index++) {

					SiteFileFactory archiveFactory = new SiteFileFactory();
					newFilePath = featurePath + dir[index];
					featureURL = new File(newFilePath).toURL();
					IFeature newFeature = createFeature(featureURL);

					featureRef = archiveFactory.createFeatureReferenceModel();
					featureRef.setSiteModel(site);
					featureRef.setURLString(featureURL.toExternalForm());
					((Site) site).addFeatureReferenceModel(featureRef);

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
	 * @return VersionedIdentifier
	 * @throws CoreException
	 */
	private void parseExecutablePlugin(String path) throws CoreException {
		PluginIdentifier plugin = null;
		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		MultiStatus parsingStatus = new MultiStatus(id, IStatus.WARNING, "Error parsing plugin.xml in " + path, new Exception());

		try {
			File dir = new File(path);
			if (dir.exists() && dir.isDirectory()) {
				File[] files = dir.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						File pluginFile = null;

						if (!(pluginFile = new File(files[i], "plugin.xml")).exists()) {
							pluginFile = new File(files[i], "fragment.xml");
						}

						if (pluginFile != null && pluginFile.exists()) {
							VersionedIdentifier identifier = new DefaultPluginParser().parse(new FileInputStream(pluginFile));
							plugin = new PluginIdentifier(identifier, pluginFile);

							addParsedPlugin(plugin);

						}
					} // files[i] is a directory
				}
			} // path is a directory
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error parsing file :" + path + " \r\n" + e.getMessage(), e);
			throw new CoreException(status);
		}

	}

	/**
	 * Method addParsedPlugins.
	 * @param model
	 * @throws CoreException
	 */
	private void addParsedPlugin(PluginIdentifier plugin) throws CoreException {

		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();

		// tranform each Plugin and Fragment in an Archive for the Site
		// and a pluginEntry
		String location = null;
		try {
			if (plugin != null) {
				URLEntry info;
				PluginEntry entry = new PluginEntry();
				entry.setContainer(site);
				entry.setPluginIdentifier(plugin.getIdentifier());
				entry.setPluginVersion(plugin.getVersion().toString());
				((Site) site).addPluginEntry(entry);

				SiteFileFactory archiveFactory = new SiteFileFactory();
				// the id is plugins\<pluginid>_<ver>.jar as per the specs
				ArchiveReferenceModel archive = archiveFactory.createArchiveReferenceModel();
				String pluginID = Site.DEFAULT_PLUGIN_PATH + plugin.toString() + FeaturePackagedContentProvider.JAR_EXTENSION;
				archive.setPath(pluginID);
				location = plugin.getLocation().toURL().toExternalForm();
				URL url = new URL(location);
				archive.setURLString(url.toExternalForm());
				((Site) site).addArchiveReferenceModel(archive);
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
		String[] dir;

		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		PluginIdentifier plugin;
		MultiStatus parsingStatus = new MultiStatus(id, IStatus.WARNING, "Error parsing plugin.xml", new Exception());

		try {
			if (pluginDir.exists()) {
				dir = pluginDir.list(FeaturePackagedContentProvider.filter);
				for (int i = 0; i < dir.length; i++) {

					file = new File(pluginPath, dir[i]);
					JarContentReference jarReference = new JarContentReference(null, file);
					ContentReference ref = jarReference.peek("plugin.xml", null, null);
					if (ref == null)
						jarReference.peek("fragment.xml", null, null);

					if (ref != null) {
						VersionedIdentifier identifier = new DefaultPluginParser().parse(ref.getInputStream());
						plugin = new PluginIdentifier(identifier,file);
						addParsedPlugin(plugin);
					} //ref!=null
				} //for
			}

		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, id, IStatus.OK, "Error accessing plugin.xml in file :" + file, e));
		}

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

	/**
	 * removes site.xml from the URL
	 */
	private URL removeSiteXML(URL url) throws MalformedURLException {
		URL result = url;

		// No need for decode encode
		if (url != null && url.getFile().endsWith(Site.SITE_XML)) {
			int index = url.getFile().lastIndexOf(Site.SITE_XML);
			String newPath = url.getFile().substring(0, index);
			result = new URL(url.getProtocol(), url.getHost(), url.getPort(), newPath);
		}
		return result;
	}

}