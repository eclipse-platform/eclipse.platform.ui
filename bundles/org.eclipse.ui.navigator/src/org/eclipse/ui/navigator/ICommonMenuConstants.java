/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Feb 5, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.navigator;

import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Defines strings used for menu insertion points.
 *
 * @since 3.2
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICommonMenuConstants {

	/**
	 * Pop-up menu: name of group for the top of the menu (value
	 * <code>"group.top"</code>).
	 */
	String GROUP_TOP = "group.top"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for goto actions (value
	 * <code>"group.goto"</code>).
	 * <p>
	 * Examples for open actions are:
	 * </p>
	 * <ul>
	 * <li>Go Into</li>
	 * <li>Go To</li>
	 * </ul>
	 */
	String GROUP_GOTO = "group.goto"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for show actions (value
	 * <code>"group.show"</code>).
	 * <p>
	 * Examples for show actions are:
	 * </p>
	 * <ul>
	 * <li>Show in Navigator</li>
	 * <li>Show in Type Hierarchy</li>
	 * </ul>
	 */
	String GROUP_SHOW = "group.show"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for new actions (value <code>"group.new"</code>).
	 * <p>
	 * Examples for new actions are:
	 * </p>
	 * <ul>
	 * <li>Create new class</li>
	 * <li>Create new interface</li>
	 * </ul>
	 */
	String GROUP_NEW = "group.new"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for open actions (value
	 * <code>"group.open"</code>).
	 * <p>
	 * Examples for open actions are:
	 * </p>
	 * <ul>
	 * <li>Open To</li>
	 * <li>Open With</li>
	 * </ul>
	 *
	 * @see #GROUP_OPEN_WITH
	 */
	String GROUP_OPEN = "group.open"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for open actions (value
	 * <code>"group.openWith"</code>).
	 * <p>
	 * Examples for open actions are:
	 * </p>
	 * <ul>
	 * <li>Open With</li>
	 * </ul>
	 */
	String GROUP_OPEN_WITH = "group.openWith"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for porting actions (value
	 * <code>"group.port"</code>).
	 * <p>
	 * Examples for open actions are:
	 * </p>
	 * <ul>
	 * <li>Import</li>
	 * <li>Export</li>
	 * </ul>
	 */
	String GROUP_PORT = "group.port";//$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for properties actions (value
	 * <code>"group.edit"</code>).
	 */
	String GROUP_EDIT = "group.edit"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for build actions (value
	 * <code>"group.build"</code>).
	 */
	String GROUP_BUILD = "group.build"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for reorganize actions (value
	 * <code>"group.reorganize"</code>).
	 */
	String GROUP_REORGANIZE = IWorkbenchActionConstants.GROUP_REORGANIZE;

	/**
	 * Pop-up menu: name of group for code generation actions ( value
	 * <code>"group.generate"</code>).
	 */
	String GROUP_GENERATE = "group.generate"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for source actions. This is an alias for
	 * <code>GROUP_GENERATE</code> to be more consistent with main menu bar
	 * structure.
	 *
	 */
	String GROUP_SOURCE = GROUP_GENERATE;

	/**
	 * Pop-up menu: name of group for search actions (value
	 * <code>"group.search"</code>).
	 */
	String GROUP_SEARCH = "group.search"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for additional actions (value
	 * <code>"additions"</code>).
	 */
	String GROUP_ADDITIONS = "additions"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for viewer setup actions (value
	 * <code>"group.viewerSetup"</code>).
	 */
	String GROUP_VIEWER_SETUP = "group.viewerSetup"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for properties actions (value
	 * <code>"group.properties"</code>).
	 */
	String GROUP_PROPERTIES = "group.properties"; //$NON-NLS-1$

}
