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
		 * Returns the URL for this site
		 * 
		 * @return site url
		 */		
		public URL getURL();
		
		/**
		 * Returns the policy for this site
		 * 
		 * @return site policy
		 */				
		public ISitePolicy getSitePolicy();
		
		/**
		 * Sets the site policy
		 * 
		 * @param policy site policy
		 */
		public void setSitePolicy(ISitePolicy policy);				
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
	 * <li>specify site-defined plug-in list(s) (type==SITE_INCLUDE). 
	 * This is typically the best policy when using a remote administered 
	 * site where the user wants to defer to the site administrator the
	 * maintenance of the list of plug-ins that will be included at
	 * startup.
	 * </ul>
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
		 * Site-defined inclusion list. The list associated with this
		 * policy type is interpreted as path entries of site-include
		 * files <b>relative</b> to the site URL. Each site-include file
		 * is a Java properties file with one or more properties containing
		 * comma-separated list of plugin.xml or fragment.xml paths 
		 * <b>relative</b> to the site URL. The property keys are not
		 * interpreted. They are simply supported as a convenience to the
		 * list administrator (able to organize the site-include file
		 * as several shorter path lists, rather than one long one).
		 * 
		 * Following is an example of the content of a site-include file.
		 * <code>
		 * tools.xml = plugins/com.xyz.xml_1.0.3/plugin.xml,\
		 *             plugins/com.xyz.xml.nl/fragment.xml
		 * tools.ejb = ejb/com.xyz.ejbV1/plugin.xml,\
		 *             plugins/com.xyz.ejb_3/plugin.xml
		 * util.common = utils/testharness/plugin.xml,\
		 *               utils/relengtools/plugin.xml,\
		 *               utils/nltools/plugin.xml
		 * </code>
		 */
		public static final int SITE_INCLUDE = 2;
		
		/**
		 * Return policy type
		 * 
		 * @return policy type
		 */
		public int getType();
		
		/**
		 * Return policy inclusion/ exclusion list
		 * 
		 * @return the list as an array
		 */
		public String[] getList();
		
		/**
		 * Set new policy list
		 * 
		 * @param list policy inclusion/ exclusion list as an array.
		 * Returns an empty array if there are no entries.
		 */
		public void setList(String[] list);
	}
	
	/**
	 * Create a site entry
	 *
	 * @param url site URL
	 * @param policy site policy
	 * @return created site entry
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
	 */		
	public ISitePolicy createSitePolicy(int type, String[] list);		
		
	/**
	 * Configures the specified site entry. If a site entry with the
	 * same site URL is already configured, the entry is <b>not</b> replaced.
	 * 
	 * @param entry site entry 
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
	 */	
	public void configureSite(ISiteEntry entry, boolean replace);
		
	/**
	 * Unconfigures the specified entry. Does not do anything if the entry
	 * is not configured.
	 * 
	 * @param entry site entry
	 */	
	public void unconfigureSite(ISiteEntry entry);
		
	/**
	 * Returns configured site entries
	 * 
	 * @return array of site entries. Returns an empty array if no sites are
	 * configured
	 */	
	public ISiteEntry[] getConfiguredSites();
		
	/**
	 * Returns a site entry matching the specified URL
	 * 
	 * @param url site url
	 * @return matching site entry, or <code>null</code> if no match found
	 */	
	public ISiteEntry findConfiguredSite(URL url);
	
	/**
	 * Returns the URL location of the configuration information
	 * 
	 * @return configuration location URL, or <code>null</code> if the
	 * configuration location could not be determined.
	 */
	public URL getConfigurationLocation();
	
	/**
	 * Called to save the configuration information
	 */	
	public void save() throws IOException;
	
	/**
	 * Called to save the configuration information in the
	 * specified location
	 * 
	 * @param url save location.
	 */	
	public void save(URL url) throws IOException;

}

