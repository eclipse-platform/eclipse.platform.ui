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
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.registry.ActionSetDescriptor;
import org.eclipse.ui.internal.registry.IActionSet;

/**
 * A PluginActionSet is a proxy for an action set defined in XML.
 * It creates a PluginAction for each action and does the required
 * cleanup on dispose.
 */
public class PluginActionSet implements IActionSet {
    private ActionSetDescriptor desc;

    private ArrayList pluginActions = new ArrayList(4);

    private ActionSetActionBars bars;

    /**
     * PluginActionSet constructor comment.
     * 
     * @param desc the descriptor
     */
    public PluginActionSet(ActionSetDescriptor desc) {
        super();
        this.desc = desc;
    }

    /**
     * Adds one plugin action ref to the list.
     * 
     * @param action the action
     */
    public void addPluginAction(WWinPluginAction action) {
        pluginActions.add(action);
    }

    /**
     * Returns the list of plugin actions for the set.
     * 
     * @return the actions for the set
     */
    public IAction[] getPluginActions() {
        IAction result[] = new IAction[pluginActions.size()];
        pluginActions.toArray(result);
        return result;
    }

    /**
     * Disposes of this action set.
     */
    public void dispose() {
        Iterator iter = pluginActions.iterator();
        while (iter.hasNext()) {
            WWinPluginAction action = (WWinPluginAction) iter.next();
            action.dispose();
        }
        pluginActions.clear();
        bars = null;
    }

    /**
     */
    /* package */ActionSetActionBars getBars() {
        return bars;
    }

    /**
     * Returns the configuration element.
     * 
     * @return the configuration element
     */
    public IConfigurationElement getConfigElement() {
        return desc.getConfigurationElement();
    }

    /**
     * Returns the underlying descriptor.
     * 
     * @return the descriptor
     */
    public ActionSetDescriptor getDesc() {
        return desc;
    }

    /**
     * Initializes this action set, which is expected to add it actions as required
     * to the given workbench window and action bars.
     *
     * @param window the workbench window
     * @param bars the action bars
     */
    public void init(IWorkbenchWindow window, IActionBars bars) {
        this.bars = (ActionSetActionBars) bars;
    }
}
