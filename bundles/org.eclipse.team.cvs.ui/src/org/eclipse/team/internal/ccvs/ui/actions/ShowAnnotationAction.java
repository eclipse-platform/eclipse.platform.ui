/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.listeners.LogEntry;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.ShowAnnotationOperation;

public class ShowAnnotationAction extends WorkspaceAction {

	/**
	 * Action to open a CVS Annotate View
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
	    final ICVSResource resource= getSingleSelectedCVSResource();
	    if (resource == null) 
	        return;
		execute(resource);
	}
	
	/**
	 * Fetch the revision number of a CVS resource and perform a ShowAnnotationOperation
	 * in the background.
	 *  
	 * @param cvsResource The CVS resource (must not be null)
	 * 
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	public void execute(final ICVSResource cvsResource) throws InvocationTargetException, InterruptedException {
		final String revision= getRevision(cvsResource);
		if (revision == null)
		    return;
		boolean binary = isBinary(cvsResource);
        if (binary) {
		    if (!MessageDialog.openQuestion(getShell(), Policy.bind("ShowAnnotationAction.2"), Policy.bind("ShowAnnotationAction.3", cvsResource.getName()))) { //$NON-NLS-1$ //$NON-NLS-2$
		        return;
		    }
		}
		new ShowAnnotationOperation(getTargetPart(), cvsResource, revision, binary).run();
	}

    private boolean isBinary(ICVSResource cvsResource) {
        if (cvsResource.isFolder()) return false;
        try {
            byte[] syncBytes = ((ICVSFile)cvsResource).getSyncBytes();
                if (syncBytes == null) 
                    return false;
            return ResourceSyncInfo.isBinary(syncBytes);
        } catch (CVSException e) {
            return false;
        }
    }

    /**
	 * Ony enabled for single resource selection
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSResource resource = getSingleSelectedCVSResource();
		return (resource != null && ! resource.isFolder() && resource.isManaged());
	}
	
	/**
	 * This action is called from one of a Resource Navigator a CVS Resource
	 * Navigator or a History Log Viewer. Return the selected resource as an
	 * ICVSResource
	 * 
	 * @return ICVSResource
	 */
	private ICVSResource getSingleSelectedCVSResource() {
		// Selected from a CVS Resource Navigator
		final ICVSResource[] cvsResources = getSelectedCVSResources();
		if (cvsResources.length == 1) {
			return cvsResources[0];
		}

		// Selected from a History Viewer
		final Object[] logEntries = getSelectedResources(LogEntry.class);
		if (logEntries.length == 1) {
			final LogEntry aLogEntry = (LogEntry) logEntries[0];
			final ICVSRemoteFile cvsRemoteFile = aLogEntry.getRemoteFile();
			return cvsRemoteFile;
		}

		// Selected from a Resource Navigator
		final IResource[] resources = getSelectedResources();
		if (resources.length == 1) {
			return CVSWorkspaceRoot.getCVSResourceFor(resources[0]);
		}
		return null;
	}

    
	/**
	 * Get the revision for the CVS resource. Throws an InvocationTargetException
	 * if the revision could not be determined.
	 * 
	 * @param cvsResource The CVS resource
	 * @return The revision of the resource.
	 * @throws InvocationTargetException
	 */
	private String getRevision(ICVSResource cvsResource) throws InvocationTargetException {
        final ResourceSyncInfo info;
        try {
            info= cvsResource.getSyncInfo();
            if (info == null) 
                throw new CVSException(Policy.bind("ShowAnnotationAction.noSyncInfo", cvsResource.getName())); //$NON-NLS-1$
        } catch (CVSException e) {
            throw new InvocationTargetException(e);
        }
        return info.getRevision();
    }
	
	public String getId() {
		return ICVSUIConstants.CMD_ANNOTATE;
	}
}
