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
package org.eclipse.update.internal.ui.wizards;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.ui.*;
import org.eclipse.ui.progress.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.security.*;
import org.eclipse.update.operations.*;
import org.eclipse.update.search.*;


public class InstallWizard
	extends Wizard
	implements IOperationListener, ISearchProvider {
	private ModeSelectionPage modePage;
	private SitePage sitePage;
	private ReviewPage reviewPage;
	private LicensePage licensePage;
	private OptionalFeaturesPage optionalFeaturesPage;
	private TargetPage targetPage;
	private IInstallConfiguration config;
	private int installCount = 0;
	private SearchRunner searchRunner;
	private UpdateSearchRequest searchRequest;
	private ArrayList jobs;
	private boolean needsRestart;
	private static boolean isRunning;
	private IBatchOperation installOperation;
	private Job job;
	public static final Object jobFamily = new Object();
	private IJobChangeListener jobListener;

	private class UpdateJobChangeListener extends JobChangeAdapter {
		public void done(IJobChangeEvent event) {
			// the job listener is triggered when the download job is done, and it proceeds
			// with the actual install
			if (event.getJob() == InstallWizard.this.job && event.getResult() == Status.OK_STATUS) {
				Platform.getJobManager().removeJobChangeListener(this);
				Platform.getJobManager().cancel(job);
				
				final IProgressService progressService= PlatformUI.getWorkbench().getProgressService();
				UpdateUI.getStandardDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							progressService.busyCursorWhile( new IRunnableWithProgress() {
								public void run(final IProgressMonitor monitor){
									install(monitor);
								}
							});
						} catch (InvocationTargetException e) {
							UpdateUI.logException(e);
						} catch (InterruptedException e) {
							UpdateUI.logException(e, false);
						}
					}
				});	
			}
		}
	}
		
	public InstallWizard() {
		this((UpdateSearchRequest) null);
	}

	public InstallWizard(UpdateSearchRequest searchRequest) {
		isRunning = true;
		this.searchRequest = searchRequest;
		setDialogSettings(UpdateUI.getDefault().getDialogSettings());
		setDefaultPageImageDescriptor(UpdateUIImages.DESC_UPDATE_WIZ);
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(true);
		setWindowTitle(UpdateUI.getString("InstallWizard.wtitle")); //$NON-NLS-1$
	}

	public InstallWizard(UpdateSearchRequest searchRequest, ArrayList jobs) {
		this(searchRequest);
		this.jobs = jobs;
	}

	public int getInstallCount() {
		return installCount;
	}
	public boolean isRestartNeeded() {
		return installCount > 0 && needsRestart; // or == selectedJobs.length
	}

	public boolean performCancel() {
		isRunning = false;
		if (targetPage != null)
			targetPage.removeAddedSites();
		return super.performCancel();
	}
	/**
	 * @see Wizard#performFinish()
	 */
	public boolean performFinish() {
		IInstallFeatureOperation[] selectedJobs = reviewPage.getSelectedJobs();

		saveSettings();
		
		// Check for duplication conflicts
		ArrayList conflicts = DuplicateConflictsValidator.computeDuplicateConflicts(selectedJobs, config);
		if (conflicts != null) {
			DuplicateConflictsDialog dialog = new DuplicateConflictsDialog(getShell(), conflicts);
			if (dialog.open() != 0)
				return false;
		}
		
		if (Platform.getJobManager().find(jobFamily).length > 0) {
			// another update/install job is running, need to wait to finish or cancel old job
			boolean proceed = MessageDialog.openQuestion(
					UpdateUI.getActiveWorkbenchShell(),
					UpdateUI.getString("InstallWizard.anotherJobTitle"),
					UpdateUI.getString("InstallWizard.anotherJob")); //$NON-NLS-1$
			if (!proceed)
				return false; // cancel this job, and let the old one go on
		}
		
		// set the install operation
		installOperation = getBatchInstallOperation(selectedJobs);
		if (installOperation != null)
			launchInBackground();
		return true;
	}

	public void addPages() {
		searchRunner = new SearchRunner(getShell(), getContainer());

		if (searchRequest == null && jobs == null) {
			modePage = new ModeSelectionPage(searchRunner);
			addPage(modePage);
			sitePage = new SitePage(searchRunner);
			addPage(sitePage);
		} else {
			searchRunner.setSearchProvider(this);
			if (jobs != null)
				searchRunner.setNewSearchNeeded(false);
		}
		reviewPage = new ReviewPage(searchRunner, jobs);
		searchRunner.setResultCollector(reviewPage);
		addPage(reviewPage);

		try {
//			config = UpdateUtils.createInstallConfiguration();
			config = SiteManager.getLocalSite().getCurrentConfiguration();
		} catch (CoreException e) {
			UpdateUI.logException(e);
		}

		licensePage = new LicensePage(true);
		addPage(licensePage);
		optionalFeaturesPage = new OptionalFeaturesPage(config);
		addPage(optionalFeaturesPage);
		targetPage = new TargetPage(config);
		addPage(targetPage);
	}

	private void saveSettings() {
		if (modePage != null)
			modePage.saveSettings();
	}

	private boolean isPageRequired(IWizardPage page) {
		if (page == null)
			return false;
			
		if (page.equals(licensePage)) {
			return OperationsManager.hasSelectedJobsWithLicenses(
				reviewPage.getSelectedJobs());
		}
		if (page.equals(optionalFeaturesPage)) {
			return OperationsManager.hasSelectedJobsWithOptionalFeatures(
				reviewPage.getSelectedJobs());
		}
		if (page.equals(targetPage)) {
			return reviewPage.getSelectedJobs().length > 0;
		}
		return true;
	}

	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage[] pages = getPages();
		boolean start = false;
		IWizardPage nextPage = null;

		if (modePage != null && page.equals(modePage)) {
			boolean update = modePage.isUpdateMode();
			if (update)
				return reviewPage;
			else
				return sitePage;
		}
		if (sitePage != null && page.equals(sitePage))
			return reviewPage;

		if (page.equals(reviewPage)) {
			updateDynamicPages();
		}

		for (int i = 0; i < pages.length; i++) {
			if (pages[i].equals(page)) {
				start = true;
			} else if (start) {
				if (isPageRequired(pages[i])) {
					nextPage = pages[i];
					break;
				}
			}
		}
		return nextPage;
	}

	private void updateDynamicPages() {
		if (licensePage != null) {
			IInstallFeatureOperation[] licenseJobs =
				OperationsManager.getSelectedJobsWithLicenses(
					reviewPage.getSelectedJobs());
			licensePage.setJobs(licenseJobs);
		}
		if (optionalFeaturesPage != null) {
			IInstallFeatureOperation[] optionalJobs =
				OperationsManager.getSelectedJobsWithOptionalFeatures(
					reviewPage.getSelectedJobs());
			optionalFeaturesPage.setJobs(optionalJobs);
		}
		if (targetPage != null) {
			IInstallFeatureOperation[] installJobs =
				reviewPage.getSelectedJobs();
			targetPage.setJobs(installJobs);
		}
	}

	public boolean canFinish() {
		IWizardPage page = getContainer().getCurrentPage();
		return page.equals(targetPage) && page.isPageComplete();
	}

	private void preserveOriginatingURLs(
		IFeature feature,
		IFeatureReference[] optionalFeatures) {
		// walk the hierarchy and preserve the originating URL
		// for all the optional features that are not chosen to
		// be installed.
		URL url = feature.getSite().getURL();
		try {
			IIncludedFeatureReference[] irefs =
				feature.getIncludedFeatureReferences();
			for (int i = 0; i < irefs.length; i++) {
				IIncludedFeatureReference iref = irefs[i];
				boolean preserve = false;
				if (iref.isOptional()) {
					boolean onTheList = false;
					for (int j = 0; j < optionalFeatures.length; j++) {
						if (optionalFeatures[j].equals(iref)) {
							//was on the list
							onTheList = true;
							break;
						}
					}
					if (!onTheList)
						preserve = true;
				}
				if (preserve) {
					try {
						String id =
							iref.getVersionedIdentifier().getIdentifier();
						UpdateUI.setOriginatingURL(id, url);
					} catch (CoreException e) {
						// Silently ignore
					}
				} else {
					try {
						IFeature ifeature = iref.getFeature(null);
						preserveOriginatingURLs(ifeature, optionalFeatures);
					} catch (CoreException e) {
						// Silently ignore
					}
				}
			}
		} catch (CoreException e) {
			// Silently ignore
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.operations.IOperationListener#afterExecute(org.eclipse.update.operations.IOperation)
	 */
	public boolean afterExecute(IOperation operation, Object data) {
		if (!(operation instanceof IInstallFeatureOperation))
			return true;
		IInstallFeatureOperation job = (IInstallFeatureOperation) operation;
		IFeature oldFeature = job.getOldFeature();
		if (oldFeature == null && job.getOptionalFeatures() != null)
			preserveOriginatingURLs(
				job.getFeature(),
				job.getOptionalFeatures());

		installCount++;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.operations.IOperationListener#beforeExecute(org.eclipse.update.operations.IOperation)
	 */
	public boolean beforeExecute(IOperation operation, Object data) {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.ui.wizards.ISearchProvider2#getSearchRequest()
	 */
	public UpdateSearchRequest getSearchRequest() {
		return searchRequest;
	}

	public static synchronized boolean isRunning() {
		return isRunning;
	}
	
	private IBatchOperation getBatchInstallOperation(final IInstallFeatureOperation[] selectedJobs) {
		final IVerificationListener verificationListener =new JarVerificationService(
				//InstallWizard.this.getShell());
				UpdateUI.getActiveWorkbenchShell());
		
		// setup jobs with the correct environment
		IInstallFeatureOperation[] operations =	new IInstallFeatureOperation[selectedJobs.length];
		for (int i = 0; i < selectedJobs.length; i++) {
			IInstallFeatureOperation job = selectedJobs[i];
			IFeature[] unconfiguredOptionalFeatures = null;
			IFeatureReference[] optionalFeatures = null;
			if (UpdateUtils.hasOptionalFeatures(job.getFeature())) {
				optionalFeatures = optionalFeaturesPage.getCheckedOptionalFeatures(job);
				unconfiguredOptionalFeatures = optionalFeaturesPage.getUnconfiguredOptionalFeatures(job, job.getTargetSite());
			}
			IInstallFeatureOperation op =
				OperationsManager
					.getOperationFactory()
					.createInstallOperation(
					job.getTargetSite(),
					job.getFeature(),
					optionalFeatures,
					unconfiguredOptionalFeatures,
					verificationListener);
			operations[i] = op;
		}
		return OperationsManager.getOperationFactory().createBatchInstallOperation(operations);
	}



	private void launchInBackground() {
		// Downloads the feature content in the background.
		// The job listener will then install the feature when download is finished.
		
		// TODO: should we cancel existing jobs?
		Platform.getJobManager().removeJobChangeListener(jobListener);
		Platform.getJobManager().cancel(job);
		jobListener = new UpdateJobChangeListener();
		Platform.getJobManager().addJobChangeListener(jobListener);
		
		job = new Job(UpdateUI.getString("InstallWizard.jobName")) { //$NON-NLS-1$	
			public IStatus run(IProgressMonitor monitor) {
				try {
					if (download(monitor))
						return Status.OK_STATUS;
					else
						return Status.CANCEL_STATUS;
				} finally {
					isRunning = false;
				}
			}
			public boolean belongsTo(Object family) {
				return InstallWizard.jobFamily == family;
			}
		};

		job.setUser(true);
		job.setPriority(Job.INTERACTIVE);
//		if (wait) {
//			progressService.showInDialog(workbench.getActiveWorkbenchWindow().getShell(), job); 
//		}
		job.schedule();
	}
	
	private void install(IProgressMonitor monitor) {
		// Installs the (already downloaded) features and prompts for restart
		try {
			needsRestart = installOperation.execute(monitor, InstallWizard.this);
			UpdateUI.getStandardDisplay().asyncExec(new Runnable() {
				public void run() {
					UpdateUI.requestRestart(InstallWizard.this.isRestartNeeded());
				}
			});
		} catch (final InvocationTargetException e) {
			final Throwable targetException = e.getTargetException();
			if (!(targetException instanceof InstallAbortedException)){
				UpdateUI.getStandardDisplay().syncExec(new Runnable() {
					public void run() {
						UpdateUI.logException(targetException);
					}
				});
			}
		} catch (CoreException e) {
		} 
	}
	
	private boolean download(final IProgressMonitor monitor) {
		// Downloads the feature content.
		// This method is called from a background job.
		// If download fails, the user is prompted to retry.
		try {
			IFeatureOperation[] ops = installOperation.getOperations();
			monitor.beginTask(UpdateUI.getString("InstallWizard.download"), 3*ops.length);
			for (int i=0; i<ops.length; i++) {
				IInstallFeatureOperation op = (IInstallFeatureOperation)ops[i];
				IFeatureReference[] optionalFeatures = op.getOptionalFeatures();
				SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 3);
				try {
					UpdateUtils.downloadFeatureContent(op.getFeature(), subMonitor);
				} catch (final CoreException e) {
					if(e instanceof FeatureDownloadException){
						boolean retry = retryDownload((FeatureDownloadException)e);
						if (retry) {
							// redownload current feature
							i--;
							continue;
						}
					} 
					return false;
				}
			}
			return true;
		} finally {
			monitor.done();
		}
	}
	
	private boolean retryDownload(final FeatureDownloadException e) {

		final boolean retry[] = new boolean[1];
		UpdateUI.getStandardDisplay().syncExec(new Runnable() {
			public void run() {
				retry[0] =	MessageDialog.openQuestion(
					UpdateUI.getActiveWorkbenchShell(),
					UpdateUI.getString("InstallWizard.retryTitle"), //$NON-NLS-1$
					e.getMessage()+"\n" //$NON-NLS-1$
						+ UpdateUI.getString("InstallWizard.retry")); //$NON-NLS-1$
			}
		});
		return retry[0];
	}
}
