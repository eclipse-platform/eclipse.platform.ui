/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Utility class to help with dialogs.
 * <p>
 * Note that a copy of this class exists in the org.eclipse.ui.internal.ide
 * package.
 * </p>
 */
public class DialogUtil {

	/**
	 * Prevent instantiation.
	 */
	private DialogUtil() {
	}

	/**
	 * Open an error style dialog for PartInitException by including any extra
	 * information from the nested CoreException if present.
	 */
	public static void openError(String message, PartInitException exception) {
		// Check for a nested CoreException
		CoreException nestedException = null;
		IStatus status = exception.getStatus();
		if (status != null && status.getException() instanceof CoreException) {
			nestedException = (CoreException) status.getException();
		}

		IStatus errorStatus = null;

		if (nestedException != null) {
			// Open an error dialog and include the extra
			// status information from the nested CoreException
			errorStatus = StatusUtil.newStatus(nestedException.getStatus(), message);
		} else {
			// Open a regular error dialog since there is no
			// extra information to displa
			errorStatus = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, message);
		}

		StatusUtil.handleStatus(errorStatus, StatusManager.SHOW);
	}

	/**
	 * Return the number of rows available in the current display using the current
	 * font.
	 *
	 * @param parent The Composite whose Font will be queried.
	 * @return int The result of the display size divided by the font size.
	 */
	public static int availableRows(Composite parent) {

		int fontHeight = (parent.getFont().getFontData())[0].getHeight();
		int displayHeight = parent.getDisplay().getClientArea().height;

		return displayHeight / fontHeight;
	}

	/**
	 * Return whether or not the font in the parent is the size of a result font
	 * (i.e. smaller than the High Contrast Font). This method is used to make
	 * layout decisions based on screen space.
	 *
	 * @param parent The Composite whose Font will be queried.
	 * @return boolean. True if there are more than 50 lines of possible text in the
	 *         display.
	 */
	public static boolean inRegularFontMode(Composite parent) {

		return availableRows(parent) > 50;
	}
}
