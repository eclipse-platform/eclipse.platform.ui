/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.components.ComponentException;
import org.eclipse.ui.components.FactoryMap;
import org.eclipse.ui.components.IDisposable;
import org.eclipse.ui.components.ServiceFactory;
import org.eclipse.ui.internal.part.multiplexer.Multiplexer;
import org.eclipse.ui.internal.part.multiplexer.MultiplexerChild;
import org.eclipse.ui.internal.part.multiplexer.NestedContext;
import org.eclipse.ui.internal.part.multiplexer.SiteServices;
import org.eclipse.ui.part.Part;
import org.eclipse.ui.part.services.INameable;
import org.eclipse.ui.part.services.IWorkbenchPartFactory;
import org.osgi.framework.Bundle;

/**
 * This example view demonstrates how to multiplex an interface 
 * between multiple nested children. In this case, the parent
 * contains two nested children. The parent sets its name to 
 * the name of the child with focus by multiplexing its 
 * INameable dependency. 
 * 
 * @since 3.1
 */
public class MultiplexNameView implements IDisposable {
    private Multiplexer mplex;
    private SiteServices mplexContainer;
    
    private MultiplexerChild view1;
    private MultiplexerChild view2;
    
    private Listener focusListener = new Listener() {
        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
         */
        public void handleEvent(Event e) {
            if (e.widget == view1.getPart().getControl()) {
                mplex.setActive(view1);
            } else if (e.widget == view2.getPart().getControl()) {
                mplex.setActive(view2);
            }         
        }
    };
    
    /**
     * Component constructor. Do not invoke directly.
     */
    public MultiplexNameView(Composite parent, IWorkbenchPartFactory factory, INameable name, Bundle bundle, IWorkbenchPage page) throws ComponentException {
         
        // Create a site for the multiplexer. Tell the site to use our nameable. Th  
        mplexContainer = new SiteServices(parent, bundle, page, new FactoryMap()
                        .mapInstance(INameable.class, name));
        
        // Create a multiplexer, and hook it up to the site that will redirect its name to this view
        mplex = new Multiplexer(mplexContainer);
        
        // Create a nested context for view 1. Any service that we pass to the view through this nested
        // context will be activated and deactivated by the multiplexer.
        NestedContext viewContext1 = mplex.createNested();

        // Create the actual context for view 1. In this case, we route the INameable interface
        // through the NestedContext. The view will get the default implementations for everything else.
        ServiceFactory view1Args = new FactoryMap().map(INameable.class, viewContext1);
        
        // Create the part for view1 
        Part view1Part = factory.createView(
                IPageLayout.ID_RES_NAV, parent, null, view1Args);
        
        // Create a handle for the multiplexer to select view1. This allows the multiplexer to find the part
        // and all of the services that need to be toggled whenever the part's activation changes. Note that
        // different services on the part can be hooked into different multiplexers, in which case more than
        // one multiplexer will have a handle to the same part.
        this.view1 = new MultiplexerChild(viewContext1, view1Part);
                
        view1.getPart().getControl().addListener(SWT.Activate, focusListener);
        
        // Create property view. Give the properties view a multiplexed INameable, but use
        // defaults for everything else.
        NestedContext viewContext2 = mplex.createNested();
        
        view2 = new MultiplexerChild(viewContext2, factory.createView(IPageLayout.ID_PROP_SHEET, parent, null, new FactoryMap()
                                        .map(INameable.class, viewContext2)));
        view2.getPart().getControl().addListener(SWT.Activate, focusListener);
        
        // Make the navigator active initially
        mplex.setActive(view1);
        
        parent.setLayout(new FillLayout());        
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.component.IDisposable#dispose()
     */
    public void dispose() {
        mplexContainer.dispose();
    }
}
