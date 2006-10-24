/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.internal.ui.contexts.DebugContextManager;
import org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextListener;
import org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextService;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Global retargettable run to line action.
 * 
 * @since 3.0
 */
public class RetargetRunToLineAction extends RetargetAction {
	
	private IDebugContextListener fContextListener = new DebugContextListener();
	private ISuspendResume fTargetElement = null;
	
	class DebugContextListener implements IDebugContextListener {

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextListener#contextActivated(org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart)
		 */
		public void contextActivated(ISelection selection, IWorkbenchPart part) {
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

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextListener#contextChanged(org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart)
		 */
		public void contextChanged(ISelection selection, IWorkbenchPart part) {
			contextActivated(selection, part);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		DebugContextManager.getDefault().getContextService(fWindow).removeDebugContextListener(fContextListener);
		super.dispose();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		super.init(window);
		IDebugContextService service = DebugContextManager.getDefault().getContextService(window);
		service.addDebugContextListener(fContextListener);
		ISelection activeContext = service.getActiveContext();
		fContextListener.contextActivated(activeContext, null);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RetargetAction#canPerformAction(java.lang.Object, org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart)
	 */
	protected boolean canPerformAction(Object target, ISelection selection,	IWorkbenchPart part) {
		return fTargetElement != null &&
			((IRunToLineTarget)target).canRunToLine(part, selection, fTargetElement);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RetargetAction#getAdapterClass()
	 */
	protected Class getAdapterClass() {
		return IRunToLineTarget.class;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RetargetAction#performAction(java.lang.Object, org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart)
	 */
	protected void performAction(Object target, ISelection selection, IWorkbenchPart part) throws CoreException {
		((IRunToLineTarget)target).runToLine(part, selection, fTargetElement);
	}
}
