/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;

import org.eclipse.search.internal.ui.util.ComboFieldEditor;

/*
 * The page for setting the Search preferences.
 */
public class SearchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String IGNORE_POTENTIAL_MATCHES= "org.eclipse.search.potentialMatch.ignore"; //$NON-NLS-1$
	public static final String EMPHASIZE_POTENTIAL_MATCHES= "org.eclipse.search.potentialMatch.emphasize"; //$NON-NLS-1$
	public static final String POTENTIAL_MATCH_FG_COLOR= "org.eclipse.search.potentialMatch.fgColor"; //$NON-NLS-1$
	public static final String REUSE_EDITOR= "org.eclipse.search.reuseEditor"; //$NON-NLS-1$
	public static final String DEFAULT_PERSPECTIVE= "org.eclipse.search.defaultPerspective"; //$NON-NLS-1$
	private static final String NO_DEFAULT_PERSPECTIVE= "org.eclipse.search.defaultPerspective.none"; //$NON-NLS-1$

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
		store.setDefault(REUSE_EDITOR, false);
		store.setDefault(DEFAULT_PERSPECTIVE, NO_DEFAULT_PERSPECTIVE);
	}

	public static boolean isEditorReused() {
		IPreferenceStore store= SearchPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(REUSE_EDITOR);
	}

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

	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(getControl(), ISearchHelpContextIds.SEARCH_PREFERENCE_PAGE);
	}
	
	protected void createFieldEditors() {
		BooleanFieldEditor boolEditor= new BooleanFieldEditor(
			REUSE_EDITOR,
			SearchMessages.getString("SearchPreferencePage.reuseEditor"), //$NON-NLS-1$
			getFieldEditorParent()
        );
		addField(boolEditor);

		fIgnorePotentialMatchesCheckbox= new BooleanFieldEditor(
			IGNORE_POTENTIAL_MATCHES,
			SearchMessages.getString("SearchPreferencePage.ignorePotentialMatches"), //$NON-NLS-1$
			getFieldEditorParent());
		addField(fIgnorePotentialMatchesCheckbox);

		fEmphasizedCheckbox= new BooleanFieldEditor(
			EMPHASIZE_POTENTIAL_MATCHES,
			SearchMessages.getString("SearchPreferencePage.emphasizePotentialMatches"), //$NON-NLS-1$
			getFieldEditorParent());
		addField(fEmphasizedCheckbox);

		fColorEditor= new ColorFieldEditor(
			POTENTIAL_MATCH_FG_COLOR,
			SearchMessages.getString("SearchPreferencePage.potentialMatchFgColor"), //$NON-NLS-1$
			getFieldEditorParent()
        );
		addField(fColorEditor);

		fEmphasizedCheckbox.setEnabled(!arePotentialMatchesIgnored(), getFieldEditorParent());
		fColorEditor.setEnabled(!arePotentialMatchesIgnored() && arePotentialMatchesEmphasized(), getFieldEditorParent());

		handleDeletedPerspectives();
		String[][] perspectiveNamesAndIds = getPerspectiveNamesAndIds();
		ComboFieldEditor comboEditor= new ComboFieldEditor(
			DEFAULT_PERSPECTIVE,
			SearchMessages.getString("SearchPreferencePage.defaultPerspective"), //$NON-NLS-1$
			perspectiveNamesAndIds,
			getFieldEditorParent());
		addField(comboEditor);
	}

	public void setVisible(boolean state) {
		handleDeletedPerspectives();
		super.setVisible(state);
	}

	public void propertyChange(PropertyChangeEvent event) {
		boolean arePotentialMatchesIgnored= fIgnorePotentialMatchesCheckbox.getBooleanValue();
		fEmphasizedCheckbox.setEnabled(!arePotentialMatchesIgnored, getFieldEditorParent());
		fColorEditor.setEnabled(!arePotentialMatchesIgnored && fEmphasizedCheckbox.getBooleanValue(), getFieldEditorParent());
	}

	public void init(IWorkbench workbench) {
	}

	protected void performDefaults() {
		super.performDefaults();
		boolean arePotentialMatchesIgnored= fIgnorePotentialMatchesCheckbox.getBooleanValue();		
		fEmphasizedCheckbox.setEnabled(!arePotentialMatchesIgnored, getFieldEditorParent());
		fColorEditor.setEnabled(!arePotentialMatchesIgnored && fEmphasizedCheckbox.getBooleanValue(), getFieldEditorParent());
	}

	/**
	 * Return a 2-dimensional array of perspective names and ids.
	 */
	private String[][] getPerspectiveNamesAndIds() {
		
		IPerspectiveRegistry registry= PlatformUI.getWorkbench().getPerspectiveRegistry();
		IPerspectiveDescriptor[] perspectiveDescriptors= registry.getPerspectives();
		
		Arrays.sort(perspectiveDescriptors, new PerspectiveDescriptorComparator());
		
		String[][] table = new String[perspectiveDescriptors.length + 1][2];
		table[0][0] = SearchMessages.getString("SearchPreferencePage.defaultPerspective.none"); //$NON-NLS-1$;
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
}
