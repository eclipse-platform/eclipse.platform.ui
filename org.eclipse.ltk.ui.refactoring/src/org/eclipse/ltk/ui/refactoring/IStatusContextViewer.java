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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;

/**
 * A special viewer to present the context of a <code>RefactoringStatusEntry</code>.
 * The life cycle of a status context viewer is as follows: first <code>createControl
 * </code> is called, followed by an arbitray number of calls to <code>getControl</code>
 * and <code>setInput</code>.
 * <p>
 * A status viewer is responsible to present a corresponding label for the viewer.
 * To achieve a consistent layout it is recommened to surround the SWT control
 * actually presenting the status inside a <code>ViewForm</code>.
 * 
 * TODO some words about the extension point this interface is connected to.
 * 
 * @see org.eclipse.swt.custom.ViewForm
 * 
 * @since 3.0
 */
public interface IStatusContextViewer {
	
	/**
	 * Creates the status viewer's widget hierarchy. This method 
	 * should only be called once. Method <code>getControl()</code>
	 * should be used to retrieve the widget hierarchy.
	 * 
	 * @param parent the parent for the widget hierarchy
	 * 
	 * @see #getControl()
	 */
	public void createControl(Composite parent);
	
	/**
	 * Returns the status context viewer's SWT control.
	 * 
	 * @return the status context viewer's SWT control
	 */
	public Control getControl();	
	
	/**
	 * Sets the status context viewer's input element.
	 * 
	 * @param input the input element
	 */
	public void setInput(RefactoringStatusContext input);	
}

