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
package org.eclipse.team.tests.ccvs.ui.sync;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.ui.sync.views.SubscriberInput;
import org.eclipse.team.internal.ui.sync.views.SyncSet;
import org.eclipse.team.internal.ui.sync.views.SyncViewer;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.eclipse.team.tests.ccvs.core.subscriber.CVSWorkspaceSubscriberTest;

public class SyncViewTests extends CVSWorkspaceSubscriberTest {

	private SyncViewer syncView;

	public SyncViewTests() {
		super();
	}

	public SyncViewTests(String name) {
		super(name);
	}

	public static Test suite() {
		String testName = System.getProperty("eclipse.cvs.testName");
		if (testName == null) {
			TestSuite suite = new TestSuite(SyncViewTests.class);
			return new CVSTestSetup(suite);
		} else {
			return new CVSTestSetup(new SyncViewTests(testName));
		}
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		// show the sync view
		syncView = (SyncViewer)SyncViewer.showInActivePage(null);
		// ensure that the CVS subscriber is active
		syncView.activateSubscriber(getSubscriber());
	}
	
	/* (non-Javadoc)
	 * 
	 * The shareProject method is invoked when creating new projects.
	 * @see org.eclipse.team.tests.ccvs.core.EclipseTest#shareProject(org.eclipse.core.resources.IProject)
	 */
	protected void shareProject(final IProject project) throws TeamException, CoreException {
		mapNewProject(project);
		// Everything should be outgoing addition except he project
		project.accept(new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (resource.getType() == IResource.PROJECT) {
					assertSyncEquals(project.getName(), getSubscriber(), resource, SyncInfo.IN_SYNC);
				} else {
					assertSyncEquals(project.getName(), getSubscriber(), resource, SyncInfo.OUTGOING | SyncInfo.ADDITION);
				}
				return true;
			}
		});
		commitNewProject(project);
		// Everything should be in-sync
		project.accept(new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				assertSyncEquals(project.getName(), getSubscriber(), resource, SyncInfo.IN_SYNC);
				return true;
			}
		});
	}
	
	/* (non-Javadoc)
	 * 
	 * Overriding this methd changes how the superclass obtains the sync info for a resource
	 * 
	 * @see org.eclipse.team.tests.ccvs.core.subscriber.CVSSyncSubscriberTest#getSyncInfo(org.eclipse.team.core.subscribers.TeamSubscriber, org.eclipse.core.resources.IResource)
	 */
	protected SyncInfo getSyncInfo(TeamSubscriber subscriber, IResource resource) throws TeamException {
		assertTrue(subscriber == getSubscriber());
		SubscriberInput input = syncView.getInput();
		assertTrue(subscriber == input.getSubscriber());
		SyncSet set = input.getFilteredSyncSet();
		SyncInfo info = set.getSyncInfo(resource);
		if (info == null) {
			info = subscriber.getSyncInfo(resource, DEFAULT_MONITOR);
			assertTrue(info == null || info.getKind() == SyncInfo.IN_SYNC);
		}
		return info;
	}
}
