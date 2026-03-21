package org.eclipse.jface.snippets.notifications;

import java.util.function.Function;

import org.eclipse.jface.notifications.NotificationPopup;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Snippet083NotificationPopupWithFunctions {

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(400, 200);
		shell.open();

		Function<Composite, Control> contentCreator = WidgetFactory.label(SWT.NONE)
				.text("Just a notification")::create;
		Function<Composite, Control> titleCreator = WidgetFactory.label(SWT.NONE).text("Test")::create;

		NotificationPopup.forShell(shell).content(contentCreator).title(titleCreator, true).open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}
}