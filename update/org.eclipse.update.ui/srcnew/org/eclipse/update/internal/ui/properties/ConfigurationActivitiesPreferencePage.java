package org.eclipse.update.internal.ui.properties;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.IWorkbenchPropertyPage;

/**
 * @see PropertyPage
 */
public class ConfigurationActivitiesPreferencePage extends PropertyPage implements IWorkbenchPropertyPage {
	/**
	 *
	 */
	public ConfigurationActivitiesPreferencePage() {
		noDefaultAndApplyButton();
	}

	/**
	 * @see PropertyPage#createContents
	 */
	protected Control createContents(Composite parent)  {
		return null;
	}
}
