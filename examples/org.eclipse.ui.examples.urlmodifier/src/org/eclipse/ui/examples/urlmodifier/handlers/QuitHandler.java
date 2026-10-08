package org.eclipse.ui.examples.urlmodifier.handlers;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.dialogs.PlainMessageDialog;
import org.eclipse.swt.widgets.Shell;

public class QuitHandler {
	@Execute
	public void execute(IWorkbench workbench, Shell shell) {

		PlainMessageDialog dialog = PlainMessageDialog.getBuilder(shell, "Confirmation")
				.message("Do you want to exit?").buttonLabels(List.of("Exit", "Cancel")).build();

		if (dialog.open() == 0) {
			workbench.close();
		}
	}
}
