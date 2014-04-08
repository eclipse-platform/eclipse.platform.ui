/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 404136, 424730
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.common;

/**
 * Specifies the scope when searching for a resource. This differs from the PDE
 * search functions in that <li>more than just java objects can be queried. <li>
 * Searches can specify the current project only, current workspace, non bundle
 * projects, and target platform.
 *
 * @author Steven Spungin
 *
 */
public enum ResourceSearchScope {
	/**
	 * The current project only
	 */
	PROJECT,
	/**
	 * Projects in the current workspace
	 */
	WORKSPACE,
	/**
	 * All plugins in target platform
	 */
	TARGET_PLATFORM,
	/**
	 * Eclipse Plugins and OSGi Bundles
	 */
	// BUNDLES,
	/**
	 * Eclipse Plugins
	 */
	// PLUGINS,
	/**
	 * Include resources in packages not exported by their bundles. Only applies
	 * to SearchScope.TargetPlatform and SearchScope.Workspace
	 */
	NOT_EXPORTED,
	/**
	 * Follow references to dependent projects and bundles. Only applies to
	 * SearchScope.Project
	 */
	REFERENCES
}
