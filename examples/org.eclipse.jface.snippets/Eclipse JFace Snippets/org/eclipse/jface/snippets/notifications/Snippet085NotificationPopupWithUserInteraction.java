package org.eclipse.jface.snippets.notifications;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.notifications.NotificationPopup;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Snippet085NotificationPopupWithUserInteraction {

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(600, 400);
		shell.open();

		NotificationPopup.forShell(shell)
				.content(parent -> {
					WidgetFactory.label(SWT.WRAP).text("It is recommended that you\nupdate your configuration")
							.create(parent);
					WidgetFactory.button(SWT.PUSH).text("Confirm")
							.onSelect(e -> MessageDialog.openConfirm(shell, "Confirmation", "Button was pressed!"))
							.create(parent);
					return parent;
				})
				.title("System update", true)
				.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}
}
