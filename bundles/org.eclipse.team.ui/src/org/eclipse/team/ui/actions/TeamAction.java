package org.eclipse.team.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.ui.TeamUIPlugin;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * The abstract superclass of all Team actions. This class contains some convenience
 * methods for getting selected objects and mapping selected objects to their
 * providers.
 * 
 * Team providers may subclass this class when creating their actions.
 * Team providers may also instantiate or subclass any of the  
 * subclasses of TeamAction provided in this package.
 */
public abstract class TeamAction extends ActionDelegate implements IObjectActionDelegate {
	// The current selection
	protected IStructuredSelection selection;
	
	// The shell, required for the progress dialog
	protected Shell shell;

	// Constants for determining the type of progress. Subclasses may
	// pass one of these values to the run method.
	public final static int PROGRESS_DIALOG = 1;
	public final static int PROGRESS_BUSYCURSOR = 2;

	/**
	 * Returns the selected projects.
	 * 
	 * @return the selected projects
	 */
	protected IProject[] getSelectedProjects() {
		ArrayList projects = null;
		if (!selection.isEmpty()) {
			projects = new ArrayList();
			Iterator elements = ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof IProject) {
					projects.add(next);
					continue;
				}
				if (next instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) next;
					Object adapter = a.getAdapter(IResource.class);
					if (adapter instanceof IProject) {
						projects.add(adapter);
						continue;
					}
				}
			}
		}
		if (projects != null && !projects.isEmpty()) {
			IProject[] result = new IProject[projects.size()];
			projects.toArray(result);
			return result;
		}
		return new IProject[0];
	}
	/**
	 * Returns the selected resources.
	 * 
	 * @return the selected resources
	 */
	protected IResource[] getSelectedResources() {
		ArrayList resources = null;
		if (!selection.isEmpty()) {
			resources = new ArrayList();
			Iterator elements = ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof IResource) {
					resources.add(next);
					continue;
				}
				if (next instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) next;
					Object adapter = a.getAdapter(IResource.class);
					if (adapter instanceof IResource) {
						resources.add(adapter);
						continue;
					}
				}
			}
		}
		if (resources != null && !resources.isEmpty()) {
			IResource[] result = new IResource[resources.size()];
			resources.toArray(result);
			return result;
		}
		return new IResource[0];
	}

	/**
	 * Convenience method for getting the current shell.
	 * 
	 * @return the shell
	 */
	protected Shell getShell() {
		if (shell != null) {
			return shell;
		} else {
			return TeamUIPlugin
				.getPlugin()
				.getWorkbench()
				.getActiveWorkbenchWindow()
				.getShell();
		}
	}
	/**
	 * Returns a status object for the given exception.
	 * 
	 * @param t  the throwable to get a status for
	 * @return a status for the given throwable
	 */
	protected IStatus getStatus(Throwable t) {
		if (t instanceof CoreException) {
			return ((CoreException) t).getStatus();
		}
		return new Status(
			IStatus.ERROR,
			TeamUIPlugin.ID,
			1,
			Policy.bind("simpleInternal"),
			t);
	}
	/**
	 * Convenience method for running an operation with progress and
	 * error feedback.
	 * 
	 * @param runnable  the runnable which executes the operation
	 * @param problemMessage  the message to display in the case of errors
	 * @param progressKind  one of PROGRESS_BUSYCURSOR or PROGRESS_DIALOG
	 */
	final protected void run(final IRunnableWithProgress runnable, final String problemMessage, int progressKind) {
		final IStatus[] errors = new IStatus[] {null};
		switch (progressKind) {
			case PROGRESS_BUSYCURSOR :
				BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
					public void run() {
						try {
							runnable.run(new NullProgressMonitor());
						} catch (InvocationTargetException e) {
							errors[0] = getStatusFromException(e, problemMessage);
						} catch (InterruptedException e) {
							errors[0] = null;
						}
					}
				});
				break;
			default :
			case PROGRESS_DIALOG :
				try {
					new ProgressMonitorDialog(getShell()).run(true, true, runnable);
				} catch (InvocationTargetException e) {
					errors[0] = getStatusFromException(e, problemMessage);
				} catch (InterruptedException e) {
					errors[0] = null;
				}
				break;
		}
		if (errors[0] != null) {
			String msg = problemMessage;
			ErrorDialog.openError(getShell(), msg, null, errors[0]);
			TeamUIPlugin.log(errors[0]);
		}
	}
	/**
	 * Convenience method for converting an exception into the appropriate status object.
	 * 
	 * @param e  the exception to get a status for
	 * @param msg  the message to include in the status
	 * @return a status for the given exception
	 */
	private IStatus getStatusFromException(InvocationTargetException e, String msg) {
		Throwable t = e.getTargetException();
		IStatus errors = null;
		if (t instanceof TeamException) {
			errors = ((TeamException) t).getStatus();
		} else {
			errors = new Status(IStatus.ERROR, TeamUIPlugin.ID, 1, msg, t);
		}
		return errors;
	}
	/*
	 * Method declared on IActionDelegate.
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
			try {
				action.setEnabled(isEnabled());
			} catch (TeamException e) {
				action.setEnabled(false);
			}
		}
	}
	/*
	 * Method declared on IObjectActionDelegate.
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.shell = targetPart.getSite().getShell();
	}
	/**
	 * Shows the given errors to the user.
	 * 
	 * @param status  the status containing the error
	 * @param title  the title of the error dialog
	 * @param message  the message for the error dialog
	 * @param shell  the shell to open the error dialog in
	 */
	protected void showError(IStatus status, String title, String message, Shell shell) {
		if (!status.isOK()) {
			IStatus toShow = status;
			if (status.isMultiStatus()) {
				IStatus[] children = status.getChildren();
				if (children.length == 1) {
					toShow = children[0];
				}
			}
			if (title == null)
				title = status.getMessage();
			ErrorDialog.openError(shell, title, message, toShow);
			TeamUIPlugin.log(toShow);
		}
	}
	/**
	 * Shows the given errors to the user.
	 * 
	 * @param status  the status containing the error to show
	 */
	protected void showError(IStatus status) {
		showError(status, null, null, getShell());
	}
	/**
	 * Concrete action enablement code.
	 * Subclasses must implement.
	 * 
	 * @return whether the action is enabled
	 * @throws TeamException if an error occurs during enablement detection
	 */
	abstract protected boolean isEnabled() throws TeamException;
	
	/**
	 * Convenience method that maps the selected resources to their providers.
	 * The returned Hashtable has keys which are ITeamProviders, and values
	 * which are Lists of IResources that are shared with that provider.
	 * 
	 * @return a hashtable mapping providers to their selected resources
	 */
	protected Hashtable getProviderMapping() {
		IResource[] resources = getSelectedResources();
		Hashtable result = new Hashtable();
		for (int i = 0; i < resources.length; i++) {
			ITeamProvider provider = TeamPlugin.getManager().getProvider(resources[i].getProject());
			List list = (List)result.get(provider);
			if (list == null) {
				list = new ArrayList();
				result.put(provider, list);
			}
			list.add(resources[i]);
		}
		return result;
	}
}