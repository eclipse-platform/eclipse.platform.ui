/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.themes;

import org.eclipse.swt.graphics.RGB;

/**
 * LightColorSelectionFactory is a factory that returns the lighter of two
 * {@link RGB} instances.
 * 
 * @since 3.3
 * 
 */

public class LightColorSelectionFactory extends ColorSelectionFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.themes.ColorSelectionFactory#compare(org.eclipse.swt.graphics.RGB, org.eclipse.swt.graphics.RGB)
	 */
	RGB compare(RGB rgb1, RGB rgb2) {
		if (difference(rgb1, rgb2) > 0)
			return rgb1;
		return rgb2;
	}

}
