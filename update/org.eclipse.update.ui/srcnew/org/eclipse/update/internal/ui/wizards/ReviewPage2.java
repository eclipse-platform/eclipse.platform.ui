/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;

public class ReviewPage2 extends BannerPage2 {
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

	private PendingOperation job;
	/**
	 * Constructor for ReviewPage2
	 */
	public ReviewPage2(PendingOperation job) {
		super("Review");
		setTitle(UpdateUI.getString(KEY_TITLE));
		setDescription(UpdateUI.getString(KEY_DESC));
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
			case PendingOperation.UNINSTALL :
				label.setText(UpdateUI.getString(KEY_ABOUT_UNINSTALL));				
				break;
			case PendingOperation.INSTALL :
				label.setText(UpdateUI.getString(KEY_ABOUT_INSTALL));				
				break;	
			case PendingOperation.UNCONFIGURE:
				label.setText(UpdateUI.getString(KEY_ABOUT_UNCONFIGURE));		
				break;
			case PendingOperation.CONFIGURE:
				label.setText(UpdateUI.getString(KEY_ABOUT_CONFIGURE));		
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
		label.setText(UpdateUI.getString(KEY_NAME));
		
		label = new Label(client, SWT.NULL);
		label.setFont(JFaceResources.getBannerFont());
		label.setText(job.getFeature().getLabel());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);
		label = new Label(client, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_PROVIDER));
		label = new Label(client, SWT.NULL);
		label.setFont(JFaceResources.getBannerFont());
		label.setText(job.getFeature().getProvider());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);
		label = new Label(client, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_VERSION));
		label = new Label(client, SWT.NULL);
		label.setFont(JFaceResources.getBannerFont());
		label.setText(job.getFeature().getVersionedIdentifier().getVersion().toString());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);

		label = new Label(client, SWT.NULL);
		if (job.getJobType()==PendingOperation.INSTALL)
			label.setText(UpdateUI.getString(KEY_CORRECT_INSTALL));
		else
			label.setText(UpdateUI.getString(KEY_CORRECT_UNINSTALL));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		WorkbenchHelp.setHelp(client, "org.eclipse.update.ui.ReviewPage2");
		return client;
	}
}
