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
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.custom.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.search.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.operations.*;
import org.eclipse.update.search.*;


public class InstallWizard
	extends Wizard
	implements ISearchProvider {
	private ModeSelectionPage modePage;
	private SitePage sitePage;
	private IInstallConfiguration config;
	private int installCount = 0;
	private UpdateSearchRequest searchRequest;
	private ArrayList jobs;
	private boolean needsRestart;
	private static boolean isRunning;
	private IBatchOperation installOperation;
	private UpdateJob job;
	private IJobChangeListener jobListener;


	public InstallWizard(UpdateSearchRequest searchRequest) {
		isRunning = true;
        if (searchRequest == null) {
            searchRequest =
                new UpdateSearchRequest(
                    new SiteSearchCategory(),
                    new UpdateSearchScope());
            searchRequest.addFilter(new BackLevelFilter());
        }
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

	public boolean isRestartNeeded() {
		return installCount > 0 && needsRestart; // or == selectedJobs.length
	}

	public boolean performCancel() {
		isRunning = false;
		return super.performCancel();
	}

	public void addPages() {
		modePage = new ModeSelectionPage(searchRequest);
		addPage(modePage);
		sitePage = new SitePage(searchRequest);
		addPage(sitePage);
	}

	private void saveSettings() {
		if (modePage != null)
			modePage.saveSettings();
	}

	private boolean isPageRequired(IWizardPage page) {
		if (page == null)
			return false;
		return true;
	}

	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage[] pages = getPages();
		boolean start = false;
		IWizardPage nextPage = null;

		if (modePage != null && page.equals(modePage)) {
			boolean update = modePage.isUpdateMode();
			if (!update)
				return sitePage;
		}
        return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.ui.wizards.ISearchProvider2#getSearchRequest()
	 */
	public UpdateSearchRequest getSearchRequest() {
		return searchRequest;
	}

	public static synchronized boolean isRunning() {
		return isRunning || Platform.getJobManager().find(UpdateJob.family).length > 0;
	}

    /**
     * @see Wizard#performFinish()
     */
    public boolean performFinish() {
        saveSettings();
        
        if (Platform.getJobManager().find(UpdateJob.family).length > 0) {
            // another update/install job is running, need to wait to finish or cancel old job
            boolean proceed = MessageDialog.openQuestion(
                    UpdateUI.getActiveWorkbenchShell(),
                    UpdateUI.getString("InstallWizard.anotherJobTitle"),
                    UpdateUI.getString("InstallWizard.anotherJob")); //$NON-NLS-1$
            if (!proceed)
                return false; // cancel this job, and let the old one go on
        }
        
        launchInBackground();
        isRunning = false;
        return true;
    }
    
	private void launchInBackground() {
		// Downloads the feature content in the background.
		// The job listener will then install the feature when download is finished.

		if (jobListener != null)
			Platform.getJobManager().removeJobChangeListener(jobListener);
		if (job != null)
			Platform.getJobManager().cancel(job);
		jobListener = new UpdateJobChangeListener();
		Platform.getJobManager().addJobChangeListener(jobListener);
		
        if (isUpdate())
            job = new UpdateJob(UpdateUI.getString("InstallWizard.jobName"), true, false, false);  //$NON-NLS-1$
        else
            job = new UpdateJob(UpdateUI.getString("InstallWizard.jobName"), searchRequest);  //$NON-NLS-1$    
		job.setUser(true);
		job.setPriority(Job.INTERACTIVE);
//		if (wait) {
//			progressService.showInDialog(workbench.getActiveWorkbenchWindow().getShell(), job); 
//		}
		job.schedule();
	}
    
    private class UpdateJobChangeListener extends JobChangeAdapter {
        public void done(final IJobChangeEvent event) {  
            // the job listener is triggered when the search job is done, and proceeds to next wizard
            if (event.getJob() == InstallWizard.this.job) {
                isRunning = false;
                Platform.getJobManager().removeJobChangeListener(this);
                Platform.getJobManager().cancel(job);
                
                if (InstallWizard.this.job.getStatus() == Status.CANCEL_STATUS)
                    return;
                
                if (InstallWizard.this.job.getStatus() != Status.OK_STATUS)
                    UpdateUI.getStandardDisplay().syncExec(new Runnable() {
                        public void run() {
                            UpdateUI.log(InstallWizard.this.job.getStatus(), true);
                        }
                    });
                
                UpdateUI.getStandardDisplay().asyncExec(new Runnable() {
                    public void run() {
                        UpdateUI.getStandardDisplay().beep();
                        BusyIndicator.showWhile(UpdateUI.getStandardDisplay(),
                                new Runnable() {
                                    public void run() {
                                        openInstallWizard2();
                                    }
                                });
                    }
                });
            }
        }
        
        private void openInstallWizard2() {
            if (InstallWizard2.isRunning())
                // job ends and a new one is rescheduled
                return;
                
            InstallWizard2 wizard = new InstallWizard2(job.getSearchRequest(), job.getUpdates(), job.isUpdate());
            WizardDialog dialog =
                new ResizableInstallWizardDialog(
                    UpdateUI.getActiveWorkbenchShell(),
                    wizard,
                    UpdateUI.getString("AutomaticUpdatesJob.Updates")); //$NON-NLS-1$
            dialog.create();
            dialog.open();
        }
    }

    public boolean canFinish() {
        if (isUpdate())
            return true;
        else
            return super.canFinish();
    }
    
    private boolean isUpdate() {
        return (modePage != null && modePage.isUpdateMode());
    }
}
