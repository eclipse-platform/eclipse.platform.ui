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

package org.eclipse.ui.internal;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.ui.IActionDelegate2;

/**
 * Contribution item for actions provided by plugins via workbench
 * action extension points.
 */
public class PluginActionContributionItem extends ActionContributionItem {

	/**
	 * Creates a new contribution item from the given action.
	 * The id of the action is used as the id of the item.
	 *
	 * @param action the action
	 */
	public PluginActionContributionItem(PluginAction action) {
		super(action);
	}

	/**
	 * The default implementation of this <code>IContributionItem</code>
	 * method notifies the delegate if loaded and implements the
	 * <code>IActionDelegate2</code> interface.
	 */
	public void dispose() {
		PluginAction proxy = (PluginAction)getAction();
		if (proxy != null) {
			if (proxy.getDelegate() instanceof IActionDelegate2) {
				((IActionDelegate2)proxy.getDelegate()).dispose();
			}
		}
	}
}
