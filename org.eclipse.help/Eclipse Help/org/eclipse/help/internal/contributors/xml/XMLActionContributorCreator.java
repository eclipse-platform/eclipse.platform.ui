package org.eclipse.help.internal.contributors.xml;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.contributors.*;

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
