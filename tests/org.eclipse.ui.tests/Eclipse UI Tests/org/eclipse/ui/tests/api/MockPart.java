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
package org.eclipse.ui.tests.api;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.tests.util.CallHistory;

/**
 * Base class for mock intro and workbench parts.
 * 
 * @since 3.0
 */
public class MockPart implements IExecutableExtension {

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

    private ListenerList propertyListeners = new ListenerList();

    public CallHistory getCallHistory() {
        return callTrace;
    }

    public ISelectionProvider getSelectionProvider() {
        return selectionProvider;
    }

    public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) throws CoreException {
        this.config = config;
        this.data = data;

        // Icon.
        String strIcon = config.getAttribute("icon");//$NON-NLS-1$
        if (strIcon != null) {
            try {
                IPluginDescriptor pd = config.getDeclaringExtension()
                        .getDeclaringPluginDescriptor();
                URL fullPathString = new URL(pd.getInstallURL(), strIcon);
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

    /**
     * @see IWorkbenchPart#addPropertyListener(IPropertyListener)
     */
    public void addPropertyListener(IPropertyListener listener) {
        propertyListeners.add(listener);
    }

    /**
     * @see IWorkbenchPart#createPartControl(Composite)
     */
    public void createPartControl(Composite parent) {
        callTrace.add("createPartControl");
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
        propertyListeners.remove(listener);
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
        Object[] listeners = propertyListeners.getListeners();
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