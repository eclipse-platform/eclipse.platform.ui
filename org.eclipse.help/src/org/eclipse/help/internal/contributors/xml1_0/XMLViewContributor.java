package org.eclipse.help.internal.contributors.xml1_0;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.contributors1_0.*;
import org.eclipse.help.internal.contributions.xml1_0.HelpInfoSet;
import org.eclipse.help.internal.contributions1_0.Contribution;

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
