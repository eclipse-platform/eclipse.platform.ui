package org.eclipse.jface.snippets.notifications;

import org.eclipse.jface.notifications.NotificationPopup;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Snippet001NotificationPopupWithStrings {

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(400, 200);
		shell.open();

		NotificationPopup.forShell(shell).text("Just a notification").title("Test", true).open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}
}