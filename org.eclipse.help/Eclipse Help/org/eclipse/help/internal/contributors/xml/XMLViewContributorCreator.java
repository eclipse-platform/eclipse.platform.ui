package org.eclipse.help.internal.contributors.xml;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.contributors.*;

/**
 * Factory for view contributors
 */
public class XMLViewContributorCreator implements ContributorCreator {
	/**
	 * XMLTopicContributorCreator constructor comment.
	 */
	public XMLViewContributorCreator() {
		super();
	}
	/**
	 * create method comment.
	 */
	public Contributor create(
		IPluginDescriptor plugin,
		IConfigurationElement configuration) {
		return new XMLViewContributor(plugin, configuration);
	}
}
