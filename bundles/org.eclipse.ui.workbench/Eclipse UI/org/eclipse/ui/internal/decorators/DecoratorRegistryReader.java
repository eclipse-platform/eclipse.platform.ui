/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.decorators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * The DecoratorRegistryReader is the class that reads the
 * decorator descriptions from the registry
 */
public class DecoratorRegistryReader extends RegistryReader {

    //The registry values are the ones read from the registry
    private Collection values = new ArrayList();

    private Collection ids = new HashSet();

    /**
     * Constructor for DecoratorRegistryReader.
     */
    public DecoratorRegistryReader() {
        super();
    }

    /*
     * @see RegistryReader#readElement(IConfigurationElement)
     */
    public boolean readElement(IConfigurationElement element) {

    	DecoratorDefinition desc = getDecoratorDefinition(element);
    	
    	if (desc == null) {
			return false;
		}
    	
        values.add(desc);

        return true;

    }
    
    /**
     * Return the DecoratorDefinition defined by element or <code>null</code>
     * if it cannot be determined.
     * @param element
     * @return DecoratorDefinition
     */
    DecoratorDefinition getDecoratorDefinition(IConfigurationElement element){

        String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
        if (ids.contains(id)) {
            logDuplicateId(element);
            return null;
        }
        ids.add(id);

        boolean noClass = element.getAttribute(DecoratorDefinition.ATT_CLASS) == null;

        //Lightweight or Full? It is lightweight if it is declared lightweight or if there is no class
        if (Boolean.valueOf(element.getAttribute(IWorkbenchRegistryConstants.ATT_LIGHTWEIGHT)).booleanValue() || noClass) {

            String iconPath = element.getAttribute(LightweightDecoratorDefinition.ATT_ICON);

            if (noClass && iconPath == null) {
                logMissingElement(element, LightweightDecoratorDefinition.ATT_ICON);
                return null;
            }

            return new LightweightDecoratorDefinition(id, element);
        } 
        return new FullDecoratorDefinition(id, element);
        
    }

    /**
     * Read the decorator extensions within a registry and set 
     * up the registry values.
     */
    Collection readRegistry(IExtensionRegistry in) {
        values.clear();
        ids.clear();
        readRegistry(in, PlatformUI.PLUGIN_ID,
                IWorkbenchRegistryConstants.PL_DECORATORS);
        return values;
    }

    /**
     * Return the values.
     * 
     * @return the values
     */
    public Collection getValues() {
        return values;
    }

    /**
     * Logs a registry error when the configuration element is unknown.
     */
    protected void logDuplicateId(IConfigurationElement element) {
        logError(element, "Duplicate id found: " + element.getAttribute(IWorkbenchRegistryConstants.ATT_ID));//$NON-NLS-1$
    }

}
