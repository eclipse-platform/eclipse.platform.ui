/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IntroContentDetector;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Describes an introduction extension.
 * 
 * @since 3.0
 */
public class IntroDescriptor implements IIntroDescriptor, IPluginContribution {


    private IConfigurationElement element;

    private ImageDescriptor imageDescriptor;

    /**
     * Create a new IntroDescriptor for an extension.
     */
    public IntroDescriptor(IConfigurationElement configElement)
            throws CoreException {
    	element = configElement;  

    	if (configElement.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS) == null) {
            throw new CoreException(new Status(IStatus.ERROR, configElement
                    .getNamespace(), 0,
                    "Invalid extension (Missing class name): " + getId(), //$NON-NLS-1$
                    null));
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.IIntroDescriptor#createIntro()
     */
    public IIntroPart createIntro() throws CoreException {
    	return (IIntroPart) element.createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
    }
    
    public IntroContentDetector getIntroContentDetector() throws CoreException {
    	if (element.getAttribute(IWorkbenchRegistryConstants.ATT_CONTENT_DETECTOR) == null) {
    		return null;
    	}
    	return (IntroContentDetector) element.createExecutableExtension(IWorkbenchRegistryConstants.ATT_CONTENT_DETECTOR);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IIntroDescriptor#getId()
     */
    public String getId() {    	
        return element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IIntroDescriptor#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        if (imageDescriptor != null) {
			return imageDescriptor;
		}        
		String iconName = element.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
		if (iconName == null) {
			return null;
		}
        
        imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(element
                .getNamespace(), iconName);
        return imageDescriptor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getLocalId()
     */
    public String getLocalId() {
        return element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getPluginId()
     */
    public String getPluginId() {
        return element.getNamespace();
    }
    
    /**
     * Returns the configuration element.
     * 
     * @return the configuration element
     * @since 3.1
     */
    public IConfigurationElement getConfigurationElement() {
    	return element;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.intro.IIntroDescriptor#getLabelOverride()
	 */
	public String getLabelOverride() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);
	}
}
