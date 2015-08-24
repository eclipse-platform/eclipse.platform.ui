/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris Gross (schtoo@schtoo.com) - support for ILogger added
 *       (bug 49497 [RCP] JFace dependency on org.eclipse.core.runtime enlarges standalone JFace applications)
 *******************************************************************************/
package org.eclipse.jface.util;

import java.util.Comparator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.AnimatorFactory;
import org.eclipse.jface.dialogs.ErrorSupportProvider;
import org.eclipse.swt.widgets.Display;

/**
 * The Policy class handles settings for behaviour, debug flags and logging
 * within JFace.
 *
 * @since 3.0
 */
public class Policy {

	/**
	 * Constant for the the default setting for debug options.
	 */
	public static final boolean DEFAULT = false;

	/**
	 * The unique identifier of the JFace plug-in.
	 */
	public static final String JFACE = "org.eclipse.jface"; //$NON-NLS-1$

	private static ILogger log;

	/**
	 * The comparator used by JFace to sort strings, or {@code null}.
	 * <p>
	 * Note: The type is {@code Comparator<Object>} because this is usually a
	 * {@link java.text.Collator} (or its ICU equivalent), and Collator
	 * implements {@code Comparator<Object>}. See
	 * https://bugs.eclipse.org/434325 .
	 * </p>
	 */
	private static Comparator<Object> viewerComparator;

	private static AnimatorFactory animatorFactory;

	/**
	 * A flag to indicate whether unparented dialogs should be checked.
	 */
	public static boolean DEBUG_DIALOG_NO_PARENT = DEFAULT;

	/**
	 * A flag to indicate whether actions are being traced.
	 */
	public static boolean TRACE_ACTIONS = DEFAULT;

	/**
	 * A flag to indicate whether toolbars are being traced.
	 */

	public static boolean TRACE_TOOLBAR = DEFAULT;

	private static ErrorSupportProvider errorSupportProvider;

	private static StatusHandler statusHandler;

	/**
	 * Returns the dummy log to use if none has been set
	 */
	private static ILogger getDummyLog() {
		return status -> {
			System.err.println(status.getMessage());
			if (status.getException() != null) {
				status.getException().printStackTrace(System.err);
			}
		};
	}

	/**
	 * Sets the logger used by JFace to log errors.
	 *
	 * @param logger
	 *            the logger to use, or <code>null</code> to use the default
	 *            logger
	 * @since 3.1
	 */
	public static void setLog(ILogger logger) {
		log = logger;
	}

	/**
	 * Returns the logger used by JFace to log errors.
	 * <p>
	 * The default logger prints the status to <code>System.err</code>.
	 * </p>
	 *
	 * @return the logger
	 * @since 3.1
	 */
	public static ILogger getLog() {
		if (log == null) {
			log = getDummyLog();
		}
		return log;
	}

	/**
	 * Sets the status handler used by JFace to handle statuses.
	 *
	 * @param status
	 *            the handler to use, or <code>null</code> to use the default
	 *            one
	 * @since 3.4
	 */
	public static void setStatusHandler(StatusHandler status) {
		statusHandler = status;
	}

	/**
	 * Returns the status handler used by JFace to handle statuses.
	 *
	 * @return the status handler
	 * @since 3.4
	 */
	public static StatusHandler getStatusHandler() {
		if (statusHandler == null) {
			statusHandler = getDummyStatusHandler();
		}
		return statusHandler;
	}

	private static StatusHandler getDummyStatusHandler() {
		return new StatusHandler() {
			private SafeRunnableDialog dialog;

			@Override
			public void show(final IStatus status, String title) {
				Runnable runnable = () -> {
					if (dialog == null || dialog.getShell().isDisposed()) {
						dialog = new SafeRunnableDialog(status);
						dialog.create();
						dialog.getShell().addDisposeListener(
								e -> dialog = null);
						dialog.open();
					} else {
						dialog.addStatus(status);
						dialog.refresh();
					}
				};
				if (Display.getCurrent() != null) {
					runnable.run();
				} else {
					Display.getDefault().asyncExec(runnable);
				}
			}
		};
	}

	/**
	 * Return the default comparator used by JFace to sort strings.
	 *
	 * @return a default comparator used by JFace to sort strings
	 */
	private static Comparator<Object> getDefaultComparator() {
		return (s1, s2) -> ((String) s1).compareTo((String) s2);
	}

	/**
	 * Return the comparator used by JFace to sort strings.
	 *
	 * @return the comparator used by JFace to sort strings
	 * @since 3.2
	 */
	public static Comparator<Object> getComparator() {
		if (viewerComparator == null) {
			viewerComparator = getDefaultComparator();
		}
		return viewerComparator;
	}

	/**
	 * Sets the comparator used by JFace to sort strings.
	 *
	 * @param comparator
	 *            comparator used by JFace to sort strings
	 * @since 3.2
	 */
	public static void setComparator(Comparator<Object> comparator) {
		org.eclipse.core.runtime.Assert.isTrue(viewerComparator == null);
		viewerComparator = comparator;
	}

	/**
	 * Sets the animator factory used by JFace to create control animator
	 * instances.
	 *
	 * @param factory
	 *            the AnimatorFactory to use.
	 * @since 3.2
	 * @deprecated this is no longer in use as of 3.3
	 */
	@Deprecated
	public static void setAnimatorFactory(AnimatorFactory factory) {
		animatorFactory = factory;
	}

	/**
	 * Returns the animator factory used by JFace to create control animator
	 * instances.
	 *
	 * @return the animator factory used to create control animator instances.
	 * @since 3.2
	 * @deprecated this is no longer in use as of 3.3
	 */
	@Deprecated
	public static AnimatorFactory getAnimatorFactory() {
		if (animatorFactory == null)
			animatorFactory = new AnimatorFactory();
		return animatorFactory;
	}

	/**
	 * Set the error support provider for error dialogs.
	 *
	 * @param provider
	 * @since 3.3
	 */
	public static void setErrorSupportProvider(ErrorSupportProvider provider) {
		errorSupportProvider = provider;
	}

	/**
	 * Return the ErrorSupportProvider for the receiver.
	 *
	 * @return ErrorSupportProvider or <code>null</code> if this has not been
	 *         set
	 * @since 3.3
	 */
	public static ErrorSupportProvider getErrorSupportProvider() {
		return errorSupportProvider;
	}

	/**
	 * Log the Exception to the logger.
	 *
	 * @param exception
	 * @since 3.4
	 */
	public static void logException(Exception exception) {
		getLog().log(
				new Status(IStatus.ERROR, JFACE, exception
						.getLocalizedMessage(), exception));

	}

}
