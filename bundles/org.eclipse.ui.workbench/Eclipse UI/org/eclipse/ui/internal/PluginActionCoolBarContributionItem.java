/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

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

    public String getActionSetId() {
        return actionSetId;
    }

    public void setActionSetId(String id) {
        this.actionSetId = id;
    }

}
