/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

public interface IBundleGroupProvider {
	/**
	 * Returns the human-readable name of this bundle group provider.
	 * @return the name of this bundle group provider
	 */
	public String getName();
	
	/**
	 * Returns the bundle groups provided by this provider.
	 * @return the bundle groups provided by this provider
	 */
	public IBundleGroup[] getBundleGroups();
}