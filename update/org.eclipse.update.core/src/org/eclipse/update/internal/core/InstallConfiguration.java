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
	private List activities;
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
		// FIXME: incomplete ?
		// FIXME: istaht true that Arrays.asList returns a fixed size list, so I cannot modify it ? 
		// so I have to do all this stuff to get a modifiable list ?
		if (config!=null){
			configurationSites = new ArrayList();
			configurationSites.addAll(Arrays.asList(config.getConfigurationSites()));
			featuresConfigured = new ArrayList();
			featuresConfigured.addAll(Arrays.asList(config.getConfiguredFeatures()));
			featuresUnconfigured = new ArrayList();
			featuresUnconfigured.addAll(Arrays.asList(config.getUnconfiguredFeatures()));
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
					featuresConfigured.addAll(Arrays.asList(currentSite.getConfigurationPolicy().getConfiguredFeatures()));
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
			
		//Start UOW ?
		ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_SITE_INSTALL);
		activity.setLabel("Installed configuration site: "+site.getSite().getURL().toExternalForm());
		activity.setDate(new Date());
			
		if (configurationSites == null) {
			configurationSites = new ArrayList(0);
		}
		configurationSites.add(site);
		

		// notify listeners
		Object[] configurationListeners = listeners.getListeners();
		for (int i = 0; i < configurationListeners.length; i++) {
			((IInstallConfigurationChangedListener) configurationListeners[i]).installSiteAdded(site);
		}
		
		// everything done ok
		activity.setStatus(IActivity.STATUS_OK);
		this.addActivity(activity);
	}

	/**
	 * Adds the configuration to teh list
	 */
	public void addConfigSite(IConfigurationSite site) {
		// FIXME: better name, better separation
		if (configurationSites == null) {
			configurationSites = new ArrayList(0);
		}
		configurationSites.add(site);
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
	public void export(File exportFile) throws CoreException{
		try {
				PrintWriter fileWriter = new PrintWriter(new FileOutputStream(exportFile));
				Writer writer = new Writer();
				writer.writeSite(this, fileWriter);
				fileWriter.close();
			} catch (FileNotFoundException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot save configration into " + exportFile.getAbsolutePath(), e);
				throw new CoreException(status);
			}		
	}

	/*
	 * @see IInstallConfiguration#getActivities()
	 */
	public IActivity[] getActivities() {
		IActivity[] result = new IActivity[0];
		if (activities!=null && !activities.isEmpty()){
			result = new IActivity[activities.size()];
			activities.toArray(result);
		}
		return result;
	}


	/**
	 * Adds an activity
	 */
	public void addActivity(IActivity activity) {
		if (activities==null) activities = new ArrayList(0);
		activities.add(activity);
	}

	/*
	 * @see IInstallConfiguration#getCreationDate()
	 */
	public Date getCreationDate() {
		return date;
	}
	
	/**
	 * Sets the date.
	 * @param date The date to set
	 */
	public void setCreationDate(Date date) {
		this.date = date;
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
			export(file);
		}
		
		
		// Write info for the next runtime
		Iterator iterConfigurationSites = configurationSites.iterator();
		while (iterConfigurationSites.hasNext()) {
			IConfigurationSite element = (IConfigurationSite) iterConfigurationSites.next();
			
			
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
		if (activities!=null && !activities.isEmpty()){
			Iterator iter = activities.iterator();
			while (iter.hasNext()) {
				ConfigurationActivity element = (ConfigurationActivity) iter.next();
				((IWritable)element).write(indent+IWritable.INDENT,w);				
			}	
		}
		
		// end
		w.println(gap+"</"+InstallConfigurationParser.CONFIGURATION+">");
		
		
	}



}