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
package org.eclipse.team.internal.ccvs.ui.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.CheckoutIntoOperation;
import org.eclipse.team.internal.ccvs.ui.operations.CheckoutMultipleProjectsOperation;
import org.eclipse.team.internal.ccvs.ui.operations.CheckoutSingleProjectOperation;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.NewProjectAction;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CheckoutAsWizard extends Wizard {
	
	private ICVSRemoteFolder[] remoteFolders;
	private boolean allowProjectConfiguration;

	private CheckoutAsMainPage mainPage;
	private CheckoutAsProjectSelectionPage projectSelectionPage;
	private CheckoutAsLocationSelectionPage locationSelectionPage;

	class NewProjectListener implements IResourceChangeListener {
		private IProject newProject = null;
		/**
		 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta root = event.getDelta();
			IResourceDelta[] projectDeltas = root.getAffectedChildren();
			for (int i = 0; i < projectDeltas.length; i++) {							
				IResourceDelta delta = projectDeltas[i];
				IResource resource = delta.getResource();
				if (delta.getKind() == IResourceDelta.ADDED) {
					newProject = (IProject)resource;
				}
			}
		}
		/**
		 * Gets the newProject.
		 * @return Returns a IProject
		 */
		public IProject getNewProject() {
			return newProject;
		}
	}
	
	public CheckoutAsWizard(ICVSRemoteFolder[] remoteFolders, boolean allowProjectConfiguration) {
		super();
		this.remoteFolders = remoteFolders;
		setWindowTitle(Policy.bind("CheckoutAsWizard.title")); //$NON-NLS-1$
		this.allowProjectConfiguration = allowProjectConfiguration;
	}
	
	/**
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		setNeedsProgressMonitor(true);
		ImageDescriptor substImage = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_CHECKOUT);
		
		mainPage = new CheckoutAsMainPage(substImage, remoteFolders, allowProjectConfiguration);
		addPage(mainPage);
		
		projectSelectionPage = new CheckoutAsProjectSelectionPage(substImage, remoteFolders);
		addPage(projectSelectionPage);
		
		locationSelectionPage = new CheckoutAsLocationSelectionPage(substImage, remoteFolders);
		addPage(locationSelectionPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		try {
			if (mainPage.isPerformConfigure()) {
				return performConfigureAndCheckout();
			} else if (mainPage.isPerformCheckoutAs()) {
				if (isSingleFolder()) {
					return performSingleCheckoutAs();
				} else {
					return performMultipleCheckoutAs();
				}
			} else if (mainPage.isPerformCheckoutInto()) {
				return performCheckoutInto();
			}
		} catch (CVSException e) {
			handle(e);
			// drop through
		} catch (InterruptedException e) {
			// drop through
		}
		return false;
	}

	/**
	 * @return
	 */
	private boolean isSingleFolder() {
		return remoteFolders.length == 1;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#canFinish()
	 */
	public boolean canFinish() {
		return (mainPage.isPageComplete() 
		&& (mainPage.isPerformConfigure()
			|| (mainPage.isPerformCheckoutInto() && projectSelectionPage.isPageComplete()) 
			|| (mainPage.isPerformCheckoutAs() && locationSelectionPage.isPageComplete())));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == mainPage) {
			if (mainPage.isPerformConfigure()) return null;
			if (mainPage.isPerformCheckoutInto()) return projectSelectionPage;
			if (mainPage.isPerformCheckoutAs()) {
				if (isSingleFolder()) {
					locationSelectionPage.setProjectName(mainPage.getProjectName());
				} else {
					locationSelectionPage.setProject(null);
				}
				return locationSelectionPage; 
			} 
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (page == mainPage) return null;
		return mainPage;
	}
	
	private void handle(Throwable e) {
		CVSUIPlugin.openError(getShell(), Policy.bind("CheckoutAsWizard.error"), null, e); //$NON-NLS-1$
	}
	
	/*
	 * Configure a local project and checkout the selected remote folder into the project.
	 * This only occurs for single folders.
	 */
	private boolean performConfigureAndCheckout() throws CVSException, InterruptedException {
		IProject newProject = getNewProject();
		if (newProject == null) return false;
		// Run the checkout in the background
		new CheckoutSingleProjectOperation(getShell(), remoteFolders[0], newProject, null, true).run();
		return true;
	}
	
	private boolean performSingleCheckoutAs() throws CVSException, InterruptedException {
		IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(mainPage.getProjectName());
		String targetLocation = locationSelectionPage.getTargetLocation();
		// Run the checkout in the background
		new CheckoutSingleProjectOperation(getShell(), remoteFolders[0], newProject, targetLocation, false).run();
		return true;
	}

	/**
	 * Check out multiple folders to the workspace using a custom location if one is
	 * specified.
	 */
	private boolean performMultipleCheckoutAs() throws CVSException, InterruptedException {
		String targetLocation = locationSelectionPage.getTargetLocation();
		// Run the checkout in the background
		new CheckoutMultipleProjectsOperation(getShell(), remoteFolders, targetLocation).run();
		return true;
	}

	private boolean performCheckoutInto() throws CVSException, InterruptedException {
		CheckoutIntoOperation operation;
		boolean recursive = projectSelectionPage.isRecurse();
		if (isSingleFolder()) {
			operation = new CheckoutIntoOperation(getShell(), remoteFolders[0] , projectSelectionPage.getLocalFolder(), recursive);
		} else {
			operation = new CheckoutIntoOperation(getShell(), remoteFolders, projectSelectionPage.getParentFolder(), recursive);
		}
		// Run the checkout in the background
		operation.run();
		return true;
	}

	/**
	 * Get a new project that is configured by the new project wizard.
	 * This is currently the only way to do this.
	 */
	private IProject getNewProject() {
		NewProjectListener listener = new NewProjectListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
		(new NewProjectAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow())).run();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		IProject project = listener.getNewProject();
		return project;
	}

}
