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
package org.eclipse.ui.internal.presentation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IPresentationPreview;
import org.eclipse.ui.internal.WorkbenchPlugin;


/**
 * @since 3.0
 */
public class PresentationCategory implements IPluginContribution {
    
	public static PresentationCategory [] categories;
	
	/**
	 * <code>Comparator</code> used in <code>ColorDefinition</code> [] 
	 * and <code>ColorCategory</code> [] searching.  May match against 
	 * <code>String</code>s.
	 */
	public static final Comparator ID_COMPARATOR = new Comparator() {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object arg0, Object arg1) {
			String str0 = getCompareString(arg0);
			String str1 = getCompareString(arg1);
			return str0.compareTo(str1);
		}

		/**
		 * @param object
		 * @return <code>String</code> representation of the object.
		 */
		private String getCompareString(Object object) {
			if (object instanceof String)
				return (String) object;
			else if (object instanceof PresentationCategory)
				return ((PresentationCategory) object).getId();
			return ""; //$NON-NLS-1$
		}
	};
	
	
	/**
	 * Get the currently defined categories for the workbench. Read them in if
	 * they are not set.
	 * 
	 * @return an array containing <code>ColorCategory</code>s.  This array 
	 * will be sorted based on the id of the contributing 
	 * <code>ColorCategory</code> objects. 
	 */
	public static PresentationCategory[] getCategories() {
	    initialize();
	    return categories;
	}
	
	/**
	 * Read the definitions and categories.
	 */
	private static void initialize() {
	    PresentationDefinitionReader reader = null;
	    
	    if (categories == null) {
	        reader = new PresentationDefinitionReader();
	        reader.readRegistry(Platform.getPluginRegistry());

			Collection values = reader.getCategories();
			ArrayList sorted = new ArrayList(values);
			Collections.sort(sorted, ID_COMPARATOR);
			
			categories = new PresentationCategory[sorted.size()];
			sorted.toArray(categories);
		}	    
	}
	
	
    private String description;
    private IConfigurationElement element;
	private String id;

    private String label;    
    private String pluginId;

    /**
     * 
     * @param label
     * @param id
     * @param description
     * @param pluginId
     * @param element
     */
    public PresentationCategory(
            String label,
			String id,
			String description,
			String pluginId,
			IConfigurationElement element) {
	    
        this.label = label;
	    this.id = id;
	    this.description = description;
	    this.pluginId = pluginId;
	    this.element = element;	    
	}
    
    /**
     * @return Returns the <code>IColorExample</code> for this category.  If one
     * is not available, <code>null</code> is returned.
     */
    public IPresentationPreview createPreview() throws CoreException {
        String classString = element.getAttribute("class"); //$NON-NLS-1$
        if (classString == null || "".equals(classString)) //$NON-NLS-1$
                return null;
        return (IPresentationPreview) WorkbenchPlugin.createExtension(element, PresentationDefinitionReader.ATT_CLASS);
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
    
    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }
    
    /**
     * @return Returns the label.
     */
    public String getLabel() {
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
}
