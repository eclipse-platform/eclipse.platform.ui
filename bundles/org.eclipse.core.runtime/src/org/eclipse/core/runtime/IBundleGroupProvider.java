/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

/**
 * Bundle group providers define groups of plug-ins which have been installed in
 * the current system.  Typically, a configuration agent (i.e., plug-in installer) will 
 * define a bundle group provider so that it can report to the system the list 
 * of plug-ins it has installed.
 * 
 * @see IBundleGroup
 * @since 3.0
 */
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
