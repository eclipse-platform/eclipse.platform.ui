/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * An input dialog which prompts the user for a breakpoint
 * group.
 */
class SelectBreakpointGroupDialog extends InputDialog {

    /**
     * Creates a new SelectBreakpointGroupDialog.
     * 
     * @param view the breakpoints view
     * @param dialogTitle the dialog title, or <code>null</code> if none
     * @param dialogMessage the dialog message, or <code>null</code> if none
     * @param initialValue the initial input value, or <code>null</code> if none
     *  (equivalent to the empty string)
     * @param validator an input validator, or <code>null</code> if none
     * 
     * @see org.eclipse.jface.dialogs.InputDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue, IInputValidator validator)
     */
    public SelectBreakpointGroupDialog(BreakpointsView view, String dialogTitle, String dialogMessage, String initialValue, IInputValidator validator) {
        super(view.getViewSite().getShell(), dialogTitle, dialogMessage, initialValue, validator);
    }
  
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite area= (Composite) super.createDialogArea(parent);
		
		Button button= SWTUtil.createPushButton(area, BreakpointGroupMessages.getString("SelectBreakpointGroupDialog.0"), null); //$NON-NLS-1$
		GridData data= (GridData) button.getLayoutData();
		data.horizontalAlignment= GridData.BEGINNING;
		data.verticalAlignment= GridData.BEGINNING;

        final String[] groups= getGroups();
        if (groups.length > 0) {
			button.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {
	                handleBrowsePressed(groups);
	            }
	        });
        } else {
            button.setEnabled(false);
        }
		
		return area;
	}
	
	/**
	 * Returns all existing custom breakpoint groups
	 * @return all existing custom breakpoint groups
	 */
	protected String[] getGroups() {
	    IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
	    Set groups= new HashSet();
	    for (int i = 0; i < breakpoints.length; i++) {
            try {
                String group = breakpoints[i].getGroup();
                if (group != null) {
                    groups.add(group);
                }
            } catch (CoreException e) {
                DebugUIPlugin.log(e);
            }
        }
	    return (String[]) groups.toArray(new String[groups.size()]);
	}

    /**
     * The browse button has been pressed. Prompt the user to choose a group.
     */
    private void handleBrowsePressed(Object[] groups) {
        ElementListSelectionDialog dialog= new ElementListSelectionDialog(
                getShell(),
                new LabelProvider() {
                    public Image getImage(Object element) {
                        return DebugPluginImages.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT_GROUP);
                    }

                    public String getText(Object element) {
                        return (String) element;
                    }
                });
        dialog.setElements(groups);
        dialog.setMultipleSelection(false);
        dialog.setMessage(BreakpointGroupMessages.getString("SelectBreakpointGroupDialog.1")); //$NON-NLS-1$
        dialog.setTitle(BreakpointGroupMessages.getString("SelectBreakpointGroupDialog.2")); //$NON-NLS-1$
        if (dialog.open() != Window.OK) {
            return;
        }
        Object[] result = dialog.getResult();
        getText().setText((String) result[0]);
    }
}