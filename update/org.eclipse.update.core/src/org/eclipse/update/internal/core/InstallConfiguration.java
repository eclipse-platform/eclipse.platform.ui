package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.xml.sax.SAXException;

public class InstallConfiguration implements IInstallConfiguration, IWritable {

	private ListenersList listeners = new ListenersList();
	private boolean isCurrent;
	private URL location;
	private Date date;
	private String label;
	private List configurationSites;
	private List featuresConfigured;
	private List featuresUnconfigured;

	/*
	 * default constructor. Create
	 */
	public InstallConfiguration(URL newLocation, String label) throws CoreException {
		this.location = newLocation;
		this.label = label;
		this.isCurrent = false;
		initialize();
	}

	/*
	 * copy constructor
	 */
	public InstallConfiguration(IInstallConfiguration config,URL newLocation, String label) {
		this.location = newLocation;
		this.label = label;
		// do not copy list of listeners
		// FIXME: incomplete
		if (config!=null){
			configurationSites = Arrays.asList(config.getConfigurationSites());
			featuresConfigured = Arrays.asList(config.getConfiguredFeatures());
			featuresUnconfigured = Arrays.asList(config.getUnconfiguredFeatures());
		}
		this.isCurrent = false;
	}
	
	/**
	 * initialize the configurations from the persistent model.
	 */
	private void initialize() throws CoreException {

		try {
			//URL configXml = UpdateManagerUtils.getURL(location, SITE_LOCAL_FILE, null);
			InstallConfigurationParser parser = new InstallConfigurationParser(location.openStream(), this);
			
		}  catch (FileNotFoundException exception) {
			// file doesn't exist, ok, log it and continue 
			// log no config
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(location.toExternalForm() + " does not exist, the local site is not in synch with the filesystem and is pointing to a file taht doesn;t exist.");
			}
		} catch (SAXException exception) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error during parsing of the install config XML:" + location.toExternalForm(), exception);
			throw new CoreException(status);
		} catch (IOException exception) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error during file access :" , exception);
			throw new CoreException(status);
		}

	}
	
	
	/**
	 * Returns all the featuresConfigured of all teh sites
	 */
	private IFeatureReference[] getFeatures() {

		IFeatureReference[] result = new IFeatureReference[0];

		// initialize if needed
		if (featuresConfigured == null) {
			featuresConfigured = new ArrayList();

			if (configurationSites != null) {
				Iterator iter = configurationSites.iterator();
				while (iter.hasNext()) {
					IConfigurationSite currentSite = (IConfigurationSite) iter.next();
					featuresConfigured.addAll(Arrays.asList(currentSite.getConfiguredFeatures()));
					// unconfigured features are getSIte.getFeatures - configuredFeatures ?
				}
			}
		}

		if (featuresConfigured != null && !featuresConfigured.isEmpty()) {
			// move List in Array
			result = new IFeatureReference[featuresConfigured.size()];
			featuresConfigured.toArray(result);
		}

		return result;
	}

	/*
	 * @see IInstallConfiguration#getConfigurationSites()
	 */
	public IConfigurationSite[] getConfigurationSites() {
		IConfigurationSite[] sites = new IConfigurationSite[0];
		if (configurationSites != null && !configurationSites.isEmpty()) {
			sites = new IConfigurationSite[configurationSites.size()];
			configurationSites.toArray(sites);
		}
		return sites;
	}


	/*
	 * @see IInstallConfiguration#addConfigurationSite(IConfigurationSite)
	 */
	public void addConfigurationSite(IConfigurationSite site) {
		if (!isCurrent)
			return;
		if (configurationSites == null) {
			configurationSites = new ArrayList();
		}
		configurationSites.add(site);

		// notify listeners
		Object[] configurationListeners = listeners.getListeners();
		for (int i = 0; i < configurationListeners.length; i++) {
			((IInstallConfigurationChangedListener) configurationListeners[i]).installSiteAdded(site);
		}
	}

	/*
	 * @see IInstallConfiguration#removeConfigurationSite(IConfigurationSite)
	 */
	public void removeConfigurationSite(IConfigurationSite site) {
		if (!isCurrent)
			return;
		//FIXME: remove should make sure we synchronize
		if (configurationSites != null) {
			configurationSites.remove(site);

			// notify listeners
			Object[] configurationListeners = listeners.getListeners();
			for (int i = 0; i < configurationListeners.length; i++) {
				((IInstallConfigurationChangedListener) configurationListeners[i]).installSiteRemoved(site);
			}
		}
	}

	/*
	 * @see IInstallConfiguration#isCurrent()
	 */
	public boolean isCurrent() {
		return isCurrent;
	}


	/*
	 *  @see IInstallConfiguration#setCurrent(boolean)
	 */
	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	/*
	 * @see IInstallConfiguration#addInstallConfigurationChangedListener(IInstallConfigurationChangedListener)
	 */
	public void addInstallConfigurationChangedListener(IInstallConfigurationChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/*
	 * @see IInstallConfiguration#removeInstallConfigurationChangedListener(IInstallConfigurationChangedListener)
	 */
	public void removeInstallConfigurationChangedListener(IInstallConfigurationChangedListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/*
	 * @see IInstallConfiguration#getConfiguredFeatures()
	 */
	public IFeatureReference[] getConfiguredFeatures() {
		// FIXME: 
		//if (featuresConfigured==null) featuresConfigured = getFeatures();
		return getFeatures();
	}

	/*
	 * @see IInstallConfiguration#getUnconfiguredFeatures()
	 */
	public IFeatureReference[] getUnconfiguredFeatures() {
		IFeatureReference[] result = new IFeatureReference[0];
		// FIXME:
		return result;
	}

	/*
	 * @see IInstallConfiguration#export(File)
	 */
	public void export(File exportFile) {
	}

	/*
	 * @see IInstallConfiguration#getActivities()
	 */
	public IActivity[] getActivities() {
		return null;
	}

	/*
	 * @see IInstallConfiguration#getCreationDate()
	 */
	public Date getCreationDate() {
		return date;
	}

	/*
	 * @see IInstallConfiguration#getURL()
	 */
	public URL getURL() {
		return location;
	}

	/**
	 * Sets the URL.
	 * @param location The URL to set
	 */
	public void setURL(URL location) {
		this.location = location;
	}

	/*
	 * @see IInstallConfiguration#getLabel()
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
	 * Saves the configuration into its URL/location
	 */
	public void save() throws CoreException {
		
		// save the configuration
		if (location.getProtocol().equalsIgnoreCase("file")) {
			// the location points to a file
			File file = new File(location.getFile());
			try {
				PrintWriter fileWriter = new PrintWriter(new FileOutputStream(file));
				Writer writer = new Writer();
				writer.writeSite(this, fileWriter);
				fileWriter.close(); 
			} catch (FileNotFoundException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot save configuration into " + file.getAbsolutePath(), e);
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
		
		
		w.print(gap + "<" + InstallConfigurationParser.CONFIGURATION + " ");
		w.print("date=\"" + date.getTime() + "\" ");
		w.println(">");
		w.println("");
		
		// site configurations
		if (configurationSites!=null){
		Iterator iter = configurationSites.iterator();
		while (iter.hasNext()) {
			ConfigurationSite element = (ConfigurationSite) iter.next();
			((IWritable)element).write(indent+IWritable.INDENT,w);
		}
		}
		
		// activities
		//FIXME
		
		// end
		w.println(gap+"</"+InstallConfigurationParser.CONFIGURATION+">");
		
		
	}

	/**
	 * Sets the date.
	 * @param date The date to set
	 */
	public void setCreationDate(Date date) {
		this.date = date;
	}

}