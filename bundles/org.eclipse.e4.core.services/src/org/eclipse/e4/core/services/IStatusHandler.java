/*******************************************************************************
 *  Copyright (c) 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.services;

import org.eclipse.core.runtime.IStatus;

/**
 * Handling warnings or errors, with the intent of making the end user aware of
 * these. Strings are expected to be translated.
 * 
 * @see ILogger
 */
public interface IStatusHandler {
	/**
	 * A style indicating that the status should not be acted on. This is used
	 * by objects such as log listeners that do not want to report a status
	 * twice.
	 */
	public static final int NONE = 0;

	/**
	 * A style indicating that the status should be logged only.
	 */
	public static final int LOG = 0x01;

	/**
	 * A style indicating that handlers should show a problem to an user without
	 * blocking the calling method while awaiting user response. This is
	 * generally done using a non modal Dialog.
	 */
	public static final int SHOW = 0x02;

	/**
	 * A style indicating that the handling should block the calling thread
	 * until the status has been handled.
	 * <p>
	 * A typical usage of this would be to ensure that the user's actions are
	 * blocked until they've dealt with the status in some manner. It is
	 * therefore likely but not required that the <code>StatusHandler</code>
	 * would achieve this through the use of a modal dialog.
	 * </p>
	 * <p>
	 * Due to the fact that use of <code>BLOCK</code> will block any thread,
	 * care should be taken in using this flag.
	 * </p>
	 */
	public static final int BLOCK = 0x04;

	/**
	 * Handle the given status, using the given style as a hint.
	 * 
	 * @param status
	 *            a status, not <code>null</code>
	 * @param style
	 *            one of the style constants
	 */
	public void handle(IStatus status, int style);

	/**
	 * Handle the given error, using the given style as a hint.
	 * 
	 * @param t
	 *            a throwable, or optionally <code>null</code> if message is not
	 *            <code>null</code>
	 * @param message
	 *            a message, or optionally <code>null</code> if t is not
	 *            <code>null</code>
	 * @param style
	 *            one of the style constants
	 */
	public void handleError(Throwable t, String message, int style);

	/**
	 * Handle the given warning, using the given style as a hint.
	 * 
	 * @param t
	 *            a throwable, or optionally <code>null</code> if message is not
	 *            <code>null</code>
	 * @param message
	 *            a message, or optionally <code>null</code> if t is not
	 *            <code>null</code>
	 * @param style
	 *            one of the style constants
	 */
	public void handleWarning(Throwable t, String message, int style);
}
