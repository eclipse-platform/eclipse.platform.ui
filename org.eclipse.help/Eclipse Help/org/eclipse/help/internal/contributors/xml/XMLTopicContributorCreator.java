package org.eclipse.help.internal.contributors.xml;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
