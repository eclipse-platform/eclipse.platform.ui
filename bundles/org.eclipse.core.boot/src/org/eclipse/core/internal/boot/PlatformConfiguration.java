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
import java.net.URLDecoder;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.boot.IPlatformConfiguration.IFeatureEntry;
import org.eclipse.core.boot.IPlatformConfiguration.ISiteEntry;
import org.eclipse.core.boot.IPlatformConfiguration.ISitePolicy;

public class PlatformConfiguration implements IPlatformConfiguration {

	private static PlatformConfiguration currentPlatformConfiguration = null;

	private URL configLocation;
	private HashMap sites;
	private HashMap externalLinkSites; // used to restore prior link site state
	private HashMap cfgdFeatures;
	private HashMap bootPlugins;
	private String defaultFeature;
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
	private boolean transientConfig = false;
	
	private static String cmdConfiguration;
	private static String cmdFeature;
	private static String cmdApplication;
	private static URL cmdPlugins;

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
	
	private static final String[] BOOTSTRAP_PLUGINS = {"org.eclipse.core.boot", "org.eclipse.core.runtime"};
	private static final String CFG_BOOT_PLUGIN = "bootstrap";
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
	private static final String CFG_FEATURE_ENTRY = "feature";
	private static final String CFG_FEATURE_ENTRY_DEFAULT = "feature.default.id";
	private static final String CFG_FEATURE_ENTRY_ID = "id";
	private static final String CFG_FEATURE_ENTRY_VERSION = "version";
	private static final String CFG_FEATURE_ENTRY_APPLICATION = "application";
	private static final String CFG_FEATURE_ENTRY_ROOT = "root";
	
	private static final String INIT_DEFAULT_FEATURE_ID = "feature.default.id";
	private static final String INIT_DEFAULT_FEATURE_APPLICATION = "feature.default.application";
	private static final String DEFAULT_FEATURE_ID = "org.eclipse.sdk";
	private static final String DEFAULT_FEATURE_APPLICATION = "org.eclipse.ui.workbench";
	
	private static final String CFG_VERSION = "version";
	private static final String CFG_TRANSIENT = "transient";
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
	
	private static final String CMD_CONFIGURATION = "-configuration";
	private static final String CMD_FEATURE = "-feature";
	private static final String CMD_APPLICATION = "-application";
	private static final String CMD_PLUGINS = "-plugins";
	
	public class SiteEntry implements IPlatformConfiguration.ISiteEntry {

		private URL url; // this is the external URL for the site
		private URL resolvedURL; // this is the resolved URL used internally
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
			this.resolvedURL = this.url;
			if (url.getProtocol().equals(PlatformURLHandler.PROTOCOL)) {
				try {
					resolvedURL = ((PlatformURLConnection)url.openConnection()).getResolvedURL();
				} catch(IOException e) {
				}
			}
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
		public synchronized void setSitePolicy(ISitePolicy policy) {				
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
				ArrayList detectedPlugins = new ArrayList(Arrays.asList(getDetectedPlugins()));
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
				
			if (!supportsDetection(resolvedURL))
				return new String[0];

			// locate feature entries on site
			long start = 0;
			if (DEBUG)
				start = (new Date()).getTime();
			File root =
				new File(resolvedURL.getFile().replace('/', File.separatorChar) + FEATURES);
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
				debug(resolvedURL.toString()+" located  "+features.size()+" feature(s) in "+(end-start)+"ms");
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
			
			if (!supportsDetection(resolvedURL))
				return new String[0];
								
			// locate plugin entries on site
			long start = 0;
			if (DEBUG)
				start = (new Date()).getTime();
			File root =
				new File(resolvedURL.getFile().replace('/', File.separatorChar) + PLUGINS);
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
				debug(resolvedURL.toString()+" located  "+plugins.size()+" plugin(s) in "+(end-start)+"ms");
			}								
				
			return (String[])plugins.toArray(new String[0]);
		}
		
		private synchronized String[] getDetectedFeatures() {
			if (features == null)
				return detectFeatures();
			else
				return (String[])features.toArray(new String[0]);
		}
		
		private synchronized String[] getDetectedPlugins() {
			if (plugins == null)
				return detectPlugins();
			else 
				return (String[])plugins.toArray(new String[0]);
		}
		
		private URL getResolvedURL() {
			return resolvedURL;
		}
		
		private void computeChangeStamp() {
			computeFeaturesChangeStamp();
			computePluginsChangeStamp();
			changeStamp = resolvedURL.hashCode() ^ featuresChangeStamp ^ pluginsChangeStamp;
			changeStampIsValid = true;
		}
		
		private synchronized void computeFeaturesChangeStamp() {
			if (featuresChangeStampIsValid)
				return;
				
			String[] features = getFeatures();
			featuresChangeStamp = computeStamp(features);
			featuresChangeStampIsValid = true;
			if (DEBUG) 
				debug(resolvedURL.toString()+" feature stamp: "+featuresChangeStamp+((featuresChangeStamp==lastFeaturesChangeStamp)?" [no changes]":" [was "+lastFeaturesChangeStamp+"]"));
		}
		
		private synchronized void computePluginsChangeStamp() {
			if (pluginsChangeStampIsValid)
				return;
				
			String[] plugins = getPlugins();
			pluginsChangeStamp = computeStamp(plugins);
			pluginsChangeStampIsValid = true;
			if (DEBUG) 
				debug(resolvedURL.toString()+" plugin stamp: "+pluginsChangeStamp+((pluginsChangeStamp==lastPluginsChangeStamp)?" [no changes]":" [was "+lastPluginsChangeStamp+"]"));
		}
		
		private long computeStamp(String[] targets) {
			
			long result = 0;
			if (!supportsDetection(resolvedURL)) {
				// FIXME: this path should not be executed until we support running
				//        from an arbitrary URL (in particular from http server). For
				//        now just compute stamp across the list of names. Eventually
				//        when general URLs are supported we need to do better (factor
				//        in at least the existence of the target). However, given this
				//        code executes early on the startup sequence we need to be
				//        extremely mindful of performance issues.
				for (int i=0; i<targets.length; i++)
					result ^= targets[i].hashCode();
				if (DEBUG)
					debug("*WARNING* computing stamp using URL hashcodes only");				
			} else {
				// compute stamp across local targets		
				String rootPath = resolvedURL.getFile().replace('/',File.separatorChar);
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
		
		private synchronized void refresh() {
			// reset computed values. Will be updated on next access.		
			lastChangeStamp = changeStamp;
			lastFeaturesChangeStamp = featuresChangeStamp;
			lastPluginsChangeStamp = pluginsChangeStamp;
			changeStampIsValid = false;
			featuresChangeStampIsValid = false;
			pluginsChangeStampIsValid = false;
			features = null;
			plugins = null;
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
		public synchronized void setList(String[] list) {
			if (list == null)
				this.list = new String[0];
			else
				this.list = list;
		}

	}
	
	private class FeatureEntry implements IPlatformConfiguration.IFeatureEntry {
		private String id;
		private String version;
		private String application;
		private URL root;
		
		private FeatureEntry(String id, String version, String application, URL root) {
			if (id == null)
				throw new IllegalArgumentException();
			this.id = id;
			this.version = version;
			this.application = application;
			this.root = root;
		}
				
		/*
		 * @see IFeatureEntry#getFeatureIdentifier()
		 */
		public String getFeatureIdentifier() {
			return id;
		}
		
		/*
		 * @see IFeatureEntry#getFeatureVersion()
		 */
		public String getFeatureVersion() {
			return version;
		}
		
		/*
		 * @see IFeatureEntry#getFeatureApplication()
		 */
		public String getFeatureApplication() {
			return application;
		}
		
		/*
		 * @see IFeatureEntry#getFeatureRootURL()
		 */
		public URL getFeatureRootURL() {
			return root;
		}
	}
	
	private class VersionedIdentifier {
		private String identifier = "";		
		private int major = 0;
		private int minor = 0;
		private int service = 0;
		private String qualifier = "";
	
		private static final String VER_SEPARATOR = ".";
		private static final String ID_SEPARATOR = "_";
		
		public VersionedIdentifier(String s) {		
			if (s==null || (s=s.trim()).equals("")) 
				return;
		
			int loc = s.lastIndexOf(ID_SEPARATOR);
			if (loc != -1) {
				this.identifier = s.substring(0, loc);
				String version = s.substring(loc+1);
				parseVersion(version);
			} else
			 this.identifier = s;		
		}
		
		public boolean identifierEquals(String id) {
			if (id == null)
				return identifier == null;
			else
				return id.equals(identifier);
		}		
		
		public int compareVersion(VersionedIdentifier id) {

			if (id == null) {
				if (major==0 && minor==0 && service==0) return -1;
				else return 1;
			}

			if (major > id.major) return 1;
			if (major < id.major) return -1;
			if (minor > id.minor) return 1;
			if (minor < id.minor) return -1;	
			if (service > id.service) return 1;
			if (service < id.service) return -1;
			return compareQualifiers(qualifier, id.qualifier);
		}

		private int compareQualifiers(String q1, String q2) {
			int result = q1.compareTo(q2);
			if (result<0)
				return -1;
			else if (result>0)
				return 1;
			else
				return 0;
		}	
		
		private void parseVersion(String v) {				
			if( v == null || (v=v.trim()).equals(""))
				return;
		
			try{
				StringTokenizer st = new StringTokenizer(v, VER_SEPARATOR);
				ArrayList elements = new ArrayList(4);

				while(st.hasMoreTokens()) {
					elements.add(st.nextToken());
				}

				if (elements.size()>=1) this.major = (new Integer((String)elements.get(0))).intValue();
				if (elements.size()>=2) this.minor = (new Integer((String)elements.get(1))).intValue();
				if (elements.size()>=3) this.service = (new Integer((String)elements.get(2))).intValue();
				if (elements.size()>=4) this.qualifier = removeWhiteSpace((String)elements.get(3));
		
			} catch (Exception e) { // use what we got so far
			}
		}
		
		private String removeWhiteSpace(String s) {
			char[] chars = s.trim().toCharArray();
			boolean whitespace = false;
			for(int i=0; i<chars.length; i++) {
				if (Character.isWhitespace(chars[i])) {
					chars[i] = '_';
					whitespace = true;
				}
			}
			return whitespace ? new String(chars) : s;
		}
	}

	private PlatformConfiguration(String configArg) throws IOException {
		this.sites = new HashMap();
		this.externalLinkSites = new HashMap();
		this.cfgdFeatures = new HashMap();
		this.bootPlugins = new HashMap();
						
		// Determine configuration URL to use (based on command line argument)	
		URL configURL = getConfigurationURL(configArg);

		// initialize configuration
		initializeCurrent(configURL);
		
		// pick up any first-time default settings relative to selected config location
		loadInitializationAttributes(configLocation);		

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
		this.cfgdFeatures = new HashMap();
		this.bootPlugins = new HashMap();
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
	 * @see IPlatformConfiguration#createFeatureEntry(String, String, String, URL)
	 */
	public IFeatureEntry createFeatureEntry(
		String id,
		String version,
		String application,
		URL root) {
		return new PlatformConfiguration.FeatureEntry(id, version, application, root);
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
	public synchronized void configureSite(ISiteEntry entry, boolean replace) {

		if (entry == null)
			return;

		URL url = entry.getURL();
		if (url == null)
			return;
		String key = url.toExternalForm();

		if (sites.containsKey(key) && !replace)
			return;

		sites.put(key, entry);
	}

	/*
	 * @see IPlatformConfiguration#unconfigureSite(ISiteEntry)
	 */
	public synchronized void unconfigureSite(ISiteEntry entry) {
		if (entry == null)
			return;

		URL url = entry.getURL();
		if (url == null)
			return;
		String key = url.toExternalForm();

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
		String key = url.toExternalForm();

		ISiteEntry result = (ISiteEntry) sites.get(key);
		if (result == null) // retry with decoded URL string
			result = (ISiteEntry) sites.get(URLDecoder.decode(key));
		return result;
	}
	
	/*
	 * @see IPlatformConfiguration#configureFeatureEntry(IFeatureEntry)
	 */
	public synchronized void configureFeatureEntry(IFeatureEntry entry) {
		if (entry == null)
			return;

		String key = entry.getFeatureIdentifier();
		if (key == null)
			return;

		cfgdFeatures.put(key, entry);
	}

	/*
	 * @see IPlatformConfiguration#unconfigureFeatureEntry(IFeatureEntry)
	 */
	public synchronized void unconfigureFeatureEntry(IFeatureEntry entry) {
		if (entry == null)
			return;

		String key = entry.getFeatureIdentifier();
		if (key == null)
			return;

		cfgdFeatures.remove(key);
	}

	/*
	 * @see IPlatformConfiguration#getConfiguredFeatureEntries()
	 */
	public IFeatureEntry[] getConfiguredFeatureEntries() {
		if (cfgdFeatures.size() == 0)
			return new IFeatureEntry[0];

		return (IFeatureEntry[]) cfgdFeatures.values().toArray(new IFeatureEntry[0]);
	}

	/*
	 * @see IPlatformConfiguration#findConfiguredFeatureEntry(String)
	 */
	public IFeatureEntry findConfiguredFeatureEntry(String id) {
		if (id == null)
			return null;

		return (IFeatureEntry) cfgdFeatures.get(id);
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
	 * @see IPlatformConfiguration#getApplicationIdentifier()
	 */
	public String getApplicationIdentifier() {
		
		if (cmdApplication != null) // application was specified
			return cmdApplication;
			
		// if -feature was not specified use the default feature
		String feature = cmdFeature;
		if (feature == null)
			feature = defaultFeature;
			
		// lookup application for feature (specified or defaulted)
		if (feature != null) {
			IFeatureEntry fe = findConfiguredFeatureEntry(feature);
			if (fe != null) {
				if (fe.getFeatureApplication() != null)
					return fe.getFeatureApplication();
			}
		}
		
		// return hardcoded default if we failed
		return DEFAULT_FEATURE_APPLICATION;
	}

	/*
	 * @see IPlatformConfiguration#getPrimaryFeatureIdentifier()
	 */
	public String getPrimaryFeatureIdentifier() {
		
		if (cmdFeature != null) // -feature was specified on command line
			return cmdFeature; 
		
		// feature was not specified on command line
		if (defaultFeature != null)
			return defaultFeature; // return customized default if set
		else
			return DEFAULT_FEATURE_ID; // return hardcoded default
	}

	/*
	 * @see IPlatformConfiguration#getPluginPath()
	 */
	public URL[] getPluginPath() {
		ArrayList path = new ArrayList();
		if (DEBUG)
			debug("computed plug-in path:");
			
		ISiteEntry[] sites = getConfiguredSites();
		URL pathURL;
		for (int i=0; i<sites.length; i++) {
			String[] plugins = sites[i].getPlugins();
			for (int j=0; j<plugins.length; j++) {
				try {
					pathURL = new URL(((SiteEntry)sites[i]).getResolvedURL(),plugins[j]);
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
	 * @see IPlatformConfiguration#getBootstrapPluginIdentifiers()
	 */
	public String[] getBootstrapPluginIdentifiers() {
		return BOOTSTRAP_PLUGINS;
	}

	/*
	 * @see IPlatformConfiguration#setBootstrapPluginLocation(String, URL)
	 */
	public void setBootstrapPluginLocation(String id, URL location) {
		String[] ids = getBootstrapPluginIdentifiers();
		for (int i=0; i<ids.length; i++) {
			if (ids[i].equals(id)) {
				bootPlugins.put(id, location.toExternalForm());
				break;
			}
		}
	}
		
	/*
	 * @see IPlatformConfiguration#isUpdateable()
	 */
	public boolean isUpdateable() {
		// FIXME: support r/o configuration
		return true;
	}
	
	/*
	 * @see IPlatformConfiguration#isTransient()
	 */
	public boolean isTransient() {
		return transientConfig;
	}
	
	/*
	 * @see IPlatformConfiguration#isTransient(boolean)
	 */
	public void isTransient(boolean value) {
		if (this != BootLoader.getCurrentPlatformConfiguration())
			transientConfig = value;
	}
	
	/*
	 * @see IPlatformConfiguration#refresh()
	 */
	public synchronized void refresh() {
		// Reset computed values. Will be lazily refreshed
		// on next access
		ISiteEntry[] sites = getConfiguredSites();
		for (int i = 0; i < sites.length; i++) {
			// reset site entry
			((SiteEntry)sites[i]).refresh();
		}		
		// reset configuration entry.
		lastChangeStamp = changeStamp;
		lastFeaturesChangeStamp = featuresChangeStamp;
		lastPluginsChangeStamp = pluginsChangeStamp;
		changeStampIsValid = false;
		featuresChangeStampIsValid = false;
		pluginsChangeStampIsValid = false;
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
	public synchronized void save(URL url) throws IOException {		
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
	
	URL getPluginPath(String pluginId) {
		return getPluginPath(pluginId, null);
	}	
		
	// This method is currently public because it is called by InternalPlatform.
	// However, it is NOT intended as API
	// FIXME: restructure the code so that InternalBootLoader passes the
	// required information to InternalPlatform
	public URL getPluginPath(String pluginId, String versionId) {
		// return the plugin path element for the specified plugin. This method
		// is used during boot processing to obtain "kernel" plugins whose
		// class loaders must be created prior to the plugin registry being 
		// available (ie. loaders needed to create the plugin registry)
		// must be created 
				
		ISiteEntry[] sites = getConfiguredSites();
		if (sites == null || sites.length == 0)
			return null;
				
		// for now look for the "latest" version of the requested plugin
		// using naming convention of the installer and the policy set for
		// the site
		// FIXME: the current code in this method implements the R1.0 "best guess"
		//        algorithm
		VersionedIdentifier savedVid = new VersionedIdentifier(null);
		String savedEntry = null;
		URL savedURL = null;
		for (int j=0; j<sites.length; j++) {							
			String[] plugins = sites[j].getPlugins();
			for (int i=0; plugins!=null && i<plugins.length; i++) {
				// look for best match. 
				// The entries are in the form <path>/<pluginDir>/plugin.xml
				// look for -------------------------^
				int ix = findEntrySeparator(plugins[i],2); // second from end
				if (ix == -1)
					continue; // bad entry ... skip
				String pluginDir = plugins[i].substring(ix+1);
				ix = pluginDir.indexOf("/");
				if (ix != -1)
					pluginDir = pluginDir.substring(0,ix);
				if (pluginDir.equals(""))
					continue; // bad entry ... skip
												
				VersionedIdentifier vid = new VersionedIdentifier(pluginDir);
				if (vid.identifierEquals(pluginId)) {
					if (vid.compareVersion(savedVid) >= 0) {
						savedVid = vid;
						savedEntry = plugins[i];
						savedURL = ((SiteEntry)sites[j]).getResolvedURL();
					}
				}			
			}				
		}			

		if (savedEntry == null)
			return null;
				
		// callers are expecting a directory URL
		if (!savedEntry.endsWith("/")) {
			int ix = savedEntry.lastIndexOf("/");
			if (ix == -1)
				return null; // bad entry
			savedEntry = savedEntry.substring(0,ix+1); // include trailing separator
		}
			
		try {
			return new URL(savedURL,savedEntry);
		} catch(MalformedURLException e) {
			return null;
		}
	}
	
	static PlatformConfiguration getCurrent() {
		return currentPlatformConfiguration;
	}
	
	/**
	 * Create and initialize the current platform configuration
	 * @param cmdArgs command line arguments (startup and boot arguments are
	 * already consumed)
	 * @param r10plugins plugin-path URL as passed on the BootLoader.run(...)
	 * or BootLoader.startup(...) method. Supported for R1.0 compatibility
	 * @param r10apps application identifies as passed on the BootLoader.run(...)
	 * method. Supported for R1.0 compatibility.
	 */
	static synchronized String[] startup(String[] cmdArgs, URL r10plugins, String r10app) throws Exception {			
		
		// initialize command line settings
		cmdConfiguration = null;
		cmdFeature = null;
		cmdApplication = null;
		cmdPlugins = null;
		cmdPlugins = r10plugins; // R1.0 compatibility
		cmdApplication = r10app; // R1.0 compatibility
		
		String[] passthruArgs = processCommandLine(cmdArgs);
		
		// determine launch mode
		if (cmdPlugins != null) {
			// R1.0 compatibility mode ... explicit plugin-path was specified.
			// Convert the plugins path into a configuration 
			try {
				cmdConfiguration = createConfigurationFromPlugins(cmdPlugins, cmdConfiguration);
			} catch (Exception e) {
				if (DEBUG)
					debug("Unable to use specified plugin-path: "+e);
			}
		}
		
		// create current configuration
		if (currentPlatformConfiguration == null)
			currentPlatformConfiguration = new PlatformConfiguration(cmdConfiguration);
				
		return passthruArgs;
	}
		
	static synchronized void shutdown() throws IOException {

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
		
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		//  NOTE: 
		//       Due to Eclipse class loader structure and class
		//       visibility, the configuration file search is
		//       implemented in 2 different classes:
		//          org.eclipse.core.internal.boot.PlatformConfiguration
		//             getConfigurationURL(String)
		//             initializeCurrent(URL)
		//          org.eclipse.core.launcher.Main
		//             getConfigurationURL(String)
		//             loadConfiguration(URL)
		//       If you are making changes to this method make sure
		//       the change is applied in both places
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
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
			verifyPath(configLocation);	
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
		URL siteURL = null;
		try {
			siteURL = new URL(PlatformURLBaseConnection.PLATFORM_URL_STRING); // try using platform-relative URL
		} catch (MalformedURLException e) {
			siteURL = BootLoader.getInstallURL(); // ensure we come up ... use absolute file URL
		}
		ISiteEntry defaultSite = createSiteEntry(siteURL, defaultPolicy);
		configureSite(defaultSite);
		configLocation = commonURL;
		verifyPath(configLocation);
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
		changeStamp = featuresChangeStamp ^ pluginsChangeStamp;
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
				
		// load simple properties
		defaultFeature = loadAttribute(props, CFG_FEATURE_ENTRY_DEFAULT, null);
		
		String flag = loadAttribute(props, CFG_TRANSIENT, null);
		if (flag != null) {
			if (flag.equals("true"))
				transientConfig = true;
			else
				transientConfig = false;
		}
		
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
		
		// load bootstrap entries
		String[] ids = getBootstrapPluginIdentifiers();
		for (int i=0; i<ids.length; i++) {
			bootPlugins.put(ids[i], loadAttribute(props, CFG_BOOT_PLUGIN + "." + ids[i], null));
		}		
		
		// load feature entries
		IFeatureEntry fe = loadFeatureEntry(props, CFG_FEATURE_ENTRY+".0", null);
		for (int i=1; fe != null; i++) {
			configureFeatureEntry(fe);
			fe = loadFeatureEntry(props, CFG_FEATURE_ENTRY+"."+i, null);
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
	
	private IFeatureEntry loadFeatureEntry(Properties props, String name, IFeatureEntry dflt) {
		String id = loadAttribute(props, name+"."+CFG_FEATURE_ENTRY_ID, null);
		if (id == null)
			return dflt;
		String version = loadAttribute(props, name+"."+CFG_FEATURE_ENTRY_VERSION, null);
		String application = loadAttribute(props, name+"."+CFG_FEATURE_ENTRY_APPLICATION, null);
		String rootString = loadAttribute(props, name+"."+CFG_FEATURE_ENTRY_ROOT, null);
		URL root = null;
		if (rootString != null)
			try {
				root = new URL(rootString);
			} catch (MalformedURLException e) {
			}
		return createFeatureEntry(id, version, application, root);
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
	
	private void loadInitializationAttributes(URL url) {
		
		if (url == null)
			return;
		if (defaultFeature != null)
			return; // already set
								
		// load any initialization attributes. These become the initial default settings
		// for critical attributes (eg. default primary feature) supplied by the packaging team.
		// Once these are reflected in the configuration they cannot be changed via the
		// initialization mechanism
		Properties initProps = new Properties();
		InputStream is = null;
		try {
			URL initURL = new URL(url,"../"+CONFIG_FILE_INIT);
			is = initURL.openStream();
			initProps.load(is);
		} catch(IOException e) {
			return; // could not load "first-time" settings
		} finally {
			if (is!=null) {
				try {
					is.close();
				} catch(IOException e) {
				}
			}
		}
				
		// use "first-time" settings if not already set
		defaultFeature = loadAttribute(initProps, INIT_DEFAULT_FEATURE_ID, null);
		if (defaultFeature != null) {	
			String application = loadAttribute(initProps, INIT_DEFAULT_FEATURE_APPLICATION, null);
			IFeatureEntry fe = createFeatureEntry(defaultFeature, null, application, null);
			configureFeatureEntry(fe);
		}
	}
	
	private boolean isReadWriteLocation(URL url) {
		if (!url.getProtocol().equals("file"))
			return false;
			
		File f = new File(url.getFile());
		return f.canWrite();
	}
	
	private void write(PrintWriter w) {
		// write header
		w.println("# "+(new Date()).toString());
		writeAttribute(w, CFG_VERSION, VERSION);
		if (transientConfig)
			writeAttribute(w,CFG_TRANSIENT,"true");
		w.println("");
		
		// write global attributes
		writeAttribute(w,CFG_STAMP,Long.toString(getChangeStamp()));
		writeAttribute(w,CFG_FEATURE_STAMP,Long.toString(getFeaturesChangeStamp()));
		writeAttribute(w,CFG_PLUGIN_STAMP,Long.toString(getPluginsChangeStamp()));
		
		// write out bootstrap entries
		String[] ids = getBootstrapPluginIdentifiers();
		for (int i=0; i<ids.length; i++) {
			String location = (String) bootPlugins.get(ids[i]);
			if (location != null)
				writeAttribute(w, CFG_BOOT_PLUGIN + "." + ids[i], location);
		}
		
		// write out feature entries
		w.println("");
		writeAttribute(w,CFG_FEATURE_ENTRY_DEFAULT,defaultFeature);
		IFeatureEntry[] feats = getConfiguredFeatureEntries();
		for (int i=0; i<feats.length; i++) {
			writeFeatureEntry(w, CFG_FEATURE_ENTRY + "." + Integer.toString(i), feats[i]);
		}
		
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

	private void writeFeatureEntry(PrintWriter w, String id, IFeatureEntry entry) {
		
		// write feature entry separator
		w.println("");
				
		// write out feature entry settings
		writeAttribute(w, id + "." + CFG_FEATURE_ENTRY_ID, entry.getFeatureIdentifier());
		writeAttribute(w, id + "." + CFG_FEATURE_ENTRY_VERSION, entry.getFeatureVersion());
		writeAttribute(w, id + "." + CFG_FEATURE_ENTRY_APPLICATION, entry.getFeatureApplication());
		writeAttribute(w, id + "." + CFG_FEATURE_ENTRY_ROOT, entry.getFeatureRootURL()==null ? null : entry.getFeatureRootURL().toExternalForm());
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
	
	private static String[] processCommandLine(String[] args) throws Exception {
		int[] configArgs = new int[100];
		configArgs[0] = -1; // need to initialize the first element to something that could not be an index.
		int configArgIndex = 0;
		for (int i = 0; i < args.length; i++) {
			boolean found = false;
		
			// check for args without parameters (i.e., a flag arg)
		
			// currently none defined for PlatformConfiguration
		
			if (found) {
				configArgs[configArgIndex++] = i;
				continue;
			}
		
			// check for args with parameters. If we are at the last argument or if the next one
			// has a '-' as the first character, then we can't have an arg with a parm so continue.
		
			if (i == args.length - 1 || args[i + 1].startsWith("-")) {
				continue;
			}
		
			String arg = args[++i];

			// look for the platform configuration to use.
			if (args[i - 1].equalsIgnoreCase(CMD_CONFIGURATION)) {
				found = true;
				cmdConfiguration = arg;
			}

			// look for the feature to use for customization.
			if (args[i - 1].equalsIgnoreCase(CMD_FEATURE)) {
				found = true;
				cmdFeature = arg;
			}

			// look for the application to run.  Only use the value from the
			// command line if the application identifier was not explicitly
			// passed on BootLoader.run(...) invocation.
			if (args[i - 1].equalsIgnoreCase(CMD_APPLICATION)) {
				found = true;
				if (cmdApplication == null)
					cmdApplication = arg;
			}

			// R1.0 compatibility
			// look for the plugins location to use.  Only use the value from the
			// command line if the plugins location was not explicitly passed on
			// BootLoader.run(...) or BootLoader.startup(...) invocation.
			if (args[i - 1].equalsIgnoreCase(CMD_PLUGINS)) {
				found = true;
				// if the arg can be made into a URL use it.  Otherwise assume that
				// it is a file path so make a file URL.
				try {
					if (cmdPlugins == null)
						cmdPlugins = new URL(arg);
				} catch (MalformedURLException e) {
					try {
						cmdPlugins = new URL("file:" + arg);
					} catch (MalformedURLException e2) {
					}
				}
			}

			// done checking for args.  Remember where an arg was found 
			if (found) {
				configArgs[configArgIndex++] = i - 1;
				configArgs[configArgIndex++] = i;
			}
		}

		// remove all the arguments consumed by this argument parsing
		if (configArgIndex == 0)
			return args;
		String[] passThruArgs = new String[args.length - configArgIndex];
		configArgIndex = 0;
		int j = 0;
		for (int i = 0; i < args.length; i++) {
			if (i == configArgs[configArgIndex])
				configArgIndex++;
			else
				passThruArgs[j++] = args[i];
		}
		return passThruArgs;
	}
		
	private static URL getConfigurationURL(String configArg) throws MalformedURLException {
	
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		//  NOTE: 
		//       Due to Eclipse class loader structure and class
		//       visibility, the configuration file search is
		//       implemented in 2 different classes:
		//          org.eclipse.core.internal.boot.PlatformConfiguration
		//             getConfigurationURL(String)
		//             initializeCurrent(URL)
		//          org.eclipse.core.launcher.Main
		//             getConfigurationURL(String)
		//             loadConfiguration(URL)
		//       If you are making changes to this method make sure
		//       the change is applied in both places
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	
		// Determine configuration URL to use (based on command line argument)		
		// flag: -configuration COMMON | USER.HOME | USER.DIR | <url>
		//        	COMMON		in <eclipse>/install/<cfig>
		//        	USER.HOME	in <user.home>/eclipse/install/<cfig>
		//        	USER.DIR	in <user.dir>/eclipse/install/<cfig>
		//        	<url>		URL as specififed
		String tmp;
		URL result = null;
		if (configArg != null && !configArg.trim().equals("")) {
			if (configArg.equalsIgnoreCase(ARG_USER_DIR)) {
				tmp = System.getProperty("user.dir");
				if (!tmp.endsWith(File.separator))
					tmp += File.separator;
				result = new URL("file:" + tmp.replace(File.separatorChar,'/') + ECLIPSEDIR + "/" + INSTALL + "/" + CONFIG_FILE);
			} else if (configArg.equalsIgnoreCase(ARG_USER_HOME)) {				
				tmp = System.getProperty("user.home");
				if (!tmp.endsWith(File.separator))
					tmp += File.separator;
				result = new URL("file:" + tmp.replace(File.separatorChar,'/') + ECLIPSEDIR + "/" + INSTALL + "/" + CONFIG_FILE);
			} else if (configArg.equalsIgnoreCase(ARG_COMMON)) {				
				result = new URL(BootLoader.getInstallURL(), INSTALL + "/" + CONFIG_FILE);
			} else {
				try {
					result = new URL(configArg);
				} catch(MalformedURLException e) {
					throw new IllegalArgumentException(Policy.bind("cfig.badUrlArg",configArg));
				}
			}
		}
		return result;
	}
	
	/*
	 * R1.0 compatibility mode ... -plugins was specified (possibly with -configuration)
	 */
	private static String createConfigurationFromPlugins(URL file, String cfigCmd) throws Exception {
		// get the actual plugin path
		URL[] pluginPath = BootLoader.getPluginPath(file);
		if (pluginPath == null || pluginPath.length == 0)
			return null;
			
		// create a temp configuration and populate it based on plugin path
		PlatformConfiguration tempConfig = new PlatformConfiguration((URL)null);
		for (int i=0; i<pluginPath.length; i++) {
			String entry = pluginPath[i].toExternalForm();
			String sitePortion;
			String pluginPortion;
			int ix;
			if (entry.endsWith("/")) {
				// assume directory path in the form <site>/plugins/
				// look for -------------------------------^
				ix = findEntrySeparator(entry,2); // second from end
				sitePortion = entry.substring(0,ix+1);
				pluginPortion = entry.substring(ix+1);
				if (!pluginPortion.equals("plugins/"))
					continue; // unsupported entry ... skip it ("fragments/" are handled)
				pluginPortion = null;
			} else {
				// assume full path in the form <site>/<pluginsDir>/<some.plugin>/plugin.xml
				// look for --------------------------^
				ix = findEntrySeparator(entry, 3); // third from end
				sitePortion = entry.substring(0,ix+1);
				pluginPortion = entry.substring(ix+1);
			}
			if (ix == -1)
				continue; // bad entry ... skip it
				
			URL siteURL = null;
			try {
				siteURL = new URL(sitePortion);
			} catch (MalformedURLException e) {
				continue; // bad entry ... skip it
			}
			
			// configure existing site or create a new one for the entry
			ISiteEntry site = tempConfig.findConfiguredSite(siteURL);
			ISitePolicy policy;
			if (site == null) {
				// new site
				if (pluginPortion == null) 
					policy = tempConfig.createSitePolicy(ISitePolicy.USER_EXCLUDE, null);
				else 
					policy = tempConfig.createSitePolicy(ISitePolicy.USER_INCLUDE, new String[] { pluginPortion });
				site = tempConfig.createSiteEntry(siteURL,policy);
				tempConfig.configureSite(site);
			} else {
				// existing site
				policy = site.getSitePolicy();
				if (policy.getType() == ISitePolicy.USER_EXCLUDE)
					continue; // redundant entry ... skip it
				if (pluginPortion == null) {
					// directory entry ... change policy to exclusion (with empty list)
					policy = tempConfig.createSitePolicy(ISitePolicy.USER_EXCLUDE, null);
				} else {
					// explicit entry ... add it to the inclusion list
					ArrayList list = new ArrayList(Arrays.asList(policy.getList()));
					list.add(pluginPortion);
					policy = tempConfig.createSitePolicy(ISitePolicy.USER_INCLUDE,(String[])list.toArray(new String[0]));
				}	
				site.setSitePolicy(policy);
			}				
		}
							
		// check to see if configuration was specified. If specified, will be used to
		// persist the new configuration. Otherwise a transient configuration will be
		// created in temp space.
		URL tmpURL = null;
		if (cfigCmd != null && !cfigCmd.trim().equals("")) {
			try {
				tmpURL = getConfigurationURL(cfigCmd);
				try {
					// attemp to load the specified configuration. If found, merge
					// it with the newly computed one. The merge algorithm includes
					// sites from the old configuration that are not part of the new
					// configuration. Note, that this does not provide for a complete
					// merge, but the assumption is that if -plugins was specified,
					// the sites included in the specification are explicitly
					// controlled.
					PlatformConfiguration oldConfig = new PlatformConfiguration(tmpURL);
					ISiteEntry[] oldSites = oldConfig.getConfiguredSites();
					for (int i=0; i<oldSites.length; i++) {
						tempConfig.configureSite(oldSites[i], false /*do not replace*/);
					}
				} catch(IOException e) {
				}
			} catch(MalformedURLException e) {
			}
		}
		
		if (tmpURL == null) {		
			// save the configuration in temp location
			String tmpDirName = System.getProperty("java.io.tmpdir");
			if (!tmpDirName.endsWith(File.separator))
				tmpDirName += File.separator;
			tmpDirName += Long.toString((new Date()).getTime()) + File.separator;
			File tmpDir = new File(tmpDirName);
			tmpDir.mkdirs();
			tmpDir.deleteOnExit();
			File tmpCfg = File.createTempFile("platform",".cfg",tmpDir);
			tmpCfg.deleteOnExit();
			tmpURL = new URL("file:" + tmpCfg.getAbsolutePath().replace(File.separatorChar, '/'));
			tempConfig.transientConfig = true;
		}
		
		// force writing null stamps
		ISiteEntry[] se = tempConfig.getConfiguredSites();
		for (int i=0; i<se.length; i++) {
			((SiteEntry)se[i]).changeStampIsValid = true;
			((SiteEntry)se[i]).pluginsChangeStampIsValid = true;
			((SiteEntry)se[i]).featuresChangeStampIsValid = true;
		}
		tempConfig.changeStampIsValid = true;
		tempConfig.pluginsChangeStampIsValid = true;
		tempConfig.featuresChangeStampIsValid = true;
		
		// write out configuration
		tempConfig.save(tmpURL); // write the temporary configuration we just created

		
		// return reference to new configuration
		return tmpURL.toExternalForm();
	}
	
	private static int findEntrySeparator(String pathEntry, int cnt) {
		for (int i=pathEntry.length()-1; i>=0; i--) {
			if (pathEntry.charAt(i) == '/') {
				if (--cnt == 0)
					return i;
			}
		}
		return -1;
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
		String protocol = url.getProtocol();
		if (protocol.equals("file"))
			return true;
		else if (protocol.equals(PlatformURLHandler.PROTOCOL)) {
			URL resolved = null;
			try {
				resolved = ((PlatformURLConnection)url.openConnection()).getResolvedURL();
			} catch(IOException e) {
				return false; // we tried but failed to resolve the platform URL
			}
			return resolved.getProtocol().equals("file");
		} else
			return false;
	}
	
	private static void verifyPath(URL url) {
		String protocol = url.getProtocol();
		String path = null;
		if (protocol.equals("file"))
			path = url.getFile();
		else if (protocol.equals(PlatformURLHandler.PROTOCOL)) {
			URL resolved = null;
			try {
				resolved = ((PlatformURLConnection)url.openConnection()).getResolvedURL();
				if (resolved.getProtocol().equals("file"))
					path = resolved.getFile();
			} catch(IOException e) {
			}
		} 
		
		if (path != null) {
			File dir = new File(path).getParentFile();
			if (dir != null)
				dir.mkdirs();
		}
	}

	private static void debug(String s) {
		System.out.println("PlatformConfig: " + s);
	}
}