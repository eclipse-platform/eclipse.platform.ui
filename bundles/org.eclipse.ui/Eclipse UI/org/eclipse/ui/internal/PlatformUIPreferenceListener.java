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
		if (event
			.getProperty()
			.equals(IPreferenceConstants.ENABLED_DECORATORS))
			WorkbenchPlugin
				.getDefault()
				.getDecoratorManager()
				.restoreListeners();

		if (event
			.getProperty()
			.equals(IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID)) {
			//Check that they do not match as setDefaultPerspective updates the store
			//and we want to avoid infinite loops
			if (! event.getNewValue().equals(event.getOldValue())) {
				IPerspectiveRegistry perspectiveRegistry = getWorkbench().getPerspectiveRegistry();
				perspectiveRegistry.setDefaultPerspective((String) event.getNewValue());
			}
		}
	}

	private IWorkbench getWorkbench() {
		return WorkbenchPlugin.getDefault().getWorkbench();
	}
}
