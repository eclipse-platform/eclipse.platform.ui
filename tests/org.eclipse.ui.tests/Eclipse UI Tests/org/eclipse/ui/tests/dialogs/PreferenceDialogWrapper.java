package org.eclipse.ui.tests.dialogs;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Shell;

public class PreferenceDialogWrapper extends PreferenceDialog {
	
	public PreferenceDialogWrapper(Shell parentShell, PreferenceManager manager) {
		super(parentShell, manager);
	}
	protected boolean showPage(IPreferenceNode node) {
		return super.showPage(node);
	}
}

