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
package org.eclipse.ui.internal;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Workbench common <code>Save As</code> action.
 */
public class SaveAsAction extends BaseSaveAction {

    /**
     * Create an instance of this class
     * 
     * @param window the window
     */
    public SaveAsAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.SaveAs_text, window); 
        setActionDefinitionId("org.eclipse.ui.file.saveAs"); //$NON-NLS-1$
        setText(WorkbenchMessages.SaveAs_text); 
        setToolTipText(WorkbenchMessages.SaveAs_toolTip); 
        setId("saveAs"); //$NON-NLS-1$
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.SAVE_AS_ACTION);
        setImageDescriptor(WorkbenchImages
                .getImageDescriptor(ISharedImages.IMG_ETOOL_SAVEAS_EDIT));
        setDisabledImageDescriptor(WorkbenchImages
                .getImageDescriptor(ISharedImages.IMG_ETOOL_SAVEAS_EDIT_DISABLED));
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
         * in order to support save actions from a view (see bug 10234). 
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
         * in order to support save actions from a view (see bug 10234). 
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
