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
	 * Configuration entry representing an install site. 
	 */
	public interface ISiteEntry {
		
		/**
		 * Site policy constants.
		 */
	
		/** 
		 * Auto-detect changes to plug-in and feature configuration on startup
		 */
		public static final int AUTO_DISCOVER_CHANGES = 1;
	
		/**
		 * Only use plug-ins explicitly configured by user
		 */
		public static final int USER_CONFIGURED_PLUGINS = 2;
	
		/**
		 * Only use plug-in explicitly configured via site file (eg. by administrator)
		 */
		public static final int SITE_CONFIGURED_PLUGINS = 3;
		
		/**
		 * Create and optionally configure a plug-in entry for this site
		 *
		 * @param id unique identifier of the plug-in being configured
		 * @param version plug-in version. It must be a valid string
		 * form of a plug-in version identifier
		 * @param path path relative to the site URL of the plugin.xml 
		 * or fragment.xml for this entry
		 * @param configure flag indicating whether the entry is
		 * only created <code>false</code> or created and configured <code>true</code>.
		 * @return created plug-in entry
		 */		
		public IPluginEntry createPluginEntry(String id, String version, String path, boolean configure);
		
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
	}
		
	/**
	 * Configuration entry representing a plug-in or plug-in fragment.
	 */
	public interface IPluginEntry {
				
		/**
		 * Returns the site entry for this plug-in entry
		 * 
		 * @return site entry
		 */				
		public ISiteEntry getSiteEntry();
				
		/**
		 * Returns the relative path to plugin.xnml or fragment.xml on its site
		 * 
		 * @return path relative to site entry URL
		 */				
		public String getRelativePath();
				
		/**
		 * Returns the plug-in entry version
		 * 
		 * @return version
		 */				
		public String getVersion();
				
		/**
		 * Returns the plug-in unique identifier
		 * 
		 * @return site policy setting
		 */				
		public String getUniqueIdentifier();
	}
	
	/**
	 * Create and optionally configure a site entry
	 *
	 * @param url site URL
	 * @param int site policy
	 * @param configure flag indicating whether the entry is
	 * only created (<code>false</code>) or created and configured (<code>true</code>).
	 * If <code>true</code> is specified, the new entry is configured only
	 * if an entry with the same site URL does not already exist (ie. this
	 * method will not replace existing entries)
	 * @return created site entry
	 */	
	public ISiteEntry createSiteEntry(URL url, int policy, boolean configure);		
		
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
	 * <li>AUTO_DISCOVER_CHANGES to USER_CONFIGURED_PLUGINS: configured plug-in
	 * entries are reused, but the site no longer detects changes
	 * <li>AUTO_DISCOVER_CHANGES to SITE_CONFIGURED_PLUGINS: configured plug-in
	 * entries are discarded. Site-specified configuration is used
	 * <li>USER_CONFIGURED_PLUGINS to AUTO_DISCOVER_CHANGES: configured plug-in
	 * entries are reused, and changes are detected on startup
	 * <li>USER_CONFIGURED_PLUGINS to SITE_CONFIGURED_PLUGINS: configured plug-in
	 * entries are discarded. Site-specified configuration is used
	 * <li>SITE_CONFIGURED_PLUGINS to AUTO_DISCOVER_CHANGES: the site is initialized
	 * with no plugins configured (the site-specified setting are not saved).
	 * Changes are detected on startup.
	 * <li>SITE_CONFIGURED_PLUGINS to USER_CONFIGURED_PLUGINS: the site is initialized
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
	 * Tests if specified site entry is configured
	 * 
	 * @param entry site entry
	 * @return <code>true</code> if site is configured, <code>false</code> otherwise
	 */	
	public boolean isConfigured(ISiteEntry entry);
			
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
	 * Returns configured plug-in entries
	 * 
	 * @return array of plug-in entries. Returns an empty array if no 
	 * plug-in entries are configured
	 */	
	public IPluginEntry[] getConfiguredPluginEntries();
		
	/**
	 * Returns an array of plug-in entries matching the specified criteria.
	 * 
	 * @param id plug-in identifier
	 * @return array of matching plug-in entries, or <code>null</code>
	 * if no match found
	 */	
	public IPluginEntry[] findConfiguredPluginEntries(String id);
		
	/**
	 * Returns an array of plug-in entries matching the specified criteria.
	 * 
	 * @param id plug-in identifier
	 * @param version plug-in version. It must be a valid string
	 * form of a plug-in version identifier
	 * @return array of matching plug-in entries, or <code>null</code>
	 * if no match found
	 */	
	public IPluginEntry[] findConfiguredPluginEntries(String id, String version);
		
	/**
	 * Tests if specified plug-in entry is configured
	 * 
	 * @param entry plug-in entry
	 * @return <code>true</code> if plug-in entry is configured, <code>false</code> otherwise
	 */	
	public boolean isConfigured(IPluginEntry entry);
		
	/**
	 * Called to save the configuration information
	 */	
	public void save() throws IOException;

}

