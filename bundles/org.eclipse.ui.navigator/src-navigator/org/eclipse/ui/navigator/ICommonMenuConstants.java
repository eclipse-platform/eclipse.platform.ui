/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 *<p>
* <strong>EXPERIMENTAL</strong>. This class or interface has been added as
* part of a work in progress. There is a guarantee neither that this API will
* work nor that it will remain the same. Please do not use this API without
* consulting with the Platform/UI team.
* </p>
 *  
 *  @since 3.2
 */
public interface ICommonMenuConstants {

	// the following was taken directly from the JDT's menu constants

	//		/**
	//		 * Type hierarchy view part: pop-up menu target ID for type hierarchy viewer
	//		 * (value <code>"org.eclipse.jdt.ui.TypeHierarchy.typehierarchy"</code>).
	//		 *
	//		 * @since 2.0
	//		 */
	//		public static final String TARGET_ID_HIERARCHY_VIEW= JavaUI.ID_TYPE_HIERARCHY +
	// ".typehierarchy"; //$NON-NLS-1$
	//
	//		/**
	//		 * Type hierarchy view part: pop-up menu target ID for supertype hierarchy viewer
	//		 * (value <code>"org.eclipse.jdt.ui.TypeHierarchy.supertypes"</code>).
	//		 *
	//		 * @since 2.0
	//		 */
	//		public static final String TARGET_ID_SUPERTYPES_VIEW= JavaUI.ID_TYPE_HIERARCHY +
	// ".supertypes"; //$NON-NLS-1$
	//
	//		/**
	//		 * Type hierarchy view part: Pop-up menu target ID for the subtype hierarchy viewer
	//		 * (value <code>"org.eclipse.jdt.ui.TypeHierarchy.subtypes"</code>).
	//		 *
	//		 * @since 2.0
	//		 */
	//		public static final String TARGET_ID_SUBTYPES_VIEW= JavaUI.ID_TYPE_HIERARCHY + ".subtypes";
	// //$NON-NLS-1$
	//
	//		/**
	//		 * Type hierarchy view part: pop-up menu target ID for the meber viewer
	//		 * (value <code>"org.eclipse.jdt.ui.TypeHierarchy.members"</code>).
	//		 *
	//		 * @since 2.0
	//		 */
	//		public static final String TARGET_ID_MEMBERS_VIEW= JavaUI.ID_TYPE_HIERARCHY + ".members";
	// //$NON-NLS-1$


	/**
	 * Pop-up menu: name of group for goto actions (value <code>"group.open"</code>).
	 * <p>
	 * Examples for open actions are:
	 * <ul>
	 * <li>Go Into</li>
	 * <li>Go To</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_GOTO = "group.goto"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for porting actions (value <code>"group.port"</code>).
	 * <p>
	 * Examples for open actions are:
	 * <ul>
	 * <li>Import</li>
	 * <li>Export</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_PORT = "group.port";//$NON-NLS-1$
	/**
	 * Pop-up menu: name of group for open actions (value <code>"group.open"</code>).
	 * <p>
	 * Examples for open actions are:
	 * <ul>
	 * <li>Open To</li>
	 * <li>Open With</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_OPEN = "group.open"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for show actions (value <code>"group.show"</code>).
	 * <p>
	 * Examples for show actions are:
	 * <ul>
	 * <li>Show in Navigator</li>
	 * <li>Show in Type Hierarchy</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_SHOW = "group.show"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for new actions (value <code>"group.new"</code>).
	 * <p>
	 * Examples for new actions are:
	 * <ul>
	 * <li>Create new class</li>
	 * <li>Create new interface</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_NEW = "group.new"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for build actions (value <code>"group.build"</code>).
	 */
	public static final String GROUP_BUILD = "group.build"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for reorganize actions (value <code>"group.reorganize"</code>).
	 */
	public static final String GROUP_REORGANIZE = IWorkbenchActionConstants.GROUP_REORGANIZE;

	/**
	 * Pop-up menu: name of group for code generation actions ( value <code>"group.generate"</code>).
	 */
	public static final String GROUP_GENERATE = "group.generate"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for source actions. This is an alias for
	 * <code>GROUP_GENERATE</code> to be more consistent with main menu bar structure.
	 * 
	 * @since 2.0
	 */
	public static final String GROUP_SOURCE = GROUP_GENERATE;

	/**
	 * Pop-up menu: name of group for search actions (value <code>"group.search"</code>).
	 */
	public static final String GROUP_SEARCH = "group.search"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for additional actions (value <code>"additions"</code>).
	 */
	public static final String GROUP_ADDITIONS = "additions"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for viewer setup actions (value <code>"group.viewerSetup"</code>).
	 */
	public static final String GROUP_VIEWER_SETUP = "group.viewerSetup"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for properties actions (value <code>"group.properties"</code>).
	 */
	public static final String GROUP_PROPERTIES = "group.properties"; //$NON-NLS-1$ 

	public static final String COMMON_MENU_TOP = GROUP_NEW;
	public static final String COMMON_MENU_OPEN_ACTION = GROUP_OPEN;
	public static final String COMMON_MENU_OPEN_WITH_ACTIONS = "common-menu-openwith-actions"; //$NON-NLS-1$
	public static final String COMMON_MENU_EDIT_ACTIONS = "common-menu-edit-actions"; //$NON-NLS-1$
	public static final String COMMON_MENU_EDIT_CUT = "common-menu-edit-cut"; //$NON-NLS-1$
	public static final String COMMON_MENU_EDIT_COPY = "common-menu-edit-copy"; //$NON-NLS-1$		
	public static final String COMMON_MENU_EDIT_PASTE = "common-menu-edit-paste"; //$NON-NLS-1$
	public static final String COMMON_MENU_EDIT_DELETE = "common-menu-edit-delete"; //$NON-NLS-1$
	public static final String COMMON_MENU_ADDITIONS = IWorkbenchActionConstants.MB_ADDITIONS;
	public static final String COMMON_MENU_BOTTOM = "common-menu-bottom"; //$NON-NLS-1$

}
