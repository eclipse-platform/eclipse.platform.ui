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

package org.eclipse.debug.internal.ui.stringsubstitution;

import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

public class SelectedTextResolver implements IDynamicVariableResolver {
	private SelectedResourceManager selectedResourceManager;
	
	public SelectedTextResolver() {
		selectedResourceManager = SelectedResourceManager.getDefault();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.variables.IDynamicVariableResolver#resolveValue(org.eclipse.core.variables.IDynamicVariable, java.lang.String)
	 */
	public String resolveValue(IDynamicVariable variable, String argument) {
		String selection = selectedResourceManager.getSelectedText();
		String selectedText = argument;
		if (selection != null && selection.length() > 0) {
			selectedText = selection;
		}
		return selectedText;
	}
}
