/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.actions;

import org.eclipse.ant.internal.ui.editor.AntEditor;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jdt.ui.actions.OpenAction;
import org.eclipse.jface.text.ITextSelection;

public class OpenDeclarationAction extends OpenAction {
	
	private AntEditor fEditor;
	
	public OpenDeclarationAction(AntEditor antEditor) {
		super(antEditor.getSite());
		fEditor= antEditor;
		setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_EDITOR);
		antEditor.getSite().getKeyBindingService().registerAction(this);

		setText(AntEditorActionMessages.getString("OpenDeclarationAction.0"));  //$NON-NLS-1$
		setDescription(AntEditorActionMessages.getString("OpenDeclarationAction.1")); //$NON-NLS-1$
		setToolTipText(AntEditorActionMessages.getString("OpenDeclarationAction.1")); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		if (fEditor == null) {
			return;
		}
		
		fEditor.openReferenceElement();		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.actions.SelectionDispatchAction#selectionChanged(org.eclipse.jface.text.ITextSelection)
	 */
	public void selectionChanged(ITextSelection selection) {
		setEnabled(fEditor != null);
	}
	
	public void setEditor(AntEditor editor) {
		fEditor= editor;
	}
}