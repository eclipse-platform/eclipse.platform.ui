/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 404136
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

/**
 * Specifies the scope when searching for a resource
 *
 * @author Steven Spungin
 *
 */
public enum SearchScope {
	/**
	 * Resources in the current project
	 */
	PROJECT,
	/**
	 * Opened projects in the current workspace
	 */
	WORKSPACE,
	/**
	 * The current project, and projects the current project depends on
	 */
	DEPENDENT,
	/**
	 * All projects in the current workspace and bundles in the target platform
	 */
	TARGET_PLATFORM
}
