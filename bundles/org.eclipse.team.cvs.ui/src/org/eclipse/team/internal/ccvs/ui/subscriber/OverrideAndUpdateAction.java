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

/**
 * Runs an update command that will prompt the user for overwritting local
 * changes to files that have non-mergeable conflicts. All the prompting logic
 * is in the super class.
 */
public class OverrideAndUpdateAction extends WorkspaceUpdateAction {
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SafeUpdateAction#getOverwriteLocalChanges()
	 */
	protected boolean getOverwriteLocalChanges() {
		// allow overriding of local changes with this update
		return true;
	}
}
