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

public class BranchWizardVersionPage extends CVSWizardPage {
	String versionTag;
	boolean doVersion;
	
	Text versionText;
	
	public BranchWizardVersionPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 2);
		
		Label label = new Label(composite, SWT.WRAP);
		label.setText(Policy.bind("BranchWizardVersionPage.description"));
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.widthHint = 350;
		label.setLayoutData(data);
		
		createLabel(composite, "");
		createLabel(composite, "");
		
		final Button specifyVersion = new Button(composite, SWT.CHECK);
		data = new GridData();
		data.horizontalSpan = 2;
		specifyVersion.setLayoutData(data);
		specifyVersion.setText(Policy.bind("BranchWizardVersionPage.specifyVersion"));
		specifyVersion.setSelection(true);
		doVersion = true;
		
		createLabel(composite, Policy.bind("BranchWizardVersionPage.versionName"));
		versionText = createTextField(composite);
		versionText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				versionTag = versionText.getText();
				updateEnablement();
			}
		});
		specifyVersion.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				doVersion = specifyVersion.getSelection();
				versionText.setEnabled(doVersion);
				updateEnablement();
			}
		});
			
		setControl(composite);
		updateEnablement();
	}

	public String getVersionTag() {
		if (doVersion) {
			return versionTag;
		} else {
			return null;
		}
	}
	
	private void updateEnablement() {
		if (doVersion) {
			IStatus status = CVSTag.validateTagName(versionText.getText());
			if (status.isOK()) {
				setErrorMessage(null);
			} else {
				setErrorMessage(status.getMessage());
				setPageComplete(false);
				return;
			}
		} else {
			setErrorMessage(null);
		}
		setPageComplete(true);
	}
}
