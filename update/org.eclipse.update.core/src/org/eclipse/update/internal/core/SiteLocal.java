package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.server.ExportException;
import java.util.*;

import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;
import org.eclipse.update.core.IInstallConfiguration;
import org.xml.sax.SAXException;

/**
 * This class manages the configurations.
 */

public class SiteLocal implements ILocalSite, IWritable {

	private ListenersList listeners = new ListenersList();
	private String label;
	private URL location;
	public static final String SITE_LOCAL_FILE = "LocalSite.xml";
	public static final String DEFAULT_CONFIG_LABEL = "Default configuration";
	public static final String DEFAULT_CONFIG_FILE = "DefaultConfig.xml";

	private List configurations;
	private IInstallConfiguration currentConfiguration;

	/*
	 * Constructor for LocalSite
	 */
	public SiteLocal() throws CoreException {
		super();
		initialize();
	}

	/*
	 * @see ILocalSite#getCurrentConfiguration()
	 */
	public IInstallConfiguration getCurrentConfiguration() {
		return currentConfiguration;
	}

	/*
	 * @see ILocalSite#getConfigurationHistory()
	 */
	public IInstallConfiguration[] getConfigurationHistory() {
		// return the current config as the last one
		IInstallConfiguration[] result = new IInstallConfiguration[0];
		if (configurations != null && !configurations.isEmpty()) {
			result = new IInstallConfiguration[configurations.size()];
			configurations.toArray(result);
		}
		return result;
	}

	/**
	 * adds a new configuration to the LocalSite
	 *  the newly added configuration is teh current one
	 */
	public void addConfiguration(IInstallConfiguration config) {
		if (config != null){
		if (configurations == null)
			configurations = new ArrayList(0);
		configurations.add(config);
		// FIXME: check if we have to remove a configuration

		// set configuration as current		
		if (currentConfiguration != null)
			currentConfiguration.setCurrent(false);
		config.setCurrent(true);
		currentConfiguration = config;

		// notify listeners
		Object[] siteLocalListeners = listeners.getListeners();
		for (int i = 0; i < siteLocalListeners.length; i++) {
			((ISiteLocalChangedListener) siteLocalListeners[i]).currentInstallConfigurationChanged(config);
		}
		}
		
	}
	/**
	 * initialize the configurations from the persistent model.
	 * The configurations are per user, so we save the data in the 
	 * user path, not the .metadata of any workspace, so the data
	 * is shared between the workspaces.
	 */
	private void initialize() throws CoreException {
		
		URL configXml=null;
		try {
			IPlatformConfiguration platformConfig = UpdateManagerUtils.getRuntimeConfiguration();
			location = platformConfig.getConfigurationLocation();
			configXml = UpdateManagerUtils.getURL(location, SITE_LOCAL_FILE, null);
			//if the file exists, parse it			
			SiteLocalParser parser = new SiteLocalParser(configXml.openStream(), this);
		}  catch (FileNotFoundException exception) {
			// file doesn't exist, ok, log it and continue 
			// log no config
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(location.toExternalForm() + " does not exist, there is no previous state or install history we can recover, we shall use default.");
			}
			createDefaultConfiguration();
			
			// FIXME: always save ?
			this.save();
			
		} catch (SAXException exception) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error during parsing of the install config XML:" + location.toExternalForm(), exception);
			throw new CoreException(status);
		}catch (MalformedURLException exception) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot create URL from: "+location.toExternalForm() +" & "+SITE_LOCAL_FILE , exception);
			throw new CoreException(status);
		}	catch (IOException exception) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot read xml file: "+configXml , exception);
			throw new CoreException(status);
		}

	}
	private void createDefaultConfiguration() throws CoreException {
		// FIXME: VK: in the first pass, we always return as the only install
		// site the install tree we are executing from. Once install 
		// configuration is fully supported, we will return whatever
		// install sites are part of the local configuration. As default
		// behavior, if we are executing out of read/write install tree accessible
		// through the "file:" protocol we will assume it is (one of) the
		// install sites (ie. does not need to be explicitly configured).
		try {
			URL execURL = BootLoader.getInstallURL();
			ISite site = SiteManager.getSite(execURL);
			IInstallConfiguration newDefaultConfiguration = cloneCurrentConfiguration(new URL(location, DEFAULT_CONFIG_FILE), DEFAULT_CONFIG_LABEL);
			addConfiguration(newDefaultConfiguration);			

			// notify listeners
			Object[] localSiteListeners = listeners.getListeners();
			for (int i = 0; i < localSiteListeners.length; i++) {
				((ISiteLocalChangedListener) localSiteListeners[i]).currentInstallConfigurationChanged(currentConfiguration);
			}

			//FIXME: the plugin site may not be read-write
			//the default is USER_EXCLUDE 
			ConfigurationSite configSite = (ConfigurationSite) SiteManager.createConfigurationSite(site, IPlatformConfiguration.ISitePolicy.USER_EXCLUDE);
			configSite.setInstallSite(true);
			currentConfiguration.addConfigurationSite(configSite);

		} catch (Exception e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot create the Local Site Object", e);
			throw new CoreException(status);
		}
	}
	/*
	 * @see ILocalSite#addLocalSiteChangedListener(ISiteLocalChangedListener)
	 */
	public void addLocalSiteChangedListener(ISiteLocalChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/*
	 * @see ILocalSite#removeLocalSiteChangedListener(ISiteLocalChangedListener)
	 */
	public void removeLocalSiteChangedListener(ISiteLocalChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/*
	 * @see ILocalSite#importConfiguration(File)
	 */
	public IInstallConfiguration importConfiguration(URL importURL,String label) throws CoreException{
		InstallConfiguration config = new InstallConfiguration(importURL,label);
		return config;
	}

	/**
	 * Gets the location of the local site.
	 * @return Returns a URL
	 */
	public URL getLocation() {
		return location;
	}

	/*
	 * @see ILocalSite#getLabel()
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 * @param label The label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Saves the site into the config file
	 */
	public void save() throws CoreException {

		// Save the current configuration as
		// the other are already saved
		 ((InstallConfiguration) currentConfiguration).save();
		// save the local site
		if (location.getProtocol().equalsIgnoreCase("file")) {
			File file = null;
			try {
				file = new File(UpdateManagerUtils.getURL(location, SITE_LOCAL_FILE, null).getFile());
				PrintWriter fileWriter = new PrintWriter(new FileOutputStream(file));
				Writer writer = new Writer();
				writer.writeSite(this, fileWriter);
				fileWriter.close();
			} catch (FileNotFoundException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot save site into " + file.getAbsolutePath(), e);
				throw new CoreException(status);
			} catch (MalformedURLException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot get handle on configuration file " + location.toExternalForm() + " : " + SITE_LOCAL_FILE, e);
				throw new CoreException(status);
			}
		}
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

		w.print(gap + "<" + SiteLocalParser.SITE + " ");
		if (getLabel() != null) {
			w.print("label=\"" + Writer.xmlSafe(getLabel()) + "\"");
		}
		w.println(">");
		w.println("");

		// teh last one is teh current configuration
		IInstallConfiguration[] refs = getConfigurationHistory();
		for (int index = 0; index < refs.length; index++) {
			IInstallConfiguration element = refs[index];
			if (!element.isCurrent()) {
				writeConfig(gap + increment, w, element);
			}
		}
		// write current configuration last
		writeConfig(gap + increment, w, getCurrentConfiguration());

		w.println("");
		// end
		w.println("</" + SiteLocalParser.SITE + ">");
	}

	private void writeConfig(String gap, PrintWriter w, IInstallConfiguration config) {
		w.print(gap + "<" + SiteLocalParser.CONFIG + " ");
		String URLInfoString = UpdateManagerUtils.getURLAsString(getLocation(), config.getURL());
		w.print("url=\"" + Writer.xmlSafe(URLInfoString) + "\" ");

		if (config.getLabel() != null) {
			w.print("label=\"" + Writer.xmlSafe(config.getLabel()) + "\"");
		}

		w.println("/>");
	}

	/*
	 * @see ILocalSite#createNewCurrentConfiguration(String)
	 */
	public IInstallConfiguration cloneCurrentConfiguration(URL newFile, String name) throws CoreException {
		
		// save previous current configuration
		if (getCurrentConfiguration()!=null)	((InstallConfiguration)getCurrentConfiguration()).save();
		
		InstallConfiguration result = null;
		String newFileName = UpdateManagerUtils.getLocalRandomIdentifier(DEFAULT_CONFIG_FILE);
		try {
			if (newFile == null)
				newFile = UpdateManagerUtils.getURL(getLocation(), newFileName, null);
			Date currentDate = new Date();
			// pass the date onto teh name
			if (name == null)	name = DEFAULT_CONFIG_LABEL + currentDate.getTime();
			result = new InstallConfiguration(getCurrentConfiguration(), newFile, name);
			// set teh same date in the installConfig
			result.setCreationDate(currentDate);
			
		} catch (MalformedURLException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot create a new configuration in:" + newFileName, e);
			throw new CoreException(status);
		}
		return result;
	}

	/*
	 * @see ILocalSite#revertTo(IInstallConfiguration)
	 */
	public void revertTo(IInstallConfiguration configuration) throws CoreException {
		// create a configuration
		IInstallConfiguration newConfiguration = cloneCurrentConfiguration(null, configuration.getLabel());
		// process delta
		// the Configured featuresConfigured are the same as the old configuration
		// the unconfigured featuresConfigured are the rest...

		// add to the stack which will set up as current
		addConfiguration(newConfiguration);

	}
}