/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.Components;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.part.components.services.IActionBarContributorFactory;
import org.eclipse.ui.internal.part.components.services.INameable;
import org.eclipse.ui.internal.part.components.services.IPartActionBars;
import org.eclipse.ui.internal.part.components.services.IPartDescriptor;
import org.eclipse.ui.internal.part.components.services.ISavedState;
import org.eclipse.ui.internal.part.components.services.ISecondaryId;
import org.eclipse.ui.internal.part.components.services.ISelectionHandler;
import org.eclipse.ui.internal.part.components.services.IStatusFactory;
import org.eclipse.ui.internal.part.components.services.IStatusHandler;
import org.eclipse.ui.internal.part.components.services.ISystemLog;
import org.osgi.framework.Bundle;

/**
 * Contains all services needed to create an IWorkbenchPart-style view or editor
 * 
 * @since 3.1
 */
public class StandardWorkbenchServices {
    ISelectionHandler selectionHandler;
    IEditorInput editorInput;
    Bundle pluginBundle;
    Composite parentComposite;
    IWorkbenchPage page;
    INameable nameable;
    //ISavedState savedState;
    IStatusFactory statusFactory;
    IPartDescriptor descriptor;
    //IActionBars actionBars;
    ISecondaryId secondaryId;
    ISystemLog log;
    IMemento state;
    IKeyBindingService keyBindingService;
    IStatusHandler statusHandler;
    
    IServiceProvider componentProvider;
    IPartActionBars partActionBars;
    IActionBarContributorFactory sharedActionBarsFactory;

    public StandardWorkbenchServices(IServiceProvider availableServices) throws ComponentException {
        componentProvider = availableServices;
        
        pluginBundle = (Bundle) Components.queryInterface(availableServices,
                Bundle.class);
        parentComposite = (Composite) Components.queryInterface(availableServices,
                Composite.class);
        editorInput = (IEditorInput) Components.queryInterface(availableServices,
                IEditorInput.class);
        page = (IWorkbenchPage) Components.queryInterface(availableServices,
                IWorkbenchPage.class);
        partActionBars = (IPartActionBars) Components.queryInterface(availableServices,
                IPartActionBars.class);
        selectionHandler = (ISelectionHandler) Components.queryInterface(availableServices,
                ISelectionHandler.class);
        nameable = (INameable) Components.queryInterface(availableServices,
                INameable.class);
        state = ((ISavedState) Components.queryInterface(availableServices,
                ISavedState.class)).getState();        
        statusFactory = (IStatusFactory) Components.queryInterface(availableServices,
                IStatusFactory.class);
        descriptor = (IPartDescriptor) Components.queryInterface(availableServices,
                IPartDescriptor.class);        
        secondaryId = (ISecondaryId) Components.queryInterface(availableServices,
                ISecondaryId.class);
        sharedActionBarsFactory = (IActionBarContributorFactory) Components.queryInterface(availableServices,
                IActionBarContributorFactory.class);
        log = (ISystemLog) Components.queryInterface(availableServices,
                ISystemLog.class);
        keyBindingService = (IKeyBindingService) Components.queryInterface(availableServices,
                IKeyBindingService.class);
        statusHandler = (IStatusHandler) Components.queryInterface(availableServices,
                IStatusHandler.class);
    }
    
    public IStatusHandler getStatusHandler() {
        return statusHandler;
    }
    
    public IKeyBindingService getKeyBindingService() {
        return keyBindingService;
    }
    
    public IActionBarContributorFactory getActionBarContributorFactory() {
        return sharedActionBarsFactory;
    }
    
    public IPartActionBars getActionBars() {
        return partActionBars;
    }
    
    public Object getAdapter(Class key) {
        try {
            return componentProvider.getService(key);
        } catch (ComponentException e) {
            log.log(e);
            return null;
        }
    }
    
    /**
     * @return Returns the secondaryId.
     */
    public ISecondaryId getSecondaryId() {
        return secondaryId;
    }
//    /**
//     * @return Returns the actionBars.
//     */
//    public IActionBars getActionBars() {
//        return actionBars;
//    }
    public IPartDescriptor getDescriptor() {
        return descriptor;
    }
    public IEditorInput getEditorInput() {
        return editorInput;
    }
    public INameable getNameable() {
        return nameable;
    }
    
    
    public IWorkbenchPage getPage() {
        return page;
    }
    public Composite getParentComposite() {
        return parentComposite;
    }
    public Bundle getPluginBundle() {
        return pluginBundle;
    }
    public IMemento getState() {
        return state;
    }
    public ISelectionHandler getSelectionHandler() {
        return selectionHandler;
    }
    public IStatusFactory getStatusFactory() {
        return statusFactory;
    }
}
