/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/*
 * The page for setting the Search preferences.
 */
public class SearchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String POTENTIAL_MATCH_BG_COLOR= "org.eclipse.search.potentialMatch.bgColor"; //$NON-NLS-1$
	public static final String REUSE_EDITOR= "org.eclipse.search.reuseEditor"; //$NON-NLS-1$


	public SearchPreferencePage() {
		super(GRID);

		setPreferenceStore(SearchPlugin.getDefault().getPreferenceStore());
	}

	public static void initDefaults(IPreferenceStore store) {
		RGB lightSkyBlue= new RGB(176, 226, 255);
		PreferenceConverter.setValue(store, POTENTIAL_MATCH_BG_COLOR, lightSkyBlue);
		store.setDefault(REUSE_EDITOR, false);
	}

	public static boolean isEditorReused() {
		IPreferenceStore store= SearchPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(REUSE_EDITOR);
	}

	public static RGB getPotentialMatchBackgroundColor() {
		IPreferenceStore store= SearchPlugin.getDefault().getPreferenceStore();
		return PreferenceConverter.getColor(store, POTENTIAL_MATCH_BG_COLOR);
	}

	/*
	 * Method declared on PreferencePage 
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(getControl(), ISearchHelpContextIds.SEARCH_PREFERENCE_PAGE);
	}
	
	protected void createFieldEditors() {
		Composite parent= getFieldEditorParent();

		ColorFieldEditor colorEditor= new ColorFieldEditor(
			POTENTIAL_MATCH_BG_COLOR,
			SearchMessages.getString("SearchPreferencePage.potentialMatchBgColor"), //$NON-NLS-1$
			parent
        );
		addField(colorEditor);

		BooleanFieldEditor boolEditor= new BooleanFieldEditor(
			REUSE_EDITOR,
			SearchMessages.getString("SearchPreferencePage.reuseEditor"), //$NON-NLS-1$
			parent
        );
		addField(boolEditor);
	}

	/*
	 * Method declared on IWorkbenchPreferencePage
	 */
	public void init(IWorkbench workbench) {
	}
}
