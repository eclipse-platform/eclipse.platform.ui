/*******************************************************************************
 * Copyright (c) 2007 - 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.menus;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.menus.InternalControlContribution;

/**
 * Abstract base class from which all controls contributions to the workbench
 * through the 'org.eclipse.ui.menus' extension point must derive.
 * <p>
 * The extends the {@link ControlContribution} by adding accessor methods that
 * provide extra state information about the placement of the control:
 * </p>
 * <ul>
 * <li>getWorkbenchWindow() - indicates which workbench window this control is
 * being hosted by</li>
 * <li>getCurSide() - indicates which side of the workbench window the control
 * is being displayed on</li>
 * </ul>
 *
 * @since 3.3
 *
 * @see ControlContribution
 */
public abstract class WorkbenchWindowControlContribution extends InternalControlContribution {

	/**
	 * Default contstructor that allows the use of this class as the basis for XML
	 * contributions and will be used by the workbench implementation. This is
	 * public only by necessity and should not be used outside of the workbench
	 * implemenation code.
	 */
	public WorkbenchWindowControlContribution() {
		this("unknown ID"); //$NON-NLS-1$
	}

	/**
	 * Constructor for use by clients programmatically creating control
	 * contributions in the workbench.
	 *
	 * @param id The id of this contribution
	 */
	public WorkbenchWindowControlContribution(String id) {
		super(id);
	}

	/**
	 * @return Returns the workbench window currently hosting the control.
	 */
	@Override
	public final IWorkbenchWindow getWorkbenchWindow() {
		return super.getWorkbenchWindow();
	}

	/**
	 * @return Returns the side of the workbench window that the control is
	 *         currently being display on. This allows derivatives to tailor their
	 *         created control based on the orientation...
	 */
	@Override
	public final int getCurSide() {
		return super.getCurSide();
	}

	@Override
	public final int getOrientation() {
		if (getCurSide() == SWT.LEFT || getCurSide() == SWT.RIGHT)
			return SWT.VERTICAL;

		return SWT.HORIZONTAL;
	}

	/**
	 * Important: This method is *NOT* to be used/extended by clients. This is for
	 * the internal use inside Workbench
	 *
	 * @param parent the parent composite
	 * @return newly created Control
	 * @since 3.6
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by
	 *             clients.
	 */
	public Control delegateCreateControl(Composite parent) {
		return createControl(parent);
	}
}
