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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Global action that saves all targets in the
 * workbench that implement ISaveTarget interface.
 * The action keeps track of opened save targets
 * and their 'save' state. If none of the currently
 * opened targets needs saving, it will disable.
 * This action is somewhat different from all
 * other global actions in that it works on
 * multiple targets at the same time i.e. it
 * does not disconnect from the target when it
 * becomes deactivated.
 */
public class SaveAllAction extends PageEventAction implements IPropertyListener {
    /**
     * List of parts (element type: <code>IWorkbenchPart</code>)
     * against which this class has outstanding property listeners registered.
     */
    private List partsWithListeners = new ArrayList(1);

    /**
     * The default constructor.
     */
    public SaveAllAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.getString("SaveAll.text"), window); //$NON-NLS-1$
        setToolTipText(WorkbenchMessages.getString("SaveAll.toolTip")); //$NON-NLS-1$
        setId("saveAll"); //$NON-NLS-1$
        setEnabled(false);
        WorkbenchHelp.setHelp(this, IHelpContextIds.SAVE_ALL_ACTION);
        setImageDescriptor(WorkbenchImages
                .getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_SAVEALL_EDIT));
        setDisabledImageDescriptor(WorkbenchImages
                .getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_SAVEALL_EDIT_DISABLED));
        setActionDefinitionId("org.eclipse.ui.file.saveAll"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * Method declared on PageEventAction.
     */
    public void pageActivated(IWorkbenchPage page) {
        super.pageActivated(page);
        updateState();
    }

    /* (non-Javadoc)
     * Method declared on PageEventAction.
     */
    public void pageClosed(IWorkbenchPage page) {
        super.pageClosed(page);
        updateState();
    }

    /* (non-Javadoc)
     * Method declared on PartEventAction.
     */
    public void partClosed(IWorkbenchPart part) {
        super.partClosed(part);
        if (part instanceof IEditorPart) {
            part.removePropertyListener(this);
            partsWithListeners.remove(part);
            updateState();
        }
    }

    /* (non-Javadoc)
     * Method declared on PartEventAction.
     */
    public void partOpened(IWorkbenchPart part) {
        super.partOpened(part);
        if (part instanceof IEditorPart) {
            part.addPropertyListener(this);
            partsWithListeners.add(part);
            updateState();
        }
    }

    /* (non-Javadoc)
     * Method declared on IPropertyListener.
     */
    public void propertyChanged(Object source, int propID) {
        if (source instanceof IEditorPart) {
            if (propID == IEditorPart.PROP_DIRTY) {
                updateState();
            }
        }
    }

    /* (non-Javadoc)
     * Method declared on Action.
     */
    public void run() {
        if (getWorkbenchWindow() == null) {
            // action has been disposed
            return;
        }
        IWorkbenchPage page = getActivePage();
        if (page != null) {
            page.saveAllEditors(false);
        }
    }

    /**
     * Updates availability depending on number of
     * targets that need saving.
     */
    protected void updateState() {
        IWorkbenchPage page = getActivePage();
        setEnabled(page != null && page.getDirtyEditors().length > 0);
    }

    /* (non-Javadoc)
     * Method declared on PageEventAction.
     */
    public void dispose() {
        super.dispose();
        for (Iterator it = partsWithListeners.iterator(); it.hasNext();) {
            IWorkbenchPart part = (IWorkbenchPart) it.next();
            part.removePropertyListener(this);
        }
        partsWithListeners.clear();
    }
}