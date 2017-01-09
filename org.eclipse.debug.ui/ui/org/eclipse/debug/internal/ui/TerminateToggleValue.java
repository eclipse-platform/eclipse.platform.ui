/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;

import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;

/**
 * Class stores the data for shortcut and if Shift was pressed with shortcut
 * This is used to store that data for selected selection and later used at the
 * launching of the shortcut
 *
 * @since 3.12
 */
public class TerminateToggleValue {
	private boolean fIsShift;
	private LaunchShortcutExtension fShortcut;

	public TerminateToggleValue(boolean isShift, LaunchShortcutExtension shortcut) {
		fIsShift = isShift;
		fShortcut = shortcut;
	}

	public boolean isShift() {
		return fIsShift;
	}

	public LaunchShortcutExtension getShortcut() {
		return fShortcut;
	}

}
