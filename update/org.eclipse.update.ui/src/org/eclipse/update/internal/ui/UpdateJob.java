/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.operations.*;
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
                resultCollector = new ResultCollectorWithMirrors();
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
            resultCollector = new ResultCollectorWithMirrors();
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

    public ArrayList getUpdates() {
        return updates;
    }
    
    public IStatus getStatus() {
        return jobStatus;
    }
    
    public UpdateSearchRequest getSearchRequest() {
        return searchRequest;
    }
    
    private class ResultCollectorWithMirrors extends SearchResultCollector
            implements IUpdateSearchResultCollectorFromMirror {
        
        private HashMap mirrors = new HashMap(0);
        
        /* (non-Javadoc)
         * @see org.eclipse.update.search.IUpdateSearchResultCollectorFromMirror#getMirror(org.eclipse.update.core.ISite, java.lang.String)
         */
        public IURLEntry getMirror(final ISiteWithMirrors site, final String siteName) {
            return null;
//            if (mirrors.containsKey(site))
//                return (IURLEntry)mirrors.get(site);
//            try {
//                IURLEntry[] mirrorURLs = site.getMirrorSiteEntries();
//                if (mirrorURLs.length == 0)
//                    return null;
//                else {
//                    // here we need to prompt the user
//                    final Shell shell = UpdateUI.getActiveWorkbenchShell();
//                    final IURLEntry[] returnValue = new IURLEntry[1];
//                    shell.getDisplay().syncExec(new Runnable() {
//                        public void run() {
//                            MirrorsDialog dialog = new MirrorsDialog(shell, site, siteName);
//                            dialog.create();
//                            dialog.open();
//                            IURLEntry mirror = dialog.getMirror();
//                            mirrors.put(site, mirror);
//                            returnValue[0] = mirror;
//                        }
//                    });
//                    return returnValue[0];
//                }
//            } catch (CoreException e) {
//                return null;
//            }
        }
    }
}
