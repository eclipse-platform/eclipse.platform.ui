/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.syncinfo;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;
import org.eclipse.team.internal.core.BackgroundEventHandler;

/**
 * This class handles resources changes that are reported in deltas
 * in a deferred manner (i.e. in a background job)
 */
public class DeferredResourceChangeHandler extends BackgroundEventHandler {

	public DeferredResourceChangeHandler() {
		super(Policy.bind("DeferredResourceChangeHandler.0"), Policy.bind("DeferredResourceChangeHandler.1")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static final int IGNORE_FILE_CHANGED = 1;
	private static final int RECREATED_CVS_RESOURCE = 2;
	
	private Set changedIgnoreFiles = new HashSet();
	private Set recreatedResources = new HashSet();

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.BackgroundEventHandler#processEvent(org.eclipse.team.core.subscribers.BackgroundEventHandler.Event, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void processEvent(Event event, IProgressMonitor monitor) throws TeamException {
		int type = event.getType();
		switch (type) {
			case IGNORE_FILE_CHANGED :
				changedIgnoreFiles.add(event.getResource());
			case RECREATED_CVS_RESOURCE :
				recreatedResources.add(event.getResource());
		}				
	}
	
	private IContainer[] getParents(Set files) {
		Set parents = new HashSet();
		for (Iterator iter = files.iterator(); iter.hasNext();) {
			IFile file = (IFile) iter.next();
			parents.add(file.getParent());
		}
		return (IContainer[]) parents.toArray(new IContainer[parents.size()]);
	}

	public void ignoreFileChanged(IFile file) {
		queueEvent(new Event(file, IGNORE_FILE_CHANGED, IResource.DEPTH_ZERO), false);
	}
	
	/**
	 * @param resource
	 */
	public void recreated(IResource resource) {
		queueEvent(new Event(resource, RECREATED_CVS_RESOURCE, IResource.DEPTH_ZERO), false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.BackgroundEventHandler#dispatchEvents()
	 */
	protected void dispatchEvents(IProgressMonitor monitor) throws TeamException {
		EclipseSynchronizer.getInstance().ignoreFilesChanged(getParents(changedIgnoreFiles));
		changedIgnoreFiles.clear();
		EclipseSynchronizer.getInstance().resourcesRecreated((IResource[]) recreatedResources.toArray(new IResource[recreatedResources.size()]), monitor);
		recreatedResources.clear();
	}
}
