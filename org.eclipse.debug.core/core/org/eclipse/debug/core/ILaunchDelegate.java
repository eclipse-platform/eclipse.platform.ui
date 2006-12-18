/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

/**
 * A proxy to an {@link ILaunchConfigurationDelegate}. Represents a
 * launch delegate contributed to the <code>org.eclipse.debug.core.launchDelegates</code>
 * extension point. 
 * <p>
 * This interface is NOT to be implemented by clients
 * </p>
 * @since 3.3
 * <p>
 * <strong>EXPERIMENTAL</strong>. This interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * remain unchanged during the 3.3 release cycle. Please do not use this API
 * without consulting with the Platform/Debug team.
 * </p>
 */
public interface ILaunchDelegate {

	/**
	 * Returns this delegate's unique identifier.
	 * 
	 * @return launch delegate identifier
	 */
	public String getId();
	
	/**
	 * Returns a human readable name for this launch delegate
	 * or <code>null</code> if none.
	 * 
	 * @return name or <code>null</code>
	 */
	public String getName();
	
	/**
	 * Returns a description of this launch delegate, or 
	 * <code>null</code> if none.
	 * 
	 * @return description or <code>null</code>
	 */
	public String getDescription();
	
	/**
	 * Returns the name of the plug-in that contributed this delegate.
	 * 
	 * @return contributor name
	 */
	public String getContributorName();
	
	/**
	 * Returns the underlying launch configuration.
	 * Causes the delegate to be instantiated.
	 * 
	 * @return launch configuration delegate
	 * @exception CoreException if unable to instantiate the delegate
	 */
	public ILaunchConfigurationDelegate getDelegate() throws CoreException;
	
	/**
	 * Returns the complete set of launch modes supported by this delegate as a list of sets.
	 * Each set contains one of more launch mode identifiers. When a set contains more than
	 * one launch mode, it indicates that a mixed launch mode is supported.
	 * If no modes are available an empty list is returned.
	 * 
	 * @return the complete set of launch modes this delegate supports
	 */
	public List getModes();
	
	/**
	 * Returns the id of the plug-in that contributed this launch delegate.
	 * 
	 * @return the id of the plug-in that contributed this launch delegate
	 */
	public String getPluginIdentifier();
	
}
