package org.eclipse.ui.internal.actions.keybindings;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class Messages {

	private static ResourceBundle resourceBundle = ResourceBundle.getBundle(Messages.class.getName());
	
	public static String getString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException eMissingResource) {
			System.err.println(eMissingResource);
			return key;
		}
	}

	private Messages() {
		super();
	}
}
