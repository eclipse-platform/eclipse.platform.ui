package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.update.core.IInstallConfiguration;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.ILocalSite;
import org.eclipse.update.core.ISiteLocalChangedListener;
import org.eclipse.update.core.SiteManager;
import org.xml.sax.SAXException;

/**
 * This class manages the configurations.
 */

public class SiteLocal implements ILocalSite, IWritable {

	private ListenersList listeners = new ListenersList();
	private String label;
	private URL location;
	public static final String INSTALL_CONFIGURATION_FILE = "LocalSite.xml";
	public static final String DEFAULT_LABEL = "Default configuration";
	public static final String DEFAULT_LOCATION = "DefaultConfig.xml";

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
		if (configurations == null)
			configurations = new ArrayList(0);
		configurations.add(config);
		setCurrentConfiguration(config);
		// notify listeners
		Object[] siteLocalListeners = listeners.getListeners();
		for (int i = 0; i < siteLocalListeners.length; i++) {
			((ISiteLocalChangedListener) siteLocalListeners[i]).currentInstallConfigurationChanged(config);
		}
	}
	/**
	 * initialize the configurations from the persistent model.
	 * The configurations are per user, so we save the data in the 
	 * user path, not the .metadata of any workspace, so the data
	 * is shared between the workspaces.
	 */
	private void initialize() throws CoreException {
		File config = UpdateManagerPlugin.getPlugin().getStateLocation().append(INSTALL_CONFIGURATION_FILE).toFile();

		// FIXME... call VK API getConfigurationLocation which will return a URL
		try {
			location = new URL("file", null, config.getAbsolutePath());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		//if the file exists, parse it
		try {
			InputStream inStream = location.openStream();
			SiteLocalParser parser = new SiteLocalParser(inStream, this);
		} catch (SAXException exception) {
		} catch (IOException exception) {
			// file doesn't exist, ok, log it and continue 
			// log no config
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(location.toExternalForm() + " does not exist, there is no previous state or install history we can recover, we shall use default.");
			}
			createDefaultConfiguration();
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
			addConfiguration(new InstallConfiguration(UpdateManagerUtils.getURL(location,DEFAULT_LOCATION,null), DEFAULT_LABEL));
		
			// notify listeners
			Object[] localSiteListeners = listeners.getListeners();
			for (int i = 0; i < localSiteListeners.length; i++) {
				((ISiteLocalChangedListener) localSiteListeners[i]).currentInstallConfigurationChanged(currentConfiguration);
			}
		
			//FIXME: the pluign site may not be read-write
			currentConfiguration.addInstallSite(site);
		
			this.save();
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
	 * @see ILocalSite#setCurrentConfiguration(IInstallConfiguration)
	 */
	public void setCurrentConfiguration(IInstallConfiguration configuration) {
		if (currentConfiguration!=null)	currentConfiguration.setCurrent(false);
		configuration.setCurrent(true);
		currentConfiguration = configuration;
		//FIXME: revert
	}

	/*
	 * @see ILocalSite#importConfiguration(File)
	 */
	public IInstallConfiguration importConfiguration(File importFile) {
		return null;
	}

	/**
	 * Gets the location.
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
		if (location.getProtocol().equalsIgnoreCase("file")) {
			File file = new File(location.getFile());
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
		String URLInfoString = UpdateManagerUtils.getURLAsString(getLocation(),config.getURL());
		w.print("url=\"" + Writer.xmlSafe(URLInfoString) + "\" ");

		if (config.getLabel() != null) {
			w.print("label=\"" + Writer.xmlSafe(config.getLabel()) + "\"");
		}

		w.println("/>");
	}
}