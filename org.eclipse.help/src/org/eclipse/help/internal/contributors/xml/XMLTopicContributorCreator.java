package org.eclipse.help.internal.contributors.xml;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.contributors.*;

/**
 * Factory for topic contributors
 */
public class XMLTopicContributorCreator implements ContributorCreator {
	/**
	 * XMLTopicContributorCreator constructor comment.
	 */
	public XMLTopicContributorCreator() {
		super();
	}
	/**
	 * create method comment.
	 */
	public Contributor create(
		IPluginDescriptor plugin,
		IConfigurationElement configuration) {
		return new XMLTopicContributor(plugin, configuration);
	}
}
