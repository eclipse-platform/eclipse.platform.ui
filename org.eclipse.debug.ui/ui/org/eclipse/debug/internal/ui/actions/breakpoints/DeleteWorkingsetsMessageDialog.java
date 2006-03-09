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
package org.eclipse.debug.internal.ui.actions.breakpoints;

import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Allows the user to specify if they want to delete the working set, all the breakpoints in the working
 * set or both
 * @since 3.2
 */
public class DeleteWorkingsetsMessageDialog extends MessageDialog {

	/**
	 * to determine if we should delete the working set as well 
	 */
	private boolean fDeleteWorkingsets = false;
	
	/**
	 * to determine if we should delete all the breakpoints in the set.
	 * to maintain backward compatibility this is by default true 
	 */
	private boolean fDeleteBreakpoints = true;
	
	//widgets
	private Button fDeleteWS;
	private Button fDeleteBPS;
	
	public DeleteWorkingsetsMessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage, int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createCustomArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		Font font = parent.getFont();
		fDeleteWS = new Button(comp, SWT.CHECK);
		fDeleteWS.setText(ActionMessages.DeleteWorkingsetsMessageDialog_0);
		fDeleteWS.setFont(font);
		fDeleteWS.setSelection(fDeleteWorkingsets);
		
		fDeleteBPS = new Button(comp, SWT.CHECK);
		fDeleteBPS.setText(ActionMessages.DeleteWorkingsetsMessageDialog_1);
		fDeleteBPS.setFont(font);
		fDeleteBPS.setSelection(fDeleteBreakpoints);
		return comp;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.MessageDialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if(buttonId == 0) {
			fDeleteBreakpoints = fDeleteBPS.getSelection();
			fDeleteWorkingsets = fDeleteWS.getSelection();
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * return the checked value of the delete working set check box
	 * @return the checked state of the delete working set check box
	 */
	public boolean deleteWorkingset() {
		return fDeleteWorkingsets;
	}
	
	/**
	 * returns the checked state of the delete all breakpoints in working set check box 
	 * @return the checked state of the delete all breakpoints... check box
	 */
	public boolean deleteAllBreakpoints() {
		return fDeleteBreakpoints;
	}
	
}
