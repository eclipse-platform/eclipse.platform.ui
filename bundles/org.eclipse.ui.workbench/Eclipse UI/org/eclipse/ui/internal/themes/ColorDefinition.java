/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.DataFormatException;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.themes.ColorUtil;

/**
 * A <code>ColorDefiniton </code> is the representation of the extensions 
 * defined by the <code>org.eclipse.ui.colorDefinitions</code> extension point.
 * 
 *  @since 3.0
 */
public class ColorDefinition implements IPluginContribution,
        IHierarchalThemeElementDefinition, ICategorizedThemeElementDefinition,
        IEditable {
	
	/**
	 * Default color value - black - for colors that cannot be parsed.
	 */
    private static final RGB DEFAULT_COLOR_VALUE = new RGB(0,0,0);

	private String defaultsTo;

    private String description;

    private String id;

    private String label;

    private String pluginId;

    private String rawValue;

    private String categoryId;

    boolean isEditable;

    private RGB parsedValue;

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
    public ColorDefinition(String label, String id, String defaultsTo,
            String value, String categoryId, boolean isEditable,
            String description, String pluginId) {

        this.label = label;
        this.id = id;
        this.defaultsTo = defaultsTo;
        this.rawValue = value;
        this.categoryId = categoryId;
        this.description = description;
        this.isEditable = isEditable;
        this.pluginId = pluginId;
    }

    /**
     * Create a new instance of the receiver.
     * 
     * @param original the original definition.  This will be used to populate 
     * all fields except defaultsTo and value.  defaultsTo will always be 
     * <code>null</code>.
     * @param value the RGB value
     */
    public ColorDefinition(ColorDefinition original, RGB value) {

        this.label = original.getName();
        this.id = original.getId();
        this.categoryId = original.getCategoryId();
        this.description = original.getDescription();
        this.isEditable = original.isEditable();
        this.pluginId = original.getPluginId();

        this.parsedValue = value;
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
    public String getName() {
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
     * @return the value. Any SWT constants  supplied to the constructor will be 
     * evaluated and converted into their RGB value.
     */
    public RGB getValue() {
        if (parsedValue == null) {
			try {
				parsedValue = ColorUtil.getColorValue(rawValue);
			} catch (DataFormatException e) {
				parsedValue = DEFAULT_COLOR_VALUE;
				IStatus status = StatusUtil.newStatus(IStatus.WARNING,
						"Could not parse value for theme color " + id, e); //$NON-NLS-1$
				StatusManager.getManager().handle(status, StatusManager.LOG);
			}
		}
        return parsedValue;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
    public String toString() {
        return getId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IEditable#isEditable()
     */
    public boolean isEditable() {
        return isEditable;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof ColorDefinition) {
            return getId().equals(((ColorDefinition)obj).getId());
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
