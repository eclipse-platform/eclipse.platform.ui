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
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/*
 * The page for setting the Search preferences.
 */
public class SearchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/*
	 * XXX: Need to subclass due to bug: 18183: setEnabled(...) for FieldEditors
	 */	
	private class ColorEditor extends ColorFieldEditor {
		public ColorEditor(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
		}		
		void setEnabled(boolean state) {
			getLabelControl().setEnabled(state);
			getChangeControl(getFieldEditorParent()).setEnabled(state);
		}
	}


	public static final String EMPHASIZE_POTENTIAL_MATCHES= "org.eclipse.search.potentialMatch.emphasize"; //$NON-NLS-1$
	public static final String POTENTIAL_MATCH_FG_COLOR= "org.eclipse.search.potentialMatch.fgColor"; //$NON-NLS-1$
	public static final String REUSE_EDITOR= "org.eclipse.search.reuseEditor"; //$NON-NLS-1$

	private ColorEditor fColorEditor;
	private BooleanFieldEditor fEmphasizedCheckbox;
	private Composite fParent;

	public SearchPreferencePage() {
		super(GRID);
		setPreferenceStore(SearchPlugin.getDefault().getPreferenceStore());
	}

	public static void initDefaults(IPreferenceStore store) {
		RGB gray= new RGB(85, 85, 85);
		store.setDefault(EMPHASIZE_POTENTIAL_MATCHES, true);
		PreferenceConverter.setDefault(store, POTENTIAL_MATCH_FG_COLOR, gray);
		store.setDefault(REUSE_EDITOR, false);
	}

	public static boolean isEditorReused() {
		IPreferenceStore store= SearchPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(REUSE_EDITOR);
	}

	public static boolean arePotentialMatchesEmphasized() {
		IPreferenceStore store= SearchPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(EMPHASIZE_POTENTIAL_MATCHES);
	}

	public static RGB getPotentialMatchBackgroundColor() {
		IPreferenceStore store= SearchPlugin.getDefault().getPreferenceStore();
		return PreferenceConverter.getColor(store, POTENTIAL_MATCH_FG_COLOR);
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(getControl(), ISearchHelpContextIds.SEARCH_PREFERENCE_PAGE);
	}
	
	protected void createFieldEditors() {
		Composite parent= getFieldEditorParent();

		BooleanFieldEditor boolEditor= new BooleanFieldEditor(
			REUSE_EDITOR,
			SearchMessages.getString("SearchPreferencePage.reuseEditor"), //$NON-NLS-1$
			parent
        );
		addField(boolEditor);

		fEmphasizedCheckbox= new BooleanFieldEditor(
			EMPHASIZE_POTENTIAL_MATCHES,
			SearchMessages.getString("SearchPreferencePage.emphasizePotentialMatches"), //$NON-NLS-1$
			parent);
		addField(fEmphasizedCheckbox);

		fColorEditor= new ColorEditor(
			POTENTIAL_MATCH_FG_COLOR,
			SearchMessages.getString("SearchPreferencePage.potentialMatchFgColor"), //$NON-NLS-1$
			parent
        );
		addField(fColorEditor);
		fColorEditor.setEnabled(arePotentialMatchesEmphasized());
	}

	public void propertyChange(PropertyChangeEvent event) {
		fColorEditor.setEnabled(fEmphasizedCheckbox.getBooleanValue());
	}

	public void init(IWorkbench workbench) {
	}

	protected void performDefaults() {
		super.performDefaults();
		fColorEditor.setEnabled(fEmphasizedCheckbox.getBooleanValue());
	}
}
