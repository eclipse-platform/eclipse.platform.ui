package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class BranchWizardBranchPage extends CVSWizardPage {
	boolean update;
	String branchTag;

	Text branchText;
	
	public BranchWizardBranchPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 2);
		
		Label label = new Label(composite, SWT.WRAP);
		label.setText(Policy.bind("BranchWizardBranchPage.description"));
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.widthHint = 350;
		label.setLayoutData(data);
		
		createLabel(composite, "");
		createLabel(composite, "");
		
		createLabel(composite, Policy.bind("BranchWizardBranchPage.branchName"));
		branchText = createTextField(composite);
		branchText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				branchTag = branchText.getText();
				updateEnablement();
			}
		});

		createLabel(composite, "");
		createLabel(composite, "");

		final Button check = new Button(composite, SWT.CHECK);
		data = new GridData();
		data.horizontalSpan = 2;
		check.setLayoutData(data);
		check.setText(Policy.bind("BranchWizardBranchPage.startWorking"));
		check.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				update = check.getSelection();
			}
		});
		check.setSelection(true);
		update = true;
			
		setControl(composite);
		updateEnablement();
	}
	
	public String getBranchTag() {
		return branchTag;
	}
	
	public boolean getUpdate() {
		return update;
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
		setPageComplete(true);
	}
}