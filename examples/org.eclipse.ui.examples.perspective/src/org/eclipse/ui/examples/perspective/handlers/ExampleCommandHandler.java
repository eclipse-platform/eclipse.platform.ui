package org.eclipse.ui.examples.perspective.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.PlainMessageDialog;
import org.eclipse.jface.dialogs.PlainMessageDialog.Builder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class ExampleCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		Shell shell = window.getShell();
		Image infoImage = shell.getDisplay().getSystemImage(SWT.ICON_INFORMATION);
		List<String> buttons = List.of(IDialogConstants.CLOSE_LABEL);

		Builder builder = PlainMessageDialog.getBuilder(shell, "Example Perspective");
		builder.message("Hello World").image(infoImage).buttonLabels(buttons);
		builder.build().open();
		return null;
	}
}
