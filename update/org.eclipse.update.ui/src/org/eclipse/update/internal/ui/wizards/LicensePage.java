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

public class LicensePage extends WizardPage {
	private ChecklistJob job;
	/**
	 * Constructor for ReviewPage
	 */
	public LicensePage(ChecklistJob job) {
		super("License");
		setTitle("Feature License");
		setDescription("This feature has a license agreement that you need to accept before proceeding with the installation.");
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
		
		final Button button = new Button(client, SWT.PUSH);
		button.setText("&Accept");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				button.setEnabled(false);
				setPageComplete(true);
			}
		});
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		button.setLayoutData(gd);
		setControl(client);
	}
}