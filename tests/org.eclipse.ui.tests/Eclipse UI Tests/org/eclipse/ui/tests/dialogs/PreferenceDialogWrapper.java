package org.eclipse.ui.tests.dialogs;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;

public class PreferenceDialogWrapper extends PreferenceDialog {

	public PreferenceDialogWrapper(
		Shell parentShell,
		PreferenceManager manager) {
		super(parentShell, manager);
	}
	public boolean showPage(IPreferenceNode node) {
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
