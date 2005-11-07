/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts.actions;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.internal.ui.viewers.TreePath;
import org.eclipse.debug.internal.ui.viewers.TreeSelection;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.progress.WorkbenchJob;

public class RemoveExpressionAction extends AbstractRemoveActionDelegate {

	protected void doAction(Object element) {
		WorkbenchJob job = new WorkbenchJob("remove expression") { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IExpressionManager manager = DebugPlugin.getDefault().getExpressionManager();
				IExpression exp = getExpression();
				if (exp != null) {
					manager.removeExpression(exp);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}
	
	/**
	 * Returns the expression associated with the given
	 * element.
	 * 
	 * @param element an expression of child of an expression in
	 *  the expression view.
	 * @return associated expression
	 */
	protected IExpression getExpression() {
		if (getView() == null) {
			return null;
		}
		IDebugView adapter= (IDebugView)getView().getAdapter(IDebugView.class);
		if (adapter != null) {
			Viewer v= adapter.getViewer();
			if (v instanceof AsynchronousTreeViewer) {
				AsynchronousTreeViewer viewer = (AsynchronousTreeViewer) v;
				TreeSelection selection = (TreeSelection) viewer.getSelection();
				TreePath[] paths = selection.getPaths();
				for (int i = paths.length-1; i >=0; i--) {
					TreePath path = paths[i];
					Object segment = path.getLastSegment();
					if (segment instanceof IExpression) {
						return (IExpression) segment;
					}
				}
			}	
		}
		return null;
	}
}

