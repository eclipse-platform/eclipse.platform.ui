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
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.team.internal.ccvs.core.resources.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
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
	
	public void addPages() {
		if (doesCVSDirectoryExist()) {
			autoconnectPage = new ConfigurationWizardAutoconnectPage("autoconnectPage", Policy.bind("SharingWizard.autoConnectTitle"), null);
			autoconnectPage.setProject(project);
			addPage(autoconnectPage);
		} else {
			locationPage = new RepositorySelectionPage("importPage", Policy.bind("SharingWizard.importTitle"), null);
			addPage(locationPage);
			createLocationPage = new ConfigurationWizardMainPage("createLocationPage", Policy.bind("SharingWizard.enterInformation"), null);
			addPage(createLocationPage);
			modulePage = new ModuleSelectionPage("modulePage", Policy.bind("SharingWizard.enterModuleName"), null);
			addPage(modulePage);
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
		return null;
	}
	/*
	 * @see IWizard#performFinish
	 */
	public boolean performFinish() {
		final boolean[] result = new boolean[] { true };
		try {
			final boolean[] doSync = new boolean[] { false };
			getContainer().run(false, true, new WorkspaceModifyOperation() {
				public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
					try {
						if (autoconnectPage != null) {
							// Autoconnect to the repository using CVS/ directories
							
							// Create the repository location
							Properties properties = autoconnectPage.getProperties();
							ICVSRepositoryLocation location = CVSProviderPlugin.getProvider().getRepository(properties);
							boolean created = false;
							if (location == null) {
								location = CVSProviderPlugin.getProvider().createRepository(properties);
								created = true;
							}
							
							// Associate project with provider
							ICVSFolder folder = (ICVSFolder)Session.getManagedResource(project);
							FolderSyncInfo info = folder.getFolderSyncInfo();
							if (info == null) {
								// Error!
								return;
							}
							CVSTag tag = info.getTag();
	
							// Validate the connection if the user wants to
							boolean validate = autoconnectPage.getValidate();					
							if (validate) {
								// Do the validation right now
								try {
									location.validateConnection(monitor);
								} catch (TeamException e) {
									if (created)
										CVSProviderPlugin.getProvider().disposeRepository(location);
									throw e;
								}
							}
							
							// Set the sharing
							CVSProvider.getInstance().setSharing(project, location, properties.getProperty("module"), tag, monitor);
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
									IRemoteResource[] members = folder.members(new NullProgressMonitor());
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
								return;
							}
							// Create the remote module for the project
							CVSProviderPlugin.getProvider().createModule(project, getProperties(), monitor);
						}
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
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
	 * Return a Properties with connection info populated.
	 */
	private Properties getProperties() {
		// If the import page has a location, use it.
		ICVSRepositoryLocation location = locationPage.getLocation();
		if (location != null) {
			Properties result = new Properties();
			result.setProperty("host", location.getHost());
			result.setProperty("connection", location.getMethod().getName());
			result.setProperty("user", location.getUsername());
			int port = location.getPort();
			if (port != ICVSRepositoryLocation.USE_DEFAULT_PORT) {
				result.setProperty("port", "" + port);
			}
			result.setProperty("root", location.getRootDirectory());
			result.setProperty("module", getModuleName());
			return result;
		}
		Properties properties = createLocationPage.getProperties();
		properties.setProperty("module", getModuleName());
		return properties;
	}
	/**
	 * Return an ICVSRepositoryLocation
	 */
	private ICVSRepositoryLocation getLocation() {
		// If the import page has a location, use it.
		ICVSRepositoryLocation location = locationPage.getLocation();
		if (location != null) return location;
		
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
