/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.ui;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.text.quicksearch.internal.core.preferences.QuickSearchPreferences;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class QuickSearchPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public QuickSearchPreferencesPage() {
		super(FieldEditorPreferencePage.GRID);
		setPreferenceStore(QuickSearchActivator.getDefault().getPreferenceStore());
		QuickSearchPreferences.initializeDefaults();
	}

	@Override
	public void init(IWorkbench arg0) {
	}

	private static final String[] prefsKeys = {
			QuickSearchPreferences.IGNORED_EXTENSIONS,
			QuickSearchPreferences.IGNORED_PREFIXES,
			QuickSearchPreferences.IGNORED_NAMES
	};

	private static final String[] fieldNames = {
			Messages.QuickSearchPreferencesPage_0, Messages.QuickSearchPreferencesPage_1, Messages.QuickSearchPreferencesPage_2
	};

	private static final String[] toolTips = {
			Messages.QuickSearchPreferencesPage_3
		,
			Messages.QuickSearchPreferencesPage_4
		,
			Messages.QuickSearchPreferencesPage_5
	};

	@Override
	protected void createFieldEditors() {
		IntegerFieldEditor field_maxLineLen = new IntegerFieldEditor(QuickSearchPreferences.MAX_LINE_LEN, Messages.QuickSearchPreferencesPage_6, getFieldEditorParent());
		field_maxLineLen.getTextControl(getFieldEditorParent()).setToolTipText(
				Messages.QuickSearchPreferencesPage_7);
		addField(field_maxLineLen);

		for (int i = 0; i < fieldNames.length; i++) {
			final String tooltip = toolTips[i];
			StringFieldEditor field = new StringFieldEditor(prefsKeys[i], Messages.QuickSearchPreferencesPage_8+" "+fieldNames[i], 45, 5, StringFieldEditor.VALIDATE_ON_FOCUS_LOST, getFieldEditorParent()) {
				@Override
				protected Text createTextWidget(Composite parent) {
					Text w = super.createTextWidget(parent);
					w.setToolTipText(tooltip);
					return w;
				}
			};
			addField(field);
		}
	}
}
