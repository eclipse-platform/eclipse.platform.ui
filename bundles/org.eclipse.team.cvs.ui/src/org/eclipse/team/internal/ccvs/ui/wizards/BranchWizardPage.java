package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class BranchWizardPage extends CVSWizardPage {
	boolean update;
	
	String branchTag;
	String versionTag;
	boolean allStickyResources;
	
	Text versionText;
	Text branchText;
	
	public BranchWizardPage(String pageName,String title,  boolean allStickyResources, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		if(allStickyResources) {
			setDescription(Policy.bind("BranchWizardPage.pageDescriptionVersion"));
		} else {
			setDescription(Policy.bind("BranchWizardPage.pageDescription"));
		}
		this.allStickyResources = allStickyResources;
	}
	
	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 2);
		Label label;
		GridData data;
		
		createLabel(composite, Policy.bind("BranchWizardPage.branchName"));
		branchText = createTextField(composite);
		branchText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				branchTag = branchText.getText();				
				updateEnablement();
				updateVersionName(branchTag);
			}
		});

		createLabel(composite, "");
		createLabel(composite, "");

		final Button check = new Button(composite, SWT.CHECK);
		data = new GridData();
		data.horizontalSpan = 2;
		check.setLayoutData(data);
		check.setText(Policy.bind("BranchWizardPage.startWorking"));
		check.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				update = check.getSelection();
			}
		});
		check.setSelection(true);		
		update = true;
		
		if(!allStickyResources) {			
			createLabel(composite, "");
			createLabel(composite, "");
			
			label = new Label(composite, SWT.WRAP);
			label.setText(Policy.bind("BranchWizardPage.specifyVersion"));
			data = new GridData();
			data.horizontalSpan = 2;
			data.widthHint = 350;
			label.setLayoutData(data);
			
			createLabel(composite, Policy.bind("BranchWizardPage.versionName"));
			versionText = createTextField(composite);
			versionText.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event event) {
					versionTag = versionText.getText();
					updateEnablement();
				}
			});
		}

		branchText.setFocus();
		setControl(composite);
		updateEnablement();
	}

	public String getBranchTag() {
		return branchTag;
	}

	public boolean getUpdate() {
		return update;
	}

	public String getVersionTag() {
		return versionTag;
	}
	
	private void updateVersionName(String branchName) {
		if(versionText!=null) {
			versionText.setText(Policy.bind("BranchWizardPage.versionPrefix") + branchName);
		}
	}
	
	private void updateEnablement() {
		String branch = branchText.getText();
		if (branch.length() == 0) {
			setErrorMessage(null);
			setPageComplete(false);
			return;
		}
		IStatus status = CVSTag.validateTagName(branch);
		if (status.isOK()) {
			setErrorMessage(null);
		} else {
			setErrorMessage(status.getMessage());
			setPageComplete(false);
			return;
		}
		
		if(versionText!=null) {
			status = CVSTag.validateTagName(versionText.getText());
			if (status.isOK()) {
				setErrorMessage(null);
			} else {
				setErrorMessage(status.getMessage());
				setPageComplete(false);
				return;
			}
		}
		setPageComplete(true);
	}
}