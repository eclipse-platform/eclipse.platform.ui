package org.eclipse.help.internal.contributors;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.help.internal.contributions.*;
import org.eclipse.core.runtime.IPluginDescriptor;

/**
 * Contributor objects. Reads the contribution documents of a certain type.
 */
public interface Contributor {
	public static final String NAME_ATTR = "name";
	public static final String ID_ATTR = "id";
	public static final String PLUGIN_ATTR = "plugin";

	/**
	 */
	Contribution getContribution();
	/**
	 */
	IPluginDescriptor getPlugin();
	/**
	 */
	String getType();
}
