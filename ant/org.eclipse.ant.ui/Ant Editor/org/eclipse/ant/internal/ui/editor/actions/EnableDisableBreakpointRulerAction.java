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

import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.ITextEditor;

public class EnableDisableBreakpointRulerAction extends AbstractBreakpointRulerAction {
	
	/**
	 * Creates the action to enable/disable breakpoints
	 */
	public EnableDisableBreakpointRulerAction(ITextEditor editor, IVerticalRulerInfo info) {
		setInfo(info);
		setTextEditor(editor);
		setText(AntEditorActionMessages.getString("EnableDisableBreakpointRulerAction.0")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		if (getBreakpoint() != null) {
			try {
				getBreakpoint().setEnabled(!getBreakpoint().isEnabled());
			} catch (CoreException e) {
				ErrorDialog.openError(getTextEditor().getEditorSite().getShell(), AntEditorActionMessages.getString("EnableDisableBreakpointRulerAction.1"), AntEditorActionMessages.getString("EnableDisableBreakpointRulerAction.2"), e.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setBreakpoint(determineBreakpoint());
		if (getBreakpoint() == null) {
			setEnabled(false);
			return;
		}
		setEnabled(true);
		try {
			boolean enabled= getBreakpoint().isEnabled();
			setText(enabled ? AntEditorActionMessages.getString("EnableDisableBreakpointRulerAction.3"): AntEditorActionMessages.getString("EnableDisableBreakpointRulerAction.4")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (CoreException ce) {
			AntUIPlugin.log(ce);
		}
	}
}
