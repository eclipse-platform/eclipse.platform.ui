package org.eclipse.jface.snippets.notifications;

import java.util.function.Function;

import org.eclipse.jface.notifications.NotificationPopup;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Snippet002NotificationPopupWithFunctions {

	public static void main(String[] args) {
		Display display = new Display();

		Function<Composite, Control> contentCreator = WidgetFactory.label(SWT.NONE)
				.text("Just a notification")::create;
		Function<Composite, Control> titleCreator = WidgetFactory.label(SWT.NONE).text("Test")::create;

		NotificationPopup.forDisplay(display).content(contentCreator).title(titleCreator, true).open();

		Shell shell = display.getShells()[0];
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}
}