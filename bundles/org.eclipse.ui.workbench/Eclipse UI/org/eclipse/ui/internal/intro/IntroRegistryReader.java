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
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * Introduction registry reader.
 *  
 * @since 3.0
 */
public class IntroRegistryReader extends RegistryReader {
    private static final String TAG_INTRO = "intro";//$NON-NLS-1$	

    private static final String TAG_INTROPRODUCTBINDING = "introProductBinding";//$NON-NLS-1$

    private IntroRegistry introRegistry;

    /**
     */
    public IntroRegistryReader() {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
     */
    protected boolean readElement(IConfigurationElement element) {
        if (element.getName().equals(TAG_INTRO)) {
            readIntro(element);
            return true;
        } else if (element.getName().equals(TAG_INTROPRODUCTBINDING)) {
            readBinding(element);
            return true;
        }
        return false;
    }

    /**
     * Read binding information.
     * 
     * @param element the configuration element to be read.
     */
    private void readBinding(IConfigurationElement element) {
        try {
            introRegistry.addBinding(element);
        } catch (CoreException e) {
            // log an error since its not safe to open a dialog here
            WorkbenchPlugin
                    .log(
                            IntroMessages
                                    .getString("Intro.could_not_create_binding"), e.getStatus());//$NON-NLS-1$            
        }
    }

    /**
     * Read introduction information.
     * 
     * @param element the configuration element to read. 
     */
    private void readIntro(IConfigurationElement element) {
        try {
            IIntroDescriptor descriptor = new IntroDescriptor(element);
            introRegistry.add(descriptor);
        } catch (CoreException e) {
            // log an error since its not safe to open a dialog here
            WorkbenchPlugin
                    .log(
                            IntroMessages
                                    .getString("Intro.could_not_create_descriptor"), e.getStatus());//$NON-NLS-1$
        }
    }

    /**
     * Read all introdcution extensions from the registry.
     * 
     * @param in the registry to read.
     * @param out the registry to populate.
     */
    public void readIntros(IExtensionRegistry in, IntroRegistry out) {
        introRegistry = out;
        readRegistry(in, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_INTRO);
    }
}