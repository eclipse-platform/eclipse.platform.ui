package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.IInstallConfiguration;
import org.eclipse.update.core.ILocalSiteChangedListener;

/**
 * This class manages the configurations.
 */

public class SiteLocal implements ISiteLocal {

	private ListenersList listeners = new ListenersList();
	private String label;
	private URL location;
	public static final String INSTALL_CONFIGURATION_FILE = "Local_site.config";

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
	 * @see ISiteLocal#getCurrentConfiguration()
	 */
	public IInstallConfiguration getCurrentConfiguration() {
		return currentConfiguration;
	}

	/*
	 * @see ISiteLocal#getConfigurationHistory()
	 */
	public IInstallConfiguration[] getConfigurationHistory() {
		IInstallConfiguration[] result = new IInstallConfiguration[0];
		if (configurations!=null && !configurations.isEmpty() ){
			result = new IInstallConfiguration[configurations.size()];
			configurations.toArray(result);
		}
		return result;
	}
	
	/**
	 * adds a new configuration to the LocalSite
	 *  the newly added configuration is teh current one
	 */
	public void addConfiguration(IInstallConfiguration config){
		if (configurations==null) configurations = new ArrayList(0);
		configurations.add(config);
		currentConfiguration = config;
		//FIXME: notify listeners
	}
	/**
	 * initialize the configurations from the persistent model.
	 * The configurations are per user, so we save the data in the 
	 * user path, not the .metadata of any workspace, so the data
	 * is shared between the workspaces.
	 */
	private void initialize() throws CoreException {
		File config = UpdateManagerPlugin.getPlugin().getStateLocation().append( INSTALL_CONFIGURATION_FILE).toFile();
		
		// FIXME... call VK API getConfigurationLocation which will return a URL
		try {
		location = new URL("file",null,config.getAbsolutePath());
		} catch (MalformedURLException e){
			e.printStackTrace();
		}
		
		if (config.exists()) {
			//if the file exists, parse it
			
			
		} else {
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
				currentConfiguration = new InstallConfiguration(location);

				// notify listeners
				Object[] localSiteListeners = listeners.getListeners();
				for (int i = 0; i < localSiteListeners.length; i++) {
					((ILocalSiteChangedListener) localSiteListeners[i]).currentInstallConfigurationChanged(currentConfiguration);
				}

				//FIXME: the pluign site may not be read-write
				currentConfiguration.addInstallSite(site);
			} catch (Exception e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot create the Local Site Object", e);
				throw new CoreException(status);
			}
		}

	}
	/*
	 * @see ISiteLocal#addLocalSiteChangedListener(ILocalSiteChangedListener)
	 */
	public void addLocalSiteChangedListener(ILocalSiteChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/*
	 * @see ISiteLocal#removeLocalSiteChangedListener(ILocalSiteChangedListener)
	 */
	public void removeLocalSiteChangedListener(ILocalSiteChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/*
	 * @see ISiteLocal#setCurrentConfiguration(IInstallConfiguration)
	 */
	public void setCurrentConfiguration(IInstallConfiguration configuration) {
		//FIXME: revert
	}

	/*
	 * @see ISiteLocal#importConfiguration(File)
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
	 * @see ISiteLocal#getLabel()
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

}