package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.Properties;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.TagSelectionDialog;
import org.eclipse.team.internal.ui.sync.SyncView;
import org.eclipse.team.ui.IConfigurationWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.team.internal.ccvs.ui.sync.CVSSyncCompareInput;
import org.eclipse.team.internal.ccvs.ui.sync.CVSSyncCompareUnsharedInput;

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
			addPage(autoconnectPage);
		} else {
			ICVSRepositoryLocation[] locations = CVSUIPlugin.getPlugin().getRepositoryManager().getKnownRoots();
			if (locations.length > 0) {
				locationPage = new RepositorySelectionPage("importPage", Policy.bind("SharingWizard.importTitle"), sharingImage); //$NON-NLS-1$ //$NON-NLS-2$
				addPage(locationPage);
			}
			createLocationPage = new ConfigurationWizardMainPage("createLocationPage", Policy.bind("SharingWizard.enterInformation"), sharingImage); //$NON-NLS-1$ //$NON-NLS-2$
			addPage(createLocationPage);
			createLocationPage.setDialogSettings(getDialogSettings());
			modulePage = new ModuleSelectionPage("modulePage", Policy.bind("SharingWizard.enterModuleName"), sharingImage); //$NON-NLS-1$ //$NON-NLS-2$
			addPage(modulePage);
			finishPage = new SharingWizardFinishPage("finishPage", Policy.bind("SharingWizard.readyToFinish"), sharingImage); //$NON-NLS-1$ //$NON-NLS-2$
			addPage(finishPage);
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
			getContainer().run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						monitor.beginTask("", 100); //$NON-NLS-1$
						if (autoconnectPage != null) {
							// Autoconnect to the repository using CVS/ directories
							
							FolderSyncInfo info = autoconnectPage.getFolderSyncInfo();
							if (info == null) {
								// Error!
								return;
							}
							
							// Get the repository location (the get will add the locatin to the provider)
							boolean isPreviouslyKnown = CVSProviderPlugin.getProvider().isKnownRepository(info.getRoot());
							ICVSRepositoryLocation location = CVSProviderPlugin.getProvider().getRepository(info.getRoot());
	
							// Validate the connection if the user wants to
							boolean validate = autoconnectPage.getValidate();					
							if (validate) {
								// Do the validation
								try {
									location.validateConnection(new SubProgressMonitor(monitor, 50));
								} catch (TeamException e) {
									// Exception validating. We can continue if the user wishes.
									boolean keep = MessageDialog.openQuestion(getContainer().getShell(),
										Policy.bind("SharingWizard.validationFailedTitle"), //$NON-NLS-1$
										Policy.bind("SharingWizard.validationFailedText", new Object[] {e.getStatus().getMessage()})); //$NON-NLS-1$
									if (!keep) {
										// Remove the root
										try {
											if (!isPreviouslyKnown) {
												CVSProviderPlugin.getProvider().disposeRepository(location);
											}
										} catch (TeamException e1) {
											ErrorDialog.openError(getContainer().getShell(), Policy.bind("exception"), null, e1.getStatus()); //$NON-NLS-1$
										}
										result[0] = false;
										return;
									}
									// They want to keep the connection anyway. Fall through.
								}
							}
							
							// Set the sharing
							CVSProviderPlugin.getProvider().setSharing(project, info, new SubProgressMonitor(monitor, 50));
						} else {
							// Import
							doSync[0] = true;
							// Make sure the directory does not already exist on the server.
							// If it does, return false.
							ICVSRepositoryLocation location;
							boolean isKnown;
							try {
								location = getLocation();
								isKnown = CVSProviderPlugin.getProvider().isKnownRepository(location.getLocation());
								location.validateConnection(monitor);
								String moduleName = getModuleName();
								ICVSRemoteFolder folder = location.getRemoteFolder(moduleName, null);
								if (folder.exists(new SubProgressMonitor(monitor, 50))) {
									projectExists[0] = true;
									boolean sync = MessageDialog.openQuestion(getShell(), Policy.bind("SharingWizard.couldNotImport"), Policy.bind("SharingWizard.couldNotImportLong", getModuleName())); //$NON-NLS-1$ //$NON-NLS-2$
									result[0] = sync;
									doSync[0] = sync;
									return;
								}
							} catch (TeamException e) {
								ErrorDialog.openError(getShell(), null, null, e.getStatus());
								result[0] = false;
								doSync[0] = false;
								return;
							}
							// Add the location to the provider if it is new
							if (!isKnown) {
								CVSProviderPlugin.getProvider().addRepository(location);
							}
							// Create the remote module for the project
							CVSProviderPlugin.getProvider().createModule(location, project, getModuleName(), new SubProgressMonitor(monitor, 50));
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
				SyncView view = (SyncView)CVSUIPlugin.getActivePage().findView(SyncView.VIEW_ID);
				if (view == null) {
					view = SyncView.findInActivePerspective();
				}
				if (view != null) {
					try {
						CVSUIPlugin.getActivePage().showView(SyncView.VIEW_ID);
					} catch (PartInitException e) {
						CVSUIPlugin.log(e.getStatus());
					}
					CVSSyncCompareInput input;
					if (projectExists[0]) {
						try {
							ICVSRepositoryLocation location = getLocation();
							String moduleName = getModuleName();
							TagSelectionDialog dialog = new TagSelectionDialog(getShell(), 
								new ICVSFolder[] {(ICVSFolder)location.getRemoteFolder(moduleName, null)}, 
								Policy.bind("SharingWizard.selectTagTitle"),  //$NON-NLS-1$
								Policy.bind("SharingWizard.selectTag"), TagSelectionDialog.INCLUDE_HEAD_TAG | TagSelectionDialog.INCLUDE_BRANCHES, false /*don't show recurse option*/); //$NON-NLS-1$
							dialog.setBlockOnOpen(true);
							if (dialog.open() == Dialog.CANCEL) {
								return false;
							}
							CVSTag tag = dialog.getResult();
							input = new CVSSyncCompareUnsharedInput(project, getLocation(), moduleName, tag);
						} catch (TeamException e) {
							throw new InvocationTargetException(e);
						}
					} else {
						input = new CVSSyncCompareInput(new IResource[] {project});
					}
					view.showSync(input);
				}
			}
		} catch (InterruptedException e) {
			return true;
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof RuntimeException) {
				throw (RuntimeException)target;
			}
			if (target instanceof Error) {
				throw (Error)target;
			} else if (target instanceof TeamException) {
				ErrorDialog.openError(getContainer().getShell(), null, null, ((TeamException)target).getStatus());
			}
		}

		return result[0];
	}

	/**
	 * Return an ICVSRepositoryLocation
	 */
	private ICVSRepositoryLocation getLocation() throws TeamException {
		// If the import page has a location, use it.
		if (locationPage != null) {
			ICVSRepositoryLocation location = locationPage.getLocation();
			if (location != null) return location;
		}
				
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				createLocationPage.finish(new NullProgressMonitor());
			}
		});
		Properties properties = createLocationPage.getProperties();
		return CVSProviderPlugin.getProvider().createRepository(properties);
	}
	/**
	 * Return the module name.
	 */
	private String getModuleName() {
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
			final IStatus[] status = new IStatus[] { null };
			if (e.getTargetException() instanceof CoreException) {
				status[0] = ((CoreException)e.getTargetException()).getStatus();
			} else if (e.getTargetException() instanceof TeamException) {
				status[0] = ((TeamException)e.getTargetException()).getStatus();
			} else {
				status[0] = new Status(IStatus.ERROR, CVSUIPlugin.ID, 0, e.getTargetException().getMessage(), e.getTargetException());
			}
			Runnable runnable = new Runnable() {
				public void run() {
					Shell shell = null;
					if (getContainer() != null) {
						shell = getContainer().getShell();
					}
					ErrorDialog.openError(shell, null, null, status[0]);
				}
			};
			if (shell == null) {
				Display.getDefault().syncExec(runnable);
			} else {
				runnable.run();
			}
		} catch (InterruptedException e) {
		}
		return isCVSFolder[0];
	}
}
