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
package org.eclipse.ui.tests.dnd;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.dnd.TestDropLocation;

public class DetachedDropTarget implements TestDropLocation {

    @Override
	public String toString() {
        return "out of the window";
    }

    @Override
	public Point getLocation() {
        return new Point(0,0);
    }

    @Override
	public Shell[] getShells() {
        return new Shell[0];
    }
}
