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

import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 */
public class AddBreakpointToGroupAction implements IViewActionDelegate {
	
	private Object[] fBreakpoints= null;
	private BreakpointsView fView= null;
	
	private static String fgLastValue= null;
	
	/**
	 * A dialog that sets the focus to the text area.
	 */
	class BreakpointGroupDialog extends InputDialog {
		
		private Button fAutoAddToGroup;
		
		protected  BreakpointGroupDialog(Shell parentShell,
									String dialogTitle,
									String dialogMessage,
									String initialValue,
									IInputValidator validator) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
		}
		
		
		/**
		 * @see Dialog#createDialogArea(Composite)
		 */
		protected Control createDialogArea(Composite parent) {
			Composite area= (Composite)super.createDialogArea(parent);
			
			fAutoAddToGroup = new Button(area, SWT.CHECK);
			GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			fAutoAddToGroup.setLayoutData(data);
			fAutoAddToGroup.setFont(parent.getFont());
			fAutoAddToGroup.setText("Automatically add new breakpoints to this group");
			fAutoAddToGroup.setSelection(fView.getAutoGroup() != null);
			
			return area;
		}
		protected void okPressed() {
		    String value= getValue();
		    if (fAutoAddToGroup.getSelection()) {
		        fView.setAutoGroup(value);
		    }
		    fgLastValue= value;
			super.okPressed();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		String initialValue= fView.getAutoGroup();
		if (initialValue == null) {
		    initialValue= fgLastValue;
		}
		BreakpointGroupDialog dialog = new BreakpointGroupDialog(null, "Add To Group", "Specify the name of the group", initialValue, null);
		int dialogResult = dialog.open();
		if (dialogResult == Window.OK) {
			String value= dialog.getValue();
			if (value.equals("")) {
				value= null;
			}
			try {
				for (int i = 0; i < fBreakpoints.length; i++) {
					((IBreakpoint) fBreakpoints[i]).setGroup(value);
				}
			} catch (CoreException e) {
				DebugUIPlugin.errorDialog(dialog.getShell(), "Error Adding To Group", "An exception occurred while attempting to add the breakpoint to the specified group.", e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection sel) {
		IStructuredSelection selection= (IStructuredSelection) sel;
		Iterator iterator = selection.iterator();
		while (iterator.hasNext()) {
			if (!(iterator.next() instanceof IBreakpoint)) {
				action.setEnabled(false);
				fBreakpoints= null;
				return;
			}
		}
		action.setEnabled(true);
		fBreakpoints= selection.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		fView= (BreakpointsView) view;
	}

}
