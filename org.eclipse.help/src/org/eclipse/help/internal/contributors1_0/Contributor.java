package org.eclipse.help.internal.contributors1_0;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.help.internal.contributions1_0.*;
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
