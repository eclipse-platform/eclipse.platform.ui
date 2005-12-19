/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.compare.internal.INavigatable;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.actions.ExpandAllAction;
import org.eclipse.team.internal.ui.synchronize.actions.NavigateAction;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.IActionBars;

/**
 * Action group that provide expand, collapse and naviGAtion atCions.
 */
public class NavigationActionGroup extends SynchronizePageActionGroup {

	private ExpandAllAction expandAllAction;
	private Action collapseAll;
	private NavigateAction gotoNext;
	private NavigateAction gotoPrevious;
	
	public void initialize(ISynchronizePageConfiguration configuration) {
		super.initialize(configuration);
		final Viewer viewer = configuration.getPage().getViewer();
		if (viewer instanceof AbstractTreeViewer) {
			
			expandAllAction = new ExpandAllAction((AbstractTreeViewer) viewer);
			Utils.initAction(expandAllAction, "action.expandAll."); //$NON-NLS-1$
			
			collapseAll = new Action() {
				public void run() {
					if (viewer == null || viewer.getControl().isDisposed() || !(viewer instanceof AbstractTreeViewer)) return;
					viewer.getControl().setRedraw(false);		
					((AbstractTreeViewer)viewer).collapseToLevel(viewer.getInput(), AbstractTreeViewer.ALL_LEVELS);
					viewer.getControl().setRedraw(true);
				}
			};
			Utils.initAction(collapseAll, "action.collapseAll."); //$NON-NLS-1$
			
			INavigatable nav = (INavigatable)configuration.getProperty(SynchronizePageConfiguration.P_NAVIGATOR);
			if (nav != null) {
				ISynchronizeParticipant participant = configuration.getParticipant();
				ISynchronizePageSite site = configuration.getSite();
	
				gotoNext = new NavigateAction(site, participant, configuration, true /*next*/);		
				gotoPrevious = new NavigateAction(site, participant, configuration, false /*previous*/);
			}
		}
	}
	public void fillContextMenu(IMenuManager manager) {
		appendToGroup(manager, ISynchronizePageConfiguration.NAVIGATE_GROUP, expandAllAction);
	}
	public void fillActionBars(IActionBars actionBars) {
		IToolBarManager manager = actionBars.getToolBarManager();
		if (gotoNext != null)
			appendToGroup(manager, ISynchronizePageConfiguration.NAVIGATE_GROUP, gotoNext);
		if (gotoPrevious != null)
			appendToGroup(manager, ISynchronizePageConfiguration.NAVIGATE_GROUP, gotoPrevious);
		appendToGroup(manager, ISynchronizePageConfiguration.NAVIGATE_GROUP, collapseAll);
	}
}