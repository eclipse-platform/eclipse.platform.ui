package org.eclipse.ui.internal;

/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.*;

/**
 * The PlatformUIPreferenceListener is a class that listens to 
 * changes in the preference store and propogates the change
 * for any special cases that require updating of other
 * values within the workbench.
 */
class PlatformUIPreferenceListener implements IPropertyChangeListener {

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

			String newValue =
				workbench.getPreferenceStore().getString(
					IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID);

			workbench.getPerspectiveRegistry().setDefaultPerspective(newValue);
		}
	}

}
