package org.eclipse.help.internal.contributors.xml;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.contributors.*;
import org.eclipse.help.internal.contributions.xml.HelpInfoSet;
import org.eclipse.help.internal.contributions.Contribution;

/**
 * View contributor
 */
public class XMLViewContributor
	extends XMLContributor
	implements ViewContributor {

	/**
	 * XMLViewContributor constructor comment.
	 * @param plugin com.ibm.itp.core.api.plugins.IPluginDescriptor
	 * @param configuration com.ibm.itp.core.api.plugins.IConfigurationElement
	 */
	public XMLViewContributor(
		IPluginDescriptor plugin,
		IConfigurationElement configuration) {
		super(plugin, configuration);
	}
	/**
	 * getViews method comment.
	 */
	public Contribution getContribution() {
		return getContribution(INFOSET_NAME_ATTR);
	}
	/**
	 * @return java.lang.String
	 */
	public String getType() {
		return ViewContributor.INFOSET_ELEM;
	}
	/**
	 * @param doc org.w3c.dom.Document
	 */
	protected void preprocess(Contribution contrib) {
		updateIDs(contrib);

		// set the href on the input infoset
		if (contrib instanceof HelpInfoSet) {
			HelpInfoSet infoset = (HelpInfoSet) contrib;
			String href = infoset.getHref();
			if (href == null)
				infoset.setHref("");
			else {
				if (!href.equals("") // no empty link
					&& !href.startsWith("/") // no help url
					&& href.indexOf(':') == -1) // no other protocols
					{
					infoset.setHref("/" + plugin.getUniqueIdentifier() + "/" + href);
				}
			}
		}
	}
}
