package org.eclipse.update.internal.ui.wizards;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.wizard.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.*;
import java.util.*;
import org.eclipse.update.core.*;
import org.eclipse.core.runtime.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.jface.operation.*;
import java.lang.reflect.*;

public class InstallWizard extends Wizard {
	private ReviewPage reviewPage;
	private TargetPage targetPage;
	private PendingChange job;
	private boolean successfulInstall=false;
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
		return info!=null;
	}

	
	/**
	 * @see Wizard#performFinish()
	 */
	public boolean performFinish() {
		final IConfigurationSite targetSite = (targetPage==null)?null: targetPage.getTargetSite();
		IRunnableWithProgress operation = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				try {
					successfulInstall=false;
					makeConfigurationCurrent();
					performInstall(targetSite, monitor);
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
		
		if (job.getJobType()==PendingChange.INSTALL){			
			if (hasLicense()) {
				addPage(new LicensePage(job));
			}
			config = createInstallConfiguration();
			targetPage = new TargetPage(config);
			addPage(targetPage);
		}
	}
	
	private IInstallConfiguration createInstallConfiguration() {
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			IInstallConfiguration config = localSite.cloneCurrentConfiguration(null, null);
			config.setLabel(config.getCreationDate().toString());
			return config;
		}
		catch (CoreException e) {
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
		return page.getNextPage()==null && super.canFinish();
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
	private void performInstall(IConfigurationSite targetSite, IProgressMonitor monitor) throws CoreException {
		IFeature feature = job.getFeature();		
		if (job.getJobType()==PendingChange.UNINSTALL) {
			
			//find the  config site of this feature
			ILocalSite localSite = SiteManager.getLocalSite();
			IConfigurationSite[] configSite = localSite.getCurrentConfiguration().getConfigurationSites();
			for (int i = 0; i < configSite.length; i++) {
				IConfigurationSite site = configSite[i];
				if (site.getSite().getURL().equals(feature.getSite().getURL())){
					site.remove(feature,monitor);
					break;
				}
			}
		}
		if (job.getJobType()==PendingChange.INSTALL) {
			targetSite.install(feature,monitor);
		}
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		model.addPendingChange(job);
	}
}

