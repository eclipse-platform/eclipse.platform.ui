/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dnd;

import org.eclipse.swt.graphics.Point;

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
}