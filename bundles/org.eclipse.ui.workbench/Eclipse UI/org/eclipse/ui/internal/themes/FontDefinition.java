/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.DataFormatException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;


/**
 * The FontDefiniton is the representation of the fontDefinition
 * from the plugin.xml of a type.
 */
public class FontDefinition implements IHierarchalThemeElementDefinition, ICategorizedThemeElementDefinition {

	
	private static final String FONT_SEPERATOR = ";"; //$NON-NLS-1$
	
    /**
	 * The FontPreferenceListener is a class that listens to 
	 * changes in the preference store and propogates the change
	 * for any special cases that require updating of other
	 * values within the workbench.
	 * 
	 * TODO is this necessary anymore?
	 */
	public static class FontPreferenceListener implements IPropertyChangeListener {

		//The values that we need to check default fonts for
		private Set defaultCheckNames;
		//The names of all of the fonts that will require updating
		private Set fontNames;
		/**
		 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {

			String propertyName = event.getProperty();

			//Collect the names if required
			if (defaultCheckNames == null) {
				initializeFontNames();
			}

			if (defaultCheckNames.contains(propertyName)) {
				processDefaultsTo(propertyName);
			}
			if (fontNames.contains(propertyName)) {
				FontData[] newSetting;
				Object newValue = event.getNewValue();
				
				if (newValue == null)
					return;
				
				//The preference change can come as as a String or a FontData[]
				//so make sure we have the right type
				if(newValue instanceof String)
					newSetting = 
						PreferenceConverter.readFontData((String) newValue);
				else
					newSetting = (FontData[]) newValue;
				
				JFaceResources.getFontRegistry().put(propertyName, newSetting);
				
			}
		}

		/**
		 * There has been an update to a font that other fonts
		 * default to. Propogate if required.
		 * @param propertyName
		 */
		private void processDefaultsTo(String propertyName) {

			FontDefinition[] defs = WorkbenchPlugin.getDefault().getThemeRegistry().getFonts();
			IPreferenceStore store =
				WorkbenchPlugin.getDefault().getPreferenceStore();
			for (int i = 0; i < defs.length; i++) {
				String defaultsTo = defs[i].getDefaultsTo();
				if (defaultsTo != null
						&& defaultsTo.equals(propertyName)
						&& store.isDefault(defs[i].getId())) {

					FontData[] data =
						PreferenceConverter.getFontDataArray(store, defaultsTo);
					JFaceResources.getFontRegistry().put(
							defs[i].getId(),
							data);
				}
			}
		}

		/**
		 * Initialixe the fontNames and the list of fonts that have a 
		 * defaultsTo tag.
		 */
		private void initializeFontNames() {
			defaultCheckNames = new HashSet();
			fontNames = new HashSet();
			FontDefinition[] defs = WorkbenchPlugin.getDefault().getThemeRegistry().getFonts();
			for (int i = 0; i < defs.length; i++) {
				fontNames.add(defs[i].getId());
				String defaultsTo = defs[i].getDefaultsTo();
				if (defaultsTo != null)
					defaultCheckNames.add(defaultsTo);
			}
		}
		
		/**
		 * For dynamic UI
		 */
		public void clearCache() {
			defaultCheckNames = null;
		}
	}
	
	private static FontPreferenceListener fontPreferenceUpdateListener;
	
	private String label;
	private String id;
	private String defaultsTo;
	private String categoryId;
	private String description;
    private String value;

    private boolean isEditable;

    private FontData [] parsedValue;
	/**
	 * For dynamic UI
	 * 
	 * @return a preference listener that will update font settings
	 * @since 3.0
	 */
	public static IPropertyChangeListener getPreferenceListener() {
		if (fontPreferenceUpdateListener == null) {
			fontPreferenceUpdateListener = new FontPreferenceListener();
		}
		return fontPreferenceUpdateListener;
		
	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param fontName The name display
	 * ed in the preference page.
	 * @param uniqueId The id used to refer to this definition.
	 * @param defaultsId The id of the font this defaults to.
	 * @param fontDescription The description of the font in the preference page.
	 */
	public FontDefinition(
		String fontName,
		String uniqueId,
		String defaultsId,
		String value,
		String categoryId,
		boolean isEditable, 
		String fontDescription) {
		this.label = fontName;
		this.id = uniqueId;
		this.defaultsTo = defaultsId;
		this.value = value;
		this.categoryId = categoryId;
		this.description = fontDescription;
		this.isEditable = isEditable;
	}

	/**
	 * Returns the defaultsTo. This is the id of the text font
	 * that this font defualts to.
	 * @return String or <pre>null</pre>.
	 */
	public String getDefaultsTo() {
		return defaultsTo;
	}

	/**
	 * Returns the description.
	 * @return String or <pre>null</pre>.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the label.
	 * @return String
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Returns the id.
	 * @return String
	 */
	public String getId() {
		return id;
	}
	
    /**
     * Returns the categoryId.
     * @return String
     */
    public String getCategoryId() {
        return categoryId;
    }
    
    /**
     * Returns the value.
     * 
     * @return FontData []
     */
    public FontData [] getValue() {
        if (value == null)
            return null;
        if (parsedValue == null) {
            String [] strings = value.split(FONT_SEPERATOR);
            ArrayList data = new ArrayList(strings.length);
            for (int i = 0; i < strings.length; i++) {            
                try {
                	data.add(StringConverter.asFontData(strings[i]));
               	}
               	catch (DataFormatException e) {
               		//do-nothing
               	}
            }
            parsedValue = JFaceResources.getFontRegistry().bestDataArray((FontData []) data.toArray(new FontData [data.size()]), Workbench.getInstance().getDisplay());
        }

        return parsedValue;
    }
    
    /**
     * @return Returns the isEditable.
     */
    public boolean isEditable() {
        return isEditable;
    }
}
