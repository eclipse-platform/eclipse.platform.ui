/*******************************************************************************
 * Copyright (c) 2008, 2009 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Serge Beauchamp(Freeescale Semiconductor) - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.core.filesystem;

/**
 * A filter that is used to match {@link IFileInfo} instances that satisfy certain
 * criteria.
 * 
 * @since 1.3
 */
public interface IFileInfoFilter {

	/**
	 * Return if this filter matches with the fileInfo provided.
	 * 
	 * @param fileInfo the object to test
	 * @return <code>true</code> if this filter matches the given file info,
	 * and <code>false</code> otherwise.
	 */
	public boolean matches(IFileInfo fileInfo);
}
