package org.eclipse.ui.views.properties;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.WorkbenchMessages;

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
		
		setClipboard(buffer.toString());
	}

	/** 
	 * Updates enablement based on the current selection
	 */
	public void selectionChanged(IStructuredSelection sel) {
		setEnabled(!sel.isEmpty());
	}

	private void setClipboard(String text) {
		try {
			Object[] data = new Object[] {text};				
			Transfer[] transferTypes = new Transfer[] {TextTransfer.getInstance()};
			clipboard.setContents(data, transferTypes);
		} catch (SWTError e){
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
				throw e;
			if (MessageDialog.openQuestion(getPropertySheet().getControl().getShell(), WorkbenchMessages.getString("CopyToClipboardProblemDialog.title"), WorkbenchMessages.getString("CopyToClipboardProblemDialog.message"))) //$NON-NLS-1$ //$NON-NLS-2$
				setClipboard(text);
		}	
	}
}


