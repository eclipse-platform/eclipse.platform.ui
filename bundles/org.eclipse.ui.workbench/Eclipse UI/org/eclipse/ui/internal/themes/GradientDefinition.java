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
package org.eclipse.ui.internal.themes;

import java.util.Comparator;

import org.eclipse.ui.IPluginContribution;


/**
 * A <code>GradientDefiniton </code> is the representation of the extensions 
 * defined by the <code>org.eclipse.ui.colorDefinitions</code> extension point.
 * 
 * @since 3.0
 */
public class GradientDefinition implements IPluginContribution, ICategorizedThemeElementDefinition {

	/**
	 * <code>Comparator</code> used in <code>GradientDefinition</code> []
	 *  searching.  May match against <code>String</code>s.
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
			else if (object instanceof GradientDefinition)
				return ((GradientDefinition) object).getId();
			return ""; //$NON-NLS-1$
		}
	};
    
    
    private String label;
    private String id;
    private String [] rawValues;
    private String pluginId;
    private int[] percentages;
    private String categoryId;
    private String description;
    private int direction;

    /**
     * 
     */
    public GradientDefinition(String label,
    		String id,
    		int direction, 
    		String [] values,
    		int [] percentages,
    		String categoryId,
    		String description,
    		String pluginId) {
        
        this.label = label;
        this.direction = direction;
		this.id = id;
		this.rawValues = values;
		this.percentages = percentages;
		this.categoryId = categoryId;
		this.description = description;
		this.pluginId = pluginId;        
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
     * @return Returns the categoryId.
     */
    public String getCategoryId() {
        return categoryId;
    }
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
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
    /**
     * @return Returns the percentages.
     */
    public int[] getPercentages() {
        return percentages;
    }
    /**
     * @return Returns the rawValues.
     */
    public String[] getValues() {
        return ColorUtils.getColorValues(rawValues);
    }
    /**
     * @return Returns the direction.
     */
    public int getDirection() {
        return direction;
    }
}
