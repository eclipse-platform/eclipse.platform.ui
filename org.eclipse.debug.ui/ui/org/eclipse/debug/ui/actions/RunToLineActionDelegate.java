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
package org.eclipse.debug.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * A run to line action that can be contributed to a an editor. The action
 * will perform the "run to line" operation for editors that provide
 * an appropriate <code>IRunToLineTarget</code> adapter.
 * <p>
 * Clients may reference/contribute this class as an editor action delegate
 * in plug-in XML. This class is not intended to be subclassed.
 * </p>
 * @since 3.0
 */
public class RunToLineActionDelegate implements IEditorActionDelegate, IActionDelegate2 {
	
	private IEditorPart fActivePart = null;
	private IRunToLineTarget fPartTarget = null;
	private IAction fAction = null;
	private ISelectionListener fSelectionListener = new DebugSelectionListener();
	private ISuspendResume fTargetElement = null;
	
	class DebugSelectionListener implements ISelectionListener {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
		 */
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			fTargetElement = null;
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (ss.size() == 1) {
					Object object = ss.getFirstElement();
					if (object instanceof ISuspendResume) {
						fTargetElement = (ISuspendResume) object;
					}
				}
			}
			update();
		}
		
	}
	
	/*(non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
		fActivePart.getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, fSelectionListener);
		fActivePart = null;
		fPartTarget = null;
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (fPartTarget != null && fTargetElement != null) {
			try {
				fPartTarget.runToLine(fActivePart, fActivePart.getSite().getSelectionProvider().getSelection(), fTargetElement);
			} catch (CoreException e) {
				DebugUIPlugin.errorDialog(fActivePart.getSite().getWorkbenchWindow().getShell(), ActionMessages.getString("RunToLineAction.0"), ActionMessages.getString("RunToLineAction.1"), e.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.fAction = action;
		update();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		if (fAction == null) {
			return;
		}
		boolean enabled = false;
		if (fPartTarget != null && fTargetElement != null) {
			IWorkbenchPartSite site = fActivePart.getSite();
			if (site != null) {
			    ISelectionProvider selectionProvider = site.getSelectionProvider();
			    if (selectionProvider != null) {
			        ISelection selection = selectionProvider.getSelection();
			        enabled = fTargetElement.isSuspended() && fPartTarget.canRunToLine(fActivePart, selection, fTargetElement);
			    }
			}
		}
		fAction.setEnabled(enabled);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
		this.fAction = action; 
		if (action != null) {
			action.setText(ActionMessages.getString("RunToLineActionDelegate.4")); //$NON-NLS-1$
			action.setImageDescriptor(DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_RUN_TO_LINE));
			action.setDisabledImageDescriptor(DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_RUN_TO_LINE));
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		init(action);
		if (fActivePart != null && !fActivePart.equals(targetEditor)) {
			fActivePart.getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, fSelectionListener);
		}
		fPartTarget = null;
		fActivePart = targetEditor;
		if (targetEditor != null) {
			targetEditor.getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, fSelectionListener);
			fPartTarget  = (IRunToLineTarget) targetEditor.getAdapter(IRunToLineTarget.class);
			if (fPartTarget == null) {
				IAdapterManager adapterManager = Platform.getAdapterManager();
				// TODO: we could restrict loading to cases when the debugging context is on
				if (adapterManager.hasAdapter(targetEditor, "org.eclipse.debug.internal.ui.actions.IRunToLineTarget")) { //$NON-NLS-1$
					fPartTarget = (IRunToLineTarget) adapterManager.loadAdapter(targetEditor, "org.eclipse.debug.internal.ui.actions.IRunToLineTarget"); //$NON-NLS-1$
				}
			}
		}
		update();		
	}
}
