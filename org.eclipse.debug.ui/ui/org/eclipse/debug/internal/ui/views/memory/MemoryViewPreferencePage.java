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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * @since 3.0
 */
public class MemoryViewPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{	
	private static final String PREFIX = "MemoryViewPreferencePage."; //$NON-NLS-1$
	private static final String DEFAULT_COLUMN_SIZE = PREFIX + "Default_column_size"; //$NON-NLS-1$
	
	IPreferenceStore fPrefStore;
	Combo fColumnSize;
	
	private int[] fColumnSizes = new int[] {1, 2, 4, 8, 16};
	
	public MemoryViewPreferencePage()
	{
		super();
		fPrefStore = DebugUIPlugin.getDefault().getPreferenceStore();
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent)
	{	
		WorkbenchHelp.setHelp(parent, IDebugUIConstants.PLUGIN_ID + ".MemoryViewPreferencePage_context"); //$NON-NLS-1$
		
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		content.setLayout(layout);
		
		Label textLabel = new Label(content, SWT.NONE);
		textLabel.setText(DebugUIMessages.getString(DEFAULT_COLUMN_SIZE));
		
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
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench)
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk()
	{
		int idx = fColumnSize.getSelectionIndex();
		int colSize = fColumnSizes[idx];
		
		fPrefStore.setValue(IDebugPreferenceConstants.PREF_COLUMN_SIZE, colSize);			
		
		return super.performOk();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults()
	{
		fPrefStore.setValue(IDebugPreferenceConstants.PREF_COLUMN_SIZE, IDebugPreferenceConstants.PREF_COLUMN_SIZE_DEFAULT);	
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
		super.performDefaults();
	}

}
