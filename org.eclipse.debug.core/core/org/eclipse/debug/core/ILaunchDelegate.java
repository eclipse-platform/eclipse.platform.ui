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
 * A proxy to a launch configuration delegate. Represents a
 * launch delegate contributed to the <code>org.eclipse.debug.core.launchDelegates</code>
 * extension point. 
 * 
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
	 * Returns the name of the plug-in that contributed this delegate.
	 * 
	 * @return contributor name
	 */
	public String getContributorName();
	
	/**
	 * Returns the delegate that performs the actual launch.
	 * Causes the delegate to be instantiated.
	 * 
	 * @return launch delegate
	 * @exception CoreException if unable to instantiate the delegate
	 */
	public ILaunchConfigurationDelegate getDelegate() throws CoreException;
	
	/**
	 * Returns the complete set of launch modes as a list of sets.
	 * If no modes are available an empty list is returned, never <code>null</code>
	 * @return the complete set of launch modes for this delegate as a list
	 * of sets
	 */
	public List getModes();
	
}
