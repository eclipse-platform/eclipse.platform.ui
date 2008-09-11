/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import java.util.Arrays;
import java.util.Comparator;

import com.ibm.icu.text.Collator;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import org.eclipse.search.internal.core.text.TextSearchEngineRegistry;


/*
 * The page for setting the Search preferences.
 */
public class SearchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String PAGE_ID= "org.eclipse.search.preferences.SearchPreferencePage"; //$NON-NLS-1$


	public static final String IGNORE_POTENTIAL_MATCHES= "org.eclipse.search.potentialMatch.ignore"; //$NON-NLS-1$
	public static final String EMPHASIZE_POTENTIAL_MATCHES= "org.eclipse.search.potentialMatch.emphasize"; //$NON-NLS-1$
	public static final String POTENTIAL_MATCH_FG_COLOR= "org.eclipse.search.potentialMatch.fgColor"; //$NON-NLS-1$
	public static final String REUSE_EDITOR= "org.eclipse.search.reuseEditor"; //$NON-NLS-1$
	public static final String DEFAULT_PERSPECTIVE= "org.eclipse.search.defaultPerspective"; //$NON-NLS-1$
	private static final String NO_DEFAULT_PERSPECTIVE= "org.eclipse.search.defaultPerspective.none"; //$NON-NLS-1$
	public static final String BRING_VIEW_TO_FRONT= "org.eclipse.search.bringToFront"; //$NON-NLS-1$
    public static final String TEXT_SEARCH_ENGINE = "org.eclipse.search.textSearchEngine"; //$NON-NLS-1$
    public static final String TEXT_SEARCH_QUERY_PROVIDER = "org.eclipse.search.textSearchQueryProvider"; //$NON-NLS-1$
	public static final String LIMIT_HISTORY= "org.eclipse.search.limitHistory"; //$NON-NLS-1$

	private ColorFieldEditor fColorEditor;
	private BooleanFieldEditor fEmphasizedCheckbox;
	private BooleanFieldEditor fIgnorePotentialMatchesCheckbox;


	private static class PerspectiveDescriptorComparator implements Comparator {
		/*
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			if (o1 instanceof IPerspectiveDescriptor && o2 instanceof IPerspectiveDescriptor) {
				String id1= ((IPerspectiveDescriptor)o1).getLabel();
				String id2= ((IPerspectiveDescriptor)o2).getLabel();
				return Collator.getInstance().compare(id1, id2);
			}
			return 0;
		}
	}



	public SearchPreferencePage() {
		super(GRID);
		setPreferenceStore(SearchPlugin.getDefault().getPreferenceStore());
	}

	public static void initDefaults(IPreferenceStore store) {
		RGB gray= new RGB(85, 85, 85);
		store.setDefault(EMPHASIZE_POTENTIAL_MATCHES, true);
		store.setDefault(IGNORE_POTENTIAL_MATCHES, false);
		PreferenceConverter.setDefault(store, POTENTIAL_MATCH_FG_COLOR, gray);
		store.setDefault(REUSE_EDITOR, true);
		store.setDefault(BRING_VIEW_TO_FRONT, true);
		store.setDefault(DEFAULT_PERSPECTIVE, NO_DEFAULT_PERSPECTIVE);
		store.setDefault(TEXT_SEARCH_ENGINE, ""); //default search engine is empty string //$NON-NLS-1$
		store.setDefault(TEXT_SEARCH_QUERY_PROVIDER, ""); // default query provider is empty string  //$NON-NLS-1$
		store.setDefault(LIMIT_HISTORY, 10);
	}


	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ISearchHelpContextIds.SEARCH_PREFERENCE_PAGE);
	}

	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(REUSE_EDITOR, SearchMessages.SearchPreferencePage_reuseEditor, getFieldEditorParent()));
		addField(new BooleanFieldEditor(BRING_VIEW_TO_FRONT, SearchMessages.SearchPreferencePage_bringToFront, getFieldEditorParent()));

		fIgnorePotentialMatchesCheckbox= new BooleanFieldEditor(
			IGNORE_POTENTIAL_MATCHES,
			SearchMessages.SearchPreferencePage_ignorePotentialMatches,
			getFieldEditorParent());
		addField(fIgnorePotentialMatchesCheckbox);

		fEmphasizedCheckbox= new BooleanFieldEditor(
			EMPHASIZE_POTENTIAL_MATCHES,
			SearchMessages.SearchPreferencePage_emphasizePotentialMatches,
			getFieldEditorParent());
		addField(fEmphasizedCheckbox);

		fColorEditor= new ColorFieldEditor(
			POTENTIAL_MATCH_FG_COLOR,
			SearchMessages.SearchPreferencePage_potentialMatchFgColor,
			getFieldEditorParent()
        );
		addField(fColorEditor);

		fEmphasizedCheckbox.setEnabled(!arePotentialMatchesIgnored(), getFieldEditorParent());
		fColorEditor.setEnabled(!arePotentialMatchesIgnored() && arePotentialMatchesEmphasized(), getFieldEditorParent());

		handleDeletedPerspectives();
		String[][] perspectiveNamesAndIds = getPerspectiveNamesAndIds();
		ComboFieldEditor comboEditor= new ComboFieldEditor(
			DEFAULT_PERSPECTIVE,
			SearchMessages.SearchPreferencePage_defaultPerspective,
			perspectiveNamesAndIds,
			getFieldEditorParent());
		addField(comboEditor);

        // in case we have a contributed engine, let the user choose.
        TextSearchEngineRegistry reg= SearchPlugin.getDefault().getTextSearchEngineRegistry();
        String[][] engineNamesAndIds= reg.getAvailableEngines();
        if (engineNamesAndIds.length > 1) {
            comboEditor= new ComboFieldEditor(
                    TEXT_SEARCH_ENGINE,
                    SearchMessages.SearchPreferencePage_textSearchEngine,
                    engineNamesAndIds,
                    getFieldEditorParent());
            addField(comboEditor);
        }
	}

	public void setVisible(boolean state) {
		handleDeletedPerspectives();
		super.setVisible(state);
	}

	public void propertyChange(PropertyChangeEvent event) {
		updateFieldEnablement();
	}

	public void init(IWorkbench workbench) {
	}

	protected void performDefaults() {
		super.performDefaults();
		updateFieldEnablement();
	}


	private void updateFieldEnablement() {
		boolean arePotentialMatchesIgnored= fIgnorePotentialMatchesCheckbox.getBooleanValue();
		fEmphasizedCheckbox.setEnabled(!arePotentialMatchesIgnored, getFieldEditorParent());
		fColorEditor.setEnabled(!arePotentialMatchesIgnored && fEmphasizedCheckbox.getBooleanValue(), getFieldEditorParent());
	}

	/*
	 * Return a 2-dimensional array of perspective names and ids.
	 */
	private String[][] getPerspectiveNamesAndIds() {

		IPerspectiveRegistry registry= PlatformUI.getWorkbench().getPerspectiveRegistry();
		IPerspectiveDescriptor[] perspectiveDescriptors= registry.getPerspectives();

		Arrays.sort(perspectiveDescriptors, new PerspectiveDescriptorComparator());

		String[][] table = new String[perspectiveDescriptors.length + 1][2];
		table[0][0] = SearchMessages.SearchPreferencePage_defaultPerspective_none;
		table[0][1] = NO_DEFAULT_PERSPECTIVE;
		for (int i = 0; i < perspectiveDescriptors.length; i++) {
			table[i + 1][0] = perspectiveDescriptors[i].getLabel();
			table[i + 1][1] = perspectiveDescriptors[i].getId();
		}
		return table;
	}

	private static void handleDeletedPerspectives() {
		IPreferenceStore store= SearchPlugin.getDefault().getPreferenceStore();
		String id= store.getString(DEFAULT_PERSPECTIVE);
		if (PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(id) == null) {
			store.putValue(DEFAULT_PERSPECTIVE, NO_DEFAULT_PERSPECTIVE);
		}
	}


	// Accessors to preference values
	public static String getDefaultPerspectiveId() {
		handleDeletedPerspectives();
		IPreferenceStore store= SearchPlugin.getDefault().getPreferenceStore();
		String id= store.getString(DEFAULT_PERSPECTIVE);
		if (id == null || id.length() == 0 || id.equals(NO_DEFAULT_PERSPECTIVE))
			return null;
		else if (PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(id) == null) {
			store.putValue(DEFAULT_PERSPECTIVE, id);
			return null;
		}
		return id;
	}

	public static boolean isEditorReused() {
		IPreferenceStore store= SearchPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(REUSE_EDITOR);
	}

	public static boolean isViewBroughtToFront() {
		IPreferenceStore store= SearchPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(BRING_VIEW_TO_FRONT);
	}

	public static boolean arePotentialMatchesIgnored() {
		IPreferenceStore store= SearchPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(IGNORE_POTENTIAL_MATCHES);
	}

	public static boolean arePotentialMatchesEmphasized() {
		IPreferenceStore store= SearchPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(EMPHASIZE_POTENTIAL_MATCHES);
	}

	public static RGB getPotentialMatchForegroundColor() {
		IPreferenceStore store= SearchPlugin.getDefault().getPreferenceStore();
		return PreferenceConverter.getColor(store, POTENTIAL_MATCH_FG_COLOR);
	}

	public static int getHistoryLimit() {
		IPreferenceStore store= SearchPlugin.getDefault().getPreferenceStore();
		int limit= store.getInt(LIMIT_HISTORY);
		if (limit < 1) {
			limit= 1;
		} else if (limit >= 100) {
			limit= 99;
		}
		return limit;
	}

}
