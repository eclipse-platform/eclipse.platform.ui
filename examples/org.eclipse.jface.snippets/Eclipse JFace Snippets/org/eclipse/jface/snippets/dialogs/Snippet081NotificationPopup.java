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
 *     SAP SE - initial contribution
 *     SAP SE - update to use WidgetFactories and NotificationPopup builder api
 *******************************************************************************/
package org.eclipse.jface.snippets.dialogs;

import org.eclipse.jface.notifications.NotificationPopup;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Snippet081NotificationPopup {

	private Display display = new Display();
	private Shell shell = WidgetFactory.shell(SWT.NONE).layout(new FillLayout(SWT.VERTICAL)).create(display);

	public static void main(String[] args) {
		new Snippet081NotificationPopup().start();
	}

	private void start() {
		ButtonFactory buttonFactory = WidgetFactory.button(SWT.PUSH).onSelect(this::showNotification);
		buttonFactory.text("Hello World").create(shell);
		buttonFactory.text("I am a notification popup").create(shell);
		buttonFactory.text("Press me!").create(shell);

		shell.setSize(500, 500);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private void showNotification(SelectionEvent event) {
		String text = ((Button) event.widget).getText();
		NotificationPopup.forShell(shell).delay(10000).fadeIn(true).text(text).build().open();
	}
}