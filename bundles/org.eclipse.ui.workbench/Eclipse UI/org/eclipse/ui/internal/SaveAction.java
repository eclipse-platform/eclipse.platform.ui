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
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Workbench common <code>Save</code> action.
 */
public class SaveAction extends BaseSaveAction implements IBackgroundSaveListener {

    /**
     * Create an instance of this class
     * 
     * @param window the window
     */
    public SaveAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.SaveAction_text, window); 
        setText(WorkbenchMessages.SaveAction_text); 
        setToolTipText(WorkbenchMessages.SaveAction_toolTip);
        setId("save"); //$NON-NLS-1$
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.SAVE_ACTION);
        setImageDescriptor(WorkbenchImages
                .getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));
        setDisabledImageDescriptor(WorkbenchImages
                .getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT_DISABLED));
        setActionDefinitionId("org.eclipse.ui.file.save"); //$NON-NLS-1$
        ((WorkbenchWindow)window).addBackgroundSaveListener(this);
    }
    
    public void dispose() {
    	((WorkbenchWindow)getWorkbenchWindow()).removeBackgroundSaveListener(this);
    	super.dispose();
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     * Performs the <code>Save</code> action by calling the
     * <code>IEditorPart.doSave</code> method on the active editor.
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
            ((WorkbenchPage) getActivePart().getSite().getPage()).savePart(
                    saveView, getActivePart(), false);
            return;
        }

        IEditorPart part = getActiveEditor();
        if (part != null) {
            IWorkbenchPage page = part.getSite().getPage();
            page.saveEditor(part, false);
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
        ISaveablePart saveable = getSaveableView();
        if (saveable == null) {
        	saveable = getActiveEditor();
        }
        /* **********************************************************************************/
        if (saveable instanceof ISaveablesSource) {
			ISaveablesSource modelSource = (ISaveablesSource) saveable;
			setEnabled(SaveableHelper.needsSave(modelSource));
			return;
        }
        setEnabled(saveable != null && saveable.isDirty());
    }

	public void handleBackgroundSaveStarted() {
		updateState();
	}
}
