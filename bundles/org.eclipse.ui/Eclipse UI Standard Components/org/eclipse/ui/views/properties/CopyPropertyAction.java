package org.eclipse.ui.views.properties;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.help.WorkbenchHelp;


/**
 * Copies a property to the clipboard.
 */
/*package*/ class CopyPropertyAction extends PropertySheetAction {
	/**
	 * System clipboard
	 */
	private Clipboard clipboard;

	/**
	 * Creates the action.
	 */
	public CopyPropertyAction(PropertySheetViewer viewer, String name) {
		super(viewer, PropertiesMessages.getString("CopyProperty.text")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IPropertiesHelpContextIds.COPY_PROPERTY_ACTION);
		clipboard = new Clipboard(Display.getCurrent());
	}
	
	/**
	 * Performs this action.
	 */
	public void run() {
		// Get the selected property
		IStructuredSelection selection = (IStructuredSelection)getPropertySheet().getSelection();
		if (selection.isEmpty()) 
			return;
		// Assume single selection
		IPropertySheetEntry entry = (IPropertySheetEntry)selection.getFirstElement();

		// Place text on the clipboard
		StringBuffer buffer = new StringBuffer();
		buffer.append(entry.getDisplayName());
		buffer.append("\t"); //$NON-NLS-1$
		buffer.append(entry.getValueAsString());
		
		Object[] data = new Object[] {buffer.toString()};				
		Transfer[] transferTypes = new Transfer[] {TextTransfer.getInstance()};
		clipboard.setContents(data, transferTypes);
	}

	/** 
	 * Updates enablement based on the current selection
	 */
	public void selectionChanged(IStructuredSelection sel) {
		setEnabled(!sel.isEmpty());
	}
}


