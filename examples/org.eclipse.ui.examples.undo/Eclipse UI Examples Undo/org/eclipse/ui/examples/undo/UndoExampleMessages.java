/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.examples.undo;

import org.eclipse.osgi.util.NLS;

/**
 * Message class for the undo example.
 *
 */
public class UndoExampleMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.examples.undo.messages";//$NON-NLS-1$

	public static String UndoPreferences_Description;
	public static String UndoPreferences_HistoryLimit;
	public static String UndoPreferences_ConfirmUndo;
	public static String UndoPreferences_ShowDebug;
	public static String UndoPreferences_DoNotConfirm;

	public static String UndoHistoryView_ContextFilterDialog;
	public static String UndoHistoryView_ChooseContextMessage;
	public static String UndoHistoryView_FilterText;
	public static String UndoHistoryView_FilterToolTipText;
	public static String UndoHistoryView_OperationInvalid;
	public static String UndoHistoryView_OperationException;
	public static String UndoHistoryView_UndoSelected;
	public static String UndoHistoryView_UndoSelectedToolTipText;
	public static String UndoHistoryView_RefreshList;
	public static String UndoHistoryView_RefreshListToolTipText;
	public static String UndoHistoryView_InfoDialogTitle;

	public static String BoxView_Title;
	public static String BoxView_ClearBoxes;
	public static String BoxView_ClearBoxesToolTipText;
	public static String BoxView_Move;
	public static String BoxView_Add;
	public static String BoxView_ConfirmUndo;
	public static String BoxView_Undo;
	public static String BoxView_Redo;

    static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, UndoExampleMessages.class);
	}

}