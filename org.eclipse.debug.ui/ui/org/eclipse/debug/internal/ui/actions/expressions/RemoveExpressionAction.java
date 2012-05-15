/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - integration with non-standard debug models (Bug 209883)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.expressions;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.actions.AbstractSelectionActionDelegate;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.progress.WorkbenchJob;

public class RemoveExpressionAction extends AbstractSelectionActionDelegate {
	
	protected IExpression[] getExpressions() {
		TreeSelection selection = (TreeSelection) getSelection();
		TreePath[] paths = selection.getPaths();
		List expressions = new ArrayList();
		for (int i = paths.length-1; i >=0; i--) {
			TreePath path = paths[i];
			Object segment = path.getFirstSegment();
			if (segment instanceof IExpression) {
				expressions.add(segment);
			} else if (segment instanceof IAdaptable) {
			    IExpression expression = (IExpression)((IAdaptable)segment).getAdapter(IExpression.class);
			    if (expression != null) {
			        expressions.add(expression);
			    }
			}
		}
		return (IExpression[]) expressions.toArray(new IExpression[expressions.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		WorkbenchJob job = new WorkbenchJob("remove expression") { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IExpressionManager expManager = DebugPlugin.getDefault().getExpressionManager();
				IExpression[] exp = getExpressions();
				if (exp != null) {
					expManager.removeExpressions(exp);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		schedule(job);
	}
}

