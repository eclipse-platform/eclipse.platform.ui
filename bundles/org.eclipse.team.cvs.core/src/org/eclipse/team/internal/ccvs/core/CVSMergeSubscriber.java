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
package org.eclipse.team.internal.ccvs.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.ContentComparisonCriteria;
import org.eclipse.team.core.subscribers.TeamProvider;
import org.eclipse.team.internal.ccvs.core.syncinfo.RemoteSynchronizer;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSynchronizer;
import org.eclipse.team.internal.core.SaveContext;

/**
 * A CVSMergeSubscriber is responsible for maintaining the remote trees for a merge into
 * the workspace. The remote trees represent the CVS revisions of the start and end
 * points (version or branch) of the merge.
 * 
 * This subscriber stores the remote handles in the resource tree sync info slot. When
 * the merge is cancelled this sync info is cleared.
 * 
 * A merge can persist between workbench sessions and thus can be used as an
 * ongoing merge.
 * 
 * TODO: Is the merge subscriber interested in workspace sync info changes?
 * TODO: Do certain operations (e.g. replace with) invalidate a merge subscriber?
 * TODO: How to ensure that sync info is flushed when projects are deleted?
 */
public class CVSMergeSubscriber extends CVSSyncTreeSubscriber {

	public static final String UNIQUE_ID_PREFIX = "merge-";
	
	private CVSTag start, end;
	private IResource[] roots;
	private RemoteSynchronizer remoteSynchronizer;
	private RemoteSynchronizer baseSynchronizer;

	private static QualifiedName getUniqueId() {
		String uniqueId = Long.toString(System.currentTimeMillis());
		return new QualifiedName(CVSSubscriberFactory.ID, UNIQUE_ID_PREFIX + uniqueId);
	}
	
	public CVSMergeSubscriber(IResource[] roots, CVSTag start, CVSTag end) {		
		this(getUniqueId(), roots, start, end);
	}
	
	public CVSMergeSubscriber(QualifiedName id, IResource[] roots, CVSTag start, CVSTag end) {		
		super(id, "CVS Merge: " + start.getName() + " to " + end.getName(), "CVS Merge");
		this.start = start;
		this.end = end;
		this.roots = roots;
		initialize();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.CVSWorkspaceSubscriber#initialize()
	 */
	private void initialize() {				
		QualifiedName id = getId();
		String syncKeyPrefix = id.getLocalName();
		remoteSynchronizer = new RemoteSynchronizer(syncKeyPrefix + end.getName(), end);
		baseSynchronizer = new RemoteSynchronizer(syncKeyPrefix + start.getName(), start);
		
		try {
			setCurrentComparisonCriteria(ContentComparisonCriteria.ID_IGNORE_WS);
		} catch (TeamException e) {
			// use the default but log an exception because the content comparison should
			// always be available.
			CVSProviderPlugin.log(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.TeamSubscriber#cancel()
	 */
	public void cancel() {
		super.cancel();
		TeamProvider.deregisterSubscriber(this);
		remoteSynchronizer.dispose();
		baseSynchronizer.dispose();				
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.TeamSubscriber#isCancellable()
	 */
	public boolean isCancellable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.TeamSubscriber#roots()
	 */
	public IResource[] roots() throws TeamException {
		return roots;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.CVSSyncTreeSubscriber#getRemoteSynchronizer()
	 */
	protected ResourceSynchronizer getRemoteSynchronizer() {
		return remoteSynchronizer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.CVSSyncTreeSubscriber#getBaseSynchronizer()
	 */
	protected ResourceSynchronizer getBaseSynchronizer() {
		return baseSynchronizer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.TeamSubscriber#isSupervised(org.eclipse.core.resources.IResource)
	 */
	public boolean isSupervised(IResource resource) throws TeamException {
		return getBaseSynchronizer().getSyncBytes(resource) != null || getRemoteSynchronizer().getSyncBytes(resource) != null; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.TeamSubscriber#saveState(org.eclipse.team.internal.core.SaveContext)
	 */
	public SaveContext saveState() {
		// start and end tags
		SaveContext state = new SaveContext();
		state.setName("merge");
		state.putString("startTag", start.getName());
		state.putInteger("startTagType", start.getType());
		state.putString("endTag", end.getName());
		state.putInteger("endTagType", end.getType());
		
		// resources roots
		SaveContext[] ctxRoots = new SaveContext[roots.length];
		for (int i = 0; i < roots.length; i++) {
			ctxRoots[i] = new SaveContext();
			ctxRoots[i].setName("resource");
			ctxRoots[i].putString("fullpath", roots[i].getFullPath().toString());			
		}
		state.setChildren(ctxRoots);
		return state;
	}
	
	public static CVSMergeSubscriber restore(QualifiedName id, SaveContext saveContext) throws CVSException {
		String name = saveContext.getName(); 
		if(! name.equals("merge")) {
			throw new CVSException("error restoring merge subscriber: " + name + " is an invalid save context.");
		}
		
		CVSTag start = new CVSTag(saveContext.getString("startTag"), saveContext.getInteger("startTagType"));
		CVSTag end = new CVSTag(saveContext.getString("endTag"), saveContext.getInteger("endTagType"));
		
		SaveContext[] ctxRoots = saveContext.getChildren();
		if(ctxRoots == null || ctxRoots.length == 0) {
			throw new CVSException("error restoring merge subscriber: there are no roots in the save context.");
		}
		
		List resources = new ArrayList();
		for (int i = 0; i < ctxRoots.length; i++) {
			SaveContext context = ctxRoots[i];
			IPath path = new Path(context.getString("fullpath"));
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path, true /* include phantoms */);
			if(resource != null) {
				resources.add(resource);
			} else {
				// log that a resource previously in the merge set is no longer in the workspace
				CVSProviderPlugin.log(new CVSStatus(CVSStatus.INFO, "ignoring root resource not found in current workspace"));
			}
		}
		if(resources.isEmpty()) {
			throw new CVSException("error restoring merge subscriber: there are no existing roots in the save context.");
		}
		IResource[] roots = (IResource[]) resources.toArray(new IResource[resources.size()]);
		return new CVSMergeSubscriber(id, roots, start, end);
	}
	
	public CVSTag getStartTag() {
		return start;
	}
	
	public CVSTag getEndTag() {
		return end;
	}
	
	public boolean isReleaseSupported() {
		// you can't release changes to a merge
		return false;
	}
}