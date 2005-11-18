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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

public class TableRenderingPreferencePage extends PreferencePage implements
	IPropertyChangeListener{
	
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
		fAutoLoadPref.store();
		fPageSizePref.store();
		return super.performOk();
	}

	protected void performDefaults() {
		fAutoLoadPref.loadDefault();
		fPageSizePref.loadDefault();
		updatePageSizePref();
		super.performDefaults();
	}

	protected IPreferenceStore doGetPreferenceStore() {
		return DebugUIPlugin.getDefault().getPreferenceStore();
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(FieldEditor.VALUE))
		{
			if (event.getSource().equals(fAutoLoadPref))
			{
				updatePageSizePref();
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

	private void updatePageSizePref() {
		boolean autoLoad = fAutoLoadPref.getBooleanValue();
		if (autoLoad)
		{
			fPageSizePref.setEnabled(false, fBufferComposite);
		}
		else
		{
			fPageSizePref.setEnabled(true, fBufferComposite);
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
