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

import java.util.Collection;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IObjectActivityManager;
import org.eclipse.ui.activities.IObjectContributionRecord;

/**
 * Contribution item for actions provided by plugins via workbench
 * action extension points.
 */
public class PluginActionContributionItem extends ActionContributionItem {

	private static final String PLUGIN_CONTRIBUTION_ITEM = "PLUGIN_CONTRIBUTION_ITEM"; //$NON-NLS-1$
    /**
	 * Creates a new contribution item from the given action.
	 * The id of the action is used as the id of the item.
	 *
	 * @param action the action
	 */
	public PluginActionContributionItem(PluginAction action) {
		super(action);
        
        IObjectActivityManager objectManager = PlatformUI.getWorkbench().getObjectActivityManager(PLUGIN_CONTRIBUTION_ITEM, true);
        if(objectManager != null){
	        Object activityObject = getActivityObject(action);
	        if (activityObject != null) {
	            IObjectContributionRecord record = objectManager.addObject(
	                action.getConfigElement().getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier(), 
	                action.getId(), 
	                activityObject);
	            objectManager.applyPatternBindings(record);
	        }
        }
	}

    /**
     * Temporary method that will generate a String that is an object that can 
     * represent this ActionContributionItem.  This is the sum of the pluginId
     * and the actionId in the given PluginAction.
     * 
     * TODO: determine if this object itself may be stored as the object of 
     * interest.
     *   
     * @param action the PluginAction
     * @return an Object representing the plugin/action combination or 
     * <code>null</code> if either the plugin or action id was null.
     * @since 3.0
     */    
    private Object getActivityObject(PluginAction action) {        
        String pluginId = action.getConfigElement().getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier();
        String actionId = action.getId();
        if (pluginId == null || actionId == null) {
            return null;
        }
        else {
            return pluginId + '/' + actionId;
        }
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ActionContributionItem#isVisible()
	 */
	public boolean isVisible() {
	
        IObjectActivityManager objectManager = PlatformUI.getWorkbench().getObjectActivityManager(PLUGIN_CONTRIBUTION_ITEM, false);
		// if there was no manager return isVisible().
		if (objectManager == null) 
			return super.isVisible();
		
        Object activityObject = getActivityObject((PluginAction) getAction());

        // if there is an object for this contribution could 
        // not be created, return isVisible().
        if (activityObject == null) 
            return super.isVisible();

        Collection activeObjects = objectManager.getActiveObjects();
        // check for visibility only if the active objects contains this object.
        if (activeObjects.contains(activityObject)) {
            return super.isVisible();
        }
        else {
            return false;   
        }
    }
}
