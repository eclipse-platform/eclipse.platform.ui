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

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IWorkingSet;

/**
 * A working set new wizard allows creating new working sets using a plug-in
 * specific working set page.
 * <p>
 * Use org.eclipse.ui.IWorkingSetManager#createWorkingSetNewWizard(String[]
 * workingSetIds) to create an instance of this wizard.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see org.eclipse.ui.IWorkingSetManager
 * @see org.eclipse.ui.dialogs.IWorkingSetPage
 *
 * @since 3.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkingSetNewWizard extends IWizard {

	/**
	 * Returns the new working set. Returns <code>null</code> if the wizard has been
	 * cancelled.
	 *
	 * @return the new working set or <code>null</code> if the wizard has been
	 *         cancelled.
	 */
	IWorkingSet getSelection();
}
