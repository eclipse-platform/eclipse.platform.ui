/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;
 
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
