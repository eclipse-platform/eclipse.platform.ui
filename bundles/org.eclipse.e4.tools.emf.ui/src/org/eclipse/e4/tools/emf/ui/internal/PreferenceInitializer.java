package org.eclipse.e4.tools.emf.ui.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences pref = InstanceScope.INSTANCE.getNode("org.eclipse.e4.tools.emf.ui");

		if (pref.get("notRenderedColor", null) == null) {
			String val = StringConverter.asString(new RGB(200, 200, 200));
			pref.put("notRenderedColor", val);
		}

		if (pref.get("notVisibleColor", null) == null) {
			String val = StringConverter.asString(new RGB(200, 200, 200));
			pref.put("notVisibleColor", val);
		}

		if (pref.get("notVisibleAndRenderedColor", null) == null) {
			String val = StringConverter.asString(new RGB(200, 200, 200));
			pref.put("notVisibleAndRenderedColor", val);
		}

		if (pref.get("autoCreateElementId", null) == null) {
			pref.putBoolean("autoCreateElementId", true);
		}
	}

}
