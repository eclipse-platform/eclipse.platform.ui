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

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.FactoryMap;
import org.eclipse.ui.internal.components.framework.ServiceFactory;
import org.eclipse.ui.internal.part.components.services.IPartActionBars;
import org.eclipse.ui.internal.part.components.services.IPartDescriptor;
import org.eclipse.ui.internal.part.components.services.IWorkbenchPartFactory;
import org.eclipse.ui.internal.part.services.NullEditorInput;
import org.eclipse.ui.internal.part.services.ViewToPartActionBarsAdapter;

/**
 * This class is used to wrap a new-style view inside something that implements
 * the IViewPart interface. This is used by ViewDescriptor when the old-style
 * API is used to create a view, but the view being referenced uses constructor
 * injection for initialization.
 * <p>
 * The real part is only created when createPartControl is invoked.
 * </p>
 * 
 * @since 3.1
 */
public class NewViewToOldWrapper extends NewPartToOldWrapper implements IViewPart {
    
    private IMemento viewMemento = null;
    
    /**
     * 
     */
    public NewViewToOldWrapper(IPartDescriptor descriptor) {
        super(new PartPropertyProvider(null, null, null, descriptor, new NullEditorInput()));
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    public void init(IViewSite site, IMemento memento) {
        init(site);
        viewMemento = memento;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite)
     */
    public void init(IViewSite site) {
        setSite(site);
    }

    public IViewSite getViewSite() {
        return (IViewSite)getSite();
    }
    
    protected Part createPart(Composite parent, ServiceFactory args) throws ComponentException {
        IWorkbenchPartFactory factory = getFactory();
        String id = getSite().getId();
        
        Part result = factory.createView(id, parent, getMemento(), args);
        
        return result;
    }
    
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        viewMemento = null;
    }
    
    protected IMemento getMemento() {
        return viewMemento;
    }
    
    protected String getSecondaryId() {
        return getViewSite().getSecondaryId();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.compatibility.NewPartToOldAdapter#addServices(org.eclipse.core.component.ContainerContext)
     */
    protected void addServices(FactoryMap context) {
        super.addServices(context);
        
        context.mapInstance(IActionBars.class, getViewSite().getActionBars());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewPart#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento) {      
        Part part = getPart();
        if (part != null) {
        	part.saveState(memento);
        }
    }    
    
    protected IPartActionBars createPartActionBars() {
        return new ViewToPartActionBarsAdapter(getViewSite().getActionBars());
    }
    
    protected IStatusLineManager getStatusLineManager() {
        return getViewSite().getActionBars().getStatusLineManager();
    }
}
