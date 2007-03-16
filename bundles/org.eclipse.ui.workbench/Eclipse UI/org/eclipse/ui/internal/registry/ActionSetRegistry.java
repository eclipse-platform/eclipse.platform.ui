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
package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The registry of action set extensions.
 */
public class ActionSetRegistry implements IExtensionChangeHandler {
    
    /** 
     * @since 3.1
     */
    private class ActionSetPartAssociation {
        /**
         * @param partId 
         * @param actionSetId 
         */
        public ActionSetPartAssociation(String partId, String actionSetId) {
            this.partId = partId;
            this.actionSetId = actionSetId;
        }
        
        
        String partId;
        String actionSetId;
    }
    
    private ArrayList children = new ArrayList();

    private Map mapPartToActionSetIds = new HashMap();
    
    private Map mapPartToActionSets = new HashMap();

	private IContextService contextService;
    
    /**
     * Creates the action set registry.
     */
    public ActionSetRegistry() {
    	contextService = (IContextService) PlatformUI
				.getWorkbench().getService(IContextService.class);
		PlatformUI.getWorkbench().getExtensionTracker().registerHandler(
                this,
                ExtensionTracker
                        .createExtensionPointFilter(new IExtensionPoint[] {
                                getActionSetExtensionPoint(),
                                getActionSetPartAssociationExtensionPoint() }));
        readFromRegistry();
    }

    /**
     * Return the action set part association extension point.
     * 
     * @return the action set part association extension point
     * @since 3.1
     */
    private IExtensionPoint getActionSetPartAssociationExtensionPoint() {
        return Platform
        .getExtensionRegistry().getExtensionPoint(
                PlatformUI.PLUGIN_ID,
                IWorkbenchRegistryConstants.PL_ACTION_SET_PART_ASSOCIATIONS);
    }

    /**
     * Return the action set extension point.
     * 
     * @return the action set extension point
     * @since 3.1
     */
    private IExtensionPoint getActionSetExtensionPoint() {
        return Platform
                .getExtensionRegistry().getExtensionPoint(
                        PlatformUI.PLUGIN_ID,
                        IWorkbenchRegistryConstants.PL_ACTION_SETS);
    }

    /**
     * Adds an action set.
     * @param desc
     */
    private void addActionSet(ActionSetDescriptor desc) {
		children.add(desc);
		Context actionSetContext = contextService.getContext(desc.getId());
		if (!actionSetContext.isDefined()) {
			actionSetContext.define(desc.getLabel(), desc.getDescription(),
					"org.eclipse.ui.contexts.actionSet"); //$NON-NLS-1$
		}
	}

	/**
	 * Remove the action set.
	 * 
	 * @param desc
	 */
	private void removeActionSet(IActionSetDescriptor desc) {
		Context actionSetContext = contextService.getContext(desc.getId());
		if (actionSetContext.isDefined()) {
			actionSetContext.undefine();
		}
		children.remove(desc);
	}

	/**
     * Adds an association between an action set an a part.
     */
    private Object addAssociation(String actionSetId, String partId) {
        // get the action set ids for this part
        ArrayList actionSets = (ArrayList) mapPartToActionSetIds.get(partId);
        if (actionSets == null) {
            actionSets = new ArrayList();
            mapPartToActionSetIds.put(partId, actionSets);
        }
        actionSets.add(actionSetId);
        
        ActionSetPartAssociation association = new ActionSetPartAssociation(partId, actionSetId);
        return association;
    }

    /**
     * Finds and returns the registered action set with the given id.
     *
     * @param id the action set id 
     * @return the action set, or <code>null</code> if none
     * @see IActionSetDescriptor#getId
     */
    public IActionSetDescriptor findActionSet(String id) {
        Iterator i = children.iterator();
        while (i.hasNext()) {
            IActionSetDescriptor desc = (IActionSetDescriptor) i.next();
            if (desc.getId().equals(id)) {
				return desc;
			}
        }
        return null;
    }

    /**
     * Returns a list of the action sets known to the workbench.
     *
     * @return a list of action sets
     */
    public IActionSetDescriptor[] getActionSets() {
        return (IActionSetDescriptor []) children.toArray(new IActionSetDescriptor [children.size()]);
    }

    /**
     * Returns a list of the action sets associated with the given part id.
     * 
     * @param partId the part id
     * @return a list of action sets
     */
    public IActionSetDescriptor[] getActionSetsFor(String partId) {
        // check the resolved map first
        ArrayList actionSets = (ArrayList) mapPartToActionSets.get(partId);
        if (actionSets != null) {
            return (IActionSetDescriptor[]) actionSets
                    .toArray(new IActionSetDescriptor[actionSets.size()]);
        }
        
        // get the action set ids for this part
        ArrayList actionSetIds = (ArrayList) mapPartToActionSetIds.get(partId);
        if (actionSetIds == null) {
			return new IActionSetDescriptor[0];
		}
        
        // resolve to action sets
        actionSets = new ArrayList(actionSetIds.size());
        for (Iterator i = actionSetIds.iterator(); i.hasNext();) {
            String actionSetId = (String) i.next();
            IActionSetDescriptor actionSet = findActionSet(actionSetId);
            if (actionSet != null) {
				actionSets.add(actionSet);
			} else {
               WorkbenchPlugin.log("Unable to associate action set with part: " + //$NON-NLS-1$
                        partId + ". Action set " + actionSetId + " not found."); //$NON-NLS-2$ //$NON-NLS-1$
            }
        }
        
        mapPartToActionSets.put(partId, actionSets);
        
        return (IActionSetDescriptor[]) actionSets
                .toArray(new IActionSetDescriptor[actionSets.size()]);
    }

    /**
     * Reads the registry.
     */
    private void readFromRegistry() {      
        IExtension[] extensions = getActionSetExtensionPoint().getExtensions();
        for (int i = 0; i < extensions.length; i++) {
            addActionSets(PlatformUI.getWorkbench().getExtensionTracker(),
                    extensions[i]);
        }

        extensions = getActionSetPartAssociationExtensionPoint()
                .getExtensions();
        for (int i = 0; i < extensions.length; i++) {
            addActionSetPartAssociations(PlatformUI.getWorkbench()
                    .getExtensionTracker(), extensions[i]);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamichelpers.IExtensionTracker, org.eclipse.core.runtime.IExtension)
     */
    public void addExtension(IExtensionTracker tracker, IExtension extension) {
        String extensionPointUniqueIdentifier = extension.getExtensionPointUniqueIdentifier();
        if (extensionPointUniqueIdentifier.equals(getActionSetExtensionPoint().getUniqueIdentifier())) {
            addActionSets(tracker, extension);
        }
        else if (extensionPointUniqueIdentifier.equals(getActionSetPartAssociationExtensionPoint().getUniqueIdentifier())){
            addActionSetPartAssociations(tracker, extension);
        }
    }

    /**
     * @param tracker
     * @param extension
     */
    private void addActionSetPartAssociations(IExtensionTracker tracker, IExtension extension) {
        IConfigurationElement [] elements = extension.getConfigurationElements();
        for (int i = 0; i < elements.length; i++) {
            IConfigurationElement element = elements[i];
            if (element.getName().equals(IWorkbenchRegistryConstants.TAG_ACTION_SET_PART_ASSOCIATION)) {
                String actionSetId = element.getAttribute(IWorkbenchRegistryConstants.ATT_TARGET_ID);
                IConfigurationElement[] children = element.getChildren();
                for (int j = 0; j < children.length; j++) {
                    IConfigurationElement child = children[j];
                    if (child.getName().equals(IWorkbenchRegistryConstants.TAG_PART)) {
                        String partId = child.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
                        if (partId != null) {
                            Object trackingObject = addAssociation(actionSetId, partId);
                            if (trackingObject != null) {
                                tracker.registerObject(extension,
                                        trackingObject,
                                        IExtensionTracker.REF_STRONG);

                            }
                            
                        }
                    } else {
                        WorkbenchPlugin.log("Unable to process element: " + //$NON-NLS-1$
                                child.getName() + " in action set part associations extension: " + //$NON-NLS-1$
                                extension.getUniqueIdentifier());
                    }
                }
            }
        }

        // TODO: optimize
        mapPartToActionSets.clear();
    }

    /**
     * @param tracker
     * @param extension
     */
    private void addActionSets(IExtensionTracker tracker, IExtension extension) {
        IConfigurationElement [] elements = extension.getConfigurationElements();
        for (int i = 0; i < elements.length; i++) {
            IConfigurationElement element = elements[i];
            if (element.getName().equals(IWorkbenchRegistryConstants.TAG_ACTION_SET)) {
                try {
                    ActionSetDescriptor desc = new ActionSetDescriptor(element);
                    addActionSet(desc);
                    tracker.registerObject(extension, desc, IExtensionTracker.REF_WEAK);

                } catch (CoreException e) {
                    // log an error since its not safe to open a dialog here
                    WorkbenchPlugin
                            .log(
                                    "Unable to create action set descriptor.", e.getStatus());//$NON-NLS-1$
                }
            } 
        }   

        // TODO: optimize
        mapPartToActionSets.clear();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension, java.lang.Object[])
     */
    public void removeExtension(IExtension extension, Object[] objects) {
        String extensionPointUniqueIdentifier = extension.getExtensionPointUniqueIdentifier();
        if (extensionPointUniqueIdentifier.equals(getActionSetExtensionPoint().getUniqueIdentifier())) {
            removeActionSets(objects);
        }
        else if (extensionPointUniqueIdentifier.equals(getActionSetPartAssociationExtensionPoint().getUniqueIdentifier())){
            removeActionSetPartAssociations(objects);
        }
    }

    /**
     * @param objects 
     */
    private void removeActionSetPartAssociations(Object[] objects) {
        for (int i = 0; i < objects.length; i++) {
            Object object = objects[i];
            if (object instanceof ActionSetPartAssociation) {
                ActionSetPartAssociation association = (ActionSetPartAssociation) object;
                String actionSetId = association.actionSetId;
                ArrayList actionSets = (ArrayList) mapPartToActionSetIds.get(association.partId);
                if (actionSets == null) {
					return;
				}
                actionSets.remove(actionSetId);
                if (actionSets.isEmpty()) {
					mapPartToActionSetIds.remove(association.partId);
				}  
            }
        }
        // TODO: optimize
        mapPartToActionSets.clear();
        
    }

    /**
     * @param objects
     */
    private void removeActionSets(Object[] objects) {
        for (int i = 0; i < objects.length; i++) {
            Object object = objects[i];
            if (object instanceof IActionSetDescriptor) {
                IActionSetDescriptor desc = (IActionSetDescriptor) object;
                removeActionSet(desc);

                // now clean up the part associations
                // TODO: this is expensive. We should consider another map from
                // actionsets
                // to parts.
                for (Iterator j = mapPartToActionSetIds.values().iterator(); j
                        .hasNext();) {
                    ArrayList list = (ArrayList) j.next();
                    list.remove(desc.getId());
                    if (list.isEmpty()) {
						j.remove();
					}
                }
            }
        }
        // TODO: optimize
        mapPartToActionSets.clear();
    }
}
