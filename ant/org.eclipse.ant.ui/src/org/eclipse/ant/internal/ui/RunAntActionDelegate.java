package org.eclipse.ant.internal.ui;

import java.lang.reflect.InvocationTargetException;
import org.apache.tools.ant.BuildException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;


public class RunAntActionDelegate implements IWorkbenchWindowActionDelegate, IRunnableWithProgress {

	private ISelection selection;

	/*
	 * @see IWorkbenchWindowActionDelegate
	 */
	public void dispose() {
	}
	/**
	  * Returns the active shell.
	  */
	protected Shell getShell() {
		return AntUIPlugin.getPlugin().getWorkbench().getActiveWorkbenchWindow().getShell();
	}
	/*
	 * @see IWorkbenchWindowActionDelegate
	 */
	public void init(IWorkbenchWindow window) {
	}
	/*
	 * @see IRunnableWithProgress
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		String buildFileName= null;
		IFile buildFile= null;
		if (selection instanceof IStructuredSelection) {
			Object first= ((IStructuredSelection) selection).getFirstElement();
			if (first instanceof IFile) {
				buildFile= (IFile) first;
				buildFileName= buildFile.getLocation().toOSString();
			}
		}

		String[] args= {"-buildfile", buildFileName};
		monitor.beginTask("Running Ant", IProgressMonitor.UNKNOWN);

		try {
			//TBD: should remove the build listener somehow
			new AntRunner().run(args, new UIBuildListener(monitor, buildFile));
		} 
		catch (BuildCanceledException e) {
			// build was canceled don't propagate exception
			return;
		}
		catch (Exception e) {
			throw new InvocationTargetException(e);
		}
		finally {
			monitor.done();
		}
	}
	/*
	 * @see IActionDelegate
	 */
	public void run(IAction action) {
		Shell shell= getShell();
		try {
			ProgressMonitorDialog dialog= new ProgressMonitorDialog(shell);
			dialog.run(true, true, this);
		} catch (InvocationTargetException e) {
			Throwable target= e.getTargetException();
			IStatus s= new Status(IStatus.ERROR, AntUIPlugin.PI_ANTUI, IStatus.ERROR, target.getMessage(), null);
			ErrorDialog.openError(getShell(), "Ant", "Exception while running Ant", s);
		} catch (InterruptedException e) {
			// do nothing on cancel
			return;
		}
	}
	/*
	 * @see IWorkbenchActionDelegate
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection= selection;
	}
}
