/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Dialog for setting default column size in Memory View
 */
public class DefaultColumnSizeDialog extends Dialog {

	private static final String PREFIX = "DefaultColumnSizeDialog."; //$NON-NLS-1$
	private static final String DEFAULT_COLUMN_SIZE = PREFIX + "DefaultColumnSize"; //$NON-NLS-1$
	private static final String COLUMN_SIZE = PREFIX + "ColumnSize"; //$NON-NLS-1$
	
	IPreferenceStore fPrefStore;
	Combo fColumnSize;
	
	private int[] fColumnSizes = new int[] {1, 2, 4, 8, 16};
	
	/**
	 * @param parentShell
	 */
	protected DefaultColumnSizeDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fPrefStore = DebugUIPlugin.getDefault().getPreferenceStore();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		WorkbenchHelp.setHelp(parent, IDebugUIConstants.PLUGIN_ID + ".DefaultColumnSizeDialog_context"); //$NON-NLS-1$
		
		getShell().setText(DebugUIMessages.getString(DEFAULT_COLUMN_SIZE));
		
		Composite content = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		content.setLayout(layout);
		
		Label textLabel = new Label(content, SWT.NONE);
		textLabel.setText(DebugUIMessages.getString(COLUMN_SIZE));
		
		GridData textLayout = new GridData();
		textLabel.setLayoutData(textLayout);
		
		fColumnSize = new Combo(content, SWT.BORDER|SWT.READ_ONLY);

		GridData columnLayout= new GridData();	
		fColumnSize.setLayoutData(columnLayout);
		
		for (int i=0; i<fColumnSizes.length; i++)
		{
			fColumnSize.add(String.valueOf(fColumnSizes[i]));
		}
		
		int colSize = fPrefStore.getInt(IDebugPreferenceConstants.PREF_COLUMN_SIZE);
		int idx = 0;
		
		for (int i=0; i<fColumnSizes.length; i++)
		{
			if (fColumnSizes[i] == colSize)
			{
				idx = i;
				break;
			}
		}
		
		fColumnSize.select(idx);				
		
		return content;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		int idx = fColumnSize.getSelectionIndex();
		int colSize = fColumnSizes[idx];
		
		fPrefStore.setValue(IDebugPreferenceConstants.PREF_COLUMN_SIZE, colSize);	
		
		super.okPressed();
	}
}
