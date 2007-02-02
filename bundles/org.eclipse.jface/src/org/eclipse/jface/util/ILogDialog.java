/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Chris Gross (schtoo@schtoo.com) - initial API and implementation
 *       (bug 49497 [RCP] JFace dependency on org.eclipse.core.runtime enlarges standalone JFace applications)
 *******************************************************************************/

package org.eclipse.jface.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;

/**
 * A mechanism for showing statuses throughout JFace.
 * <p>
 * Clients may provide their own implementation to change the way statuses are
 * shown from within JFace.
 * </p>
 * 
 * @see org.eclipse.jface.util.Policy#getLogDialog()
 * @see org.eclipse.jface.util.Policy#setLogDialog(ILogDialog)
 * @since 3.3
 */
public interface ILogDialog {

	/**
	 * Shows the given status.
	 * 
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param title
	 *            the dialog's title, or <code>null</code> if none
	 * @param message
	 *            the message to show in this dialog, or <code>null</code> to
	 *            indicate that the error's message should be shown as the
	 *            primary message
	 * @param status
	 *            the error to show to the user
	 * @param displayMask
	 *            the mask to use to filter the displaying of child items, as
	 *            per <code>IStatus.matches</code>
	 * @return the code of the button that was pressed that resulted in this
	 *         dialog closing. This will be <code>Dialog.OK</code> if the OK
	 *         button was pressed, or <code>Dialog.CANCEL</code> if this
	 *         dialog's close window decoration or the ESC key was used.
	 */
	public int log(Shell parent, String title, String message, IStatus status,
			int displayMask);

	/**
	 * Shows the given status.
	 * 
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param severity
	 *            the severity of logged message
	 * @param title
	 *            the dialog's title, or <code>null</code> if none
	 * @param message
	 *            the message to show in this dialog
	 * @return the code of the button that was pressed that resulted in this
	 *         dialog closing. This will be <code>Dialog.OK</code> if the OK
	 *         button was pressed, or <code>Dialog.CANCEL</code> if this
	 *         dialog's close window decoration or the ESC key was used.
	 */
	public int log(Shell parent, int severity, String title, String message);
}
