/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Action representing a generic export operation.
 * <p>
 * This class may be instantiated. It is not intended to be subclassed.
 * </p>
 * <p>
 * This method automatically registers listeners so that it can keep its
 * enablement state up to date. Ordinarily, the window's references to these
 * listeners will be dropped automatically when the window closes. However,
 * if the client needs to get rid of an action while the window is still open,
 * the client must call IWorkbenchAction#dispose to give the
 * action an opportunity to deregister its listeners and to perform any other
 * cleanup.
 * </p>
 * <p>
 * Note: Despite the name, an export operation can deal with things other than
 * resources; the current name was retained for historical reasons.
 * </p>
 * 
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ExportResourcesAction extends BaseSelectionListenerAction
        implements ActionFactory.IWorkbenchAction {
    /**
     * Reference to the WorkbenchCommandAction that executes the Export Wizard.
     */
    private ActionFactory.IWorkbenchAction action;

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * Create a new instance of this class.
     * 
     * @param window the window
     */
    public ExportResourcesAction(IWorkbenchWindow window) {
        this(window, WorkbenchMessages.ExportResourcesAction_text);
    }

    /**
     * Create a new instance of this class.
     * 
     * @param window the window
     * @param label the label 
     */
    public ExportResourcesAction(IWorkbenchWindow window, String label) {
        super(label);
        if (window == null) {
            throw new IllegalArgumentException();
        }
        
        this.workbenchWindow = window;
        action = ActionFactory.EXPORT.create(window);
        
        setText(action.getText()); 
        setToolTipText(action.getToolTipText());
        setId(action.getId());
        setActionDefinitionId(action.getActionDefinitionId());
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.EXPORT_ACTION);
        setImageDescriptor(action.getImageDescriptor());
    }

    /**
     * Create a new instance of this class
     * 
     * @param workbench the workbench
     * @deprecated use the constructor <code>ExportResourcesAction(IWorkbenchWindow)</code>
     */
    public ExportResourcesAction(IWorkbench workbench) {
        this(workbench.getActiveWorkbenchWindow());
    }

    /**
     * Create a new instance of this class.
     * 
     * @param workbench the workbench 
     * @param label the label
     * @deprecated use the constructor <code>ExportResourcesAction(IWorkbenchWindow, String)</code>
     */
    public ExportResourcesAction(IWorkbench workbench, String label) {
        this(workbench.getActiveWorkbenchWindow(), label);
    }

    /**
     * Invoke the Export wizards selection Wizard.
     */
    public void run() {
        if (workbenchWindow == null) {
            // action has been disposed
            return;
        }
        action.run();
    }

    /**
     * Sets the current selection. 
     * In for backwards compatability. Use selectionChanged() instead.
     * @param selection the new selection
     * @deprecated
     */
    public void setSelection(IStructuredSelection selection) {
        selectionChanged(selection);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionFactory.IWorkbenchAction#dispose()
	 */
	public void dispose() {
		workbenchWindow = null;
		if (action!=null) {
			action.dispose();
		}
		action = null;
	}
}
