
package org.eclipse.ui.tests.e4;


import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.ActionFactory;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class E4UndoRedoTestPart {

	@Inject
	private EHandlerService handlerService;

	@PostConstruct
	public void postConstruct(Composite parent) {

		new Label(parent, SWT.NONE).setText("Press Undo or Redo");

		handlerService.activateHandler(ActionFactory.UNDO.getCommandId(), new Handler("Undo"));
		handlerService.activateHandler(ActionFactory.REDO.getCommandId(), new Handler("Redo"));
	}

	private class Handler {

		private final String type;

		public Handler(String type) {
			this.type = type;
		}

		@Execute
		public void execute(Shell shell) {

			MessageBox messageBox = new MessageBox(shell, SWT.NONE);
			messageBox.setMessage(type);
			messageBox.setText("E4 Undo Redo Test");
			messageBox.open();
		}
	}
}