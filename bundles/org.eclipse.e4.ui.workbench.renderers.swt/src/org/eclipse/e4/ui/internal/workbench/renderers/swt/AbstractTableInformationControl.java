/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.renderers.swt;

import org.eclipse.e4.ui.workbench.swt.internal.copy.SearchPattern;
import org.eclipse.e4.ui.workbench.swt.internal.copy.WorkbenchSWTMessages;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.0
 */
public abstract class AbstractTableInformationControl {

	/**
	 * The NamePatternFilter selects the elements which match the given string
	 * patterns.
	 */
	protected class NamePatternFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			SearchPattern matcher = getMatcher();
			if (matcher == null || !(viewer instanceof TableViewer)) {
				return true;
			}
			TableViewer tableViewer = (TableViewer) viewer;

			String matchName = ((ILabelProvider) tableViewer.getLabelProvider())
					.getText(element);

			if (matchName == null) {
				return false;
			}
			// A dirty editor's label will start with dirty prefix, this prefix
			// should not be taken in consideration when matching with a pattern
			if (matchName.startsWith("*")) { //$NON-NLS-1$
				matchName = matchName.substring(1);
			}
			return matcher.matches(matchName);
		}
	}

	/** The control's shell */
	private Shell fShell;

	/** The composite */
	private Composite fComposite;

	/** The control's text widget */
	private Text fFilterText;

	/** The control's table widget */
	private TableViewer fTableViewer;

	/** The current search pattern */
	private SearchPattern fSearchPattern;

	/**
	 * Creates an information control with the given shell as parent. The given
	 * styles are applied to the shell and the table widget.
	 *
	 * @param parent
	 *            the parent shell
	 * @param shellStyle
	 *            the additional styles for the shell
	 * @param controlStyle
	 *            the additional styles for the control
	 */
	public AbstractTableInformationControl(Shell parent, int shellStyle,
			int controlStyle) {
		fShell = new Shell(parent, shellStyle);
		fShell.setLayout(new FillLayout());

		// Composite for filter text and viewer
		fComposite = new Composite(fShell, SWT.RESIZE);
		GridLayout layout = new GridLayout(1, false);
		fComposite.setLayout(layout);
		createFilterText(fComposite);

		fTableViewer = createTableViewer(fComposite, controlStyle);

		final Table table = fTableViewer.getTable();
		table.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
				case SWT.ESC:
					dispose();
					break;
				case SWT.DEL:
					removeSelectedItems();
					e.character = SWT.NONE;
					e.doit = false;
					break;
				case SWT.ARROW_UP:
					if (table.getSelectionIndex() == 0) {
						// on the first item, going up should grant focus to
						// text field
						fFilterText.setFocus();
					}
					break;
				case SWT.ARROW_DOWN:
					if (table.getSelectionIndex() == table.getItemCount() - 1) {
						// on the last item, going down should grant focus to
						// the text field
						fFilterText.setFocus();
					}
					break;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// do nothing
			}
		});

		table.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// do nothing;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				gotoSelectedElement();
			}
		});

		/*
		 * Bug in GTK, see SWT bug: 62405 Editor drop down performance slow on
		 * Linux-GTK on mouse move. Rather then removing the support altogether
		 * this feature has been worked around for GTK only as we expect that
		 * newer versions of GTK will no longer exhibit this quality and we will
		 * be able to have the desired support running on all platforms. See
		 * comment https://bugs.eclipse.org/bugs/show_bug.cgi?id=62405#c22 TODO:
		 * remove this code once bug 62405 is fixed for the mainstream GTK
		 * version
		 */
		final int ignoreEventCount = Util.isGtk() ? 4 : 1;

		table.addMouseMoveListener(new MouseMoveListener() {
			TableItem fLastItem = null;
			int lastY = 0;
			int itemHeightdiv4 = table.getItemHeight() / 4;
			int tableHeight = table.getBounds().height;
			Point tableLoc = table.toDisplay(0, 0);
			int divCount = 0;

			@Override
			public void mouseMove(MouseEvent e) {
				if (divCount == ignoreEventCount) {
					divCount = 0;
				}
				if (table.equals(e.getSource())
						& ++divCount == ignoreEventCount) {
					Object o = table.getItem(new Point(e.x, e.y));
					if (fLastItem == null ^ o == null) {
						table.setCursor(o == null ? null : table.getDisplay()
								.getSystemCursor(SWT.CURSOR_HAND));
					}
					if (o instanceof TableItem && lastY != e.y) {
						lastY = e.y;
						if (!o.equals(fLastItem)) {
							fLastItem = (TableItem) o;
							table.setSelection(new TableItem[] { fLastItem });
						} else if (e.y < itemHeightdiv4) {
							// Scroll up
							Item item = fTableViewer.scrollUp(e.x + tableLoc.x,
									e.y + tableLoc.y);
							if (item instanceof TableItem) {
								fLastItem = (TableItem) item;
								table.setSelection(new TableItem[] { fLastItem });
							}
						} else if (e.y > tableHeight - itemHeightdiv4) {
							// Scroll down
							Item item = fTableViewer.scrollDown(e.x
									+ tableLoc.x, e.y + tableLoc.y);
							if (item instanceof TableItem) {
								fLastItem = (TableItem) item;
								table.setSelection(new TableItem[] { fLastItem });
							}
						}
					} else if (o == null) {
						fLastItem = null;
					}
				}
			}
		});

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (table.getSelectionCount() < 1) {
					return;
				}

				if (e.button == 1) {
					if (table.equals(e.getSource())) {
						Object o = table.getItem(new Point(e.x, e.y));
						TableItem selection = table.getSelection()[0];
						if (selection.equals(o)) {
							gotoSelectedElement();
						}
					}
				}
				if (e.button == 3) {
					TableItem tItem = fTableViewer.getTable().getItem(
							new Point(e.x, e.y));
					if (tItem != null) {
						Menu menu = new Menu(fTableViewer.getTable());
						MenuItem mItem = new MenuItem(menu, SWT.NONE);
						mItem.setText(SWTRenderersMessages.menuClose);
						mItem.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(
									SelectionEvent selectionEvent) {
								removeSelectedItems();
							}
						});
						menu.setVisible(true);
					}
				}
			}
		});

		fShell.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				switch (e.detail) {
				case SWT.TRAVERSE_PAGE_NEXT:
					e.detail = SWT.TRAVERSE_NONE;
					e.doit = true;
					{
						int n = table.getItemCount();
						if (n == 0)
							return;

						int i = table.getSelectionIndex() + 1;
						if (i >= n)
							i = 0;
						table.setSelection(i);
					}
					break;

				case SWT.TRAVERSE_PAGE_PREVIOUS:
					e.detail = SWT.TRAVERSE_NONE;
					e.doit = true;
					{
						int n = table.getItemCount();
						if (n == 0)
							return;

						int i = table.getSelectionIndex() - 1;
						if (i < 0)
							i = n - 1;
						table.setSelection(i);
					}
					break;
				}
			}
		});

		setInfoSystemColor();
		installFilter();
	}

	/**
	 * Removes the selected items from the list and closes their corresponding
	 * tabs Selects the next item in the list or disposes it if its presentation
	 * is disposed
	 */
	protected void removeSelectedItems() {
		int selInd = fTableViewer.getTable().getSelectionIndex();
		if (deleteSelectedElements()) {
			return;
		}
		fTableViewer.refresh();
		if (selInd >= fTableViewer.getTable().getItemCount()) {
			selInd = fTableViewer.getTable().getItemCount() - 1;
		}
		if (selInd >= 0) {
			fTableViewer.getTable().setSelection(selInd);
		}
	}

	protected abstract TableViewer createTableViewer(Composite parent, int style);

	public TableViewer getTableViewer() {
		return fTableViewer;
	}

	protected Text createFilterText(Composite parent) {
		fFilterText = new Text(parent, SWT.NONE);

		GridData data = new GridData();
		GC gc = new GC(parent);
		gc.setFont(parent.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();

		data.heightHint = org.eclipse.jface.dialogs.Dialog
				.convertHeightInCharsToPixels(fontMetrics, 1);
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		fFilterText.setLayoutData(data);

		fFilterText.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
				case SWT.CR:
				case SWT.KEYPAD_CR:
					gotoSelectedElement();
					break;
				case SWT.ARROW_DOWN:
					fTableViewer.getTable().setFocus();
					fTableViewer.getTable().setSelection(0);
					break;
				case SWT.ARROW_UP:
					fTableViewer.getTable().setFocus();
					fTableViewer.getTable().setSelection(
							fTableViewer.getTable().getItemCount() - 1);
					break;
				case SWT.ESC:
					dispose();
					break;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// do nothing
			}
		});

		// Horizontal separator line
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return fFilterText;
	}

	private void setInfoSystemColor() {
		Display display = fShell.getDisplay();
		setForegroundColor(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		setBackgroundColor(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
	}

	private void installFilter() {
		fFilterText.setMessage(WorkbenchSWTMessages.FilteredTree_FilterMessage);
		fFilterText.setText(""); //$NON-NLS-1$

		fFilterText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String text = ((Text) e.widget).getText();
				setMatcherString(text);
			}
		});
	}

	/**
	 * The string matcher has been modified. The default implementation
	 * refreshes the view and selects the first matched element
	 */
	private void stringMatcherUpdated() {
		// refresh viewer to refilter
		fTableViewer.getControl().setRedraw(false);
		fTableViewer.refresh();
		selectFirstMatch();
		fTableViewer.getControl().setRedraw(true);
	}

	/**
	 * Sets the patterns to filter out for the receiver.
	 * <p>
	 * The following characters have special meaning: ? => any character * =>
	 * any string
	 * </p>
	 */
	private void setMatcherString(String pattern) {
		if (pattern.length() == 0) {
			fSearchPattern = null;
		} else {
			SearchPattern patternMatcher = new SearchPattern();
			patternMatcher.setPattern(pattern);
			fSearchPattern = patternMatcher;
		}
		stringMatcherUpdated();
	}

	private SearchPattern getMatcher() {
		return fSearchPattern;
	}

	/**
	 * Implementers can modify
	 */
	protected Object getSelectedElement() {
		return ((IStructuredSelection) fTableViewer.getSelection())
				.getFirstElement();
	}

	protected abstract void gotoSelectedElement();

	/**
	 * Delete all selected elements.
	 *
	 * @return <code>true</code> if there are no elements left after deletion.
	 */
	protected abstract boolean deleteSelectedElements();

	/**
	 * Selects the first element in the table which matches the current filter
	 * pattern.
	 */
	protected void selectFirstMatch() {
		Table table = fTableViewer.getTable();
		Object element = findElement(table.getItems());
		if (element != null) {
			fTableViewer.setSelection(new StructuredSelection(element), true);
		} else {
			fTableViewer.setSelection(StructuredSelection.EMPTY);
		}
	}

	private Object findElement(TableItem[] items) {
		ILabelProvider labelProvider = (ILabelProvider) fTableViewer
				.getLabelProvider();
		for (int i = 0; i < items.length; i++) {
			Object element = items[i].getData();
			if (fSearchPattern == null) {
				return element;
			}

			if (element != null) {
				String label = labelProvider.getText(element);
				if (label == null) {
					return null;
				}
				// remove the dirty prefix from the editor's label
				if (label.startsWith("*")) { //$NON-NLS-1$
					label = label.substring(1);
				}
				if (fSearchPattern.matches(label)) {
					return element;
				}
			}
		}
		return null;
	}

	public void setVisible(boolean visible) {
		fShell.setVisible(visible);
	}

	public void dispose() {
		if (fShell != null) {
			if (!fShell.isDisposed()) {
				fShell.dispose();
			}
			fShell = null;
			fTableViewer = null;
			fComposite = null;
			fFilterText = null;
		}
	}

	public Point computeSizeHint() {
		// Resize the table's height accordingly to the new input
		Table viewerTable = fTableViewer.getTable();
		Point tableSize = viewerTable.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int tableMaxHeight = fComposite.getDisplay().getBounds().height / 2;
		// removes padding if necessary
		int tableHeight = (tableSize.y <= tableMaxHeight) ? tableSize.y
				- viewerTable.getItemHeight() - viewerTable.getItemHeight() / 2
				: tableMaxHeight;
		((GridData) viewerTable.getLayoutData()).heightHint = tableHeight;
		Point fCompSize = fComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		fComposite.setSize(fCompSize);
		return fCompSize;
	}

	public void setLocation(Point location) {
		Rectangle trim = fShell.computeTrim(0, 0, 0, 0);
		Point textLocation = fComposite.getLocation();
		location.x += trim.x - textLocation.x;
		location.y += trim.y - textLocation.y;
		fShell.setLocation(location);
	}

	public void setSize(int width, int height) {
		fShell.setSize(width, height);
	}

	public Shell getShell() {
		return fShell;
	}

	private void setForegroundColor(Color foreground) {
		fTableViewer.getTable().setForeground(foreground);
		fFilterText.setForeground(foreground);
		fComposite.setForeground(foreground);
	}

	private void setBackgroundColor(Color background) {
		fTableViewer.getTable().setBackground(background);
		fFilterText.setBackground(background);
		fComposite.setBackground(background);
	}

	public void setFocus() {
		fShell.forceFocus();
		fFilterText.setFocus();
	}
}
