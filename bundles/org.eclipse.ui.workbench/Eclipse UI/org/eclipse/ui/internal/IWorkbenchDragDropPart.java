/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.window.Window;

import org.eclipse.ui.IWorkbenchWindow;

/**
 * IWorkbenchDragDropPart is the interface or behaviour that is common
 * between drag and drop parts.
 */
public interface IWorkbenchDragDropPart {
	
	public static final int UNDEFINED = 0;
	public static final int VIEW = 1;
	public static final int EDITOR = 2;
	
	/**
	 * Get the part control.  
	 * @return Control or <code>null</code>.
	 */
	public Control getControl();
	
	/**
	 * Get the workbench window for the receiver.
	 * @return IWorkbenchWindow
	 */
	public IWorkbenchWindow getWorkbenchWindow();
	
	/**
	 * Gets the parent for this part.
	 * @return ILayoutContainer
	 */
	public ILayoutContainer getContainer();
	
	/**
	 * Return the LayoutPart for the receiver.
	 * @return LayoutPart
	 */
	public LayoutPart getPart();
	
	/**
	 * Return the window that contains the receiver.
	 * @return
	 */
	public Window getWindow();

}
