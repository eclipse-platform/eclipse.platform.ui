package org.eclipse.ui.externaltools.internal.model;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ExternalToolsModelMessages {

	private static final String BUNDLE_NAME = "org.eclipse.ui.externaltools.internal.model.ExternalToolsModelMessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private ExternalToolsModelMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
