/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.colors;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IPluginContribution;

/**
 * A <code>ColorDefiniton </code> is the representation of the extensions 
 * defined by the <code>org.eclipse.ui.colorDefinitions</code> extension point.
 * 
 * TODO: integrate color/gradient/fonts into the presentation package
 * 
 * @since 3.0
 */
public class ColorDefinition implements IPluginContribution {

	//The elements for use by the preference page
	private static ColorDefinition [] colorDefinitions;	
	private static GradientDefinition [] gradientDefinitions;

	/**
	 * <code>Comparator</code> used for sorting ColorDefinitions based on their 
	 * dependency depth.
	 */
	public static final Comparator HIERARCHY_COMPARATOR = new Comparator() {
		public int compare(Object arg0, Object arg1) {
			String def0 = arg0 == null ? null : ((ColorDefinition) arg0).getDefaultsTo();
			String def1 = arg1 == null ? null : ((ColorDefinition) arg1).getDefaultsTo();

			if (def0 == null && def1 == null)
				return 0;

			if (def0 == null)
				return -1;

			if (def1 == null)
				return 1;

			return compare(getDef(def0), getDef(def1));
		}

		/** 
		 * @param id the identifier to search for.
		 * @return the <code>ColorDefinition</code> that matches the id.
		 */
		private Object getDef(String id) {
			int idx = Arrays.binarySearch(ColorDefinition.getColorDefinitions(), id, ID_COMPARATOR);
			if (idx >= 0) 
				return ColorDefinition.getColorDefinitions()[idx];
			return null;
		}
	};

	/**
	 * <code>Comparator</code> used in <code>ColorDefinition</code> []
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
			else if (object instanceof ColorDefinition)
				return ((ColorDefinition) object).getId();
			return ""; //$NON-NLS-1$
		}
	};

	/**
	 * Get the currently defined definitions for the workbench. Read them in if
	 * they are not set.
	 * 
	 * @return an array containing <code>ColorDefinition</code>s.  This array 
	 * will be sorted based on the id of the contributing 
	 * <code>ColorDefinition</code> objects. 
	 */
	public static ColorDefinition[] getColorDefinitions() {
	    initialize();
		return colorDefinitions;
	}
	
	/**
	 * Get the currently defined definitions for the workbench. Read them in if
	 * they are not set.
	 * 
	 * @return an array containing <code>ColorDefinition</code>s.  This array 
	 * will be sorted based on the id of the contributing 
	 * <code>ColorDefinition</code> objects. 
	 */
	public static GradientDefinition[] getGradientDefinitions() {
	    initialize();
		return gradientDefinitions;
	}	
	
	/**
	 * Read the definitions and categories.
	 */
	private static void initialize() {
	    ColorDefinitionReader reader = null;
	    
	    if (colorDefinitions == null || gradientDefinitions == null) {
	        reader = new ColorDefinitionReader();
	        reader.readRegistry(Platform.getPluginRegistry());
		}
	    
	    if (colorDefinitions == null) {
			Collection values = reader.getColorDefinitions();			
			colorDefinitions = new ColorDefinition[values.size()];
			values.toArray(colorDefinitions);				        
	    }
	    
	    if (gradientDefinitions == null) {
			Collection values = reader.getGradientDefinitions();			
			gradientDefinitions = new GradientDefinition[values.size()];
			values.toArray(gradientDefinitions);			
	    }

	}
	
	// for dynamic UI
	public static final void clearCache() {
		colorDefinitions = null;
		gradientDefinitions = null;
	}
	
	private String defaultsTo;
	private String description;
	private String id;
	private String label;
	private String pluginId;
	private String rawValue;
	private String categoryId;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param label the label for this definition
	 * @param id the identifier for this definition
	 * @param defaultsTo the id of a definition that this definition will 
	 * 		default to.
	 * @param value the default value of this definition, either in the form 
	 * rrr,ggg,bbb or the name of an SWT color constant. 
	 * @param description the description for this definition.
	 * @param pluginId the identifier of the plugin that contributed this 
	 * 		definition.
	 */
	public ColorDefinition(
		String label,
		String id,
		String defaultsTo,
		String value,
		String categoryId,
		String description,
		String pluginId) {

		this.label = label;
		this.id = id;
		this.defaultsTo = defaultsTo;
		this.rawValue = value;
		this.categoryId = categoryId;
		this.description = description;
		this.pluginId = pluginId;
	}

    /**
     * @return the categoryId, or <code>null</code> if none was supplied.
     */
    public String getCategoryId() {
        return categoryId;
    }	
	
	/**
	 * @return the defaultsTo value, or <code>null</code> if none was supplied.
	 */
	public String getDefaultsTo() {
		return defaultsTo;
	}

	/**
	 * @return the description text, or <code>null</code> if none was supplied.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the id of this definition.  Should not be <code>null</code>.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the label text.  Should not be <code>null</code>.
	 */
	public String getLabel() {
		return label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPluginContribution#getLocalId()
	 */
	public String getLocalId() {
		return getId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPluginContribution#getPluginId()
	 */
	public String getPluginId() {
		return pluginId;
	}

	/**
	 * @return the value.  It will either be <code>null</code> or a 
	 *		<code>String</code> in the form rrr,ggg,bbb.  Any SWT constants 
	 *		supplied to the constructor will be evaluated and converted into 
	 *		their RGB value.
	 */
	public String getValue() {
	    return ColorUtils.getColorValue(rawValue);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getId();
	}	
}
