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

	public InstallWizard(ChecklistJob job) {
		setDialogSettings(UpdateUIPlugin.getDefault().getDialogSettings());
		setDefaultPageImageDescriptor(UpdateUIPluginImages.DESC_INSTALL_WIZ);
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(true);
		this.job = job;
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
		IRunnableWithProgress operation = new IRunnableWithProgress() {
		final ISite targetSite = targetPage.getTargetSite();
			public void run(IProgressMonitor monitor) {
				try {
					performInstall(targetSite, monitor);
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
		targetPage = new TargetPage();
		addPage(targetPage);
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
	private void performInstall(ISite targetSite, IProgressMonitor monitor) throws CoreException {
		IFeature feature = job.getFeature();
	   	targetSite.install(feature, monitor);
	}
}

