package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class ModuleSelectionPage extends CVSWizardPage {
	Button useProjectNameButton;
	Button useSpecifiedNameButton;
	Text text;
	
	String result;
	boolean useProjectName = true;
	
	public ModuleSelectionPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 2);
		// set F1 help
		// WorkbenchHelp.setHelp(composite, new DialogPageContextComputer (this, ITeamHelpContextIds.REPO_CONNECTION_MAIN_PAGE));
		
		useProjectNameButton = createRadioButton(composite, Policy.bind("ModuleSelectionPage.moduleIsProject"), 2); //$NON-NLS-1$
		useSpecifiedNameButton = createRadioButton(composite, Policy.bind("ModuleSelectionPage.specifyModule"), 1); //$NON-NLS-1$
		useProjectNameButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				useProjectName = useProjectNameButton.getSelection();
				if (useProjectName) {
					text.setEnabled(false);
					result = null;
					setPageComplete(true);
				} else {
					text.setEnabled(true);
					result = text.getText();
					if (result.length() == 0) {
						result = null;
						setPageComplete(false);
					} else {
						setPageComplete(true);
					}
				}
			}
		});

		text = createTextField(composite);
		text.setEnabled(false);
		text.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				result = text.getText();
				if (result.length() == 0) {
					result = null;
					setPageComplete(false);
				} else {
					setPageComplete(true);
				}
			}
		});
		useSpecifiedNameButton.setSelection(false);
		useProjectNameButton.setSelection(true);
		setControl(composite);
		setPageComplete(true);
	}	
	public String getModuleName() {
		return result;
	}
	public boolean useProjectName() {
		return useProjectName;
	}
}
