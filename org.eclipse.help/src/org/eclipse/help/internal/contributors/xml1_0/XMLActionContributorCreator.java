package org.eclipse.help.internal.contributors.xml1_0;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.contributors1_0.*;

/**
 * XML Action Contribution Creator
 */
public class XMLActionContributorCreator implements ContributorCreator {
	/**
	 * XMLTopicContributorCreator constructor
	 */
	public XMLActionContributorCreator() {
		super();
	}
	/**
	 * create
	 */
	public Contributor create(
		IPluginDescriptor plugin,
		IConfigurationElement configuration) {
		return new XMLActionContributor(plugin, configuration);
	}
}
