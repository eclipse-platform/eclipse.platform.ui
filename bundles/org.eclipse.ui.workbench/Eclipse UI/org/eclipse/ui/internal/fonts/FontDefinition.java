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
package org.eclipse.ui.internal.fonts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.internal.WorkbenchPlugin;


/**
 * The FontDefiniton is the representation of the fontDefinition
 * from the plugin.xml of a type.
 */
public class FontDefinition {

	/**
	 * The FontPreferenceListener is a class that listens to 
	 * changes in the preference store and propogates the change
	 * for any special cases that require updating of other
	 * values within the workbench.
	 */
	private static class FontPreferenceListener implements IPropertyChangeListener {

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

			FontDefinition[] defs = getDefinitions();
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
			FontDefinition[] defs = getDefinitions();
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
	
	/**
	 * <code>Comparator</code> used for sorting FontDefinitions based on their 
	 * dependency depth.
	 */
	public static final Comparator HIERARCHY_COMPARATOR = new Comparator() {
		public int compare(Object arg0, Object arg1) {
			String def0 = arg0 == null ? null : ((FontDefinition) arg0).getDefaultsTo();
			String def1 = arg1 == null ? null : ((FontDefinition) arg1).getDefaultsTo();

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
		    FontDefinition[] defs = FontDefinition.getDefinitions();
            int idx = Arrays.binarySearch(defs, id, ID_COMPARATOR);
			if (idx >= 0) 
				return defs[idx];
			return null;
		}
	};
	
	/**
	 * <code>Comparator</code> used in <code>FontDefinition</code> [] 
	 * searching.  May match against <code>String</code>s.
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
			else if (object instanceof FontDefinition)
				return ((FontDefinition) object).getId();
			return ""; //$NON-NLS-1$
		}
	};
	
	
	
	private static FontPreferenceListener fontPreferenceUpdateListener;
	
	private String label;
	private String id;
	private String defaultsTo;
	private String categoryId;
	private String description;

	//The elements for use by the preference page
	private static FontDefinition[] definitions;

	
	/**
	 * Get the currently defined definitions for the workbench. Read them in if
	 * they are not set.
	 * @return FontDefinition[]
	 */
	public static FontDefinition[] getDefinitions() {
		if (definitions == null) {
			FontDefinitionReader reader = new FontDefinitionReader();
			Collection values =
				reader.readRegistry(Platform.getPluginRegistry());
			definitions = new FontDefinition[values.size()];			
			ArrayList sorted = new ArrayList(values);
			Collections.sort(sorted, ID_COMPARATOR);

			sorted.toArray(definitions);			
			
		}
		return definitions;
	}
	
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
	
	
	// clears the definitions cache.
	// for Dynamic UI
	public static void clearCache() {
		definitions = null;
		if (fontPreferenceUpdateListener != null)
			fontPreferenceUpdateListener.clearCache();
		
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
	FontDefinition(
		String fontName,
		String uniqueId,
		String defaultsId,
		String categoryId,
		String fontDescription) {
		this.label = fontName;
		this.id = uniqueId;
		this.defaultsTo = defaultsId;
		this.categoryId = categoryId;
		this.description = fontDescription;
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
}
