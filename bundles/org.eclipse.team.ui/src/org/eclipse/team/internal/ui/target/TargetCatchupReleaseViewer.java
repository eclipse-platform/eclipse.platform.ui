/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ui.target;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.IHelpContextIds;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.sync.CatchupReleaseViewer;
import org.eclipse.team.internal.ui.sync.SyncView;
import org.eclipse.ui.help.WorkbenchHelp;
public class TargetCatchupReleaseViewer extends CatchupReleaseViewer {
	private GetSyncAction getAction;
	private PutSyncAction putAction;
	
	public TargetCatchupReleaseViewer(Composite parent, TargetSyncCompareInput input) {
		super(parent, input);
		initializeActions(input);
		// set F1 help
		WorkbenchHelp.setHelp(this.getControl(), IHelpContextIds.CATCHUP_RELEASE_VIEWER);
	}
	
	/**
	 * Creates the actions for this viewer.
	 */
	private void initializeActions(final TargetSyncCompareInput diffModel) {
		Shell shell = getControl().getShell();
		getAction = new GetSyncAction(diffModel, this, Policy.bind("TargetCatchupReleaseViewer.Get_1"), shell); //$NON-NLS-1$
		putAction = new PutSyncAction(diffModel, this, Policy.bind("TargetCatchupReleaseViewer.Put_2"), shell); //$NON-NLS-1$
	}
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.add(new Separator());
		switch (getSyncMode()) {
			case SyncView.SYNC_INCOMING:
				getAction.update(SyncView.SYNC_INCOMING);
				manager.add(getAction);
				break;
			case SyncView.SYNC_OUTGOING:
				putAction.update(SyncView.SYNC_INCOMING);
				manager.add(putAction);
				break;
			case SyncView.SYNC_BOTH:
				getAction.update(SyncView.SYNC_INCOMING);
				manager.add(getAction);
				putAction.update(SyncView.SYNC_INCOMING);
				manager.add(putAction);
				break;
		}
	}
}
