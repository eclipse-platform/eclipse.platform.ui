/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.themes;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.themes.IColorFactory;

/**
 * @since 3.0
 */
public class TestColorFactory implements IColorFactory {

    public static final RGB RGB = new RGB(91, 92, 93);

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.IColorFactory#createColor()
     */
    @Override
	public RGB createColor() {
        return RGB;
    }

}
