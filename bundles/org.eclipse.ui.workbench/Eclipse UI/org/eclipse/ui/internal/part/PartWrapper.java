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

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.FactoryMap;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.components.framework.ServiceFactory;
import org.eclipse.ui.internal.part.multiplexer.SiteServices;
import org.osgi.framework.Bundle;

/**
 * Wraps a Part in a form that can be adapted to IViewPart and IEditorPart
 * 
 * Note: this is a bit of a hack. This should really be done using a custom
 * component factory and a raw ComponentPart. The current implementation
 * means that we need to create a Site before the part's composite, which
 * means that the Site in a PartWrapper points to the wrong composite.
 * This currently isn't causing problems since the site is only needed 
 * for one component (the IPropertyProvider) and it doesn't use a composite.
 * The rest of a part's dependencies get filled by the container in the ComponentPart,
 * which knows the correct composite.
 */
public abstract class PartWrapper extends Part {

    private Part wrappedPart;
    private SiteServices container;
    
    public PartWrapper(Composite parentControl, Bundle bundle, IWorkbenchPage page, PartGenerator gen, ServiceFactory context) throws ComponentException {
        
        container = new SiteServices(parentControl, bundle, page, context);
        IPartPropertyProvider provider;

        try {
            provider = (IPartPropertyProvider)container.getService(IPartPropertyProvider.class);
        
            FactoryMap childContext = new FactoryMap()
                .addInstance(provider)
                .add(container);
            
            wrappedPart = gen.createPart(parentControl, childContext);
            wrappedPart.getControl().addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    disposed();
                }
            });
            
        } catch (ComponentException e1) {
            container.dispose();
            throw e1;
        }
    }
    
    protected Part getWrappedPart() {
        return wrappedPart;
    }
    
    protected IServiceProvider getContainer() {
        return container;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.Part#getControl()
     */
    public Control getControl() {
        return wrappedPart.getControl();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.components.interfaces.IPersistable#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento) {
        wrappedPart.saveState(memento);
    }

    private void disposed() {
        container.dispose();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentProvider#getComponent(java.lang.Object)
     */
    public Object getService(Object key) throws ComponentException {
        return wrappedPart.getService(key);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentProvider#hasKey(java.lang.Object)
     */
    public boolean hasService(Object key) {
        return wrappedPart.hasService(key);
    }
}
