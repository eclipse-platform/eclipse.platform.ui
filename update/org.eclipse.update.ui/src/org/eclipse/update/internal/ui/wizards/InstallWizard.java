package org.eclipse.update.internal.ui.wizards;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.wizard.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.security.JarVerificationService;
import org.eclipse.update.internal.core.FeaturePackagedContentProvider;
import org.eclipse.update.internal.ui.*;
import java.util.*;
import org.eclipse.update.core.*;
import org.eclipse.update.configuration.*;
import org.eclipse.core.runtime.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.jface.operation.*;
import java.lang.reflect.*;
import org.eclipse.update.internal.ui.manager.UIProblemHandler;

public class InstallWizard extends Wizard {
	private ReviewPage reviewPage;
	private TargetPage targetPage;
	private PendingChange job;
	private boolean successfulInstall = false;
	private IInstallConfiguration config;

	public InstallWizard(PendingChange job) {
		setDialogSettings(UpdateUIPlugin.getDefault().getDialogSettings());
		setDefaultPageImageDescriptor(UpdateUIPluginImages.DESC_INSTALL_WIZ);
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(true);
		this.job = job;
	}

	public boolean isSuccessfulInstall() {
		return successfulInstall;
	}

	private boolean hasLicense() {
		IFeature feature = job.getFeature();
		IURLEntry info = feature.getLicense();
		return info != null;
	}

	/**
	 * @see Wizard#performFinish()
	 */
	public boolean performFinish() {
		final IConfiguredSite targetSite =
			(targetPage == null) ? null : targetPage.getTargetSite();
		IRunnableWithProgress operation = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				try {
					successfulInstall = false;
					makeConfigurationCurrent();
					execute(targetSite, monitor);
					saveLocalSite();
					successfulInstall = true;
				} catch (CoreException e) {
					UpdateUIPlugin.logException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(false, true, operation);
		} catch (InvocationTargetException e) {
			UpdateUIPlugin.logException(e);
			return false;
		} catch (InterruptedException e) {
			UpdateUIPlugin.logException(e);
			return false;
		}
		return true;
	}

	public void addPages() {
		reviewPage = new ReviewPage(job);
		addPage(reviewPage);

		config = createInstallConfiguration();

		if (job.getJobType() == PendingChange.INSTALL) {
			if (hasLicense()) {
				addPage(new LicensePage(job));
			}
			targetPage = new TargetPage(config);
			addPage(targetPage);
		}
	}

	private IInstallConfiguration createInstallConfiguration() {
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			IInstallConfiguration config = localSite.cloneCurrentConfiguration();
			config.setLabel(config.getCreationDate().toString());
			return config;
		} catch (CoreException e) {
			UpdateUIPlugin.logException(e);
			return null;
		}
	}

	private void makeConfigurationCurrent() throws CoreException {
		ILocalSite localSite = SiteManager.getLocalSite();
		localSite.addConfiguration(config);
	}

	private void saveLocalSite() throws CoreException {
		ILocalSite localSite = SiteManager.getLocalSite();
		localSite.save();
	}

	public boolean canFinish() {
		IWizardPage page = getContainer().getCurrentPage();
		return page.getNextPage() == null && super.canFinish();
	}

	public IWizardPage getPreviousPage(IWizardPage page) {
		return super.getPreviousPage(page);
	}
	public IWizardPage getNextPage(IWizardPage page) {
		return super.getNextPage(page);
	}
	/*
	 * When we are uninstalling, there is not targetSite
	 */
	private void execute(
		IConfiguredSite targetSite,
		IProgressMonitor monitor)
		throws CoreException {
		IFeature feature = job.getFeature();
		if (job.getJobType() == PendingChange.UNINSTALL) {
			//find the  config site of this feature
			IConfiguredSite site = findConfigSite(feature);
			if (site != null) {
				site.remove(feature, monitor);
			} else {
				// we should do something here
				String message = "Unable to locate configuration site for the feature";
				IStatus status =
					new Status(
						IStatus.ERROR,
						UpdateUIPlugin.getPluginId(),
						IStatus.OK,
						message,
						null);
				throw new CoreException(status);
			}
		} else if (job.getJobType() == PendingChange.INSTALL) {
			IFeature oldFeature = job.getOldFeature();
			boolean success = true;
			if (oldFeature != null) {
				success = unconfigure(oldFeature);
			}
			if (success) targetSite.install(feature,verifierFor(feature), monitor);
			else return;
		}
		else if (job.getJobType() == PendingChange.CONFIGURE) {
			configure(job.getFeature());
		}
		else if (job.getJobType() == PendingChange.UNCONFIGURE) {
			unconfigure(job.getFeature());
		}
		else {
			return;
		}
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		model.addPendingChange(job);
	}

	private IConfiguredSite findConfigSite(IFeature feature)
		throws CoreException {
		ILocalSite localSite = SiteManager.getLocalSite();
		IConfiguredSite[] configSite =
			localSite.getCurrentConfiguration().getConfiguredSites();
		for (int i = 0; i < configSite.length; i++) {
			IConfiguredSite site = configSite[i];
			if (site.getSite().getURL().equals(feature.getSite().getURL())) {
				return site;
			}
		}
		return null;
	}

	private boolean unconfigure(IFeature feature) throws CoreException {
		IConfiguredSite site = findConfigSite(feature);
		if (site != null) {
			return site.unconfigure(feature, new UIProblemHandler());
		}
		return false;
	}
	private void configure(IFeature feature) throws CoreException {
		IConfiguredSite site = findConfigSite(feature);
		if (site != null) {
			site.configure(feature);
		}
	}
	private IFeatureVerification verifierFor(IFeature feature) throws CoreException {
		if (feature.getFeatureContentProvider() instanceof FeaturePackagedContentProvider){ 
			return new JarVerificationService(this.getShell());
		}
		return null;
	}
}