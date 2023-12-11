/*******************************************************************************
 * Copyright (c) 2011, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import org.eclipse.pde.api.tools.annotations.NoImplement;

/**
 * IDialogLabelKeys contains publicly accessible keys to the common dialog
 * labels used throughout JFace.  <code>IDialogConstants</code> provides
 * access to these labels using static constants.  This is the preferred
 * method when the client is optimizing for performance and is known to be
 * used in a single-locale system.  Using the keys and accessing the
 * common dialog labels dynamically is the preferred method when the client
 * may be running in a multi-locale system.
 *
 * @see IDialogConstants
 * @since 3.7
 */
@NoImplement
public interface IDialogLabelKeys {

	/**
	 * The key used to retrieve the label for OK buttons. Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.OK_LABEL_KEY)</code> to retrieve the label
	 * dynamically when using multiple locales.
	 *
	 * @since 3.7
	 */
	String OK_LABEL_KEY = "ok"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for cancel buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.CANCEL_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String CANCEL_LABEL_KEY = "cancel"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for yes buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.YES_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String YES_LABEL_KEY = "yes"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for no buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.NO_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String NO_LABEL_KEY = "no"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for no to all buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.NO_TO_ALL_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String NO_TO_ALL_LABEL_KEY = "notoall"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for yes to all buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.YES_TO_ALL_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String YES_TO_ALL_LABEL_KEY = "yestoall"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for skip buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.SKIP_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String SKIP_LABEL_KEY = "skip"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for stop buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.STOP_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String STOP_LABEL_KEY = "stop"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for abort buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.ABORT_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String ABORT_LABEL_KEY = "abort"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for retry buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.RETRY_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String RETRY_LABEL_KEY = "retry"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for ignore buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.IGNORE_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String IGNORE_LABEL_KEY = "ignore"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for proceed buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.PROCEED_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String PROCEED_LABEL_KEY = "proceed"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for open buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.OPEN_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String OPEN_LABEL_KEY = "open"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for close buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.CLOSE_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String CLOSE_LABEL_KEY = "close"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for show details buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.SHOW_DETAILS_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String SHOW_DETAILS_LABEL_KEY = "showDetails"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for hide details buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.HIDE_DETAILS_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String HIDE_DETAILS_LABEL_KEY = "hideDetails"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for back buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.BACK_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String BACK_LABEL_KEY = "backButton"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for next buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.NEXT_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String NEXT_LABEL_KEY = "nextButton"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for finish buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.FINISH_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String FINISH_LABEL_KEY = "finish"; //$NON-NLS-1$

	/**
	 * The key used to retrieve the label for help buttons.
	 * Clients should use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.HELP_LABEL_KEY)</code>
	 * to retrieve the label dynamically when using multiple locales.
	 * @since 3.7
	 */
	String HELP_LABEL_KEY = "help"; //$NON-NLS-1$
}
