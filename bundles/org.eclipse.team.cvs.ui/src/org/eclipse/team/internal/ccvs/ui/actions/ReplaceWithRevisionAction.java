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
package org.eclipse.team.internal.ccvs.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.ui.CVSCompareRevisionsInput;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.synchronize.viewers.SynchronizeDialog;

/**
 * Displays a compare dialog and allows the same behavior as the compare. In addition
 * a replace button is added to the dialog that will replace the local with the currently 
 * selected revision.
 * 
 * @since 3.0
 */
public class ReplaceWithRevisionAction extends CompareWithRevisionAction {
	
	protected static final int REPLACE_ID = 10;
	
	protected class ReplaceCompareDialog extends SynchronizeDialog {
		private Button replaceButton;
		
		public ReplaceCompareDialog(Shell shell, String title, CVSCompareRevisionsInput input) {
			super(shell, title, input);
		}
		
		/**
		 * Add the replace button to the dialog.
		 */
		protected void createButtonsForButtonBar(Composite parent) {
			replaceButton = createButton(parent, REPLACE_ID, Policy.bind("ReplaceWithRevisionAction.0"), false); //$NON-NLS-1$
			replaceButton.setEnabled(false);
			((CVSCompareRevisionsInput)getCompareEditorInput()).getViewer().addSelectionChangedListener(
				new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent e) {
						ISelection s= e.getSelection();
						replaceButton.setEnabled(s != null && ! s.isEmpty());
					}
				}
			);
			super.createButtonsForButtonBar(parent);
		}
		
		/**
		 * If the replace button was pressed.
		 */
		protected void buttonPressed(int buttonId) {
			if(buttonId == REPLACE_ID) {
				CVSCompareRevisionsInput input = (CVSCompareRevisionsInput)getCompareEditorInput();
				try {
					input.replaceLocalWithCurrentlySelectedRevision();
				} catch (CoreException e) {
					Utils.handle(e);
				}
				buttonId = IDialogConstants.OK_ID;
			}
			super.buttonPressed(buttonId);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CompareWithRevisionAction#createCompareDialog(org.eclipse.swt.widgets.Shell, org.eclipse.team.internal.ccvs.ui.CVSCompareRevisionsInput)
	 */
	protected SynchronizeDialog createCompareDialog(Shell shell, CVSCompareRevisionsInput input) {
		return new ReplaceCompareDialog(shell, Policy.bind("ReplaceWithRevisionAction.1"), input); //$NON-NLS-1$
	}
}
