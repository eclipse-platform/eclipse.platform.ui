package org.eclipse.help.internal.contributors.xml;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.contributors.*;
import org.eclipse.help.internal.contributions.*;

/**
 * Contributions manager
 */
public class HelpContributionManager implements ContributionManager {
	// Versioning info for detecting changes
	protected static final String PLUGIN_VERSION_FILE =
		Resources.getString("contribution_versions");
	protected PluginVersionInfo pluginVersions = null;

	// Manifest contributions
	protected List contributors = new ArrayList();
	protected List contributingPlugins = null;
	public HelpContributionManager() {
		super();
		// create the help contributors based on plugin.xml files
		createContributors();

	}
	/**
	 * Create a contributor (for actions,topics, or infosets)
	 */
	private Contributor createContributor(
		IPluginDescriptor plugin,
		IConfigurationElement contribution) {
		return ContributorFactory.getFactory().createContributor(plugin, contribution);
	}
	/**
	 * Creates the list of contributors for each help contributions
	 */
	private void createContributors() {
		// read extension point and retrieve all contributions
		IExtensionPoint xpt =
			Platform.getPluginRegistry().getExtensionPoint(CONTRIBUTION_EXTENSION);
		if (xpt == null)
			return; // no contributions...

		IExtension[] extensions = xpt.getExtensions();
		//// (1) For now, we keep track of all the plugins with contributions
		/// including those without manifest
		contributingPlugins = new ArrayList(extensions.length);

		for (int i = 0; i < extensions.length; i++) {
			IPluginDescriptor plugin = extensions[i].getDeclaringPluginDescriptor();
			contributingPlugins.add(plugin); //// see above comment (1)
			IConfigurationElement[] pluginContributions =
				extensions[i].getConfigurationElements();
			for (int j = 0; j < pluginContributions.length; j++) {
				///if (MapContributor.MAP_ELEM.equals(pluginContributions[j].getName())) continue;
				Contributor contributor = createContributor(plugin, pluginContributions[j]);
				if (contributor != null) {
					contributors.add(contributor);
				}
			}
		}
	}
	/**
	 * @return java.util.Vector
	 */
	public Iterator getContributingPlugins() {
		/*
		/////NOTE: Uncomment this later, when only want to know the real contributors
		//// Currently all the plugins with a contribution, even without manifests are returned.
		//// This is done to allow migration from old manifests to new format....
		
		if (contributingPlugins == null)
		{
			contributingPlugins = new Vector(contributors.size());
			for (Enumeration e = contributors.elements(); e.hasMoreElements(); )
			{
				IPluginDescriptor pluginDescriptor = ((Contributor) e.nextElement()).getPlugin();
				if (!contributingPlugins.contains(pluginDescriptor))
				{
					contributingPlugins.addElement(pluginDescriptor);
				}
			}
		}
		*/
		if (contributingPlugins == null)
			return null;
		else
			return contributingPlugins.iterator();
	}
	/**
		 * Get contributions of some type, like topics, actions or views
		 * @return java.util.Iterator
		 * @param typeName java.lang.String
		 */
	public Iterator getContributionsOfType(String typeName) {
		List contribDocs = new ArrayList();
		for (Iterator it = contributors.iterator(); it.hasNext();) {
			Contributor c = (Contributor) it.next();
			if (c.getType().equals(typeName)) { // CASE SENSITIVE
				Contribution contrib = c.getContribution();
				if (contrib != null) {
					contribDocs.add(contrib);
				}
			}
		}

		return contribDocs.iterator();
	}
	/**
	 * @return boolean
	 */
	public boolean hasNewContributions() {
		if (HelpSystem.isClient())
			// contributions might have changed on a client
			// but it is ignored, as caller should
			// obtain everything from the server
			return false;
			
		if (pluginVersions == null) {
			// see what has changed since last time
			pluginVersions =
				new PluginVersionInfo(PLUGIN_VERSION_FILE, getContributingPlugins());
		}
		return pluginVersions.detectChange();
	}
	/**
	 * Insert the method's description here.
	 * 
	 */
	public void versionContributions() {
		pluginVersions.save();
	}
}
