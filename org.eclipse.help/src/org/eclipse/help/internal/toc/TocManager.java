/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.toc;
import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.util.*;
/**
 * Manages the navigation model. It keeps track of all the tables of contents.
 */
public class TocManager {
	
	private IToc[] tocs;

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

	/**
	 * Returns the list of TOC's available in the help system
	 */
	public IToc[] getTocs()
	{
		return tocs;
	}
	
	/**
	 * Returns the navigation model for specified toc
	 */
	public IToc getToc(String href) {
		if (href == null || href.equals(""))
			return null;
		for (int i = 0; i < tocs.length; i++)
			if (tocs[i].getHref().equals(href))
				return tocs[i];
		return null;
	}

	
	/**
	 * Builds the toc from the contribution files
	 */
	private void build() {
		try {
			Collection contributedTocFiles = getContributedTocFiles();
			TocBuilder builder = new TocBuilder();
			builder.build(contributedTocFiles);
			Collection builtTocs = builder.getBuiltTocs();
			tocs = new IToc[builtTocs.size()];
			int i = 0;
			for (Iterator it = builtTocs.iterator(); it.hasNext();) {
				tocs[i++] = (IToc) it.next();
			}
			orderTocs(builtTocs);

		} catch (Exception e) {
			tocs = new IToc[0];
			Logger.logError("", e);
		}
	}
	
	/**
	 * Orders the TOCs according to a product wide preference.
	 */
	private void orderTocs(Collection unorderedTocs)
	{
		ArrayList orderedHrefs = getPreferredTocOrder();
		ArrayList orderedTocs = new ArrayList(unorderedTocs.size());
		
		// add the tocs from the preferred order...
		for (Iterator it = orderedHrefs.iterator(); it.hasNext(); )
		{
			String href = (String)it.next();
			IToc toc = getToc(unorderedTocs, href);
			if (toc != null)
				orderedTocs.add(toc);
		}
		// add the remaining tocs 
		for (Iterator it=unorderedTocs.iterator(); it.hasNext(); )
		{
			IToc toc = (IToc)it.next();
			if (!orderedTocs.contains(toc))
				orderedTocs.add(toc);
		}
		
		this.tocs = new IToc[orderedTocs.size()];
		orderedTocs.toArray(this.tocs);
	}
	
	/**
	 * Reads product.ini to determine toc ordering.
	 * It works in current drivers, but will not
	 * if location/name of product.ini change.
	 * Return the list of href's.
	 */
	private ArrayList getPreferredTocOrder() {
		ArrayList orderedTocs = new ArrayList();
		try {
			Plugin p = Platform.getPlugin("org.eclipse.sdk");
			if (p == null)
				return orderedTocs;
			URL pURL = p.getDescriptor().getInstallURL();
			URL productIniURL = new URL(pURL, "product.ini");
			Properties prop = new Properties();
			prop.load(productIniURL.openStream());
			String preferredTocs = prop.getProperty("baseInfosets");
			if (preferredTocs != null) {
				StringTokenizer suggestdOrderedInfosets =
					new StringTokenizer(preferredTocs, " ;,");

				while (suggestdOrderedInfosets.hasMoreElements()) {
					orderedTocs.add(suggestdOrderedInfosets.nextElement());
				}
			}
		} catch (Exception e) {
			Logger.logWarning(Resources.getString("W001"));
		}
		return orderedTocs;
	}
	
	/**
	 * Returns the toc from a list of IToc by identifying it with its (unique) href.
	 */
	private IToc getToc(Collection list, String href)
	{
		for(Iterator it=list.iterator(); it.hasNext(); )
		{
			IToc toc = (IToc)it.next();
			if (toc.getHref().equals(href))
				return toc;
		}
		return null;
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