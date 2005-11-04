/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

public class TableRenderingPreferencePage extends PreferencePage implements
	IPropertyChangeListener{
	
	Combo fColumnSize;
	Combo fRowSize;
	private int[] fColumnSizes = new int[] {1, 2, 4, 8, 16};
	private int[] fRowSizes = new int[] {1, 2, 4, 8, 16};
	private BooleanFieldEditor fAutoLoadPref;
	private IntegerFieldEditor fPageSizePref;
	private Composite fBufferComposite;
	
	public TableRenderingPreferencePage(String title)
	{
		super(title);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IDebugUIConstants.PLUGIN_ID + ".table_renderings_preference_page_context"); //$NON-NLS-1$
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridData data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		composite.setLayoutData(data);
		
		Composite columnSizeComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		columnSizeComposite.setLayout(layout);
		data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		columnSizeComposite.setLayoutData(data);
		
		SelectionListener listener = new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				validateFormat();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}};
		
		Label textLabel = new Label(columnSizeComposite, SWT.NONE);
		textLabel.setText(DebugUIMessages.TableRenderingPreferencePage_0);
		
		fColumnSize = new Combo(columnSizeComposite, SWT.BORDER|SWT.READ_ONLY);
		fColumnSize.addSelectionListener(listener);

		GridData columnLayout= new GridData();	
		fColumnSize.setLayoutData(columnLayout);
		
		for (int i=0; i<fColumnSizes.length; i++)
		{
			fColumnSize.add(String.valueOf(fColumnSizes[i]));
		}
		
		int colSize = getPreferenceStore().getInt(IDebugPreferenceConstants.PREF_COLUMN_SIZE);
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
		
		createSpacer(composite);
		
		
		Label rowLabel = new Label(columnSizeComposite, SWT.NONE);
		rowLabel.setText(DebugUIMessages.TableRenderingPreferencePage_5);
		
		fRowSize = new Combo(columnSizeComposite, SWT.BORDER|SWT.READ_ONLY);
		fRowSize.addSelectionListener(listener);

		GridData rowLayout= new GridData();	
		fRowSize.setLayoutData(rowLayout);
		
		for (int i=0; i<fRowSizes.length; i++)
		{
			fRowSize.add(String.valueOf(fRowSizes[i]));
		}
		
		int rowSize = getPreferenceStore().getInt(IDebugPreferenceConstants.PREF_ROW_SIZE);
		idx = 0;
		
		for (int i=0; i<fRowSizes.length; i++)
		{
			if (fRowSizes[i] == rowSize)
			{
				idx = i;
				break;
			}
		}
		
		fRowSize.select(idx);				
		
		createSpacer(composite);
		
		fAutoLoadPref = new BooleanFieldEditor(IDebugPreferenceConstants.PREF_DYNAMIC_LOAD_MEM, DebugUIMessages.TableRenderingPreferencePage_1,  composite);
		fAutoLoadPref.setPreferenceStore(getPreferenceStore());
		fAutoLoadPref.load();
		
		fBufferComposite = new Composite(composite, SWT.NONE);
		data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		fBufferComposite.setLayoutData(data);
		
		fPageSizePref = new IntegerFieldEditor(IDebugPreferenceConstants.PREF_TABLE_RENDERING_PAGE_SIZE, DebugUIMessages.TableRenderingPreferencePage_2, fBufferComposite);
		fPageSizePref.setPreferenceStore(getPreferenceStore());
		fPageSizePref.load();
		
		boolean autoLoad = getPreferenceStore().getBoolean(IDebugPreferenceConstants.PREF_DYNAMIC_LOAD_MEM);
		if (autoLoad)
			fPageSizePref.setEnabled(false, fBufferComposite);
		else
			fPageSizePref.setEnabled(true, fBufferComposite);
		
		fAutoLoadPref.setPropertyChangeListener(this);
		fPageSizePref.setPropertyChangeListener(this);
		fPageSizePref.setValidRange(1, Integer.MAX_VALUE);
		
		return composite;
	}

	
	public boolean performOk() {
		int idx = fColumnSize.getSelectionIndex();
		int colSize = fColumnSizes[idx];
		getPreferenceStore().setValue(IDebugPreferenceConstants.PREF_COLUMN_SIZE, colSize);
		
		idx = fRowSize.getSelectionIndex();
		int rowSize = fRowSizes[idx];
		getPreferenceStore().setValue(IDebugPreferenceConstants.PREF_ROW_SIZE, rowSize);
		
		fAutoLoadPref.store();
		fPageSizePref.store();
		return super.performOk();
	}

	protected void performDefaults() {
		int colSize = IDebugPreferenceConstants.PREF_COLUMN_SIZE_DEFAULT;
		getPreferenceStore().setValue(IDebugPreferenceConstants.PREF_COLUMN_SIZE, colSize);
		int idx = -1;
		for (int i=0; i<fColumnSizes.length; i++)
		{
			if (colSize == fColumnSizes[i])
			{
				idx = i;
				break;
			}
		}
		if (idx > 0)
			fColumnSize.select(idx);

		int rowSize = IDebugPreferenceConstants.PREF_ROW_SIZE_DEFAULT;
		getPreferenceStore().setValue(IDebugPreferenceConstants.PREF_ROW_SIZE, rowSize);
		idx = -1;
		for (int i=0; i<fRowSizes.length; i++)
		{
			if (rowSize == fRowSizes[i])
			{
				idx = i;
				break;
			}
		}
		if (idx > 0)
			fRowSize.select(idx);
		
		fAutoLoadPref.loadDefault();
		fPageSizePref.loadDefault();
		super.performDefaults();
	}

	protected IPreferenceStore doGetPreferenceStore() {
		return DebugUIPlugin.getDefault().getPreferenceStore();
	}

    /**
     * Adds in a spacer.
     * 
     * @param composite the parent composite
     */
    private void createSpacer(Composite composite) {
        Label spacer = new Label(composite, SWT.NONE);
        GridData spacerData = new GridData();
        spacerData.horizontalSpan = 1;
        spacer.setLayoutData(spacerData);
    }

	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(FieldEditor.VALUE))
		{
			if (event.getSource().equals(fAutoLoadPref))
			{
				boolean autoLoad = fAutoLoadPref.getBooleanValue();
				if (autoLoad)
				{
					fPageSizePref.setEnabled(false, fBufferComposite);
				}
				else
				{
					fPageSizePref.setEnabled(true, fBufferComposite);
				}
				validatePageSize();
			}
		}
		if (event.getProperty().equals(FieldEditor.VALUE))
		{
			if (event.getSource().equals(fPageSizePref))
			{
				validatePageSize();
			}
		}
	}
	
	private void validateFormat()
	{
		int idx = fColumnSize.getSelectionIndex();
		int colSize = fColumnSizes[idx];
		
		idx = fRowSize.getSelectionIndex();
		int rowSize = fRowSizes[idx];
		
		if (colSize > rowSize)
		{
			setValid(false);
			setErrorMessage(DebugUIMessages.TableRenderingPreferencePage_6);
		}
		else
		{
			setValid(true);
			setErrorMessage(null);
		}
	}

	private void validatePageSize() {
		boolean autoLoad = fAutoLoadPref.getBooleanValue();
		try {
			int bufferSize = fPageSizePref.getIntValue();
			if (!autoLoad && bufferSize < 1)
			{
				setValid(false);
				setErrorMessage(DebugUIMessages.TableRenderingPreferencePage_3);
			}
			else
			{
				setValid(true);
				setErrorMessage(null);
				
			}
		} catch (NumberFormatException e) {
			if (!autoLoad)
			{
				setValid(false);
				setErrorMessage(DebugUIMessages.TableRenderingPreferencePage_4);
			}
		}
	}

	public void dispose() {

		fAutoLoadPref.setPropertyChangeListener(null);
		fPageSizePref.setPropertyChangeListener(null);
		super.dispose();
	}
}
