/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.swt.modeling;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class RunAway {
	static class TestFilter implements Listener {
		public void handleEvent(Event event) {
			if (!(event.widget instanceof Menu)) {
				return;
			}
			System.err.println(getType(event.type) + ": " + event.widget);
		}
	}

	static String getType(int type) {
		switch (type) {
		case SWT.Show:
			return "SWT.Show";
		case SWT.Hide:
			return "SWT.Hide";
		case SWT.Dispose:
			return "SWT.Dispose";
		}
		return "UNKNOWN";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();

		TestFilter listener = new TestFilter();
		display.addFilter(SWT.Show, listener);
		display.addFilter(SWT.Hide, listener);
		display.addFilter(SWT.Dispose, listener);

		Shell shell = new Shell(display);
		Menu bar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(bar);

		MenuItem fileItem = new MenuItem(bar, SWT.CASCADE);
		fileItem.setText("&File");
		Menu submenu = new Menu(shell, SWT.DROP_DOWN);
		fileItem.setMenu(submenu);
		MenuItem item = new MenuItem(submenu, SWT.PUSH);
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				System.out.println("Select All");
			}
		});
		item.setText("Select &All\tCtrl+A");
		item.setAccelerator(SWT.MOD1 + 'A');

		new MenuItem(submenu, SWT.PUSH).setText("five");
		new MenuItem(submenu, SWT.PUSH).setText("six");
		new MenuItem(submenu, SWT.PUSH).setText("seven");

		MenuItem windowItem = new MenuItem(bar, SWT.CASCADE);
		windowItem.setText("&Window");
		Menu wMenu = new Menu(windowItem);
		windowItem.setMenu(wMenu);

		new MenuItem(wMenu, SWT.PUSH).setText("one");
		new MenuItem(wMenu, SWT.PUSH).setText("two");
		new MenuItem(wMenu, SWT.PUSH).setText("three");

		windowItem = new MenuItem(wMenu, SWT.CASCADE);
		windowItem.setText("&Window Sub");
		wMenu = new Menu(windowItem);
		windowItem.setMenu(wMenu);

		new MenuItem(wMenu, SWT.PUSH).setText("one Sub");
		new MenuItem(wMenu, SWT.PUSH).setText("two Sub");
		new MenuItem(wMenu, SWT.PUSH).setText("three Sub");

		shell.setSize(200, 200);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();

	}

}
