/*
 * Created on Jan 14, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.debug.internal.ui.stringsubstitution;

import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.jface.text.ITextSelection;

/**
 * @author kbarnes
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
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
		if (selection != null && !selection.getText().equals("")) {
			selectedText = selection.getText();
		}
		return selectedText;
	}
}
