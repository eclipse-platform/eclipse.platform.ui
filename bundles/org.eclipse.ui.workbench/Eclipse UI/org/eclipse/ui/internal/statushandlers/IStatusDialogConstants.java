/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.statushandlers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.statushandlers.WorkbenchStatusDialogManager;

/**
 * This class contains constant necessary to read/write the
 * {@link WorkbenchStatusDialogManager} properties. Some properties may be
 * promoted to the API.
 * 
 */
public interface IStatusDialogConstants {
	
	/**
	 * This property can be only read. It will return the current dialog
	 * {@link Shell}. It may be null.
	 */
	public static final Object SHELL = Shell.class;
	
	/**
	 * This property indicates if the support area should be opened when the
	 * dialog appears. The value must be of {@link Boolean} type.
	 * {@link Boolean#TRUE} means that the support area will be opened, while
	 * {@link Boolean#FALSE} means that it will be in the closed state.
	 */
	public static final Object SHOW_SUPPORT = new Object();
	
	/**
	 * This property indicates if the dialog should display a link to the Error
	 * Log if the Error Log view is available. The value must be of
	 * {@link Boolean} type. {@link Boolean#TRUE} means that the link will be
	 * present if the Error Log is available, {@link Boolean#FALSE} means that
	 * it will never be displayed.
	 */
	public static final Object ERRORLOG_LINK = new Object();

	/**
	 * This property indicates if the dialog should threat {@link Status}es with
	 * severity {@link IStatus#OK} as all other statuses. The value must be of
	 * {@link Boolean} type. A {@link Boolean#TRUE} means that those
	 * {@link Status}es will be handled as all others, while
	 * {@link Boolean#FALSE} means that they will be silently ignored.
	 */
	public static final Object HANDLE_OK_STATUSES = new Object();
}
