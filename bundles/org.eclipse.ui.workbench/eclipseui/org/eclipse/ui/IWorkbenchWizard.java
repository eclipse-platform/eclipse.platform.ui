/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

package org.eclipse.ui;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;

/**
 * Implementors represent creation wizards that are to be contributed to the
 * workbench's creation wizard extension point.
 *
 * @see org.eclipse.jface.wizard.IWizard
 */
public interface IWorkbenchWizard extends IWizard {
	/**
	 * Initializes this creation wizard using the passed workbench and object
	 * selection.
	 * <p>
	 * This method is called after the no argument constructor and before other
	 * methods are called.
	 * </p>
	 *
	 * @param workbench the current workbench
	 * @param selection the current object selection
	 */
	void init(IWorkbench workbench, IStructuredSelection selection);
}
