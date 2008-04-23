/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.ui.internal.util;

import org.eclipse.swt.widgets.Display;

public class FontUtils {
	
	private static final int TRAY_FONT_HEIGHT_LIMIT = 17;

	/**
	 * Determine whether the font is suitable for use in a tray dialog
	 * @return true if the font is so large that the tray would look bad.
	 */
	static public boolean isFontTooLargeForTray() {
		try {
			int height = Display.getDefault().getSystemFont().getFontData()[0].getHeight();
			return height > TRAY_FONT_HEIGHT_LIMIT;
		} catch (RuntimeException e) {
			return true;
		}
	}

}
