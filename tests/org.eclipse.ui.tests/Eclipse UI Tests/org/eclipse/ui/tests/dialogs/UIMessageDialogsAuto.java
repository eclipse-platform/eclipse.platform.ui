/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 490700, 489250, 511198
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;
import org.eclipse.ui.tests.harness.util.DialogCheck;

import junit.framework.TestCase;

public class UIMessageDialogsAuto extends TestCase {
	private static final String DUMMY_RESOURCE = "Dummy.resource";

	private static final String DUMMY_PROJECT = "DummyProject";

	private static final String DUMMY_RELATIVE_PATH = "\\" + DUMMY_PROJECT
			+ "\\" + DUMMY_RESOURCE;

	public UIMessageDialogsAuto(String name) {
		super(name);
	}

	private Shell getShell() {
		return DialogCheck.getShell();
	}


	/*
	 * Convenience method simliar to org.eclipse.jface.dialogs.MessageDialog::openInformation.
	 * The method will return the dialog instead of opening.
	 * @param title the dialog's title, or <code>null</code> if none.
	 * @param message the message.
	 * @return MessageDialog the information dialog.
	 */
	private MessageDialog getInformationDialog(String title, String message) {
		return new MessageDialog(getShell(), title, null, message,
				MessageDialog.INFORMATION, 0,
				IDialogConstants.OK_LABEL);
	}

	/*
	 * Convenience method simliar to org.eclipse.jface.dialogs.MessageDialog::openQuestion.
	 * The method will return the dialog instead of opening.
	 * @param title the dialog's title, or <code>null</code> if none.
	 * @param message the message.
	 * @return MessageDialog the question dialog.
	 */
	private MessageDialog getQuestionDialog(String title, String message) {
		return new MessageDialog(getShell(), title, null, message,
				MessageDialog.QUESTION,0,
				IDialogConstants.YES_LABEL,
				IDialogConstants.NO_LABEL );
	}

	/*
	 * Convenience method simliar to org.eclipse.jface.dialogs.MessageDialog::getWarningDialog.
	 * The method will return the dialog instead of opening.
	 * @param title the dialog's title, or <code>null</code> if none.
	 * @param message the message.
	 * @return MessageDialog the confirm dialog.
	 */
	private MessageDialog getWarningDialog(String title, String message) {
		return new MessageDialog(getShell(), title, null, message,
				MessageDialog.WARNING, 0,
				IDialogConstants.OK_LABEL);
	}

	public void testAbortPageFlipping() {
		Dialog dialog = getWarningDialog(JFaceResources
				.getString("AbortPageFlippingDialog.title"), JFaceResources
				.getString("AbortPageFlippingDialog.message"));
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testCopyOverwrite() {
		Dialog dialog = getQuestionDialog("Exists","");
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testDeleteProject() {
		String title = "Project";
		String msg ="";
		Dialog dialog = new MessageDialog(getShell(), title, null, // accept the default window icon
				msg, MessageDialog.QUESTION, 0,
				IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
				IDialogConstants.CANCEL_LABEL);
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testDeleteReadOnlyCheck() {
		Dialog dialog = new MessageDialog(getShell(), "Delete", null,
				"Exists",
				MessageDialog.QUESTION, 0,
				IDialogConstants.YES_LABEL,
				IDialogConstants.YES_TO_ALL_LABEL,
				IDialogConstants.NO_LABEL,
				IDialogConstants.CANCEL_LABEL);
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testDeleteResource() {
		Dialog dialog = getQuestionDialog("Delete","");
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testDeleteResources() {
		Dialog dialog = getQuestionDialog("Delete","");
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testDropOverwrite() {
		Dialog dialog = new MessageDialog(
				getShell(),
				ResourceNavigatorMessages.DropAdapter_question,
				null, MessageFormat.format(ResourceNavigatorMessages.DropAdapter_overwriteQuery, DUMMY_RELATIVE_PATH),
				MessageDialog.QUESTION, 0,
				IDialogConstants.YES_LABEL,
				IDialogConstants.YES_TO_ALL_LABEL,
				IDialogConstants.NO_LABEL,
				IDialogConstants.CANCEL_LABEL);
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testErrorClosing() {
		Dialog dialog = getQuestionDialog(WorkbenchMessages.Error,
				WorkbenchMessages.ErrorClosingNoArg);
		DialogCheck.assertDialogTexts(dialog);
	}
	public void testFileExtensionEmpty() {
		Dialog dialog = getInformationDialog(
				"Empty",
				"ExtensionEmptyMessage");
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testFileNameWrong() {
		Dialog dialog = getInformationDialog(
				"InvalidTitle",
				"InvalidMessage");
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testFileTypeExists() {
		Dialog dialog = getInformationDialog(WorkbenchMessages.FileEditorPreference_existsTitle,
				WorkbenchMessages.FileEditorPreference_existsMessage);
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testInvalidType_1() {
		Dialog dialog = getWarningDialog("invalidTitle","invalidMessage");
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testInvalidType_2() {
		Dialog dialog = getWarningDialog("invalidType", "invalidTypeMessage");
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testMoveReadOnlyCheck() {
		Dialog dialog = new MessageDialog(getShell(), "Move_title", null, ".MoveResourceAction",
				MessageDialog.QUESTION, 0,
				IDialogConstants.YES_LABEL,
				IDialogConstants.YES_TO_ALL_LABEL,
				IDialogConstants.NO_LABEL,
				IDialogConstants.CANCEL_LABEL);
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testNoBuilders() {
		Dialog dialog = getWarningDialog("BuildAction_warning", "noBuilders");
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testNoGlobalBuildersDialog() {
		Dialog dialog = getWarningDialog("GlobalBuildAction_warning","GlobalBuildAction_noBuilders");
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testNoPropertyPage() {
		Dialog dialog = getInformationDialog(WorkbenchMessages.PropertyDialog_messageTitle, NLS.bind(WorkbenchMessages.PropertyDialog_noPropertyMessage, (new Object[] { "DummyPropertyPage" })));
		DialogCheck.assertDialogTexts(dialog);
	}


	public void testOperationNotAvailable() {
		Dialog dialog = getInformationDialog(WorkbenchMessages.Information, "operationNotAvailableMessage");
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testOverwritePerspective() {
		Dialog dialog = new MessageDialog(getShell(), WorkbenchMessages.SavePerspective_overwriteTitle, null,
				NLS.bind(WorkbenchMessages.SavePerspective_overwriteQuestion, (new Object[] { "Dummy Perspective" })),
				MessageDialog.QUESTION, 0,
				IDialogConstants.YES_LABEL,
				IDialogConstants.NO_LABEL,
				IDialogConstants.CANCEL_LABEL);
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testRefreshDeleteProject() {
		Dialog dialog = new MessageDialog(getShell(), "RefreshAction_dialogTitle", null,
				"c:\\dummypath\\" + DUMMY_PROJECT,
				MessageDialog.QUESTION, 0,
				IDialogConstants.YES_LABEL,
				IDialogConstants.NO_LABEL);
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testRenameOverwrite() {
		Dialog dialog = getQuestionDialog(".RenameResourceAction_resourceExist",DUMMY_RELATIVE_PATH);
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testResetPerspective() {
		Dialog dialog = new MessageDialog(getShell(), WorkbenchMessages.ResetPerspective_title, null, NLS.bind(WorkbenchMessages.ResetPerspective_message, (new Object[] { "Dummy Perspective" })),
				MessageDialog.QUESTION, 0,
				IDialogConstants.OK_LABEL,
				IDialogConstants.CANCEL_LABEL);
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testSaveAsOverwrite() {
		Dialog dialog = new MessageDialog(getShell(), "WorkbenchMessages.Question", null, DUMMY_RELATIVE_PATH,
				MessageDialog.QUESTION, 0,
				IDialogConstants.YES_LABEL,
				IDialogConstants.NO_LABEL,
				IDialogConstants.CANCEL_LABEL);
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testSaveChanges() {
		Dialog dialog = new MessageDialog(getShell(), WorkbenchMessages.Save_Resource, null, NLS.bind(WorkbenchMessages.EditorManager_saveChangesQuestion, (new Object[] { DUMMY_RESOURCE })), MessageDialog.QUESTION,
				0,
				WorkbenchMessages.Save, WorkbenchMessages.Dont_Save,
				IDialogConstants.CANCEL_LABEL);
		DialogCheck.assertDialogTexts(dialog);
	}



	public void testWizardClosing() {
		Dialog dialog = new MessageDialog(getShell(), JFaceResources
				.getString("WizardClosingDialog_title"), null, JFaceResources
						.getString("WizardClosingDialog_message"),
				MessageDialog.QUESTION,
				0, IDialogConstants.OK_LABEL);
		DialogCheck.assertDialogTexts(dialog);
	}

	public void testWizardOverwrite() {
		Dialog dialog = new MessageDialog(getShell(), "WorkbenchMessages.Question", null, "WizardDataTransfer_existsQuestion", MessageDialog.QUESTION, 0,
				IDialogConstants.YES_LABEL,
				IDialogConstants.YES_TO_ALL_LABEL,
				IDialogConstants.NO_LABEL,
				IDialogConstants.CANCEL_LABEL);
		DialogCheck.assertDialogTexts(dialog);
	}

}

