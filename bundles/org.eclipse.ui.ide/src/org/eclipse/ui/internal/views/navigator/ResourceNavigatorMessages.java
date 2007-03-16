/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.navigator;

import org.eclipse.osgi.util.NLS;

public class ResourceNavigatorMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.views.navigator.messages";//$NON-NLS-1$

	public static String ResourceNavigator_oneItemSelected;
	public static String ResourceNavigator_statusLine;
	public static String ResourceNavigator_workingSetToolTip;
	public static String ResourceNavigator_workingSetInputToolTip;
	public static String ResourceManager_toolTip;
	public static String ShowInNavigator_errorMessage;

	// --- Actions ---
	public static String ResourceNavigator_sort;
	public static String SortView_byType;
	public static String SortView_toolTipByType;
	public static String SortView_byName;
	public static String SortView_toolTipByName;

	public static String ToggleLinkingAction_text;
	public static String ToggleLinkingAction_toolTip;

	public static String ResourceNavigator_filterText;

	public static String ResourceNavigator_new;
	public static String ResourceNavigator_openWith;

	public static String ShowInNavigator_text;
	public static String ShowInNavigator_toolTip;

	public static String CopyAction_title;
	public static String CopyAction_toolTip;

	public static String PasteAction_title;
	public static String PasteAction_toolTip;

	public static String CollapseAllAction_title;
	public static String CollapseAllAction_toolTip;

	public static String GoToResource_label;

	public static String NavigatorFrameSource_closedProject_title;
	public static String NavigatorFrameSource_closedProject_message;
	// --- Dialogs ---
	public static String Goto_title;
	public static String FilterSelection_message;
	public static String FilterSelection_toolTip;
	public static String FilterSelection_title;

	public static String DropAdapter_title;
	public static String DropAdapter_problemImporting;
	public static String DropAdapter_problemsMoving;
	public static String DropAdapter_question;
	public static String DropAdapter_targetMustBeResource;
	public static String DropAdapter_canNotDropIntoClosedProject;
	public static String DropAdapter_resourcesCanNotBeSiblings;
	public static String DropAdapter_ok;
	public static String DropAdapter_overwriteQuery;
	public static String DropAdapter_dropOperationErrorOther;

	public static String DragAdapter_title;
	public static String DragAdapter_checkDeleteMessage;

	public static String CopyToClipboardProblemDialog_title;
	public static String CopyToClipboardProblemDialog_message;

	public static String MoveResourceAction_title;
	public static String MoveResourceAction_checkMoveMessage;


	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ResourceNavigatorMessages.class);
	}
}
