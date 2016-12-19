/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422040
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.AnimationEngine;
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

	@SuppressWarnings("unchecked")
	static class ProgressViewerComparator extends ViewerComparator {
		@Override
		@SuppressWarnings("rawtypes")
		public int compare(Viewer testViewer, Object e1, Object e2) {
			return ((Comparable) e1).compareTo(e2);
		}

		@Override
		public void sort(final Viewer viewer, Object[] elements) {
			/*
			 * https://bugs.eclipse.org/371354
			 *
			 * This ordering is inherently unstable, since it relies on
			 * modifiable properties of the elements: E.g. the default
			 * implementation in JobTreeElement compares getDisplayString(),
			 * many of whose implementations use getPercentDone().
			 *
			 * JavaSE 7+'s TimSort introduced a breaking change: It now throws a
			 * new IllegalArgumentException for bad comparators. Workaround is
			 * to retry a few times.
			 */
			for (int retries = 3; retries > 0; retries--) {
				try {
					Arrays.sort(elements, (a, b) -> ProgressViewerComparator.this.compare(viewer, a, b));
					return; // success
				} catch (IllegalArgumentException e) {
					// retry
				}
			}

			// One last try that will log and throw TimSort's IAE if it happens:
			super.sort(viewer, elements);
		}
	}

	/**
	 * A constant used by the progress support to determine if an operation is
	 * too short to show progress.
	 */
	public static long SHORT_OPERATION_TIME = 250;

	static final QualifiedName KEEP_PROPERTY = IProgressConstants.KEEP_PROPERTY;

	static final QualifiedName KEEPONE_PROPERTY = IProgressConstants.KEEPONE_PROPERTY;

	static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

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
		return new ProgressViewerComparator();
	}

	/**
	 * Open the progress view in the supplied window.
	 *
	 * @param window
	 */
	static void openProgressView(IWorkbenchWindow window) {
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
		int maxWidth = control.getBounds().width - 5;
		String ellipsisString = ellipsis;
		GC gc = new GC(control);
		try {
			return clipToSize(gc, textValue, ellipsisString, maxWidth);
		} finally {
			gc.dispose();
		}
	}

	private static String clipToSize(GC gc, String textValue, String ellipsisString, int maxWidth) {
		int averageCharWidth = gc.getFontMetrics().getAverageCharWidth();
		int length = textValue.length();

		int secondWord = findSecondWhitespace(textValue, gc, maxWidth);
		int pivot = ((length - secondWord) / 2) + secondWord;

		int currentLength;
		int upperBoundWidth;
		int upperBoundLength = 0;

		// Now use newton's method to search for the correct string size
		int lowerBoundLength = 0;
		int lowerBoundWidth = 0;

		// Try to guess the size of the string based on the font's average
		// character width
		int estimatedCharactersThatWillFit = maxWidth / averageCharWidth;

		if (estimatedCharactersThatWillFit >= length) {
			int maxExtent = gc.textExtent(textValue).x;
			if (maxExtent <= maxWidth) {
				return textValue;
			}
			currentLength = Math.max(0,
					Math.round(length * ((float) maxWidth / maxExtent)) - ellipsisString.length());
			upperBoundWidth = maxExtent;
			upperBoundLength = length;
		} else {
			currentLength = Math.min(length, Math.max(0, estimatedCharactersThatWillFit - ellipsisString.length()));
			for (;;) {
				String s = clipToLength(textValue, ellipsisString, pivot, currentLength);
				int currentExtent = gc.textExtent(s).x;
				if (currentExtent > maxWidth) {
					upperBoundWidth = currentExtent;
					upperBoundLength = currentLength;
					break;
				}
				if (currentLength == length) {
					// No need to clip the string if the whole thing fits.
					return textValue;
				}
				lowerBoundWidth = currentExtent;
				lowerBoundLength = currentLength;
				currentLength = Math.min(length, currentLength * 2 + 1);
			}
		}

		String s;
		for (;;) {
			int oldLength = currentLength;
			s = clipToLength(textValue, ellipsisString, pivot, currentLength);

			int l = gc.textExtent(s).x;
			int tooBigBy = l - maxWidth;
			if (tooBigBy == 0) {
				// If this was exactly the right size, stop the binary
				// search
				break;
			} else if (tooBigBy > 0) {
				// The string is too big. Need to clip more.
				upperBoundLength = currentLength;
				upperBoundWidth = l;
				if (currentLength <= lowerBoundLength + 1) {
					// We're one character away from a value that is known
					// to clip too much, so opt for clipping slightly too
					// much
					currentLength = lowerBoundLength;
					break;
				}
				if (tooBigBy <= averageCharWidth * 2) {
					currentLength--;
				} else {
					int spaceToRightOfLowerBound = maxWidth - lowerBoundWidth;
					currentLength = lowerBoundLength
							+ (currentLength - lowerBoundLength) * spaceToRightOfLowerBound / (l - lowerBoundWidth);
					if (currentLength >= oldLength) {
						currentLength = oldLength - 1;
					} else if (currentLength <= lowerBoundLength) {
						currentLength = lowerBoundLength + 1;
					}
				}
			} else {
				// The string is too small. Need to clip less.
				lowerBoundLength = currentLength;
				lowerBoundWidth = l;
				if (currentLength >= upperBoundLength - 1) {
					// We're one character away from a value that is known
					// to clip too little, so opt for clipping slightly
					// too much
					currentLength = upperBoundLength - 1;
					break;
				}
				if (-tooBigBy <= averageCharWidth * 2) {
					currentLength++;
				} else {
					currentLength = currentLength
							+ (upperBoundLength - currentLength) * (-tooBigBy) / (upperBoundWidth - l);
					if (currentLength <= oldLength) {
						currentLength = oldLength + 1;
					} else if (currentLength >= upperBoundLength) {
						currentLength = upperBoundLength - 1;
					}
				}
			}
		}

		s = clipToLength(textValue, ellipsisString, pivot, currentLength);
		return s;
	}

	private static String clipToLength(String textValue, String ellipsisString, int pivot, int newLength) {
		return getClippedString(textValue, ellipsisString, pivot, textValue.length() - newLength);
	}

	private static String getClippedString(String textValue, String ellipsisString, int pivot, int charsToClip) {
		int length = textValue.length();
		if (charsToClip <= 0) {
			return textValue;
		}
		if (charsToClip >= length) {
			return ""; //$NON-NLS-1$
		}
		String s;
		int start = pivot - charsToClip / 2;
		int end = pivot + (charsToClip + 1) / 2;

		if (start < 0) {
			end -= start;
			start = 0;
		}
		if (end < 0) {
			start -= end;
			end = 0;
		}

		String s1 = textValue.substring(0, start);
		String s2;
		if (end < length) {
			s2 = textValue.substring(end, length);
		} else {
			s2 = ""; //$NON-NLS-1$
		}
		s = s1 + ellipsisString + s2;
		return s;
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
	 * return null. If there are stacked modal shells, return the top one.
	 *
	 * @param shell
	 *            A shell to exclude from the search. May be <code>null</code>.
	 *
	 * @return Shell or <code>null</code>.
	 */

	public static Shell getModalShellExcluding(Shell shell) {

		// If shell is null or disposed, then look through all shells
		if (shell == null || shell.isDisposed()) {
			return getModalChildExcluding(PlatformUI.getWorkbench()
					.getDisplay().getShells(), shell);
		}

		// Start with the shell to exclude and check it's shells
		return getModalChildExcluding(shell.getShells(), shell);
	}

	/**
	 * Return the modal shell that is currently open. If there isn't one then
	 * return null.
	 *
	 * @param toSearch shells to search for modal children
	 * @param toExclude shell to ignore
	 * @return the most specific modal child, or null if none
	 */
	private static Shell getModalChildExcluding(Shell[] toSearch, Shell toExclude) {
		int modal = SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL
				| SWT.PRIMARY_MODAL;

		// Make sure we don't pick a parent that has a modal child (this can
		// lock the app)
		// If we picked a parent with a modal child, use the modal child instead

		for (int i = toSearch.length - 1; i >= 0; i--) {
			Shell shell = toSearch[i];
			if(shell.equals(toExclude)) {
				continue;
			}

			// Check if this shell has a modal child
			Shell[] children = shell.getShells();
			Shell modalChild = getModalChildExcluding(children, toExclude);
			if (modalChild != null) {
				return modalChild;
			}

			// If not, check if this shell is modal itself
			if (shell.isVisible() && (shell.getStyle() & modal) != 0) {
				return shell;
			}
		}

		return null;
	}

	/**
	 * Utility method to get the best parenting possible for a dialog. If there
	 * is a modal shell return it so as to avoid two modal dialogs. If not then
	 * return the shell of the active workbench window. If that shell is
	 * <code>null</code> or not visible, then return the splash shell if still
	 * visible. Otherwise return the shell of the active workbench window.
	 *
	 * @return the best parent shell or <code>null</code>
	 */
	public static Shell getDefaultParent() {
		Shell modal = getModalShellExcluding(null);
		if (modal != null) {
			return modal;
		}

		Shell nonModalShell = getNonModalShell();
		if (nonModalShell != null && nonModalShell.isVisible())
			return nonModalShell;

		try {
			Shell splashShell = WorkbenchPlugin.getSplashShell(PlatformUI.getWorkbench().getDisplay());
			if (splashShell != null && splashShell.isVisible()) {
				return splashShell;
			}
		} catch (IllegalAccessException e) {
			// Use non-modal shell
		} catch (InvocationTargetException e) {
			// Use non-modal shell
		}

		return nonModalShell;
	}

	/**
	 * Get the active non modal shell. If there isn't one return null.
	 *
	 * @return Shell
	 */
	public static Shell getNonModalShell() {
		MApplication application = PlatformUI.getWorkbench().getService(MApplication.class);
		if (application == null) {
			// better safe than sorry
			return null;
		}
		MWindow window = application.getSelectedElement();
		if (window != null) {
			Object widget = window.getWidget();
			if (widget instanceof Shell) {
				return (Shell) widget;
			}
		}
		for (MWindow child : application.getChildren()) {
			Object widget = child.getWidget();
			if (widget instanceof Shell) {
				return (Shell) widget;
			}
		}
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

		// animate the progress dialog's removal
		AnimationEngine.createTweakedAnimation(internalWindow.getShell(), 400, startPosition, endPosition);
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

		// animate the progress dialog's arrival
		AnimationEngine.createTweakedAnimation(internalWindow.getShell(), 400, startPosition, endPosition);
	}

	/**
	 * Get the shell provider to use in the progress support dialogs. This
	 * provider will try to always parent off of an existing modal shell. If
	 * there isn't one it will use the current workbench window.
	 *
	 * @return IShellProvider
	 */
	static IShellProvider getShellProvider() {
		return () -> getDefaultParent();
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
			return new URL(getIconsRoot(), "progress_spinner.png");//$NON-NLS-1$
		} catch (MalformedURLException e) {
			return null;
		}
	}
}
