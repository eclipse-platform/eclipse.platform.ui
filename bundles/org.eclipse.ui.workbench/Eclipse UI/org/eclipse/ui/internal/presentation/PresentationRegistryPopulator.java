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

import java.util.Arrays;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.GradientData;
import org.eclipse.jface.resource.GradientRegistry;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;


/**
 * @since 3.0
 */
public final class PresentationRegistryPopulator {

    public static void populateRegistry(FontRegistry registry, FontDefinition [] definitions, IPreferenceStore store) {
		// sort the definitions by dependant ordering so that we process 
		// ancestors before children.		
		FontDefinition [] copyOfDefinitions = new FontDefinition[definitions.length];
		System.arraycopy(definitions, 0, copyOfDefinitions, 0, definitions.length);
		Arrays.sort(copyOfDefinitions, FontDefinition.HIERARCHY_COMPARATOR);

		for (int i = 0; i < copyOfDefinitions.length; i++) {
			FontDefinition definition = copyOfDefinitions[i];
			installFont(definition, registry, store);
		}
    }
    
    
    /**
     * @param definition
     * @param registry
     * @param store
     */
    private static void installFont(FontDefinition definition, FontRegistry registry, IPreferenceStore store) {
		String id = definition.getId();
		FontData [] prefFont = store != null ? PreferenceConverter.getFontDataArray(store, id) : null;
		FontData [] defaultFont = null;
		if (definition.getValue() != null)
		    defaultFont = new FontData [] {StringConverter.asFontData(definition.getValue(), null)};
		else if (definition.getDefaultsTo() != null)
		    defaultFont = registry.getFontData(definition.getDefaultsTo());
		else
		    defaultFont = PreferenceConverter.FONTDATA_ARRAY_DEFAULT_DEFAULT;
		    
		
		if (prefFont == null || prefFont == PreferenceConverter.FONTDATA_ARRAY_DEFAULT_DEFAULT) {
		    prefFont = defaultFont;
		}
		
		if (defaultFont != null && store != null) {
			PreferenceConverter.setDefault(
					store, 
					id, 
					defaultFont);
		}

		
		if (prefFont != null) {		    
			registry.put(id, prefFont);
		}
    }


    public static void populateRegistry(GradientRegistry registry, GradientDefinition [] definitions, IPreferenceStore store) {		
		for (int i = 0; i < definitions.length; i++) {
			installGradient(definitions[i], registry, store);
		}        
    }
        
    
    /**
     * @param definition
     * @param registry
     * @param store
     */
    private static void installGradient(GradientDefinition definition, GradientRegistry registry, IPreferenceStore store) {
		String id = definition.getId();
		GradientData prefGradient = store != null ? PreferenceConverter.getGradient(store, id) : null;
		
		String[] values = definition.getValues();
        RGB [] rgbs = new RGB[values.length];
		for (int i = 0; i < rgbs.length; i++) {
            rgbs[i] = StringConverter.asRGB(values[i], null);
        }
        
        GradientData defaultGradient = new GradientData(rgbs, definition.getPercentages(), definition.getDirection());		
		
		if (prefGradient == null || prefGradient == PreferenceConverter.GRADIENT_DEFAULT_DEFAULT) {
		    prefGradient = defaultGradient;
		}
		
		if (defaultGradient != null && store != null) {
			PreferenceConverter.setDefault(
					store, 
					id, 
					defaultGradient);
		}
		
		if (prefGradient != null) {		    
			registry.put(id, prefGradient);
		}
    }

    public static void populateRegistry(ColorRegistry registry, ColorDefinition [] definitions, IPreferenceStore store) {
		// sort the definitions by dependant ordering so that we process 
		// ancestors before children.		
		ColorDefinition [] copyOfDefinitions = new ColorDefinition[definitions.length];
		System.arraycopy(definitions, 0, copyOfDefinitions, 0, definitions.length);
		Arrays.sort(copyOfDefinitions, ColorDefinition.HIERARCHY_COMPARATOR);

		for (int i = 0; i < copyOfDefinitions.length; i++) {
			ColorDefinition definition = copyOfDefinitions[i];
			installColor(definition, registry, store);
		}        
    }
    
	/**
	 * Installs the given color in the color registry.
	 * 
	 * @param definition
	 *            the color definition
	 * @param registry
	 *            the color registry
	 * @param store
	 *            the preference store from which to set and obtain color data
	 */
	private static void installColor(
		ColorDefinition definition,
		ColorRegistry registry,
		IPreferenceStore store) {
				
		String id = definition.getId();
		RGB prefColor = store != null ? PreferenceConverter.getColor(store, id) : null;
		RGB defaultColor = null;
		if (definition.getValue() != null)
		    defaultColor = StringConverter.asRGB(definition.getValue(), null);
		else 
		    defaultColor = registry.getRGB(definition.getDefaultsTo());
		
		if (prefColor == null || prefColor == PreferenceConverter.COLOR_DEFAULT_DEFAULT) {
		    prefColor = defaultColor;
		}
		
		if (defaultColor != null && store != null) {
			PreferenceConverter.setDefault(
					store, 
					id, 
					defaultColor);
		}

		
		if (prefColor != null) {		    
			registry.put(id, prefColor);
		}
	}

    
    /**
     * Not intended to be instantiated.
     */
    private PresentationRegistryPopulator() {
        // no-op
    }
}
