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
package org.eclipse.ui.internal.intro;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Describes an introduction extension.
 * 
 * @since 3.0
 */
public class IntroDescriptor implements IIntroDescriptor, IPluginContribution {

    private static final String ATT_ID = "id"; //$NON-NLS-1$

    private static final String ATT_CLASS = "class"; //$NON-NLS-1$

    private static final String ATT_ICON = "icon"; //$NON-NLS-1$	

    private IConfigurationElement element;

    private ImageDescriptor imageDescriptor;

    /**
     * Create a new IntroDescriptor for an extension.
     */
    public IntroDescriptor(IConfigurationElement configElement)
            throws CoreException {
    	element = configElement;  

    	if (configElement.getAttribute(ATT_CLASS) == null) {
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
    	return (IIntroPart) element.createExecutableExtension(ATT_CLASS);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IIntroDescriptor#getId()
     */
    public String getId() {    	
        return element.getAttribute(ATT_ID);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IIntroDescriptor#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        if (imageDescriptor != null)
            return imageDescriptor;        
		String iconName = element.getAttribute(ATT_ICON);
		if (iconName == null)
            return null;
        
        imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(element
                .getNamespace(), iconName);
        return imageDescriptor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getLocalId()
     */
    public String getLocalId() {
        return element.getAttribute(ATT_ID);
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
}
