package org.eclipse.ui.internal.findandreplace.overlay;

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

import org.eclipse.ui.internal.findandreplace.HistoryStore;

/**
 * Menu dropdown for the search history in the find/replace overlay
 */
class SearchHistoryMenu extends Dialog {
	private final SelectionListener selectionListener;
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

	public SearchHistoryMenu(Shell parent, HistoryStore history, SelectionListener menuItemSelectionListener) {
		super(parent);
		setShellStyle(SWT.NONE);
		setBlockOnOpen(false);

		this.selectionListener = menuItemSelectionListener;
		this.history = history;
	}

	public void setPosition(int x, int y, int width) {
		location = new Point(x, y);
		this.width = width;
	}

	@Override
	public Control createContents(Composite parent) {
		Table table = new Table(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(table);
		TableColumn column = new TableColumn(table, SWT.NONE);

		for (String entry : history.get()) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(entry);
		}

		table.addSelectionListener(selectionListener);
		table.addMouseListener(MouseListener.mouseDownAdapter(e -> {
			table.notifyListeners(SWT.Selection, null);
			close();
		}));
		table.addKeyListener(KeyListener.keyPressedAdapter(e -> {
			if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
				close();
			}
		}));
		getShell().layout();

		positionShell(table, column);
		return table;
	}

	private void positionShell(Table table, TableColumn column) {
		if (location != null) {
			getShell().setBounds(location.x, location.y, width,
					Math.min(table.getItemHeight() * 7, table.getItemHeight() * (table.getItemCount() + 2)));
		}
		int columnSize = table.getSize().x;
		if (table.getVerticalBar() != null) {
			columnSize -= table.getVerticalBar().getSize().x;
		}
		column.setWidth(columnSize);
	}

	@Override
	public int open() {
		int code = super.open();

		getShell().addShellListener(shellFocusListener);

		return code;
	}
}
