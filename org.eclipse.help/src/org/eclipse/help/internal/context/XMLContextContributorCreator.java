package org.eclipse.help.internal.context;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.contributors1_0.*;
/**
 * Factory for map contributors
 */
public class XMLContextContributorCreator implements ContributorCreator {
	/**
	 * XMLTopicContributorCreator constructor.
	 */
	public XMLContextContributorCreator() {
		super();
	}
	/**
	 * create method.
	 */
	public Contributor create(
		IPluginDescriptor plugin,
		IConfigurationElement configuration) {
		return new XMLContextContributor(plugin, configuration);
	}
}