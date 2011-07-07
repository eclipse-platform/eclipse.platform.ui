/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.themes.IThemePreview;

/**
 * @since 3.0
 */
public class ThemeElementCategory implements IPluginContribution,
        IThemeElementDefinition {

    private String description;

    private IConfigurationElement element;

    private String id;

    private String parentId;

    private String label;

    private String pluginId;

    /**
     * 
     * @param label
     * @param id
     * @param parentId 
     * @param description
     * @param pluginId
     * @param element
     */
    public ThemeElementCategory(String label, String id, String parentId,
            String description, String pluginId, IConfigurationElement element) {

        this.label = label;
        this.id = id;
        this.parentId = parentId;
        this.description = description;
        this.pluginId = pluginId;
        this.element = element;
    }

    /**
     * @return Returns the <code>IColorExample</code> for this category.  If one
     * is not available, <code>null</code> is returned.
     * @throws CoreException thrown if there is a problem instantiating the preview
     */
    public IThemePreview createPreview() throws CoreException {
        String classString = element.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS);
        if (classString == null || "".equals(classString)) { //$NON-NLS-1$
			return null;
		}
        return (IThemePreview) WorkbenchPlugin.createExtension(element,
                IWorkbenchRegistryConstants.ATT_CLASS);
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return Returns the element.
     */
    public IConfigurationElement getElement() {
        return element;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeElementDefinition#getId()
     */
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeElementDefinition#getLabel()
     */
    public String getName() {
        return label;
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

    /**
     * @return Returns the parentId.  May be <code>null</code>.
     */
    public String getParentId() {
        return parentId;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof ThemeElementCategory) {
            return getId().equals(((ThemeElementCategory)obj).getId());
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return id.hashCode();
    }    
}
