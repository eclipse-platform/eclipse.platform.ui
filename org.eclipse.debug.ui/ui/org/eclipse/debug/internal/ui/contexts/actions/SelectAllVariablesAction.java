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
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.progress.WorkbenchJob;

public class SelectAllVariablesAction extends SelectAllAction {

	protected void update() {
		WorkbenchJob job = new WorkbenchJob("update select all variables action") { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (!(getView() instanceof IDebugView)) {
					return Status.OK_STATUS;
				}
				Viewer viewer = ((IDebugView) getView()).getViewer();
				if (viewer != null) {
					Tree tree = (Tree) viewer.getControl();
					getAction().setEnabled(tree.getItemCount() != 0);
				}
				return Status.OK_STATUS;
			}
		};

		job.setSystem(true);
		job.schedule();
	}

	protected String getActionId() {
		return IDebugView.SELECT_ALL_ACTION + ".Variables"; //$NON-NLS-1$
	}
}
