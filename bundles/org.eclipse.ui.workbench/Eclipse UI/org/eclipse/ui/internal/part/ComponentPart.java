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

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistable;
import org.eclipse.ui.internal.components.ComponentUtil;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.ComponentFactory;
import org.eclipse.ui.internal.components.framework.ComponentHandle;
import org.eclipse.ui.internal.components.framework.Components;
import org.eclipse.ui.internal.components.framework.Container;
import org.eclipse.ui.internal.components.framework.FactoryMap;
import org.eclipse.ui.internal.components.framework.ServiceFactory;
import org.eclipse.ui.internal.part.components.interfaces.IFocusable;
import org.eclipse.ui.internal.part.multiplexer.SiteServices;

/**
 * Standard implementation of a Part, built by an AbstractComponentFactory.
 * 
 * <p>Not intended to be subclassed by clients.</p>
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public final class ComponentPart extends Part implements IFocusable {

	private IPersistable persistable;
    private SiteServices container;
    private Container adapters;
	private SiteComposite control;
    private ComponentHandle component;
    
    /**
     * Context that is visible from outside the part (used as a filter -- unless
     * a key exists in this context, it won't be returned by the part).
     */
    private FactoryMap visibleContext;
        
    private DisposeListener partDisposeListener = new DisposeListener() {
        public void widgetDisposed(DisposeEvent e) {
            disposed();
        }
    };
    
    /**
     * Constructs a new Part from the given factory with the given set of services.
     * 
     * @param parent parent composite
     * @param overrides services available to the part
     * @param factory factory for the main implementation of the part
     * @throws ComponentException if unable to construct the part
     */
	public ComponentPart(Composite parent, ServiceFactory overrides, ComponentFactory factory) throws ComponentException {
        Assert.isNotNull(parent);
        Assert.isNotNull(overrides);
        Assert.isNotNull(factory);
        Assert.isTrue(!(parent.isDisposed()));

        if (overrides == null) {
            overrides = new FactoryMap();
        }
	    try {
            control = new SiteComposite(parent);
            control.setLayout(new FillLayout());
       
            
            ServiceFactory outputContext = ComponentUtil.getContext(IWorkbenchScopeConstants.PART_SCOPE);
            
            // create the site for this part
            this.container = new SiteServices(new FactoryMap().add(overrides).mapInstance(Composite.class, control));
                   
            // Only hook the dispose listener after the container is created, since the purpose of this
            // listener is to dispose the container when the control goes away
            control.addDisposeListener(partDisposeListener);

            component = factory.createHandle(container);			
			
            Object part = component.getInstance();
            
            // Construct the context for publicly-visible components in this part
            {
                visibleContext = new FactoryMap();
                
                visibleContext.addInstance(part);
    
                ServiceFactory additionalContext = (ServiceFactory) Components.getAdapter(part, ServiceFactory.class);
                if (additionalContext != null) {
                    visibleContext.add(additionalContext);
                }
                
                visibleContext.add(outputContext);
            }
            
            // Construct the container for adapters on this part.
            adapters = new Container(new FactoryMap().add(visibleContext).add(container));
            
			Object[] interfaces = Components.queryInterfaces(adapters, new Class[] { 
					IPersistable.class
					});
			persistable = (IPersistable) interfaces[0];
            
            control.setFocusable((IFocusable)Components.getAdapter(adapters, IFocusable.class));
            
			control.layout(true);
           
            
	    } catch (SWTException e) {
            control.dispose();
            throw new ComponentException(factory, e);
        } catch (ComponentException e) {
	        control.dispose();
            throw e;
	    }

	}
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.part.components.interfaces.IPart#getControl()
	 */
	public Control getControl() {
		return control;
	}
    
    private void disposed() {
        if (adapters != null) {
            adapters.dispose();
        }
        if (component != null) {
            component.getDisposable().dispose();
        }
        if (container != null) {
            container.dispose();
        }
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.part.components.interfaces.IPersistable#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		persistable.saveState(memento);
	}

    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentProvider#getComponent(java.lang.Object)
     */
    public Object getService(Object key) throws ComponentException {
     
        if (key == IFocusable.class) {
            return this;
        }
        
        // Only return components that are supposed to be publicly visible from this part.
        // The "adapters" container will also be able to find components supplied by the site
        // and we don't want to expose those.
        if (visibleContext.hasService(key)) {
            return Components.queryInterface(adapters, key);
        }

        // try to adapt the component object itself
        if(key instanceof Class) {
        	return Components.getAdapter(component.getInstance(), (Class)key);
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentProvider#hasKey(java.lang.Object)
     */
    public boolean hasService(Object key) {
        return visibleContext.hasService(key);
    }
    
    public boolean setFocus() {
        return getControl().setFocus();
    }
}
