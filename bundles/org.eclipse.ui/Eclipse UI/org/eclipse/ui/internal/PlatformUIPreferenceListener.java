package org.eclipse.ui.internal;

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
