/*******************************************************************************
 * Copyright (c) 2020 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initil contribution
 *******************************************************************************/
package org.eclipse.jface.snippets.dialogs;

import org.eclipse.jface.notifications.AbstractNotificationPopup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class Snippet081AbstractNotificationPopup {

	private Display display = new Display();
	private final Shell shell = new Shell(display);

	public static void main(String[] args) {
		new Snippet081AbstractNotificationPopup().start();
	}

	private void start() {

		SelectionAdapter s = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String text = ((Button) e.widget).getText();
				NotificationPopUp notificationPopUp = Snippet081AbstractNotificationPopup.this.new NotificationPopUp(
						display, text);
				notificationPopUp.open();
			}
		};

		shell.setLayout(new FillLayout(SWT.VERTICAL));
		Button a = new Button(shell, SWT.PUSH);
		Button b = new Button(shell, SWT.PUSH);
		Button c = new Button(shell, SWT.PUSH);
		a.setText("Hello World");
		b.setText("I am a notification popup");
		c.setText("Press me!");
		a.addSelectionListener(s);
		b.addSelectionListener(s);
		c.addSelectionListener(s);
		shell.setSize(500, 500);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private class NotificationPopUp extends AbstractNotificationPopup {

		private String fText;

		public NotificationPopUp(Display display, String text) {
			super(display);
			this.fText = text;
			setParentShell(shell);
			setDelayClose(1000);
		}

		@Override
		protected String getPopupShellTitle() {
			return "Notification";
		}

		@Override
		protected void createContentArea(Composite parent) {
			Label label = new Label(parent, SWT.WRAP);
			label.setText(fText);
		}
	}
}
