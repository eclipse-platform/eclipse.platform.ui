package org.eclipse.team.internal.ui.target;

import org.eclipse.team.internal.ui.ConfigureProjectWizard;
import org.eclipse.team.internal.ui.UIConstants;

public class ConfigureTargetWizard extends ConfigureProjectWizard {
	/**
	 * @see ConfigureProjectWizard#getExtensionPoint()
	 */
	protected String getExtensionPoint() {
		return UIConstants.PT_TARGETCONFIG;
	}
}
