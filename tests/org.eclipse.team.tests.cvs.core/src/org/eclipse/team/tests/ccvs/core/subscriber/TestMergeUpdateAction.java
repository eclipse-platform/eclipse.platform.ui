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
package org.eclipse.team.tests.ccvs.core.subscriber;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.internal.ccvs.ui.subscriber.MergeUpdateAction;
import org.eclipse.team.tests.ccvs.core.EclipseTest;
import org.eclipse.team.ui.sync.SyncInfoSet;


class TestMergeUpdateAction extends MergeUpdateAction {
	boolean allowOverwrite = false;

	public TestMergeUpdateAction(boolean allowOverwrite) {
		this.allowOverwrite = allowOverwrite;
	}

	protected boolean promptForOverwrite(SyncInfoSet syncSet) {
		if (allowOverwrite) return true;
		if (syncSet.isEmpty()) return true;
		IResource[] resources = syncSet.getResources();
		EclipseTest.fail(resources[0].getFullPath().toString() + " failed to merge properly");
		return false;
	}
	
	public IRunnableWithProgress getRunnable(SyncInfoSet syncSet) {
		return super.getRunnable(syncSet);
	}

}