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


import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;

/**
 * A follow hyperlink action that is always enabled but reports problems
 * when a run fails. Bound to the open editor action definition by the
 * ConsoleView.
 */
public class KeyBindingFollowHyperlinkAction extends FollowHyperlinkAction {

	private boolean fSelectionNotAHyperlink = false;
	private IActionBars fActionBars;
	
	public KeyBindingFollowHyperlinkAction(ISelectionProvider selectionProvider, IActionBars actionBars) {
		super(selectionProvider);
		fActionBars = actionBars;
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		IConsoleHyperlink link = getHyperLink();
		if (link == null) {		
			IStatusLineManager statusLine= getStatusLineManager();
			if (statusLine != null) {
				statusLine.setErrorMessage(ActionMessages.getString("KeyBindingFollowHyperLinkAction.No_hyperlink")); //$NON-NLS-1$
				fSelectionNotAHyperlink = true;
			}
			DebugUIPlugin.getStandardDisplay().beep();
		} else {
			link.linkActivated();
			fSelectionNotAHyperlink = false;
		}
	}
	
	public void clearStatusLine() {
		if (fSelectionNotAHyperlink) {
			IStatusLineManager statusLine= getStatusLineManager();
			if (statusLine != null) {		
				statusLine.setErrorMessage(null);
				fSelectionNotAHyperlink = false;
			}
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(ISelection selection) {
		if (isEmptySelection(selection)) {
			clearStatusLine();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection selection) {
		selectionChanged((ISelection)selection);
	}
	
	/**
	 * This method is required because ITextSelection's of length zero are
	 * NOT considered empty according to the implementation of TextSelection.isEmpty()
	 * (see bug 32063).
	 */
	protected boolean isEmptySelection(ISelection selection) {
		if (selection instanceof ITextSelection) {
			return ((ITextSelection)selection).getLength() < 1;
		} 
		return selection.isEmpty();
	}
	
	/**
	 * Convenience method
	 */
	protected IStatusLineManager getStatusLineManager() {
		return fActionBars.getStatusLineManager();
	}

}
