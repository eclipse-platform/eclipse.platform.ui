/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.sourcelookup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * A source path computer computes the default source lookup path (set of source
 * containers that should be considered) for a launch configuration.
 * <p>
 * A source path computer is contributed in plug-in XML via the 
 * <code>sourcePathComputers</code> extension point, providing a delegate
 * to compute the default source lookup path specific to a launch configuration.
 * </p>
 * <pre>
 * <extension point="org.eclipse.debug.core.sourcePathComputers">
 *	 <sourcePathComputer
 *		id="com.example.debug.pdt.PDTSourcePathComputer"
 *		class="com.example.debug.pdt.PDTSourcePathComputer">
 *	 </sourcePathComputer>
 * </extension>
 * </pre>
 * 
 * TODO: Didn't we also want to be able to override the registered computer
 *  with a configuration attribute? 
 * 
 * @since 3.0
 */
public interface ISourcePathComputerDelegate {
	
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
	 * @exception CoreException if unable to compute a default source lookup path
	 */
	public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException;
	
}
