package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * Wizard page which determines what action to perform after a project
 * is configured for use with a CVS provider. The user may specify:
 * 
 * - Open the synchronize view (default)
 * - Run CVS import followed by checkout (for projects that don't exist remotely)
 * - Run update
 * - Do nothing
 * 
 * In addition, the user may select whether the connection should be validated
 * on creation (default is yes).
 */
public class ConfigurationWizardPostPage extends CVSWizardPage {
	private boolean validate = true;
	private int result = ConfigurationWizard.DO_SYNCHRONIZE;
	
	/**
	 * ConfigurationWizardPostPage constructor.
	 * 
	 * @param pageName  the name of the page
	 * @param title  the title of the page
	 * @param titleImage  the image for the page
	 */
	public ConfigurationWizardPostPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		createLabel(composite, Policy.bind("After_configuring_the_project__1")); //$NON-NLS-1$
		Button radio = createRadio(composite, Policy.bind("Open_the_Synchronize_view_on_the_project_2")); //$NON-NLS-1$
		radio.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				result = ConfigurationWizard.DO_SYNCHRONIZE;
			}
		});
		radio.setSelection(true);
		radio = createRadio(composite, Policy.bind("Automatically_import_the_local_resources_into_the_repository_3")); //$NON-NLS-1$
		radio.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				result = ConfigurationWizard.DO_IMPORT_CHECKOUT;
			}
		});
		radio = createRadio(composite, Policy.bind("Run_update_4")); //$NON-NLS-1$
		radio.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				result = ConfigurationWizard.DO_UPDATE;
			}
		});
		radio = createRadio(composite, Policy.bind("Do_nothing_5")); //$NON-NLS-1$
		radio.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				result = ConfigurationWizard.DO_NOTHING;
			}
		});
	
		// Spacer
		createLabel(composite, ""); //$NON-NLS-1$
		
		final Button check = new Button(composite, SWT.CHECK);
		check.setText(Policy.bind("Validate_Connection_on_Finish_7")); //$NON-NLS-1$
		check.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				validate = check.getSelection();
			}
		});
		check.setSelection(true);
		setControl(composite);
	}
	private Button createRadio(Composite parent, String text) {
		Button result = new Button(parent, SWT.RADIO);
		result.setText(text);
		return result;
	}
	
	/**
	 * Return which of the four options should be performed after configuration
	 */
	public int getPostOperation() {
		return result;
	}
	public boolean getValidate() {
		return validate;
	}
}
