package org.eclipse.core.internal.boot;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.boot.IPlatformConfiguration.ISiteEntry;
import org.eclipse.core.boot.IPlatformConfiguration.ISitePolicy;

public class PlatformConfiguration implements IPlatformConfiguration {

	private static PlatformConfiguration currentPlatformConfiguration = null;
	static boolean r2_0 = false;

	private URL configLocation;
	private HashMap sites;
	private HashMap externalLinkSites; // used to restore prior link site state
	private String primaryFeature;
	private String primaryFeatureVersion;
	private String primaryFeatureApplication;
	private long lastChangeStamp;
	private long changeStamp;
	private boolean changeStampIsValid = false;
	private long lastFeaturesChangeStamp;
	private long featuresChangeStamp;
	private boolean featuresChangeStampIsValid = false;
	private long lastPluginsChangeStamp;
	private long pluginsChangeStamp;
	private boolean pluginsChangeStampIsValid = false;
	private boolean featureChangesConfigured = false;

	static boolean DEBUG = false;

	private static final String ECLIPSEDIR = "eclipse";
	private static final String PLUGINS = "plugins";
	private static final String INSTALL = "install";
	private static final String CONFIG_FILE = "platform.cfg";
	private static final String CONFIG_FILE_INIT = "install.properties";
	private static final String FEATURES = INSTALL + "/features";
	private static final String LINKS = "links";
	private static final String PLUGIN_XML = "plugin.xml";
	private static final String FRAGMENT_XML = "fragment.xml";
	private static final String FEATURE_XML = "feature.xml";

	private static final String CFG_SITE = "site";
	private static final String CFG_URL = "url";
	private static final String CFG_POLICY = "policy";
	private static final String[] CFG_POLICY_TYPE = {"USER-INCLUDE", "USER-EXCLUDE"};
	private static final String CFG_POLICY_TYPE_UNKNOWN = "UNKNOWN";
	private static final String CFG_LIST = "list";
	private static final String CFG_STAMP = "stamp";
	private static final String CFG_FEATURE_STAMP = "stamp.features";
	private static final String CFG_PLUGIN_STAMP = "stamp.plugins";
	private static final String CFG_UPDATEABLE = "updateable";
	private static final String CFG_LINK_FILE = "linkfile";
	private static final String CFG_PRIMARY_FEATURE = "primary.feature";
	private static final String INIT_PRIMARY_FEATURE = "application.configuration";
	private static final String DFLT_PRIMARY_FEATURE = "org.eclipse.sdk";
	private static final String CFG_PRIMARY_FEATURE_VERSION = "primary.feature.version";
	private static final String INIT_PRIMARY_FEATURE_VERSION = "application.configuration";
	private static final String CFG_PRIMARY_FEATURE_APP = "primary.feature.application";
	private static final String INIT_PRIMARY_FEATURE_APP = "application";
	private static final String DFLT_PRIMARY_FEATURE_APP = "org.eclipse.ui.workbench";
	private static final String CFG_VERSION = "version";
	private static final String VERSION = "1.0";
	private static final String EOF = "eof";
	private static final int CFG_LIST_LENGTH = 10;
	
	private static final int DEFAULT_POLICY_TYPE = ISitePolicy.USER_EXCLUDE;
	private static final String[] DEFAULT_POLICY_LIST = new String[0];
	
	private static final String ARG_USER_DIR = "user.dir";
	private static final String ARG_USER_HOME = "user.home";
	private static final String ARG_COMMON = "common";
	
	private static final String LINK_PATH = "path";
	private static final String LINK_READ = "r";
	private static final String LINK_READ_WRITE = "rw";

	public class SiteEntry implements IPlatformConfiguration.ISiteEntry {

		private URL url;
		private ISitePolicy policy;
		private boolean updateable = true;
		private ArrayList features;
		private ArrayList plugins;
		private PlatformConfiguration parent;
		private long lastChangeStamp;
		private long changeStamp;
		private boolean changeStampIsValid = false;
		private long lastFeaturesChangeStamp;
		private long featuresChangeStamp;
		private boolean featuresChangeStampIsValid = false;
		private long lastPluginsChangeStamp;
		private long pluginsChangeStamp;
		private boolean pluginsChangeStampIsValid = false;
		private String linkFileName = null;

		private SiteEntry() {
		}
		private SiteEntry(URL url, ISitePolicy policy, PlatformConfiguration parent) {
			if (url==null)
				throw new IllegalArgumentException();
				
			if (policy==null)
				throw new IllegalArgumentException();
				
			if (parent==null)
				throw new IllegalArgumentException();
				
			this.url = url;
			this.policy = policy;
			this.parent = parent;
			this.features = null;
			this.plugins = null;
		}

		/*
		 * @see ISiteEntry#getURL()
		 */
		public URL getURL() {
			return url;
		}

		/*
		* @see ISiteEntry#getSitePolicy()
		*/
		public ISitePolicy getSitePolicy() {
			return policy;
		}

		/*
		 * @see ISiteEntry#setSitePolicy(ISitePolicy)
		 */
		public void setSitePolicy(ISitePolicy policy) {				
			if (policy==null)
				throw new IllegalArgumentException();
			this.policy = policy;
		}
		
		/*
		 * @see ISiteEntry#getFeatures()
		 */
		public String[] getFeatures() {
			return getDetectedFeatures();
		}
		
		/*
		 * @see ISiteEntry#getPlugins()
		 */
		public String[] getPlugins() {
			
			ISitePolicy policy = getSitePolicy();
			
			if (policy.getType()==ISitePolicy.USER_INCLUDE)
				return policy.getList();
				
			if (policy.getType()==ISitePolicy.USER_EXCLUDE) {
				List detectedPlugins = Arrays.asList(getDetectedPlugins());
				String[] excludedPlugins = policy.getList();
				for (int i=0; i<excludedPlugins.length; i++) {
					if (detectedPlugins.contains(excludedPlugins[i]))
						detectedPlugins.remove(excludedPlugins[i]);
				}
				return (String[])detectedPlugins.toArray(new String[0]);
			}
			
			// bad policy type
			return new String[0];
		}
		
		/*
		 * @see ISiteEntry#getChangeStamp()
		 */
		public long getChangeStamp() {
			if (!changeStampIsValid)				
				computeChangeStamp();
			return changeStamp;
		}
		
		/*
		 * @see ISiteEntry#getFeaturesChangeStamp()
		 */
		public long getFeaturesChangeStamp() {
			if (!featuresChangeStampIsValid)				
				computeFeaturesChangeStamp();
			return featuresChangeStamp;
		}
		
		/*
		 * @see ISiteEntry#getPluginsChangeStamp()
		 */
		public long getPluginsChangeStamp() {
			if (!pluginsChangeStampIsValid)
				computePluginsChangeStamp();				
			return pluginsChangeStamp;
		}
		
		/*
		 * @see ISiteEntry#isUpdateable()
		 */
		public boolean isUpdateable() {
			//FIXME: should add actual read-write check
			return updateable;
		}
		
		private String[] detectFeatures() {
			
			// invalidate stamps ... we are doing discovery
			changeStampIsValid = false;
			featuresChangeStampIsValid = false;
			parent.changeStampIsValid = false;
			parent.featuresChangeStampIsValid = false;
			
			features = new ArrayList();
				
			if (!supportsDetection(url))
				return new String[0];

			// locate feature entries on site
			long start = 0;
			if (DEBUG)
				start = (new Date()).getTime();
			File root =
				new File(url.getFile().replace('/', File.separatorChar) + FEATURES);
			String[] list = root.list();
			String path;
			File plugin;
			for (int i = 0; list != null && i < list.length; i++) {
				path = list[i] + File.separator + FEATURE_XML;
				plugin = new File(root, path);
				if (!plugin.exists()) {
					continue;
				}
				features.add(FEATURES + "/" + path.replace(File.separatorChar, '/'));
			}
			if (DEBUG) {
				long end = (new Date()).getTime();
				debug(url.toString()+" located  "+features.size()+" feature(s) in "+(end-start)+"ms");
			}				
				
			return (String[])features.toArray(new String[0]);
		}
		
		private String[] detectPlugins() {
				
			// invalidate stamps ... we are doing discovery
			changeStampIsValid = false;
			pluginsChangeStampIsValid = false;
			parent.changeStampIsValid = false;
			parent.pluginsChangeStampIsValid = false;
			
			plugins = new ArrayList();
			
			if (!supportsDetection(url))
				return new String[0];
								
			// locate plugin entries on site
			long start = 0;
			if (DEBUG)
				start = (new Date()).getTime();
			File root =
				new File(url.getFile().replace('/', File.separatorChar) + PLUGINS);
			String[] list = root.list();
			String path;
			File plugin;
			for (int i = 0; list != null && i < list.length; i++) {
				path = list[i] + File.separator + PLUGIN_XML;
				plugin = new File(root, path);
				if (!plugin.exists()) {
					path = list[i] + File.separator + FRAGMENT_XML;
					plugin = new File(root, path);
					if (!plugin.exists())
						continue;
				}
				plugins.add(PLUGINS + "/" + path.replace(File.separatorChar, '/'));
			}
			if (DEBUG) {
				long end = (new Date()).getTime();
				debug(url.toString()+" located  "+plugins.size()+" plugin(s) in "+(end-start)+"ms");
			}								
				
			return (String[])plugins.toArray(new String[0]);
		}
		
		private String[] getDetectedFeatures() {
			if (features == null)
				return detectFeatures();
			else
				return (String[])features.toArray(new String[0]);
		}
		
		private String[] getDetectedPlugins() {
			if (plugins == null)
				return detectPlugins();
			else 
				return (String[])plugins.toArray(new String[0]);
		}
		
		private void computeChangeStamp() {
			computeFeaturesChangeStamp();
			computePluginsChangeStamp();
			changeStamp = url.hashCode() ^ featuresChangeStamp ^ pluginsChangeStamp;
			changeStampIsValid = true;
		}
		
		private void computeFeaturesChangeStamp() {
			if (featuresChangeStampIsValid)
				return;
				
			String[] features = getFeatures();
			featuresChangeStamp = computeStamp(features);
			featuresChangeStampIsValid = true;
			if (DEBUG) 
				debug(url.toString()+" feature stamp: "+featuresChangeStamp+((featuresChangeStamp==lastFeaturesChangeStamp)?" [no changes]":" [was "+lastFeaturesChangeStamp+"]"));
		}
		
		private void computePluginsChangeStamp() {
			if (pluginsChangeStampIsValid)
				return;
				
			String[] plugins = getPlugins();
			pluginsChangeStamp = computeStamp(plugins);
			pluginsChangeStampIsValid = true;
			if (DEBUG) 
				debug(url.toString()+" plugin stamp: "+pluginsChangeStamp+((pluginsChangeStamp==lastPluginsChangeStamp)?" [no changes]":" [was "+lastPluginsChangeStamp+"]"));
		}
		
		private long computeStamp(String[] targets) {
			
			long result = 0;
			if (!supportsDetection(url)) {
				// FIXME: this path should not be executed until we support running
				//        from an arbitrary URL (in particular from http server). For
				//        now just compute stamp across the list of names. Eventually
				//        when general URLs are supported we need to do better (factor
				//        in at least the existence of the target). However, given this
				//        code executes early on the startup sequence we need to be
				//        extremely mindful of performance issues.
				for (int i=0; i<targets.length; i++)
					result ^= targets[i].hashCode();				
			} else {
				// compute stamp across local targets		
				String rootPath = url.getFile().replace('/',File.separatorChar);
				if (rootPath.endsWith(File.separator))
					rootPath += File.separator;
				File rootFile = new File(rootPath);
				if (rootFile.exists()) {
					File f = null;
					for (int i=0; i<targets.length; i++) {
						f = new File(rootFile,targets[i]);
						if (f.exists())
							result ^= f.getAbsolutePath().hashCode() ^ f.lastModified() ^ f.length();
					}
				}
			}
			
			return result;
		}
		
		private boolean isExternallyLinkedSite() {
			return (linkFileName!=null && !linkFileName.trim().equals(""));
		}
}

	public class SitePolicy implements IPlatformConfiguration.ISitePolicy {

		private int type;
		private String[] list;

		private SitePolicy() {
		}
		private SitePolicy(int type, String[] list) {
			if (type != ISitePolicy.USER_INCLUDE
				&& type != ISitePolicy.USER_EXCLUDE)
				throw new IllegalArgumentException();
			this.type = type;

			if (list == null)
				this.list = new String[0];
			else
				this.list = list;
		}

		/*
		 * @see ISitePolicy#getType()
		 */
		public int getType() {
			return type;
		}

		/*
		* @see ISitePolicy#getList()
		*/
		public String[] getList() {
			return list;
		}

		/*
		 * @see ISitePolicy#setList(String[])
		 */
		public void setList(String[] list) {
			if (list == null)
				this.list = new String[0];
			else
				this.list = list;
		}

	}

	private PlatformConfiguration(String configArg) throws IOException {
		this.sites = new HashMap();
		this.externalLinkSites = new HashMap();
						
		// Determine configuration URL to use (based on command line argument)		
		// flag: -configuration COMMON | USER.HOME | USER.DIR | <path>
		//        	COMMON		in <eclipse>/install/<cfig>
		//        	USER.HOME	in <user.home>/eclipse/install/<cfig>
		//        	USER.DIR	in <user.dir>/eclipse/install/<cfig>
		//        	<path>		URL as specififed
		URL configURL = null;
		String tmp;
		if (configArg != null && !configArg.trim().equals("")) {
			if (configArg.equalsIgnoreCase(ARG_USER_DIR)) {
				tmp = System.getProperty("user.dir");
				if (!tmp.endsWith(File.separator))
					tmp += File.separator;
				configURL = new URL("file:" + tmp.replace(File.separatorChar,'/') + ECLIPSEDIR + "/" + INSTALL + "/" + CONFIG_FILE);
			} else if (configArg.equalsIgnoreCase(ARG_USER_HOME)) {				
				tmp = System.getProperty("user.home");
				if (!tmp.endsWith(File.separator))
					tmp += File.separator;
				configURL = new URL("file:" + tmp.replace(File.separatorChar,'/') + ECLIPSEDIR + "/" + INSTALL + "/" + CONFIG_FILE);
			} else if (configArg.equalsIgnoreCase(ARG_COMMON)) {				
				configURL = new URL(BootLoader.getInstallURL(), INSTALL + "/" + CONFIG_FILE);
			} else {
				try {
					configURL = new URL(configArg);
				} catch(MalformedURLException e) {
					throw new IllegalArgumentException(Policy.bind("cfig.badUrlArg",configArg));
				}
			}
		}
		initializeCurrent(configURL);		

		// FIXME: support for "safe mode"
		
		// Detect external links. These are "soft link" to additional sites. The link
		// files are usually provided by external installation programs. They are located
		// relative to this configuration URL.
		configureExternalLinks();
		
		// compute differences between configuration and actual content of the sites
		// (base sites and link sites)
		computeChangeStamp();
	}
	
	PlatformConfiguration(URL url) throws IOException {
		this.sites = new HashMap();
		this.externalLinkSites = new HashMap();
		initialize(url);
	}

	/*
	 * @see IPlatformConfiguration#createSiteEntry(URL, ISitePolicy)
	 */
	public ISiteEntry createSiteEntry(URL url, ISitePolicy policy) {
		return new PlatformConfiguration.SiteEntry(url, policy, this);
	}

	/*
	 * @see IPlatformConfiguration#createSitePolicy(int, String[])
	 */
	public ISitePolicy createSitePolicy(int type, String[] list) {
		return new PlatformConfiguration.SitePolicy(type, list);
	}

	/*
	 * @see IPlatformConfiguration#configureSite(ISiteEntry)
	 */
	public void configureSite(ISiteEntry entry) {
		configureSite(entry, false);
	}

	/*
	 * @see IPlatformConfiguration#configureSite(ISiteEntry, boolean)
	 */
	public void configureSite(ISiteEntry entry, boolean replace) {

		if (entry == null)
			return;

		URL key = entry.getURL();
		if (key == null)
			return;

		if (sites.containsKey(key) && !replace)
			return;

		sites.put(key, entry);
	}

	/*
	 * @see IPlatformConfiguration#unconfigureSite(ISiteEntry)
	 */
	public void unconfigureSite(ISiteEntry entry) {
		if (entry == null)
			return;

		URL key = entry.getURL();
		if (key == null)
			return;

		sites.remove(key);
	}

	/*
	 * @see IPlatformConfiguration#getConfiguredSites()
	 */
	public ISiteEntry[] getConfiguredSites() {
		if (sites.size() == 0)
			return new ISiteEntry[0];

		return (ISiteEntry[]) sites.values().toArray(new ISiteEntry[0]);
	}

	/*
	 * @see IPlatformConfiguration#findConfiguredSite(URL)
	 */
	public ISiteEntry findConfiguredSite(URL url) {
		if (url == null)
			return null;

		return (ISiteEntry) sites.get(url);
	}

	/*
	 * @see IPlatformConfiguration#getConfigurationLocation()
	 */
	public URL getConfigurationLocation() {
		return configLocation;
	}
	
	/*
	 * @see IPlatformConfiguration#getChangeStamp()
	 */
	public long getChangeStamp() {
		if (!changeStampIsValid)				
			computeChangeStamp();
		return changeStamp;
	}

	/*
	 * @see IPlatformConfiguration#getFeaturesChangeStamp()
	 */
	public long getFeaturesChangeStamp() {
		if (!featuresChangeStampIsValid)				
			computeFeaturesChangeStamp();
		return featuresChangeStamp;
	}

	/*
	 * @see IPlatformConfiguration#getPluginsChangeStamp()
	 */
	public long getPluginsChangeStamp() {
		if (!pluginsChangeStampIsValid)
			computePluginsChangeStamp();				
		return pluginsChangeStamp;
	}
	
	/*
	 * @see IPlatformConfiguration#setFeatureChangesConfigured()
	 */
	public void setFeatureChangesConfigured() {
		featureChangesConfigured = true;
	}

	/*
	 * @see IPlatformConfiguration#getPluginPath()
	 */
	public URL[] getPluginPath() {
		ArrayList path = new ArrayList();
		if (DEBUG)
			debug("computed plug-in path:");
			
		ISiteEntry[] sites = getConfiguredSites();
		for (int i=0; i<sites.length; i++) {
			String[] plugins = sites[i].getPlugins();
			for (int j=0; j<plugins.length; j++) {
				URL pathURL;
				try {
					pathURL = new URL(sites[i].getURL(),plugins[j]);
					path.add(pathURL);
					if (DEBUG)
						debug("   "+pathURL.toString());
				} catch(MalformedURLException e) {
					if (DEBUG)
						debug("   bad URL: "+e);
				}
			}
		}			
		return (URL[])path.toArray(new URL[0]);
	}
	
	/*
	 * @see IPlatformConfiguration#isUpdateable()
	 */
	public boolean isUpdateable() {
		// FIXME: support r/o configuration
		return true;
	}

	/*
	 * @see IPlatformConfiguration#save()
	 */
	public void save() throws IOException {
		if (isUpdateable())
			save(configLocation);
	}
	
	/*
	 * @see IPlatformConfiguration#save(URL)
	 */
	public void save(URL url) throws IOException {		
		if (url == null)
			throw new IOException(Policy.bind("cfig.unableToSave.noURL"));

		URLConnection uc = url.openConnection();
		uc.setDoOutput(true);
		OutputStream os = null;
		try {
			os = uc.getOutputStream();
		} catch (UnknownServiceException e) {
			// retry with direct file i/o
			if (!url.getProtocol().equals("file"))
				throw e;
			File cfigFile = new File(url.getFile().replace('/',File.separatorChar));
			File cfigDir = cfigFile.getParentFile();
			if (cfigDir!=null) {
				cfigDir.mkdirs();
			}
			os = new FileOutputStream(cfigFile);
		}
		PrintWriter w = new PrintWriter(os);
		try {
			write(w);
		} finally {
			w.close();
		}
	}
	
	static PlatformConfiguration getCurrent() {
		return currentPlatformConfiguration;
	}
	
	static void startup(URL url, String configArg) throws IOException {			
		// for 1.0 compatibility
		LaunchInfo.startup(url);
		if (!r2_0)
			return;
		
		// create current configuration
		if (currentPlatformConfiguration == null)
			currentPlatformConfiguration = new PlatformConfiguration(configArg);
	}
	
	static void shutdown() throws IOException {
		// for 1.0 compatibility
		LaunchInfo.shutdown();
		if (!r2_0)
			return;
		
		// save platform configuration
		PlatformConfiguration config = getCurrent();
		if (config != null) {
			try {
				config.save();
			} catch(IOException e) {
				if (DEBUG)
					debug("Unable to save configuration "+e.toString());
				// will recover on next startup
			}
		}
	}

	private void initializeCurrent(URL url) throws IOException {
		
		// URL of configuration file was specified ... just use it, or create one in specified
		// location if it does not exist
		if (url != null) {
			try {
				load(url);
				if (DEBUG)
					debug("Using configuration " + url.toString());
			} catch(IOException e) {
				ISitePolicy defaultPolicy = createSitePolicy(DEFAULT_POLICY_TYPE, DEFAULT_POLICY_LIST);
				ISiteEntry defaultSite = createSiteEntry(BootLoader.getInstallURL(), defaultPolicy);
				configureSite(defaultSite);	
				if (DEBUG)
					debug("Creating configuration " + url.toString());			
			}
			configLocation = url;
			return;
		}

		// URL was not specified. Default behavior is to look for configuration file in
		// USER.DIR then USER.HOME then COMMON
		
		URL userdirURL = null;
		try {
			String tmp = System.getProperty("user.dir");
			if (!tmp.endsWith(File.separator))
				tmp += File.separator;
			userdirURL = new URL("file:" + tmp.replace(File.separatorChar,'/') + ECLIPSEDIR + "/" + INSTALL + "/" + CONFIG_FILE);
			load(userdirURL);
			configLocation = userdirURL;
			if (DEBUG)
				debug("Using configuration " + configLocation.toString());
			return;
		} catch (IOException e) {
			if (DEBUG)
				debug("Unable to load configuration from USER.DIR " + e);
		}
		
		URL userhomeURL = null;
		try {
			String tmp = System.getProperty("user.home");
			if (!tmp.endsWith(File.separator))
				tmp += File.separator;
			userhomeURL = new URL("file:" + tmp.replace(File.separatorChar,'/') + ECLIPSEDIR + "/" + INSTALL + "/" + CONFIG_FILE);
			load(userhomeURL);
			configLocation = userhomeURL;
			if (DEBUG)
				debug("Using configuration " + configLocation.toString());
			return;
		} catch (IOException e) {
			if (DEBUG)
				debug("Unable to load configuration from USER.HOME " + e);
		}
		
		URL commonURL = null;
		try {
			commonURL = new URL(BootLoader.getInstallURL(), INSTALL + "/" + CONFIG_FILE);
			load(commonURL);
			configLocation = commonURL;
			if (DEBUG)
				debug("Using configuration " + configLocation.toString());
			return;
		} catch (IOException e) {
			if (DEBUG)
				debug("Unable to load configuration from COMMON " + e);
		}
		
		// No configuration files found. Assume COMMON (note: this may not be a r/w
		// location so we may not be able to save the configuration on shutdown. This is OK
		// for the default case) 
		
		ISitePolicy defaultPolicy = createSitePolicy(DEFAULT_POLICY_TYPE, DEFAULT_POLICY_LIST);
		ISiteEntry defaultSite = createSiteEntry(BootLoader.getInstallURL(), defaultPolicy);
		configureSite(defaultSite);
		configLocation = commonURL;
		if (DEBUG)
			debug("Creating configuration " + configLocation.toString());
	}
	
	private void initialize(URL url) throws IOException {
		if (url == null) {
			if (DEBUG)
				debug("Creating empty configuration");
			return;
		}
			
		load(url);
		configLocation = url;
		if (DEBUG)
			debug("Using configuration " + configLocation.toString());
	}
			
	private void computeChangeStamp() {
		computeFeaturesChangeStamp();
		computePluginsChangeStamp();
		changeStamp = configLocation.hashCode() ^ featuresChangeStamp ^ pluginsChangeStamp;
		changeStampIsValid = true;
	}
		
	private void computeFeaturesChangeStamp() {
		if (featuresChangeStampIsValid)
			return;
				
		long result = 0;
		ISiteEntry[] sites = getConfiguredSites();
		for (int i=0; i<sites.length; i++) {
			result ^= sites[i].getFeaturesChangeStamp();
		}
		featuresChangeStamp = result;
		featuresChangeStampIsValid = true;
	}
		
	private void computePluginsChangeStamp() {
		if (pluginsChangeStampIsValid)
			return;
				
		long result = 0;
		ISiteEntry[] sites = getConfiguredSites();
		for (int i=0; i<sites.length; i++) {
			result ^= sites[i].getPluginsChangeStamp();
		}
		pluginsChangeStamp = result;
		pluginsChangeStampIsValid = true;
	}
	
	private void configureExternalLinks() {
		if (!supportsDetection(configLocation))
			return;
		
		File cfigFile = new File(configLocation.getFile());
		File linkDir = new File(cfigFile.getParentFile(),LINKS+File.separator);
		File[] links = linkDir.listFiles();
		if (links==null || links.length==0) {
			if (DEBUG)
				debug("No links detected in "+linkDir.getAbsolutePath());
			return;
		}
		
		for (int i=0; i<links.length; i++) {
			if (links[i].isDirectory())
				continue;
			if (DEBUG)
				debug("Link file "+links[i].getAbsolutePath());	
			Properties props = new Properties();
			FileInputStream is = null;
			try {
				is = new FileInputStream(links[i]);
				props.load(is);			
				configureExternalLinkSites(links[i],props);				
			} catch(IOException e) {
				if (DEBUG)
					debug("   unable to load link file "+e);
				continue;
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch(IOException e) {
					}
				}
			}
		}		
	}
	
	private void configureExternalLinkSites(File linkFile, Properties props) {
		String path = props.getProperty(LINK_PATH);
		if (path==null) {
			if (DEBUG)
				debug("   no path definition");
			return;
		}
			
		String[] links = stringListToArray(path,",");
		
		String link;
		boolean updateable;
		URL siteURL;
		SiteEntry linkSite;
		ISitePolicy linkSitePolicy = createSitePolicy(DEFAULT_POLICY_TYPE, DEFAULT_POLICY_LIST);
		for (int i=0; i<links.length; i++) {
			updateable = false;
			if (links[i].startsWith(LINK_READ+" ")) {
				link = links[i].substring(2).trim();
			} else if (links[i].startsWith(LINK_READ_WRITE+" ")) {
				updateable = true;
				link = links[i].substring(3).trim();
			} else {
				link = links[i];
			}
			try {
				link = "file:"+link;
				siteURL = new URL(link);
			} catch(MalformedURLException e) {
				if (DEBUG)
					debug("  bad URL "+e);
				continue;
			}
			linkSite = (SiteEntry) createSiteEntry(siteURL, linkSitePolicy);
			linkSite.updateable = updateable;
			linkSite.linkFileName = linkFile.getAbsolutePath();
			SiteEntry lastLinkSite = (SiteEntry) externalLinkSites.get(siteURL);
			if (lastLinkSite != null) {
				// restore previous change stamps
				linkSite.lastChangeStamp = lastLinkSite.lastChangeStamp;
				linkSite.lastFeaturesChangeStamp = lastLinkSite.lastFeaturesChangeStamp;
				linkSite.lastPluginsChangeStamp = lastLinkSite.lastPluginsChangeStamp; 
			}			
			configureSite(linkSite);
			if (DEBUG)
				debug("   "+(updateable?"R/W -> ":"R/O -> ")+siteURL.toString());
		}
	}
	
	private void load(URL url) throws IOException {		
		
		if (url == null) 
			throw new IOException(Policy.bind("cfig.unableToLoad.noURL"));

		
		// try to load saved configuration file
		Properties props = new Properties();
		InputStream is = null;
		try {
			is = url.openStream();
			props.load(is);
			// check to see if we have complete config file
			if (!EOF.equals(props.getProperty(EOF))) {
				throw new IOException(Policy.bind("cfig.unableToLoad.incomplete",url.toString()));
			}
		} finally {
			if (is!=null) {
				try {
					is.close();
				} catch(IOException e) {
				}
			}
		}
		
		// check version
		String v = props.getProperty(CFG_VERSION);
		if (!VERSION.equals(v)) {			
			throw new IOException(Policy.bind("cfig.badVersion",v));
		}
		
						
		// load any initialization attributes. These become the initial default settings
		// for critical attributes (eg. primary feature) supplied by the packaging team
		Properties initProps = new Properties();
		is = null;
		try {
			URL initURL = new URL(url,CONFIG_FILE_INIT);
			is = initURL.openStream();
			initProps.load(is);
		} catch(IOException e) { // ignore errors
		} finally {
			if (is!=null) {
				try {
					is.close();
				} catch(IOException e) {
				}
			}
		}
				
		// load "bootstrap" properties
		primaryFeature = loadAttribute(props, CFG_PRIMARY_FEATURE, loadAttribute(initProps, INIT_PRIMARY_FEATURE,DFLT_PRIMARY_FEATURE));
		primaryFeatureVersion = loadAttribute(props, CFG_PRIMARY_FEATURE_VERSION, loadAttribute(initProps, INIT_PRIMARY_FEATURE_VERSION,null));
		primaryFeatureApplication = loadAttribute(props, CFG_PRIMARY_FEATURE_APP, loadAttribute(initProps, INIT_PRIMARY_FEATURE_APP,DFLT_PRIMARY_FEATURE_APP));
		// FIXME: temporary code for 1.0/ 2.0 compatibility
		int ix;
		if (primaryFeature!=null && (ix=primaryFeature.indexOf("_"))!=-1) 
			primaryFeature = primaryFeature.substring(0,ix);
		if (primaryFeatureVersion!=null && (ix=primaryFeatureVersion.indexOf("_"))!=-1)
			primaryFeatureVersion = primaryFeatureVersion.substring(ix+1);
			
		// load simple properties
		String stamp = loadAttribute(props, CFG_STAMP, null);
		if (stamp != null) {
			try {
				lastChangeStamp = Long.parseLong(stamp);
			} catch(NumberFormatException e) {
			}
		}
		
		stamp = loadAttribute(props, CFG_FEATURE_STAMP, null);
		if (stamp != null) {
			try {
				lastFeaturesChangeStamp = Long.parseLong(stamp);
			} catch(NumberFormatException e) {
			}
		}
		
		stamp = loadAttribute(props, CFG_PLUGIN_STAMP, null);
		if (stamp != null) {
			try {
				lastPluginsChangeStamp = Long.parseLong(stamp);
			} catch(NumberFormatException e) {
			}
		}
		
		// load site properties
		SiteEntry se = (SiteEntry) loadSite(props, CFG_SITE+".0", null);					
		for (int i=1; se != null; i++) {
			if (!se.isExternallyLinkedSite())
				configureSite(se);
			else
				// remember external link site state, but do not configure
				externalLinkSites.put(se.getURL(),se); 
			se = (SiteEntry) loadSite(props, CFG_SITE+"."+i, null);	
		}
	}
	
	private ISiteEntry loadSite(Properties props, String name, ISiteEntry dflt) {

		String urlString = loadAttribute(props, name+"."+CFG_URL, null);
		if (urlString == null)
			return dflt;
			
		URL url = null;
		try {
			url = new URL(urlString);
		} catch(MalformedURLException e) {
			return dflt;
		}
			
		int policyType;
		String[] policyList;
		String typeString = loadAttribute(props, name+"."+CFG_POLICY, null);
		if (typeString == null) {
			policyType = DEFAULT_POLICY_TYPE;
			policyList = DEFAULT_POLICY_LIST;
		} else {
			int i;
			for (i=0; i<CFG_POLICY_TYPE.length; i++) {
				if (typeString.equals(CFG_POLICY_TYPE[i])) {
					break;
				}
			}
			if (i>=CFG_POLICY_TYPE.length) {
				policyType = DEFAULT_POLICY_TYPE;
				policyList = DEFAULT_POLICY_LIST;
			} else {
				policyType = i;
				policyList = loadListAttribute(props, name+"."+CFG_LIST, new String[0]);
			}
		}

		ISitePolicy sp = createSitePolicy(policyType, policyList);
		SiteEntry site = (SiteEntry) createSiteEntry(url,sp);
		
		String stamp = loadAttribute(props, name+"."+CFG_STAMP, null);
		if (stamp != null) {
			try {
				site.lastChangeStamp = Long.parseLong(stamp);
			} catch(NumberFormatException e) {
			}
		}
		
		stamp = loadAttribute(props, name+"."+CFG_FEATURE_STAMP, null);
		if (stamp != null) {
			try {
				site.lastFeaturesChangeStamp = Long.parseLong(stamp);
			} catch(NumberFormatException e) {
			}
		}
		
		stamp = loadAttribute(props, name+"."+CFG_PLUGIN_STAMP, null);
		if (stamp != null) {
			try {
				site.lastPluginsChangeStamp = Long.parseLong(stamp);
			} catch(NumberFormatException e) {
			}
		}
		
		String flag = loadAttribute(props, name+"."+CFG_UPDATEABLE, null);
		if (flag != null) {
			if (flag.equals("true"))
				site.updateable = true;
			else
				site.updateable = false;
		}
		
		String linkname = loadAttribute(props, name+"."+CFG_LINK_FILE, null);
		if (linkname != null && !linkname.equals("")) {
			site.linkFileName = linkname.replace('/',File.separatorChar);
		}
		
		return site;
	}
	
	private String[] loadListAttribute(Properties props, String name, String[] dflt) {
		ArrayList list = new ArrayList();
		String value = loadAttribute(props, name+".0",null);
		if (value == null)
			return dflt;
			
		for (int i=1; value != null; i++) {
			loadListAttributeSegment(list, value);
			value = loadAttribute(props, name+"."+i, null);
		}
		return (String[])list.toArray(new String[0]);
	}
	
	private void loadListAttributeSegment(ArrayList list,String value) {
		
		if (value==null) return;
	
		StringTokenizer tokens = new StringTokenizer(value, ",");
		String token;
		while (tokens.hasMoreTokens()) {
			token = tokens.nextToken().trim();
			if (!token.equals("")) 
				list.add(token);
		}
		return;
	}
		
	private String loadAttribute(Properties props, String name, String dflt) {
		String prop = props.getProperty(name);
		if (prop == null)
			return dflt;
		else
			return prop.trim();
	}
	
	private boolean isReadWriteLocation(URL url) {
		return true; // FIXME: for now force use of the first location tried
	}
	
	private void write(PrintWriter w) {
		// write header
		w.println("# "+(new Date()).toString());
		writeAttribute(w, CFG_VERSION, VERSION);
		w.println("");
		
		// write global attributes
		writeAttribute(w,CFG_PRIMARY_FEATURE,primaryFeature);
		writeAttribute(w,CFG_PRIMARY_FEATURE_VERSION,primaryFeatureVersion); 
		writeAttribute(w,CFG_PRIMARY_FEATURE_APP,primaryFeatureApplication); 
		writeAttribute(w,CFG_STAMP,Long.toString(getChangeStamp()));
		writeAttribute(w,CFG_FEATURE_STAMP,Long.toString(getFeaturesChangeStamp()));
		writeAttribute(w,CFG_PLUGIN_STAMP,Long.toString(getPluginsChangeStamp()));
		
		// write out site entries
		SiteEntry[] list = (SiteEntry[]) sites.values().toArray(new SiteEntry[0]);
		for (int i = 0; i < list.length; i++) {
			writeSite(w, CFG_SITE + "." + Integer.toString(i), list[i]);
		}
		
		// write end-of-file marker
		writeAttribute(w, EOF, EOF);
	}

	private void writeSite(PrintWriter w, String id, SiteEntry entry) {
		
		// write site separator
		w.println("");
		
		// write out site settings
		writeAttribute(w, id + "." + CFG_URL, entry.getURL().toString());
		writeAttribute(w, id + "." + CFG_STAMP,Long.toString(entry.getChangeStamp()));
		writeAttribute(w, id + "." + CFG_FEATURE_STAMP,Long.toString(entry.getFeaturesChangeStamp()));
		writeAttribute(w, id + "." + CFG_PLUGIN_STAMP,Long.toString(entry.getPluginsChangeStamp()));
		writeAttribute(w, id + "." + CFG_UPDATEABLE, entry.updateable?"true":"false");
		if (entry.linkFileName != null && !entry.linkFileName.trim().equals(""))
			writeAttribute(w, id + "." + CFG_LINK_FILE, entry.linkFileName.trim().replace(File.separatorChar,'/'));
		
		// write out site policy
		int type = entry.getSitePolicy().getType();
		String typeString = CFG_POLICY_TYPE_UNKNOWN;
		try {
			typeString = CFG_POLICY_TYPE[type];
		} catch (IndexOutOfBoundsException e) {
		}
		writeAttribute(w, id + "." + CFG_POLICY, typeString);
		writeListAttribute(w, id + "." + CFG_LIST, entry.getSitePolicy().getList());
	}
	
	private void writeListAttribute(PrintWriter w, String id, String[] list) {
		if (list == null || list.length == 0)
			return;
			
		String value = "";
		int listLen = 0;
		int listIndex = 0;
		for (int i = 0; i < list.length; i++) {
			if (listLen != 0)
				value += ",";
			else
				value = "";
			value += list[i];

			if (++listLen >= CFG_LIST_LENGTH) {
				writeAttribute(w, id + "." + Integer.toString(listIndex++), value);
				listLen = 0;
			}
		}
		if (listLen != 0)
			writeAttribute(w, id + "." + Integer.toString(listIndex), value);
	}

	private void writeAttribute(PrintWriter w, String id, String value) {
		if (value==null || value.trim().equals(""))
			return;
		w.println(id + "=" + escapedValue(value));
	}
	
	private String escapedValue(String value) {
		// FIXME: implement escaping for property file
		return value;
	}
	
	private static String[] stringListToArray(String prop, String separator) {	
		if (prop == null || prop.trim().equals(""))
			return new String[0];
		ArrayList list = new ArrayList();
		StringTokenizer tokens = new StringTokenizer(prop, separator);
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken().trim();
			if (!token.equals(""))
				list.add(token);
		}
		return list.isEmpty() ? new String[0] : (String[]) list.toArray(new String[0]);
	}
	
	private static boolean supportsDetection(URL url) {
		return url.getProtocol().equals("file");
	}

	private static void debug(String s) {
		System.out.println("PlatformConfig: " + s);
	}

}