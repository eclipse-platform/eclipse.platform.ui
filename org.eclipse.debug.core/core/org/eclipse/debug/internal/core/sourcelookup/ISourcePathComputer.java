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
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * A source path computer computes the default source lookup path (set of source
 * containers that should be considered) for a launch configuration.
 * 
 * TODO: contribution exampl
 * TODO: add factory method to launch manager to create source path computer
 *  for a launch configuration
 * TODO: how does one associate a source path computer with a launch config?
 *  is it done via launch config types, or an attribute, or both?
 * 
 * @since 3.0
 */
public interface ISourcePathComputer {

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
