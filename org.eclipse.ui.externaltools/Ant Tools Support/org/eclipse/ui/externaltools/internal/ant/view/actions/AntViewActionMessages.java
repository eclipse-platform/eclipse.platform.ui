package org.eclipse.ui.externaltools.internal.ant.view.actions;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class AntViewActionMessages {

	private static final String BUNDLE_NAME = "org.eclipse.ui.externaltools.internal.ant.view.actions.AntViewActionMessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private AntViewActionMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
