package org.eclipse.ui.externaltools.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.externaltools.group.IExternalToolGroup;
import org.eclipse.ui.externaltools.group.IGroupDialogPage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Generic wizard page that will display an <code>IExternalToolGroup</code>
 * as its content. Client is responsible to set the page's title, 
 * description, and image descriptor.
 * <p>
 * This class can be used as is by clients.
 * </p>
 */
public class ExternalToolGroupWizardPage extends WizardPage implements IGroupDialogPage {
	private IExternalToolGroup group;
	private String helpContextId;

	/**
	 * Creates a wizard page for the specified group.
	 * 
	 * @param pageName name given to the page
	 * @param group the external tool group component to display
	 */
	public ExternalToolGroupWizardPage(String pageName, IExternalToolGroup group, String helpContextId) {
		super(pageName);
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
	 * Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		if (group != null && getControl() == null) {
			initializeDialogUnits(parent);
			Composite composite = new Composite(parent, SWT.NULL);
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			if (helpContextId != null)
				WorkbenchHelp.setHelp(composite, helpContextId);
			group.createContents(composite, null, this);
			setControl(composite);
		}
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
	 * Returns the external tool group component.
	 */
	public IExternalToolGroup getGroup() {
		return group;
	}

	/* (non-Javadoc)
	 * Method declared on IGroupDialogPage.
	 */
	public GridData setButtonGridData(Button button) {
		return setButtonLayoutData(button);
	}

	/* (non-Javadoc)
	 * Method declared on IGroupDialogPage.
	 */
	public void updateValidState() {
		if (group != null)
			setPageComplete(group.isValid());
		else
			setPageComplete(true);
	}
}
