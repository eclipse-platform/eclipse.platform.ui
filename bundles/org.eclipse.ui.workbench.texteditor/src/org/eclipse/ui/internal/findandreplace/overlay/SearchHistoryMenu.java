/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace.overlay;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;

import org.eclipse.ui.internal.findandreplace.FindReplaceMessages;
import org.eclipse.ui.internal.findandreplace.HistoryStore;

/**
 * Menu dropdown for the search history in the find/replace overlay
 */
class SearchHistoryMenu extends Dialog {
	private final Consumer<String> historyEntrySelectedCallback;
	private final HistoryStore history;
	private final ShellListener shellFocusListener = new ShellAdapter() {
		@Override
		public void shellDeactivated(ShellEvent e) {
			if (!getShell().isDisposed()) {
				getShell().getDisplay().asyncExec(SearchHistoryMenu.this::close);
			}
		}
	};
	private Point location;
	private int width;
	private Table table;
	private TableColumn column;
	private int selectedIndexInTable = -1;

	public SearchHistoryMenu(Shell parent, HistoryStore history, Consumer<String> historyEntrySelectedCallback) {
		super(parent);
		setShellStyle(SWT.NONE);
		setBlockOnOpen(false);

		this.historyEntrySelectedCallback = historyEntrySelectedCallback;
		this.history = history;
	}

	public void setPosition(int x, int y, int width) {
		location = new Point(x, y);
		this.width = width;
	}

	@Override
	public Control createContents(Composite parent) {
		table = new Table(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(table);
		column = new TableColumn(table, SWT.NONE);

		if (history.size() == 0) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(FindReplaceMessages.SearchHistoryMenu_SEARCH_HISTORY_EMPTY_STRING);
			table.setEnabled(false);
		} else {
			for (String entry : history.get()) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(entry);
			}
		}

		attachTableListeners();
		getShell().pack();
		getShell().layout();
		return table;
	}

	private void moveSelectionInTable(int indexShift) {
		selectedIndexInTable += indexShift;
		if (selectedIndexInTable < 0) {
			selectedIndexInTable = table.getItemCount() - 1;
		} else if (selectedIndexInTable > table.getItemCount() - 1) {
			selectedIndexInTable = 0;
		}
		table.setSelection(selectedIndexInTable);
		historyEntrySelectedCallback.accept(table.getSelection()[0].getText());
	}

	private void attachTableListeners() {
		table.addListener(SWT.MouseMove, event -> {
			Point point = new Point(event.x, event.y);
			TableItem item = table.getItem(point);
			if (item != null) {
				table.setSelection(item);
				selectedIndexInTable = table.getSelectionIndex();
			}
		});
		table.addKeyListener(KeyListener.keyPressedAdapter(e -> {
			if (e.keyCode == SWT.ARROW_DOWN) {
				moveSelectionInTable(1);
				e.doit = false;
			} else if (e.keyCode == SWT.ARROW_UP) {
				moveSelectionInTable(-1);
				e.doit = false;
			} else if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
				notifyParentOfSelectionInput();
				close();
			}
		}));
		table.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			notifyParentOfSelectionInput();
		}));
		table.addMouseListener(MouseListener.mouseDownAdapter(e -> {
			table.notifyListeners(SWT.Selection, null);
			close();
		}));
	}

	private void notifyParentOfSelectionInput() {
		TableItem[] selection = table.getSelection();
		if (selection.length == 0) {
			historyEntrySelectedCallback.accept(null);
			return;
		}
		String text = selection[0].getText();
		if (text != null) {
			historyEntrySelectedCallback.accept(text);
		}
		historyEntrySelectedCallback.accept(null);
	}

	private void positionShell() {
		if (location != null && table.getItemCount() != 0) {
			getShell().setBounds(location.x, location.y, width,
					Math.min(table.getItemHeight() * 7, table.getItemHeight() * table.getItemCount() + 2));
		}
		int columnWidth = table.getSize().x;
		if (table.getVerticalBar() != null && table.getVerticalBar().isVisible()) {
			columnWidth = table.getSize().x - table.getVerticalBar().getSize().x;
		}
		column.setWidth(columnWidth);
	}

	@Override
	public int open() {
		int code = super.open();

		getShell().addShellListener(shellFocusListener);
		positionShell();

		return code;
	}
}
