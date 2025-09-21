/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.editors.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import org.eclipse.ui.texteditor.AbstractTextEditor;


/**
 * A preference page to set the font used in the default text editor.
 * <p>
 * This preference page uses the text editor's preference bundle and
 * uses the key <code>"PreferencePage.description"</code> to look up
 * the page description. In addition, it uses <code>"PreferencePage.fontEditor"</code>
 * for the editor description.
 * </p>
 * @deprecated As of 2.1, fonts are managed by the workbench, no longer supported
 */
@Deprecated
public class TextEditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Indicates whether the preferences that this page manipulates have been initialized.
	 *
	 * @since 2.0
	 */
	private static boolean fgInitialized= false;

	/**
	 * Creates and returns the text editor preference page.
	 */
	public TextEditorPreferencePage() {
		super(GRID);

		setDescription(TextEditorMessages.PreferencePage_description);
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.eclipse.ui.workbench")); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ITextEditorHelpContextIds.TEXT_EDITOR_PREFERENCE_PAGE);
	}

	@Override
	public void createFieldEditors() {
		addField(new FontFieldEditor(JFaceResources.TEXT_FONT, TextEditorMessages.PreferencePage_fontEditor, getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	/**
	 * Initializes the defaults for the given store.
	 *
	 * @param store the preference store
	 * @since 2.0
	 */
	public static void initDefaults(IPreferenceStore store) {

		if (fgInitialized) {
			return;
		}

		fgInitialized= true;

		Font font= JFaceResources.getTextFont();
		if (font != null) {
			FontData[] data= font.getFontData();
			if (data != null && data.length > 0) {
				PreferenceConverter.setDefault(store, JFaceResources.TEXT_FONT, data[0]);
			}
		}

		Display display= Display.getDefault();
		Color color= display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		PreferenceConverter.setDefault(store,  AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND, color.getRGB());
		store.setDefault(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT, true);

		color= display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		PreferenceConverter.setDefault(store,  AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, color.getRGB());
		store.setDefault(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, true);
	}
}
