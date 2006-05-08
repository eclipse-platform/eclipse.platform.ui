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
package org.eclipse.ui.tests.performance.layout;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.WorkbenchException;

/**
 * @since 3.1
 */
public abstract class TestWidgetFactory {
	public Point getMaxSize() throws CoreException, WorkbenchException {
	    Composite control = getControl();
	    Composite parent = control.getParent();
	    
	    if (parent == null) {
	        return new Point(800, 600);
	    }
	    return Geometry.getSize(parent.getClientArea());
	}
	public void init() throws CoreException, WorkbenchException {}
	public void done() throws CoreException, WorkbenchException {}
	public abstract String getName();
	public abstract Composite getControl() throws CoreException, WorkbenchException;
}
