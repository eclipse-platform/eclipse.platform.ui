/*
 * Created on 18.11.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.search.internal.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class WorkInProgressPreferencePage extends FieldEditorPreferencePage	implements IWorkbenchPreferencePage {

	public WorkInProgressPreferencePage() {
		super(GRID);
		setPreferenceStore(SearchPlugin.getDefault().getPreferenceStore());
	}
	public static final String SEARCH_IN_BACKGROUND= "org.eclipse.search.newsearch"; //$NON-NLS-1$
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		BooleanFieldEditor boolEditor= new BooleanFieldEditor(
				SEARCH_IN_BACKGROUND,
				SearchMessages.getString("WorkInProgressPreferencePage.newsearch.label"),  //$NON-NLS-1$
				getFieldEditorParent()
				);
		addField(boolEditor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// do nothing
	}
	public static boolean useNewSearch() {
		IPreferenceStore store= SearchPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(SEARCH_IN_BACKGROUND);
	}
	
}
