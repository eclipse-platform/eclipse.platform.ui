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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * 
 */
public class SetDefaultBreakpointGroupAction extends AbstractBreakpointsViewAction {
    
    private class SetDefaultGroupDialog extends InputDialog {

        public SetDefaultGroupDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue, IInputValidator validator) {
            super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
        }
        
        /**
		 * @see Dialog#createDialogArea(Composite)
		 */
		protected Control createDialogArea(Composite parent) {
			Composite area= (Composite) super.createDialogArea(parent);
			
			Button button= SWTUtil.createPushButton(area, "Browse...", null);
			GridData data= (GridData) button.getLayoutData();
			data.horizontalAlignment= GridData.BEGINNING;
			data.verticalAlignment= GridData.BEGINNING;
			

            Object[] children = fView.getTreeContentProvider().getElements(fView.getViewer().getInput());
            final List groups= new ArrayList();
            for (int i = 0; i < children.length; i++) {
                Object child= children[i];
                if (child instanceof String) {
                    groups.add(child);
                }
            }
            
            if (groups.size() > 0) {
				button.addSelectionListener(new SelectionAdapter() {
		            public void widgetSelected(SelectionEvent e) {
		                handleBrowsePressed(groups.toArray());
		            }
		        });
            } else {
                button.setEnabled(false);
            }
			
			return area;
		}

        /**
         * The browse button has been pressed. Prompt the user to choose a group.
         */
        private void handleBrowsePressed(final Object[] groups) {
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
            dialog.setMessage("Choose a group");
            dialog.setTitle("Choose Default Group");
            if (dialog.open() != Dialog.OK) {
                return;
            }
            Object[] result = dialog.getResult();
            getText().setText((String) result[0]);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        String currentGroup= fView.getAutoGroup();
        if (currentGroup == null) {
            currentGroup= "";
        }
        InputDialog dialog= new SetDefaultGroupDialog(fView.getViewSite().getShell(), "Set Default Group", "Specify the group in which new breakpoints will be automatically placed:", currentGroup, null);
        if (dialog.open() != Dialog.OK) {
            return;
        }
        String group= dialog.getValue();
        fView.setAutoGroup(group);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

}
