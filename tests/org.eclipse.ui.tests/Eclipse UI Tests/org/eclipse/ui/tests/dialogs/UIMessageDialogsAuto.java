/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import java.util.ResourceBundle;

import junit.framework.TestCase;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.tests.util.DialogCheck;

public class UIMessageDialogsAuto extends TestCase {
    private static final String DUMMY_RESOURCE = "Dummy.resource";

    private static final String DUMMY_PROJECT = "DummyProject";

    private static final String DUMMY_ABSOLUTE_PATH = "C:\\Dummypath\\Dummy.resource";

    private static final String DUMMY_RELATIVE_PATH = "\\" + DUMMY_PROJECT
            + "\\" + DUMMY_RESOURCE;

    public UIMessageDialogsAuto(String name) {
        super(name);
    }

    private Shell getShell() {
        return DialogCheck.getShell();
    }

    /*
     * Convenience method simliar to org.eclipse.jface.dialogs.MessageDialog::openConfirm.
     * The method will return the dialog instead of opening.
     * @param title the dialog's title, or <code>null</code> if none.
     * @param message the message.
     * @return Dialog the confirm dialog.
     */
    private MessageDialog getConfirmDialog(String title, String message) {
        return new MessageDialog(getShell(), title, null, message,
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.OK_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
    }

    /*
     * Convenience method simliar to org.eclipse.jface.dialogs.MessageDialog::openError.
     * The method will return the dialog instead of opening.
     * @param title the dialog's title, or <code>null</code> if none.
     * @param message the message.
     * @return MessageDialog the error confirm dialog.
     */
    private MessageDialog getErrorDialog(String title, String message) {
        return new MessageDialog(getShell(), title, null, message,
                MessageDialog.ERROR,
                new String[] { IDialogConstants.OK_LABEL }, 0);
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
                MessageDialog.INFORMATION,
                new String[] { IDialogConstants.OK_LABEL }, 0);
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
                MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL }, 0);
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
                MessageDialog.WARNING,
                new String[] { IDialogConstants.OK_LABEL }, 0);
    }

    public void testAbortPageFlipping() {
        Dialog dialog = getWarningDialog(JFaceResources
                .getString("AbortPageFlippingDialog.title"), JFaceResources
                .getString("AbortPageFlippingDialog.message"));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testCloseFileDeleted() {
        Dialog dialog = null;
        ResourceBundle bundle = ResourceBundle
                .getBundle("org.eclipse.ui.texteditor.EditorMessages");
        if (bundle != null) {
            dialog = getConfirmDialog(
                    bundle
                            .getString("Editor.error.activated.deleted.close.title"),
                    bundle
                            .getString("Editor.error.activated.deleted.close.message"));
        }
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testCopyOverwrite() {
        Dialog dialog = getQuestionDialog(WorkbenchMessages
                .getString("CopyResourceAction.resourceExists"),
                WorkbenchMessages.format(
                        "CopyResourceAction.overwriteQuestion",
                        new Object[] { DUMMY_RELATIVE_PATH }));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testDeleteProject() {
        String title = WorkbenchMessages
                .getString("DeleteResourceAction.titleProject");
        String msg = WorkbenchMessages.format(
                "DeleteResourceAction.confirmProject1", new Object[] {
                        DUMMY_PROJECT, DUMMY_ABSOLUTE_PATH });
        Dialog dialog = new MessageDialog(getShell(), title, null, // accept the default window icon
                msg, MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testDeleteReadOnlyCheck() {
        Dialog dialog = new MessageDialog(getShell(), WorkbenchMessages
                .getString("DeleteResourceAction.checkDelete"), null,
                WorkbenchMessages.format(
                        "DeleteResourceAction.readOnlyQuestion",
                        new Object[] { DUMMY_RESOURCE }),
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL,
                        IDialogConstants.YES_TO_ALL_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testDeleteResource() {
        Dialog dialog = getQuestionDialog(WorkbenchMessages
                .getString("DeleteResourceAction.title"), WorkbenchMessages
                .format("DeleteResourceAction.confirm1",
                        new Object[] { DUMMY_RESOURCE }));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testDeleteResources() {
        Dialog dialog = getQuestionDialog(WorkbenchMessages
                .getString("DeleteResourceAction.title"), WorkbenchMessages
                .format("DeleteResourceAction.confirmN",
                        new Object[] { DUMMY_RESOURCE }));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testDropOverwrite() {
        Dialog dialog = new MessageDialog(
                getShell(),
                ResourceNavigatorMessagesCopy.getString("DropAdapter.question"),
                null, ResourceNavigatorMessagesCopy.format(
                        "DropAdapter.overwriteQuery",
                        new Object[] { DUMMY_RELATIVE_PATH }),
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL,
                        IDialogConstants.YES_TO_ALL_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testErrorClosing() {
        Dialog dialog = getQuestionDialog(WorkbenchMessages.getString("Error"),
                WorkbenchMessages.getString("ErrorClosingNoArg"));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testFileChanged() {
        MessageDialog dialog = null;
        ResourceBundle bundle = ResourceBundle
                .getBundle("org.eclipse.ui.texteditor.EditorMessages");
        if (bundle != null) {
            dialog = getQuestionDialog(
                    bundle.getString("Editor.error.activated.outofsync.title"),
                    bundle
                            .getString("Editor.error.activated.outofsync.message"));
        }
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testFileExtensionEmpty() {
        Dialog dialog = getInformationDialog(
                WorkbenchMessages
                        .getString("FileEditorPreference.extensionEmptyTitle"),
                WorkbenchMessages
                        .getString("FileEditorPreference.extensionEmptyMessage"));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testFileNameWrong() {
        Dialog dialog = getInformationDialog(
                WorkbenchMessages
                        .getString("FileEditorPreference.fileNameInvalidTitle"),
                WorkbenchMessages
                        .getString("FileEditorPreference.fileNameInvalidMessage"));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testFileTypeExists() {
        Dialog dialog = getInformationDialog(WorkbenchMessages
                .getString("FileEditorPreference.existsTitle"),
                WorkbenchMessages
                        .getString("FileEditorPreference.existsMessage"));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testInvalidType_1() {
        Dialog dialog = getWarningDialog(WorkbenchMessages
                .getString("FileExtension.invalidTitle"), WorkbenchMessages
                .getString("FileExtension.invalidMessage"));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testInvalidType_2() {
        Dialog dialog = getWarningDialog(WorkbenchMessages
                .getString("FileExtension.invalidType"), WorkbenchMessages
                .getString("FileExtension.invalidTypeMessage"));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testMoveReadOnlyCheck() {
        Dialog dialog = new MessageDialog(getShell(), WorkbenchMessages
                .getString("MoveResourceAction.title"), null, WorkbenchMessages
                .format("MoveResourceAction.checkMoveMessage",
                        new Object[] { DUMMY_RESOURCE }),
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL,
                        IDialogConstants.YES_TO_ALL_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testNoBuilders() {
        Dialog dialog = getWarningDialog(WorkbenchMessages
                .getString("BuildAction.warning"), WorkbenchMessages
                .getString("BuildAction.noBuilders"));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testNoGlobalBuildersDialog() {
        Dialog dialog = getWarningDialog(WorkbenchMessages
                .getString("GlobalBuildAction.warning"), WorkbenchMessages
                .getString("GlobalBuildAction.noBuilders"));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testNoPropertyPage() {
        Dialog dialog = getInformationDialog(WorkbenchMessages
                .getString("PropertyDialog.messageTitle"), WorkbenchMessages
                .format("PropertyDialog.noPropertyMessage",
                        new Object[] { "DummyPropertyPage" }));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testNoSelectedExportResources() {
        Dialog dialog = getInformationDialog(DataTransferMessagesCopy
                .getString("DataTransfer.information"),
                DataTransferMessagesCopy.getString("FileExport.noneSelected"));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testNoSelectedImportResources() {
        Dialog dialog = getInformationDialog(DataTransferMessagesCopy
                .getString("DataTransfer.information"),
                DataTransferMessagesCopy.getString("FileImport.noneSelected"));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testOperationNotAvailable() {
        Dialog dialog = getInformationDialog(WorkbenchMessages
                .getString("Information"), WorkbenchMessages
                .getString("PluginActino.operationNotAvailableMessage"));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testOverwritePerspective() {
        Dialog dialog = new MessageDialog(getShell(), WorkbenchMessages
                .getString("SavePerspective.overwriteTitle"), null,
                WorkbenchMessages.format("SavePerspective.overwriteQuestion",
                        new Object[] { "Dummy Perspective" }),
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testRefreshDeleteProject() {
        Dialog dialog = new MessageDialog(getShell(), WorkbenchMessages
                .getString("RefreshAction.dialogTitle"), null,
                WorkbenchMessages.format(
                        "RefreshAction.locationDeletedMessage", new Object[] {
                                DUMMY_PROJECT,
                                "c:\\dummypath\\" + DUMMY_PROJECT }),
                MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testRenameOverwrite() {
        Dialog dialog = getQuestionDialog(WorkbenchMessages
                .getString("RenameResourceAction.resourceExists"),
                WorkbenchMessages.format(
                        "RenameResourceAction.overwriteQuestion",
                        new Object[] { DUMMY_RELATIVE_PATH }));
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testResetPerspective() {
        Dialog dialog = new MessageDialog(getShell(), WorkbenchMessages
                .getString("ResetPerspective.title"), null, WorkbenchMessages
                .format("ResetPerspective.message",
                        new Object[] { "Dummy Perspective" }),
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.OK_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testSaveAsOverwrite() {
        Dialog dialog = new MessageDialog(getShell(), WorkbenchMessages
                .getString("Question"), null, WorkbenchMessages.format(
                "SaveAsDialog.overwriteQuestion",
                new Object[] { DUMMY_RELATIVE_PATH }), MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testSaveChanges() {
        Dialog dialog = new MessageDialog(getShell(), WorkbenchMessages
                .getString("Save_Resource"), null, WorkbenchMessages.format(
                "EditorManager.saveChangesQuestion",
                new Object[] { DUMMY_RESOURCE }), MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testSaveFileDeleted() {
        MessageDialog dialog = null;
        ResourceBundle bundle = ResourceBundle
                .getBundle("org.eclipse.ui.texteditor.EditorMessages");
        if (bundle != null) {
            dialog = new MessageDialog(
                    getShell(),
                    bundle
                            .getString("Editor.error.activated.deleted.save.title"),
                    null,
                    bundle
                            .getString("Editor.error.activated.deleted.save.message"),
                    MessageDialog.QUESTION,
                    new String[] {
                            bundle
                                    .getString("Editor.error.activated.deleted.save.button.save"),
                            bundle
                                    .getString("Editor.error.activated.deleted.save.button.close") },
                    0);
        }
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testUpdateConflict() {
        MessageDialog dialog = null;
        ResourceBundle bundle = ResourceBundle
                .getBundle("org.eclipse.ui.texteditor.EditorMessages");
        if (bundle != null) {
            dialog = getQuestionDialog(bundle
                    .getString("Editor.error.save.outofsync.title"), bundle
                    .getString("Editor.error.save.outofsync.message"));
        }
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testWizardClosing() {
        Dialog dialog = new MessageDialog(getShell(), JFaceResources
                .getString("WizardClosingDialog.title"), null, JFaceResources
                .getString("WizardClosingDialog.message"),
                MessageDialog.QUESTION,
                new String[] { IDialogConstants.OK_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

    public void testWizardOverwrite() {
        Dialog dialog = new MessageDialog(getShell(), WorkbenchMessages
                .getString("Question"), null, WorkbenchMessages.format(
                "WizardDataTransfer.existsQuestion",
                new Object[] { DUMMY_ABSOLUTE_PATH }), MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.YES_TO_ALL_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
    }

}

