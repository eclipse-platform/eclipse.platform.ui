package org.eclipse.core.internal.boot;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.boot.IPlatformConfiguration.IPluginEntry;
import org.eclipse.core.boot.IPlatformConfiguration.ISiteEntry;

public class PlatformConfiguration implements IPlatformConfiguration {
	
	private static PlatformConfiguration config;
	private URL configLocation;
	private HashMap sites;
	private HashMap sitesNative;
	
	public static boolean DEBUG = true;
	
	public static final int NO_CHANGES = 0;
	public static final int PLUGIN_CHANGES = 1;
	public static final int FEATURE_CHANGES = 2;
	
	private static final String PLUGINS = "plugins";
	private static final String INSTALL = "install";
	private static final String CONFIG_FILE = "platform.cfg";
	private static final String FEATURES = INSTALL+"/features";
	private static final String LINKS = INSTALL+"/links";
	
	private static final String CFG_SITE = "site";
	private static final String CFG_URL = "url";
	private static final String CFG_POLICY = "policy";
	private static final String CFG_CONFIGURED = "configured";
	private static final String CFG_UNCONFIGURED = "unconfigured";
	private static final String CFG_UNMANAGED = "unmanaged";
	private static final String EOF = "eof";
	private static final int CFG_LIST_LENGTH = 10;
	
	public class SiteEntry implements IPlatformConfiguration.ISiteEntry {
		
		private URL url;
		private int policy;
		
		private HashMap plugins;
		private HashMap pluginsUncfgd;
		private HashMap pluginsUnmgd;
		
		private SiteEntry() {}
		private SiteEntry(URL url, int policy) {
			this.url = url;
			this.policy = policy;
			this.plugins = new HashMap();
			this.pluginsUncfgd = new HashMap();
			this.pluginsUnmgd = new HashMap();
		}

		/*
		 * @see ISiteEntry#getURL()
		 */
		public URL getURL() {
			return url;
		}

		/*
		 * @see ISiteEntry#getPolicy()
		 */
		public int getPolicy() {
			return policy;
		}

		/*
		 * @see ISiteEntry#configurePluginEntry(IPluginEntry)
		 */
		public void configurePluginEntry(IPluginEntry entry) {
			if (entry == null)
				return;
				
			String key = entry.getRelativePath();
			if (key == null)
				return;
				
			if (!plugins.containsKey(key))
				plugins.put(key,entry);
		}

		/*
		 * @see ISiteEntry#unconfigurePluginEntry(IPluginEntry)
		 */
		public void unconfigurePluginEntry(IPluginEntry entry) {
			if (entry == null)
				return;
				
			String key = entry.getRelativePath();
			if (key == null)
				return;
				
			plugins.remove(key);
		}		
			
		/*
		 * @see ISiteEntry#getConfiguredPluginEntries()
		 */
		public IPluginEntry[] getConfiguredPluginEntries() {
			if (plugins.size() == 0)
				return new IPluginEntry[0];
				
			return (IPluginEntry[]) plugins.values().toArray();
		}

		/*
		 * @see ISiteEntry#isConfigured(IPluginEntry)
		 */
		public boolean isConfigured(IPluginEntry entry) {
			if (entry == null)
				return false;
				
			String key = entry.getRelativePath();
			if (key == null)
				return false;
				
			return plugins.containsKey(key);
		}

}
	
	public class PluginEntry implements IPlatformConfiguration.IPluginEntry {
		
		private String path;
		
		private PluginEntry() {}
		private PluginEntry(String path) {
			this.path = path;
		}
		
			/*
		 * @see IPluginEntry#getRelativePath()
		 */
		public String getRelativePath() {
			return path;
		}

}
	
	private PlatformConfiguration() {
		if (config == null)
			config = this;
			
		this.sites = new HashMap();
		this.sitesNative = new HashMap();
	}

	/*
	 * @see IPlatformConfiguration#createPluginEntry(String)
	 */
	public IPluginEntry createPluginEntry(String path) {
		return new PlatformConfiguration.PluginEntry(path);
	}

	/*
	 * @see IPlatformConfiguration#createSiteEntry(URL, int)
	 */
	public ISiteEntry createSiteEntry(URL url, int policy) {
		return new PlatformConfiguration.SiteEntry(url, policy);
	}
	
	/*
	 * @see IPlatformConfiguration#configureSiteEntry(ISiteEntry)
	 */
	public void configureSiteEntry(ISiteEntry entry) {
		configureSiteEntry(entry, false);
	}

	/*
	 * @see IPlatformConfiguration#configureSiteEntry(ISiteEntry, boolean)
	 */
	public void configureSiteEntry(ISiteEntry entry, boolean replace) {
		
		if (entry == null)
			return;
				
		URL key = entry.getURL();
		if (key == null)
			return;
				
		if (sites.containsKey(key) && !replace)
			return;
		
		sites.put(key,entry);
	}

	/*
	 * @see IPlatformConfiguration#unconfigureSiteEntry(ISiteEntry)
	 */
	public void unconfigureSiteEntry(ISiteEntry entry) {
		if (entry == null)
			return;
				
		URL key = entry.getURL();
		if (key == null)
			return;
				
		sites.remove(key);
	}

	/*
	 * @see IPlatformConfiguration#getConfiguredSiteEntries()
	 */
	public ISiteEntry[] getConfiguredSiteEntries() {
		if (sites.size() == 0)
			return new ISiteEntry[0];
				
		return (ISiteEntry[]) sites.values().toArray();
	}

	/*
	 * @see IPlatformConfiguration#findConfiguredSiteEntry(URL)
	 */
	public ISiteEntry findConfiguredSiteEntry(URL url) {
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
	 * @see IPlatformConfiguration#save()
	 */
	public void save() throws IOException {
		
		String fn = configLocation.getFile();
		FileOutputStream fs = new FileOutputStream(fn);
		PrintWriter w = new PrintWriter(fs);
		write(w);
		w.close();
	}
		
	static int startup() {
		
		PlatformConfiguration current = new PlatformConfiguration();
		
		// check for safe mode
		// flag: -safe		come up in safe mode (loads configuration
		//                  but compute "safe" plugin path
		
		// determine configuration to use
		// flag: -config COMMON | HOME | CURRENT | <path>
		//        COMMON	in <eclipse>/install/<cfig>
		//        HOME		in <user.home>/eclipse/install/<cfig>
		//        CURRENT	in <user.dir>/eclipse/install/<cfig>
		//        <path>	as specififed
		//        SAFE		come up in SAFE mode
		// if none specified, search order for configuration is
		// CURRENT, HOME, COMMON
		// if none found new configuration is created in the first
		// r/w location in order of COMMON, HOME, CURRENT
				
		// load configuration. 
		
		// if none found, do default setup
		current.setupDefaultConfiguration();
		
		// detect links
		
		// detect changes
		int changeStatus = current.detectChanges();
		
		// if (feature changes)
		// 	trigger alternate startup (into update app)
		// else 
		//	process any plugin-only changes
		
		// we are configured at this point
		config = current;
		
		return changeStatus;
	}
	
	static void shutdown() {
		PlatformConfiguration current = getCurrent();
		if (current == null)
			return;
			
		try {
			current.save();
		} catch (IOException e) {
		}
	}
	
	static PlatformConfiguration getCurrent() {
		return config;
	}
		
	private static void debug(String s) {
		System.out.println("PlatformConfiguration: "+s);
	}
	
	private int detectChanges() {
		
		if (sites.size() == 0)
			return NO_CHANGES;
					
		Iterator site = sites.values().iterator();
		SiteEntry se;
		while(site.hasNext()) {
			se = (SiteEntry) site.next();			
		}		
		
		return NO_CHANGES;
	}
	
	private void setupDefaultConfiguration() {
		// default initial startup configuration:
		// site: current install, policy: AUTO_DISCOVER
		ISiteEntry se = createSiteEntry(BootLoader.getInstallURL(), IPlatformConfiguration.ISiteEntry.AUTO_DISCOVER_CHANGES);
		configureSiteEntry(se);
		try {
			configLocation = new URL(BootLoader.getInstallURL(),INSTALL+"/"+CONFIG_FILE);
		} catch(MalformedURLException e) {
		}
		if (DEBUG)
			debug("creating default configuration "+configLocation.getFile());
	}
	
	private void write(PrintWriter w) {
		SiteEntry[] list = (SiteEntry[]) sites.values().toArray();
		if (list.length == 0)
			return;
			
		for (int i=0; i<list.length; i++) {
			writeSite(w, CFG_SITE+"."+Integer.toString(i), list[i]);
		}
		writeAttribute(w,EOF,EOF);		
	}
	
	private void writeSite(PrintWriter w, String id, SiteEntry entry) {
		writeAttribute(w,id+"."+CFG_URL,entry.getURL().toString());
		writeAttribute(w,id+"."+CFG_POLICY,Integer.toString(entry.getPolicy()));
		writePluginMap(w,id+"."+CFG_CONFIGURED,entry.plugins);
		writePluginMap(w,id+"."+CFG_UNCONFIGURED,entry.pluginsUncfgd);
		writePluginMap(w,id+"."+CFG_UNMANAGED,entry.pluginsUnmgd);
	}
	
	private void writePluginMap(PrintWriter w, String id, Map map) {
		if (map.size() == 0)
			return;
			
		PluginEntry[] list = (PluginEntry[]) map.values().toArray();
		String value = "";
		int listLen = 0;
		int listIndex = 0;
		for (int i=0; i<list.length; i++) {
			if (listLen!=0)
				value += ",";
			else value = "";
			value += list[i].getRelativePath();
			
			if (listLen++ > CFG_LIST_LENGTH) {
				writeAttribute(w,id+"."+Integer.toString(listIndex++),value);
				listLen = 0;	
			}
		}
		if (listLen != 0)
			writeAttribute(w,id+"."+Integer.toString(listIndex),value);
	}
	
	private void writeAttribute(PrintWriter w, String id, String value) {
		w.println(id+"="+escapedValue(value));
	}
	
	private String escapedValue(String value) {
		// FIXME: implement escaping for property file
		return value;
	}	
}

