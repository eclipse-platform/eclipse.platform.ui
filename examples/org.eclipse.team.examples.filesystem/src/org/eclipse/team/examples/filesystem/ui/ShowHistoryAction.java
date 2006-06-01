package org.eclipse.team.examples.filesystem.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.history.GenericHistoryView;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionDelegate;

public class ShowHistoryAction extends ActionDelegate implements IObjectActionDelegate {

	private IStructuredSelection fSelection;
	private IWorkbenchPart targetPart;

	public void run(IAction action) {
		final Shell shell = Display.getDefault().getActiveShell();
		try {
			new ProgressMonitorDialog(shell).run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final IResource resource = (IResource) fSelection.getFirstElement();
					Runnable r = new Runnable() {
						public void run() {
							try {
								IViewPart view = targetPart.getSite().getPage().showView("org.eclipse.team.ui.GenericHistoryView"); //$NON-NLS-1$
								if (view instanceof GenericHistoryView) {
									GenericHistoryView historyView = (GenericHistoryView) view;
									historyView.itemDropped(resource, true);
								}
							} catch (PartInitException e) {
							}
						}
					};

					TeamUIPlugin.getStandardDisplay().asyncExec(r);
				}
			});
		} catch (InvocationTargetException exception) {
			ErrorDialog.openError(shell, null, null, new Status(IStatus.ERROR, TeamUIPlugin.PLUGIN_ID, IStatus.ERROR, TeamUIMessages.ShowLocalHistory_1, exception.getTargetException()));
		} catch (InterruptedException exception) {
		}
	}

	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fSelection = (IStructuredSelection) sel;
		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}
}
