/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.templateeditor.preferences;

import org.eclipse.ui.examples.templateeditor.editors.TemplateEditorUI;

import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;


/**
 * @see org.eclipse.jface.preference.PreferencePage
 */
public class TemplatesPreferencePage extends TemplatePreferencePage {

	public TemplatesPreferencePage() {
		setPreferenceStore(TemplateEditorUI.getDefault().getPreferenceStore());
		setTemplateStore(TemplateEditorUI.getDefault().getTemplateStore());
		setContextTypeRegistry(TemplateEditorUI.getDefault().getContextTypeRegistry());
	}

	@Override
	protected boolean isShowFormatterSetting() {
		return false;
	}


	@Override
	public boolean performOk() {
		boolean ok= super.performOk();

		TemplateEditorUI.getDefault().savePluginPreferences();

		return ok;
	}
}
