/*******************************************************************************
 * Copyright (c) 2008, 2009 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Serge Beauchamp(Freescale Semiconductor) - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.core.resources;

import org.eclipse.core.filesystem.IFileInfoFilter;

/**
 * A factory for instantiating {@link IFileInfoFilter} instances
 * of a particular type.
 * 
 * @since 3.6
 */
public interface IFileInfoFilterFactory  {

	/**
	 * Returns a filter instance with the given project and arguments.
	 * 
	 * @param project the project from which this filter is called
	 * @param arguments the test arguments, or <code>null</code> if not applicable
	 * for this filter type.
	 */
	public IFileInfoFilter instantiate(IProject project, Object arguments);
}
