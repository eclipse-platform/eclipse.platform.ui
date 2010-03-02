/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dnd;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * This is an interface intended for use in test suites. Objects can implement
 * this interface to force any dragged object to be dropped at a particular
 * location.
 * 
 * @since 3.0
 */
public interface TestDropLocation {

    /**
     * Location where the object should be dropped, in display coordinates
     * 
     * @return a location in display coordinates
     */
    public Point getLocation();
    
    /**
     * The drop code will pretend that only the given shells are open,
     * and that they have the specified Z-order.
     *
     * @return the shells to check for drop targets, from bottom to top.
     */
    public Shell[] getShells();
}
