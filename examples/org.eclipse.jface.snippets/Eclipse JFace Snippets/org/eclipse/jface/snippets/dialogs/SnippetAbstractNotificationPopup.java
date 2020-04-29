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

@SuppressWarnings("restriction")
public class SnippetAbstractNotificationPopup {

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.open();
		display.readAndDispatch(); // so that activeShell is set

		NotificationPopUp notificationPopUp = new NotificationPopUp(display);
		notificationPopUp.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

	private static class NotificationPopUp extends AbstractNotificationPopup {

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
	}

}
