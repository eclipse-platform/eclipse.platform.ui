package org.eclipse.ui.examples.fieldassist.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.examples.fieldassist.FieldAssistPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = FieldAssistPlugin.getDefault()
				.getPreferenceStore();
		store.setDefault(PreferenceConstants.PREF_SHOWERRORDECORATION, true);
		store.setDefault(PreferenceConstants.PREF_SHOWERRORCOLOR, true);
		store.setDefault(PreferenceConstants.PREF_SHOWERRORMESSAGE, true);
		store.setDefault(PreferenceConstants.PREF_SHOWWARNINGDECORATION, true);
		store.setDefault(PreferenceConstants.PREF_SHOWREQUIREDFIELDCOLOR, true);
		store.setDefault(PreferenceConstants.PREF_SHOWREQUIREDFIELDDECORATION, true);
		store.setDefault(PreferenceConstants.PREF_SHOWCONTENTPROPOSALCUE, true);
		store.setDefault(PreferenceConstants.PREF_CONTENTASSISTKEY, PreferenceConstants.PREF_CONTENTASSISTKEY1);
		store.setDefault(PreferenceConstants.PREF_CONTENTASSISTKEY_PROPAGATE, false);
		store.setDefault(PreferenceConstants.PREF_SHOWSECONDARYPOPUP, true);
		store.setDefault(PreferenceConstants.PREF_CONTENTASSISTDELAY, 1000);
		store.setDefault(PreferenceConstants.PREF_CONTENTASSISTRESULT, PreferenceConstants.PREF_CONTENTASSISTRESULT_REPLACE);
		store.setDefault(PreferenceConstants.PREF_CONTENTASSISTFILTER, PreferenceConstants.PREF_CONTENTASSISTFILTER_CHAR);
	}

}
