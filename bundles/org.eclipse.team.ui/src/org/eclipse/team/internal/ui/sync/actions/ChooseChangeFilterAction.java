/*
 * Created on Jul 11, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.team.internal.ui.sync.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.team.internal.ui.Utils;


class ChooseChangeFilterAction extends SyncViewerToolbarDropDownAction {
	private final SyncViewerActions actions;
	private SyncViewerChangeFilters filters;
	public void run() {		
		Action[] enabled = filters.getActiveFilters();
		Action[] actions = filters.getFilters();
		if(actions.length != enabled.length) {
			filters.setAllEnabled();
			this.actions.refreshFilters();
		}
	}		
	public ChooseChangeFilterAction(SyncViewerActions actions, SyncViewerChangeFilters filters) {
		super(filters);
		this.actions = actions;
		this.filters = filters;
		Utils.initAction(this, "action.changeFilters."); //$NON-NLS-1$
	}		
}