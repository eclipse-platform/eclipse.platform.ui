/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.repo;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
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
    
    // The initial size of this wizard.
    private final static int INITIAL_WIDTH = 300;
    private final static int INITIAL_HEIGHT = 350;
    
	private Dialog parentDialog;
	private ICVSRepositoryLocation root;
	private ICVSRemoteResource[] rootFolders;
	private RefreshRemoteProjectSelectionPage projectSelectionPage;
	private IDialogSettings settings;
	
	public static boolean execute(Shell shell, final ICVSRepositoryLocation root) {
		final ICVSRemoteResource[][] rootFolders = new ICVSRemoteResource[1][0];
		rootFolders[0] = null;
		try {
			new ProgressMonitorDialog(shell).run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						rootFolders[0] = CVSUIPlugin.getPlugin().getRepositoryManager().getFoldersForTag(root, CVSTag.DEFAULT, monitor);
					} catch (CVSException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InvocationTargetException e) {
			CVSUIPlugin.openError(shell, null, null, e);
			return false;
		} catch (InterruptedException e) {
			return false;
		}
		RefreshRemoteProjectWizard wizard = new RefreshRemoteProjectWizard(root, rootFolders[0]);
		WizardDialog dialog = new WizardDialog(shell, wizard);
		/**
		 * This is the only place where a size hint > 0 is required. The wizard
		 * page should in general have hints of 0 (and grab excessive space).
		 */
		dialog.setMinimumPageSize(INITIAL_WIDTH, INITIAL_HEIGHT);
		wizard.setParentDialog(dialog);
		return (dialog.open() == Window.OK);
	}
	
	public RefreshRemoteProjectWizard(ICVSRepositoryLocation root, ICVSRemoteResource[] rootFolders) {
		this.root = root;
		this.rootFolders = rootFolders;
		IDialogSettings workbenchSettings = CVSUIPlugin.getPlugin().getDialogSettings();
		this.settings = workbenchSettings.getSection("RefreshRemoteProjectWizard");//$NON-NLS-1$
		if (settings == null) {
			this.settings = workbenchSettings.addNewSection("RefreshRemoteProjectWizard");//$NON-NLS-1$
		}
		setWindowTitle(Policy.bind("RefreshRemoteProjectWizard.title")); //$NON-NLS-1$
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
			parentDialog, settings, root, rootFolders);
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
					final RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
					// Run in the manager to avoid multiple repo view updates
					manager.run(new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException,
								InterruptedException {
							monitor.beginTask(null, 100 * selectedFolders.length);
							try {
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
					}, monitor);
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
