package org.eclipse.help.internal.contributors1_0;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginDescriptor;

/**
 * Contributor factory.
 */
public interface ContributorCreator {
	/**
	 */
	Contributor create(
		IPluginDescriptor plugin,
		IConfigurationElement configuration);
}
