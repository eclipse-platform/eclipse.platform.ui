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

package org.eclipse.debug.internal.ui.stringsubstitution;

import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.jface.text.ITextSelection;

public class SelectedTextResolver implements IDynamicVariableResolver {
	SelectedResourceManager selectedResourceManager;
	
	public SelectedTextResolver() {
		selectedResourceManager = SelectedResourceManager.getDefault();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.variables.IDynamicVariableResolver#resolveValue(org.eclipse.core.variables.IDynamicVariable, java.lang.String)
	 */
	public String resolveValue(IDynamicVariable variable, String argument) {
		ITextSelection selection = selectedResourceManager.getSelectedText();
		String selectedText = argument;
		if (selection != null && !selection.getText().equals("")) { //$NON-NLS-1$
			selectedText = selection.getText();
		}
		return selectedText;
	}
}
