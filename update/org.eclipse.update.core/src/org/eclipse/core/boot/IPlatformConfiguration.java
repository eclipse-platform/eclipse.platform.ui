package org.eclipse.core.boot;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.net.URL;

/**
 * Platform configuration interface. Represents the runtime
 * configuration used by the Eclipse platform. Any configuration
 * changes do not take effect until next startup of the Eclipse
 * platform
 */
public interface IPlatformConfiguration {
	
	/**
	 * Configuration change status constants.
	 */
	
	/**
	 * No changes 
	 */
	public static final int NO_CHANGES = 0;
	
	/**
	 * Plug-in changes only
	 */
	public static final int PLUGIN_CHANGES = 1;
	
	/**
	 * Feature changes
	 */
	public static final int FEATURE_CHANGES = 2;
	
	/**
	 * Configuration entry representing an install site. 
	 */
	public interface ISiteEntry {
		
		/**
		 * Site policy constants.
		 */
	
		/** 
		 * Auto-detect changes to plug-in and feature configuration on startup
		 */
		public static final int USER_CONFIGURED_PLUS_CHANGES = 1;
	
		/**
		 * Only use plug-ins explicitly configured by user
		 */
		public static final int USER_CONFIGURED = 2;
	
		/**
		 * Only use plug-in explicitly configured via site file (eg. by administrator)
		 */
		public static final int SITE_CONFIGURED = 3;
		
		/**
		 * Returns the URL for this site
		 * 
		 * @return site url
		 */		
		public URL getURL();
		
		/**
		 * Returns the policy setting for this site
		 * 
		 * @return site policy setting
		 */				
		public int getPolicy();		
					
		/**
		 * Configures the specified plug-in entry. If the plug-in entry is already
		 * configured this method does nothing.
		 * 
		 * @param entry plug-in entry 
		 */	
		public void configurePluginEntry(IPluginEntry entry);
							
		/**
		 * Unconfigures the specified entry. Does not do anything is the entry
		 * is not configured.
		 * 
		 * @param entry plug-in entry
		 */	
		public void unconfigurePluginEntry(IPluginEntry entry);
				
		/**
		 * Returns all configured plug-in entries for this site
		 * 
		 * @return array of plug-in entries. An empty array is returned
		 * if there are no plug-ins explicitly configured for this site.
		 * An empty array is always returned for sites using
		 * <code>AUTO_DISCOVER_CHANGES</code> and <code>SITE_CONFIGURED_PLUGINS</code>
		 * policies.
		 */
		public IPluginEntry[] getConfiguredPluginEntries();
				
		/**
		 * Tests if specified plug-in entry is configured on this site
		 * 
		 * @param entry plug-in entry
		 * @return <code>true</code> if plug-in entry is configured, <code>false</code> otherwise
		 */	
		public boolean isConfigured(IPluginEntry entry);
				
	}
		
	/**
	 * Configuration entry representing a plug-in or plug-in fragment.
	 */
	public interface IPluginEntry {
				
		/**
		 * Returns the relative path to plugin.xnml or fragment.xml on its site
		 * 
		 * @return path relative to site entry URL
		 */				
		public String getRelativePath();
				
}
			
	/**
	 * Create a plug-in entry
	 *
	 * @param path site-relative path of the plugin.xml or fragment.xml
	 * for this entry
	 * @return created plug-in entry
	 */		
	public IPluginEntry createPluginEntry(String path);
	
	/**
	 * Create a site entry
	 *
	 * @param url site URL
	 * @param int site policy
	 * @return created site entry
	 */	
	public ISiteEntry createSiteEntry(URL url, int policy);		
		
	/**
	 * Configures the specified site entry. If a site entry with the
	 * same site URL is already configured, the entry is <b>not</b> replaced.
	 * 
	 * @param entry site entry 
	 */	
	public void configureSiteEntry(ISiteEntry entry);
		
	/**
	 * Configures the specified site entry. Note, that policy change may cause explicitly configured
	 * plug-in entries to be discarded, as follows:
	 * <ul>
	 * <li>USER_CONFIGURED_PLUS_CHANGES to USER_CONFIGURED: configured plug-in
	 * entries are reused, but the site no longer detects changes
	 * <li>USER_CONFIGURED_PLUS_CHANGES to SITE_CONFIGURED: configured plug-in
	 * entries are discarded. Site-specified configuration is used
	 * <li>USER_CONFIGURED to USER_CONFIGURED_PLUS_CHANGES: configured plug-in
	 * entries are reused, and changes are detected on startup
	 * <li>USER_CONFIGURED to SITE_CONFIGURED: configured plug-in
	 * entries are discarded. Site-specified configuration is used
	 * <li>SITE_CONFIGURED to USER_CONFIGURED_PLUS_CHANGES: the site is initialized
	 * with no plugins configured (the site-specified setting are not saved).
	 * Changes are detected on startup.
	 * <li>SITE_CONFIGURED to USER_CONFIGURED: the site is initialized
	 * with no plugins configured (the site-specified setting are not saved).
	 * Changes are not detected on startup.
	 * </ul>
	 * 
	 * @param entry site entry 
	 * @param  flag indicating whether an existing configured entry with
	 * the same URL should be replaced (<code>true</code>) or not (<code>false</code>).
	 */	
	public void configureSiteEntry(ISiteEntry entry, boolean replace);
		
	/**
	 * Unconfigures the specified entry. Does not do anything is the entry
	 * is not configured.
	 * 
	 * @param entry site entry
	 */	
	public void unconfigureSiteEntry(ISiteEntry entry);
		
	/**
	 * Returns configured site entries
	 * 
	 * @return array of site entries. Returns an empty array if no sites are
	 * configured
	 */	
	public ISiteEntry[] getConfiguredSiteEntries();
		
	/**
	 * Returns a site entry matching the specified URL
	 * 
	 * @param url site url
	 * @return matching site entry, or <code>null</code> if no match found
	 */	
	public ISiteEntry findConfiguredSiteEntry(URL url);
	
	/**
	 * Returns the URL location of the configuration information
	 * 
	 * @return configuration location URL, or <code>null</code> if the
	 * configuration location could not be determined.
	 */
	public URL getConfigurationLocation();
	
	/**
	 * Returns a status code indicating if the referenced site
	 * content has changes since the last time the configuration
	 * was saved. The status is computed at the time the 
	 * IPlatformConfiguration object is constructed. It takes into
	 * account the policy settings specified for each of its sites.
	 * Only sites that specify USER_CONFIGURED_PLUS_CHANGES policy
	 * are considered when computing the change status.
	 * 
	 * @return change status code. Valid values are NO_CHANGES,
	 * PLUGIN_CHANGES, FEATURE_CHANGES
	 */
	public int getChangeStatus();
		
	/**
	 * Called to save the configuration information
	 */	
	public void save() throws IOException;

}

