/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.internal.databinding.provisional.swt.TableUpdater;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * @since 3.2
 * 
 */
public class Snippet016TableUpdater {
	public static void main(String[] args) {
		final Display display = new Display();

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				final Shell shell = createShell(display);
				GridLayoutFactory.fillDefaults().generateLayout(shell);
				shell.open();
				// The SWT event loop
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}
		});
	}

	static class Stuff {
		private WritableValue counter = new WritableValue(new Integer(1), Integer.class);

		public Stuff(final Display display) {
			display.timerExec(1000, new Runnable() {
				public void run() {
					counter.setValue(new Integer(1 + ((Integer) counter
							.getValue()).intValue()));
					display.timerExec(1000, this);
				}
			});
		}
		
		public String toString() {
			return counter.getValue().toString();
		}
	}

	protected static Shell createShell(final Display display) {
		Shell shell = new Shell();
		Table t = new Table(shell, SWT.VIRTUAL);
		final WritableList list = new WritableList();
		new TableUpdater(t, list) {

			protected void updateItem(int index, TableItem item, Object element) {
				item.setText(element.toString());
			}
		};
		display.timerExec(2000, new Runnable() {
			public void run() {
				list.add(new Stuff(display));
				display.timerExec(2000, this);
			}
		});
		return shell;
	}

}
