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

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.internal.ccvs.ui.subscriber.OverrideAndCommitAction;
import org.eclipse.team.ui.sync.SyncInfoSet;

class TestCommitAction extends OverrideAndCommitAction {

	public IRunnableWithProgress getRunnable(SyncInfoSet syncSet) {
		return super.getRunnable(syncSet);
	}
	
	protected boolean canRunAsJob() {
		return false;
	}
}
