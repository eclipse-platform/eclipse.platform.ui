/*******************************************************************************
 * Copyright (c) 2003, 2018 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.renderers.swt;

import org.eclipse.e4.ui.workbench.swt.internal.copy.SearchPattern;
import org.eclipse.e4.ui.workbench.swt.internal.copy.WorkbenchSWTMessages;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
	 * True if the focus is still in one of the control elements.
	 *
	 * @return
	 */
	public boolean hasFocus() {
		if (fShell == null || fShell.isDisposed()) {
			return false;
		}
		// check if the focus is still in dialog elements
		Control fc = fShell.getDisplay().getFocusControl();
		return fc == fFilterText || fc == fTableViewer.getTable() || fc == fComposite || fc == fShell;
	}

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
		table.addKeyListener(KeyListener.keyPressedAdapter(e -> {
			switch (e.keyCode) {
			case SWT.ESC:
				dispose();
				break;
			case SWT.DEL:
				removeSelectedItem(null);
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
		}));

		table.addSelectionListener(SelectionListener.widgetDefaultSelectedAdapter(e -> gotoSelectedElement()));

		final int ignoreEventCount = 1;

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
					TableItem tableItem = table.getItem(new Point(e.x, e.y));
					if (fLastItem == null ^ tableItem == null) {
						table.setCursor(tableItem == null ? null
								: table.getDisplay()
								.getSystemCursor(SWT.CURSOR_HAND));
					}
					if (tableItem != null && lastY != e.y) {
						lastY = e.y;
						if (!tableItem.equals(fLastItem)) {
							fLastItem = tableItem;
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
					} else if (tableItem == null) {
						fLastItem = null;
					}
				}
			}
		});

		table.addMouseListener(MouseListener.mouseUpAdapter(e -> {
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
				TableItem tItem = fTableViewer.getTable().getItem(new Point(e.x, e.y));
				if (tItem != null) {
					Menu menu = new Menu(fTableViewer.getTable());
					MenuItem mItem = new MenuItem(menu, SWT.NONE);
					mItem.setText(SWTRenderersMessages.menuClose);
					mItem.addSelectionListener(SelectionListener
							.widgetSelectedAdapter(selectionEvent -> removeSelectedItem(tItem.getData())));
					menu.setVisible(true);
				}
			}
		}));

		fShell.addTraverseListener(e -> {
			switch (e.detail) {
			case SWT.TRAVERSE_PAGE_NEXT:
				e.detail = SWT.TRAVERSE_NONE;
				e.doit = true;
				{
					int n1 = table.getItemCount();
					if (n1 == 0)
						return;

					int i1 = table.getSelectionIndex() + 1;
					if (i1 >= n1)
						i1 = 0;
					table.setSelection(i1);
				}
				break;

			case SWT.TRAVERSE_PAGE_PREVIOUS:
				e.detail = SWT.TRAVERSE_NONE;
				e.doit = true;
				{
					int n2 = table.getItemCount();
					if (n2 == 0)
						return;

					int i2 = table.getSelectionIndex() - 1;
					if (i2 < 0)
						i2 = n2 - 1;
					table.setSelection(i2);
				}
				break;
			}
		});

		setInfoSystemColor();
		installFilter();
	}

	/**
	 * Removes the given selected item from the list and closes corresponding tab.
	 * Selects the next item in the list or disposes it if its presentation is
	 * disposed.
	 *
	 * @param selected
	 *            can be {@code null} in this case current selection should be used
	 */
	protected void removeSelectedItem(Object selected) {
		int selInd = fTableViewer.getTable().getSelectionIndex();
		if (deleteSelectedElement(selected)) {
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

		fFilterText.addKeyListener(KeyListener.keyPressedAdapter(e -> {
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
				fTableViewer.getTable().setSelection(fTableViewer.getTable().getItemCount() - 1);
				break;
			case SWT.ESC:
				dispose();
				break;
			}
		}));

		// Horizontal separator line
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return fFilterText;
	}

	private void setInfoSystemColor() {
		ColorRegistry colorRegistry = JFaceResources.getColorRegistry();

		Color foreground = colorRegistry.get(JFacePreferences.INFORMATION_FOREGROUND_COLOR);
		if (foreground == null) {
			foreground = JFaceColors.getInformationViewerForegroundColor(fShell.getDisplay());
		}

		Color background = colorRegistry.get(JFacePreferences.INFORMATION_BACKGROUND_COLOR);
		if (background == null) {
			background = JFaceColors.getInformationViewerBackgroundColor(fShell.getDisplay());
		}

		setForegroundColor(foreground);
		setBackgroundColor(background);
	}

	private void installFilter() {
		fFilterText.setMessage(WorkbenchSWTMessages.FilteredTree_FilterMessage);
		fFilterText.setText(""); //$NON-NLS-1$

		fFilterText.addModifyListener(e -> {
			String text = ((Text) e.widget).getText();
			setMatcherString(text);
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
		return fTableViewer.getStructuredSelection().getFirstElement();
	}

	protected abstract void gotoSelectedElement();

	/**
	 * Delete given selected element.
	 *
	 * @param element
	 *            can be {@code null} in this case current selection should be used
	 *
	 * @return <code>true</code> if there are no elements left after deletion.
	 */
	protected abstract boolean deleteSelectedElement(Object element);

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
		ILabelProvider labelProvider = (ILabelProvider) fTableViewer.getLabelProvider();
		for (TableItem item : items) {
			Object element = item.getData();
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
		fShell.forceActive();
		fShell.forceFocus();
		fFilterText.setFocus();
	}
}
