/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Presents a preview of a <code>ChangeElement</code>
 */
public interface IChangePreviewViewer {

	/**
	 * Creates the preview viewer's widget hierarchy. This method 
	 * should only be called once. Method <code>getControl()</code>
	 * should be use retrieve the widget hierarchy.
	 * 
	 * @param parent the parent for the widget hierarchy
	 * 
	 * @see #getControl()
	 */
	public void createControl(Composite parent);
	
	/**
	 * Returns the preview viewer's SWT control.
	 * 
	 * @return the preview viewer's SWT control
	 */
	public Control getControl();	
	
	/**
	 * Sets the preview viewer's input element.
	 * 
	 * @param input the input element
	 */
	public void setInput(Object input) throws CoreException;
	
	/**
	 * Refreshes the preview viewer.
	 */
	public void refresh();	
}

