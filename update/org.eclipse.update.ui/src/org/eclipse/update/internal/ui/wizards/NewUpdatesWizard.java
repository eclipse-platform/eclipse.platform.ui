package org.eclipse.update.internal.ui.wizards;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.search.*;
import org.eclipse.update.internal.ui.security.JarVerificationService;

public class NewUpdatesWizard extends Wizard {
	private IInstallConfiguration config;
	private boolean successfulInstall;
	private NewUpdatesWizardPage mainPage;
	private PendingChange[] jobs;

	public NewUpdatesWizard(SearchObject searchObject) {
		setDialogSettings(UpdateUIPlugin.getDefault().getDialogSettings());
		setDefaultPageImageDescriptor(UpdateUIPluginImages.DESC_UPDATE_WIZ);
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(true);
		createPendingChanges(searchObject);
	}

	public boolean isSuccessfulInstall() {
		return successfulInstall;
	}

	private void createPendingChanges(SearchObject searchObject) {
		ArrayList result = new ArrayList();
		Object[] sites = searchObject.getChildren(null);
		for (int i = 0; i < sites.length; i++) {
			SearchResultSite site = (SearchResultSite) sites[i];
			createPendingChanges(site, result);
		}
		jobs =
			(PendingChange[]) result.toArray(new PendingChange[result.size()]);
	}

	private void createPendingChanges(
		SearchResultSite site,
		ArrayList result) {
		Object[] candidates = site.getChildren(null);
		for (int i = 0; i < candidates.length; i++) {
			SimpleFeatureAdapter adapter = (SimpleFeatureAdapter) candidates[i];
			try {
				IFeature feature = adapter.getFeature();
				IFeature[] installed =
					UpdateUIPlugin.getInstalledFeatures(feature);
				PendingChange change = new PendingChange(installed[0], feature);
				result.add(change);
			} catch (CoreException e) {
				UpdateUIPlugin.logException(e);
			}
		}
	}

	private IFeature[] getFeaturesWithLicenses() {
		ArrayList result = new ArrayList();
		for (int i = 0; i < jobs.length; i++) {
			PendingChange job = jobs[i];
			IFeature feature = job.getFeature();
			IURLEntry info = feature.getLicense();
			if (info == null)
				continue;
			if (info.getAnnotation() != null
				&& info.getAnnotation().length() > 0)
				result.add(feature);
		}
		return (IFeature[]) result.toArray(new IFeature[result.size()]);
	}

	/**
	 * @see Wizard#performFinish()
	 */
	public boolean performFinish() {
		/*
		final IConfiguredSite targetSite =
			(targetPage == null) ? null : targetPage.getTargetSite();
		*/
		IRunnableWithProgress operation = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
				throws InvocationTargetException {
				try {
					successfulInstall = false;
					makeConfigurationCurrent(config);
					execute(monitor);
					saveLocalSite();
					successfulInstall = true;
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
			UpdateUIPlugin.logException(e);
			return false;
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}

	public void addPages() {
		config = InstallWizard.createInstallConfiguration();
		if (config != null) {
			mainPage = new NewUpdatesWizardPage(jobs, config);
			addPage(mainPage);
		}
	}

	public static void makeConfigurationCurrent(IInstallConfiguration config)
		throws CoreException {
		ILocalSite localSite = SiteManager.getLocalSite();
		localSite.addConfiguration(config);
	}

	public static void saveLocalSite() throws CoreException {
		ILocalSite localSite = SiteManager.getLocalSite();
		localSite.save();
	}

	public boolean canFinish() {
		IWizardPage page = getContainer().getCurrentPage();
		return page.getNextPage() == null && super.canFinish();
	}

	/*
	 * When we are uninstalling, there is not targetSite
	 */
	private void execute(IProgressMonitor monitor) throws CoreException {
		/*
		IFeature feature = job.getFeature();
		if (job.getJobType() == PendingChange.UNINSTALL) {
			//find the  config site of this feature
			IConfiguredSite site = findConfigSite(feature, config);
			if (site != null) {
				site.remove(feature, monitor);
			} else {
				// we should do something here
				throwError(UpdateUIPlugin.getResourceString(KEY_UNABLE));
			}
		} else if (job.getJobType() == PendingChange.INSTALL) {
			IFeature oldFeature = job.getOldFeature();
			boolean success = true;
			if (oldFeature != null) {
				success = unconfigure(oldFeature);
			}
			if (success)
				targetSite.install(feature, getVerificationListener(), monitor);
			else {
				throwError(UpdateUIPlugin.getResourceString(KEY_OLD));
			}
		} else if (job.getJobType() == PendingChange.CONFIGURE) {
			configure(job.getFeature());
		} else if (job.getJobType() == PendingChange.UNCONFIGURE) {
			unconfigure(job.getFeature());
		} else {
			// should not be here
			return;
		}
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		model.addPendingChange(job);
		*/
	}

	private void throwError(String message) throws CoreException {
		IStatus status =
			new Status(
				IStatus.ERROR,
				UpdateUIPlugin.getPluginId(),
				IStatus.OK,
				message,
				null);
		throw new CoreException(status);
	}

	static IConfiguredSite findConfigSite(
		IFeature feature,
		IInstallConfiguration config)
		throws CoreException {
		IConfiguredSite[] configSites = config.getConfiguredSites();
		for (int i = 0; i < configSites.length; i++) {
			IConfiguredSite site = configSites[i];
			if (site.getSite().equals(feature.getSite())) {
				return site;
			}
		}
		return null;
	}

	private boolean unconfigure(IFeature feature) throws CoreException {
		IConfiguredSite site = findConfigSite(feature, config);
		if (site != null) {
			return site.unconfigure(feature);
		}
		return false;
	}
	private void configure(IFeature feature) throws CoreException {
		IConfiguredSite site = findConfigSite(feature, config);
		if (site != null) {
			site.configure(feature);
		}
	}

	private IVerificationListener getVerificationListener() {
		return new JarVerificationService(this.getShell());
	}
}