/*******************************************************************************
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - Bug 431735
 ******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences pref = InstanceScope.INSTANCE.getNode("org.eclipse.e4.tools.emf.ui"); //$NON-NLS-1$

		if (pref.get("notRenderedColor", null) == null) { //$NON-NLS-1$
			String val = StringConverter.asString(new RGB(200, 200, 200));
			pref.put("notRenderedColor", val); //$NON-NLS-1$
		}

		if (pref.get("notVisibleColor", null) == null) { //$NON-NLS-1$
			String val = StringConverter.asString(new RGB(200, 200, 200));
			pref.put("notVisibleColor", val); //$NON-NLS-1$
		}

		if (pref.get("notVisibleAndRenderedColor", null) == null) { //$NON-NLS-1$
			String val = StringConverter.asString(new RGB(200, 200, 200));
			pref.put("notVisibleAndRenderedColor", val); //$NON-NLS-1$
		}

		if (pref.get("autoCreateElementId", null) == null) { //$NON-NLS-1$
			pref.putBoolean("autoCreateElementId", true); //$NON-NLS-1$
		}

		// TODO: if accepted, this should default to true
		if (pref.get("tab-list-show", null) == null) { //$NON-NLS-1$
			pref.putBoolean("tab-list-show", false); //$NON-NLS-1$
		}

		// TODO: After XMI tab is not forced disabled, this should default to
		// true
		if (pref.get("tab-form-search-show", null) == null) { //$NON-NLS-1$
			pref.putBoolean("tab-form-search-show", false); //$NON-NLS-1$
		}
	}

}
