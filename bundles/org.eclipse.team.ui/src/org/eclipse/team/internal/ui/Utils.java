/*
 * Created on Jun 26, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.team.internal.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;

/**
 * @author Jean-Michel Lemieux
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Utils {

	/**
	 * Shows the given errors to the user.
	 * 
	 * @param Exception  the exception containing the error
	 * @param title  the title of the error dialog
	 * @param message  the message for the error dialog
	 * @param shell  the shell to open the error dialog in
	 */
	public static void handleError(Shell shell, Exception exception, String title, String message) {
		IStatus status = null;
		boolean log = false;
		boolean dialog = false;
		Throwable t = exception;
		if (exception instanceof TeamException) {
			status = ((TeamException)exception).getStatus();
			log = false;
			dialog = true;
		} else if (exception instanceof InvocationTargetException) {
			t = ((InvocationTargetException)exception).getTargetException();
			if (t instanceof TeamException) {
				status = ((TeamException)t).getStatus();
				log = false;
				dialog = true;
			} else if (t instanceof CoreException) {
				status = ((CoreException)t).getStatus();
				log = true;
				dialog = true;
			} else if (t instanceof InterruptedException) {
				return;
			} else {
				status = new Status(IStatus.ERROR, TeamUIPlugin.ID, 1, Policy.bind("TeamAction.internal"), t); //$NON-NLS-1$
				log = true;
				dialog = true;
			}
		}
		if (status == null) return;
		if (!status.isOK()) {
			IStatus toShow = status;
			if (status.isMultiStatus()) {
				IStatus[] children = status.getChildren();
				if (children.length == 1) {
					toShow = children[0];
				}
			}
			if (title == null) {
				title = status.getMessage();
			}
			if (message == null) {
				message = status.getMessage();
			}
			if (dialog && shell != null) {
				ErrorDialog.openError(shell, title, message, toShow);
			}
			if (log || shell == null) {
				TeamUIPlugin.log(toShow.getSeverity(), message, t);
			}
		}
	}
	public static void runWithProgress(Shell parent, boolean cancelable,
		final IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		boolean createdShell = false;
		try {
			if (parent == null || parent.isDisposed()) {
				Display display = Display.getCurrent();
				if (display == null) {
					// cannot provide progress (not in UI thread)
					runnable.run(new NullProgressMonitor());
					return;
				}
				// get the active shell or a suitable top-level shell
				parent = display.getActiveShell();
				if (parent == null) {
					parent = new Shell(display);
					createdShell = true;
				}
			}
			// pop up progress dialog after a short delay
			final Exception[] holder = new Exception[1];
			BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
				public void run() {
					try {
						runnable.run(new NullProgressMonitor());
					} catch (InvocationTargetException e) {
						holder[0] = e;
					} catch (InterruptedException e) {
						holder[0] = e;
					}
				}
			});
			if (holder[0] != null) {
				if (holder[0] instanceof InvocationTargetException) {
					throw (InvocationTargetException) holder[0];
				} else {
					throw (InterruptedException) holder[0];
				}
			}
			//new TimeoutProgressMonitorDialog(parent, TIMEOUT).run(true /*fork*/, cancelable, runnable);
		} finally {
			if (createdShell) parent.dispose();
		}
	}
	/**
	 * Creates a progress monitor and runs the specified runnable.
	 * 
	 * @param parent the parent Shell for the dialog
	 * @param cancelable if true, the dialog will support cancelation
	 * @param runnable the runnable
	 * 
	 * @exception InvocationTargetException when an exception is thrown from the runnable
	 * @exception InterruptedException when the progress monitor is cancelled
	 */
	public static void runWithProgressDialog(Shell parent, boolean cancelable,
		final IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
			
		new ProgressMonitorDialog(parent).run(cancelable, cancelable, runnable);
	}
	/*
	 * This method is only for use by the Target Management feature (see bug
	 * 16509).
	 * 
	 * @param t
	 */
	public static void handle(Throwable t) {
		IStatus error = null;
		if (t instanceof InvocationTargetException) {
			t = ((InvocationTargetException)t).getTargetException();
		}
		if (t instanceof CoreException) {
			error = ((CoreException)t).getStatus();
		} else if (t instanceof TeamException) {
			error = ((TeamException)t).getStatus();
		} else {
			error = new Status(IStatus.ERROR, TeamUIPlugin.ID, 1, Policy.bind("simpleInternal"), t); //$NON-NLS-1$
		}
	
		Shell shell = new Shell(Display.getDefault());
	
		if (error.getSeverity() == IStatus.INFO) {
			MessageDialog.openInformation(shell, Policy.bind("information"), error.getMessage()); //$NON-NLS-1$
		} else {
			ErrorDialog.openError(shell, Policy.bind("exception"), null, error); //$NON-NLS-1$
		}
		shell.dispose();
		// Let's log non-team exceptions
		if (!(t instanceof TeamException)) {
			TeamUIPlugin.log(error.getSeverity(), error.getMessage(), t);
		}
	}
}
