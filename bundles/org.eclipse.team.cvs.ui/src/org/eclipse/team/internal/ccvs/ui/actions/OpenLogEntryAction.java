package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.RemoteFileEditorInput;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

public class OpenLogEntryAction extends TeamAction {
	/**
	 * Returns the selected remote files
	 */
	protected ILogEntry[] getSelectedLogEntries() {
		ArrayList entries = null;
		if (!selection.isEmpty()) {
			entries = new ArrayList();
			Iterator elements = ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof ILogEntry) {
					entries.add((ILogEntry)next);
					continue;
				}
				if (next instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) next;
					Object adapter = a.getAdapter(ILogEntry.class);
					if (adapter instanceof ILogEntry) {
						entries.add(adapter);
						continue;
					}
				}
			}
		}
		if (entries != null && !entries.isEmpty()) {
			ILogEntry[] result = new ILogEntry[entries.size()];
			entries.toArray(result);
			return result;
		}
		return new ILogEntry[0];
	}
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				IWorkbenchPage page = CVSUIPlugin.getPlugin().getWorkbench().getActiveWorkbenchWindow().getActivePage();
				final ILogEntry[] entries = getSelectedLogEntries();
				for (int i = 0; i < entries.length; i++) {
					try {
						if (entries[i].isDeletion()) {
							MessageDialog.openError(getShell(), Policy.bind("OpenLogEntryAction.deletedTitle"), Policy.bind("OpenLogEntryAction.deleted")); //$NON-NLS-1$ //$NON-NLS-2$
						} else {
							page.openEditor(new RemoteFileEditorInput(entries[i].getRemoteFile()), "org.eclipse.ui.DefaultTextEditor"); //$NON-NLS-1$
						}
					} catch (PartInitException e) {
						throw new InvocationTargetException(e);
					}
				}
			}
		}, Policy.bind("OpenLogEntryAction.open"), PROGRESS_BUSYCURSOR); //$NON-NLS-1$
	}
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ILogEntry[] entries = getSelectedLogEntries();
		if (entries.length == 0) return false;
		return true;
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