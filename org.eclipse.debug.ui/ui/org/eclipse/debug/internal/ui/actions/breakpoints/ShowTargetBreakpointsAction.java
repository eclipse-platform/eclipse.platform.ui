/*****************************************************************
 * Copyright (c) 2009 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Initial API and implementation (Bug 238956)
 *****************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpoints;

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * This class implements the show target breakpoint action.
 * 
 * @since 3.6
 */
public class ShowTargetBreakpointsAction extends Action implements ISelectionListener {
	/**
	 * Breakpoints view
	 */
	BreakpointsView fView;
	
	/**
	 * Constructor.
	 * 
	 * @param view the breakpoints view
	 */
	public ShowTargetBreakpointsAction(BreakpointsView view) {
		super();
		
		fView = view;
		
		setText(ActionMessages.ShowSupportedBreakpointsAction_Show_For_Selected); 
		setToolTipText(ActionMessages.ShowSupportedBreakpointsAction_tooltip); 
		
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET));
		setChecked(false);
		setId(DebugUIPlugin.getUniqueIdentifier() + ".ShowSupportedBreakpointsAction"); //$NON-NLS-1$
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.SHOW_BREAKPOINTS_FOR_MODEL_ACTION);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		if (fView.getViewer().getControl().isDisposed()) {
			return;
		}
		
		if (isChecked()) {
			fView.getSite().getPage().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
			ISelection selection = fView.getSite().getPage().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
			selectionChanged(null, selection);
		} else {
			fView.getSite().getPage().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
			fView.setFilterSelection(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			fView.setFilterSelection((IStructuredSelection) selection);
		}
	}
}
