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
package org.eclipse.ui.tests.performance.layout;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.WorkbenchException;

/**
 * @since 3.1
 */
public abstract class TestWidgetFactory {
	public Point getMaxSize() {return new Point(800, 600);};
	public void init() throws CoreException, WorkbenchException {};
	public void done() throws CoreException, WorkbenchException {};
	public abstract String getName();
	public abstract Composite getControl() throws CoreException, WorkbenchException;
}
