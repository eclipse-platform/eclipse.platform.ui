package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.resources.CVSEntryLineTag;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.syncinfo.*;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * This configuration page explains to the user that CVS/ directories already exists and
 * it will attach the selected project to the repository that is specified in the CVS/ files.
 * 
 * This is useful for people who have checked out a project using command-line tools.
 */
public class ConfigurationWizardAutoconnectPage extends CVSWizardPage {
	private boolean validate = true;
	private Properties properties;
	
	public ConfigurationWizardAutoconnectPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 2);
		setControl(composite);
		
		Label description = new Label(composite, SWT.WRAP);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.widthHint = 350;
		description.setLayoutData(data);
		description.setText(Policy.bind("ConfigurationWizardAutoconnectPage.description"));
		
		if (properties == null) return;
		
		// Spacer
		createLabel(composite, "");
		createLabel(composite, "");
		
		createLabel(composite, Policy.bind("ConfigurationWizardAutoconnectPage.user"));
		createLabel(composite, properties.getProperty("user"));
		createLabel(composite, Policy.bind("ConfigurationWizardAutoconnectPage.host"));
		createLabel(composite, properties.getProperty("host"));
		createLabel(composite, Policy.bind("ConfigurationWizardAutoconnectPage.port"));
		String port = properties.getProperty("port");
		if (port == null) {
			createLabel(composite, "ConfigurationWizardAutoconnectPage.default");
		} else {
			createLabel(composite, port);
		}
		createLabel(composite, Policy.bind("ConfigurationWizardAutoconnectPage.connectionType"));
		createLabel(composite, properties.getProperty("connection"));
		createLabel(composite, Policy.bind("ConfigurationWizardAutoconnectPage.repositoryPath"));
		createLabel(composite, properties.getProperty("root"));
		createLabel(composite, Policy.bind("ConfigurationWizardAutoconnectPage.module"));
		createLabel(composite, properties.getProperty("module"));
		
		// Spacer
		createLabel(composite, "");
		createLabel(composite, "");
		
		final Button check = new Button(composite, SWT.CHECK);
		data = new GridData();
		data.horizontalSpan = 2;
		check.setText(Policy.bind("ConfigurationWizardAutoconnectPage.validate"));
		check.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				validate = check.getSelection();
			}
		});
		check.setSelection(true);		
	}
	
	public Properties getProperties() {
		return properties;
	}
	public boolean getValidate() {
		return validate;
	}
	public void setProject(IProject project) {
		try {
			ICVSFolder folder = (ICVSFolder)Session.getManagedResource(project);
			FolderSyncInfo info = folder.getFolderSyncInfo();
			if (info == null) {
				// This should never happen
				ErrorDialog.openError(getContainer().getShell(), Policy.bind("ConfigurationWizardAutoconnectPage.noSyncInfo"), Policy.bind("ConfigurationWizardAutoconnectPage.noCVSDirectory"), null);
				return;
			}
			ICVSRepositoryLocation location = CVSProviderPlugin.getProvider().getRepository(info.getRoot());
			properties = new Properties();
			properties.setProperty("connection", location.getMethod().getName());
			properties.setProperty("host", location.getHost());
			int port = location.getPort();
			if (port != location.USE_DEFAULT_PORT) {
				properties.setProperty("port", "" + port);
			}
			properties.setProperty("user", location.getUsername());
			properties.setProperty("root", location.getRootDirectory());
			
			String repository = info.getRepository();
			properties.setProperty("module", repository);

			CVSEntryLineTag tag = info.getTag();
			if (tag != null) {
				properties.setProperty("tag", tag.getName());
			}
		} catch (TeamException e) {
			ErrorDialog.openError(getContainer().getShell(), null, null, e.getStatus());
		}
	}
}
