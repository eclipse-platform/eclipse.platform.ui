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

	public InstallWizard() {
		this(null);
	}
	
	public InstallWizard(UpdateSearchRequest searchRequest) {
		this.searchRequest = searchRequest;
		setDialogSettings(UpdateUI.getDefault().getDialogSettings());
		setDefaultPageImageDescriptor(UpdateUIImages.DESC_INSTALL_WIZ);
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(true);
		setWindowTitle(UpdateUI.getString("MultiInstallWizard.wtitle"));
	}

	public boolean isSuccessfulInstall() {
		return installCount > 0; // or == selectedJobs.length
	}

	/**
	 * @see Wizard#performFinish()
	 */
	public boolean performFinish() {
		final IInstallFeatureOperation[] selectedJobs = reviewPage.getSelectedJobs();
		installCount = 0;

		saveSettings();

		// Check for duplication conflicts
		ArrayList conflicts =
			DuplicateConflictsValidator.computeDuplicateConflicts(
				targetPage.getTargetSites(),
				config);
		if (conflicts != null) {
			DuplicateConflictsDialog dialog =
				new DuplicateConflictsDialog(getShell(), conflicts);
			if (dialog.open() != 0)
				return false;
		}
		
		// ok to continue		
		IRunnableWithProgress operation = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
				throws InvocationTargetException {
					// setup jobs with the correct environment
		IInstallFeatureOperation[] operations =
		new IInstallFeatureOperation[selectedJobs.length];
				for (int i = 0; i < selectedJobs.length; i++) {
					IInstallFeatureOperation job = selectedJobs[i];
					IFeature[] unconfiguredOptionalFeatures = null;
					IFeatureReference[] optionalFeatures = null;
					if (optionalFeaturesPage != null) {
						optionalFeatures =
							optionalFeaturesPage.getCheckedOptionalFeatures(
								job);
						unconfiguredOptionalFeatures =
							optionalFeaturesPage
								.getUnconfiguredOptionalFeatures(
								job,
								targetPage.getTargetSite(job));
					}
					IInstallFeatureOperation op =
						OperationsManager
							.getOperationFactory()
							.createInstallOperation(
								config,
								targetPage.getTargetSite(job),
								job.getFeature(),
								optionalFeatures,
								unconfiguredOptionalFeatures,
								new JarVerificationService(
									InstallWizard.this.getShell()));
					operations[i] = op;
				}
				IOperation installOperation =
					OperationsManager
						.getOperationFactory()
						.createBatchInstallOperation(
						operations);
				try {
					installOperation.execute(
						monitor,
						InstallWizard.this);
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

	public void addPages() {
		searchRunner = new SearchRunner(getShell(), getContainer());
		if (searchRequest==null) {
			modePage = new ModeSelectionPage(searchRunner);
			addPage(modePage);
			sitePage = new SitePage(searchRunner);
			addPage(sitePage);
		}
		else {
			searchRunner.setSearchProvider(this);
		}
		reviewPage = new ReviewPage(searchRunner);
		searchRunner.setResultCollector(reviewPage);
		addPage(reviewPage);

		try {
			config = UpdateUtils.createInstallConfiguration();
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
		if (modePage!=null) modePage.saveSettings();
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
			return reviewPage.getSelectedJobs().length > 0;
		}
		return true;
	}

	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage[] pages = getPages();
		boolean start = false;
		IWizardPage nextPage = null;

		if (modePage!=null && page.equals(modePage)) {
			boolean update = modePage.isUpdateMode();
			if (update)
				return reviewPage;
			else
				return sitePage;
		}
		if (sitePage!=null && page.equals(sitePage))
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
			IInstallFeatureOperation[] installJobs = reviewPage.getSelectedJobs();
			targetPage.setJobs(installJobs);
		}
	}

	public boolean canFinish() {
		IWizardPage page = getContainer().getCurrentPage();
		return page.getNextPage() == null && super.canFinish();
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
//		if (operation instanceof IBatchOperation
//			&& data != null
//			&& data instanceof ArrayList) {
//
//			DuplicateConflictsDialog2 dialog =
//				new DuplicateConflictsDialog2(getShell(), (ArrayList) data);
//			if (dialog.open() != 0)
//				return false;
//		}
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.ui.wizards.ISearchProvider2#getSearchRequest()
	 */
	public UpdateSearchRequest getSearchRequest() {
		return searchRequest;
	}

}
