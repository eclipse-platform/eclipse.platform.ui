/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Workbench common <code>Save As</code> action.
 */
public class SaveAsAction extends BaseSaveAction {

    /**
     *	Create an instance of this class
     */
    public SaveAsAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.getString("SaveAs.text"), window); //$NON-NLS-1$
        setActionDefinitionId("org.eclipse.ui.file.saveAs"); //$NON-NLS-1$
        setText(WorkbenchMessages.getString("SaveAs.text")); //$NON-NLS-1$
        setToolTipText(WorkbenchMessages.getString("SaveAs.toolTip")); //$NON-NLS-1$
        setId("saveAs"); //$NON-NLS-1$
        WorkbenchHelp.setHelp(this, IWorkbenchHelpContextIds.SAVE_AS_ACTION);
        setImageDescriptor(WorkbenchImages
                .getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_SAVEAS_EDIT));
        setDisabledImageDescriptor(WorkbenchImages
                .getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_SAVEAS_EDIT_DISABLED));
    }

    /* (non-Javadoc)
     * Method declared on Action.
     */
    public void run() {
        if (getWorkbenchWindow() == null) {
            // action has been disposed
            return;
        }
        /* **********************************************************************************
         * The code below was added to track the view with focus
         * in order to support save actions from a view. Remove this
         * experimental code if the decision is to not allow views to 
         * participate in save actions (see bug 10234) 
         */
        ISaveablePart saveView = getSaveableView();
        if (saveView != null) {
            saveView.doSaveAs();
            return;
        }
        /* **********************************************************************************/

        IEditorPart editor = getActiveEditor();
        if (editor != null) {
            editor.doSaveAs();
        }
    }

    /* (non-Javadoc)
     * Method declared on ActiveEditorAction.
     */
    protected void updateState() {
        /* **********************************************************************************
         * The code below was added to track the view with focus
         * in order to support save actions from a view. Remove this
         * experimental code if the decision is to not allow views to 
         * participate in save actions (see bug 10234) 
         */
        ISaveablePart saveView = getSaveableView();
        if (saveView != null) {
            setEnabled(saveView.isSaveAsAllowed());
            return;
        }
        /* **********************************************************************************/

        IEditorPart editor = getActiveEditor();
        setEnabled(editor != null && editor.isSaveAsAllowed());
    }
}