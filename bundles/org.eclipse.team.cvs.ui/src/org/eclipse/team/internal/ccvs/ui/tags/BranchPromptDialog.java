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
package org.eclipse.team.internal.ccvs.ui.tags;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.wizards.CVSWizardPage;
import org.eclipse.team.internal.ui.dialogs.DetailsDialog;
import org.eclipse.ui.help.WorkbenchHelp;

public class BranchPromptDialog extends DetailsDialog {

	private String branchTag = ""; //$NON-NLS-1$
	private String versionTag= ""; //$NON-NLS-1$
	private String versionName= "";		 //$NON-NLS-1$

	private boolean allStickyResources;	
	private boolean update;
		
	private Text versionText;
	private Text branchText;
	
	private static final int TAG_AREA_HEIGHT_HINT = 200;
	
	// widgets;
    private TagSource tagSource;
    private TagSelectionArea tagArea;
    private final IResource[] resources;
	
	public BranchPromptDialog(Shell parentShell, String title, IResource[] resources, boolean allResourcesSticky, String versionName) {
		super(parentShell, title);
        this.resources = resources;
		this.tagSource = TagSource.create(resources);
		this.allStickyResources = allResourcesSticky;
		this.versionName = versionName;
	}	

	/**
	 * @see DetailsDialog#createMainDialogArea(Composite)
	 */
	protected void createMainDialogArea(Composite composite) {
		// create message
		Label label = new Label(composite, SWT.WRAP);
		String message;
		if(allStickyResources) {
			message = Policy.bind("BranchWizardPage.pageDescriptionVersion"); //$NON-NLS-1$
		} else {
			message = Policy.bind("BranchWizardPage.pageDescription"); //$NON-NLS-1$
		}
		label.setText(message);
		GridData data = new GridData(
			GridData.GRAB_HORIZONTAL |
			GridData.HORIZONTAL_ALIGN_FILL |
			GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		label.setLayoutData(data);
		
		CVSWizardPage.createLabel(composite, Policy.bind("BranchWizardPage.branchName")); //$NON-NLS-1$
		branchText = CVSWizardPage.createTextField(composite);
		branchText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				branchTag = branchText.getText();				
				updateEnablements();
				updateVersionName(branchTag);
			}
		});
		addBranchContentAssist();

		final Button check = new Button(composite, SWT.CHECK);
		data = new GridData();
		data.horizontalSpan = 2;
		check.setLayoutData(data);
		check.setText(Policy.bind("BranchWizardPage.startWorking")); //$NON-NLS-1$
		check.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				update = check.getSelection();
			}
		});
		check.setSelection(true);		
		update = true;
		
		label = new Label(composite, SWT.WRAP);
		label.setText(Policy.bind("BranchWizardPage.specifyVersion")); //$NON-NLS-1$
		data = new GridData(
				GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL |
				GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
			
		CVSWizardPage.createLabel(composite, Policy.bind("BranchWizardPage.versionName")); //$NON-NLS-1$
		versionText = CVSWizardPage.createTextField(composite);
		versionText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				versionTag = versionText.getText();
				updateEnablements();
			}
		});
		
		if(allStickyResources) {
			versionText.setEditable(false);
			versionText.setText(versionName);
		}

		// F1 Help
		WorkbenchHelp.setHelp(composite, IHelpContextIds.BRANCH_DIALOG);
		Dialog.applyDialogFont(composite);
		branchText.setFocus();
	}

    private void addBranchContentAssist() {
        TagSource projectTagSource = LocalProjectTagSource.create(getSeedProject());
        if (projectTagSource != null)
            TagContentAssistProcessor.createContentAssistant(branchText, projectTagSource, TagSelectionArea.INCLUDE_BRANCHES); 
    }

    private IProject getSeedProject() {
        return resources[0].getProject();
    }

    /**
	 * Updates version name
	 */
	protected void updateVersionName(String branchName) {
		if(versionText!=null && !allStickyResources) {
			versionText.setText(Policy.bind("BranchWizardPage.versionPrefix") + branchName); //$NON-NLS-1$
		}
	}
	
	/**
	 * @see DetailsDialog#createDropDownDialogArea(Composite)
	 */
	protected Composite createDropDownDialogArea(Composite parent) {
		
		// create a composite with standard margins and spacing
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = TAG_AREA_HEIGHT_HINT;
		composite.setLayoutData(gridData);
		
		tagArea = new TagSelectionArea(getShell(), tagSource, TagSelectionArea.INCLUDE_VERSIONS | TagSelectionArea.INCLUDE_BRANCHES, null);
		tagArea.setTagAreaLabel(Policy.bind("BranchWizardPage.existingVersionsAndBranches")); //$NON-NLS-1$
		tagArea.setIncludeFilterInputArea(false);
		tagArea.createArea(composite);

		return composite;
	}
	
	/**
	 * Validates branch and version names
	 */
	protected void updateEnablements() {
		String message = null;
		
		if (branchTag.length() == 0) {
			message = ""; //$NON-NLS-1$
		} else {
			IStatus status = CVSTag.validateTagName(branchTag);
			if (!status.isOK()) {
				message = Policy.bind("BranchWizard.branchNameWarning", status.getMessage()); //$NON-NLS-1$
			} else {
				if(versionText!=null) {
					status = CVSTag.validateTagName(versionText.getText());
					if (!status.isOK()) {
						message = Policy.bind("BranchWizard.versionNameWarning", status.getMessage()); //$NON-NLS-1$
					} else {
						if(versionTag.length() != 0 && versionTag.equals(branchTag)) {
							message = Policy.bind("BranchWizard.branchAndVersionMustBeDifferent"); //$NON-NLS-1$
						}
					}
				}
			}
		}
		setPageComplete(message == null);
		setErrorMessage(message);
	}
	
	/**
	 * Returns the branch tag name
	 */
	public String getBranchTagName() {
		return branchTag;
	}
	
	/**
	 * Returns the version tag name
	 */
	public String getVersionTagName() {
		return versionTag;
	}
	
	/**
	 * Returns the state of the update checkbox
	 */
	public boolean getUpdate() {
		return update;
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#isMainGrabVertical()
     */
    protected boolean isMainGrabVertical() {
        return false;
    }

}
