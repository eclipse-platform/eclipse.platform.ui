/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

public class ResetMemoryBlockPreferencePage extends FieldEditorPreferencePage {

	public ResetMemoryBlockPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		setPreferenceStore(DebugUITools.getPreferenceStore());
		setTitle(DebugUIMessages.ResetMemoryBlockPreferencePage_0);
	}

	@Override
	protected void createFieldEditors() {
		RadioGroupFieldEditor editor = new RadioGroupFieldEditor(IDebugPreferenceConstants.PREF_RESET_MEMORY_BLOCK, DebugUIMessages.ResetMemoryBlockPreferencePage_1, 1, new String[][] {
				{
						DebugUIMessages.ResetMemoryBlockPreferencePage_2,
						IDebugPreferenceConstants.RESET_VISIBLE },
				{
						DebugUIMessages.ResetMemoryBlockPreferencePage_3,
						IDebugPreferenceConstants.RESET_ALL } }, getFieldEditorParent());
		addField(editor);
	}

	@Override
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IDebugUIConstants.PLUGIN_ID + ".resetMemoryMonitor_preference_page_context"); //$NON-NLS-1$
		return super.createContents(parent);
	}

}
