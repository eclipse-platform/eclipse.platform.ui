/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeNavigationDialog;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeNavigationModel;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

/**
 * Action which prompts the user to find/navigate to an element in an async tree.
 */
public class FindElementAction extends Action implements IUpdate {
	
	private AsynchronousTreeViewer fViewer;
	private IViewPart fView;

	public FindElementAction(IViewPart view, AsynchronousTreeViewer viewer) {
		setText(ActionMessages.FindAction_0);
		setId(DebugUIPlugin.getUniqueIdentifier() + ".FindElementAction"); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.FIND_ELEMENT_ACTION);
		setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_REPLACE);
		fViewer = viewer;
		fView = view;
	}

	public void run() {
		fViewer.forceLabelPopulation();
		Job labelUpdate = new Job("Generate Labels") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				int i = 0;
				while (fViewer.hasPendingUpdates() && i < 30) {
					try {
						Thread.sleep(100);
						i++;
					} catch (InterruptedException e) {
					}
				}
				Job findJob = new UIJob("Find") { //$NON-NLS-1$
					public IStatus runInUIThread(IProgressMonitor m) {
						performFind();
						return Status.OK_STATUS;
					}
				
				};
				findJob.setSystem(true);
				findJob.setPriority(Job.INTERACTIVE);
				findJob.schedule();
				return Status.OK_STATUS;
			}
		};
		labelUpdate.setSystem(true);
		IWorkbenchSiteProgressService ps = (IWorkbenchSiteProgressService) fView.getSite().getAdapter(IWorkbenchSiteProgressService.class);
		if (ps == null) {
			labelUpdate.schedule();
		} else {
			ps.schedule(labelUpdate);
		}		
	}

	protected void performFind() {
		AsynchronousTreeNavigationModel model = new AsynchronousTreeNavigationModel(fViewer);
		AsynchronousTreeNavigationDialog dialog = new AsynchronousTreeNavigationDialog(model); 
		dialog.setTitle(ActionMessages.FindDialog_3);
		dialog.setMessage(ActionMessages.FindDialog_1);
		dialog.open();
		model.dispose();
	}
	
	public void update() {
		setEnabled(fViewer.getInput() != null);
	}
	
}
