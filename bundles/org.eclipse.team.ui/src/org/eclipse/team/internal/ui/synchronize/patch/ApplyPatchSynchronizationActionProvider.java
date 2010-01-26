/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize.patch;

import org.eclipse.team.ui.mapping.SynchronizationActionProvider;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public class ApplyPatchSynchronizationActionProvider extends
		SynchronizationActionProvider {

	public ApplyPatchSynchronizationActionProvider() {
		super();
	}

	protected void initialize() {
		super.initialize();
		final ISynchronizePageConfiguration configuration = getSynchronizePageConfiguration();
		// We provide custom handler that is disabled for hunks.
		registerHandler(MERGE_ACTION_ID, new ApplyPatchMergeActionHandler(
				configuration, false));
		// 'Overwrite' and 'Mark as merged' actions are not shown, see
		// ApplyPatchModelSynchronizeParticipant.ApplyPatchModelSynchronizeParticipantActionGroup.addToContextMenu(String,
		// Action, IMenuManager)
	}
}
