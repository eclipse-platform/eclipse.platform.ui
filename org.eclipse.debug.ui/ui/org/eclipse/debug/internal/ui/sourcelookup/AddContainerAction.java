/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;


/**
 * The action to add a new source container.
 * Used by the CommonSourceNotFoundEditor, the launch configuration source tab,
 * and the EditSourceLookupPathDialog.
 */
public class AddContainerAction extends SourceContainerAction {
	
	public AddContainerAction() {
		super(SourceLookupUIMessages.getString("sourceTab.addButton")); //$NON-NLS-1$		
	}	
	
	/**
	 * Prompts for a project to add.
	 * 
	 * @see IAction#run()
	 */	
	public void run() {
		Dialog dialog = new AddSourceContainerDialog(getShell(), getViewer());
		dialog.open();			
	}
	
	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		if(selection == null || selection.isEmpty())
			return true;
		else
			return getViewer().getTree().getSelection()[0].getParentItem()==null;
	}
	
	
}
