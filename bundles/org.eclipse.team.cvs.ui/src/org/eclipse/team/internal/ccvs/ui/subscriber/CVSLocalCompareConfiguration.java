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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SubscriberSyncInfoCollector;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.ui.synchronize.subscriber.RefreshAction;
import org.eclipse.team.ui.synchronize.viewers.TreeViewerAdvisor;

/**
 * Provides compare specific support
 */
public class CVSLocalCompareConfiguration extends TreeViewerAdvisor {

	private CVSCompareSubscriber subscriber;
	private SubscriberSyncInfoCollector collector;
	private RefreshAction refreshAction;
	private RefreshAction refreshAllAction;

	/**
	 * Return a <code>SyncInfoSetCompareConfiguration</code> that can be used in a
	 * <code>SynchronizeCompareInput</code> to show the comparsion between the local
	 * workspace resources and their tagged counterparts on the server.
	 * @param resources the resources to be compared
	 * @param tag the tag to be compared with
	 * @return a configuration for a <code>SynchronizeCompareInput</code>
	 */
	public static CVSLocalCompareConfiguration create(IResource[] resources, CVSTag tag) {
		CVSCompareSubscriber subscriber = new CVSCompareSubscriber(resources, tag);
		SubscriberSyncInfoCollector collector = new SubscriberSyncInfoCollector(subscriber);
		collector.setFilter(new SyncInfoFilter() {
			private SyncInfoFilter contentCompare = new SyncInfoFilter.ContentComparisonSyncInfoFilter();
			public boolean select(SyncInfo info, IProgressMonitor monitor) {
				if (info.getLocal().getType() == IResource.FILE) {
					// Want to select infos whose contents do not match
					return !contentCompare.select(info, monitor);
				} else {
					return true;
				}
			}
		});
		collector.start();
		return new CVSLocalCompareConfiguration(subscriber, collector);
	}
	
	private CVSLocalCompareConfiguration(CVSCompareSubscriber subscriber, SubscriberSyncInfoCollector collector) {
		super("org.eclipse.team.cvs.ui.compare-participant", null, collector.getSyncInfoTree()); //$NON-NLS-1$
		this.subscriber = subscriber;
		this.collector = collector;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SyncInfoSetCompareConfiguration#dispose()
	 */
	public void dispose() {
		collector.dispose();
		subscriber.dispose();
		super.dispose();
	}
	
	public Object prepareInput(IProgressMonitor monitor) throws TeamException {
		subscriber.refresh(subscriber.roots(), IResource.DEPTH_INFINITE, monitor);
		collector.waitForCollector(monitor);
		return super.prepareInput(monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SyncInfoSetCompareConfiguration#fillContextMenu(org.eclipse.jface.viewers.StructuredViewer, org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenu(StructuredViewer viewer, IMenuManager manager) {
		manager.add(refreshAction);
		manager.add(new Separator());
		super.fillContextMenu(viewer, manager);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.TreeViewerAdvisor#contributeToToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	public void contributeToToolBar(IToolBarManager tbm) {
		tbm.add(refreshAllAction);
	}

	protected void initializeActions(StructuredViewer viewer) {
		super.initializeActions(viewer);
		refreshAction = new RefreshAction(viewer, ((CVSSyncTreeSubscriber)collector.getSubscriber()).getName(), collector, null /* no listener */, false);
		refreshAllAction = new RefreshAction(viewer, ((CVSSyncTreeSubscriber)collector.getSubscriber()).getName(), collector, null /* no listener */, true);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SyncInfoSetCompareConfiguration#getSyncSet()
	 */
	public SyncInfoTree getSyncInfoTree() {
		return collector.getSyncInfoTree();
	}
}
