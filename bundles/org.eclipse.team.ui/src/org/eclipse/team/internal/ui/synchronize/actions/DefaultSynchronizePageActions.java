/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.team.internal.ui.synchronize.SynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizePageSite;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchSite;

/**
 * General synchronize page actions
 */
public class DefaultSynchronizePageActions extends SynchronizePageActionGroup {
	
	// Actions
	private OpenWithActionGroup openWithActions;
	private RefactorActionGroup refactorActions;
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.IActionContribution#initialize(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	public void initialize(ISynchronizePageConfiguration configuration) {
		super.initialize(configuration);
		ISynchronizePageSite site = configuration.getSite();
		IWorkbenchSite ws = site.getWorkbenchSite();
		if (ws instanceof IViewSite) {
			openWithActions = new OpenWithActionGroup(site, configuration.getParticipant());
			refactorActions = new RefactorActionGroup(site);
			configuration.setProperty(SynchronizePageConfiguration.P_OPEN_ACTION, new Action() {
				public void run() {
					openWithActions.openInCompareEditor();
				}
			});
		} else {
			// TODO: Add open menu action which opens in compare editor input
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.IActionContribution#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager manager) {
		IContributionItem group = findGroup(manager, ISynchronizePageConfiguration.FILE_GROUP);
		if (openWithActions != null && group != null) {
			openWithActions.fillContextMenu(manager, group.getId());
		}
		group = findGroup(manager, ISynchronizePageConfiguration.EDIT_GROUP);
		if (refactorActions != null && group != null) {
			refactorActions.fillContextMenu(manager, group.getId());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#dispose()
	 */
	public void dispose() {
		if (refactorActions != null) refactorActions.dispose();
		if (openWithActions != null) openWithActions.dispose();
		super.dispose();
	}
}
