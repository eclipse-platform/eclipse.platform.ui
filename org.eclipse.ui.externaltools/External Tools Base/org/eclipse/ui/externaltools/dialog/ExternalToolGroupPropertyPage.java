package org.eclipse.ui.externaltools.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.externaltools.group.IExternalToolGroup;
import org.eclipse.ui.externaltools.group.IGroupDialogPage;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.ExternalToolStorage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Generic property page that will display an <code>IExternalToolGroup</code>
 * as its content.
 * <p>
 * Client can extend this class.
 * </p>
 */
public class ExternalToolGroupPropertyPage extends PropertyPage implements IGroupDialogPage {
	private IExternalToolGroup group;
	private String helpContextId;

	/**
	 * Creates a property page for the specified group.
	 * 
	 * @param group the external tool group component to display
	 * @param helpContextId the help context id for this page
	 */
	protected ExternalToolGroupPropertyPage(IExternalToolGroup group, String helpContextId) {
		super();
		this.group = group;
		this.helpContextId = helpContextId;
	}
	
	 /*
	  * (non-Javadoc)
	  * Method declared on IGroupDialogPage.
	  */
	 public int convertHeightHint(int chars) {
	 	return convertHeightInCharsToPixels(chars);	
	 }

	/* (non-Javadoc)
	 * Method declared on PreferencePage.
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		if (helpContextId != null)
			WorkbenchHelp.setHelp(composite, helpContextId);

		if (group != null) {
			initializeDialogUnits(parent);
			group.createContents(composite, getExternalTool(), this);
		}
		
		return composite;
	}

	/* (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void dispose() {
		super.dispose();
		if (group != null)
			group.dispose();
	}

	/**
	 * Returns the external tool using the property
	 * page element, or <code>null</code> if not possible.
	 */
	protected ExternalTool getExternalTool() {
		IAdaptable element = getElement();
		if (element instanceof ExternalTool)
			return (ExternalTool)element;
		return (ExternalTool) element.getAdapter(ExternalTool.class);
	}
	
	/**
	 * Returns the external tool group component.
	 */
	public IExternalToolGroup getGroup() {
		return group;
	}

	/* (non-Javadoc)
	 * Method declared on PreferencePage.
	 */
	protected void performDefaults() {
		if (group != null) {
			ExternalTool tool = getExternalTool();
			if (tool != null)
				group.restoreValues(tool);
		}
		
		super.performDefaults();
	}

	/* (non-Javadoc)
	 * Method declared on IPreferencePage.
	 */
	public boolean performOk() {
		if (group != null) {
			if (!group.isValid())
				return false;
			ExternalTool tool = getExternalTool();
			if (tool != null) {
				group.updateTool(tool);
				return ExternalToolStorage.saveTool(tool, getShell());
			}
		}				

		return super.performOk();
	}

	/* (non-Javadoc)
	 * Method declared on IGroupDialogPage.
	 */
	public GridData setButtonGridData(Button button) {
		return setButtonLayoutData(button);
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchPropertyPage.
	 */
	public void setElement(IAdaptable element) {
		super.setElement(element);
		if (group != null) {
			ExternalTool tool = getExternalTool();
			if (tool != null)
				group.restoreValues(tool);
		}
	}

	/* (non-Javadoc)
	 * Method declared on IGroupDialogPage.
	 */
	public void updateValidState() {
		if (group != null)
			setValid(group.isValid());
		else
			setValid(true);
	}
}
