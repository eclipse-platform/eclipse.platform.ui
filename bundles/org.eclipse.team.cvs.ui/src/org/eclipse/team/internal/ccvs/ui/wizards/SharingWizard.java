package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSProvider;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.syncinfo.*;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSDecorator;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.sync.CVSSyncCompareInput;
import org.eclipse.team.ui.IConfigurationWizard;
import org.eclipse.team.ui.sync.SyncView;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

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
		setNeedsProgressMonitor(true);
	}		
	public void addPages() {
		ImageDescriptor sharingImage = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_SHARE);
		if (doesCVSDirectoryExist()) {
			autoconnectPage = new ConfigurationWizardAutoconnectPage("autoconnectPage", Policy.bind("SharingWizard.autoConnectTitle"), sharingImage);
			autoconnectPage.setProject(project);
			addPage(autoconnectPage);
		} else {
			ICVSRepositoryLocation[] locations = CVSUIPlugin.getPlugin().getRepositoryManager().getKnownRoots();
			if (locations.length > 0) {
				locationPage = new RepositorySelectionPage("importPage", Policy.bind("SharingWizard.importTitle"), sharingImage);
				addPage(locationPage);
			}
			createLocationPage = new ConfigurationWizardMainPage("createLocationPage", Policy.bind("SharingWizard.enterInformation"), sharingImage);
			addPage(createLocationPage);
			modulePage = new ModuleSelectionPage("modulePage", Policy.bind("SharingWizard.enterModuleName"), sharingImage);
			addPage(modulePage);
			finishPage = new SharingWizardFinishPage("finishPage", Policy.bind("Ready to Share Project"), sharingImage);
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
		}
		return super.canFinish();
	}
	protected String getMainPageDescription() {
		return Policy.bind("SharingWizard.description");
	}
	protected String getMainPageTitle() {
		return Policy.bind("SharingWizard.title");
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
			getContainer().run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						monitor.beginTask("", 100);
						if (autoconnectPage != null) {
							// Autoconnect to the repository using CVS/ directories
							
							FolderSyncInfo info = autoconnectPage.getFolderSyncInfo();
							if (info == null) {
								// Error!
								return;
							}
							
							// Get the repository location
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
										Policy.bind("SharingWizard.validationFailedTitle"),
										Policy.bind("SharingWizard.validationFailedText", new Object[] {e.getStatus().getMessage()}));
									if (!keep) {
										// Remove the root
										try {
											CVSProviderPlugin.getProvider().disposeRepository(location);
										} catch (TeamException e1) {
											ErrorDialog.openError(getContainer().getShell(), Policy.bind("exception"), null, e1.getStatus());
										}
										result[0] = false;
										return;
									}
									// They want to keep the connection anyway. Fall through.
								}
							}
							
							// Set the sharing
							CVSProvider.getInstance().setSharing(project, info, new SubProgressMonitor(monitor, 50));
						} else {
							// Import
							doSync[0] = true;
							// Make sure the directory does not already exist on the server.
							// If it does, return false.
							ICVSRepositoryLocation location = getLocation();
							
							try {
								location.validateConnection(monitor);
								String moduleName = getModuleName();
								ICVSRemoteFolder folder = location.getRemoteFolder(moduleName, null);
								try {
									// Hack until exists() works properly.
									IRemoteResource[] members = folder.members(new SubProgressMonitor(monitor, 50));
									if (members.length > 0) {
										// We didn't get an exception, so the folder must already exist.
										MessageDialog.openInformation(getShell(), Policy.bind("SharingWizard.couldNotImport"), Policy.bind("SharingWizard.couldNotImportLong"));
										result[0] = false;
										return;
									}
								} catch (TeamException e) {
									// Good, we got an exception. The folder doesn't exist.
								}
							} catch (TeamException e) {
								ErrorDialog.openError(getShell(), null, null, e.getStatus());
								result[0] = false;
								doSync[0] = false;
								return;
							}
							// Create the remote module for the project
							CVSProviderPlugin.getProvider().createModule(location, project, getModuleName(), new SubProgressMonitor(monitor, 50));
						}
						CVSDecorator.refresh(project);
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
					view.showSync(new CVSSyncCompareInput(new IResource[] {project}));
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
	private ICVSRepositoryLocation getLocation() {
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
		try {
			return CVSRepositoryLocation.fromProperties(properties);
		} catch (TeamException e) {
			// To do: log
			return null;
		}
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
		try {
			ICVSFolder folder = (ICVSFolder)Session.getManagedResource(project);
			FolderSyncInfo info = folder.getFolderSyncInfo();
			return info != null;
		} catch (TeamException e) {
			ErrorDialog.openError(getContainer().getShell(), null, null, e.getStatus());
			return false;
		}
	}
}
