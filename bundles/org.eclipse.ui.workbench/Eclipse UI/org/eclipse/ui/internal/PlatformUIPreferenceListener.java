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
package org.eclipse.ui.internal;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.internal.fonts.FontDefinition;

/**
 * The PlatformUIPreferenceListener is a class that listens to 
 * changes in the preference store and propogates the change
 * for any special cases that require updating of other
 * values within the workbench.
 */
public class PlatformUIPreferenceListener implements IPropertyChangeListener {

	//The values that we need to check default fonts for
	private Set defaultCheckNames;
	//The names of all of the fonts that will require updating
	private Set fontNames;
	/**
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {

		String propertyName = event.getProperty();
		if (IPreferenceConstants.ENABLED_DECORATORS.equals(propertyName))
			WorkbenchPlugin
				.getDefault()
				.getDecoratorManager()
				.restoreListeners();

		if (IWorkbenchPreferenceConstants
			.DEFAULT_PERSPECTIVE_ID
			.equals(propertyName)) {
			IWorkbench workbench = WorkbenchPlugin.getDefault().getWorkbench();
			
			workbench.getPerspectiveRegistry().setDefaultPerspective((String) event.getNewValue());
		}

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

		FontDefinition[] definitions = FontDefinition.getDefinitions();
		IPreferenceStore store =
			WorkbenchPlugin.getDefault().getPreferenceStore();
		for (int i = 0; i < definitions.length; i++) {
			String defaultsTo = definitions[i].getDefaultsTo();
			if (defaultsTo != null
				&& defaultsTo.equals(propertyName)
				&& store.isDefault(definitions[i].getId())) {

				FontData[] data =
					PreferenceConverter.getFontDataArray(store, defaultsTo);
				JFaceResources.getFontRegistry().put(
					definitions[i].getId(),
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
		FontDefinition[] definitions = FontDefinition.getDefinitions();
		for (int i = 0; i < definitions.length; i++) {
			fontNames.add(definitions[i].getId());
			String defaultsTo = definitions[i].getDefaultsTo();
			if (defaultsTo != null)
				defaultCheckNames.add(defaultsTo);
		}
	}

}
