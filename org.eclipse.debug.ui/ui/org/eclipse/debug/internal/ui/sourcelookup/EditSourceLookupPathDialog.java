/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The standalone dialog for editing the source lookup path.
 * 
 * @since 3.0
 */
public class EditSourceLookupPathDialog extends TitleAreaDialog {
	
	private SourceContainerLookupPanel fPanel;
	//The locator associated with the launch. Will be used to manage the containers.
	private ISourceLookupDirector fLocator;
	
	/**
	 * The constructor for the dialog.
	 * @param shell the shell
	 * @param locator the locator associated with the launch
	 */
	public EditSourceLookupPathDialog(Shell shell, ISourceLookupDirector locator)
	{
		super(shell);					
		fLocator = locator;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		// create a composite with standard margins and spacing
		setTitle(SourceLookupUIMessages.getString("manageSourceDialog.description")); //$NON-NLS-1$
		setTitleImage(DebugPluginImages.getImage(IDebugUIConstants.IMG_EDIT_SRC_LOC_WIZ));
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.marginHeight =
			convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth =
			convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing =
			convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing =
			convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);			
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());
		fPanel = new SourceContainerLookupPanel();
		fPanel.createControl(composite);
		fPanel.initializeFrom(fLocator);
		
		Dialog.applyDialogFont(composite);
		WorkbenchHelp.setHelp(getShell(),  IDebugHelpContextIds.EDIT_SOURCELOOKUP_DIALOG);
		
		return composite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		fPanel.performApply(null);		
		super.okPressed();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */	
	protected void configureShell(Shell shell)
	{
		super.configureShell(shell);
		shell.setText(SourceLookupUIMessages.getString("manageSourceDialog.title")); //$NON-NLS-1$
	}
	
}
