/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.part.NewViewToOldWrapper;
import org.eclipse.ui.internal.part.components.services.IPartDescriptor;
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

    private IPartDescriptor viewInfo = new IPartDescriptor() {
		public String getId() {
			return id;
		}

		public String getLabel() {
			return ViewDescriptor.this.getLabel();
		}
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.workbench.services.IPartDescriptor#getImage()
         */
        public ImageDescriptor getImage() {
            return getImageDescriptor();
        }
    };

    private HandlerSubmission handlerSubmission;
    
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

    /**
     * Return the part descriptor.
     * 
     * @return the part descriptor.
     * @since 3.1
     */
    public IPartDescriptor getPartDescriptor() {
    	return viewInfo;
    }
    

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IViewDescriptor#createView()
     */
    public IViewPart createView() throws CoreException {
        Class viewClass = configElement
                .loadExtensionClass(IWorkbenchRegistryConstants.ATT_CLASS);
        
        if (IViewPart.class.isAssignableFrom(viewClass)) {
            return (IViewPart) WorkbenchPlugin.createExtension(
                    getConfigurationElement(),
                    IWorkbenchRegistryConstants.ATT_CLASS);
        }

        return new NewViewToOldWrapper(getPartDescriptor());
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
        if (imageDescriptor != null)
            return imageDescriptor;
        String iconName = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
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
    	configElement.getAttribute(IWorkbenchRegistryConstants.ATT_RATIO); // check to ensure the element is still valid - exception thrown if it isn't
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
        
        String ratio = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_RATIO);
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
    	String string = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_MULTIPLE);    	
        return string == null ? false : Boolean.valueOf(string).booleanValue();
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

    /**
     * Return the handler submission for showing this view.
     * 
     * @return the handler submission for showing this view
     * @since 3.1
     */
    public HandlerSubmission getHandlerSubmission() {
        if (handlerSubmission == null) {
            ShowViewHandler showViewHandler = new ShowViewHandler(getId());
            handlerSubmission = new HandlerSubmission(null,
                null, null, getId(), showViewHandler, Priority.MEDIUM);
        }
        return handlerSubmission;
    }
}
