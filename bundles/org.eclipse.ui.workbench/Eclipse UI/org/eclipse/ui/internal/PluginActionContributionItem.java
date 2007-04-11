/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.IIdentifierListener;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activities.IdentifierEvent;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * Contribution item for actions provided by plugins via workbench action
 * extension points.
 */
public class PluginActionContributionItem extends ActionContributionItem
        implements IIdentifierListener, IActivityManagerListener {

    private IIdentifier identifier = null;

    /**
     * Creates a new contribution item from the given action. The id of the
     * action is used as the id of the item.
     * 
     * @param action
     *            the action
     */
    public PluginActionContributionItem(PluginAction action) {
        // dynamic UI (DDW) - this constructor has changed since 1113
        super(action);
    }

    /**
     * Hook the activity and identifier listener (if necessary);
     * 
     * @since 3.1
     */
    private void hookListeners() {
        PlatformUI.getWorkbench().getActivitySupport().getActivityManager()
                .addActivityManagerListener(this);
        // set up the identifier if necessary
        IIdentifier id = getIdentifier();
        if (id != null) {
			id.addIdentifierListener(this);
		}
    }
    
    /**
     * Unhook the activity and identifier listener (if necessary);
     * 
     * @since 3.1
     */
    private void unhookListeners() {
        PlatformUI.getWorkbench().getActivitySupport().getActivityManager()
                .removeActivityManagerListener(this);

        IIdentifier id = getIdentifier();
        if (id != null) {
			id.removeIdentifierListener(this);
		}
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#setParent(org.eclipse.jface.action.IContributionManager)
     */
    public void setParent(IContributionManager parent) {
        IContributionManager oldParent = getParent();
        super.setParent(parent);
        if (oldParent == parent) {
			return;
		}
        
        if (parent == null) {
			unhookListeners();
		} else {
			hookListeners();
		}
    }
    
    /**
     * Create the IIdentifier reference for this item.
     *
     * @since 3.0
     */
    private IIdentifier getIdentifier() {
        if (!WorkbenchActivityHelper.isFiltering()) {
			return null;
		}
        
        if (identifier == null) {
            IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI
                    .getWorkbench().getActivitySupport();
            IPluginContribution contribution = (IPluginContribution) getAction();
            // no need to check if contribution.getPluginId() == null - plugin
            // actions are always from plugins.
            identifier = workbenchActivitySupport.getActivityManager()
                    .getIdentifier(
                            WorkbenchActivityHelper
                                    .createUnifiedId(contribution));
        }
        return identifier;
    }

    /**
     * Dispose of the IIdentifier if necessary.
     * 
     * @since 3.0
     */
    private void disposeIdentifier() {
        identifier = null;
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * method notifies the delegate if loaded and implements the <code>IActionDelegate2</code>
     * interface.
     */
    public void dispose() {
        unhookListeners();
        disposeIdentifier();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.ActionContributionItem#isVisible()
     */
    public boolean isVisible() {
        if (identifier != null && !identifier.isEnabled()) {
			return false;
		}
        return super.isVisible();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.activities.IIdentifierListener#identifierChanged(org.eclipse.ui.activities.IdentifierEvent)
     */
    public void identifierChanged(IdentifierEvent identifierEvent) {
        invalidateParent();
    }

    /**
     * Mark the parent dirty if we have a parent.
     * 
     * @since 3.1
     */
    private void invalidateParent() {
        IContributionManager parent = getParent();
        if (parent != null) {
			parent.markDirty();
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.activities.IActivityManagerListener#activityManagerChanged(org.eclipse.ui.activities.ActivityManagerEvent)
     */
    public void activityManagerChanged(ActivityManagerEvent activityManagerEvent) {
        // ensure that if we're going from a non-filtering state that we get an identifier
        // and vice versa.
        if (WorkbenchActivityHelper.isFiltering() && identifier == null) {
            hookListeners();
            invalidateParent();
        } else if (!WorkbenchActivityHelper.isFiltering() && identifier != null) {
            unhookListeners();
            disposeIdentifier();
            invalidateParent();
        }
    }
    
    /*
     * For testing purposes only
     */
    public ISelection getSelection() {
    	return ((PluginAction)getAction()).getSelection();
    }
}
