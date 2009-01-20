/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.ide.dialogs;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.eclipse.ui.ide.IDEEncoding;

/**
 * The EncodingFieldEditor is a field editor that allows the
 * user to set an encoding on a preference in a preference
 * store.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 3.1
 */
public final class EncodingFieldEditor extends AbstractEncodingFieldEditor {

	
	/**
	 * Creates a new encoding field editor with the given preference name, label
	 * and parent.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param groupTitle
	 *            the title for the field editor's control. If groupTitle is
	 *            <code>null</code> the control will be unlabelled
	 *            (by default a {@link Composite} instead of a {@link Group}.
	 * @param parent
	 *            the parent of the field editor's control
	 * @see AbstractEncodingFieldEditor#setGroupTitle(String)
	 * @since 3.3
	 */
	public EncodingFieldEditor(String name, String labelText,
			String groupTitle, Composite parent) {
		super();
		init(name, labelText);
		setGroupTitle(groupTitle);
		createControl(parent);
	}
	/**
	 * Create a new instance of the receiver on the preference called name
	 * with a label of labelText.
	 * @param name
	 * @param labelText
	 * @param parent
	 */
	public EncodingFieldEditor(String name, String labelText, Composite parent) {
		super();
		init(name, labelText);
		createControl(parent);
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
		
		if(hasSameEncoding(encoding)) {
			return;
		}
		
		IDEEncoding.addIDEEncoding(encoding);
		
		if (encoding.equals(getDefaultEnc())) {
			getPreferenceStore().setToDefault(getPreferenceName());
		} else {
			getPreferenceStore().setValue(getPreferenceName(), encoding);
		}
	}

}
