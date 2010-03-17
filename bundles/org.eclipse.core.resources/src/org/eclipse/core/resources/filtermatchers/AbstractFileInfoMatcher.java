/*******************************************************************************
 * Copyright (c) 2008, 2010 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Serge Beauchamp(Freescale Semiconductor) - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.core.resources.filtermatchers;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * The abstract base class for all file info matchers. Instances
 * of this class are provided using the <tt>org.eclipse.core.resources.filterMatchers</tt>
 * extension point.
 * 
 * @since 3.6
 */
public abstract class AbstractFileInfoMatcher {

	/**
	 * Tests the given {@link FileInfo}
	 * 
	 * @param parent the parent container
	 * @param fileInfo the {@link FileInfo} object to test
	 * @return <code>true</code> if the given {@link FileInfo} matches,
	 * and <code>false</code> otherwise.
	 * @throws CoreException the implementor should throw a CoreException if, 
	 * 		in the case that the parent or fileInfo doesn't exist in the workspace
	 * 		or in the file system, the return value can't be determined.
	 */
	public abstract boolean matches(IContainer parent, IFileInfo fileInfo) throws CoreException;

	/**
	 * Sets initialization data for this matcher.
	 * @param project
	 * @param arguments
	 * @throws CoreException if initialization failed
	 */
	public abstract void initialize(IProject project, Object arguments) throws CoreException;
}