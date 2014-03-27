/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.HashSet;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.ui.PlatformUI;

/**
 * Contribution item for actions provided by plugins via workbench
 * action extension points.
 */
public class PluginActionCoolBarContributionItem extends
        PluginActionContributionItem implements IActionSetContributionItem {
    private String actionSetId;

    /**
     * Creates a new contribution item from the given action.
     * The id of the action is used as the id of the item.
     *
     * @param action the action
     */
    public PluginActionCoolBarContributionItem(PluginAction action) {
        super(action);
        setActionSetId(((WWinPluginAction) action).getActionSetId());
    }

    @Override
	public String getActionSetId() {
        return actionSetId;
    }

    @Override
	public void setActionSetId(String id) {
        this.actionSetId = id;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.PluginActionContributionItem#invalidateParent()
	 */
	@Override
	protected void invalidateParent() {
		super.invalidateParent();
		IContributionManager parent = getParent();
		if (parent != null && managersToUpdate.add(parent)) {
			if (!queued) {
				queued = true;
				PlatformUI.getWorkbench().getDisplay().asyncExec(updater);
			}
		}
	}

	private static Runnable updater = new Runnable() {
		@Override
		public void run() {
			IContributionManager[] managers = managersToUpdate
					.toArray(new IContributionManager[managersToUpdate.size()]);
			managersToUpdate.clear();
			queued = false;
			for (IContributionManager manager : managers) {
				manager.update(false);
			}
		}
	};
	private static HashSet<IContributionManager> managersToUpdate = new HashSet<IContributionManager>();
	private static boolean queued = false;
}
