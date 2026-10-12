package org.eclipse.ui.examples.urlmodifier;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/** Stores the icon pack selected for the next start. */
public final class IconPacks {

	public static final String BOLD = "bold";
	private static final String KEY = "iconPack";

	private IconPacks() {
	}

	private static Preferences preferences() {
		return InstanceScope.INSTANCE.getNode("org.eclipse.ui.examples.urlmodifier");
	}

	public static String selected() {
		return preferences().get(KEY, null);
	}

	public static void select(String pack) throws BackingStoreException {
		Preferences preferences = preferences();
		if (pack == null) {
			preferences.remove(KEY);
		} else {
			preferences.put(KEY, pack);
		}
		preferences.flush();
	}
}
