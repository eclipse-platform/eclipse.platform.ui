package org.eclipse.ui.internal.dialogs;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class ProjectCapabilitySimpleAddWizard extends Wizard {
	private ProjectCapabilitySimpleSelectionPage mainPage;
	private IWorkbench workbench;
	private IStructuredSelection selection;
	private IProject project;
	
	public ProjectCapabilitySimpleAddWizard(IWorkbench workbench, IStructuredSelection selection, IProject project) {
		super();
		this.workbench = workbench;
		this.selection = selection;
		this.project = project;
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(true);
		initializeDefaultPageImageDescriptor();
		setWindowTitle(WorkbenchMessages.getString("ProjectCapabilitySimpleSelectionPage.windowTitle")); //$NON-NLS-1$
	}
		
	/* (non-Javadoc)
	 * Method declared on IWizard
	 */
	public void addPages() {
		mainPage =
			new ProjectCapabilitySimpleSelectionPage(
				"projectCapabilitySimpleSelectionPage", //$NON-NLS-1$
				workbench,
				selection,
				project);
		mainPage.setTitle(WorkbenchMessages.getString("ProjectCapabilitySimpleSelectionPage.title")); //$NON-NLS-1$
		mainPage.setDescription(WorkbenchMessages.getString("ProjectCapabilitySimpleSelectionPage.description")); //$NON-NLS-1$
		addPage(mainPage);
	}

	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean canFinish() {
		return false;
	}

	/**
	 * Sets the image banner for the wizard
	 */
	protected void initializeDefaultPageImageDescriptor() {
		String iconPath = "icons/full/";//$NON-NLS-1$		
		try {
			URL installURL = WorkbenchPlugin.getDefault().getDescriptor().getInstallURL();
			URL url = new URL(installURL, iconPath + "wizban/newprj_wiz.gif");//$NON-NLS-1$
			ImageDescriptor desc = ImageDescriptor.createFromURL(url);
			setDefaultPageImageDescriptor(desc);
		}
		catch (MalformedURLException e) {
			// Should not happen. Ignore.
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard
	 */
	public boolean performFinish() {
		return true;
	}
}
