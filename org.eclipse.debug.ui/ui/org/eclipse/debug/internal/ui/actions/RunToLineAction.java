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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Global retargettable run to line action.
 * 
 * @since 3.0
 */
public class RunToLineAction implements IWorkbenchWindowActionDelegate, IPartListener, IUpdate {
	
	private IWorkbenchWindow window = null;
	private IWorkbenchPart activePart = null;
	private IRunToLineTarget partTarget = null;
	private IAction action = null;
	private ISelectionListener selectionListener = new DebugSelectionListener();
	private ISuspendResume targetElement = null;
	
	class DebugSelectionListener implements ISelectionListener {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
		 */
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			targetElement = null;
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (ss.size() == 1) {
					Object object = ss.getFirstElement();
					if (object instanceof ISuspendResume) {
						targetElement = (ISuspendResume) object;
					}
				}
			}
			update();
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		window.getSelectionService().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, selectionListener);
		window.getPartService().removePartListener(this);
		activePart = null;
		partTarget = null;
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
		window.getSelectionService().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, selectionListener);
		IPartService partService = window.getPartService();
		partService.addPartListener(this);
		IWorkbenchPart part = partService.getActivePart();
		if (part != null) {
			partActivated(part);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (partTarget != null && targetElement != null) {
			try {
				partTarget.runToLine(targetElement);
			} catch (CoreException e) {
				DebugUIPlugin.errorDialog(window.getShell(), ActionMessages.getString("RunToLineAction.0"), ActionMessages.getString("RunToLineAction.1"), e.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.action = action;
		update();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		activePart = part;
		partTarget  = (IRunToLineTarget) part.getAdapter(IRunToLineTarget.class);
		if (partTarget == null) {
			IAdapterManager adapterManager = Platform.getAdapterManager();
			// TODO: we could restrict loading to cases when the debugging context is on
			if (adapterManager.hasAdapter(part, "org.eclipse.debug.internal.ui.actions.IRunToLineTarget")) { //$NON-NLS-1$
				partTarget = (IRunToLineTarget) adapterManager.loadAdapter(part, "org.eclipse.debug.internal.ui.actions.IRunToLineTarget"); //$NON-NLS-1$
			}
		}
		update();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		partDeactivated(part);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
		if (part.equals(activePart)) {
			activePart = null;
			if (partTarget != null) {
				partTarget.dispose();
				partTarget = null;
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		if (action == null) {
			return;
		}
		if (partTarget != null && targetElement != null) {
			if (targetElement.isSuspended()) {
				action.setEnabled(true);
			}
		} else {
			action.setEnabled(false);
		}
	}
}
