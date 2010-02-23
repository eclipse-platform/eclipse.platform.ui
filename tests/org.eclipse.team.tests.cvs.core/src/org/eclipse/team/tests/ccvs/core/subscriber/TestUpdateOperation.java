/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.core.subscriber;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.mapping.ISynchronizationScopeManager;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.operations.UpdateOnlyMergableOperation;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceUpdateOperation;
import org.eclipse.team.internal.ui.mapping.BuildScopeOperation;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

class TestUpdateOperation extends WorkspaceUpdateOperation {

	protected TestUpdateOperation(IDiffElement[] elements) {
		super(null, elements, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.actions.TeamOperation#canRunAsJob()
	 */
	protected boolean canRunAsJob() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SafeUpdateOperation#warnAboutFailedResources(org.eclipse.team.core.synchronize.SyncInfoSet)
	 */
	protected void warnAboutFailedResources(SyncInfoSet syncSet) {
		return;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.CVSSubscriberOperation#promptForOverwrite(org.eclipse.team.core.synchronize.SyncInfoSet)
	 */
	protected boolean promptForOverwrite(SyncInfoSet syncSet) {
		EclipseTest.fail("Should never prompt on update, simply update nodes that are valid.");
		return false;
	}
	
	protected UpdateOnlyMergableOperation createUpdateOnlyMergableOperation(
			IProject project, IResource[] resources, LocalOption[] localOptions) {
		return new UpdateOnlyMergableOperation(getPart(), project, resources, localOptions) {
			protected BuildScopeOperation createBuildScopeOperation(ISynchronizationScopeManager manager) {
				return new BuildScopeOperation(getPart(), manager) {
					protected boolean promptForInputChange(String requestPreviewMessage, IProgressMonitor monitor) {
						return false;
					}
				};
			}
		};
	}
}
