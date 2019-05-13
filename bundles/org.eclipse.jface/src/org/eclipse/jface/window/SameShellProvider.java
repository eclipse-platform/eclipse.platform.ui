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
package org.eclipse.jface.window;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Standard shell provider that always returns the shell containing the given
 * control. This will always return the correct shell for the control, even if
 * the control is reparented.
 *
 * @since 3.1
 */
public class SameShellProvider implements IShellProvider {

	private Control targetControl;

	/**
	 * Returns a shell provider that always returns the current
	 * shell for the given control.
	 *
	 * @param targetControl control whose shell will be tracked, or null if getShell() should always
	 * return null
	 */
	public SameShellProvider(Control targetControl) {
		this.targetControl = targetControl;
	}

	@Override
	public Shell getShell() {
		if (targetControl instanceof Shell) {
			return (Shell)targetControl;
		}

		return targetControl == null? null :targetControl.getShell();
	}

}
