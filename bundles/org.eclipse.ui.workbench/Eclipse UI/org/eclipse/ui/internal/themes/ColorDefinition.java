/*******************************************************************************
 * Copyright (c) 2003, 2014 IBM Corporation and others.
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
import org.eclipse.e4.ui.internal.css.swt.definition.IColorDefinitionOverridable;
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
public class ColorDefinition extends ThemeElementDefinition implements IPluginContribution,
		IHierarchalThemeElementDefinition, ICategorizedThemeElementDefinition, IEditable,
		IColorDefinitionOverridable {
	/**
	 * Default color value - black - for colors that cannot be parsed.
	 */
    private static final RGB DEFAULT_COLOR_VALUE = new RGB(0,0,0);

	private String defaultsTo;

    private String pluginId;

    private String rawValue;

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
		super(id, label, description, categoryId);
        this.defaultsTo = defaultsTo;
        this.rawValue = value;
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
		super(original.getId(), original.getName(), original.getDescription(), original
				.getCategoryId());
        this.isEditable = original.isEditable();
        this.pluginId = original.getPluginId();
        this.parsedValue = value;
    }

    /**
     * @return the defaultsTo value, or <code>null</code> if none was supplied.
     */
    @Override
	public String getDefaultsTo() {
        return defaultsTo;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getLocalId()
     */
    @Override
	public String getLocalId() {
        return getId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getPluginId()
     */
    @Override
	public String getPluginId() {
        return pluginId;
    }

    /**
     * @return the value. Any SWT constants  supplied to the constructor will be 
     * evaluated and converted into their RGB value.
     */
    @Override
	public RGB getValue() {
        if (parsedValue == null) {
			try {
				parsedValue = ColorUtil.getColorValue(rawValue);
			} catch (DataFormatException e) {
				parsedValue = DEFAULT_COLOR_VALUE;
				IStatus status = StatusUtil.newStatus(IStatus.WARNING,
						"Could not parse value for theme color " + getId(), e); //$NON-NLS-1$
				StatusManager.getManager().handle(status, StatusManager.LOG);
			}
		}
        return parsedValue;
    }

	@Override
	public void resetToDefaultValue() {
		parsedValue = null;
		super.resetToDefaultValue();
	}

    /*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
    @Override
	public String toString() {
        return getId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IEditable#isEditable()
     */
    @Override
	public boolean isEditable() {
        return isEditable;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object obj) {
        if (obj instanceof ColorDefinition) {
            return getId().equals(((ColorDefinition)obj).getId());
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
	public int hashCode() {
		return getId().hashCode();
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.internal.css.swt.definition.
	 * IThemeElementDefinitionOverridable#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(RGB data) {
		if (data != null) {
			parsedValue = data;
			appendState(State.OVERRIDDEN);
		}
	}
}
