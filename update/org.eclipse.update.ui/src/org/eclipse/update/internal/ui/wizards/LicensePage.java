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
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.parts.SWTUtil;

public class LicensePage extends WizardPage {
	private static final String KEY_TITLE = "InstallWizard.LicensePage.title";
	private static final String KEY_DESC = "InstallWizard.LicensePage.desc";
	private static final String KEY_ACCEPT = "InstallWizard.LicensePage.accept";
	private static final String KEY_DECLINE =
		"InstallWizard.LicensePage.decline";

	private PendingChange job;
	/**
	 * Constructor for ReviewPage
	 */
	public LicensePage(PendingChange job) {
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
		Text text =
			new Text(
				client,
				SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		GridData gd = new GridData(GridData.FILL_BOTH);
		text.setLayoutData(gd);
		text.setText(job.getFeature().getLicense().getAnnotation());

		Composite buttonContainer = new Composite(client, SWT.NULL);
		layout = new GridLayout();
		gd = new GridData(GridData.FILL_HORIZONTAL);
		buttonContainer.setLayout(layout);
		buttonContainer.setLayoutData(gd);

		final Button acceptButton = new Button(buttonContainer, SWT.RADIO);
		acceptButton.setText(UpdateUIPlugin.getResourceString(KEY_ACCEPT));
		acceptButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setPageComplete(acceptButton.getSelection());
			}
		});
		Button declineButton = new Button(buttonContainer, SWT.RADIO);
		declineButton.setText(UpdateUIPlugin.getResourceString(KEY_DECLINE));
		declineButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setPageComplete(acceptButton.getSelection());
			}
		});
		setControl(client);
	}
}