/*******************************************************************************
 * Copyright (c) 2003, 2018 IBM Corporation and others.
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
package org.eclipse.ui.internal.forms.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;

/**
 * Utility methods to access shared form-specific resources.
 * <p>
 * All methods declared on this class are static. This
 * class cannot be instantiated.
 * </p>
 * <p>
 * </p>
 */
public class FormsResources {

	public static Cursor getBusyCursor() {
		return Display.getCurrent().getSystemCursor(SWT.CURSOR_WAIT);
	}
	public static Cursor getHandCursor() {
		return Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND);
	}
	public static Cursor getTextCursor() {
		return Display.getCurrent().getSystemCursor(SWT.CURSOR_IBEAM);
	}

	public static int getProgressDelay(int index) {
		/*
		if (progressDelays==null)
			return 0;
		return progressDelays[index];
		*/
		return 100;
	}

	public static void shutdown() {
	}
}
