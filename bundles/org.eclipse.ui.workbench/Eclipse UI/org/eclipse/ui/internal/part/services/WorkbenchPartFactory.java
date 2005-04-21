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
package org.eclipse.ui.internal.part.services;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.ActionDescriptor;
import org.eclipse.ui.internal.ViewActionBuilder;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.ComponentFactory;
import org.eclipse.ui.internal.components.framework.FactoryMap;
import org.eclipse.ui.internal.components.framework.ServiceFactory;
import org.eclipse.ui.internal.part.ComponentPart;
import org.eclipse.ui.internal.part.EditorWrapper;
import org.eclipse.ui.internal.part.OldEditorToNewWrapperFactory;
import org.eclipse.ui.internal.part.OldViewToNewWrapperFactory;
import org.eclipse.ui.internal.part.Part;
import org.eclipse.ui.internal.part.PartGenerator;
import org.eclipse.ui.internal.part.ViewWrapper;
import org.eclipse.ui.internal.part.components.services.IPartDescriptor;
import org.eclipse.ui.internal.part.components.services.ISavedState;
import org.eclipse.ui.internal.part.components.services.IWorkbenchPartFactory;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.ViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;
import org.osgi.framework.Bundle;

/**
 * This class can be used to construct views and editors inside arbitrary user
 * composites.
 * 
 * @since 3.1
 */
public class WorkbenchPartFactory implements IWorkbenchPartFactory {

	private IWorkbenchPage page;
    private OldEditorToNewWrapperFactory oldEditorFactory = new OldEditorToNewWrapperFactory();
    private OldViewToNewWrapperFactory oldViewFactory = new OldViewToNewWrapperFactory();
		
	public WorkbenchPartFactory(IWorkbenchPage page) {
	    this.page = page;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.part.components.interfaces.IPartFactory#createView(java.lang.String, org.eclipse.swt.widgets.Composite, org.eclipse.ui.IMemento, org.eclipse.core.component.IComponentArguments)
	 */
	public Part createView(String viewId, Composite parentComposite, 
			final IMemento savedState, ServiceFactory args) throws ComponentException {
        
		IViewRegistry viewRegistry = WorkbenchPlugin.getDefault().getViewRegistry();
		
		final ViewDescriptor descriptor = (ViewDescriptor)viewRegistry.find(viewId);
        
        Bundle bundle = Platform.getBundle(descriptor.getConfigurationElement().getNamespace());
        
        FactoryMap context = new FactoryMap()
            .mapInstance(IPartDescriptor.class, descriptor.getPartDescriptor())
            .add(args);
    
        if (savedState != null) {
            context.mapInstance(ISavedState.class, new SavedState(savedState));
        }
        
        // Factory for the real part (the part returned here might not be adaptable to IViewPart, so
        // we need to wrap it)
        PartGenerator generator = new PartGenerator() {
            public Part createPart(Composite parent, ServiceFactory context) throws ComponentException {
                return WorkbenchPartFactory.this.createPart(descriptor.getConfigurationElement(), "class", //$NON-NLS-1$ 
                        parent, context);
            }
        };
        
        // Create a wrapper for the part -- the wrapper makes it possible to adapt it to an IViewPart
		ViewWrapper result = new ViewWrapper(parentComposite, bundle, page, generator, context); 
        
        IViewPart viewPart = result.getViewPart();
        
        ViewActionBuilder builder = new ViewActionBuilder();
        builder.readActionExtensions(viewPart);
        
        IKeyBindingService keyBindingService = viewPart.getSite().getKeyBindingService();
        
        ActionDescriptor[] actionDescriptors = builder.getExtendedActions();
        
        if (actionDescriptors != null) {
            for (int i = 0; i < actionDescriptors.length; i++) {
                ActionDescriptor actionDescriptor = actionDescriptors[i];
    
                if (actionDescriptor != null) {
                    IAction action = actionDescriptors[i]
                            .getAction();
    
                    if (action != null
                            && action.getActionDefinitionId() != null)
                        keyBindingService.registerAction(action);
                }
            }
        }
        
        return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.part.components.interfaces.IPartFactory#createEditor(java.lang.String, org.eclipse.swt.widgets.Composite, org.eclipse.ui.IMemento, org.eclipse.core.component.IComponentArguments, org.eclipse.ui.IEditorInput)
	 */
	public Part createEditor(String editorId, Composite parentComposite,
			IEditorInput input, IMemento savedState, ServiceFactory args) throws ComponentException {
        
	    EditorRegistry editorRegistry = (EditorRegistry)WorkbenchPlugin.getDefault().getEditorRegistry();
		
		final EditorDescriptor descriptor = (EditorDescriptor)editorRegistry.findEditor(editorId);
        
        Bundle bundle = Platform.getBundle(descriptor.getConfigurationElement().getNamespace());
        
        FactoryMap context = new FactoryMap()
            .mapInstance(IPartDescriptor.class, descriptor.getPartDescriptor())
            .mapInstance(IEditorInput.class, input)
            .add(args);
    
        if (savedState != null) {
            context.mapInstance(ISavedState.class, new SavedState(savedState));
        }
        
        // Factory for the real part (the part returned here might not be adaptable to IViewPart, so
        // we need to wrap it)
        PartGenerator generator = new PartGenerator() {
            public Part createPart(Composite parent, ServiceFactory context) throws ComponentException {
                return WorkbenchPartFactory.this.createPart(descriptor.getConfigurationElement(), "class", //$NON-NLS-1$ 
                        parent, context);
            }
        };
        
        // Create a wrapper for the part -- the wrapper makes it possible to adapt it to an IEditorPart
        EditorWrapper result = new EditorWrapper(parentComposite, bundle, page, generator, context); 
        
        return result;
	}
    
	private Part createPart(IConfigurationElement e, 
            String attrib, Composite parentComposite, 
            ServiceFactory args) throws ComponentException {

        Class extensionClass = null;
        
        try {
            Object partOrFactory = e.createExecutableExtension(attrib);
            
    		if (partOrFactory instanceof IEditorPart) {                
    	        return new ComponentPart(parentComposite, new FactoryMap()
                        .add(args).mapInstance(IEditorPart.class, partOrFactory), 
                        oldEditorFactory);
    		} else if (partOrFactory instanceof IViewPart) {
                IViewPart part = (IViewPart) e.createExecutableExtension(attrib);
                
    	        return new ComponentPart(parentComposite, new FactoryMap()
                        .add(args).mapInstance(IViewPart.class, part),  
                        oldViewFactory);
    	    } else if (partOrFactory instanceof ComponentFactory) {
                return new ComponentPart(parentComposite, args, (ComponentFactory)partOrFactory);
            }
            
            String msg = NLS.bind(WorkbenchMessages.PartFactory_wrongtype,partOrFactory.getClass().getName()); 
            
            throw new ComponentException(msg, null);
            
        } catch (CoreException ex) {
            throw new ComponentException(extensionClass != null ? extensionClass : getClass(), ex);
        }
	}
}
