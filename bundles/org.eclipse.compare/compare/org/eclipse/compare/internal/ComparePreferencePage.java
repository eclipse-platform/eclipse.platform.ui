/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.util.ResourceBundle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.resource.JFaceResources;import org.eclipse.ui.IWorkbench;import org.eclipse.ui.IWorkbenchPreferencePage;

public class ComparePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
		
	private static final String PREFIX= CompareUIPlugin.PLUGIN_ID + ".";
	public static final String SYNCHRONIZE_SCROLLING= PREFIX + "SynchronizeScrolling";
	public static final String SHOW_PSEUDO_CONFLICTS= PREFIX + "ShowPseudoConflicts";
	public static final String INITIALLY_SHOW_ANCESTOR_PANE= PREFIX + "InitiallyShowAncestorPane";
	public static final String TEXT_FONT= PREFIX + "TextFont";

	private ResourceBundle fResourceBundle;


	public ComparePreferencePage() {
		super(GRID);
		fResourceBundle= CompareUIPlugin.getResourceBundle();
	}
	
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(SYNCHRONIZE_SCROLLING, true);
		store.setDefault(SHOW_PSEUDO_CONFLICTS, false);
		store.setDefault(INITIALLY_SHOW_ANCESTOR_PANE, false);
		
		Font font= JFaceResources.getTextFont();
		if (font != null) {
			FontData[] data= font.getFontData();
			if (data != null && data.length > 0)
				PreferenceConverter.setDefault(store, TEXT_FONT, data[0]);
		}
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
				getResourceString(SYNCHRONIZE_SCROLLING), BooleanFieldEditor.DEFAULT, parent);
			addField(editor);	
		}
		
		// three way merging
		{
			BooleanFieldEditor editor= new BooleanFieldEditor(SHOW_PSEUDO_CONFLICTS,
				getResourceString(SHOW_PSEUDO_CONFLICTS), BooleanFieldEditor.DEFAULT, parent);
			addField(editor);	
		}
		
		{
			BooleanFieldEditor editor= new BooleanFieldEditor(INITIALLY_SHOW_ANCESTOR_PANE,
				getResourceString(INITIALLY_SHOW_ANCESTOR_PANE), BooleanFieldEditor.DEFAULT, parent);
			addField(editor);	
		}
		
		{
			FontFieldEditor editor= new FontFieldEditor(TEXT_FONT, getResourceString(TEXT_FONT), parent);
			addField(editor);
		}
	}
	
	private String getResourceString(String key) {
		int pos= key.lastIndexOf(".");
		return Utilities.getString(fResourceBundle, key.substring(pos+1) + ".label", key);
	}
}
