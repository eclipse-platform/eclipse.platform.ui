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
package org.eclipse.ui.tests.components;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.components.ComponentException;
import org.eclipse.ui.components.IServiceProvider;
import org.eclipse.ui.components.ServiceFactory;
import org.eclipse.ui.internal.part.multiplexer.Multiplexer;
import org.eclipse.ui.internal.part.multiplexer.MultiplexerChild;
import org.eclipse.ui.internal.part.multiplexer.NestedContext;
import org.eclipse.ui.part.services.IWorkbenchPartFactory;

/**
 * 
 * @since 3.1
 */
public class MultiplexAllView implements IAdaptable {    
    
    private Multiplexer mplex;
    
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
    public MultiplexAllView(Composite parent, IServiceProvider provider, IWorkbenchPartFactory factory) throws ComponentException {
    
        mplex = new Multiplexer(provider);
        
        NestedContext view1Context = mplex.createNested();
        
        // Create a resource navigator. Multiplex all of its dependencies. 
        view1 = new MultiplexerChild(view1Context, factory.createView(
                        IPageLayout.ID_RES_NAV, parent, null, view1Context));
        view1.getPart().getControl().addListener(SWT.Activate, focusListener);
        
        NestedContext view2Context = mplex.createNested();
        
        // Create property view. Multiplex all of its dependencies.
        view2 = new MultiplexerChild(view2Context,
                factory.createView(IPageLayout.ID_PROP_SHEET, parent, null, view2Context));
        view2.getPart().getControl().addListener(SWT.Activate, focusListener);
        
        // Make the navigator active initially
        mplex.setActive(view1);
        
        parent.setLayout(new FillLayout());        
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        if (adapter == ServiceFactory.class) {
            return mplex.getDelegatingContext();
        }
        return null;
    }
    
}
