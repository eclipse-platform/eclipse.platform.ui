package org.eclipse.ui.tests.dialogs;

import org.eclipse.jface.preference.*;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Shell;

public class PreferenceDialogWrapper extends PreferenceDialog {

	public PreferenceDialogWrapper(
		Shell parentShell,
		PreferenceManager manager) {
		super(parentShell, manager);
	}
	protected boolean showPage(IPreferenceNode node) {
		return super.showPage(node);
	}

	public IPreferencePage getPage(IPreferenceNode node) {
		if (node == null)
			return null;

		// Create the page if nessessary
		if (node.getPage() == null)
			node.createPage();

		if (node.getPage() == null)
			return null;

		return node.getPage();
	}
}
