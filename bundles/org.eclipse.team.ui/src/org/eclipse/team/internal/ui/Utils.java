/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.ui.TeamImages;

public class Utils {

	/**
	 * The SortOperation takes a collection of objects and returns
	 * a sorted collection of these objects.  Concrete instances of this
	 * class provide the criteria for the sorting of the objects based on
	 * the type of the objects.
	 */
	static public abstract class Sorter {
		/**
		 * Returns true is elementTwo is 'greater than' elementOne
		 * This is the 'ordering' method of the sort operation.
		 * Each subclass overides this method with the particular
		 * implementation of the 'greater than' concept for the 
		 * objects being sorted.
		 */
		public abstract boolean compare(Object elementOne, Object elementTwo);
		/**
		 * Sort the objects in sorted collection and return that collection.
		 */
		private Object[] quickSort(Object[] sortedCollection, int left, int right) {
			int originalLeft = left;
			int originalRight = right;
			Object mid = sortedCollection[ (left + right) / 2];
			do {
				while (compare(sortedCollection[left], mid))
					left++;
				while (compare(mid, sortedCollection[right]))
					right--;
				if (left <= right) {
					Object tmp = sortedCollection[left];
					sortedCollection[left] = sortedCollection[right];
					sortedCollection[right] = tmp;
					left++;
					right--;
				}
			} while (left <= right);
			if (originalLeft < right)
				sortedCollection = quickSort(sortedCollection, originalLeft, right);
			if (left < originalRight)
				sortedCollection = quickSort(sortedCollection, left, originalRight);
			return sortedCollection;
		}
		/**
		 * Return a new sorted collection from this unsorted collection.
		 * Sort using quick sort.
		 */
		public Object[] sort(Object[] unSortedCollection) {
			int size = unSortedCollection.length;
			Object[] sortedCollection = new Object[size];
			//copy the array so can return a new sorted collection	
			System.arraycopy(unSortedCollection, 0, sortedCollection, 0, size);
			if (size > 1)
				quickSort(sortedCollection, 0, size - 1);
			return sortedCollection;
		}
	}
	
	
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
	
	public static void initAction(IAction a, String prefix) {
		Utils.initAction(a, prefix, Policy.bundle);
	}
	/**
	 * Initialize the given Action from a ResourceBundle.
	 */
	public static void initAction(IAction a, String prefix, ResourceBundle bundle) {
		
		String labelKey= "label"; //$NON-NLS-1$
		String tooltipKey= "tooltip"; //$NON-NLS-1$
		String imageKey= "image"; //$NON-NLS-1$
		String descriptionKey= "description"; //$NON-NLS-1$
		
		if (prefix != null && prefix.length() > 0) {
			labelKey= prefix + labelKey;
			tooltipKey= prefix + tooltipKey;
			imageKey= prefix + imageKey;
			descriptionKey= prefix + descriptionKey;
		}
	
		String s = Policy.bind(labelKey, bundle);
		if(s != null)
			a.setText(s);
		s = Policy.bind(tooltipKey, bundle);
		if(s != null)
			a.setToolTipText(s);
		s = Policy.bind(descriptionKey, bundle);
		if(s != null)
			a.setDescription(s);
	
		String relPath= Policy.bind(imageKey, bundle);
		if (relPath != null && ! relPath.equals(imageKey) && relPath.trim().length() > 0) {
		
			String cPath;
			String dPath;
			String ePath;
		
			if (relPath.indexOf("/") >= 0) { //$NON-NLS-1$
				String path= relPath.substring(1);
				cPath= 'c' + path;
				dPath= 'd' + path;
				ePath= 'e' + path;
			} else {
				cPath= "clcl16/" + relPath; //$NON-NLS-1$
				dPath= "dlcl16/" + relPath; //$NON-NLS-1$
				ePath= "elcl16/" + relPath; //$NON-NLS-1$
			}
		
			ImageDescriptor id= TeamImages.getImageDescriptor(dPath);	// we set the disabled image first (see PR 1GDDE87)
			if (id != null)
				a.setDisabledImageDescriptor(id);
			id= TeamUIPlugin.getImageDescriptor(cPath);
			if (id != null)
				a.setHoverImageDescriptor(id);
			id= TeamUIPlugin.getImageDescriptor(ePath);
			if (id != null)
				a.setImageDescriptor(id);
		}
	}
}
