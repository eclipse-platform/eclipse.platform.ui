/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core.sourcelookup;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * A source path computer computes the default source lookup path (set of source
 * containers that should be considered) for a launch configuration.
 * 
 * TODO: contribution exampl
 * TODO: add factory method to launch manager to create source path computer
 *  for a launch configuration
 * 
 * @since 3.0
 */
public interface ISourcePathComputer {
	
	/**
	 * Launch configuration attribute to specify a source path computer
	 * that should be used for a launch configuration. The value is an identifer
	 * of a source path computer extension, or unspecified (<code>null</code>), if the
	 * default source path computer should be used. A default source path computer
	 * can be associated with a launch configuration type.
	 */
	public static final String ATTR_SOURCE_PATH_COMPUTER_ID = DebugPlugin.getUniqueIdentifier() + ".SOURCE_PATH_COMPUTER_ID"; //$NON-NLS-1$

	/**
	 * Returns a default collection source containers to be considered for the
	 * given launch configuration. The collection returned represents the default
	 * source lookup path for the given configuration.
	 * 
	 * @param configuration the launch configuration for which a default source lookup path
	 *  is to be computed
	 * @param monitor a progress monitor to be used in case of long operations
	 * @return a default collection source containers to be considered for the
	 *  given launch configuration
	 */
	public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor);

	
}
