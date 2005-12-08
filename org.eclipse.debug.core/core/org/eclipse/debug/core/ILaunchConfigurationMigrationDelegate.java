/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.core;

import org.eclipse.core.runtime.CoreException;

/**
 * Provides API for migrating pre 3.2 launch configurations to the new style, which have specific resources mapped to them.
 * This change in mapping allows for a more robust context sensitive launching framework, as well as filtering and managing of
 * launch configurations for projects not immediately available (i.e. closed or remote)
 * 
 * @since 3.2
 */
public interface ILaunchConfigurationMigrationDelegate {

	/**
	 * Gets the list of candidates from the <code>LaunchManager</code> which would be suitable
	 * for migration, typically those that have not been migrated thus far. 
	 * <p>
	 * More specifically though, candiates are chosen based on:
	 * <ul>
	 * <li> Type of the delegate, i.e. Java Application, Java Applet, etc</li>
	 * <li> If the project exists</li>
	 * <li> If the project is accessible</li>
	 * <li> If the project has not been migrated thus far</li>
	 * </ul>
	 * </p>
	 * 
	 * This method does not return null, if no suitable candidates are found an empty array is returned.
	 * @param candidate, the candidate to detemrine the migraiton status of 
	 * @return if the configuration is a migration candidate or not
	 * @throws CoreException
	 */
	public boolean isCandidate(ILaunchConfiguration candidate) throws CoreException;
	
	/**
	 * Method that performs the actual migration of pre 3.2 launch configurations.
	 * 
	 * <p>
	 * Example code usage as follows:
	 * ILaunchConfiguration[] configs = getCandidates();
	 * for(int i = 0; i < configs.length; i++) {
	 * 		migrate(configs[i]);
	 * }
	 * </p>
	 * @param candidate the candidate to be migrated, which can either be a launch configuration, or working copy.
	 * @throws CoreException
	 */
	public void migrate(ILaunchConfiguration candidate) throws CoreException;
	
}
