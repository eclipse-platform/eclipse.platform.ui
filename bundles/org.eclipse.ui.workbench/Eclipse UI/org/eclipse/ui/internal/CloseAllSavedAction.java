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
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 *	Closes all editors except ones with unsaved changes.
 */
public class CloseAllSavedAction extends PageEventAction implements
        IPropertyListener {

    /**
     * List of parts (element type: <code>IWorkbenchPart</code>)
     * against which this class has outstanding property listeners registered.
     */
    private List partsWithListeners = new ArrayList(1);

    /**
     *	Create an instance of this class
     */
    public CloseAllSavedAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.getString("CloseAllSavedAction.text"), window); //$NON-NLS-1$
        setToolTipText(WorkbenchMessages
                .getString("CloseAllSavedAction.toolTip")); //$NON-NLS-1$
        // @issue Should create a ID in IWorkbenchActionConstants when it becames API?
        setId("closeAllSaved"); //$NON-NLS-1$
        updateState();
        WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_ALL_SAVED_ACTION);
        setActionDefinitionId("org.eclipse.ui.file.closeAllSaved"); //$NON-NLS-1$
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
            // action has been dispose
            return;
        }
        IWorkbenchPage page = getActivePage();
        if (page != null) {
            ((WorkbenchPage) page).closeAllSavedEditors();
        }
    }

    /**
     * Enable the action if there at least one editor open.
     */
    private void updateState() {
        IWorkbenchPage page = getActivePage();
        if (page == null) {
            setEnabled(false);
            return;
        }
        IEditorReference editors[] = page.getEditorReferences();
        for (int i = 0; i < editors.length; i++) {
            if (!editors[i].isDirty()) {
                setEnabled(true);
                return;
            }
        }
        setEnabled(false);
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