/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.toc;
import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.contributions.xml1_0.HelpInfoView;
import org.eclipse.help.internal.util.*;
/**
 * Manages the navigation model. It generates it and it reads it back
 * and instantiates the model for future rendering.
 * There is a model (notifier) for each <views> node.
 */
public class TocManager {
	private Map idToToc =
		new HashMap(/* of ID to Toc */
	);
	// Ordered List of all TOC available;
	private List tocIDs;
	/**
	 * HelpNavigationManager constructor.
	 */
	public TocManager() {
		super();
		try {
			build();
		} catch (Exception e) {
			Logger.logError("", e);
		}
	}
	private void build() {
		try {
			Collection contributedTocFiles = getContributedTocFiles();
			TocBuilder builder = new TocBuilder();
			builder.build(contributedTocFiles);
			Collection builtTocs = builder.getBuiltTocs();
			for (Iterator it = builtTocs.iterator(); it.hasNext();) {
				Toc toc = (Toc) it.next();
				idToToc.put(toc.getTocID(), toc);
			}
			// 1.0 navigation support
			Collection builtTopics10 = HelpSystem.getNavigationManager().getTopicsIDs();
			for (Iterator it = builtTopics10.iterator(); it.hasNext();) {
				String id= (String) it.next();
				idToToc.put(id, HelpSystem.getNavigationManager().getTopics(id));
			}
			// eo 1.0 nava support
		} catch (Exception e) {
			Logger.logError("", e);
		}
	}
	/**
	 * Returns the navigation model for specified toc
	 */
	public ITopic getToc(String id) {
		if (id == null || id.equals(""))
			return null;
		return (ITopic) idToToc.get(id);
	}
	/**
	 * @return List of Toc hrefs available, not including
	 * ones that do not have navigation
	 * (i.e. require not met)
	 */
	public List getTocIDs() {
		if (tocIDs != null)
			return tocIDs;
		tocIDs = new ArrayList(idToToc.size());
		// obtain unordered hrefs
		List unorderedIDs = new ArrayList(idToToc.size());
		unorderedIDs.addAll(idToToc.keySet());
		// Now create ordered list, as specified in product.ini
		List preferedOrder = getPreferedInfosetsOrder();
		// add all infosets that have order specified
		if (preferedOrder != null) {
			for (Iterator it = preferedOrder.iterator(); it.hasNext();) {
				String infosetID = (String) it.next();
				if (unorderedIDs.contains(infosetID))
					tocIDs.add(infosetID);
				unorderedIDs.remove(infosetID);
				// 1.0 nav support
				for (Iterator it2 = unorderedIDs.iterator(); it2.hasNext();) {
					String infosetDDView = (String) it2.next();
					if (infosetDDView.startsWith(infosetID + "..")) {
						tocIDs.add(infosetDDView);
						unorderedIDs.remove(infosetDDView);
						// iterator is dirty, start again
						it2 = unorderedIDs.iterator();
						continue;
					}
				}
				// oe 1.0 nav support
			}
		}
		// add the rest of infosets
		tocIDs.addAll(unorderedIDs);
		return tocIDs;
	}
	/**
	 * Returns the label for Toc.
	 * This method uses the label from the toc map file
	 * so that the navigation file does not need to be
	 * read in memory
	 */
	public String getTocLabel(String href) {
		Object toc = idToToc.get(href);
		if (toc != null)
			return ((ITopic) toc).getLabel();
		return null;
	}
	/**
	 * Reads product.ini to determine infosets ordering.
	 * It works in current drivers, but will not
	 * if location/name of product.ini change
	 */
	private List getPreferedInfosetsOrder() {
		List infosets = new ArrayList();
		try {
			Plugin p = Platform.getPlugin("org.eclipse.sdk");
			if (p == null)
				return infosets;
			URL pURL = p.getDescriptor().getInstallURL();
			URL productIniURL = new URL(pURL, "product.ini");
			Properties prop = new Properties();
			prop.load(productIniURL.openStream());
			String infosetsList = prop.getProperty("baseInfosets");
			if (infosetsList != null) {
				StringTokenizer suggestdOrderedInfosets =
					new StringTokenizer(infosetsList, " ;,");
				while (suggestdOrderedInfosets.hasMoreElements()) {
					String infoset = (String) suggestdOrderedInfosets.nextElement();
					infosets.add(infoset);
				}
			}
		} catch (Exception e) {
			Logger.logWarning(Resources.getString("W001"));
		}
		return infosets;
	}
	/**
	* Returns a collection of TocFile that were not processed.
	*/
	protected Collection getContributedTocFiles() {
		Collection contributedTocFiles = new ArrayList();
		// find extension point
		IExtensionPoint xpt =
			Platform.getPluginRegistry().getExtensionPoint("org.eclipse.help", "toc");
		if (xpt == null)
			return contributedTocFiles;
		// get all extensions
		IExtension[] extensions = xpt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			// add to TopicFiles declared in this extension
			IConfigurationElement[] configElements =
				extensions[i].getConfigurationElements();
			for (int j = 0; j < configElements.length; j++)
				if (configElements[j].getName().equals("toc")) {
					String pluginId =
						configElements[j]
							.getDeclaringExtension()
							.getDeclaringPluginDescriptor()
							.getUniqueIdentifier();
					String href = configElements[j].getAttribute("file");
					boolean isPrimary = "true".equals(configElements[j].getAttribute("primary"));
					if (href != null)
						contributedTocFiles.add(new TocFile(pluginId, href, isPrimary));
				}
		}
		return contributedTocFiles;
	}
}