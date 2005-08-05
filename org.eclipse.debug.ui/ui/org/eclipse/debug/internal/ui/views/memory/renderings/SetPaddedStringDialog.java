/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory.renderings;

import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewUtil;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Dialog for setting the padded string in renderings.
 * @since 3.1
 *
 */
public class SetPaddedStringDialog extends Dialog {
	
	private StringFieldEditor fPaddedString;

	
	protected SetPaddedStringDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	protected Control createDialogArea(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IDebugUIConstants.PLUGIN_ID + ".SetPaddedStrDialog_context"); //$NON-NLS-1$
		
		getShell().setText(DebugUIMessages.SetPaddedStringDialog_0); 
		
		Composite content = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		content.setLayout(layout);
		
		GridData contentData = new GridData(SWT.FILL);
		content.setLayoutData(contentData);
		
		Label textLabel = new Label(content, SWT.NONE);
		textLabel.setText(DebugUIMessages.SetPaddedStringDialog_1); 
		
		GridData textLayout = new GridData();
		textLabel.setLayoutData(textLayout);
		
		fPaddedString = new StringFieldEditor(IDebugUIConstants.PREF_PADDED_STR, "",content ); //$NON-NLS-1$
		fPaddedString.fillIntoGrid(content, 2);
		fPaddedString.setPreferenceStore(DebugUIPlugin.getDefault().getPreferenceStore());
		fPaddedString.load();
				
		return content;		
	}

	protected void okPressed() {
		String str = fPaddedString.getStringValue();
		
		if (str == null || str.length() == 0)
		{
			MemoryViewUtil.openError(DebugUIMessages.SetPaddedStringDialog_3, DebugUIMessages.SetPaddedStringDialog_4, null); // 
			return;
		}
			
		fPaddedString.store();
		
		super.okPressed();
		
	}

}
