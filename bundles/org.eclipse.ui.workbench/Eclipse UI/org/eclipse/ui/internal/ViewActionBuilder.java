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

import java.util.ArrayList;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * This class reads the registry for extensions that plug into
 * 'viewActions' extension point.
 */
public class ViewActionBuilder extends PluginActionBuilder {
    public static final String TAG_CONTRIBUTION_TYPE = "viewContribution"; //$NON-NLS-1$

    private IViewPart targetPart;

    /**
     * Basic constructor
     */
    public ViewActionBuilder() {
    }

    /**
     * Contribute the external menus and actions applicable for this view part.
     */
    private void contributeToPart(IViewPart part) {
        IActionBars bars = part.getViewSite().getActionBars();
        contribute(bars.getMenuManager(), bars.getToolBarManager(), true);
    }

    /* (non-Javadoc)
     * Method declared on PluginActionBuilder.
     */
    protected ActionDescriptor createActionDescriptor(
            org.eclipse.core.runtime.IConfigurationElement element) {
        return new ActionDescriptor(element, ActionDescriptor.T_VIEW,
                targetPart);
    }

    /**
     * Return all extended actions.
     */
    public ActionDescriptor[] getExtendedActions() {
        if (cache == null) {
			return new ActionDescriptor[0];
		}

        ArrayList results = new ArrayList();
        for (int i = 0; i < cache.size(); i++) {
            BasicContribution bc = (BasicContribution) cache.get(i);
            if (bc.actions != null) {
				results.addAll(bc.actions);
			}
        }
        return (ActionDescriptor[]) results
                .toArray(new ActionDescriptor[results.size()]);
    }

    /**
     * Reads and apply all external contributions for this view's ID registered
     * in 'viewActions' extension point.
     */
    public void readActionExtensions(IViewPart viewPart) {
        targetPart = viewPart;
        readContributions(viewPart.getSite().getId(), TAG_CONTRIBUTION_TYPE,
                IWorkbenchRegistryConstants.PL_VIEW_ACTIONS);
        contributeToPart(targetPart);
    }
    
    public void dispose() {
		if (cache != null) {
			for (int i = 0; i < cache.size(); i++) {
				((BasicContribution) cache.get(i)).disposeActions();
			}
		}
	}
}
