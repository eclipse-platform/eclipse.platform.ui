/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;

public class MaxDetailsLengthDialog extends InputDialog {

	/**
	 * Cosntructs a new dialog on the given shell.
	 * 
	 * @param parent shell
	 */
	public MaxDetailsLengthDialog(Shell parent) {
		super(parent, VariablesViewMessages.MaxDetailsLengthDialog_0, VariablesViewMessages.MaxDetailsLengthDialog_1,
				Integer.toString(DebugUIPlugin.getDefault().getPreferenceStore().getInt(IDebugUIConstants.PREF_MAX_DETAIL_LENGTH)),
				new IInputValidator() {
					public String isValid(String newText) {
						try {
							int num = Integer.parseInt(newText);
							if (num < 0) {
								return VariablesViewMessages.MaxDetailsLengthDialog_2;
							}
						} catch (NumberFormatException e) {
							return VariablesViewMessages.MaxDetailsLengthDialog_3;
						}
						return null;
					}
				
				});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		String text = getValue();
		try {
			int max = Integer.parseInt(text);
			DebugUIPlugin.getDefault().getPreferenceStore().setValue(IDebugUIConstants.PREF_MAX_DETAIL_LENGTH, max);
		} catch (NumberFormatException e) {
		}
		super.okPressed();
	}
	
	

}
