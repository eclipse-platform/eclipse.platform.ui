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
 * The standard abstract implementation of an <code>IExternalToolGroup</code>.
 */
public abstract class ExternalToolGroup implements IExternalToolGroup {
	/**
	 * Recommended initial size for a text field within a group.
	 */
	public static final int SIZING_TEXT_FIELD_WIDTH = 250;

	/**
	 * Dialog page this group is part of.
	 */
	private IGroupDialogPage page;
	
	/**
	 * Whether the group is working with an existing external
	 * tool, or a, yet to be created, new external tool.
	 */
	private boolean isEditMode = true;

	/**
	 * Whether the group's values are all valid.
	 */
	private boolean isValid = true;
	
	/**
	 * Creates the group.
	 */
	public ExternalToolGroup() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public final Control createContents(Composite parent, ExternalTool tool, IGroupDialogPage page) {
		this.page = page;
		this.isEditMode = tool != null;
		return createGroupContents(parent, tool);
	}

	/**
	 * Creates this group's visual components.
	 *
	 * @param parent the composite to parent the group's control
	 * @param tool the external tool to be edited, or <code>null</code> for a new tool
	 * @return the control for the group
	 */
	protected abstract Control createGroupContents(Composite parent, ExternalTool tool);
	
	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void dispose() {
		page = null;
	}

	/**
	 * Returns the dialog page this group is part of.
	 * 
	 * @return the dialog page this group is part of
	 */
	protected final IGroupDialogPage getPage() {
		return page;
	}

	/**
	 * Returns <code>true</code> if the group is editing an existing
	 * external tool, or <code>false</code> if the external tool is new
	 * and yet to be created.
	 * 
	 * @return <code>true</code> if the external tool exist already, or
	 * 		<code>false</code> if the external tool is yet to be created.
	 */
	protected final boolean isEditMode() {
		return isEditMode;
	}

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public boolean isValid() {
		return isValid;
	}

	/**
	 * Sets whether the group's values are all valid.
	 * Updates the group's page valid state. No action
	 * taken if new valid state same as current one.
	 * 
	 * @param isValid <code>true</code> if all values valid,
	 * 		<code>false</code> otherwise
	 */
	protected final void setIsValid(boolean isValid) {
		if (this.isValid != isValid) {
			this.isValid = isValid;
			this.page.updateValidState();
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public abstract void restoreValues(ExternalTool tool);
	
	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void setVisible(boolean visible) {
	}

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public abstract void updateTool(ExternalTool tool);

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public abstract void validate();
	
	
	/**
	 * Helper class to report group validation information.
	 */
	protected static final class ValidationStatus {
		public boolean isValid = true;
		public String message = null;
		public int messageType = IGroupDialogPage.NONE;
	}
}
