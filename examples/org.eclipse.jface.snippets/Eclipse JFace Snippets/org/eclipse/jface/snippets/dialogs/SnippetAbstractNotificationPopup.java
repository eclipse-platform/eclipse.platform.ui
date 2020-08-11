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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SnippetAbstractNotificationPopup {

	private Display display = new Display();
	private final Shell shell = new Shell(display);

	public static void main(String[] args) {
		new SnippetAbstractNotificationPopup().start();
	}

	private void start() {
		shell.setLayout(new FillLayout());

		NotificationPopUp notificationPopUp = this.new NotificationPopUp(display);
		notificationPopUp.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}

	private class NotificationPopUp extends AbstractNotificationPopup {

		public NotificationPopUp(Display display) {
			super(display);
		}

		@Override
		protected String getPopupShellTitle() {
			return "Notification";
		}

		@Override
		protected void createContentArea(Composite parent) {
			Label label = new Label(parent, SWT.WRAP);
			label.setText("Hello World");
		}

		@Override
		public boolean close() {
			boolean closed = super.close();
			shell.close();
			return closed;
		}
	}

}
