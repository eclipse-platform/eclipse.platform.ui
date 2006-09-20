package org.eclipse.ui.forms.examples.internal.dialogs;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.examples.internal.rcp.FreeFormPage;
import org.eclipse.ui.internal.provisional.forms.FormDialog;

public class SampleFormDialog extends FormDialog {

	public SampleFormDialog(Shell shell) {
		super(shell);
	}

	public SampleFormDialog(IShellProvider parentShell) {
		super(parentShell);
	}
	
	protected void createFormContent(IManagedForm mform) {
		mform.getForm().setText("An example of a simple form dialog");
		FreeFormPage.createSharedFormContent(mform);
	}
}