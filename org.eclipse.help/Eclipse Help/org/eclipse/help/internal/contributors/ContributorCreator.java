package org.eclipse.help.internal.contributors;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
