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
 * Viewer to present the the context object of a {@linkplain org.eclipse.ltk.core.refactoring.RefactoringStatusEntry
 * refactoring status entry}. It is guaranteed that the methods <code>setInput</code> 
 * and <code>getControl</code> are called after <code>createControl</code> has been called.
 * <p>
 * Status context viewers are associated with a context object via the extension point <code>
 * org.eclipse.ltk.ui.refactoring.statusContextViewers</code>. Implementors of this
 * extension point must therefore implement this interface.
 * </p>
 * <p>
 * To ensure visual consistency across all provided context viewers the widget
 * hierarchy provided through the method {@link #createControl(Composite)} has to
 * use a {@link org.eclipse.swt.custom.ViewForm} as its root widget.
 * </p>
 * 
 * @since 3.0
 */
public interface IStatusContextViewer {
	
	/**
	 * Creates the status viewer's widget hierarchy. This method 
	 * is only called once. Method <code>getControl()</code> should 
	 * be used to retrieve the widget hierarchy.
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

