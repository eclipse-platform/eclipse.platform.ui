package org.eclipse.help.internal.contributors;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.util.Iterator;

/**
 * Contributions manager.
 */
public interface ContributionManager {
	public static final String CONTRIBUTION_EXTENSION =
		"org.eclipse.help.contributions";

	/**
	 */
	Iterator getContributingPlugins();
	/**
	 */
	Iterator getContributionsOfType(String typeName);
	/**
	 */
	boolean hasNewContributions();
	/** Saves the contribution info */
	public void versionContributions();
}
