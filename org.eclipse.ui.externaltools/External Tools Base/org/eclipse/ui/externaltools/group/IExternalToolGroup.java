package org.eclipse.ui.externaltools.group;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.externaltools.model.ExternalTool;

/**
 * Represents the API for a group of controls to edit
 * a number of attributes of an external tool.
 * <p>
 * This interface is not intended to be extended
 * nor implemented by clients. Clients should subclass
 * <code>ExternalToolGroup</code> instead.
 * </p>
 */
public interface IExternalToolGroup {
	
	/**
	 * Creates this group's visual components.
	 *
	 * @param parent the composite to parent the group's control
	 * @param tool the external tool to be edited, or <code>null</code> for a new tool
	 * @param page the dialog page this group will be part of
	 * @return the control for the group
	 */
	public Control createContents(Composite parent, ExternalTool tool, IGroupDialogPage page);
	
	/**
	 * Disposed of the group's resources. This can be called
	 * even if <code>createContents</code> was never called.
	 */
	public void dispose();
	
	/**
	 * Returns whether the group's components have acceptable
	 * values.
	 * 
	 * @return <code>true</code> if all values acceptable, or <code>false</code> otherwise
	 */
	public boolean isValid();
	
	/**
	 * Restores the group's components values from the
	 * specified external tool.
	 * 
	 * @param tool the external tool to retrieve values from
	 */
	public void restoreValues(ExternalTool tool);
	
	/**
	 * Updates the external tool with the information
	 * collected by the group's visual components.
	 * 
	 * @param tool the external tool to update
	 */
	public void updateTool(ExternalTool tool);
	
	/**
	 * Validates this group's current values entered by the
	 * user and updates it's valid state if needed
	 */
	public void validate();
}
