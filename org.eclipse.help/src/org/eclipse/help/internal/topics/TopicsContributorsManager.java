/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.topics;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.util.*;
/**
 * Contributions manager
 */
public class TopicsContributorsManager {
	// Versioning info for detecting changes
	protected static final String PLUGIN_VERSION_FILE = "contributors";
	protected PluginVersionInfo pluginVersions;
	protected List contributingPlugins = new ArrayList();
	protected List contributedTopicsFiles;
	/**
	 * Constructor
	 */
	public TopicsContributorsManager() {
		super();
		// read extension point and retrieve all contributions
		IExtensionPoint xpt =
			Platform.getPluginRegistry().getExtensionPoint("org.eclipse.help.topics");
		if (xpt == null)
			return; // no contributions...
		IExtension[] extensions = xpt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IPluginDescriptor plugin = extensions[i].getDeclaringPluginDescriptor();
			if (!contributingPlugins.contains(plugin))
				contributingPlugins.add(plugin);
		}
	}
	/**
	 * @return java.util.Iterator
	 */
	protected Iterator getContributingPlugins() {
		if (contributingPlugins == null)
			return null;
		else
			return contributingPlugins.iterator();
	}
	/**
	 * @return boolean
	 */
	public boolean hasNewContributors() {
		if (pluginVersions == null) {
			// see what has changed since last time
			pluginVersions =
				new PluginVersionInfo(
					"nl/" + Locale.getDefault().toString() + "/" + PLUGIN_VERSION_FILE,
					getContributingPlugins());
		}
		return pluginVersions.detectChange();
	}
	/**
	 */
	public void versionContributors() {
		pluginVersions.save();
	}

	/**
	* Returns a collection of TopicsFile that were not processed.
	*/
	protected Collection getContributedTopicsFiles() {
		if (contributedTopicsFiles != null)
			return contributedTopicsFiles;

		contributedTopicsFiles = new ArrayList();

		// find extension point
		IExtensionPoint xpt =
			Platform.getPluginRegistry().getExtensionPoint("org.eclipse.help", "topics");
		if (xpt == null)
			return contributedTopicsFiles;
		// get all extensions
		IExtension[] extensions = xpt.getExtensions();

		for (int i = 0; i < extensions.length; i++) {
			// add to TopicFiles declared in this extension
			IConfigurationElement[] configElements =
				extensions[i].getConfigurationElements();

			// verify the prereqs
			boolean prereqsSatisfied = true;
			for (int j = 0; j < configElements.length; j++)
				if (configElements[j].getName().equals("requires")) {
					// see if required plugin is installed
					String reqPlugin = configElements[j].getAttribute("plugin");
					prereqsSatisfied &= Platform.getPluginRegistry().getPluginDescriptor(reqPlugin)
						!= null;
				}

			if (!prereqsSatisfied)
				continue;

			for (int j = 0; j < configElements.length; j++)
				if (configElements[j].getName().equals("topics")) {
					String pluginId =
						configElements[j]
							.getDeclaringExtension()
							.getDeclaringPluginDescriptor()
							.getUniqueIdentifier();
					String href = configElements[j].getAttribute("href");
					contributedTopicsFiles.add(new TopicsFile(pluginId, href));
				}
		}

		return contributedTopicsFiles;
	}

}