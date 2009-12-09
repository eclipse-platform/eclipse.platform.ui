/*******************************************************************************
 * Copyright (c) 2008, 2009 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *     IBM - ongoing development
 *******************************************************************************/
package org.eclipse.core.resources;

/**
 * @see IContainer#createFilter(int, IFileInfoMatcherDescription, int, org.eclipse.core.runtime.IProgressMonitor)
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 3.6
 */
public interface IFileInfoMatcherDescription {

	/**
	 * Return the matcher id.
	 * 
	 * @return the file info matcher id.
	 */
	public String getId();

	public void setId(String id);

	/**
	 * Return the matcher arguments, or null if no arguments exist.
	 * 
	 * @return the argument string, or null
	 */
	public Object getArguments();

	public void setArguments(Object arguments);
}