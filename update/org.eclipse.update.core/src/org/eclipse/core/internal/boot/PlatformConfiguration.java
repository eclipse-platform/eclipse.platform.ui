package org.eclipse.core.internal.boot;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.boot.IPlatformConfiguration.IPluginEntry;
import org.eclipse.core.boot.IPlatformConfiguration.ISiteEntry;

public class PlatformConfiguration implements IPlatformConfiguration {
	
	private URL configLocation;
	private HashMap sites;
	private HashMap sitesNative;
	private int changeStatus;
	
	public static boolean DEBUG = true;
	
	private static final String PLUGINS = "plugins";
	private static final String INSTALL = "install";
	private static final String CONFIG_FILE = "platform.cfg";
	private static final String FEATURES = INSTALL+"/features";
	private static final String LINKS = INSTALL+"/links";
	private static final String PLUGIN_XML = "plugin.xml";
	private static final String FRAGMENT_XML = "fragment.xml";
	
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
	
	PlatformConfiguration() {			
		this.sites = new HashMap();
		this.sitesNative = new HashMap();
		this.changeStatus = initialize();
		
		if (DEBUG)
			debug("change status="+Integer.toString(this.changeStatus));
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
	 * @see IPlatformConfiguration#getChangeStatus()
	 */
	public int getChangeStatus() {
		return changeStatus;
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
		
	private int initialize() {
		
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
		setupDefaultConfiguration();
		
		// detect links
		
		// detect changes
		int changeStatus = detectChanges();
		
		// if (feature changes)
		// 	trigger alternate startup (into update app)
		// else 
		//	process any plugin-only changes
		
		return changeStatus;
	}
	
	private int detectChanges() {
		
		if (sites.size() == 0)
			return NO_CHANGES;
					
		// do a pass trying to detect feature changes. Exist on first change
		Iterator site = sites.values().iterator();
		SiteEntry se;
		while(site.hasNext()) {
			se = (SiteEntry) site.next();
			if (detectFeatureChanges(se))
				return FEATURE_CHANGES;
		}
		
		// look for plugin changes	
		boolean pluginChanges = false;
		site = sites.values().iterator();
		while(site.hasNext()) {
			se = (SiteEntry) site.next();
			pluginChanges |= detectPluginChanges(se);
		}	
		
		if (pluginChanges)
			return PLUGIN_CHANGES;
		else
			return NO_CHANGES;
	}
	
	private boolean detectFeatureChanges(SiteEntry se) {
		return false;
	}
	
	private boolean detectPluginChanges(SiteEntry se) {
		
		boolean pluginChanges = false;
		
		// ensure site uses "auto-discover" policy
		if (se.getPolicy()!= IPlatformConfiguration.ISiteEntry.USER_CONFIGURED_PLUS_CHANGES)
			return false;
			
		// ensure sire supports "auto-discovery"
		URL siteURL = se.getURL();
		if (!supportsDiscovery(siteURL))
			return false;
			
		// locate plugin entries on site
		ArrayList plugins = new ArrayList();
		File root = new File(siteURL.getFile().replace('/',File.separatorChar)+PLUGINS);
		String[] list = root.list();
		String path;
		File plugin;
		for (int i=0; i<list.length; i++) {
			path = list[i]+File.separator+PLUGIN_XML;
			plugin = new File(root,path);
			if (!plugin.exists()) {
				path = list[i]+File.separator+FRAGMENT_XML;
				plugin = new File(root,path);
				if (!plugin.exists())
					continue;
			}
			plugins.add(createPluginEntry(path.replace(File.separatorChar,'/')));
		}
				
		// check for additions ... picked up as unmanaged
		Iterator i = plugins.iterator();
		PluginEntry pe;
		while (i.hasNext()) {
			pe = (PluginEntry) i.next();
			path = pe.getRelativePath();
			if (se.plugins.containsKey(path))
				continue;
			if (se.pluginsUncfgd.containsKey(path))
				continue;
			if (se.pluginsUnmgd.containsKey(path))
				continue;
			if (DEBUG)
				debug("add unmanaged plug-in entry "+path);
			pluginChanges = true;
			se.pluginsUnmgd.put(path,pe);			
		}
		
		// check for deletions from configured list ... removed
		i = ((HashMap)se.plugins.clone()).keySet().iterator();
		while (i.hasNext()) {
			pe = (PluginEntry) i.next();
			if (!plugins.contains(pe)) {
				if (DEBUG)
					debug("remove configured plug-in entry "+pe.getRelativePath());
				pluginChanges = true;
				se.plugins.remove(pe);
			}
		}
		
		// check for deletions from unconfigured list ... removed
		i = ((HashMap)se.pluginsUncfgd.clone()).keySet().iterator();
		while (i.hasNext()) {
			pe = (PluginEntry) i.next();
			if (!plugins.contains(pe)) {
				if (DEBUG)
					debug("remove unconfigured plug-in entry "+pe.getRelativePath());
				pluginChanges = true;
				se.pluginsUncfgd.remove(pe);
			}
		}
		
		// check for deletions from unmanaged list ... removed
		i = ((HashMap)se.pluginsUnmgd.clone()).keySet().iterator();
		while (i.hasNext()) {
			pe = (PluginEntry) i.next();
			if (!plugins.contains(pe)) {
				if (DEBUG)
					debug("remove unmanaged plug-in entry "+pe.getRelativePath());
				pluginChanges = true;
				se.pluginsUnmgd.remove(pe);
			}
		}
			
		return pluginChanges;
	}
	
	private boolean supportsDiscovery(URL url) {
		return url.getProtocol().equals("file");
	}
	
	private void setupDefaultConfiguration() {
		// default initial startup configuration:
		// site: current install, policy: AUTO_DISCOVER
		ISiteEntry se = createSiteEntry(BootLoader.getInstallURL(), IPlatformConfiguration.ISiteEntry.USER_CONFIGURED_PLUS_CHANGES);
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
	
	private static void debug(String s) {
		System.out.println("PlatformConfiguration: "+s);
	}
}

