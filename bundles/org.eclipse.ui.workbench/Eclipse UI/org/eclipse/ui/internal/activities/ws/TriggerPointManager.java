/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ITriggerPoint;
import org.eclipse.ui.activities.ITriggerPointManager;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * Workbench implementation of the trigger point manager.
 * 
 * @since 3.1
 */
public class TriggerPointManager implements ITriggerPointManager, IExtensionChangeHandler {

    private HashMap triggerMap = new HashMap();

    /**
     * 
     */
    public TriggerPointManager() {
        super();
        triggerMap.put(ITriggerPointManager.UNKNOWN_TRIGGER_POINT_ID,
                new AbstractTriggerPoint() {

                    /*
                     * (non-Javadoc)
                     * 
                     * @see org.eclipse.ui.activities.ITriggerPoint#getId()
                     */
                    public String getId() {
                        return ITriggerPointManager.UNKNOWN_TRIGGER_POINT_ID;
                    }

                    /*
                     * (non-Javadoc)
                     * 
                     * @see org.eclipse.ui.activities.ITriggerPoint#getStringHint(java.lang.String)
                     */
                    public String getStringHint(String key) {
                        if (ITriggerPoint.HINT_INTERACTIVE.equals(key)) {
                            // TODO: change to false when we have mapped our
                            // trigger points
                            return Boolean.TRUE.toString();
                        }
                        return null;
                    }

                    /*
                     * (non-Javadoc)
                     * 
                     * @see org.eclipse.ui.activities.ITriggerPoint#getBooleanHint(java.lang.String)
                     */
                    public boolean getBooleanHint(String key) {
                        if (ITriggerPoint.HINT_INTERACTIVE.equals(key)) {
                            // TODO: change to false when we have mapped our
                            // trigger points
                            return true;
                        }
                        return false;
                    }
                });
        IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
        tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(getExtensionPointFilter()));

        IExtensionPoint point = getExtensionPointFilter();
        IExtension[] extensions = point.getExtensions();
        for (int i = 0; i < extensions.length; i++) {
            addExtension(tracker,
                    extensions[i]);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.activities.ITriggerPointManager#getTriggerPoint(java.lang.String)
     */
    public ITriggerPoint getTriggerPoint(String id) {
        return (ITriggerPoint) triggerMap.get(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.activities.ITriggerPointManager#getDefinedTriggerPointIds()
     */
    public Set getDefinedTriggerPointIds() {
        return triggerMap.entrySet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionRemovalHandler#removeInstance(org.eclipse.core.runtime.IExtension,
     *      java.lang.Object[])
     */
    public void removeExtension(IExtension extension, Object[] objects) {
        for (int i = 0; i < objects.length; i++) {
            Object object = objects[i];
            if (object instanceof RegistryTriggerPoint) {
                triggerMap.remove(((RegistryTriggerPoint) object).getId());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionAdditionHandler#addInstance(org.eclipse.core.runtime.dynamicHelpers.IExtensionTracker,
     *      org.eclipse.core.runtime.IExtension)
     */
    public void addExtension(IExtensionTracker tracker, IExtension extension) {
        IConfigurationElement[] elements = extension.getConfigurationElements();
        for (int i = 0; i < elements.length; i++) {
            IConfigurationElement element = elements[i];
            if (element.getName().equals(
                    IWorkbenchRegistryConstants.TAG_TRIGGERPOINT)) {
                String id = element
                        .getAttribute(IWorkbenchRegistryConstants.ATT_ID);
                if (id == null) {
					continue;
				}
                RegistryTriggerPoint triggerPoint = new RegistryTriggerPoint(
                        id, element);
                triggerMap.put(id, triggerPoint);
                tracker.registerObject(extension, triggerPoint,
                        IExtensionTracker.REF_WEAK);
            }
        }
    }

    private IExtensionPoint getExtensionPointFilter() {
        return Platform.getExtensionRegistry().getExtensionPoint(
                PlatformUI.PLUGIN_ID, IWorkbenchRegistryConstants.PL_ACTIVITYSUPPORT);
    }
}
