package org.eclipse.help.internal.contributors.xml1_0;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.contributors1_0.*;
import org.eclipse.help.internal.contributions1_0.Contribution;

/**
 * Action contributor
 */
public class XMLActionContributor
	extends XMLContributor
	implements ActionContributor {
	/**
	 * XMLActionContributor constructor comment.
	 * @param plugin com.ibm.itp.core.api.plugins.IPluginDescriptor
	 * @param configuration com.ibm.itp.core.api.plugins.IConfigurationElement
	 */
	public XMLActionContributor(
		IPluginDescriptor plugin,
		IConfigurationElement configuration) {
		super(plugin, configuration);
	}
	/**
	 * getActions method comment.
	 */
	public Contribution getContribution() {
		return getContribution(ACTIONS_NAME_ATTR);
	}
	/**
	 * @return java.lang.String
	 */
	public String getType() {
		return ActionContributor.ACTIONS_ELEM;
	}
	/**
	 * @param doc org.w3c.dom.Document
	 */
	protected void preprocess(Contribution contrib) {
		/// XXX TO DO - ensure ALL ids are fully qualified
	}
}
