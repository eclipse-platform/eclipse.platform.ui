/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.preferences;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.preferences.IPreferenceFilter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.PreferenceTransferRegistryReader;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Instances represent registered preference transfers.
 * 
 * @since 3.1
 */
public class PreferenceTransferElement extends WorkbenchAdapter implements
        IPluginContribution {
    private String id;
    
    private ImageDescriptor imageDescriptor;

    private IConfigurationElement configurationElement;

    private IPreferenceFilter filter;

    /**
     * Create a new instance of this class
     * 
     * @param configurationElement
     *              
     */
    public PreferenceTransferElement(IConfigurationElement configurationElement) {
        this.configurationElement = configurationElement;
        id = configurationElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
    }

    /**
     * @return IConfigurationElement
     */
    public IConfigurationElement getConfigurationElement() {
        return configurationElement;
    }

    /**
     * Answer the preference filter of this element
     * If the class attribute is specified it will be used, if not then look to the 
     * 
     * @return java.lang.String
     * @throws CoreException 
     */
    public IPreferenceFilter getFilter() throws CoreException {
        //TODO: can the CoreException be removed?
        if (filter == null) {
            IConfigurationElement[] mappings = PreferenceTransferRegistryReader.getMappings(configurationElement);
            PreferenceFilter prefFilter = new PreferenceFilter();
            prefFilter.scopes = new String[mappings.length];
            prefFilter.maps = new Map[mappings.length];
            for (int i = 0; i < mappings.length; i++) {
                prefFilter.scopes[i] = PreferenceTransferRegistryReader.getScope(mappings[i]);
                prefFilter.maps[i] = PreferenceTransferRegistryReader.getEntry(mappings[i]);
            } 
            filter = prefFilter;
        }
        return filter;
    }

    /**
     * Answer the description parameter of this element
     * 
     * @return java.lang.String
     */
    public String getDescription() {
        return RegistryReader.getDescription(configurationElement);
    }
    
    /**
     * Answer the id as specified in the extension.
     * 
     * @return java.lang.String
     */
    public String getID() {
        return id;
    }

    /**
     * Answer the icon of this element.
     * 
     * @return an image descriptor
     */
    public ImageDescriptor getImageDescriptor() {
    	if (imageDescriptor == null) {
    		String iconName = configurationElement.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
	        if (iconName == null) {
				return null;
			}
            imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
                    configurationElement.getNamespace(), iconName);    
    	}
        return imageDescriptor;
    }
    
    /**
     * Returns the name of this preference transfer element.
     * @return the name of the element
     */
    public String getName() {
        return configurationElement.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getLocalId()
     */
    public String getLocalId() {
        return getID();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getPluginId()
     */
    public String getPluginId() {
        return (configurationElement != null) ? configurationElement
                .getNamespace() : null;
    }

    class PreferenceFilter implements IPreferenceFilter {

        protected String[] scopes;
        protected Map[] maps;
        
        public String[] getScopes() {
            return scopes;
        }

        public Map getMapping(String scope) {
            for (int i = 0; i < scopes.length; i++) {
                String item = scopes[i];
                if (item.equals(scope)) {
					return maps[i];
				}                
            }
            return null;
        }
        
    }
}
