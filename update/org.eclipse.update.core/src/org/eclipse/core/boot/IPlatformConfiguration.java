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
 * 
 * @since 2.0
 */
public interface IPlatformConfiguration {
	
	/**
	 * Configuration entry representing an install site.
	 * 
	 * @since 2.0 
	 */
	public interface ISiteEntry {
		
		/**
		 * Returns the URL for this site
		 * 
		 * @return site url
		 * @since 2.0
		 */		
		public URL getURL();
		
		/**
		 * Returns the policy for this site
		 * 
		 * @return site policy
		 * @since 2.0
		 */				
		public ISitePolicy getSitePolicy();
		
		/**
		 * Sets the site policy
		 * 
		 * @param policy site policy
		 * @since 2.0
		 */
		public void setSitePolicy(ISitePolicy policy);
		
		/**
		 * Returns a list of features visible on the site. Note, that this is simply a 
		 * reflection of the site content. The features may or may not be actually configured.
		 * 
		 * @return an array of feature entries, or an empty array if no features are found.
		 * A feature entry is returned as a path relative to the site URL
		 * @since 2.0
		 */
		public String[] getFeatures();
		
		/**
		 * Returns a list of plug-ins visible on the site. Note, that this is simply a 
		 * reflection of the site content and the current policy for the site. The plug-ins
		 * may or may not end up being used by Eclipse (depends on which plug-in are 
		 * actually bound by the platform).
		 * 
		 * @return an array of plug-in entries, or an empty array if no plug-ins are found.
		 * A plug-in entry is returned as a path relative to the site URL		 * 
		 * @since 2.0
		 */
		public String[] getPlugins();
		
		/**
		 * Returns a stamp reflecting the current state of the site. If called repeatedly,
		 * returns the same value as long as no changes were made to the site (changes to
		 * features or plugins).
		 * 
		 * @return site change stamp
		 * @since 2.0
		 */
		public long getChangeStamp();
		
		/**
		 * Returns a stamp reflecting the current state of the features on the site. 
		 * If called repeatedly, returns the same value as long as no changes were made to
		 * features on the site.
		 * 
		 * @return site features change stamp 
		 * @since 2.0
		 */
		public long getFeaturesChangeStamp();
		
		/**
		 * Returns a stamp reflecting the current state of the plug-ins on the site. 
		 * If called repeatedly, returns the same value as long as no changes were made to
		 * plug-ins on the site.
		 * 
		 * @return site plug-ins change stamp 
		 * @since 2.0
		 */
		public long getPluginsChangeStamp();		
		
		/**
		 * Returns an indication whether the site can be updated.
		 * 
		 * @return <code>true</code> if site can be updated, <code>false</code> otherwise
		 * @since 2.0
		 */
		public boolean isUpdateable();			
	}
	
	/**
	 * Site policy. The site policy object determines how plug-ins
	 * contained on the site are processed during startup. In general,
	 * there are 3 ways of configuring a site policy
	 * <ul>
	 * <li>explicitly specify which plug-ins are to be included at
	 * startup (type==USER_INCLUDE). Any other plug-ins located
	 * at the site are ignored at startup. This is typically the best
	 * policy when using remote sites where the user wishes
	 * to retain explicit control over the plug-ins that are included
	 * from such site.
	 * <li>explicitly specify which plug-ins are to be excluded at
	 * startup (type==USER-EXCLUDE). All other plug-ins located
	 * at the site are used at startup. This policy requires that
	 * the site support an access "protocol" that allows plug-in
	 * discovery. In general, these are sites defined using the "file"
	 * URL protocol. This is typically the best policy for local
	 * install sites (on the user system).
	 * </ul>
	 * 
	 * @since 2.0
	 */
	public interface ISitePolicy {
		
		/**
		 * Policy type constants.
		 */
	
		/** 
		 * User-defined inclusion list. The list associated with this
		 * policy type is interpreted as path entries to included plugin.xml
		 * or fragment.xml <b>relative</b> to the site URL
		 */
		public static final int USER_INCLUDE = 0;
	
		/**
		 * User-defined exclusion list. The list associated with this
		 * policy type is interpreted as path entries to excluded plugin.xml
		 * or fragment.xml <b>relative</b> to the site URL
		 */
		public static final int USER_EXCLUDE = 1;
		
		/**
		 * Return policy type
		 * 
		 * @return policy type
		 * @since 2.0
		 */
		public int getType();
		
		/**
		 * Return policy inclusion/ exclusion list
		 * 
		 * @return the list as an array
		 * @since 2.0
		 */
		public String[] getList();
		
		/**
		 * Set new policy list. The list entries are interpreted based on the policy
		 * type. See description of the policy type constants for details.
		 * 
		 * @param list policy inclusion/ exclusion list as an array.
		 * Returns an empty array if there are no entries.
		 * @see USER_INCLUDE
		 * @see USER_EXCLUDE
		 * @since 2.0
		 */
		public void setList(String[] list);
	}
	
	/**
	 * Create a site entry
	 *
	 * @param url site URL
	 * @param policy site policy
	 * @return created site entry
	 * @since 2.0
	 */	
	public ISiteEntry createSiteEntry(URL url, ISitePolicy policy);
			
	/**
	 * Create a site policy. The policy determines the way the site
	 * plug-in are processed at startpu
	 *
	 * @param type policy type
	 * @param list an array of site-relative paths representing the
	 * inclusion/ exclusion list
	 * @return created site policy entry
	 * @since 2.0
	 */		
	public ISitePolicy createSitePolicy(int type, String[] list);		
		
	/**
	 * Configures the specified site entry. If a site entry with the
	 * same site URL is already configured, the entry is <b>not</b> replaced.
	 * 
	 * @param entry site entry 
	 * @since 2.0
	 */	
	public void configureSite(ISiteEntry entry);
		
	/**
	 * Configures the specified site entry. If a site entry with the
	 * same site URL is already configured, the replacement behavior for
	 * the entry can be specified.
	 * 
	 * @param entry site entry 
	 * @param  flag indicating whether an existing configured entry with
	 * the same URL should be replaced (<code>true</code>) or not (<code>false</code>).
	 * @since 2.0
	 */	
	public void configureSite(ISiteEntry entry, boolean replace);
		
	/**
	 * Unconfigures the specified entry. Does not do anything if the entry
	 * is not configured.
	 * 
	 * @param entry site entry
	 * @since 2.0
	 */	
	public void unconfigureSite(ISiteEntry entry);
		
	/**
	 * Returns configured site entries
	 * 
	 * @return array of site entries. Returns an empty array if no sites are
	 * configured
	 * @since 2.0
	 */	
	public ISiteEntry[] getConfiguredSites();
		
	/**
	 * Returns a site entry matching the specified URL
	 * 
	 * @param url site url
	 * @return matching site entry, or <code>null</code> if no match found
	 * @since 2.0
	 */	
	public ISiteEntry findConfiguredSite(URL url);
	
	/**
	 * Returns the URL location of the configuration information
	 * 
	 * @return configuration location URL, or <code>null</code> if the
	 * configuration location could not be determined.
	 * @since 2.0
	 */
	public URL getConfigurationLocation();
		
	/**
	 * Returns a stamp reflecting the current state of the configuration. If called repeatedly,
	 * returns the same value as long as no changes were made to the configuration (changes to
	 * sites, features or plugins).
	 * 
	 * @return configuration change stamp
	 * @since 2.0
	 */
	public long getChangeStamp();
		
	/**
	 * Returns a stamp reflecting the current state of the features in the configuration. 
	 * If called repeatedly, returns the same value as long as no changes were made to
	 * features in the configuration.
	 * 
	 * @return configuration features change stamp 
	 * @since 2.0
	 */
	public long getFeaturesChangeStamp();
		
	/**
	 * Returns a stamp reflecting the current state of the plug-ins in the configuration. 
	 * If called repeatedly, returns the same value as long as no changes were made to
	 * plug-ins in the configuration.
	 * 
	 * @return configuration plug-ins change stamp 
	 * @since 2.0
	 */
	public long getPluginsChangeStamp();	
	
	/**
	 * Indicates that feature changes have been processed and the corresponding plug-ins
	 * have been configured into this configuration. Making this call causes the 
	 * configuration change stamps to be "hardened" when the configuration is saved.
	 * If this method is not called, the change stamps are not "hardened" when the 
	 * configuration is saved. This will cause the changes to be detected again next
	 * time the configuration is recreated.
	 * 
	 * @since 2.0
	 */
	public void setFeatureChangesConfigured();		
	
	/**
	 * Computes the plug-in path for this configuration. The result includes all plug-ins
	 * visible on each of the configured sites based on each site policy.
	 * 
	 * @return an array of plug-in path elements (full URL entries), or an empty array.
	 * @since 2.0
	 */
	public URL[] getPluginPath();
	
	/**
	 * Called to save the configuration information
	 * @since 2.0
	 */	
	public void save() throws IOException;
	
	/**
	 * Called to save the configuration information in the
	 * specified location
	 * 
	 * @param url save location.
	 * @since 2.0
	 */	
	public void save(URL url) throws IOException;

}

