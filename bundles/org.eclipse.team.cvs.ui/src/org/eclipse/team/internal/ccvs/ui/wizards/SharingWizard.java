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


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.TagSelectionDialog;
import org.eclipse.team.ui.IConfigurationWizard;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.sync.ISynchronizeView;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;

/**
 * This wizard helps the user to import a new project in their workspace
 * into a CVS repository for the first time.
 */
public class SharingWizard extends Wizard implements IConfigurationWizard {
	// The project to configure
	private IProject project;

	// The autoconnect page is used if CVS/ directories already exist.
	private ConfigurationWizardAutoconnectPage autoconnectPage;
	
	// The import page is used if CVS/ directories do not exist.
	private RepositorySelectionPage locationPage;
	
	// The page that prompts the user for connection information.
	private ConfigurationWizardMainPage createLocationPage;
	
	// The page that prompts the user for module name.
	private ModuleSelectionPage modulePage;
	
	// The page that tells the user what's going to happen.
	private SharingWizardFinishPage finishPage;
	
	public SharingWizard() {
		IDialogSettings workbenchSettings = CVSUIPlugin.getPlugin().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("NewLocationWizard");//$NON-NLS-1$
		if (section == null) {
			section = workbenchSettings.addNewSection("NewLocationWizard");//$NON-NLS-1$
		}
		setDialogSettings(section);
		setNeedsProgressMonitor(true);
		setWindowTitle(Policy.bind("SharingWizard.title")); //$NON-NLS-1$
	}	
		
	public void addPages() {
		ImageDescriptor sharingImage = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_SHARE);
		if (doesCVSDirectoryExist()) {
			autoconnectPage = new ConfigurationWizardAutoconnectPage("autoconnectPage", Policy.bind("SharingWizard.autoConnectTitle"), sharingImage); //$NON-NLS-1$ //$NON-NLS-2$
			autoconnectPage.setProject(project);
			autoconnectPage.setDescription(Policy.bind("SharingWizard.autoConnectTitleDescription")); //$NON-NLS-1$
			addPage(autoconnectPage);
		} else {
			FolderSyncInfo info = getRepositoryInfoFromOneO(project);
			if (info != null) {
				// The project is from 1.0 and has sharing info
				autoconnectPage = new ConfigurationWizardAutoconnectPage("autoconnectPage", Policy.bind("SharingWizard.autoConnectOneOTitle"), sharingImage); //$NON-NLS-1$ //$NON-NLS-2$
				autoconnectPage.setSharing(info);
				autoconnectPage.setDescription(Policy.bind("SharingWizard.autoConnectTitleDescription")); //$NON-NLS-1$
				addPage(autoconnectPage);
			} else {
				ICVSRepositoryLocation[] locations = CVSUIPlugin.getPlugin().getRepositoryManager().getKnownRepositoryLocations();
				if (locations.length > 0) {
					locationPage = new RepositorySelectionPage("importPage", Policy.bind("SharingWizard.importTitle"), sharingImage); //$NON-NLS-1$ //$NON-NLS-2$
					locationPage.setDescription(Policy.bind("SharingWizard.importTitleDescription")); //$NON-NLS-1$
					addPage(locationPage);
				}
				createLocationPage = new ConfigurationWizardMainPage("createLocationPage", Policy.bind("SharingWizard.enterInformation"), sharingImage); //$NON-NLS-1$ //$NON-NLS-2$
				createLocationPage.setDescription(Policy.bind("SharingWizard.enterInformationDescription")); //$NON-NLS-1$
				addPage(createLocationPage);
				createLocationPage.setDialogSettings(getDialogSettings());
				modulePage = new ModuleSelectionPage("modulePage", Policy.bind("SharingWizard.enterModuleName"), sharingImage); //$NON-NLS-1$ //$NON-NLS-2$
				modulePage.setDescription(Policy.bind("SharingWizard.enterModuleNameDescription")); //$NON-NLS-1$
				addPage(modulePage);
				finishPage = new SharingWizardFinishPage("finishPage", Policy.bind("SharingWizard.readyToFinish"), sharingImage); //$NON-NLS-1$ //$NON-NLS-2$
				finishPage.setDescription(Policy.bind("SharingWizard.readyToFinishDescription")); //$NON-NLS-1$
				addPage(finishPage);
			}
		}
	}
	public boolean canFinish() {
		IWizardPage page = getContainer().getCurrentPage();
		if (page == locationPage) {
			if (locationPage.getLocation() == null) {
				return createLocationPage.isPageComplete();
			} else {
				return modulePage.useProjectName() || modulePage.getModuleName() != null;
			}
		} else if (page == modulePage) {
			return modulePage.useProjectName() || modulePage.getModuleName() != null;
		} else if (page == finishPage) {
			return true;
		}
		return super.canFinish();
	}
	protected String getMainPageDescription() {
		return Policy.bind("SharingWizard.description"); //$NON-NLS-1$
	}
	protected String getMainPageTitle() {
		return Policy.bind("SharingWizard.heading"); //$NON-NLS-1$
	}
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == autoconnectPage) return null;
		if (page == locationPage) {
			if (locationPage.getLocation() == null) {
				return createLocationPage;
			} else {
				return modulePage;
			}
		}
		if (page == createLocationPage) {
			return modulePage;
		}
		if (page == modulePage) {
			return finishPage;
		}
		return null;
	}
	/*
	 * @see IWizard#performFinish
	 */
	public boolean performFinish() {
		final boolean[] result = new boolean[] { true };
		try {
			final boolean[] doSync = new boolean[] { false };
			final boolean[] projectExists = new boolean[] { false };
			getContainer().run(true /* fork */, true /* cancel */, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						monitor.beginTask("", 100); //$NON-NLS-1$
						if (autoconnectPage != null && doesCVSDirectoryExist()) {
							// Autoconnect to the repository using CVS/ directories
							
							FolderSyncInfo info = autoconnectPage.getFolderSyncInfo();
							if (info == null) {
								// Error!
								return;
							}
							
							// Get the repository location (the get will add the locatin to the provider)
							boolean isPreviouslyKnown = CVSProviderPlugin.getPlugin().isKnownRepository(info.getRoot());
							ICVSRepositoryLocation location = CVSProviderPlugin.getPlugin().getRepository(info.getRoot());
	
							// Validate the connection if the user wants to
							boolean validate = autoconnectPage.getValidate();					
							if (validate) {
								// Do the validation
								try {
									location.validateConnection(new SubProgressMonitor(monitor, 50));
								} catch (final TeamException e) {
									// Exception validating. We can continue if the user wishes.
									final boolean[] keep = new boolean[] { false };
									getShell().getDisplay().syncExec(new Runnable() {
										public void run() {
											keep[0] = MessageDialog.openQuestion(getContainer().getShell(),
												Policy.bind("SharingWizard.validationFailedTitle"), //$NON-NLS-1$
												Policy.bind("SharingWizard.validationFailedText", new Object[] {e.getStatus().getMessage()})); //$NON-NLS-1$
										}
									});
									if (!keep[0]) {
										// Remove the root
										try {
											if (!isPreviouslyKnown) {
												CVSProviderPlugin.getPlugin().disposeRepository(location);
											}
										} catch (TeamException e1) {
											CVSUIPlugin.openError(getContainer().getShell(), Policy.bind("exception"), null, e1, CVSUIPlugin.PERFORM_SYNC_EXEC); //$NON-NLS-1$
										}
										result[0] = false;
										return;
									}
									// They want to keep the connection anyway. Fall through.
								}
							}
							
							// Set the sharing
							CVSWorkspaceRoot.setSharing(project, info, new SubProgressMonitor(monitor, 50));
						} else {
							// Import
							doSync[0] = true;
							// Check if the directory exists on the server
							ICVSRepositoryLocation location = null;
							boolean isKnown = false;
							try {
								location = getLocation();
								isKnown = CVSProviderPlugin.getPlugin().isKnownRepository(location.getLocation());
								location.validateConnection(monitor);
								// Purge any CVS folders that may exists in subfolders
								purgeAnyCVSFolders();
								String moduleName = getModuleName();
								ICVSRemoteFolder folder = location.getRemoteFolder(moduleName, null);
								if (folder.exists(new SubProgressMonitor(monitor, 50))) {
									projectExists[0] = true;
									final boolean[] sync = new boolean[] {true};
									if (autoconnectPage == null) {
										getShell().getDisplay().syncExec(new Runnable() {
											public void run() {
												sync[0] = MessageDialog.openQuestion(getShell(), Policy.bind("SharingWizard.couldNotImport"), Policy.bind("SharingWizard.couldNotImportLong", getModuleName())); //$NON-NLS-1$ //$NON-NLS-2$
											}
										});
									}
									result[0] = sync[0];
									doSync[0] = sync[0];
									return;
								}
							} catch (TeamException e) {
								CVSUIPlugin.openError(getShell(), null, null, e, CVSUIPlugin.PERFORM_SYNC_EXEC);
								if (!isKnown && location != null) location.flushUserInfo();
								result[0] = false;
								doSync[0] = false;
								return;
							}
							// Add the location to the provider if it is new
							if (!isKnown) {
								CVSProviderPlugin.getPlugin().addRepository(location);
							}
							
							// Create the remote module for the project
							CVSWorkspaceRoot.createModule(location, project, getModuleName(), new SubProgressMonitor(monitor, 50));
						}
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
			if (doSync[0]) {
				// Sync of the project
				if (projectExists[0]) {
					try {
						String moduleName = getModuleName();
						CVSTag tag;
						if (autoconnectPage == null) {
							TagSelectionDialog dialog = new TagSelectionDialog(getShell(), 
								new ICVSFolder[] {(ICVSFolder)getLocation().getRemoteFolder(moduleName, null)}, 
								Policy.bind("SharingWizard.selectTagTitle"),  //$NON-NLS-1$
								Policy.bind("SharingWizard.selectTag"), //$NON-NLS-1$
								TagSelectionDialog.INCLUDE_HEAD_TAG | TagSelectionDialog.INCLUDE_BRANCHES, 
								false, /*don't show recurse option*/
								IHelpContextIds.SHARE_WITH_EXISTING_TAG_SELETION_DIALOG);
							dialog.setBlockOnOpen(true);
							if (dialog.open() == Dialog.CANCEL) {
								return false;
							}
							tag = dialog.getResult();
						} else {
							tag = autoconnectPage.getSharing().getTag();
						}
						mapProject(moduleName, tag);
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					}
				}
				ISynchronizeView view = TeamUI.showSyncViewInActivePage(null);
				if(view != null) {
					IWorkingSet workingSet = CVSUIPlugin.getWorkingSet(new IResource[] {project}, Policy.bind("SyncAction.workingSetName")); //$NON-NLS-1$)
					view.setWorkingSet(workingSet);
					view.selectSubscriber(CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber());
				} else {
					CVSUIPlugin.openError(getContainer().getShell(), Policy.bind("error"), Policy.bind("Error.unableToShowSyncView"), null);
				}
			}
		} catch (InterruptedException e) {
			return true;
		} catch (InvocationTargetException e) {
			CVSUIPlugin.openError(getContainer().getShell(), null, null, e);
		}

		return result[0];
	}

	private void mapProject(final String moduleName, final CVSTag tag) throws InvocationTargetException, InterruptedException {
		getContainer().run(true /* fork */, true /* cancel */, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					// TODO: Should do a refresh with the subscriber
					// and then transfer sync info for folders
					monitor.beginTask(null, 200);
					CVSWorkspaceRoot.getRemoteSyncTree(project, getLocation(), moduleName, tag, Policy.subMonitorFor(monitor, 100));
					CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().refresh(new IResource[] { project }, IResource.DEPTH_INFINITE, Policy.subMonitorFor(monitor, 100));
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		});
	}

	/**
	 * Return an ICVSRepositoryLocation
	 */
	private ICVSRepositoryLocation getLocation() throws TeamException {
		// If there is an autoconnect page then it has the location
		if (autoconnectPage != null) {
			return autoconnectPage.getLocation();
		}
		
		// If the import page has a location, use it.
		if (locationPage != null) {
			ICVSRepositoryLocation location = locationPage.getLocation();
			if (location != null) return location;
		}
		
		// Otherwise, get the location from the create location page
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				createLocationPage.finish(new NullProgressMonitor());
			}
		});
		Properties properties = createLocationPage.getProperties();
		ICVSRepositoryLocation location = CVSProviderPlugin.getPlugin().createRepository(properties);
		return location;
	}
	/**
	 * Return the module name.
	 */
	private String getModuleName() {
		// If there is an autoconnect page then it has the module name
		if (autoconnectPage != null) {
			return autoconnectPage.getSharing().getRepository();
		}
		String moduleName = modulePage.getModuleName();
		if (moduleName == null) moduleName = project.getName();
		return moduleName;
	}
	/*
	 * @see IConfigurationWizard#init(IWorkbench, IProject)
	 */
	public void init(IWorkbench workbench, IProject project) {
		this.project = project;
	}
	private boolean doesCVSDirectoryExist() {
		// Determine if there is an existing CVS/ directory from which configuration
		// information can be retrieved.
		Shell shell = null;
		if (getContainer() != null) {
			shell = getContainer().getShell();
		}
		final boolean[] isCVSFolder = new boolean[] { false };
		try {
			CVSUIPlugin.runWithRefresh(shell, new IResource[] { project }, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						ICVSFolder folder = (ICVSFolder)CVSWorkspaceRoot.getCVSResourceFor(project);
						FolderSyncInfo info = folder.getFolderSyncInfo();
						isCVSFolder[0] = info != null;
					} catch (final TeamException e) {
						throw new InvocationTargetException(e);
					}
				}
			}, null);
		} catch (InvocationTargetException e) {
			CVSUIPlugin.openError(shell, null, null, e);
		} catch (InterruptedException e) {
		}
		return isCVSFolder[0];
	}
	
	private FolderSyncInfo getRepositoryInfoFromOneO(IProject project) {
		try {
			QualifiedName key = new QualifiedName("org.eclipse.vcm.core", "Sharing"); //$NON-NLS-1$ //$NON-NLS-2$
			byte[] syncBytes = ResourcesPlugin.getWorkspace().getSynchronizer().getSyncInfo(key, project); //$NON-NLS-1$ //$NON-NLS-2$
			if (syncBytes != null) {
				DataInputStream reader = new DataInputStream(new ByteArrayInputStream(syncBytes));
				String repoType = reader.readUTF();
				String repoLocation = reader.readUTF();
				String stream = reader.readUTF();
				reader.close();
				ResourcesPlugin.getWorkspace().getSynchronizer().flushSyncInfo(key, project, IResource.DEPTH_INFINITE);
				if (repoType.equals("CVS")) { //$NON-NLS-1$
					// Get the repository so it is added to the provider 
					// (in case the user cancels after we purge the old info)
					CVSProviderPlugin.getPlugin().getRepository(repoLocation);
					CVSTag tag;
					if (stream.equals("HEAD")) { //$NON-NLS-1$
						tag = CVSTag.DEFAULT;
					} else {
						tag = new CVSTag(stream, CVSTag.BRANCH);
					}
					return new FolderSyncInfo(project.getName(), repoLocation, tag, false);
				}
			}
		} catch (CVSException ex) {
			CVSUIPlugin.log(ex);
		}  catch (CoreException ex) {
			// Ignore the core exception since we will get one if the key is not registered
		} catch (IOException ex) {
			CVSUIPlugin.log(CVSException.wrapException(ex));
		}
		return null;
	}

	/**
	 * Method findCommonRootInSubfolders.
	 * @return String
	 */
	private void purgeAnyCVSFolders() {
		try {
			ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor(project);
			folder.accept(new ICVSResourceVisitor() {
				public void visitFile(ICVSFile file) throws CVSException {
					// nothing to do for files
				}
				public void visitFolder(ICVSFolder folder) throws CVSException {
					if (folder.isCVSFolder()) {
						// for now, just unmanage
						folder.unmanage(null);
					}
				}
			}, true /* recurse */);
		} catch (CVSException e) {
			// log the exception and return null
			CVSUIPlugin.log(e);
		}
	}
}
