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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSProvider;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.CVSEntryLineTag;
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
public class ConfigurationWizard extends ConnectionWizard implements IConfigurationWizard {
	public static final int DO_SYNCHRONIZE = 0;
	public static final int DO_IMPORT_CHECKOUT = 1;
	public static final int DO_UPDATE = 2;
	public static final int DO_NOTHING = 3;
	
	// The project to configure
	private IProject project;

	private ConfigurationWizardPostPage postPage;
		
	public void addPages() {
		super.addPages();
		postPage = new ConfigurationWizardPostPage("repositoryPage2", getMainPageTitle(), null);
		addPage(postPage);
	}
	protected String getMainPageDescription() {
		return Policy.bind("ConfigurationWizard.description");
	}
	protected String getMainPageTitle() {
		return Policy.bind("ConfigurationWizard.title");
	}
	protected int getStyle() {
		return ConfigurationWizardMainPage.CONNECTION_METHOD |
			ConfigurationWizardMainPage.USER |
			ConfigurationWizardMainPage.PASSWORD |
			ConfigurationWizardMainPage.PORT |
			ConfigurationWizardMainPage.HOST |
			ConfigurationWizardMainPage.REPOSITORY_PATH |
			ConfigurationWizardMainPage.MODULE_RADIO |
			ConfigurationWizardMainPage.TAG;
	}
	/*
	 * @see IWizard#performFinish
	 */
	public boolean performFinish() {
		try {
			final boolean[] sync = new boolean[] { false };
			final CVSTag[] syncTag = new CVSTag[1];
			getContainer().run(false, true, new WorkspaceModifyOperation() {
				public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
					try {
						getMainPage().getControl().getDisplay().syncExec(new Runnable() {
							public void run() {
								getMainPage().finish(new NullProgressMonitor());
							}
						});
						// Get the result of the wizard page
						Properties properties = getProperties();
						if (properties.getProperty("module") == null) {
							properties.setProperty("module", project.getName());
						}
						
						
						int post = postPage.getPostOperation();
						if (post == DO_IMPORT_CHECKOUT) {
							// Import and Checkout, this associates the project with a provider automatically.
							CVSProviderPlugin.getProvider().importAndCheckout(project, properties, monitor);
						} else {
							// Create the repository location
							ICVSRepositoryLocation location = CVSProviderPlugin.getProvider().getRepository(properties);
							boolean created = false;
							if (location == null) {
								location = CVSProviderPlugin.getProvider().createRepository(properties);
								created = true;
							}
							
							// Associate project with provider
							ICVSFolder folder = (ICVSFolder)Client.getManagedResource(project);
							FolderSyncInfo info = folder.getFolderSyncInfo();
							CVSTag tag = null;
							if (info != null) {
								// CVS/ directory exists. If the contents are actually the same
								// as the user's selections, do nothing. If they differ, inform
								// the user and bail out.
								tag = info.getTag();
								String existingLocation = info.getRoot();
								String newLocation = location.getLocation();
								boolean changed = !existingLocation.equals(newLocation);
								if (!changed) {
									String oldTag = tag == null ? null : tag.getName();
									String newTag = properties.getProperty("tag");
									if (oldTag == null) {
										changed = newTag != null;
									} else {
										changed = !oldTag.equals(newTag);
									}
								}
								if (changed) {
									// Tell the user that they differ. Refuse to set the sharing.
									MessageDialog.openError(getShell(), Policy.bind("ConfigurationWizard.cannotConfigure"), Policy.bind("ConfigurationWizard.cannotConfigureLong"));
									if (created)
										CVSProviderPlugin.getProvider().disposeRepository(location);
									return;
								}
							} else {
								String tagString = properties.getProperty("tag");
								if (tagString != null) {
									tag = new CVSTag(tagString, CVSTag.BRANCH);
								}
							}
							// Validate the connection if the user wants to
							boolean validate = postPage.getValidate();					
							if (validate) {
								// Do the validation right now
								try {
									location.validateConnection();
								} catch (TeamException e) {
									if (created)
										CVSProviderPlugin.getProvider().disposeRepository(location);
									throw e;
								}
							}
							
							// Set the sharing
							CVSProvider.getInstance().setSharing(project, location, properties.getProperty("module"), tag, new NullProgressMonitor());
	
							// Do whatever the user wanted afterwards.
							switch (post) {
								case DO_SYNCHRONIZE:
									sync[0] = true;
									syncTag[0] = tag;
									break;
								case DO_UPDATE:
									CVSTeamProvider provider = (CVSTeamProvider)TeamPlugin.getManager().getProvider(project);
									provider.update(new IResource[] {project}, IResource.DEPTH_INFINITE, tag, false, new NullProgressMonitor());
									break;
								default:
									// Do nothing.
							}
						}
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
			// Don't need to run this code in the workspace runnable.
			if (sync[0]) {
				SyncView view = (SyncView)CVSUIPlugin.getActivePage().findView(SyncView.VIEW_ID);
				if (view == null) {
					CVSUIPlugin plugin = CVSUIPlugin.getPlugin();
					view = SyncView.findInActivePerspective();
				}
				if (view != null) {
					try {
						CVSUIPlugin.getActivePage().showView(SyncView.VIEW_ID);
					} catch (PartInitException e) {
						CVSUIPlugin.log(e.getStatus());
					}
					CVSTeamProvider provider = (CVSTeamProvider)TeamPlugin.getManager().getProvider(project);
					try {
						IRemoteSyncElement tree = provider.getRemoteSyncTree(project, syncTag[0], new NullProgressMonitor());
						view.showSync(new CVSSyncCompareInput(new IRemoteSyncElement[] {tree}));
					} catch (TeamException e) {
						CVSUIPlugin.log(e.getStatus());
					}
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
				CVSUIPlugin.log(((TeamException)target).getStatus());
			}
		}
		return true;
	}
	/*
	 * @see IConfigurationWizard#init(IWorkbench, IProject)
	 */
	public void init(IWorkbench workbench, IProject project) {
		this.project = project;
		// Determine if there is an existing CVS/ directory from which configuration
		// information can be retrieved.
		try {
			ICVSFolder folder = (ICVSFolder)Client.getManagedResource(project);
			FolderSyncInfo info = folder.getFolderSyncInfo();
			if (info == null) return;
			ICVSRepositoryLocation location = CVSProviderPlugin.getProvider().getRepository(info.getRoot());
			Properties properties = new Properties();
			properties.setProperty("connection", location.getMethod().getName());
			properties.setProperty("host", location.getHost());
			int port = location.getPort();
			if (port != location.USE_DEFAULT_PORT) {
				properties.setProperty("port", "" + port);
			}
			properties.setProperty("user", location.getUsername());
			properties.setProperty("root", location.getRootDirectory());
			
			String repository = info.getRepository();
			if (!repository.equals(project.getName())) {
				properties.setProperty("module", repository);
			}
			CVSEntryLineTag tag = info.getTag();
			if (tag != null) {
				properties.setProperty("tag", tag.getName());
			}
			setProperties(properties);
		} catch (TeamException e) {
			CVSUIPlugin.log(e.getStatus());
		}
	}
}
