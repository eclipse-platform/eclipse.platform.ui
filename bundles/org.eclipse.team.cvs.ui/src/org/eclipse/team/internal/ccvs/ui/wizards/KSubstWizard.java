package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSStatus;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * A wizard for changing the keyword substitution mode of files.
 * 
 * 1.  Ask the user select to select the desired keyword substitution mode.
 * 2.  Compute the set of possibly affected resources
 * 3.  If the affected resources include existing committed files, warn the user
 *     and provide an option to include them in the operation anyways.
 * 4.  If the affected resources include dirty files, warn the user and provide
 *     an option to include them in the operation anyways.
 * 5.  Perform the operation on Finish.
 */
public class KSubstWizard extends Wizard {
	private KSubstOption defaultKSubst;

	private final IResource[] resources;
	private final int depth;
	private List changeList = null;
	private KSubstOption changeOption = null;

	private KSubstWizardSelectionPage mainPage;
	private KSubstWizardSummaryPage summaryPage;
	private KSubstWizardSharedFilesPage sharedFilesPage;
	private KSubstWizardDirtyFilesPage dirtyFilesPage;
	
	public class KSubstChangeElement {
		public static final int ADDED_FILE = 1;
		public static final int CHANGED_FILE = 2;
		public static final int UNCHANGED_FILE = 4;
	
		private IFile file;
		private int classification;
		private boolean excluded;
		private KSubstOption fromKSubst;
		private KSubstOption toKSubst;
		
		private KSubstChangeElement(IFile file, int classification, boolean excluded, KSubstOption fromKSubst, KSubstOption toKSubst) {
			this.file = file;
			this.classification = classification;
			this.excluded = excluded;
			this.fromKSubst = fromKSubst;
			this.toKSubst = toKSubst;
		}
		public boolean matchesFilter(int filter) {
			return (classification & filter) != 0;
		}
		public boolean isExcluded() {
			return excluded;
		}
		public void setExcluded(boolean excluded) {
			this.excluded = excluded;
		}
		public boolean isNewKSubstMode() {
			return ! fromKSubst.equals(toKSubst);
		}
		public void setKSubst(KSubstOption toKSubst) {
			this.toKSubst = toKSubst;
		}
		public KSubstOption getKSubst() {
			return toKSubst;
		}
		public IFile getFile() {
			return file;
		}
	}
	
	/**
	 * Creates a wizard to set the keyword substitution mode for the specified resources.
	 * 
	 * @param resources the resources to alter
	 * @param depth the recursion depth
	 * @param defaultOption the keyword substitution option to select by default
	 */
	public KSubstWizard(IResource[] resources, int depth, KSubstOption defaultOption) {
		super();
		this.defaultKSubst = defaultOption;
		this.resources = resources;
		this.depth = depth;
		setWindowTitle(Policy.bind("KSubstWizard.title"));
	}

	/**
	 * Returns the keyword substitution option that was selected at the time
	 * the Finish button was pressed.
	 */
	public KSubstOption getKSubstOption() {
		return defaultKSubst;
	}

	public void addPages() {
		// add main page
		String pageTitle = Policy.bind("KSubstWizardSelectionPage.pageTitle"); //$NON-NLS-1$
		String pageDescription = Policy.bind("KSubstWizardSelectionPage.pageDescription"); //$NON-NLS-1$
		mainPage = new KSubstWizardSelectionPage(pageTitle, defaultKSubst);
		mainPage.setDescription(pageDescription);
		mainPage.setTitle(pageTitle);
		addPage(mainPage);
		
		// add summary page
		pageTitle = Policy.bind("KSubstWizardSummaryPage.pageTitle"); //$NON-NLS-1$
		pageDescription = Policy.bind("KSubstWizardSummaryPage.pageDescription"); //$NON-NLS-1$
		summaryPage = new KSubstWizardSummaryPage(pageTitle, false);
		summaryPage.setDescription(pageDescription);
		summaryPage.setTitle(pageTitle);
		addPage(summaryPage);
		
		// add shared files warning page
		pageTitle = Policy.bind("KSubstWizardSharedFilesPage.pageTitle"); //$NON-NLS-1$
		pageDescription = Policy.bind("KSubstWizardSharedFilesPage.pageDescription"); //$NON-NLS-1$
		sharedFilesPage = new KSubstWizardSharedFilesPage(pageTitle, false);
		sharedFilesPage.setDescription(pageDescription);
		sharedFilesPage.setTitle(pageTitle);
		addPage(sharedFilesPage);
		
		// add changed files warning page
		pageTitle = Policy.bind("KSubstWizardDirtyFilesPage.pageTitle"); //$NON-NLS-1$
		pageDescription = Policy.bind("KSubstWizardDirtyFilesPage.pageDescription"); //$NON-NLS-1$
		dirtyFilesPage = new KSubstWizardDirtyFilesPage(pageTitle, false);
		dirtyFilesPage.setDescription(pageDescription);
		dirtyFilesPage.setTitle(pageTitle);
		addPage(dirtyFilesPage);		
	}

	public IWizardPage getNextPage(IWizardPage page) {
		if (page == mainPage) {
			if (prepareSharedFilesPage()) return sharedFilesPage;
		} else if (page == sharedFilesPage) {
			if (sharedFilesPage.includeSharedFiles() && prepareDirtyFilesPage()) return dirtyFilesPage;
		} else if (page == summaryPage) {
			return null;
		}
		prepareSummaryPage();
		return summaryPage;
	}
	
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (page == summaryPage) {
			if (sharedFilesPage.includeSharedFiles() && prepareDirtyFilesPage()) return dirtyFilesPage;
			if (prepareSharedFilesPage()) return sharedFilesPage;
			return mainPage;
		} else if (page == dirtyFilesPage) {
			if (prepareSharedFilesPage()) return sharedFilesPage;
			return mainPage;
		} else if (page == sharedFilesPage) {
			return mainPage;
		}
		return null;
	}
	
	/* (Non-javadoc)
	 * Method declared on IWizard.
	 */
	public boolean needsProgressMonitor() {
		return true;
	}

	/* (Non-javadoc)
	 * Method declared on IWizard.
	 */
	public boolean needsPreviousAndNextButtons() {
		return true;
	}
	
	/* (Non-javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		try {
			defaultKSubst = mainPage.getKSubstOption();
			final List messages = new ArrayList();
			getContainer().run(false /*fork*/, true /*cancelable*/, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask("", 10000);
						monitor.setTaskName(Policy.bind("KSubstWizard.working"));
						computeChangeList(mainPage.getKSubstOption());
						Map table = getProviderMapping();
						
						int workPerProvider = 10000 / (table.size() + 1);
						monitor.worked(workPerProvider);
						for (Iterator it = table.entrySet().iterator(); it.hasNext();) {
							Map.Entry entry = (Map.Entry) it.next();
							CVSTeamProvider provider = (CVSTeamProvider) entry.getKey();
							Map providerFiles = (Map) entry.getValue();

							IStatus status = provider.setKeywordSubstitution(providerFiles,
								Policy.subMonitorFor(monitor, workPerProvider));
							if (status.getCode() != CVSStatus.OK) {
								messages.add(status);
							}
						}
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
			// Check for any status messages and display them
			if ( ! messages.isEmpty()) {
				boolean error = false;
				MultiStatus combinedStatus = new MultiStatus(CVSUIPlugin.ID, 0,
					Policy.bind("KSubstWizard.problemsMessage"), null);
				for (int i = 0; i < messages.size(); i++) {
					IStatus status = (IStatus)messages.get(i);
					if (status.getSeverity() == IStatus.ERROR || status.getCode() == CVSStatus.SERVER_ERROR) {
						error = true;
					}
					combinedStatus.merge(status);
				}
				String message = null;
				IStatus statusToDisplay;
				if (combinedStatus.getChildren().length == 1) {
					message = combinedStatus.getMessage();
					statusToDisplay = combinedStatus.getChildren()[0];
				} else {
					statusToDisplay = combinedStatus;
				}
				String title;
				if (error) {
					title = Policy.bind("KSubstWizard.errorTitle");
				} else {
					title = Policy.bind("KSubstWizard.warningTitle");
				}
				ErrorDialog.openError(getShell(), title, message, statusToDisplay);
			}
			return true;
		} catch (InterruptedException e1) {
			return true;
		} catch (InvocationTargetException e2) {
			if (e2.getTargetException() instanceof CoreException) {
				CoreException e = (CoreException) e2.getTargetException();
				ErrorDialog.openError(getShell(), Policy.bind("KSubstWizard.problemsMessage"), null, e.getStatus()); //$NON-NLS-1$
				return false;
			} else {
				Throwable target = e2.getTargetException();
				if (target instanceof RuntimeException) {
					throw (RuntimeException) target;
				}
				if (target instanceof Error) {
					throw (Error) target;
				}
			}
			return true;
		}
	}

	private boolean prepareDirtyFilesPage() {
		BusyIndicator.showWhile(getContainer().getShell().getDisplay(), new Runnable() {
			public void run() {
				computeChangeList(mainPage.getKSubstOption());
				dirtyFilesPage.setChangeList(changeList);
			}
		});
		return ! dirtyFilesPage.isListEmpty();
	}

	private boolean prepareSharedFilesPage() {
		BusyIndicator.showWhile(getContainer().getShell().getDisplay(), new Runnable() {
			public void run() {
				computeChangeList(mainPage.getKSubstOption());
				sharedFilesPage.setChangeList(changeList);
			}
		});
		return ! sharedFilesPage.isListEmpty();
	}
	
	private void prepareSummaryPage() {
		BusyIndicator.showWhile(getContainer().getShell().getDisplay(), new Runnable() {
			public void run() {
				computeChangeList(mainPage.getKSubstOption());
				summaryPage.setChangeList(changeList, getFilters());
			}
		});
	}
	
	/**
	 * @param ksubst the desired keyword substitution mode, if null chooses for each file:
	 *         <code>KSubstOption.fromPattern(fileName).isBinary() ? KSUBST_BINARY : KSUBST_TEXT</code>
	 */
	private void computeChangeList(final KSubstOption ksubst) {
		if (changeList != null) {
			if (changeOption == ksubst) return;
			changeList.clear();
		} else {
			changeList = new ArrayList();
		}
		changeOption = ksubst;
		// recurse over all specified resources, considering each exactly once
		final Set seen = new HashSet();
		for (int i = 0; i < resources.length; i++) {
			final IResource currentResource = resources[i];
			try {
				currentResource.accept(new IResourceVisitor() {
					public boolean visit(IResource resource) throws CoreException {
						try {
							if (resource.getType() == IResource.FILE && resource.exists() && ! seen.contains(resource)) {
								seen.add(resource);
								IFile file = (IFile) resource;
								ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor(file);
								if (cvsFile.isManaged()) {
									ResourceSyncInfo info = cvsFile.getSyncInfo();
									// classify the change
									final int classification;
									if (info.isAdded()) {
										classification = KSubstChangeElement.ADDED_FILE;
									} else if (info.isDeleted()) {
										return true;
									} else if (cvsFile.isModified()) {
										classification = KSubstChangeElement.CHANGED_FILE;
									} else {
										classification = KSubstChangeElement.UNCHANGED_FILE;
									}
									// determine the to/from substitution modes
									KSubstOption fromKSubst = info.getKeywordMode();
									KSubstOption toKSubst = ksubst;
									if (ksubst == null) {
										toKSubst = KSubstOption.fromFile(file);
									}
									changeList.add(new KSubstChangeElement(file, classification, false, fromKSubst, toKSubst));
								}
							}
						} catch (TeamException e) {
							throw new CoreException(e.getStatus());
						}
						// always return true and let the depth determine if children are visited
						return true;
					}
				}, depth, false);
			} catch (CoreException e) {
				ErrorDialog.openError(getShell(), Policy.bind("KSubstWizard.problemsMessage"), null, e.getStatus()); //$NON-NLS-1$
			}
		}
	}

	private int getFilters() {
		return KSubstChangeElement.ADDED_FILE |
			(sharedFilesPage.includeSharedFiles() ? KSubstChangeElement.UNCHANGED_FILE |
			(dirtyFilesPage.includeDirtyFiles() ? KSubstChangeElement.CHANGED_FILE : 0) : 0);
	}
	
	private Map getProviderMapping() {
		Map table = new HashMap();
		int filter = getFilters();
		for (Iterator it = changeList.iterator(); it.hasNext();) {
			KSubstChangeElement change = (KSubstChangeElement) it.next();
			if (! change.isExcluded() && change.isNewKSubstMode() && change.matchesFilter(filter)) {
				// classify file according to its provider
				IFile file = change.getFile();
				RepositoryProvider provider = RepositoryProvider.getProvider(file.getProject(), CVSProviderPlugin.getTypeId());
				Map providerMap = (Map) table.get(provider);
				if (providerMap == null) {
					providerMap = new HashMap();
					table.put(provider, providerMap);
				}
				providerMap.put(file, change.toKSubst);
			}
		}
		return table;
	}
}
