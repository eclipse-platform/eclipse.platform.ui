/* * (c) Copyright IBM Corp. 2000, 2001. * All Rights Reserved. */package org.eclipse.compare.internal;
import java.util.ResourceBundle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.compare.EditionSelectionDialog;


public class CompareWithEditionDialog extends EditionSelectionDialog {
	
	CompareWithEditionDialog(Shell parent, ResourceBundle bundle) {
		super(parent, bundle);
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, Utilities.getString("CompareWithEditionDialog.closeButton.label"), false); //$NON-NLS-1$
	}		/**	 * Overidden to disable dismiss on double click (see PR 1GI3KUR).	 */	protected void okPressed() {	}
}
