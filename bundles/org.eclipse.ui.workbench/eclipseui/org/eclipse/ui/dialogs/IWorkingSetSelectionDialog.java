/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.dialogs;

import org.eclipse.ui.IWorkingSet;

/**
 * A working set selection dialog displays the list of working sets available in
 * the workbench.
 * <p>
 * Use org.eclipse.ui.IWorkingSetManager#createWorkingSetSelectionDialog(Shell)
 * to create an instance of this dialog.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see org.eclipse.ui.IWorkingSetManager
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkingSetSelectionDialog {
	/**
	 * Returns the working sets selected in the dialog or <code>null</code> if the
	 * dialog was canceled.
	 *
	 * @return the working sets selected in the dialog.
	 */
	IWorkingSet[] getSelection();

	/**
	 * Displays the working set selection dialog.
	 *
	 * @return Window.OK if the dialog closes with the working set selection
	 *         confirmed. Window.CANCEL if the dialog closes with the working set
	 *         selection dismissed.
	 * @see org.eclipse.jface.window.Window
	 */
	int open();

	/**
	 * Sets the working sets that are initially selected in the dialog.
	 *
	 * @param workingSets the working sets to select in the dialog.
	 */
	void setSelection(IWorkingSet[] workingSets);
}
