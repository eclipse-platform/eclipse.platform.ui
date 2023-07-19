/*******************************************************************************
 * Copyright (c) 2003, 2016, 2023 IBM Corporation and others.
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
 * Mickael Istria (Red Hat Inc.) - [266030] Allow "others" working set
 * Nikifor Fedorov (ArSysOp) - Import more than one project at once (eclipse.platform#226)
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


	public static String PortingActionProvider_ImportResourcesMenu_label;

	public static String PortingActionProvider_ExportResourcesMenu_label;

	public static String NewActionProvider_NewMenu_label;

	public static String OpenActionProvider_OpenWithMenu_label;

	public static String DropAdapter_title;
	public static String DropAdapter_problemImporting;
	public static String DropAdapter_problemsMoving;
	public static String DropAdapter_targetMustBeResource;
	public static String DropAdapter_canNotDropIntoClosedProject;
	public static String DropAdapter_resourcesCanNotBeSiblings;
	public static String DropAdapter_canNotDropProjectIntoProject;
	public static String DropAdapter_dropOperationErrorOther;

	public static String MoveResourceAction_title;
	public static String MoveResourceAction_checkMoveMessage;

	public static String ResourceMgmtActionProvider_logTitle;

	public static String WorkingSetRootModeActionGroup_Top_Level_Element_;
	public static String WorkingSetRootModeActionGroup_Project_;
	public static String WorkingSetRootModeActionGroup_Working_Set_;
	public static String WorkingSetActionProvider_multipleWorkingSets;

	public static String CopyAction_Cop_;
	public static String CopyAction_Copy_selected_resource_s_;

	public static String PasteAction_Past_;
	public static String PasteAction_Paste_selected_resource_s_;

	public static String GotoResourceDialog_GoToTitle;

	public static String ProjectExplorer_toolTip;
	public static String ProjectExplorer_toolTip2;
	public static String ProjectExplorer_toolTip3;

	public static String ProjectExplorerPart_workspace;
	public static String ProjectExplorerPart_workingSetModel;

	public static String OpenProjectAction_OpenExistingProject;
	public static String OpenProjectAction_OpenExistingProjects;
	public static String OpenProjectAction_OpenExistingProject_desc;
	public static String OpenProjectAction_opening;
	public static String OpenProjectAction_multiple;

	public static String SelectProjectForFolderAction_SelectProject;
	public static String SelectProjectForFolderAction_SelectProjects;

	public static String NestedProjectLabelProvider_nestedProjectLabel;

	public static String workingSet_others;
	public static String ShowInActionProvider_showInAction_label;



	static {
		initializeMessages(BUNDLE_NAME, WorkbenchNavigatorMessages.class);
	}
}
