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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.preference.*;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.*;

/**
 * This dialog is created and shown when 'Properties'
 * action is performed while an object is selected.
 * It shows one or more pages registered for object's type.
 */
public class PropertyDialog extends PreferenceDialog {
	private ISelection selection;	
	
	//The id of the last page that was selected
	private static String lastPropertyId = null;
	
/**
 * The constructor.
 */

public PropertyDialog(Shell parentShell, PreferenceManager mng, ISelection selection) {
	super(parentShell, mng);
	setSelection(selection);
}
/**
 * Returns selection in the "Properties" action context.
 */

public ISelection getSelection() {
	return selection;
}
/**
 * Sets the selection that will be used to
 * determine target object.
 */

public void setSelection(ISelection newSelection) {
	selection = newSelection;
}

/**
 * Get the name of the selected item preference
 */
protected String getSelectedNodePreference(){
	return lastPropertyId;
}

/**
 * Get the name of the selected item preference
 */
protected void setSelectedNodePreference(String pageId){
	lastPropertyId = pageId;
}

}
