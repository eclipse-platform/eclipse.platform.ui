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

import java.util.ResourceBundle;

import junit.framework.TestCase;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.tests.harness.util.DialogCheck;

public class TextMessageDialogs extends TestCase {

    public TextMessageDialogs(String name) {
        super(name);
    }

    private Shell getShell() {
        return DialogCheck.getShell();
    }

    /**
     * Returns the given string from the Text Editor's resource bundle.
     * Should call org.eclipse.ui.texteditor.EditorMessages directly,
     * but it has package visibility.
     */
    private String getEditorString(String id) {
        ResourceBundle bundle = ResourceBundle
                .getBundle("org.eclipse.ui.texteditor.EditorMessages");
        assertNotNull("EditorMessages", bundle);
        String string = bundle.getString(id);
        assertNotNull(id, string);
        return string;
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

   public void testCloseFileDeleted() {
        Dialog dialog = getConfirmDialog(
                getEditorString("Editor_error_activated_deleted_close_title"),
                getEditorString("Editor_error_activated_deleted_close_message"));
        DialogCheck.assertDialog(dialog, this);
    }

    public void testFileChanged() {
        MessageDialog dialog = getQuestionDialog(
                getEditorString("Editor_error_activated_outofsync_title"),
                getEditorString("Editor_error_activated_outofsync_message"));
        DialogCheck.assertDialog(dialog, this);
    }

  
    public void testSaveFileDeleted() {
        MessageDialog dialog = new MessageDialog(
                getShell(),
                getEditorString("Editor_error_activated_deleted_save_title"),
                null,
                getEditorString("Editor_error_activated_deleted_save_message"),
                MessageDialog.QUESTION,
                new String[] {
                        getEditorString("Editor_error_activated_deleted_save_button_save"),
                        getEditorString("Editor_error_activated_deleted_save_button_close") },
                0);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testUpdateConflict() {
        MessageDialog dialog = getQuestionDialog(
                getEditorString("Editor_error_save_outofsync_title"),
                getEditorString("Editor_error_save_outofsync_message"));
        DialogCheck.assertDialog(dialog, this);
    }


}

