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
package org.eclipse.debug.ui.variables;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Represents the API for a client extending one of the
 * variable extension points to provide visual editing
 * of the variable.
 * <p>
 * This interface is not to be extended by clients. Clients
 * may implement this interface.
 * </p>
 */
public interface IVariableComponent {
	/**
	 * Returns the control to edit the variable
	 * value, otherwise <code>null</code> if no editing
	 * supported or if <code>createContents</code> has
	 * not been called yet
	 * 
	 * @return the main control for the variable component
	 * 		or <code>null</code> if none
	 */
	public Control getControl();
	
	/**
	 * Creates the control to edit the variable. Does nothing
	 * if no editing supported.
	 * 
	 * @param parent the composite to parent all controls to
	 * @param varTag the variable tag name to create the controls for
	 * @param page the dialog page this visual component will be part of
	 */
	public void createContents(Composite parent, String varTag, IGroupDialogPage page);

	/**
	 * Returns the variable value as specified by
	 * the user thru the visual component.
	 * 
	 * @return the variable value as indicated by the visual component
	 */
	public String getVariableValue();

	/**
	 * Returns whether the variable's visual component has an
	 * acceptable value.
	 * 
	 * @return <code>true</code> if all value acceptable, or <code>false</code> otherwise
	 */
	public boolean isValid();

	/**
	 * Sets the visual component to represent the
	 * given variable value.
	 * 
	 * @param varValue the variable value the visual component should indicate
	 */
	public void setVariableValue(String varValue);

	/**
	 * Validates visual component current values entered by the
	 * user and updates it's valid state if needed
	 */
	public void validate();
	
	/**
	 * Notifies this variable component that it has
	 * been disposed. Marks the end of this component's lifecycle,
	 * allowing this component to perform any cleanup required.
	 */
	public void dispose();
}