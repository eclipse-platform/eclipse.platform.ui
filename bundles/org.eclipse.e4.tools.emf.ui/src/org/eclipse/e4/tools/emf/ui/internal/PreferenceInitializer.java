/*******************************************************************************
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - Bug 431735, Bug 437890, Bug 440469
 ******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.tools.emf.ui.common.ModelEditorPreferences;
import org.eclipse.e4.tools.emf.ui.common.Plugin;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences pref = InstanceScope.INSTANCE.getNode(Plugin.ID);

		if (pref.get(ModelEditorPreferences.NOT_RENDERED_COLOR, null) == null) {
			String val = StringConverter.asString(new RGB(200, 200, 200));
			pref.put(ModelEditorPreferences.NOT_RENDERED_COLOR, val);
		}

		if (pref.get(ModelEditorPreferences.NOT_VISIBLE_COLOR, null) == null) {
			String val = StringConverter.asString(new RGB(200, 200, 200));
			pref.put(ModelEditorPreferences.NOT_VISIBLE_COLOR, val);
		}

		if (pref.get(ModelEditorPreferences.NOT_VISIBLE_AND_RENDERED_COLOR, null) == null) {
			String val = StringConverter.asString(new RGB(200, 200, 200));
			pref.put(ModelEditorPreferences.NOT_VISIBLE_AND_RENDERED_COLOR, val);
		}

		if (pref.get(ModelEditorPreferences.AUTO_CREATE_ELEMENT_ID, null) == null) {
			pref.putBoolean(ModelEditorPreferences.AUTO_CREATE_ELEMENT_ID, true);
		}

		// TODO: After XMI tab is not forced disabled, this should default to
		// true
		if (pref.get(ModelEditorPreferences.TAB_FORM_SEARCH_SHOW, null) == null) {
			pref.putBoolean(ModelEditorPreferences.TAB_FORM_SEARCH_SHOW, false);
		}

		if (pref.get(ModelEditorPreferences.LIST_TAB_REMEMBER_FILTERS, null) == null) {
			pref.putBoolean(ModelEditorPreferences.LIST_TAB_REMEMBER_FILTERS, false);
		}

		if (pref.get(ModelEditorPreferences.LIST_TAB_REMEMBER_COLUMNS, null) == null) {
			pref.putBoolean(ModelEditorPreferences.LIST_TAB_REMEMBER_COLUMNS, false);
		}
	}

}
