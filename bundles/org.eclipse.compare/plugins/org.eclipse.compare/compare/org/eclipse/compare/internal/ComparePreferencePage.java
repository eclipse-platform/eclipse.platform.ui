/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.preference.*;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.WorkbenchChainedTextFontFieldEditor;


public class ComparePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
		
	private static final String PREFIX= CompareUIPlugin.PLUGIN_ID + "."; //$NON-NLS-1$
	public static final String SYNCHRONIZE_SCROLLING= PREFIX + "SynchronizeScrolling"; //$NON-NLS-1$
	public static final String SHOW_PSEUDO_CONFLICTS= PREFIX + "ShowPseudoConflicts"; //$NON-NLS-1$
	public static final String INITIALLY_SHOW_ANCESTOR_PANE= PREFIX + "InitiallyShowAncestorPane"; //$NON-NLS-1$
	public static final String PREF_SAVE_ALL_EDITORS= PREFIX + "SaveAllEditors"; //$NON-NLS-1$
	public static final String TEXT_FONT= PREFIX + "TextFont"; //$NON-NLS-1$

	public ComparePreferencePage() {
		super(GRID);
	}
	
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(SYNCHRONIZE_SCROLLING, true);
		store.setDefault(SHOW_PSEUDO_CONFLICTS, false);
		store.setDefault(INITIALLY_SHOW_ANCESTOR_PANE, false);
		
		WorkbenchChainedTextFontFieldEditor.startPropagate(store, TEXT_FONT);
	}

	static public boolean getSaveAllEditors() {
		IPreferenceStore store= CompareUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(PREF_SAVE_ALL_EDITORS);
	}
	
	static public void setSaveAllEditors(boolean value) {
		IPreferenceStore store= CompareUIPlugin.getDefault().getPreferenceStore();
		store.setValue(PREF_SAVE_ALL_EDITORS, value);
	}	

	public void init(IWorkbench workbench) {
	}	

	protected IPreferenceStore doGetPreferenceStore() {
		return CompareUIPlugin.getDefault().getPreferenceStore();
	}

	public void createFieldEditors() {
				
		Composite parent= getFieldEditorParent();
			
		{
			BooleanFieldEditor editor= new BooleanFieldEditor(SYNCHRONIZE_SCROLLING,
				Utilities.getString("ComparePreferences.synchronizeScrolling.label"), BooleanFieldEditor.DEFAULT, parent); //$NON-NLS-1$
			addField(editor);	
		}
		
		// three way merging
		{
			BooleanFieldEditor editor= new BooleanFieldEditor(SHOW_PSEUDO_CONFLICTS,
				Utilities.getString("ComparePreferences.showPseudoConflicts.label"), BooleanFieldEditor.DEFAULT, parent); //$NON-NLS-1$
			addField(editor);	
		}
		
		{
			BooleanFieldEditor editor= new BooleanFieldEditor(INITIALLY_SHOW_ANCESTOR_PANE,
				Utilities.getString("ComparePreferences.initiallyShowAncestorPane.label"), BooleanFieldEditor.DEFAULT, parent); //$NON-NLS-1$
			addField(editor);	
		}
		
		{
			WorkbenchChainedTextFontFieldEditor editor= new WorkbenchChainedTextFontFieldEditor(TEXT_FONT,
				Utilities.getString("ComparePreferences.textFont.label"), parent); //$NON-NLS-1$
			addField(editor);
		}
	}
}
