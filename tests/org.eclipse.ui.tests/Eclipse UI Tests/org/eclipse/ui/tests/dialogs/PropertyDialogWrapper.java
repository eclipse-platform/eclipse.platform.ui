package org.eclipse.ui.tests.dialogs;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.internal.dialogs.PropertyDialog;


public class PropertyDialogWrapper extends PropertyDialog {
	
	public PropertyDialogWrapper(Shell parentShell, PreferenceManager manager, ISelection selection) {
		super(parentShell, manager, selection);
	}
	protected boolean showPage(IPreferenceNode node) {
		return super.showPage(node);
	}
}

