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
package org.eclipse.update.internal.ui.wizards;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.security.*;

public class UnifiedInstallWizard extends Wizard {
	private static final String KEY_UNABLE = "MultiInstallWizard.error.unable";
	private static final String KEY_OLD = "MultiInstallWizard.error.old";
	private static final String KEY_SAVED_CONFIG =
		"MultiInstallWizard.savedConfig";
	private static final String KEY_INSTALLING =
		"MultiInstallWizard.installing";
	private ModeSelectionPage2 modePage;
	private UnifiedSitePage sitePage;
	private UnifiedReviewPage reviewPage;
	private LicensePage2 licensePage;
	private MultiOptionalFeaturesPage2 optionalFeaturesPage;
	private MultiTargetPage2 targetPage;
	private IInstallConfiguration config;
	private int installCount = 0;
	private SearchRunner2 searchRunner;
	private boolean updateMode = true;

	public UnifiedInstallWizard() {
		setDialogSettings(UpdateUI.getDefault().getDialogSettings());
		setDefaultPageImageDescriptor(UpdateUIImages.DESC_INSTALL_WIZ);
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(true);
		setWindowTitle(UpdateUI.getString("MultiInstallWizard.wtitle"));
	}

	public boolean isSuccessfulInstall() {
		return installCount > 0;
	}

	/**
	 * @see Wizard#performFinish()
	 */
	public boolean performFinish() {
		final PendingOperation[] selectedJobs = reviewPage.getSelectedJobs();
		installCount = 0;

		saveSettings();

		if (targetPage != null) {
			// Check for duplication conflicts
			ArrayList conflicts =
				DuplicateConflictsValidator.computeDuplicateConflicts(
					targetPage.getTargetSites(),
					config);
			if (conflicts != null) {
				DuplicateConflictsDialog2 dialog =
					new DuplicateConflictsDialog2(getShell(), conflicts);
				if (dialog.open() != 0)
					return false;
			}
		}

		// ok to continue		
		IRunnableWithProgress operation = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
				throws InvocationTargetException {
				try {
					UpdateManager.makeConfigurationCurrent(config, null);
					execute(selectedJobs, monitor);
				} catch (InstallAbortedException e) {
					throw new InvocationTargetException(e);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, true, operation);
		} catch (InvocationTargetException e) {
			Throwable targetException = e.getTargetException();
			if (targetException instanceof InstallAbortedException) {
				return true;
			} else {
				UpdateUI.logException(e);
			}
			return false;
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}

	/*
	 * When we are uninstalling, there is not targetSite
	 */
	private void execute(
		PendingOperation[] selectedJobs,
		IProgressMonitor monitor)
		throws InstallAbortedException, CoreException {
		monitor.beginTask(
			UpdateUI.getString(KEY_INSTALLING),
			selectedJobs.length);
		for (int i = 0; i < selectedJobs.length; i++) {
			PendingOperation job = selectedJobs[i];
			SubProgressMonitor subMonitor =
				new SubProgressMonitor(
					monitor,
					1,
					SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
			executeOneJob(job, subMonitor);
			//monitor.worked(1);
			UpdateManager.saveLocalSite();
			installCount++;
		}
	}

	public void addPages() {
		searchRunner = new SearchRunner2(getShell(), getContainer());
		modePage = new ModeSelectionPage2(searchRunner);
		addPage(modePage);
		sitePage = new UnifiedSitePage(searchRunner);
		addPage(sitePage);
		reviewPage = new UnifiedReviewPage(searchRunner);
		addPage(reviewPage);

		try {
			config = UpdateManager.createInstallConfiguration();
		} catch (CoreException e) {
			UpdateUI.logException(e);
		}

		licensePage = new LicensePage2(true);
		addPage(licensePage);
		optionalFeaturesPage = new MultiOptionalFeaturesPage2(config);
		addPage(optionalFeaturesPage);
		targetPage = new MultiTargetPage2(config);
		addPage(targetPage);
	}

	private void saveSettings() {
		modePage.saveSettings();
	}

	private boolean isPageRequired(IWizardPage page) {
		if (page.equals(licensePage)) {
			return OperationsManager.hasSelectedJobsWithLicenses(
				reviewPage.getSelectedJobs());
		}
		if (page.equals(optionalFeaturesPage)) {
			return OperationsManager.hasSelectedJobsWithOptionalFeatures(
				reviewPage.getSelectedJobs());
		}
		if (page.equals(targetPage)) {
			return OperationsManager.hasSelectedInstallJobs(
				reviewPage.getSelectedJobs());
		}
		return true;
	}

	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage[] pages = getPages();
		boolean start = false;
		IWizardPage nextPage = null;

		if (page.equals(modePage)) {
			boolean update = modePage.isUpdateMode();
			if (update)
				return reviewPage;
			else
				return sitePage;
		}
		if (page.equals(sitePage))
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
			PendingOperation[] licenseJobs =
				OperationsManager.getSelectedJobsWithLicenses(
					reviewPage.getSelectedJobs());
			licensePage.setJobs(licenseJobs);
		}
		if (optionalFeaturesPage != null) {
			PendingOperation[] optionalJobs =
				OperationsManager.getSelectedJobsWithOptionalFeatures(
					reviewPage.getSelectedJobs());
			optionalFeaturesPage.setJobs(optionalJobs);
		}
		if (targetPage != null) {
			PendingOperation[] installJobs =
				OperationsManager.getSelectedInstallJobs(
					reviewPage.getSelectedJobs());
			targetPage.setJobs(installJobs);
		}
	}

	public boolean canFinish() {
		IWizardPage page = getContainer().getCurrentPage();
		return page.getNextPage() == null && super.canFinish();
	}

	private void executeOneJob(PendingOperation job, IProgressMonitor monitor)
		throws CoreException {
		IConfiguredSite targetSite = null;
		Object[] optionalElements = null;
		IFeatureReference[] optionalFeatures = null;
		if (job.getJobType() == PendingChange.INSTALL) {
			if (optionalFeaturesPage != null) {
				optionalElements =
					optionalFeaturesPage.getOptionalElements(job);
				optionalFeatures =
					optionalFeaturesPage.getCheckedOptionalFeatures(job);
			}
			if (targetPage != null) {
				targetSite = targetPage.getTargetSite(job);
			}
		}
		UpdateManager.getOperationsManager().executeOneJob(
			job,
			config,
			targetSite,
			optionalElements,
			optionalFeatures,
			getVerificationListener(),
			monitor);

		if (job.getJobType() == PendingChange.INSTALL) {
			IFeature oldFeature = job.getOldFeature();
			if (oldFeature != null
				&& !job.isOptionalDelta()
				&& optionalElements != null) {
				preserveOptionalState(
					config,
					targetSite,
					UpdateManager.isPatch(job.getFeature()),
					optionalElements);
			} else if (oldFeature == null && optionalFeatures != null) {
				preserveOriginatingURLs(job.getFeature(), optionalFeatures);
			}
		}
	}

	static void preserveOptionalState(
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		boolean patch,
		Object[] optionalElements) {
		for (int i = 0; i < optionalElements.length; i++) {
			FeatureHierarchyElement2 fe =
				(FeatureHierarchyElement2) optionalElements[i];
			Object[] children = fe.getChildren(true, patch, config);
			preserveOptionalState(config, targetSite, patch, children);
			if (!fe.isEnabled(config)) {
				IFeature newFeature = fe.getFeature();
				try {
					IFeature localFeature =
						UpdateManager.getLocalFeature(targetSite, newFeature);
					if (localFeature != null)
						targetSite.unconfigure(localFeature);
				} catch (CoreException e) {
					// Eat this - we will leave with it
				}
			}
		}
	}

	private void preserveOriginatingURLs(
		IFeature feature,
		IFeatureReference[] optionalFeatures) {
		// walk the hieararchy and preserve the originating URL
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

	private IVerificationListener getVerificationListener() {
		return new JarVerificationService(this.getShell());
	}
}
