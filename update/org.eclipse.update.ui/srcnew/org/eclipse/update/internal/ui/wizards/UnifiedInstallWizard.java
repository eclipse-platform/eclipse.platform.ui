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

public class UnifiedInstallWizard
	extends Wizard
	implements IUpdateModelChangedListener {
	private static final String KEY_INSTALLING =
		"MultiInstallWizard.installing";
	private UnifiedModeSelectionPage modePage;
	private UnifiedSitePage sitePage;
	private UnifiedReviewPage reviewPage;
	private UnifiedLicensePage licensePage;
	private UnifiedOptionalFeaturesPage optionalFeaturesPage;
	private UnifiedTargetPage targetPage;
	private IInstallConfiguration config;
	private int installCount = 0;
	private SearchRunner2 searchRunner;

	public UnifiedInstallWizard() {
		setDialogSettings(UpdateUI.getDefault().getDialogSettings());
		setDefaultPageImageDescriptor(UpdateUIImages.DESC_INSTALL_WIZ);
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(true);
		setWindowTitle(UpdateUI.getString("MultiInstallWizard.wtitle"));

		UpdateManager.getOperationsManager().addUpdateModelChangedListener(
			this);
	}

	public boolean isSuccessfulInstall() {
		return installCount > 0;
	}

	public boolean performCancel() {
		UpdateManager.getOperationsManager().removeUpdateModelChangedListener(this);
		return super.performCancel();
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
				// setup jobs with the correct environment
				for (int i = 0; i < selectedJobs.length; i++) {
					InstallOperation installJob =
						(InstallOperation) selectedJobs[i];
					installJob.setInstallConfiguration(config);
					installJob.setTargetSite(
						targetPage.getTargetSite(installJob));
					installJob.setVerificationListener(
						getVerificationListener());

					if (optionalFeaturesPage != null) {
						installJob.setOptionalElements(
							optionalFeaturesPage.getOptionalElements(
								installJob));
						installJob.setOptionalFeatures(
							optionalFeaturesPage.getCheckedOptionalFeatures(
								installJob));
					}
				}
				try {

					UpdateManager.getOperationsManager().installFeatures(
						selectedJobs,
						UnifiedInstallWizard.this,
						monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
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
		finally {
			UpdateManager.getOperationsManager().removeUpdateModelChangedListener(this);
		}
		return true;
	}

	public void addPages() {
		searchRunner = new SearchRunner2(getShell(), getContainer());
		modePage = new UnifiedModeSelectionPage(searchRunner);
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

		licensePage = new UnifiedLicensePage(true);
		addPage(licensePage);
		optionalFeaturesPage = new UnifiedOptionalFeaturesPage(config);
		addPage(optionalFeaturesPage);
		targetPage = new UnifiedTargetPage(config);
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

	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.operations.IUpdateModelChangedListener#objectChanged(java.lang.Object, java.lang.String)
	 */
	public void objectChanged(Object object, String property) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.operations.IUpdateModelChangedListener#objectsAdded(java.lang.Object, java.lang.Object[])
	 */
	public void objectsAdded(Object parent, Object[] children) {
		if (!(parent instanceof InstallOperation))
			return;
		InstallOperation job = (InstallOperation)parent;
		IFeature oldFeature = job.getOldFeature();
		if (oldFeature == null && job.getOptionalFeatures() != null) 
			preserveOriginatingURLs(job.getFeature(), job.getOptionalFeatures());
			
		installCount++; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.operations.IUpdateModelChangedListener#objectsRemoved(java.lang.Object, java.lang.Object[])
	 */
	public void objectsRemoved(Object parent, Object[] children) {
	}

}
