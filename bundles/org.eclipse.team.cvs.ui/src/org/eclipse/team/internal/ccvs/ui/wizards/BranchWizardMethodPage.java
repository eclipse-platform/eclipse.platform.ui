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

public class BranchWizardMethodPage extends CVSWizardPage {
	boolean eclipseWay = true;
	
	public BranchWizardMethodPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 2);
		
		Label label = new Label(composite, SWT.WRAP);
		label.setText(Policy.bind("BranchWizardMethodPage.description")); //$NON-NLS-1$
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.widthHint = 350;
		label.setLayoutData(data);
		
		createLabel(composite, "");
		createLabel(composite, "");

		Button eclipseWay = new Button(composite, SWT.RADIO);
		eclipseWay.setText(Policy.bind("BranchWizardMethodPage.eclipseWay")); //$NON-NLS-1$
		eclipseWay.setSelection(this.eclipseWay);
		eclipseWay.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				BranchWizardMethodPage.this.eclipseWay = true;
			}
		});
		data = new GridData();
		data.horizontalSpan = 2;
		eclipseWay.setLayoutData(data);
		
		Composite eclipseDescriptionComposite = createDescriptionComposite(composite, 2);
		Label eclipseDescription = new Label(eclipseDescriptionComposite, SWT.WRAP);
		eclipseDescription.setText(Policy.bind("BranchWizardMethodPage.eclipseDescription")); //$NON-NLS-1$
		data = new GridData();
		data.widthHint = 350;
		eclipseDescription.setLayoutData(data);
		
		Button cvsWay = new Button(composite, SWT.RADIO);
		cvsWay.setText(Policy.bind("BranchWizardMethodPage.cvsWay")); //$NON-NLS-1$
		cvsWay.setSelection( ! this.eclipseWay);
		cvsWay.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				BranchWizardMethodPage.this.eclipseWay = false;
			}
		});
		data = new GridData();
		data.horizontalSpan = 2;
		cvsWay.setLayoutData(data);
		
		Composite cvsDescriptionComposite = createDescriptionComposite(composite, 2);
		Label cvsDescription = new Label(cvsDescriptionComposite, SWT.WRAP);
		cvsDescription.setText(Policy.bind("BranchWizardMethodPage.cvsDescription")); //$NON-NLS-1$
		data = new GridData();
		data.widthHint = 350;
		cvsDescription.setLayoutData(data);
		
		setControl(composite);
		updateEnablement();
	}

	public boolean getEclipseWay() {
		return eclipseWay;
	}

	private void updateEnablement() {
		setPageComplete(true);
	}
	
	private Composite createDescriptionComposite(Composite parent, int parentColumns) {
		Composite composite = new Composite(parent, SWT.NULL);
	
		// GridLayout
		GridLayout layout = new GridLayout();
		layout.marginWidth = 10;
		layout.marginHeight = 0;
		composite.setLayout(layout);
	
		// GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = parentColumns;
		composite.setLayoutData(data);
		return composite;
	}
}