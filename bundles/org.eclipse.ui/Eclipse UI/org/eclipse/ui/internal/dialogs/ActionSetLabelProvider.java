package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.*;
import org.eclipse.jface.viewers.*;

public class ActionSetLabelProvider extends LabelProvider {

	private String UNKNOWN = WorkbenchMessages.getString("ActionSetLabelProvider.Unknown"); //$NON-NLS-1$
	
public ActionSetLabelProvider() {
	super();
}
public String getText(Object element) {
	String label = UNKNOWN;
	if (element instanceof ActionSetCategory)
		label = ((ActionSetCategory)element).getLabel();
	else if (element instanceof IActionSetDescriptor)
		label = ((IActionSetDescriptor)element).getLabel();
	int aruga = label.indexOf('&');
	if (aruga >= 0)
		label = label.substring(aruga + 1);
	return label;
}
}
