package org.eclipse.update.internal.ui.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import java.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.update.ui.internal.model.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.core.runtime.CoreException;

public class ReviewPage extends WizardPage {
	private ChecklistJob job;
	/**
	 * Constructor for ReviewPage
	 */
	public ReviewPage(ChecklistJob job) {
		super("Review");
		setTitle("Feature Install");
		setDescription("This wizard helps you install new or update existing features of your product.");
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
		label.setText("You are about to install the following feature:");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		
		label = new Label(client, SWT.NULL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		
		label = new Label(client, SWT.NULL);
		label.setText("Name:");
		
		label = new Label(client, SWT.NULL);
		label.setFont(JFaceResources.getBannerFont());
		label.setText(job.getFeature().getLabel());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);
		label = new Label(client, SWT.NULL);
		label.setText("Provider:");
		label = new Label(client, SWT.NULL);
		label.setFont(JFaceResources.getBannerFont());
		label.setText(job.getFeature().getProvider());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);
		label = new Label(client, SWT.NULL);
		label.setText("Version:");
		label = new Label(client, SWT.NULL);
		label.setFont(JFaceResources.getBannerFont());
		label.setText(job.getFeature().getIdentifier().getVersion().toString());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);

		label = new Label(client, SWT.NULL);
		label.setText("\nIf that is correct, select Next to continue.");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		setControl(client);
	}
}