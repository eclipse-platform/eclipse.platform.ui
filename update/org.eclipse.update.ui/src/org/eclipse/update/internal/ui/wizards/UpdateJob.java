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
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.operations.*;
import org.eclipse.update.search.*;

public class UpdateJob extends Job {
	
	private class SearchResultCollector implements IUpdateSearchResultCollector {
		public void accept(IFeature feature) {
			IInstallFeatureOperation operation =
				OperationsManager
					.getOperationFactory()
					.createInstallOperation(null, feature, null, null, null);
			updates.add(operation);
		}
	}
	
	// job family	
	public static final Object family = new Object();
	private IUpdateSearchResultCollector resultCollector;
	private UpdateSearchRequest searchRequest;
	private ArrayList updates;
    private boolean isUpdate;
    private boolean download;
    private boolean isAutomatic;
    private IStatus jobStatus = Status.OK_STATUS;

    /**
     * 
     * @param isUpdate true if searching for updates
     * @param isAutomatic true if automatically searching for updates   
     * @param name the job name
     * @param download download updates automatically
     */
	public UpdateJob( String name, boolean isUpdate, boolean isAutomatic, boolean download ) {
		super(name);
        this.isUpdate = isUpdate;
        this.isAutomatic = isAutomatic;
        this.download = download;
		updates = new ArrayList();
		setPriority(Job.DECORATE);
	}

    public UpdateJob( String name, UpdateSearchRequest searchRequest ) {
        super(name);
        this.searchRequest = searchRequest;
        updates = new ArrayList();
        setPriority(Job.DECORATE);
    }

    public boolean isUpdate() {
        return isUpdate;
    }
    
	public boolean belongsTo(Object family) {
		return UpdateJob.family == family;
	}
	
    // will always return ok status, but the jobStatus will keep the actual status
    public IStatus run(IProgressMonitor monitor) {
        if (isUpdate)
            jobStatus = runUpdates(monitor);
        else
            jobStatus = runSearchForNew(monitor);
        return Status.OK_STATUS;
    }
    
    public IStatus runSearchForNew(IProgressMonitor monitor) {
        if (UpdateCore.DEBUG) {
            UpdateCore.debug("Search for features started."); //$NON-NLS-1$
        }

        try {
            if (resultCollector == null)
                resultCollector = new SearchResultCollector();
            searchRequest.performSearch(resultCollector, monitor);
            if (UpdateCore.DEBUG) {
                UpdateCore.debug("Automatic update search finished - " //$NON-NLS-1$
                        + updates.size() + " results."); //$NON-NLS-1$
            }
            return Status.OK_STATUS;
        } catch (CoreException e) {
            return e.getStatus();
        }
    }
    

	public IStatus runUpdates(IProgressMonitor monitor) {
        ArrayList statusList = new ArrayList();
        if (UpdateCore.DEBUG) {
            if (isAutomatic)
                UpdateCore.debug("Automatic update search started."); //$NON-NLS-1$
            else
                UpdateCore.debug("Update search started."); //$NON-NLS-1$
        }
        searchRequest = UpdateUtils.createNewUpdatesRequest(null);

        if (resultCollector == null)
            resultCollector = new SearchResultCollector();
        try {
            searchRequest.performSearch(resultCollector, monitor);
        } catch (CoreException e) {
            statusList.add(e.getStatus());
        }
        if (UpdateCore.DEBUG) {
            UpdateCore.debug("Automatic update search finished - " //$NON-NLS-1$
                    + updates.size() + " results."); //$NON-NLS-1$
        }
        if (updates.size() > 0) {
            // silently download if download enabled
            if (download) {
                if (UpdateCore.DEBUG) {
                    UpdateCore.debug("Automatic download of updates started."); //$NON-NLS-1$
                }
                for (int i = 0; i < updates.size(); i++) {
                    IInstallFeatureOperation op = (IInstallFeatureOperation) updates
                            .get(i);
                    IFeature feature = op.getFeature();
                    try {
                        UpdateUtils.downloadFeatureContent(op.getTargetSite(),
                                feature, null, monitor);
                    } catch (InstallAbortedException e) {
                        return Status.CANCEL_STATUS;
                    } catch (CoreException e) {
                        statusList.add(e.getStatus());
                        updates.remove(i);
                        i -= 1;
                    }
                }
                if (UpdateCore.DEBUG) {
                    UpdateCore.debug("Automatic download of updates finished."); //$NON-NLS-1$
                }
            }
            // prompt the user
            if (isUpdate && isAutomatic && !InstallWizard.isRunning()) {
                if (download) {
                    UpdateUI.getStandardDisplay().asyncExec(new Runnable() {
                        public void run() {
                            asyncNotifyDownloadUser();
                        }
                    });
                } else {
                    UpdateUI.getStandardDisplay().asyncExec(new Runnable() {
                        public void run() {
                            asyncNotifyUser();
                        }
                    });
                }
            }
        }
        
        if (statusList.size() == 0)
            return Status.OK_STATUS;
        else if (statusList.size() == 1)
            return (IStatus) statusList.get(0);
        else {
            IStatus[] children = (IStatus[]) statusList
                    .toArray(new IStatus[statusList.size()]);
            return new MultiStatus("org.eclipse.update.ui", //$NON-NLS-1$
                    ISite.SITE_ACCESS_EXCEPTION, children, Policy
                            .bind("Search.networkProblems"), //$NON-NLS-1$
                    null);
        }
    }

	private void asyncNotifyUser() {
		// ask the user to install updates
        UpdateUI.getStandardDisplay().beep();
		if (MessageDialog
			.openQuestion(
				UpdateUI.getActiveWorkbenchShell(),
				UpdateUI.getString("AutomaticUpdatesJob.EclipseUpdates1"), //$NON-NLS-1$
				UpdateUI.getString("AutomaticUpdatesJob.UpdatesAvailable"))) { //$NON-NLS-1$
			BusyIndicator.showWhile(UpdateUI.getStandardDisplay(), new Runnable() {
				public void run() {
					openInstallWizard2();
				}
			});
		}
		// notify the manager that the job is done
		done(Status.OK_STATUS);
	}
	
	private void asyncNotifyDownloadUser() {
		// ask the user to install updates
        UpdateUI.getStandardDisplay().beep();
		if (MessageDialog
			.openQuestion(
                UpdateUI.getActiveWorkbenchShell(),
                UpdateUI.getString("AutomaticUpdatesJob.EclipseUpdates2"), //$NON-NLS-1$
                UpdateUI.getString("AutomaticUpdatesJob.UpdatesDownloaded"))) { //$NON-NLS-1$
			BusyIndicator.showWhile(UpdateUI.getStandardDisplay(), new Runnable() {
				public void run() {
					openInstallWizard2();
				}
			});
		} else {
			// Don't discard downloaded data, as next time we compare timestamps.
			
			// discard all the downloaded data from cache (may include old data as well)
			//Utilities.flushLocalFile();
		}
		// notify the manager that the job is done
		done(Status.OK_STATUS);
	}

	private void openInstallWizard2() {
		if (InstallWizard.isRunning())
			// job ends and a new one is rescheduled
			return;
			
		InstallWizard2 wizard = new InstallWizard2(searchRequest, updates, isUpdate);
		WizardDialog dialog =
			new ResizableInstallWizardDialog(
                UpdateUI.getActiveWorkbenchShell(),
				wizard,
				UpdateUI.getString("AutomaticUpdatesJob.Updates")); //$NON-NLS-1$
		dialog.create();
		dialog.open();
	}
    
    public ArrayList getUpdates() {
        return updates;
    }
    
    public IStatus getStatus() {
        return jobStatus;
    }
    
    public UpdateSearchRequest getSearchRequest() {
        return searchRequest;
    }
}
