package org.eclipse.update.internal.ui.wizards;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import java.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.update.ui.internal.model.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

public class LicensePage extends WizardPage {
private static final String KEY_TITLE = "InstallWizard.LicensePage.title";
private static final String KEY_DESC = "InstallWizard.LicensePage.desc";
private static final String KEY_ACCEPT = "InstallWizard.LicensePage.accept";
private static final String KEY_DECLINE = "InstallWizard.LicensePage.decline";

	private ChecklistJob job;
	/**
	 * Constructor for ReviewPage
	 */
	public LicensePage(ChecklistJob job) {
		super("License");
		setTitle(UpdateUIPlugin.getResourceString(KEY_TITLE));
		setDescription(UpdateUIPlugin.getResourceString(KEY_DESC));
		this.job = job;
		setPageComplete(false);
	}

	/**
	 * @see DialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		client.setLayout(layout);
		Text text = new Text(client, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		
		GridData gd = new GridData(GridData.FILL_BOTH);
		text.setLayoutData(gd);
		text.setText(job.getFeature().getLicense().getText());
		
		Composite buttonContainer = new Composite(client, SWT.NULL);
		layout = new GridLayout();
		layout.numColumns = 2;
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		buttonContainer.setLayout(layout);
		buttonContainer.setLayoutData(gd);
		
		final Button button1 = new Button(buttonContainer, SWT.PUSH);
		button1.setText(UpdateUIPlugin.getResourceString(KEY_ACCEPT));
		button1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				button1.setEnabled(false);
				setPageComplete(true);
			}
		});
		final Button button2 = new Button(buttonContainer, SWT.PUSH);
		button2.setText(UpdateUIPlugin.getResourceString(KEY_DECLINE));
		button2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				InstallWizardDialog dialog = (InstallWizardDialog)getWizard().getContainer();
				dialog.cancel();
			}
		});
		setControl(client);
	}
}