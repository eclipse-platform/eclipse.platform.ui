/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Alexander Kuppe, Versant Corporation - bug #215797
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
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.tweaklets.InterceptContributions;
import org.eclipse.ui.internal.tweaklets.Tweaklets;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * Capture the attributes of a view extension.
 */
public class ViewDescriptor implements IViewDescriptor, IPluginContribution {
    private String id;

    private ImageDescriptor imageDescriptor;

    private IConfigurationElement configElement;

    private String[] categoryPath;

    private float fastViewWidthRatio;
    
    /**
     * Create a new <code>ViewDescriptor</code> for an extension.
     * 
     * @param e the configuration element
     * @throws CoreException thrown if there are errors in the configuration
     */
    public ViewDescriptor(IConfigurationElement e)
            throws CoreException {
        configElement = e;
        loadFromExtension();
    }    

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IViewDescriptor#createView()
     */
    public IViewPart createView() throws CoreException {
        Object extension = WorkbenchPlugin.createExtension(
                getConfigurationElement(),
                IWorkbenchRegistryConstants.ATT_CLASS);
        return ((InterceptContributions) Tweaklets
				.get(InterceptContributions.KEY)).tweakView(extension);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IViewDescriptor#getCategoryPath()
     */
    public String[] getCategoryPath() {
        return categoryPath;
    }

    /**
     * Return the configuration element for this descriptor.
     * 
     * @return the configuration element
     */
    public IConfigurationElement getConfigurationElement() {
        return configElement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IViewDescriptor#getDescription()
     */
    public String getDescription() {
    	return RegistryReader.getDescription(configElement);
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
        if (imageDescriptor != null) {
			return imageDescriptor;
		}
        String iconName = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
        // If the icon attribute was omitted, use the default one
        if (iconName == null) {
			return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                    ISharedImages.IMG_DEF_VIEW);
		}
        IExtension extension = configElement.getDeclaringExtension();
        String extendingPluginId = extension.getNamespace();
        imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
                extendingPluginId, iconName);
        // If the icon attribute was invalid, use the error icon
        if (imageDescriptor == null) {
            imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
        }
        
        return imageDescriptor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartDescriptor#getLabel()
     */
    public String getLabel() {
        return configElement.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
    }

    /**
     * Return the accelerator attribute.
     * 
     * @return the accelerator attribute
     */
    public String getAccelerator() {
        return configElement.getAttribute(IWorkbenchRegistryConstants.ATT_ACCELERATOR);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IViewDescriptor#getFastViewWidthRatio()
     */
    public float getFastViewWidthRatio() {
    	configElement.getAttribute(IWorkbenchRegistryConstants.ATT_FAST_VIEW_WIDTH_RATIO); // check to ensure the element is still valid - exception thrown if it isn't
        return fastViewWidthRatio;
    }

    /**
     * load a view descriptor from the registry.
     */
    private void loadFromExtension() throws CoreException {    	
        id = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
  
        String category = configElement.getAttribute(IWorkbenchRegistryConstants.TAG_CATEGORY);

        // Sanity check.
        if ((configElement.getAttribute(IWorkbenchRegistryConstants.ATT_NAME) == null)
                || (RegistryReader.getClassValue(configElement,
                        IWorkbenchRegistryConstants.ATT_CLASS) == null)) {
            throw new CoreException(new Status(IStatus.ERROR, configElement
                    .getNamespace(), 0,
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
        
        String ratio = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_FAST_VIEW_WIDTH_RATIO);
        if (ratio != null) {
            try {
                fastViewWidthRatio = new Float(ratio).floatValue();
                if (fastViewWidthRatio > IPageLayout.RATIO_MAX) {
					fastViewWidthRatio = IPageLayout.RATIO_MAX;
				}
                if (fastViewWidthRatio < IPageLayout.RATIO_MIN) {
					fastViewWidthRatio = IPageLayout.RATIO_MIN;
				}
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
        return "View(" + getId() + ")"; //$NON-NLS-2$//$NON-NLS-1$
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

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IViewDescriptor#getAllowMultiple()
     */
    public boolean getAllowMultiple() {
    	String string = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_ALLOW_MULTIPLE);    	
        return string == null ? false : Boolean.valueOf(string).booleanValue();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.views.IViewDescriptor#getRestorable()
     */
    public boolean isRestorable() {
    	String string = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_RESTORABLE);    	
        return string == null ? true : Boolean.valueOf(string).booleanValue();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IConfigurationElement.class)) {
			return getConfigurationElement();
		}
		return null;
	}
}
