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
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.decorators.LightweightActionDescriptor;
import org.eclipse.ui.internal.registry.ActionSetDescriptor;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * Read the actions for an plugin action set.
 *
 * [Issue: There is some overlap with the class
 *		PluginActionSetBuilder which should be reviewed
 *		at a later time and maybe merged together]
 */
public class PluginActionSetReader extends RegistryReader {
    private List cache = new ArrayList();

    /**
     * PluginActionSetReader constructor comment.
     */
    public PluginActionSetReader() {
        super();
    }

    /**
     * This factory method returns a new ActionDescriptor for the
     * configuration element.  
     */
    protected LightweightActionDescriptor createActionDescriptor(
            IConfigurationElement element) {
        return new LightweightActionDescriptor(element);
    }

    /**
     * Return all the action descriptor within the set.
     * 
     * @param actionSet the set
     * @return the descriptors
     */
    public LightweightActionDescriptor[] readActionDescriptors(
            ActionSetDescriptor actionSet) {
        readElements(new IConfigurationElement[] { actionSet.getConfigurationElement() });
        LightweightActionDescriptor[] actions = new LightweightActionDescriptor[cache
                .size()];
        cache.toArray(actions);
        return actions;
    }

    /**
     * @see RegistryReader
     */
    protected boolean readElement(IConfigurationElement element) {
        String tag = element.getName();
        if (tag.equals(IWorkbenchRegistryConstants.TAG_ACTION_SET)) {
            readElementChildren(element);
            return true;
        }
        if (tag.equals(IWorkbenchRegistryConstants.TAG_OBJECT_CONTRIBUTION)) {
            // This builder is sometimes used to read the popup menu
            // extension point.  Ignore all object contributions.
            return true;
        }
        if (tag.equals(IWorkbenchRegistryConstants.TAG_MENU)) {
            return true; // just cache the element - don't go into it
        }
        if (tag.equals(IWorkbenchRegistryConstants.TAG_ACTION)) {
            cache.add(createActionDescriptor(element));
            return true; // just cache the action - don't go into
        }

        return false;
    }
}
