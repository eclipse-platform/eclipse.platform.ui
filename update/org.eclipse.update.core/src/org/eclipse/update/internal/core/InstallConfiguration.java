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

public class InstallConfiguration implements IInstallConfiguration, IWritable {

	private ListenersList listeners = new ListenersList();
	private boolean isCurrent;
	private URL location;
	private Date date;
	private String label;
	private List installSites;
	private List linkedSites;
	private List featuresConfigured;
	private List featuresUnconfigured;

	/*
	 * default constructor. Create
	 */
	public InstallConfiguration(URL location, String label) {
		this.location = location;
		this.label = label;
		this.isCurrent = false;
	}

	/*
	 * copy constructor
	 */
	public InstallConfiguration(IInstallConfiguration config,URL newLocation, String label) {
		this.location = newLocation;
		this.label = label;
		// do not copy list of listeners
		installSites = Arrays.asList(config.getInstallSites());
		linkedSites = Arrays.asList(config.getLinkedSites());
		featuresConfigured = Arrays.asList(config.getConfiguredFeatures());
		featuresUnconfigured = Arrays.asList(config.getUnconfiguredFeatures());
		this.isCurrent = false;
	}
	/**
	 * Returns all the featuresConfigured of all teh sites
	 */
	private IFeatureReference[] getFeatures() {

		IFeatureReference[] result = new IFeatureReference[0];

		// initialize if needed
		if (featuresConfigured == null) {
			featuresConfigured = new ArrayList();

			if (installSites != null) {
				Iterator iter = installSites.iterator();
				while (iter.hasNext()) {
					ISite currentSite = (ISite) iter.next();
					featuresConfigured.addAll(Arrays.asList(currentSite.getFeatureReferences()));
				}
			}

			if (linkedSites != null) {
				Iterator iter = linkedSites.iterator();
				while (iter.hasNext()) {
					ISite currentSite = (ISite) iter.next();
					featuresConfigured.addAll(Arrays.asList(currentSite.getFeatureReferences()));
				}
			}
		}

		System.out.println(featuresConfigured);
		if (featuresConfigured != null && !featuresConfigured.isEmpty()) {
			// move List in Array
			result = new IFeatureReference[featuresConfigured.size()];
			featuresConfigured.toArray(result);
		}

		return result;
	}

	/*
	 * @see IInstallConfiguration#getInstallSites()
	 */
	public IConfigurationSite[] getInstallSites() {
		IConfigurationSite[] sites = new IConfigurationSite[0];
		if (installSites != null && !installSites.isEmpty()) {
			sites = new IConfigurationSite[installSites.size()];
			installSites.toArray(sites);
		}
		return sites;
	}

	/*
	 * @see IInstallConfiguration#addInstallSite(IConfigurationSite)
	 */
	public void addInstallSite(IConfigurationSite site) {
		if (!isCurrent)
			return;
		if (installSites == null) {
			installSites = new ArrayList();
		}
		installSites.add(site);

		// notify listeners
		Object[] configurationListeners = listeners.getListeners();
		for (int i = 0; i < configurationListeners.length; i++) {
			((IInstallConfigurationChangedListener) configurationListeners[i]).installSiteAdded(site);
		}
	}

	/*
	 * @see IInstallConfiguration#removeInstallSite(IConfigurationSite)
	 */
	public void removeInstallSite(IConfigurationSite site) {
		if (!isCurrent)
			return;
		//FIXME: remove should make sure we synchronize
		if (installSites != null) {
			installSites.remove(site);

			// notify listeners
			Object[] configurationListeners = listeners.getListeners();
			for (int i = 0; i < configurationListeners.length; i++) {
				((IInstallConfigurationChangedListener) configurationListeners[i]).installSiteRemoved(site);
			}
		}
	}

	/*
	 * @see IInstallConfiguration#getLinkedSites()
	 */
	public IConfigurationSite[] getLinkedSites() {
		IConfigurationSite[] sites = new IConfigurationSite[0];
		if (linkedSites != null) {
			sites = new IConfigurationSite[linkedSites.size()];
			linkedSites.toArray(sites);
		}
		return sites;
	}

	/*
	 * @see IInstallConfiguration#addLinkedSite(IConfigurationSite)
	 */
	public void addLinkedSite(IConfigurationSite site) {
		if (!isCurrent)
			return;
		if (linkedSites == null) {
			linkedSites = new ArrayList();
		}
		linkedSites.add(site);
		// notify listeners
		Object[] configurationListeners = listeners.getListeners();
		for (int i = 0; i < configurationListeners.length; i++) {
			((IInstallConfigurationChangedListener) configurationListeners[i]).linkedSiteAdded(site);
		}
	}
	
	/*
	 * @see IInstallConfiguration#removeLinkedSite(IConfigurationSite)
	 */
	public void removeLinkedSite(IConfigurationSite site) {
		if (!isCurrent)
			return;
		//FIXME: remove should make sure we synchronize
		if (linkedSites != null) {
			linkedSites.remove(site);
			// notify listeners
			Object[] configurationListeners = listeners.getListeners();
			for (int i = 0; i < configurationListeners.length; i++) {
				((IInstallConfigurationChangedListener) configurationListeners[i]).linkedSiteRemoved(site);
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
		
		
		w.print(gap + "<" + SiteLocalParser.CONFIG + " ");
		//String URLInfoString = UpdateManagerUtils.getURLAsString(getLocation(),config.getURL());
		//w.print("url=\"" + Writer.xmlSafe(URLInfoString) + "\" ");

		//if (config.getLabel() != null) {
		//	w.print("label=\"" + Writer.xmlSafe(config.getLabel()) + "\"");
		//}

		w.println("/>");
		
	}

	/**
	 * Sets the date.
	 * @param date The date to set
	 */
	public void setCreationDate(Date date) {
		this.date = date;
	}

}