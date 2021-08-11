package org.eclipse.jface.snippets.notifications;

import org.eclipse.jface.notifications.NotificationPopup;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Snippet001NotifcationPopupWithStrings {

	public static void main(String[] args) {
		Display display = new Display();

		NotificationPopup.forDisplay(display).text("Just a notification").title("Test", true).open();
		Shell shell = display.getShells()[0];
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}
}