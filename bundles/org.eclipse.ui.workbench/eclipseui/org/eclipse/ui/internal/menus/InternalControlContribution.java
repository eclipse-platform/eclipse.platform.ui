/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.menus;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Add workbench specific information to a standard control contribution. Allows
 * the client derivatives to access the IWorkbenchWindow hosting the control as
 * well as the side of the workbench that the control is currently being
 * displayed on.
 *
 * @since 3.3
 */
public abstract class InternalControlContribution extends ControlContribution {
	private IWorkbenchWindow wbw;
	private int curSide;

	protected InternalControlContribution(String id) {
		super(id);
	}

	public InternalControlContribution() {
		this("unknown ID"); //$NON-NLS-1$
	}

	/**
	 * @return Returns the wbw.
	 */
	public IWorkbenchWindow getWorkbenchWindow() {
		return wbw;
	}

	/**
	 * @param wbw The wbw to set.
	 */
	public void setWorkbenchWindow(IWorkbenchWindow wbw) {
		this.wbw = wbw;
	}

	/**
	 * @return Returns the curSide.
	 */
	public int getCurSide() {
		return curSide;
	}

	/**
	 * @param curSide The curSide to set.
	 */
	public void setCurSide(int curSide) {
		this.curSide = curSide;
	}

	public int getOrientation() {
		if (getCurSide() == SWT.LEFT || getCurSide() == SWT.RIGHT)
			return SWT.VERTICAL;

		return SWT.HORIZONTAL;
	}
}
