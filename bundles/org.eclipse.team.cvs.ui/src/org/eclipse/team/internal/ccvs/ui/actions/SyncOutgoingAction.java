package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;
import org.eclipse.team.internal.ccvs.ui.sync.CVSSyncCompareInput;
import org.eclipse.team.internal.ui.sync.SyncCompareInput;

/**
 * Action for catchup/release in popup menus.
 */
public class SyncOutgoingAction extends SyncAction {
	protected SyncCompareInput getCompareInput(IResource[] resources) {
		return new CVSSyncCompareInput(resources, true);
	}
}
