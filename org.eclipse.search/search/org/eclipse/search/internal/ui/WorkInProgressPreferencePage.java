/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.search.internal.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

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

	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(SEARCH_IN_BACKGROUND, true);
	}
	
}
