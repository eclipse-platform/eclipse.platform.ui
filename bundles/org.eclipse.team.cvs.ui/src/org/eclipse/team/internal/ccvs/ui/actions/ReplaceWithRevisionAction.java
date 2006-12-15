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

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.ui.CVSHistoryPage;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.history.FileRevisionTypedElement;
import org.eclipse.team.ui.history.*;

/**
 * Displays a compare dialog and allows the same behavior as the compare. In addition
 * a replace button is added to the dialog that will replace the local with the currently 
 * selected revision.
 * 
 * @since 3.0
 */
public class ReplaceWithRevisionAction extends CompareWithRevisionAction {
	
	protected void showCompareInDialog(Shell shell, Object object){
		IHistoryPageSource pageSource = HistoryPageSource.getHistoryPageSource(object);
		if (pageSource != null && pageSource.canShowHistoryFor(object)) {
			CompareConfiguration cc = new CompareConfiguration();
			cc.setLeftEditable(false);
			cc.setRightEditable(false);
			HistoryPageCompareEditorInput input = new HistoryPageCompareEditorInput(cc, pageSource, object) {
				public void saveChanges(IProgressMonitor monitor) throws CoreException {
					super.saveChanges(monitor);
					((CVSHistoryPage)getHistoryPage()).saveChanges(monitor);
					setDirty(false);
				}
				public boolean isEditionSelectionDialog() {
					return true;
				}
				public String getOKButtonLabel() {
					return CVSUIMessages.ReplaceWithRevisionAction_0;
				}
				public boolean okPressed() {
					try {
						Object o = getSelectedEdition();
						FileRevisionTypedElement right = (FileRevisionTypedElement) ((ICompareInput)o).getRight();
						IFile file = (IFile)getCompareResult();
						file.setContents(right.getContents(), false, true, null);
					} catch (CoreException e) {
						Utils.handle(e);
						return false;
					}
					return true;
				}
			};
			CompareUI.openCompareDialog(input);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CompareWithRevisionAction#getActionTitle()
	 */
	protected String getActionTitle() {
		return CVSUIMessages.ReplaceWithRevisionAction_1; 
	}
	
	protected boolean isShowInDialog() {
		// Always show a replace in a dialog
		return true;
	}
}
