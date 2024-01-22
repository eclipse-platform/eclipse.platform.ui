/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.HashSet;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.ui.PlatformUI;

/**
 * Contribution item for actions provided by plugins via workbench action
 * extension points.
 */
public class PluginActionCoolBarContributionItem extends PluginActionContributionItem
		implements IActionSetContributionItem {
	private String actionSetId;

	/**
	 * Creates a new contribution item from the given action. The id of the action
	 * is used as the id of the item.
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

	private static HashSet<IContributionManager> managersToUpdate = new HashSet<>();
	private static Runnable updater = () -> {
		IContributionManager[] managers = managersToUpdate.toArray(new IContributionManager[managersToUpdate.size()]);
		managersToUpdate.clear();
		queued = false;
		for (IContributionManager manager : managers) {
			manager.update(false);
		}
	};
	private static boolean queued = false;
}
