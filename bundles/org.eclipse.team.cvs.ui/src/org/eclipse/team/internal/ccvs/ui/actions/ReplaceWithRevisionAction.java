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
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.history.FileRevisionTypedElement;
import org.eclipse.team.ui.SaveablePartDialog;
import org.eclipse.team.ui.history.*;

/**
 * Displays a compare dialog and allows the same behavior as the compare. In addition
 * a replace button is added to the dialog that will replace the local with the currently 
 * selected revision.
 * 
 * @since 3.0
 */
public class ReplaceWithRevisionAction extends CompareWithRevisionAction {
	
	protected static final int REPLACE_ID = 10;
	
	protected class ReplaceSaveablePart extends HistoryPageSaveablePart {
		ICompareInput compareInput;
		
		public ReplaceSaveablePart(Shell shell, CompareConfiguration configuration, IHistoryPageSource pageSource, Object object) {
			super(shell, configuration, pageSource, object);
		}
		
		protected void prepareInput(ICompareInput input, CompareConfiguration configuration, IProgressMonitor monitor) throws InvocationTargetException {
			super.prepareInput(input, configuration, monitor);
			compareInput = input;
			configuration.setLeftEditable(false);
		}
		
		public void replaceLocalWithCurrentlySelectedRevision() throws CoreException{
			FileRevisionTypedElement right = (FileRevisionTypedElement) compareInput.getRight();
			IEditableContent left = (IEditableContent)compareInput.getLeft();
			left.setContent(Utils.readBytes(right.getContents()));
		}
		
		
		public ISelectionProvider getReplaceSelectionProvider(){
			return getSelectionProvider();
		}
	}
	protected class ReplaceCompareDialog extends SaveablePartDialog {
		private Button replaceButton;
		
		public ReplaceCompareDialog(Shell shell, ReplaceSaveablePart input) {
			super(shell, input);
		}
		
		/**
		 * Add the replace button to the dialog.
		 */
		protected void createButtonsForButtonBar(Composite parent) {
			replaceButton = createButton(parent, REPLACE_ID, CVSUIMessages.ReplaceWithRevisionAction_0, true); 
			replaceButton.setEnabled(false);
			((ReplaceSaveablePart) getInput()).getReplaceSelectionProvider().addSelectionChangedListener(
				new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent e) {
						IStructuredSelection s= (StructuredSelection) e.getSelection();
						replaceButton.setEnabled(s != null && ! s.isEmpty() && s.size() == 1);
					}
				}
			);
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false); 
			// Don't call super because we don't want the OK button to appear
		}
		
		/**
		 * If the replace button was pressed.
		 */
		protected void buttonPressed(int buttonId) {
			if(buttonId == REPLACE_ID) {
				try {
					((ReplaceSaveablePart) getInput()).replaceLocalWithCurrentlySelectedRevision();
				} catch (CoreException e) {
					Utils.handle(e);
				}
				buttonId = IDialogConstants.OK_ID;
			}
			super.buttonPressed(buttonId);
		}
	}
	
	protected void showCompareInDialog(Shell shell, Object object){
		IHistoryPageSource pageSource = HistoryPageSource.getHistoryPageSource(object);
		if (pageSource != null && pageSource.canShowHistoryFor(object)) {
			CompareConfiguration cc = new CompareConfiguration();
			cc.setLeftEditable(false);
			cc.setRightEditable(false);
			ReplaceSaveablePart input = new ReplaceSaveablePart(shell, cc, pageSource, object);
			try {
				SaveablePartDialog cd = new ReplaceCompareDialog(shell, input);
				cd.setBlockOnOpen(true);
				cd.open();
			} finally {
				input.dispose();
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CompareWithRevisionAction#getActionTitle()
	 */
	protected String getActionTitle() {
		return CVSUIMessages.ReplaceWithRevisionAction_1; 
	}
}
