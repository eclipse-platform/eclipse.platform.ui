/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.snippets;

import java.util.Date;
import java.util.Iterator;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.internal.databinding.provisional.swt.MenuUpdater;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 */
public class Snippet005MenuUpdater {
	public static void main(String[] args) {
		final Display display = new Display();
		Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {
			@Override
			public void run() {
				Shell shell = new Shell(display);

				final WritableList menuItemStrings = new WritableList();
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						System.out.println("adding item");
						menuItemStrings.add(new Date().toString());
						display.timerExec(5000, this);
					}
				});

				Menu bar = new Menu(shell, SWT.BAR);
				shell.setMenuBar(bar);
				MenuItem fileItem = new MenuItem(bar, SWT.CASCADE);
				fileItem.setText("&Test Menu");
				final Menu submenu = new Menu(shell, SWT.DROP_DOWN);
				fileItem.setMenu(submenu);
				new MenuUpdater(submenu) {
					@Override
					protected void updateMenu() {
						System.out.println("updating menu");
						MenuItem[] items = submenu.getItems();
						int itemIndex = 0;
						for (Iterator it = menuItemStrings.iterator(); it
								.hasNext();) {
							MenuItem item;
							if (itemIndex < items.length) {
								item = items[itemIndex++];
							} else {
								item = new MenuItem(submenu, SWT.NONE);
							}
							String string = (String) it.next();
							item.setText(string);
						}
						while (itemIndex < items.length) {
							items[itemIndex++].dispose();
						}
					}
				};

				shell.open();

				// The SWT event loop
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}
		});
		display.dispose();
	}
}
