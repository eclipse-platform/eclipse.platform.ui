/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Capture the attributes of a view extension.
 */
public class ViewDescriptor implements IViewDescriptor, IPluginContribution {
    private String id;

    private ImageDescriptor imageDescriptor;

    private static final String ATT_ID = "id"; //$NON-NLS-1$

    private static final String ATT_NAME = "name"; //$NON-NLS-1$

    private static final String ATT_ACCELERATOR = "accelerator"; //$NON-NLS-1$

    private static final String ATT_ICON = "icon"; //$NON-NLS-1$

    private static final String ATT_CATEGORY = "category"; //$NON-NLS-1$

    private static final String ATT_CLASS = "class"; //$NON-NLS-1$

    private static final String ATT_RATIO = "fastViewWidthRatio"; //$NON-NLS-1$

    private static final String ATT_MULTIPLE = "allowMultiple"; //$NON-NLS-1$

    private static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
    
    private IConfigurationElement configElement;

    private String[] categoryPath;

    private float fastViewWidthRatio;

    /**
     * Create a new ViewDescriptor for an extension.
     */
    public ViewDescriptor(IConfigurationElement e)
            throws CoreException {
        configElement = e;
        loadFromExtension();
        registerShowViewHandler();
    }

    private void registerShowViewHandler() {
        IHandler showViewHandler = new ShowViewHandler(getId());
        HandlerSubmission showViewSubmission = new HandlerSubmission(null,
                null, null, getId(), showViewHandler, Priority.MEDIUM);
        PlatformUI.getWorkbench().getCommandSupport().addHandlerSubmission(
                showViewSubmission);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IViewDescriptor#createView()
     */
    public IViewPart createView() throws CoreException {
        Object obj = WorkbenchPlugin.createExtension(configElement, ATT_CLASS);
        return (IViewPart) obj;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IViewDescriptor#getCategoryPath()
     */
    public String[] getCategoryPath() {
        return categoryPath;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IViewDescriptor#getConfigurationElement()
     */
    public IConfigurationElement getConfigurationElement() {
        return configElement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IViewDescriptor#getDescription()
     */
    public String getDescription() {
    	IConfigurationElement[] children = configElement.getChildren(TAG_DESCRIPTION);
        if (children.length >= 1) {
            return children[0].getValue();
        }
        return "";//$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IViewDescriptor#getID()
     */
    public String getID() {
        return getId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartDescriptor#getId()
     */
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartDescriptor#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        if (imageDescriptor != null)
            return imageDescriptor;
        String iconName = configElement.getAttribute(ATT_ICON);
        if (iconName == null)
            return null;
        IExtension extension = configElement.getDeclaringExtension();
        String extendingPluginId = extension.getNamespace();
        imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
                extendingPluginId, iconName);
        return imageDescriptor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartDescriptor#getLabel()
     */
    public String getLabel() {
        return configElement.getAttribute(ATT_NAME);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IViewDescriptor#getAccelerator()
     */
    public String getAccelerator() {
        return configElement.getAttribute(ATT_ACCELERATOR);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IViewDescriptor#getFastViewWidthRatio()
     */
    public float getFastViewWidthRatio() {
    	configElement.getAttribute(ATT_RATIO); // check to ensure the element is still valid - exception thrown if it isn't
        return fastViewWidthRatio;
    }

    /**
     * load a view descriptor from the registry.
     */
    private void loadFromExtension() throws CoreException {    	
        id = configElement.getAttribute(ATT_ID);
  
        String category = configElement.getAttribute(ATT_CATEGORY);

        // Sanity check.
        if ((configElement.getAttribute(ATT_NAME) == null) || (configElement.getAttribute(ATT_CLASS) == null)) {
            throw new CoreException(new Status(IStatus.ERROR, configElement
                    .getDeclaringExtension().getNamespace(), 0,
                    "Invalid extension (missing label or class name): " + id, //$NON-NLS-1$
                    null));
        }
        
        if (category != null) {
            StringTokenizer stok = new StringTokenizer(category, "/"); //$NON-NLS-1$
            categoryPath = new String[stok.countTokens()];
            // Parse the path tokens and store them
            for (int i = 0; stok.hasMoreTokens(); i++) {
                categoryPath[i] = stok.nextToken();
            }
        }
        
        String ratio = configElement.getAttribute(ATT_RATIO);
        if (ratio != null) {
            try {
                fastViewWidthRatio = new Float(ratio).floatValue();
                if (fastViewWidthRatio > IPageLayout.RATIO_MAX)
                    fastViewWidthRatio = IPageLayout.RATIO_MAX;
                if (fastViewWidthRatio < IPageLayout.RATIO_MIN)
                    fastViewWidthRatio = IPageLayout.RATIO_MIN;
            } catch (NumberFormatException e) {
                fastViewWidthRatio = IPageLayout.DEFAULT_FASTVIEW_RATIO;
            }
        } else {
            fastViewWidthRatio = IPageLayout.DEFAULT_FASTVIEW_RATIO;
        }        
    }

    /**
     * Returns a string representation of this descriptor. For debugging
     * purposes only.
     */
    public String toString() {
        return "View(" + getID() + ")"; //$NON-NLS-2$//$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.activities.support.IPluginContribution#getPluginId()
     */
    public String getPluginId() {
    	String pluginId = configElement.getNamespace();
        return pluginId == null ? "" : pluginId; //$NON-NLS-1$    
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.activities.support.IPluginContribution#getLocalId()
     */
    public String getLocalId() {
        return getId() == null ? "" : getId(); //$NON-NLS-1$	
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.activities.support.IPluginContribution#fromPlugin()
     */
    public boolean fromPlugin() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IViewDescriptor#getAllowMultiple()
     */
    public boolean getAllowMultiple() {
    	String string = configElement.getAttribute(ATT_MULTIPLE);    	
        return string == null ? false : Boolean.valueOf(string).booleanValue();
    }
}