package org.eclipse.update.internal.ui.wizards;

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
		try {
			IInfo info = feature.getLicense();
			return info!=null;
		}
		catch (CoreException e) {
			return false;
		}
	}

	
	/**
	 * @see Wizard#performFinish()
	 */
	public boolean performFinish() {
		IRunnableWithProgress operation = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				try {
					performInstall(monitor);
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
	private void performInstall(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Installing...", 5);
		for (int i=0; i<5; i++) {
			try {
				Thread.currentThread().sleep(1000);
			}
			catch (InterruptedException e) {
			}
			monitor.subTask("File "+(i+1));
			monitor.worked(1);
		}
	}
}

