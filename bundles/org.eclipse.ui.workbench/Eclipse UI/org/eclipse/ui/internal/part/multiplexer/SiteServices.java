/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.multiplexer;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.components.ComponentUtil;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.Container;
import org.eclipse.ui.internal.components.framework.FactoryMap;
import org.eclipse.ui.internal.components.framework.IDisposable;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.components.framework.ServiceFactory;
import org.eclipse.ui.internal.part.IWorkbenchScopeConstants;
import org.osgi.framework.Bundle;

/**
 * Provides all the standard services available from a part's site.
 * 
 * @since 3.1
 */
public class SiteServices implements IServiceProvider, IDisposable {

    private Container container;
    
    private static FactoryMap createContext(ServiceFactory args) {
        return new FactoryMap()
                .add(args)
                .add(ComponentUtil.getContext(IWorkbenchScopeConstants.SITE_SCOPE))
                .add(ComponentUtil.getContext(IWorkbenchScopeConstants.PLUGIN_SCOPE));
    }
    
    /**
     * Creates services with the given set of overrides
     * 
     * @param args
     */
    public SiteServices(ServiceFactory args) {
        container = new Container(createContext(args));
    }
    
    /**
     * Creates services with the given set of overrides.
     * 
     * @param composite main control for the part
     * @param pluginBundle the part's plugin bundle
     * @param page workbench page
     * @param args overrides
     */
    public SiteServices(Composite composite, Bundle pluginBundle, IWorkbenchPage page, ServiceFactory args) {
        container = new Container(createContext(args)
                .mapInstance(Composite.class, composite)
                .mapInstance(Bundle.class, pluginBundle)
                .mapInstance(IWorkbenchPage.class, page));        
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentProvider#getComponent(java.lang.Object)
     */
    public Object getService(Object key) throws ComponentException {
        return container.getService(key);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentProvider#hasKey(java.lang.Object)
     */
    public boolean hasService(Object key) {
        return container.hasService(key);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IDisposable#dispose()
     */
    public void dispose() {
        container.dispose();
    }

}
