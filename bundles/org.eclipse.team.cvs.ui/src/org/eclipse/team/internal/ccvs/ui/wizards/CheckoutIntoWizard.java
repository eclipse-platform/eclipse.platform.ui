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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.IPromptCondition;
import org.eclipse.team.internal.ui.PromptingDialog;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Wizard that performs the "Checkout Into" operation
 */
public class CheckoutIntoWizard extends Wizard {

	CheckoutIntoProjectSelectionPage projectSelectionPage;
	ICVSRemoteFolder remoteFolder;
	
	/**
	 * Constructor for CheckoutIntoWizard.
	 */
	public CheckoutIntoWizard(ICVSRemoteFolder remoteFolder) {
		super();
		this.remoteFolder = remoteFolder;
		setWindowTitle(Policy.bind("CheckoutIntoWizard.title")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		setNeedsProgressMonitor(true);
		ImageDescriptor substImage = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_CHECKOUT);
		
		projectSelectionPage = new CheckoutIntoProjectSelectionPage(
			"ProjectSelectionPage", //$NON-NLS-1$
			Policy.bind("CheckoutIntoWizard.projectSelectionPageTitle"), //$NON-NLS-1$
			substImage,
			Policy.bind("CheckoutIntoWizard.projectSelectionPageDescription")); //$NON-NLS-1$
		projectSelectionPage.setRemoteFolder(remoteFolder);
		addPage(projectSelectionPage);
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		try {
			final IContainer targetFolder = projectSelectionPage.getLocalFolder();
			if (targetFolder == null) return false;
			PromptingDialog prompt = new PromptingDialog(getShell(), new IResource[] { targetFolder },
				getOverwriteLocalAndFileSystemPrompt(), Policy.bind("CheckoutIntoWizard.confirmOverwrite"));//$NON-NLS-1$
			if (prompt.promptForMultiple().length == 0) return false;
			getContainer().run(true, true, new WorkspaceModifyOperation() {
				public void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(null, 100);
					try {
						validateUniqueMapping(remoteFolder.getFolderSyncInfo(), targetFolder, Policy.subMonitorFor(monitor, 10));
						CVSWorkspaceRoot.checkout(remoteFolder, targetFolder, true /* keep CVSness */, projectSelectionPage.isRecurse(), Policy.subMonitorFor(monitor, 90));
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InvocationTargetException e) {
			handle(e); 
			return false;
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}
	
	private void handle(Throwable e) {
		CVSUIPlugin.openError(getShell(), Policy.bind("CheckoutIntoWizard.error"), null, e); //$NON-NLS-1$
	}

	protected IPromptCondition getOverwriteLocalAndFileSystemPrompt() {
		return new IPromptCondition() {
			// prompt if resource in workspace exists or exists in local file system
			public boolean needsPrompt(IResource resource) {
				if(resource.exists()) {
					return true;
				}
				return false;
			}
			public String promptMessage(IResource resource) {
				return Policy.bind("CheckoutIntoWizard.thisResourceExists", resource.getFullPath().toString());//$NON-NLS-1$
			}
		};
	}
	
	/*
	 * Ensure that there is no equivalent mapping alreay in the project
	 */
	private void validateUniqueMapping(
		final FolderSyncInfo folderSyncInfo,
		final IContainer targetFolder,
		IProgressMonitor iProgressMonitor) throws CVSException {
		
		final IProject iProject = targetFolder.getProject();
		ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(iProject);
		cvsFolder.accept(new ICVSResourceVisitor() {
			public void visitFile(ICVSFile file) throws CVSException {
				// do nothing
			}
			public void visitFolder(ICVSFolder folder) throws CVSException {
				if (!folder.isCVSFolder()) return;
				IResource resource = folder.getIResource();
				if (resource == null || resource.equals(targetFolder)) return;
				FolderSyncInfo info = folder.getFolderSyncInfo();
				if (info.getRoot().equals(folderSyncInfo.getRoot()) 
					&& info.getRepository().equals(folderSyncInfo.getRepository())) {
						throw new CVSException(Policy.bind("CheckoutIntoWizard.,mappingAlredyExists",  //$NON-NLS-1$
						 new Object[] { folderSyncInfo.getRepository(), iProject.getName(), 
						 	targetFolder.getProjectRelativePath().toString(), 
						 	resource.getProjectRelativePath().toString() }));
				}
				folder.acceptChildren(this);
			}
		});
	}
}
