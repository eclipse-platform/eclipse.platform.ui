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
package org.eclipse.ui.ide.dialogs;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.ide.IDEEncoding;

/**
 * The EncodingFieldEditor is a field editor that allows the 
 * user to set an encoding on a preference in a preference
 * store.
 */
public class EncodingFieldEditor extends AbstractEncodingFieldEditor {

	/**
	 * Create a new instance of the receiver on the preference called name
	 * with a label of labelText.
	 * @param name
	 * @param labelText
	 * @param parent
	 */
	public EncodingFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.ide.dialogs.AbstractEncodingFieldEditor#getStoredValue()
	 */
	protected String getStoredValue() {
		return getPreferenceStore().getString(getPreferenceName());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	protected void doStore() {
		String encoding = getSelectedEncoding();
		
		if(hasSameEncoding(encoding))
			return;
		
		IDEEncoding.addIDEEncoding(encoding);	
		
		if (encoding.equals(getDefaultEnc())) {
			getPreferenceStore().setToDefault(getPreferenceName());
		} else {
			getPreferenceStore().setValue(getPreferenceName(), encoding);
		}
	}

}
