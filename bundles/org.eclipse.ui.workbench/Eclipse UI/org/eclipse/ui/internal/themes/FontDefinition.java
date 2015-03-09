/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import org.eclipse.e4.ui.internal.css.swt.definition.IFontDefinitionOverridable;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.PlatformUI;

/**
 * The FontDefiniton is the representation of the fontDefinition
 * from the plugin.xml of a type.
 */
public class FontDefinition extends ThemeElementDefinition implements
		IHierarchalThemeElementDefinition, ICategorizedThemeElementDefinition, IEditable,
		IFontDefinitionOverridable {

	private String defaultsTo;

    private String value;

	private String defaultValue;

    private boolean isEditable;

    private FontData[] parsedValue;

    /**
     * Create a new instance of the receiver.
     *
     * @param fontName The name display
     * ed in the preference page.
     * @param uniqueId The id used to refer to this definition.
     * @param defaultsId The id of the font this defaults to.
     * @param fontDescription The description of the font in the preference page.
     */
    public FontDefinition(String fontName, String uniqueId, String defaultsId,
            String value, String categoryId, boolean isEditable,
            String fontDescription) {
		super(uniqueId, fontName, fontDescription, categoryId);
        this.defaultsTo = defaultsId;
        this.value = value;
        this.isEditable = isEditable;
    }

    /**
     * Create a new instance of the receiver.
     *
     * @param originalFont the original definition.  This will be used to populate
     * all fields except defaultsTo and value.  defaultsTo will always be
     * <code>null</code>.
     * @param datas the FontData[] value
     */
    public FontDefinition(FontDefinition originalFont, FontData[] datas) {
		super(originalFont.getId(), originalFont.getName(), originalFont.getDescription(),
				originalFont.getCategoryId());
        this.isEditable = originalFont.isEditable();
        this.parsedValue = datas;
    }

    /**
     * Returns the defaultsTo. This is the id of the text font
     * that this font defualts to.
     * @return String or <pre>null</pre>.
     */
    @Override
	public String getDefaultsTo() {
        return defaultsTo;
    }

    /**
     * Returns the value.
     *
     * @return FontData []
     */
    @Override
	public FontData[] getValue() {
        if (value == null) {
			return null;
		}
        if (parsedValue == null) {
            parsedValue = JFaceResources.getFontRegistry().filterData(
                    StringConverter.asFontDataArray(value),
                    PlatformUI.getWorkbench().getDisplay());
        }

        return parsedValue;
    }

	@Override
	public void resetToDefaultValue() {
		value = defaultValue;
		parsedValue = null;
		super.resetToDefaultValue();
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
        if (obj instanceof FontDefinition) {
            return getId().equals(((FontDefinition)obj).getId());
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
	 * @see
	 * org.eclipse.e4.ui.css.swt.definition.IDefinitionOverridable#setData(java
	 * .lang.Object)
	 */
	@Override
	public void setValue(FontData[] data) {
		if (data != null && data.length > 0) {
			if (defaultValue == null) {
				defaultValue = value;
			}
			value = data[0].getName();
			parsedValue = data;
			appendState(State.OVERRIDDEN);
		}
	}
}
