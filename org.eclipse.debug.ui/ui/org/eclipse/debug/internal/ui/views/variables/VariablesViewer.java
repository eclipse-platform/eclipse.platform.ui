/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.ui.commands.IStatusMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.UIJob;

/**
 * @since 3.2
 *
 */
public class VariablesViewer extends AsynchronousTreeViewer{

	private VariablesView fView;

	private UIJob fRestoreJob = new UIJob("restore viewer state") { //$NON-NLS-1$
		public IStatus runInUIThread(IProgressMonitor monitor) {
			fView.restoreState();
			return Status.OK_STATUS;
		}
	};
	
	public VariablesViewer(Composite parent, int style, VariablesView view) {
		super(parent, style);
		fView = view;
		fRestoreJob.setSystem(true);
	}

	protected void updateComplete(IStatusMonitor update) {
		if (fView != null && !hasPendingUpdates()) {
			fRestoreJob.schedule();
			fView.populateDetailPane();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.treeviewer.AsynchronousTreeViewer#handlePresentationFailure(org.eclipse.debug.internal.ui.treeviewer.IPresentationRequestMonitor, org.eclipse.core.runtime.IStatus)
	 */
	protected void handlePresentationFailure(IStatusMonitor update, IStatus status) {
		fView.showMessage(status.getMessage());
	}
	
}
