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
package org.eclipse.debug.internal.ui.actions;

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValueModification;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.SelectionProviderAction;


public class AssignValueAction extends SelectionProviderAction {
	private VariablesView variablesView;
	private ISourceViewer detailsViewer;

	public AssignValueAction(VariablesView varView, ISourceViewer detailViewer) {
		super(varView.getViewer(), ActionMessages.getString("AssignValueAction.1")); //$NON-NLS-1$
		variablesView = varView;
		detailsViewer = detailViewer;
		setEnabled(false);
		variablesView.getSite().getKeyBindingService().registerAction(this);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection selection) {
		boolean enabled = false;
		if ((selection.size() == 1) && (selection.getFirstElement() instanceof IValueModification)) {
			IValueModification valMod = (IValueModification) selection.getFirstElement();
			if (valMod.supportsValueModification()) {
				super.selectionChanged(selection);
				enabled = true;
			}
		} 
		setEnabled(enabled);		
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		IVariable variable = (IVariable) getStructuredSelection().getFirstElement();
		
		Point selection = detailsViewer.getSelectedRange();
		String value = null;
		if (selection.y == 0) {
			value = detailsViewer.getDocument().get();
		} else {
			try {
				value = detailsViewer.getDocument().get(selection.x, selection.y);
			} catch (BadLocationException e1) {
			}
		}
		
		try {
			if (variable.verifyValue(value)) {
				variable.setValue(value);
			} else {
				IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
				if (window == null) {
					return;
				}
				Shell activeShell= window.getShell();
				
				DebugUIPlugin.errorDialog(activeShell, ActionMessages.getString("AssignValueAction.2"), MessageFormat.format(ActionMessages.getString("AssignValueAction.3"), new String[] {value, variable.getName()}), new StatusInfo(StatusInfo.ERROR, ActionMessages.getString("AssignValueAction.4")));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		} catch (DebugException e) {
			DebugUIPlugin.log(e);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#getActionDefinitionId()
	 */
	public String getActionDefinitionId() {		
		return "org.eclipse.ui.file.save"; //$NON-NLS-1$
	}

}
