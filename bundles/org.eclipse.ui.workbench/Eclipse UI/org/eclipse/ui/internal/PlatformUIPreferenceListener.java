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


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * The PlatformUIPreferenceListener is a class that listens to 
 * changes in the preference store and propogates the change
 * for any special cases that require updating of other
 * values within the workbench.
 */
public class PlatformUIPreferenceListener implements IPropertyChangeListener {

	/**
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {

		String propertyName = event.getProperty();
		if (IPreferenceConstants.ENABLED_DECORATORS.equals(propertyName)) {
			WorkbenchPlugin
				.getDefault()
				.getDecoratorManager()
				.restoreListeners();
			return;
		}
		
		if (IWorkbenchPreferenceConstants
			.DEFAULT_PERSPECTIVE_ID
			.equals(propertyName)) {
			IWorkbench workbench = PlatformUI.getWorkbench();
			
			workbench.getPerspectiveRegistry().setDefaultPerspective((String) event.getNewValue());
			return;
		}
		
		if (IPreferenceConstants.DOCK_PERSPECTIVE_BAR
			.equals(propertyName)) {
			IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow[] workbenchWindows = workbench.getWorkbenchWindows();
			for (int i = 0; i < workbenchWindows.length; i++) {
				IWorkbenchWindow window = workbenchWindows[i];
				if (window instanceof WorkbenchWindow)
					((WorkbenchWindow)window).dockPerspectiveBar(preferenceStore.getBoolean(IPreferenceConstants.DOCK_PERSPECTIVE_BAR));
			}
			return;
		}
	}
}
