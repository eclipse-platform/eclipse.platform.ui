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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
 * @since 3.1
 */
public class MultiplexerTestView implements IAdaptable {
    
    private Multiplexer mplex;
    
    private MultiplexerChild logView;
    private MultiplexerChild propertiesView;
    private MultiplexerChild problemsView;
    private MultiplexerChild packageExplorer;
        
    private Listener focusListener = new Listener() {
        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
         */
        public void handleEvent(Event e) {
            if (e.widget == propertiesView.getPart().getControl()) {
                mplex.setActive(propertiesView);
            } else if (e.widget == logView.getPart().getControl()) {
                mplex.setActive(logView);
            } else if (e.widget == problemsView.getPart().getControl()) {
                mplex.setActive(problemsView);
            } else if (e.widget == packageExplorer.getPart().getControl()) {
                mplex.setActive(packageExplorer);
            }          
        }
        
    };
    
    public MultiplexerTestView(Composite parent, IWorkbenchPartFactory factory, IServiceProvider container) throws ComponentException {
    
        mplex = new Multiplexer(container);

        {   // Create PDE error log view 
            NestedContext logViewContext = mplex.createNested();

            logView = new MultiplexerChild(
                    logViewContext, factory.createView(
                    "org.eclipse.debug.ui.BreakpointView",
                    //"org.eclipse.debug.ui.DebugView",
                    //"org.eclipse.pde.runtime.LogView", 
                    parent, null, logViewContext));
    
            logView.getPart().getControl().addListener(SWT.Activate, focusListener);
        }

        { // Create the properties view
            NestedContext propertiesViewContext = mplex.createNested();
                    
            propertiesView = new MultiplexerChild(propertiesViewContext, factory.createView(IPageLayout.ID_PROP_SHEET, 
                            parent, null, propertiesViewContext));
    
            propertiesView.getPart().getControl().addListener(SWT.Activate, focusListener);
        }
            
        { // Create the problems view        
            NestedContext problemsViewContext = mplex.createNested();
            
            problemsView = new MultiplexerChild(problemsViewContext, factory.createView("org.eclipse.ui.examples.components.views.NameTestView", 
                            parent, null, problemsViewContext));
    
            problemsView.getPart().getControl().addListener(SWT.Activate, focusListener);
        }
        
        { // Create the package explorer
            NestedContext packageExplorerContext = mplex.createNested();
            
            packageExplorer = new MultiplexerChild(
                    packageExplorerContext,
                    factory.createView("org.eclipse.jdt.ui.PackageExplorer", parent, null, packageExplorerContext));
            
            packageExplorer.getPart().getControl().addListener(SWT.Activate, focusListener);
        }
            
        // Construct layout
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        parent.setLayout(layout);

        // Arrange error log view
        GridData data1 = new GridData(GridData.FILL_BOTH);
        data1.widthHint = 100;
        data1.heightHint = 100;
        logView.getPart().getControl().setLayoutData(data1);
        
        // Arrange properties view
        GridData data2 = new GridData(GridData.FILL_BOTH);
        data2.widthHint = 100;
        data2.heightHint = 100;
        propertiesView.getPart().getControl().setLayoutData(data2);
        
        // Arrange problems view
        GridData data3 = new GridData(GridData.FILL_BOTH);
        data3.widthHint = 100;
        data3.heightHint = 100;
        problemsView.getPart().getControl().setLayoutData(data3);
        
        // Arrange package explorer
        GridData data4 = new GridData(GridData.FILL_BOTH);
        data4.widthHint = 100;
        data4.heightHint = 100;
        packageExplorer.getPart().getControl().setLayoutData(data4); 
    }
    
    public Object getAdapter(Class adapterType) {
        if (adapterType == ServiceFactory.class) {
            return mplex.getDelegatingContext();
        }
        return null;
    }
    
}
