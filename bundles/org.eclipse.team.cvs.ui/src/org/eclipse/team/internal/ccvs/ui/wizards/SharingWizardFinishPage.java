package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.help.WorkbenchHelp;

public class SharingWizardFinishPage extends CVSWizardPage {
	public SharingWizardFinishPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		// set F1 help
		WorkbenchHelp.setHelp(composite, IHelpContextIds.SHARING_FINISH_PAGE);
		Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
		label.setText(Policy.bind("SharingWizardFinishPage.message")); //$NON-NLS-1$
		GridData data = new GridData();
		data.widthHint = 350;
		label.setLayoutData(data);
		setControl(composite);
	}
}
