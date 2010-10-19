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
package org.eclipse.e4.core.internal.contexts.debug.ui;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

// Normal - locally defined simple data
// Bold - locally defined computed functions
// Italic - locally cached values for the computed functions

public class ContextData {

	private final static String EMPTY_STRING = ""; //$NON-NLS-1$

	static private class ContextDataElement {

		public enum DataType {
			NORMAL, CALCULATED, CALCULATED_INHERITED
		}

		private String key;
		private Object value;
		private DataType type;

		public ContextDataElement(String key, Object value, DataType type) {
			this.key = key;
			this.value = value;
			this.type = type;
		}

		public String getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}

		public DataType getType() {
			return type;
		}
	}

	private class ContextDataContentProvider implements ITreeContentProvider {

		private EclipseContext selectedContext;

		public ContextDataContentProvider() {
			// placeholder
		}

		public void dispose() {
			selectedContext = null;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			selectedContext = (EclipseContext) newInput;
		}

		public Object[] getElements(Object inputElement) {
			if (selectedContext == null)
				return new Object[0];
			Map<String, Object> localData = selectedContext.localData();
			Map<String, Object> localContextFunction = selectedContext.localContextFunction();
			Map<String, Object> cachedCachedContextFunctions = selectedContext.cachedCachedContextFunctions();

			int size = localData.size() + localContextFunction.size() + cachedCachedContextFunctions.size();
			Set<ContextDataElement> result = new HashSet<ContextDataElement>(size);
			for (String key : localData.keySet()) {
				result.add(new ContextDataElement(key, localData.get(key), ContextDataElement.DataType.NORMAL));
			}
			if (showFunctions) {
				for (String key : localContextFunction.keySet()) {
					result.add(new ContextDataElement(key, localContextFunction.get(key), ContextDataElement.DataType.CALCULATED));
				}
			}
			if (showCached) {
				for (String key : cachedCachedContextFunctions.keySet()) {
					result.add(new ContextDataElement(key, cachedCachedContextFunctions.get(key), ContextDataElement.DataType.CALCULATED_INHERITED));
				}
			}
			return result.toArray();
		}

		public Object[] getChildren(Object parentElement) {
			return null;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return false;
		}
	}

	private class ContextDataLabelProvider extends LabelProvider implements ITableLabelProvider, ITableFontProvider {

		public ContextDataLabelProvider() {
			// placeholder
		}

		public String getColumnText(Object obj, int index) {
			if (obj == null)
				return null;
			if (!(obj instanceof ContextDataElement))
				return obj.toString();
			switch (index) {
				case 0 :
					return ((ContextDataElement) obj).getKey();
				case 1 :
					Object value = ((ContextDataElement) obj).getValue();
					if (value == null)
						return EMPTY_STRING;
					return value.toString();
				default :
					return obj.toString();
			}
		}

		public String getText(Object element) {
			return getColumnText(element, 0);
		}

		public Image getColumnImage(Object obj, int index) {
			return null;
		}

		public Image getImage(Object obj) {
			return null;
		}

		public Font getFont(Object element, int columnIndex) {
			ContextDataElement.DataType type = ((ContextDataElement) element).getType();
			switch (type) {
				case NORMAL :
					return null;
				case CALCULATED : {
					return getBold();
				}
				case CALCULATED_INHERITED : {
					return getItalic();
				}
			}
			return null;
		}
	}

	static private class ContextDataComparator extends ViewerComparator {
		final ContextDataLabelProvider labelProvider;
		private LinkedList<Integer> sortColumns = new LinkedList<Integer>();
		private boolean ascending = true;

		public ContextDataComparator(ContextDataLabelProvider labelProvider) {
			this.labelProvider = labelProvider;
			for (int i = 0; i < NUM_OF_COLUMNS; i++) {
				sortColumns.add(i);
			}
		}

		public final int compare(final Viewer viewer, final Object a, final Object b) {
			int result = 0;
			for (int column : sortColumns) {
				String labelA = labelProvider.getColumnText(a, column);
				String labelB = labelProvider.getColumnText(b, column);
				if (labelA == null || labelB == null)
					continue;
				result = getComparator().compare(labelA, labelB);
				if (result != 0)
					break;
			}
			return (ascending) ? result : -result;
		}

		public int getSortColumn() {
			return sortColumns.getFirst();
		}

		public void setSortColumn(int column) {
			if (column == getSortColumn())
				return;
			sortColumns.remove(column);
			sortColumns.addFirst(column);
		}

		public boolean isAscending() {
			return ascending;
		}

		public void setAscending(boolean ascending) {
			this.ascending = ascending;
		}
	}

	private final class SortColumn extends SelectionAdapter {
		private final ContextDataComparator comparator;
		private final TreeColumn treeColumn;
		private final TreeViewer viewer;
		private final int column;

		public SortColumn(ContextDataComparator comparator, TreeColumn treeColumn, TreeViewer viewer, int column) {
			this.comparator = comparator;
			this.treeColumn = treeColumn;
			this.viewer = viewer;
			this.column = column;
		}

		public void widgetSelected(SelectionEvent e) {
			if (comparator.getSortColumn() == column) {
				comparator.setAscending(!comparator.isAscending());
				viewer.getTree().setSortDirection(comparator.isAscending() ? SWT.UP : SWT.DOWN);
			} else {
				viewer.getTree().setSortColumn(treeColumn);
				comparator.setSortColumn(column);
			}
			try {
				viewer.getTree().setRedraw(false);
				viewer.refresh();
			} finally {
				viewer.getTree().setRedraw(true);
			}
		}
	}

	static protected int NUM_OF_COLUMNS = 2;
	static protected int CONTEXT_DATA_KEY_COLUMN = 0;
	static protected int CONTEXT_DATA_VALUE_COLUMN = 1;

	final private TabFolder folder;
	protected TreeViewer dataViewer;
	private TabItem tabData;

	protected Button showFunctionsButton;
	protected Button showCachedButton;

	protected boolean showFunctions;
	protected boolean showCached;

	private FontRegistry registry;
	private Font bold;
	private Font italic;

	public ContextData(TabFolder folder) {
		this.folder = folder;
		registry = new FontRegistry();
	}

	public TreeViewer createControls() {
		fillFontCache();

		tabData = new TabItem(folder, SWT.NONE, 0);
		tabData.setText(ContextMessages.dataTab);
		Composite pageData = new Composite(folder, SWT.NONE);
		tabData.setControl(pageData);

		GridLayout rightPaneLayout = new GridLayout();
		rightPaneLayout.marginHeight = 0;
		rightPaneLayout.marginWidth = 0;
		pageData.setLayout(rightPaneLayout);

		FilteredTree dataTree = new FilteredTree(pageData, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL, new PatternFilter(), true);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		dataTree.setLayoutData(gridData);
		dataViewer = dataTree.getViewer();

		ContextDataLabelProvider labelProvider = new ContextDataLabelProvider();
		ContextDataContentProvider contentProvider = new ContextDataContentProvider();
		ContextDataComparator comparator = new ContextDataComparator(labelProvider);
		dataViewer.setComparator(comparator);

		final Tree tree = dataViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		final TreeColumn keyColumn = new TreeColumn(tree, SWT.LEFT);
		keyColumn.setText(ContextMessages.keyColumn);
		tree.setSortColumn(keyColumn);
		tree.setSortDirection(comparator.isAscending() ? SWT.UP : SWT.DOWN);
		keyColumn.addSelectionListener(new SortColumn(comparator, keyColumn, dataViewer, CONTEXT_DATA_KEY_COLUMN));

		final TreeColumn dataColumn = new TreeColumn(tree, SWT.LEFT);
		dataColumn.setText(ContextMessages.valueColumn);
		dataColumn.addSelectionListener(new SortColumn(comparator, dataColumn, dataViewer, CONTEXT_DATA_VALUE_COLUMN));

		dataViewer.setContentProvider(contentProvider);
		dataViewer.setLabelProvider(labelProvider);

		dataTree.getPatternFilter().setIncludeLeadingWildcard(true);

		final TreeColumn[] columns = dataViewer.getTree().getColumns();
		columns[CONTEXT_DATA_KEY_COLUMN].setWidth(150);
		columns[CONTEXT_DATA_VALUE_COLUMN].setWidth(150);

		showFunctionsButton = new Button(pageData, SWT.CHECK);
		showFunctionsButton.setFont(getBold());
		showFunctionsButton.setText(ContextMessages.showFunctions);
		showFunctionsButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				showFunctions = showFunctionsButton.getSelection();
				dataViewer.refresh();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		showFunctionsButton.setSelection(true);
		showFunctions = true;

		showCachedButton = new Button(pageData, SWT.CHECK);
		showCachedButton.setFont(getItalic());
		showCachedButton.setText(ContextMessages.showCached);
		showCachedButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				showCached = showCachedButton.getSelection();
				dataViewer.refresh();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		showCachedButton.setSelection(true);
		showCached = true;

		return dataViewer;
	}

	protected void fillFontCache() {
		FontData[] fontData = Display.getCurrent().getSystemFont().getFontData();
		String fontName = fontData[0].getName();
		// TBD use FontRegistry's default font and add a listener in case it changes
		bold = registry.getBold(fontName);
		italic = registry.getItalic(fontName);
	}

	protected Font getBold() {
		return bold;
	}

	protected Font getItalic() {
		return italic;
	}

}
