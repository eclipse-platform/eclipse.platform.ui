package org.eclipse.core.internal.boot;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.boot.IPlatformConfiguration.IPluginEntry;
import org.eclipse.core.boot.IPlatformConfiguration.ISiteEntry;

public class PlatformConfiguration implements IPlatformConfiguration {
	
	public class SiteEntry implements IPlatformConfiguration.ISiteEntry {
		
		private URL url;
		private int policy;
		
		private SiteEntry() {}
		private SiteEntry(URL url, int policy) {
			this.url = url;
			this.policy = policy;
		}
			
		/*
		 * @see ISiteEntry#createPluginEntry(String, String, String, boolean)
		 */
		public IPluginEntry createPluginEntry(
			String id,
			String version,
			String path,
			boolean configure) {
				
			PluginEntry pe = new PluginEntry(id, version, this, path);
			if (configure)
				configurePluginEntry(pe);
			return pe;
		}

			/*
		 * @see ISiteEntry#getConfiguredPluginEntries()
		 */
		public IPluginEntry[] getConfiguredPluginEntries() {
			return new IPluginEntry[0];
		}

		/*
		 * @see ISiteEntry#getURL()
		 */
		public URL getURL() {
			return null;
		}

		/*
		 * @see ISiteEntry#getPolicy()
		 */
		public int getPolicy() {
			return 0;
		}

}
	
	public class PluginEntry implements IPlatformConfiguration.IPluginEntry {
		
		private String id;
		private String version;
		private SiteEntry site;
		private String path;
		
		private PluginEntry() {}
		private PluginEntry(String id, String version, SiteEntry site, String path) {
			this.id = id;
			this.version = version;
			this.site = site;
			this.path = path;
		}
		
		/*
		 * @see IPluginEntry#getSiteEntry()
		 */
		public ISiteEntry getSiteEntry() {
			return this.site;
		}

			/*
		 * @see IPluginEntry#getRelativePath()
		 */
		public String getRelativePath() {
			return this.path;
		}

		/*
		 * @see IPluginEntry#getVersion()
		 */
		public String getVersion() {
			return this.version;
		}

		/*
		 * @see IPluginEntry#getUniqueIdentifier()
		 */
		public String getUniqueIdentifier() {
			return this.id;
		}

}
	
	/*
	 * @see IPlatformConfiguration#createSiteEntry(URL, int, boolean)
	 */
	public ISiteEntry createSiteEntry(URL url, int policy, boolean configure) {
		SiteEntry se = new SiteEntry(url, policy);
		if (configure)
			configureSiteEntry(se);
		return se;
	}

	/*
	 * @see IPlatformConfiguration#configureSiteEntry(ISiteEntry)
	 */
	public void configureSiteEntry(ISiteEntry entry) {
	}

	/*
	 * @see IPlatformConfiguration#configureSiteEntry(ISiteEntry, boolean)
	 */
	public void configureSiteEntry(ISiteEntry entry, boolean replace) {
	}

	/*
	 * @see IPlatformConfiguration#unconfigureSiteEntry(ISiteEntry)
	 */
	public void unconfigureSiteEntry(ISiteEntry entry) {
	}

	/*
	 * @see IPlatformConfiguration#getConfiguredSiteEntries()
	 */
	public ISiteEntry[] getConfiguredSiteEntries() {
		return new ISiteEntry[0];
	}

	/*
	 * @see IPlatformConfiguration#findConfiguredSiteEntry(URL)
	 */
	public ISiteEntry findConfiguredSiteEntry(URL url) {
		return null;
	}

	/*
	 * @see IPlatformConfiguration#isConfigured(ISiteEntry)
	 */
	public boolean isConfigured(ISiteEntry entry) {
		return false;
	}

	/*
	 * @see IPlatformConfiguration#configurePluginEntry(IPluginEntry)
	 */
	public void configurePluginEntry(IPluginEntry entry) {
	}

	/*
	 * @see IPlatformConfiguration#unconfigurePluginEntry(IPluginEntry)
	 */
	public void unconfigurePluginEntry(IPluginEntry entry) {
	}

	/*
	 * @see IPlatformConfiguration#getConfiguredPluginEntries()
	 */
	public IPluginEntry[] getConfiguredPluginEntries() {
		return new IPluginEntry[0];
	}

	/*
	 * @see IPlatformConfiguration#findConfiguredPluginEntries(String)
	 */
	public IPluginEntry[] findConfiguredPluginEntries(String id) {
		return new IPluginEntry[0];
	}

	/*
	 * @see IPlatformConfiguration#findConfiguredPluginEntries(String, String)
	 */
	public IPluginEntry[] findConfiguredPluginEntries(String id, String version) {
		return new IPluginEntry[0];
	}

	/*
	 * @see IPlatformConfiguration#isConfigured(IPluginEntry)
	 */
	public boolean isConfigured(IPluginEntry entry) {
		return false;
	}

	/*
	 * @see IPlatformConfiguration#save()
	 */
	public void save() throws IOException {
	}

}

