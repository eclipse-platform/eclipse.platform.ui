package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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
	
	private static final String PROP_DLG_LAST_SELECTION = "property_dialog_last_selection";//$NON-NLS-1$
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
	return PROP_DLG_LAST_SELECTION;
}

}
