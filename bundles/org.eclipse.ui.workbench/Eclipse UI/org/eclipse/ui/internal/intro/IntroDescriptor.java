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
package org.eclipse.ui.internal.intro;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.internal.WorkbenchPlugin;
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

    private IConfigurationElement configElement;

    private String id;

    private String pluginId;

    private String iconName;

    private ImageDescriptor imageDescriptor;

    /**
     * Create a new IntroDescriptor for an extension.
     */
    public IntroDescriptor(IConfigurationElement configElement)
            throws CoreException {
        this.configElement = configElement;
        loadFromExtension();
    }

    /**
     * load a intro descriptor from the registry.
     */
    private void loadFromExtension() throws CoreException {
        id = configElement.getAttribute(ATT_ID);
        pluginId = configElement.getDeclaringExtension().getNamespace();
        String className = configElement.getAttribute(ATT_CLASS);
        iconName = configElement.getAttribute(ATT_ICON);
        // Sanity check.
        if (className == null) {
            throw new CoreException(new Status(IStatus.ERROR, configElement
                    .getDeclaringExtension().getNamespace(), 0,
                    "Invalid extension (Missing class name): " + id, //$NON-NLS-1$
                    null));
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.IIntroDescriptor#createIntro()
     */
    public IIntroPart createIntro() throws CoreException {
        return (IIntroPart) WorkbenchPlugin.createExtension(configElement,
                ATT_CLASS);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IIntroDescriptor#getId()
     */
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IIntroDescriptor#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        if (imageDescriptor != null)
            return imageDescriptor;
        if (iconName == null)
            return null;
        IExtension extension = configElement.getDeclaringExtension();
        String extendingPluginId = extension.getNamespace();
        imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
                extendingPluginId, iconName);
        return imageDescriptor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getLocalId()
     */
    public String getLocalId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getPluginId()
     */
    public String getPluginId() {
        return pluginId;
    }
}