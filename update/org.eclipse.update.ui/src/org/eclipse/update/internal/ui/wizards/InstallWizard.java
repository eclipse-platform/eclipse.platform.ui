package org.eclipse.update.internal.ui.wizards;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.wizard.*;
import org.eclipse.update.ui.internal.model.*;
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
	private ChecklistJob job;
	private boolean successfulInstall=false;
	private IInstallConfiguration config;

	public InstallWizard(ChecklistJob job) {
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
		IInfo info = feature.getLicense();
		return info!=null;
	}

	
	/**
	 * @see Wizard#performFinish()
	 */
	public boolean performFinish() {
		final IConfigurationSite targetSite = targetPage.getTargetSite();
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
		if (hasLicense()) {
			addPage(new LicensePage(job));
		}
		config = createInstallConfiguration();
		targetPage = new TargetPage(config);
		addPage(targetPage);
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
	private void performInstall(IConfigurationSite targetSite, IProgressMonitor monitor) throws CoreException {
		IFeature feature = job.getFeature();
	   	targetSite.install(feature, monitor);
	}
}

