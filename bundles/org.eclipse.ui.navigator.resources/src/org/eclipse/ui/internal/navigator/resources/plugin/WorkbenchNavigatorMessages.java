/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.plugin;

import org.eclipse.osgi.util.NLS;

/**
 * Utility class which helps managing messages
 * 
 * 
 * @since 3.2
 */
public class WorkbenchNavigatorMessages extends NLS {
	/** The bundle properties file */
	public static final String BUNDLE_NAME = "org.eclipse.ui.internal.navigator.resources.plugin.messages"; //$NON-NLS-1$

	/** Message key. */
	public static String PortingActionProvider_ImportResourcesMenu_label;

	/** Message key. */
	public static String PortingActionProvider_ExportResourcesMenu_label;

	/** Message key. */
	public static String NewActionProvider_NewMenu_label;

	/** Message key. */
	public static String OpenActionProvider_OpenWithMenu_label;

	/** Message key. */
	public static String DropAdapter_title;

	/** Message key. */
	public static String DropAdapter_problemImporting;

	/** Message key. */
	public static String DropAdapter_problemsMoving;

	/** Message key. */
	public static String DropAdapter_targetMustBeResource;

	/** Message key. */
	public static String DropAdapter_canNotDropIntoClosedProject;

	/** Message key. */
	public static String DropAdapter_resourcesCanNotBeSiblings;

	/** Message key. */
	public static String DropAdapter_canNotDropProjectIntoProject;
	
	/** Message key. */
	public static String DropAdapter_dropOperationErrorOther;
	

	/** Message key. */
	public static String MoveResourceAction_title;

	/** Message key. */
	public static String MoveResourceAction_checkMoveMessage;

	/** Message key. */
	public static String WorkingSetRootModeActionGroup_Top_Level_Element_;

	/** Message key. */
	public static String WorkingSetRootModeActionGroup_Project_;

	/** Message key. */
	public static String WorkingSetRootModeActionGroup_Working_Set_;

	/** Message key. */
	public static String CopyAction_Cop_;

	/** Message key. */
	public static String CopyAction_Copy_selected_resource_s_;

	/** Message key. */
	public static String PasteAction_Past_;

	/** Message key. */
	public static String PasteAction_Paste_selected_resource_s_;

	static {
		initializeMessages(BUNDLE_NAME, WorkbenchNavigatorMessages.class);
	}
}
