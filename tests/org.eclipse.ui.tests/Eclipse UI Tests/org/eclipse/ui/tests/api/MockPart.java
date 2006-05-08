/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.tests.harness.util.CallHistory;
import org.osgi.framework.Bundle;

/**
 * Base class for mock intro and workbench parts.
 * 
 * @since 3.0
 */
public class MockPart extends EventManager implements IExecutableExtension {

    /**
     * 
     */
    public MockPart() {
        callTrace = new CallHistory(this);
        selectionProvider = new MockSelectionProvider();
    }

    protected CallHistory callTrace;

    protected MockSelectionProvider selectionProvider;

    private IConfigurationElement config;

    private Object data;

    private Image titleImage;

    private DisposeListener disposeListener = new DisposeListener() {
    	/* (non-Javadoc)
    	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
    	 */
    	public void widgetDisposed(DisposeEvent e) {
    		MockPart.this.widgetDisposed();
    	}
    };
    
    public CallHistory getCallHistory() {
        return callTrace;
    }

    public ISelectionProvider getSelectionProvider() {
        return selectionProvider;
    }

    public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) throws CoreException {
    	
    	callTrace.add("setInitializationData");
    	
        this.config = config;
        this.data = data;

        // Icon.
        String strIcon = config.getAttribute("icon");//$NON-NLS-1$
        if (strIcon != null) {
            try {
            	Bundle plugin = Platform.getBundle(config.getNamespace());
                URL installURL = plugin.getEntry("/"); //$NON-NLS-1$
                URL fullPathString = new URL(installURL, strIcon);
                ImageDescriptor imageDesc = ImageDescriptor
                        .createFromURL(fullPathString);
                titleImage = imageDesc.createImage();
            } catch (MalformedURLException e) {
                // ignore
            }
        }
    }

    protected IConfigurationElement getConfig() {
        return config;
    }

    protected Object getData() {
        return data;
    }

    // This isn't actually part of the part API, but we call this method from a dispose listener
    // in order to mark the point in time at which the widgets are disposed
    public void widgetDisposed() {
    	callTrace.add("widgetDisposed");
    }
    
    /**
     * @see IWorkbenchPart#addPropertyListener(IPropertyListener)
     */
    public void addPropertyListener(IPropertyListener listener) {
        addListenerObject(listener);
    }

    /**
     * @see IWorkbenchPart#createPartControl(Composite)
     */
    public void createPartControl(Composite parent) {
        callTrace.add("createPartControl");
        
        parent.addDisposeListener(disposeListener);
    }

    /**
     * @see IWorkbenchPart#dispose()
     */
    public void dispose() {
        callTrace.add("dispose");
    }

    /**
     * @see IWorkbenchPart#getTitleImage()
     */
    public Image getTitleImage() {
        return titleImage;
    }

    /**
     * @see IWorkbenchPart#removePropertyListener(IPropertyListener)
     */
    public void removePropertyListener(IPropertyListener listener) {
        removeListenerObject(listener);
    }

    /**
     * @see IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        callTrace.add("setFocus");
    }

    /**
     * @see IAdaptable#getAdapter(Class)
     */
    public Object getAdapter(Class arg0) {
        return null;
    }

    /**
     * Fires a selection out.
     */
    public void fireSelection() {
        selectionProvider.fireSelection();
    }

    /**
     * Fires a property change event.
     */
    protected void firePropertyChange(int propertyId) {
        Object[] listeners = getListeners();
        for (int i = 0; i < listeners.length; i++) {
            IPropertyListener l = (IPropertyListener) listeners[i];
            l.propertyChanged(this, propertyId);
        }
    }

    /**
     * boolean to declare whether the site was properly initialized in the init method. 
     */
    private boolean siteState = false;

    /**
     * Sets whether the site was properly initialized in the init method.
     */
    protected void setSiteInitialized(boolean initialized) {
        siteState = initialized;
    }

    /**
     * Gets whether the site was properly initialized in the init method.
     */
    public boolean isSiteInitialized() {
        return siteState;
    }
}
