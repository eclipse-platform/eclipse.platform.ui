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
package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.ui.merge.ProjectElement;
import org.eclipse.team.internal.ccvs.ui.repo.RepositorySorter;
import org.eclipse.team.internal.ccvs.ui.wizards.CVSWizardPage;
import org.eclipse.team.internal.ui.dialogs.DetailsDialog;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class BranchPromptDialog extends DetailsDialog {

	private ICVSFolder folder;
	private String branchTag = ""; //$NON-NLS-1$
	private String versionTag= ""; //$NON-NLS-1$
	private String versionName= "";		 //$NON-NLS-1$

	private boolean allStickyResources;	
	private boolean update;
		
	private Text versionText;
	private Text branchText;
	
	private static final int TABLE_HEIGHT_HINT = 150;
	
	// widgets;
	private TreeViewer tagTree;
	
	public BranchPromptDialog(Shell parentShell, String title, ICVSFolder folder, boolean allResourcesSticky, String versionName) {
		super(parentShell, title);
		this.folder = folder;
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
			GridData.GRAB_VERTICAL |
			GridData.HORIZONTAL_ALIGN_FILL |
			GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);;
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
		data = new GridData();
		data.horizontalSpan = 2;
		data.widthHint = 350;
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
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label = new Label(composite, SWT.WRAP);
		label.setText(Policy.bind("BranchWizardPage.existingVersionsAndBranches")); //$NON-NLS-1$
		GridData data = new GridData(
			GridData.GRAB_HORIZONTAL |
			GridData.GRAB_VERTICAL |
			GridData.HORIZONTAL_ALIGN_FILL |
			GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);;
		label.setLayoutData(data);
		
		tagTree = createTree(composite);
		tagTree.setInput(new ProjectElement(folder, ProjectElement.INCLUDE_BRANCHES | ProjectElement.INCLUDE_VERSIONS));
		Runnable refresh = new Runnable() {
			public void run() {
				getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						tagTree.refresh();
					}
				});
			}
		};
		TagConfigurationDialog.createTagDefinitionButtons(getShell(), composite, new ICVSFolder[] {folder}, 
														  convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT), 
														  convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH),
														  refresh, refresh);
		Dialog.applyDialogFont(parent);
		return composite;
	}
	
	/**
	 * Creates the existing branch and version tree viewer in the details pane
	 */
	protected TreeViewer createTree(Composite parent) {
		Tree tree = new Tree(parent, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);		
		data.heightHint = TABLE_HEIGHT_HINT;
		tree.setLayoutData(data);	
		TreeViewer result = new TreeViewer(tree);
		result.setContentProvider(new WorkbenchContentProvider());
		result.setLabelProvider(new WorkbenchLabelProvider());
		result.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {			
			}
		});
		result.setSorter(new RepositorySorter());
		return result;
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
}
