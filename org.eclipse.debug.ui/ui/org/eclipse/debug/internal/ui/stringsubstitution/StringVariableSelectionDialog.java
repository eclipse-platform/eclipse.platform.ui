/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.stringsubstitution;

import org.eclipse.debug.internal.core.stringsubstitution.IStringVariable;
import org.eclipse.debug.internal.core.stringsubstitution.StringVariableManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * A dialog that prompts the user to choose and configure a string
 * substitution variable.
 * 
 * @since 3,0 
 */
public class StringVariableSelectionDialog extends ElementListSelectionDialog {

	/**
	 * Constructs a new string substitution variable selection dialog.
	 *  
	 * @param parent parent shell
	 */
	public StringVariableSelectionDialog(Shell parent) {
		super(parent, new StringVariableLabelProvider());
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setTitle(StringSubstitutionMessages.getString("StringVariableSelectionDialog.2")); //$NON-NLS-1$
		setMessage(StringSubstitutionMessages.getString("StringVariableSelectionDialog.3")); //$NON-NLS-1$
		setMultipleSelection(false);
		setElements(StringVariableManager.getDefault().getVariables());
	}
	
	/**
	 * Returns the variable expression the user generated from this
	 * dialog, or <code>null</code> if none.
	 *  
	 * @return variable expression the user generated from this
	 * dialog, or <code>null</code> if none
	 */
	public String getVariableExpression() {
		// TODO: allow user to configure argument
		Object[] selected = getResult();
		if (selected != null && selected.length == 1) {
			IStringVariable variable = (IStringVariable)selected[0];
			StringBuffer buffer = new StringBuffer();
			buffer.append("${"); //$NON-NLS-1$
			buffer.append(variable.getName());
			buffer.append("}"); //$NON-NLS-1$
			return buffer.toString();
		}
		return null;
	}

}
