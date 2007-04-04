/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.util;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.presentations.IPresentablePart;

/**
 * @since 3.1
 */
public interface ISystemMenu {
    void show(Control control, Point displayCoordinates, IPresentablePart currentSelection);
    
    /**
     * Disposes of this system menu.
     * 
     * @since 3.3
     */
    void dispose();
}
