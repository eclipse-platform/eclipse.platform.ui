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
import org.eclipse.update.internal.ui.UpdateUIPlugin;

public class ReviewPage extends WizardPage {
// NL keys
private static final String KEY_TITLE = "InstallWizard.ReviewPage.title";
private static final String KEY_DESC = "InstallWizard.ReviewPage.desc";
private static final String KEY_ABOUT = "InstallWizard.ReviewPage.about";
private static final String KEY_NAME = "InstallWizard.ReviewPage.name";
private static final String KEY_PROVIDER = "InstallWizard.ReviewPage.provider";
private static final String KEY_VERSION = "InstallWizard.ReviewPage.version";
private static final String KEY_CORRECT = "InstallWizard.ReviewPage.correct"; 

	private ChecklistJob job;
	/**
	 * Constructor for ReviewPage
	 */
	public ReviewPage(ChecklistJob job) {
		super("Review");
		setTitle(UpdateUIPlugin.getResourceString(KEY_TITLE));
		setDescription(UpdateUIPlugin.getResourceString(KEY_DESC));
		this.job = job;
	}
	
	

	/**
	 * @see DialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		client.setLayout(layout);
		Label label = new Label(client, SWT.NULL);
		label.setText(UpdateUIPlugin.getResourceString(KEY_ABOUT));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		
		label = new Label(client, SWT.NULL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		
		label = new Label(client, SWT.NULL);
		label.setText(UpdateUIPlugin.getResourceString(KEY_NAME));
		
		label = new Label(client, SWT.NULL);
		label.setFont(JFaceResources.getBannerFont());
		label.setText(job.getFeature().getLabel());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);
		label = new Label(client, SWT.NULL);
		label.setText(UpdateUIPlugin.getResourceString(KEY_PROVIDER));
		label = new Label(client, SWT.NULL);
		label.setFont(JFaceResources.getBannerFont());
		label.setText(job.getFeature().getProvider());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);
		label = new Label(client, SWT.NULL);
		label.setText(UpdateUIPlugin.getResourceString(KEY_VERSION));
		label = new Label(client, SWT.NULL);
		label.setFont(JFaceResources.getBannerFont());
		label.setText(job.getFeature().getVersionIdentifier().getVersion().toString());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);

		label = new Label(client, SWT.NULL);
		label.setText(UpdateUIPlugin.getResourceString(KEY_CORRECT));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		setControl(client);
	}
}