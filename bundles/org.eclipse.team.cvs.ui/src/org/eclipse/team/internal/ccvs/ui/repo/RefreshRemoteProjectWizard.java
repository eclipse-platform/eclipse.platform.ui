/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.repo;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * Wizard for refreshing the tags for a CVS repository location
 */
public class RefreshRemoteProjectWizard extends Wizard {
	
	private Dialog parentDialog;
	private ICVSRepositoryLocation root;
	private RefreshRemoteProjectSelectionPage projectSelectionPage;
	
	public static boolean execute(Shell shell, ICVSRepositoryLocation root) {
		RefreshRemoteProjectWizard wizard = new RefreshRemoteProjectWizard(root);
		WizardDialog dialog = new WizardDialog(shell, wizard);
		wizard.setParentDialog(dialog);
		return (dialog.open() == Window.OK);
	}
	
	public RefreshRemoteProjectWizard(ICVSRepositoryLocation root) {
		this.root = root;
	}
	
	/**
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		setNeedsProgressMonitor(true);
		ImageDescriptor substImage = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_NEW_LOCATION);
		projectSelectionPage = new RefreshRemoteProjectSelectionPage(
			"ProjectSelectionPage", //$NON-NLS-1$
			Policy.bind("RefreshRemoteProjectSelectionPage.pageTitle"), //$NON-NLS-1$
			substImage,
			Policy.bind("RefreshRemoteProjectSelectionPage.pageDescription"), //$NON-NLS-1$
			parentDialog, root);
		addPage(projectSelectionPage);
	}
	
	/**
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		final ICVSRemoteResource[] selectedFolders = projectSelectionPage.getSelectedRemoteProject();
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(null, 100 * selectedFolders.length);
					try {
						RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
						for (int i = 0; i < selectedFolders.length; i++) {
							ICVSRemoteResource resource = selectedFolders[i];
							if (resource instanceof ICVSFolder) {
								manager.refreshDefinedTags((ICVSFolder)resource, true /* replace */, true, Policy.subMonitorFor(monitor, 100));
							}
						}
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
			return true;
		} catch (InvocationTargetException e) {
			CVSUIPlugin.openError(getShell(), null, null ,e);
		} catch (InterruptedException e) {
		}
		return false;
	}

	/**
	 * Sets the parentDialog.
	 * @param parentDialog The parentDialog to set
	 */
	public void setParentDialog(Dialog parentDialog) {
		this.parentDialog = parentDialog;
	}

}
