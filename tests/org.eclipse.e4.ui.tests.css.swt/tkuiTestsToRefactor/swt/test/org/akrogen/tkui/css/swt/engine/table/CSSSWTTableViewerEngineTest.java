/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.akrogen.tkui.css.swt.engine.table;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.swt.engine.CSSSWTEngineImpl;
import org.akrogen.tkui.css.swt.engine.table.viewers.MyCSSLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class CSSSWTTableViewerEngineTest {

	public static void main(String[] args) {
		try {
			Display display = new Display();

			CSSEngine engine = new CSSSWTEngineImpl(display);
			engine
					.parseStyleSheet(new StringReader(
							"TableItem:odd {font:Roman 12 italic normal; background-color: #2BAFFA; color:white; background-image: url(./images/icons/type/class.gif);}"
									+ "TableItem:even {background-color:#edb5f4, 100%; color:black;}"));

			Shell shell = new Shell(display, SWT.SHELL_TRIM);
			FillLayout layout = new FillLayout();
			shell.setLayout(layout);

			Composite panel1 = new Composite(shell, SWT.NONE);
			panel1.setLayout(new FillLayout());

			final List datas = new ArrayList();
			for (int i = 0; i < 20; i++) {
				datas.add("Item" + i);
			}

			final TableViewer tableViewer = new TableViewer(panel1);
			tableViewer.setContentProvider(new IStructuredContentProvider() {
				public void dispose() {
					// TODO Auto-generated method stub

				}

				public Object[] getElements(Object inputElement) {
					return datas.toArray();
				}

				public void inputChanged(Viewer viewer, Object oldInput,
						Object newInput) {

				}
			});
			tableViewer.setLabelProvider(new MyCSSLabelProvider(engine,
					tableViewer));

			Table table = tableViewer.getTable();
			table.setHeaderVisible(true);

			TableColumn tableColumn = new TableColumn(table, SWT.LEFT, 0);
			tableColumn.setText("Name");
			tableColumn.setWidth(200);
			table.setLinesVisible(true);

			tableViewer.setInput(datas);

			shell.pack();
			shell.open();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}

			display.dispose();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
