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
import org.eclipse.core.filesystem.IFileInfoFilter;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * A factory for instantiating {@link IFileInfoFilter} instances
 * of a particular type.
 * 
 * @since 3.6
 */
public abstract class AbstractFileInfoMatcher {

	/**
	 * Return if this filter matches with the fileInfo provided.
	 * 
	 * @param fileInfo the object to test
	 * @return <code>true</code> if this filter matches the given file info,
	 * and <code>false</code> otherwise.
	 * @throws CoreException the implementor should throw a CoreException if, 
	 * 		in the case that the parent or fileInfo doesn't exist in the workspace
	 * 		or in the file system, the return value can't be determined.
	 */
	public abstract boolean matches(IContainer parent, IFileInfo fileInfo) throws CoreException;

	public abstract void initialize(IProject project, Object arguments);
}