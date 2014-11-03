/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import java.text.MessageFormat;

import junit.framework.TestCase;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;
import org.eclipse.ui.tests.harness.util.DialogCheck;

public class UIMessageDialogs extends TestCase {
    private static final String DUMMY_RESOURCE = "Dummy.resource";

    private static final String DUMMY_PROJECT = "DummyProject";

    private static final String DUMMY_ABSOLUTE_PATH = "C:\\Dummypath\\Dummy.resource";

    private static final String DUMMY_RELATIVE_PATH = "\\" + DUMMY_PROJECT
            + "\\" + DUMMY_RESOURCE;

    public UIMessageDialogs(String name) {
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
        DialogCheck.assertDialog(dialog, this);
    }


    public void testCopyOverwrite() {
        Dialog dialog = getQuestionDialog("Exists","Overwrite?");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testDeleteProject() {
        String title ="Delete Project";
        String msg = NLS.bind("Delete", (new Object[] {
		DUMMY_PROJECT, DUMMY_ABSOLUTE_PATH }));
        Dialog dialog = new MessageDialog(getShell(), title, null, // accept the default window icon
                msg, MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testDeleteReadOnlyCheck() {
        Dialog dialog = new MessageDialog(getShell(),"Delete?", null,
               "This?",
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL,
                        IDialogConstants.YES_TO_ALL_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testDeleteResource() {
        Dialog dialog = getQuestionDialog("Delete","Delete?");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testDeleteResources() {
        Dialog dialog = getQuestionDialog("Delete","OK?");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testDropOverwrite() {
        Dialog dialog = new MessageDialog(
                getShell(),
                ResourceNavigatorMessages.DropAdapter_question,
                null, MessageFormat.format(ResourceNavigatorMessages.DropAdapter_overwriteQuery,
                        new Object[] { DUMMY_RELATIVE_PATH }),
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL,
                        IDialogConstants.YES_TO_ALL_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testErrorClosing() {
        Dialog dialog = getQuestionDialog(WorkbenchMessages.Error,
                WorkbenchMessages.ErrorClosingNoArg);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testFileExtensionEmpty() {
        Dialog dialog = getInformationDialog("","");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testFileNameWrong() {
        Dialog dialog = getInformationDialog(
                "Invalid",
               "Invalid file");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testFileTypeExists() {
        Dialog dialog = getInformationDialog("Exists",
                "Already Exists");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testInvalidType_1() {
        Dialog dialog = getWarningDialog("Invalid?", "Is this invalid?");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testInvalidType_2() {
        Dialog dialog = getWarningDialog("Invalid",  "Is this invalid?");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testMoveReadOnlyCheck() {
        Dialog dialog = new MessageDialog(getShell(), "Move", null, "OK to move",
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL,
                        IDialogConstants.YES_TO_ALL_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialog(dialog, this);
    }

    /*
     public void testNoBuilders() {
     Dialog dialog = getWarningDialog(
     WorkbenchMessages.getString("BuildAction.warning"),
     WorkbenchMessages.getString("BuildAction.noBuilders") );
     DialogCheck.assertDialog(dialog, this);
     }
     */
    /*
     public void testNoGlobalBuildersDialog() {
     Dialog dialog = getWarningDialog(
     WorkbenchMessages.getString("GlobalBuildAction.warning"),
     WorkbenchMessages.getString("GlobalBuildAction.noBuilders") );
     DialogCheck.assertDialog(dialog, this);
     }
     */
    public void testNoPropertyPage() {
        Dialog dialog = getInformationDialog(WorkbenchMessages.PropertyDialog_messageTitle, NLS.bind(WorkbenchMessages.PropertyDialog_noPropertyMessage, (new Object[] { "DummyPropertyPage" })));
        DialogCheck.assertDialog(dialog, this);
    }

    public void testOperationNotAvailable() {
        Dialog dialog = getInformationDialog(WorkbenchMessages.Information, "Not available");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testOverwritePerspective() {
        Dialog dialog = new MessageDialog(getShell(), WorkbenchMessages.SavePerspective_overwriteTitle, null,
                NLS.bind(WorkbenchMessages.SavePerspective_overwriteQuestion, (new Object[] { "Dummy Perspective" })),
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testRefreshDeleteProject() {
        Dialog dialog = new MessageDialog(getShell(), "Refresh", null,
                NLS.bind("deleted location", (new Object[] {
				DUMMY_PROJECT,
				"c:\\dummypath\\" + DUMMY_PROJECT })),
                MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL }, 0);
        DialogCheck.assertDialogTexts(dialog, this);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testRenameOverwrite() {
        Dialog dialog = getQuestionDialog("Exists","Overwrite");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testResetPerspective() {
        Dialog dialog = new MessageDialog(getShell(), WorkbenchMessages.ResetPerspective_title, null, NLS.bind(WorkbenchMessages.ResetPerspective_message, (new Object[] { "Dummy Perspective" })),
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.OK_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testSaveAsOverwrite() {
        Dialog dialog = new MessageDialog(getShell(), "OK?", null, "Overwrite?", MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testSaveChanges() {
        Dialog dialog = new MessageDialog(getShell(), WorkbenchMessages.Save_Resource, null, NLS.bind(WorkbenchMessages.EditorManager_saveChangesQuestion, (new Object[] { DUMMY_RESOURCE })), MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testWizardClosing() {
        Dialog dialog = new MessageDialog(getShell(), JFaceResources
                .getString("WizardClosingDialog.title"), null, JFaceResources
                .getString("WizardClosingDialog.message"),
                MessageDialog.QUESTION,
                new String[] { IDialogConstants.OK_LABEL }, 0);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testWizardOverwrite() {
        Dialog dialog = new MessageDialog(getShell(), "OK?", null, "Exists", MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.YES_TO_ALL_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        DialogCheck.assertDialog(dialog, this);
    }
}

