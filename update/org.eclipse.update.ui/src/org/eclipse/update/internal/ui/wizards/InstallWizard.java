/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.update.internal.core.UpdateCore;
import org.eclipse.update.internal.search.SiteSearchCategory;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.UpdateUIImages;
import org.eclipse.update.internal.ui.UpdateUIMessages;
import org.eclipse.update.search.BackLevelFilter;
import org.eclipse.update.search.UpdateSearchRequest;
import org.eclipse.update.search.UpdateSearchScope;
import org.eclipse.update.ui.UpdateJob;

public class InstallWizard
	extends Wizard
	implements ISearchProvider {
	private ModeSelectionPage modePage;
	private SitePage sitePage;
	private int installCount = 0;
	private UpdateSearchRequest searchRequest;
	private boolean needsRestart;
	private static boolean isRunning;
	private UpdateJob job;
	private InstallWizardOperation operation;


	public InstallWizard(UpdateSearchRequest searchRequest) {
		isRunning = true;
        if (searchRequest == null) {
            searchRequest =
                new UpdateSearchRequest(
                    new SiteSearchCategory(true),
                    new UpdateSearchScope());
            searchRequest.addFilter(new BackLevelFilter());
        }
        this.searchRequest = searchRequest;
		setDialogSettings(UpdateUI.getDefault().getDialogSettings());
		setDefaultPageImageDescriptor(UpdateUIImages.DESC_UPDATE_WIZ);
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(true);
		setWindowTitle(UpdateUIMessages.InstallWizard_wtitle); 
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

	public IWizardPage getNextPage(IWizardPage page) {

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
		return isRunning || Job.getJobManager().find(UpdateJob.FAMILY).length > 0;
	}

    /**
     * @see Wizard#performFinish()
     */
    public boolean performFinish() {

        saveSettings();
        
        if (Job.getJobManager().find(UpdateJob.FAMILY).length > 0) {
            // another update/install job is running, need to wait to finish or cancel old job
            boolean proceed = MessageDialog.openQuestion(
                    UpdateUI.getActiveWorkbenchShell(),
                    UpdateUIMessages.InstallWizard_anotherJobTitle,
                    UpdateUIMessages.InstallWizard_anotherJob); 
            if (!proceed)
                return false; // cancel this job, and let the old one go on
        }
        UpdateCore.getPlugin().getUpdateSession().reset();
        launchInBackground();
        isRunning = false;
        return true;
    }
    
	private void launchInBackground() {
		// Downloads the feature content in the background.
		// The job listener will then install the feature when download is finished.

        if (isUpdate())
            job = new UpdateJob(UpdateUIMessages.InstallWizard_jobName, false, false);  
        else
            job = new UpdateJob(UpdateUIMessages.InstallWizard_jobName, searchRequest);  
		job.setUser(true);
		job.setPriority(Job.INTERACTIVE);
//		if (wait) {
//			progressService.showInDialog(workbench.getActiveWorkbenchWindow().getShell(), job); 
//		}
		getOperation().run(UpdateUI.getActiveWorkbenchShell(), job);
	}
    
    private InstallWizardOperation getOperation() {
		if (operation == null)
			operation = new InstallWizardOperation();
		return operation;
	}

    public boolean canFinish() {
    	      
    	if ( modePage.isCurrentPage()) {
            return isUpdate();
    	} else {
            return sitePage.isPageComplete();
    	}   	
    }
    
    private boolean isUpdate() {
        return (modePage != null && modePage.isUpdateMode());
    }
}
