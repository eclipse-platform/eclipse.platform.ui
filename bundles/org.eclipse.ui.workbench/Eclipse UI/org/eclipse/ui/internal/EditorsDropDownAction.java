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

import org.eclipse.jface.action.Action;

import org.eclipse.ui.IWorkbenchWindow;

/**
 * The <code>EditorsDropDownAction</code> is used to show the
 * editors drop-down.
 */
public class EditorsDropDownAction extends Action {
	private IWorkbenchWindow window;
	
	/**
	 * Create a new instance of <code>EditorsDropDownAction</code>
	 * 
	 * @param window the workbench window this action applies to
	 */
	public EditorsDropDownAction(IWorkbenchWindow window) {
		super("Editors...");  // @issue need to externalize
		this.window = window;
		setToolTipText("Editors...");  // @issue need to externalize
		setActionDefinitionId("org.eclipse.ui.window.editorsDropDown"); //$NON-NLS-1$
//		WorkbenchHelp.setHelp(this, ...);
	}
	
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		WorkbenchPage page = (WorkbenchPage) window.getActivePage();
		if (page != null) {
			EditorArea editorArea = (EditorArea) page.getEditorPresentation().getLayoutPart();
			EditorWorkbook workbook = editorArea.getActiveWorkbook();
			if (workbook instanceof DropDownEditorWorkbook2) {
				((DropDownEditorWorkbook2) workbook).dropDown();
			}
		}
	}
}
