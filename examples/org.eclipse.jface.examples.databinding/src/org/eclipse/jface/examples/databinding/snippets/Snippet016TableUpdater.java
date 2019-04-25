/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.internal.databinding.provisional.swt.TableUpdater;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @since 3.2
 *
 */
public class Snippet016TableUpdater {
	public static void main(String[] args) {
		final Display display = new Display();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			final Shell shell = createShell(display);
			GridLayoutFactory.fillDefaults().generateLayout(shell);
			shell.open();
			// The SWT event loop
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		});
	}

	static class Stuff {
		private WritableValue<Integer> counter = new WritableValue<>(1, Integer.class);

		public Stuff(final Display display) {
			display.timerExec(1000, new Runnable() {
				@Override
				public void run() {
					counter.setValue(1 + counter.getValue());
					display.timerExec(1000, this);
				}
			});
		}

		@Override
		public String toString() {
			return counter.getValue().toString();
		}
	}

	protected static Shell createShell(final Display display) {
		Shell shell = new Shell();
		Table t = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
		t.setHeaderVisible(true);
		createColumn(t, "Values");
		t.setLinesVisible(true);
		final WritableList<Stuff> list = new WritableList<>();
		new TableUpdater<Stuff>(t, list) {
			@Override
			protected void updateItem(int index, TableItem item, Stuff element) {
				item.setText(element.toString());
			}
		};
		display.timerExec(2000, new Runnable() {
			@Override
			public void run() {
				list.add(new Stuff(display));
				display.timerExec(2000, this);
			}
		});
		return shell;
	}

	private static void createColumn(Table t, String string) {
		final TableColumn column = new TableColumn(t, SWT.NONE);
		column.setWidth(100);
		column.setText(string);
	}

}
