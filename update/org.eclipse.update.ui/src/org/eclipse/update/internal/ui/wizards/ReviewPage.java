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
import org.eclipse.update.internal.ui.UpdateUIPlugin;

public class ReviewPage extends BannerPage {
// NL keys
private static final String KEY_TITLE = "InstallWizard.ReviewPage.title";
private static final String KEY_DESC = "InstallWizard.ReviewPage.desc";
private static final String KEY_ABOUT_INSTALL = "InstallWizard.ReviewPage.about.install";
private static final String KEY_ABOUT_UNINSTALL = "InstallWizard.ReviewPage.about.uninstall";
private static final String KEY_ABOUT_UNCONFIGURE = "InstallWizard.ReviewPage.about.unconfigure";
private static final String KEY_ABOUT_CONFIGURE = "InstallWizard.ReviewPage.about.configure";
private static final String KEY_NAME = "InstallWizard.ReviewPage.name";
private static final String KEY_PROVIDER = "InstallWizard.ReviewPage.provider";
private static final String KEY_VERSION = "InstallWizard.ReviewPage.version";
private static final String KEY_CORRECT_INSTALL = "InstallWizard.ReviewPage.correct.install"; 
private static final String KEY_CORRECT_UNINSTALL = "InstallWizard.ReviewPage.correct.uninstall";

	private PendingChange job;
	/**
	 * Constructor for ReviewPage
	 */
	public ReviewPage(PendingChange job) {
		super("Review");
		setTitle(UpdateUIPlugin.getResourceString(KEY_TITLE));
		setDescription(UpdateUIPlugin.getResourceString(KEY_DESC));
		this.job = job;
	}
	
	

	/**
	 * @see DialogPage#createControl(Composite)
	 */
	public Control createContents(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = layout.marginHeight = 0;
		client.setLayout(layout);
		Label label = new Label(client, SWT.NULL);
		switch (job.getJobType()) {
			case PendingChange.UNINSTALL :
				label.setText(UpdateUIPlugin.getResourceString(KEY_ABOUT_UNINSTALL));				
				break;
			case PendingChange.INSTALL :
				label.setText(UpdateUIPlugin.getResourceString(KEY_ABOUT_INSTALL));				
				break;	
			case PendingChange.UNCONFIGURE:
				label.setText(UpdateUIPlugin.getResourceString(KEY_ABOUT_UNCONFIGURE));		
				break;
			case PendingChange.CONFIGURE:
				label.setText(UpdateUIPlugin.getResourceString(KEY_ABOUT_CONFIGURE));		
				break;
		}
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
		label.setText(job.getFeature().getVersionedIdentifier().getVersion().toString());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);

		label = new Label(client, SWT.NULL);
		if (job.getJobType()==PendingChange.INSTALL)
			label.setText(UpdateUIPlugin.getResourceString(KEY_CORRECT_INSTALL));
		else
			label.setText(UpdateUIPlugin.getResourceString(KEY_CORRECT_UNINSTALL));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		return client;
	}
}