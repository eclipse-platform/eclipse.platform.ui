/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

public class SetPaddedStringPreferencePage extends FieldEditorPreferencePage {
	
	private StringFieldEditor fPaddedString;

	public SetPaddedStringPreferencePage()
	{
		super(FieldEditorPreferencePage.GRID);
		setPreferenceStore(DebugUITools.getPreferenceStore());
		setTitle(DebugUIMessages.SetPaddedStringDialog_0);
		
	}

	protected void createFieldEditors() {
		fPaddedString = new StringFieldEditor(IDebugUIConstants.PREF_PADDED_STR, DebugUIMessages.SetPaddedStringPreferencePage_0, getFieldEditorParent());
		fPaddedString.setEmptyStringAllowed(false);
		fPaddedString.setTextLimit(5);
		addField(fPaddedString);
	}

	protected Label createDescriptionLabel(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(DebugUIMessages.SetPaddedStringDialog_1);
		return label;
	}

	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IDebugUIConstants.PLUGIN_ID + ".SetPaddedStrDialog_context"); //$NON-NLS-1$
		return super.createContents(parent);
	}

}
