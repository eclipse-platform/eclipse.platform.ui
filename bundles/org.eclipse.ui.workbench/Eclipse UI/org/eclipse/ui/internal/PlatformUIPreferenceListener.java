package org.eclipse.ui.internal;

/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.fonts.FontDefinition;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The PlatformUIPreferenceListener is a class that listens to 
 * changes in the preference store and propogates the change
 * for any special cases that require updating of other
 * values within the workbench.
 */
public class PlatformUIPreferenceListener implements IPropertyChangeListener {

	//The values that we need to check default fonts for
	private Set defaultCheckNames;
	/**
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (IPreferenceConstants
			.ENABLED_DECORATORS
			.equals(event.getProperty()))
			WorkbenchPlugin
				.getDefault()
				.getDecoratorManager()
				.restoreListeners();

		if (IWorkbenchPreferenceConstants
			.DEFAULT_PERSPECTIVE_ID
			.equals(event.getProperty())) {
			IWorkbench workbench = WorkbenchPlugin.getDefault().getWorkbench();
			AbstractUIPlugin uiPlugin =
				(AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);

			String newValue =
				uiPlugin.getPreferenceStore().getString(
					IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID);

			workbench.getPerspectiveRegistry().setDefaultPerspective(newValue);
		}

		checkForFontUpdates(event);
	}

	/**
	 * Check to see if the event is updating one of the fonts 
	 * that get propogated.
	 * @param event
	 */
	private void checkForFontUpdates(PropertyChangeEvent event) {

		//Collect the names we know we care about
		if (defaultCheckNames == null) {
			defaultCheckNames = new HashSet();
			FontDefinition[] definitions = FontDefinition.getDefinitions();
			for (int i = 0; i < definitions.length; i++) {
				String defaultsTo = definitions[i].getDefaultsTo();
				if (defaultsTo != null)
					defaultCheckNames.add(defaultsTo);
			}
		}

		String propertyName = event.getProperty();
		if (defaultCheckNames.contains(propertyName)) {
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
	}

}
