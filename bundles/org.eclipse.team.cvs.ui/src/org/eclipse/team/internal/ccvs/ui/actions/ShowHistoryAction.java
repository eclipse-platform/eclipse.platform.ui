package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.ccvs.core.IRemoteFile;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.HistoryView;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.actions.TeamAction;

public class ShowHistoryAction extends TeamAction {
	/**
	 * Returns the selected remote files
	 */
	protected IRemoteFile[] getSelectedRemoteFiles() {
		ArrayList resources = null;
		if (!selection.isEmpty()) {
			resources = new ArrayList();
			Iterator elements = ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof IRemoteFile) {
					resources.add(next);
					continue;
				}
				if (next instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) next;
					Object adapter = a.getAdapter(IRemoteFile.class);
					if (adapter instanceof IRemoteFile) {
						resources.add(adapter);
						continue;
					}
				}
			}
		}
		if (resources != null && !resources.isEmpty()) {
			IRemoteFile[] result = new IRemoteFile[resources.size()];
			resources.toArray(result);
			return result;
		}
		return new IRemoteFile[0];
	}
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				IRemoteFile[] files = getSelectedRemoteFiles();
				HistoryView view = HistoryView.openInActivePerspective();
				if (view != null) {
					view.showHistory(files[0]);
				}
			}
		}, Policy.bind("ShowHistoryAction.showHistory"), this.PROGRESS_DIALOG);
	}
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		IRemoteFile[] resources = getSelectedRemoteFiles();
		return resources.length == 1;
	}
	/** (Non-javadoc)
	 * Method declared on IActionDelegate.
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
			//this action can be invoked by double-click, in which case
			//there is no target action
			if (action != null) {
				try {
					action.setEnabled(isEnabled());
				} catch (TeamException e) {
					action.setEnabled(false);
				}
			}
		}
	}
}