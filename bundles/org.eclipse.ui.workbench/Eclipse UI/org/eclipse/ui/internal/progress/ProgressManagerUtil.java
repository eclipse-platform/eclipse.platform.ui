/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.RectangleAnimation;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * The ProgressUtil is a class that contains static utility methods used for the
 * progress API.
 */

public class ProgressManagerUtil {
	/**
	 * A constant used by the progress support to determine if an operation is
	 * too short to show progress.
	 */
	public static long SHORT_OPERATION_TIME = 250;

	static final QualifiedName KEEP_PROPERTY = IProgressConstants.KEEP_PROPERTY;

	static final QualifiedName KEEPONE_PROPERTY = IProgressConstants.KEEPONE_PROPERTY;

	static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	static final QualifiedName INFRASTRUCTURE_PROPERTY = new QualifiedName(
			WorkbenchPlugin.PI_WORKBENCH, "INFRASTRUCTURE_PROPERTY");//$NON-NLS-1$

	private static String ellipsis = ProgressMessages.ProgressFloatingWindow_EllipsisValue;

	/**
	 * Return a status for the exception.
	 * 
	 * @param exception
	 * @return IStatus
	 */
	static IStatus exceptionStatus(Throwable exception) {
		return StatusUtil.newStatus(IStatus.ERROR,
				exception.getMessage() == null ? "" : exception.getMessage(), //$NON-NLS-1$
				exception);
	}

	/**
	 * Log the exception for debugging.
	 * 
	 * @param exception
	 */
	static void logException(Throwable exception) {
		BundleUtility.log(PlatformUI.PLUGIN_ID, exception);
	}

	// /**
	// * Sets the label provider for the viewer.
	// *
	// * @param viewer
	// */
	// static void initLabelProvider(ProgressTreeViewer viewer) {
	// viewer.setLabelProvider(new ProgressLabelProvider());
	// }
	/**
	 * Return a viewer comparator for looking at the jobs.
	 * 
	 * @return ViewerComparator
	 */
	static ViewerComparator getProgressViewerComparator() {
		return new ViewerComparator() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public int compare(Viewer testViewer, Object e1, Object e2) {
				return ((Comparable) e1).compareTo(e2);
			}
		};
	}

	/**
	 * Open the progress view in the supplied window.
	 * 
	 * @param window
	 */
	static void openProgressView(WorkbenchWindow window) {
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return;
		}
		try {
			IViewDescriptor reference = WorkbenchPlugin.getDefault()
					.getViewRegistry()
					.find(IProgressConstants.PROGRESS_VIEW_ID);

			if (reference == null) {
				return;
			}
			page.showView(IProgressConstants.PROGRESS_VIEW_ID);
		} catch (PartInitException exception) {
			logException(exception);
		}
	}

	/**
	 * Shorten the given text <code>t</code> so that its length doesn't exceed
	 * the given width. The default implementation replaces characters in the
	 * center of the original string with an ellipsis ("..."). Override if you
	 * need a different strategy.
	 * 
	 * @param textValue
	 * @param control
	 * @return String
	 */

	static String shortenText(String textValue, Control control) {
		if (textValue == null) {
			return null;
		}
		GC gc = new GC(control);
		int maxWidth = control.getBounds().width - 5;
		int maxExtent = gc.textExtent(textValue).x;
		if (maxExtent < maxWidth) {
			gc.dispose();
			return textValue;
		}
		int length = textValue.length();
		int charsToClip = Math.round(0.95f * length
				* (1 - ((float) maxWidth / maxExtent)));
		int secondWord = findSecondWhitespace(textValue, gc, maxWidth);
		int pivot = ((length - secondWord) / 2) + secondWord;
		int start = pivot - (charsToClip / 2);
		int end = pivot + (charsToClip / 2) + 1;
		while (start >= 0 && end < length) {
			String s1 = textValue.substring(0, start);
			String s2 = textValue.substring(end, length);
			String s = s1 + ellipsis + s2;
			int l = gc.textExtent(s).x;
			if (l < maxWidth) {
				gc.dispose();
				return s;
			}
			start--;
			end++;
		}
		gc.dispose();
		return textValue;
	}

	/**
	 * Find the second index of a whitespace. Return the first index if there
	 * isn't one or 0 if there is no space at all.
	 * 
	 * @param textValue
	 * @param gc
	 *            The GC to test max length
	 * @param maxWidth
	 *            The maximim extent
	 * @return int
	 */
	private static int findSecondWhitespace(String textValue, GC gc,
			int maxWidth) {
		int firstCharacter = 0;
		char[] chars = textValue.toCharArray();
		// Find the first whitespace
		for (int i = 0; i < chars.length; i++) {
			if (Character.isWhitespace(chars[i])) {
				firstCharacter = i;
				break;
			}
		}
		// If we didn't find it once don't continue
		if (firstCharacter == 0) {
			return 0;
		}
		// Initialize to firstCharacter in case there is no more whitespace
		int secondCharacter = firstCharacter;
		// Find the second whitespace
		for (int i = firstCharacter; i < chars.length; i++) {
			if (Character.isWhitespace(chars[i])) {
				secondCharacter = i;
				break;
			}
		}
		// Check that we haven't gone over max width. Throw
		// out an index that is too high
		if (gc.textExtent(textValue.substring(0, secondCharacter)).x > maxWidth) {
			if (gc.textExtent(textValue.substring(0, firstCharacter)).x > maxWidth) {
				return 0;
			}
			return firstCharacter;
		}
		return secondCharacter;
	}

	/**
	 * If there are any modal shells open reschedule openJob to wait until they
	 * are closed. Return true if it rescheduled, false if there is nothing
	 * blocking it.
	 * 
	 * @param openJob
	 * @return boolean. true if the job was rescheduled due to modal dialogs.
	 */
	public static boolean rescheduleIfModalShellOpen(Job openJob) {
		Shell modal = getModalShellExcluding(null);
		if (modal == null) {
			return false;
		}

		// try again in a few seconds
		openJob.schedule(PlatformUI.getWorkbench().getProgressService()
				.getLongOperationTime());
		return true;
	}

	/**
	 * Return whether or not it is safe to open this dialog. If so then return
	 * <code>true</code>. If not then set it to open itself when it has had
	 * ProgressManager#longOperationTime worth of ticks.
	 * 
	 * @param dialog
	 *            ProgressMonitorJobsDialog that will be opening
	 * @param excludedShell
	 *            The shell
	 * @return boolean. <code>true</code> if it can open. Otherwise return
	 *         false and set the dialog to tick.
	 */
	public static boolean safeToOpen(ProgressMonitorJobsDialog dialog,
			Shell excludedShell) {
		Shell modal = getModalShellExcluding(excludedShell);
		if (modal == null) {
			return true;
		}

		dialog.watchTicks();
		return false;
	}

	/**
	 * Return the modal shell that is currently open. If there isn't one then
	 * return null.
	 * 
	 * @param shell
	 *            A shell to exclude from the search. May be <code>null</code>.
	 * 
	 * @return Shell or <code>null</code>.
	 */
	public static Shell getModalShellExcluding(Shell shell) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Shell[] shells = workbench.getDisplay().getShells();
		int modal = SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL
				| SWT.PRIMARY_MODAL;
		for (int i = 0; i < shells.length; i++) {
			if (shells[i].equals(shell)) {
				break;
			}
			// Do not worry about shells that will not block the user.
			if (shells[i].isVisible()) {
				int style = shells[i].getStyle();
				if ((style & modal) != 0) {
					return shells[i];
				}
			}
		}
		return null;
	}

	/**
	 * Utility method to get the best parenting possible for a dialog. If there
	 * is a modal shell create it so as to avoid two modal dialogs. If not then
	 * return the shell of the active workbench window. If neither can be found
	 * return null.
	 * 
	 * @return Shell or <code>null</code>
	 */
	public static Shell getDefaultParent() {
		Shell modal = getModalShellExcluding(null);
		if (modal != null) {
			return modal;
		}

		return getNonModalShell();
	}

	/**
	 * Get the active non modal shell. If there isn't one return null.
	 * 
	 * @return Shell
	 */
	public static Shell getNonModalShell() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window == null) {
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
					.getWorkbenchWindows();
			if (windows.length > 0)
				return windows[0].getShell();
		} else
			return window.getShell();

		return null;
	}

	/**
	 * Animate the closing of a window given the start position down to the
	 * progress region.
	 * 
	 * @param startPosition
	 *            Rectangle. The position to start drawing from.
	 */
	public static void animateDown(Rectangle startPosition) {
		IWorkbenchWindow currentWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (currentWindow == null) {
			return;
		}
		WorkbenchWindow internalWindow = (WorkbenchWindow) currentWindow;

		ProgressRegion progressRegion = internalWindow.getProgressRegion();
		if (progressRegion == null) {
			return;
		}
		Rectangle endPosition = progressRegion.getControl().getBounds();

		Point windowLocation = internalWindow.getShell().getLocation();
		endPosition.x += windowLocation.x;
		endPosition.y += windowLocation.y;
		RectangleAnimation animation = new RectangleAnimation(internalWindow
				.getShell(), startPosition, endPosition);
		animation.schedule();
	}

	/**
	 * Animate the opening of a window given the start position down to the
	 * progress region.
	 * 
	 * @param endPosition
	 *            Rectangle. The position to end drawing at.
	 */
	public static void animateUp(Rectangle endPosition) {
		IWorkbenchWindow currentWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (currentWindow == null) {
			return;
		}
		WorkbenchWindow internalWindow = (WorkbenchWindow) currentWindow;
		Point windowLocation = internalWindow.getShell().getLocation();

		ProgressRegion progressRegion = internalWindow.getProgressRegion();
		if (progressRegion == null) {
			return;
		}
		Rectangle startPosition = progressRegion.getControl().getBounds();
		startPosition.x += windowLocation.x;
		startPosition.y += windowLocation.y;

		RectangleAnimation animation = new RectangleAnimation(internalWindow
				.getShell(), startPosition, endPosition);
		animation.schedule();
	}

	/**
	 * Get the shell provider to use in the progress support dialogs. This
	 * provider will try to always parent off of an existing modal shell. If
	 * there isn't one it will use the current workbench window.
	 * 
	 * @return IShellProvider
	 */
	static IShellProvider getShellProvider() {
		return new IShellProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.window.IShellProvider#getShell()
			 */
			public Shell getShell() {
				return getDefaultParent();
			}
		};
	}

	/**
	 * Get the icons root for the progress support.
	 * 
	 * @return URL
	 */
	public static URL getIconsRoot() {
		return BundleUtility.find(PlatformUI.PLUGIN_ID,
				ProgressManager.PROGRESS_FOLDER);
	}

	/**
	 * Return the location of the progress spinner.
	 * 
	 * @return URL or <code>null</code> if it cannot be found
	 */
	public static URL getProgressSpinnerLocation() {
		try {
			return new URL(getIconsRoot(), "progress_spinner.gif");//$NON-NLS-1$
		} catch (MalformedURLException e) {
			return null;
		}
	}
}
