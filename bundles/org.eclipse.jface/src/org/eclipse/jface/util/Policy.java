/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
import org.eclipse.jface.dialogs.AnimatorFactory;
import org.eclipse.jface.dialogs.ErrorSupportProvider;

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
	public static final String JFACE = "org.eclipse.jface";//$NON-NLS-1$

	private static ILogger log;

	private static Comparator viewerComparator;

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

	/**
	 * Returns the dummy log to use if none has been set
	 */
	private static ILogger getDummyLog() {
		return new ILogger() {
			public void log(IStatus status) {
				System.err.println(status.getMessage());
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
	 * Return the default comparator used by JFace to sort strings.
	 * 
	 * @return a default comparator used by JFace to sort strings
	 */
	private static Comparator getDefaultComparator() {
		return new Comparator() {
			/**
			 * Compares string s1 to string s2.
			 * 
			 * @param s1
			 *            string 1
			 * @param s2
			 *            string 2
			 * @return Returns an integer value. Value is less than zero if
			 *         source is less than target, value is zero if source and
			 *         target are equal, value is greater than zero if source is
			 *         greater than target.
			 * @exception ClassCastException
			 *                the arguments cannot be cast to Strings.
			 */
			public int compare(Object s1, Object s2) {
				return ((String) s1).compareTo((String) s2);
			}
		};
	}

	/**
	 * Return the comparator used by JFace to sort strings.
	 * 
	 * @return the comparator used by JFace to sort strings
	 * @since 3.2
	 */
	public static Comparator getComparator() {
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
	public static void setComparator(Comparator comparator) {
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
	public static AnimatorFactory getAnimatorFactory() {
		if (animatorFactory == null)
			animatorFactory = new AnimatorFactory();
		return animatorFactory;
	}

	/**
	 * Set the error support provider for error dialogs.
	 * 
	 * @param provider
	 */
	public static void setErrorSupportProvider(ErrorSupportProvider provider) {
		errorSupportProvider = provider;
	}

	/**
	 * Return the ErrorSupportProvider for the receiver.
	 * 
	 * @return ErrorSupportProvider or <code>null</code> if this has not been set
	 */
	public static ErrorSupportProvider getErrorSupportProvider() {
		return errorSupportProvider;
	}

}
