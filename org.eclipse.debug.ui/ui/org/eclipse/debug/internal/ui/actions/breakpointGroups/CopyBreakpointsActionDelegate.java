/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import org.eclipse.debug.internal.ui.viewers.model.VirtualCopyToClipboardActionDelegate;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Copies breakpoint labels to the text clipboard and breakpoint objects
 * to the breakpoint paste action.
 */
public class CopyBreakpointsActionDelegate extends VirtualCopyToClipboardActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.VirtualCopyToClipboardActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		super.run(action);
		LocalSelectionTransfer.getTransfer().setSelection(getSelection());
		LocalSelectionTransfer.getTransfer().setSelectionSetTime(System.currentTimeMillis());
		IAction pasteAction = ((AbstractDebugView)getView()).getAction(ActionFactory.PASTE.getCommandId());
        // update the enablement of the paste action
        // workaround since the clipboard does not suppot callbacks
        if (pasteAction instanceof PasteBreakpointsAction) {
        	PasteBreakpointsAction pba = (PasteBreakpointsAction) pasteAction;
        	if (pba.getStructuredSelection() != null) {
        		pba.selectionChanged(pba.getStructuredSelection());
        	}
        }
        	
	}
}
