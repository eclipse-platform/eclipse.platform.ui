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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.ui.synchronize.*;

/**
 * Superclass for all CVS particpants (workspace, merge and compare)
 */
public class CVSParticipant extends SubscriberParticipant {
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscribers.SubscriberParticipant#initializeConfiguration(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	protected void initializeConfiguration(ISynchronizePageConfiguration configuration) {
		super.initializeConfiguration(configuration);
		// The decorator adds itself to the configuration
		ILabelDecorator labelDecorator = new CVSParticipantLabelDecorator(configuration);
		configuration.addLabelDecorator(labelDecorator);
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.SubscriberParticipant#updateLabels(org.eclipse.team.ui.synchronize.ISynchronizeModelElement, org.eclipse.compare.CompareConfiguration, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void updateLabels(ISynchronizeModelElement element, CompareConfiguration config, IProgressMonitor monitor) {
        super.updateLabels(element, config, monitor);
        updateLabelsForCVS(element, config, monitor);
    }

    /**
     * Helper method for updating compare editor labels
     */
    protected static void updateLabelsForCVS(ISynchronizeModelElement element, CompareConfiguration config, IProgressMonitor monitor) {
        // Add the author to the remote or base
        if (CVSUIPlugin.getPlugin().getPreferenceStore().getBoolean(ICVSUIConstants.PREF_SHOW_AUTHOR_IN_EDITOR)) {
	        SyncInfo info = getSyncInfo(element);
	        if (info != null) {
	    		final IResourceVariant remote = info.getRemote();
	    		final IResourceVariant base = info.getBase();
	    		String remoteAuthor = null;
	    		if (remote != null && !remote.isContainer()) {
	    		    try {
	                    ILogEntry entry = ((ICVSRemoteFile)remote).getLogEntry(monitor);
	                    remoteAuthor = entry.getAuthor();
	                    config.setRightLabel(Policy.bind("CVSParticipant.0", remote.getContentIdentifier(), remoteAuthor)); //$NON-NLS-1$
	                } catch (TeamException e) {
	                    CVSUIPlugin.log(e);
	                }
	    		}
	    		if (base != null && !base.isContainer()) {
	    		    try {
                        String baseAuthor;
                        if (remoteAuthor != null && remote.getContentIdentifier().equals(base.getContentIdentifier())) {
                            baseAuthor = remoteAuthor;
                        } else {
                            ILogEntry entry = ((ICVSRemoteFile)base).getLogEntry(monitor);
                            baseAuthor = entry.getAuthor();
                        }
                        config.setAncestorLabel(Policy.bind("CVSParticipant.1", base.getContentIdentifier(), baseAuthor)); //$NON-NLS-1$
                    } catch (TeamException e) {
                        CVSUIPlugin.log(e);
                    }
	    		}
	        }
        }
    }
    
	protected static SyncInfo getSyncInfo(ISynchronizeModelElement element) {
	    if (element instanceof IAdaptable) {
		    return (SyncInfo)((IAdaptable)element).getAdapter(SyncInfo.class);
	    }
	    return null;
	}
}
